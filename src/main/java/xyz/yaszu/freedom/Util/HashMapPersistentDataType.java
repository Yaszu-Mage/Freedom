package xyz.yaszu.freedom.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A stateless PersistentDataType for storing and retrieving {@code HashMap<String, Object>}.
 *
 * <p>Supported value types:</p>
 * <ul>
 *   <li>{@link String}</li>
 *   <li>{@link Integer}</li>
 *   <li>{@link Long}</li>
 *   <li>{@link Double}</li>
 *   <li>{@link Boolean}</li>
 *   <li>{@link Location} (world by UUID; pitch/yaw included)</li>
 *   <li>{@link ItemStack} (via {@code serializeAsBytes} / {@code deserializeBytes})</li>
 * </ul>
 *
 * <p>Uses the same v1/v2 dual-format detection as {@code InventoryPersistentDataType}:
 * the first byte {@code 0xAC} signals a legacy ObjectOutputStream stream (v1); anything
 * else is treated as the lightweight DataOutputStream format (v2).</p>
 *
 * <h3>Writing:</h3>
 * <pre>{@code
 *   Map<String, Object> data = new HashMap<>();
 *   data.put("home",    someLocation);
 *   data.put("kills",   42);
 *   data.put("weapon",  someItemStack);
 *
 *   SaveContext ctx = new SaveContext(playerUUID, "player-data");
 *   pdc.set(key, HashMapPersistentDataType.get(ctx), data);
 * }</pre>
 *
 * <h3>Reading:</h3>
 * <pre>{@code
 *   LoadResult result = HashMapPersistentDataType.load(pdc, key);
 *   if (result != null) {
 *       Map<String, Object> map   = result.map();
 *       UUID                owner = result.holderUUID(); // may be null
 *       String              label = result.label();      // may be null
 *
 *       Location home  = (Location)  map.get("home");
 *       int      kills = (Integer)   map.get("kills");
 *       ItemStack w    = (ItemStack) map.get("weapon");
 *   }
 * }</pre>
 */
public final class HashMapPersistentDataType
        implements PersistentDataType<byte[], Map<String, Object>> {

    // -------------------------------------------------------------------------
    // Type tags written before each map value
    // -------------------------------------------------------------------------

    private static final byte TAG_STRING    = 1;
    private static final byte TAG_INTEGER   = 2;
    private static final byte TAG_LONG      = 3;
    private static final byte TAG_DOUBLE    = 4;
    private static final byte TAG_BOOLEAN   = 5;
    private static final byte TAG_LOCATION  = 6;
    private static final byte TAG_ITEMSTACK = 7;

    // -------------------------------------------------------------------------
    // Public API types
    // -------------------------------------------------------------------------

    /**
     * Optional metadata embedded alongside the map when saving.
     *
     * @param holderUUID owning player UUID, or {@code null}
     * @param label      arbitrary label / title string, or {@code null}
     */
    public record SaveContext(UUID holderUUID, String label) {}

    /** The deserialized map together with any metadata embedded at save time. */
    public record LoadResult(Map<String, Object> map, UUID holderUUID, String label) {}

    // -------------------------------------------------------------------------
    // Factory / convenience
    // -------------------------------------------------------------------------

    private static final HashMapPersistentDataType NO_CONTEXT =
            new HashMapPersistentDataType(null);

    /**
     * Returns a type instance that embeds {@code context} during serialization.
     * Create it inline; do not cache instances that carry a context.
     */
    public static HashMapPersistentDataType get(SaveContext context) {
        return new HashMapPersistentDataType(context);
    }

    /** Returns a shared, context-free instance suitable for reading. */
    public static HashMapPersistentDataType get() {
        return NO_CONTEXT;
    }

    /**
     * Convenience: read directly from a {@link PersistentDataContainer}.
     *
     * @return {@code null} if the key is absent
     */
    public static LoadResult load(PersistentDataContainer pdc, NamespacedKey key) {
        InternalResult r = pdc.get(key, INTERNAL_TYPE);
        if (r == null) return null;
        return new LoadResult(r.map(), r.holderUUID(), r.label());
    }

    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    /**
     * First byte of every v2 payload.
     * {@code 0xAC} is the ObjectOutputStream magic byte, so it unambiguously
     * identifies v1 (legacy) data.
     */
    private static final byte FORMAT_VERSION = 2;

    private final SaveContext context;

    private HashMapPersistentDataType(SaveContext context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull Class<Map<String, Object>> getComplexType() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }

    @Override
    public @NonNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    // ------------------------------------------------------------------
    // Serialization  (v2 – DataOutputStream, no OOS framing)
    // ------------------------------------------------------------------

    @Override
    public byte @NonNull [] toPrimitive(@NonNull Map<String, Object> map,
                                        @NonNull PersistentDataAdapterContext ctx) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(FORMAT_VERSION);

            // --- optional holder UUID ---
            UUID uid = (context != null) ? context.holderUUID() : null;
            dos.writeBoolean(uid != null);
            if (uid != null) {
                dos.writeLong(uid.getMostSignificantBits());
                dos.writeLong(uid.getLeastSignificantBits());
            }

            // --- optional label ---
            String label = (context != null) ? context.label() : null;
            dos.writeBoolean(label != null);
            if (label != null) {
                dos.writeUTF(label);
            }

            // --- entries ---
            dos.writeInt(map.size());
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                dos.writeUTF(entry.getKey());
                writeValue(dos, entry.getValue());
            }

            dos.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize HashMap", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull Map<String, Object> fromPrimitive(byte @NonNull [] bytes,
                                                      @NonNull PersistentDataAdapterContext ctx) {
        return INTERNAL_TYPE.fromPrimitive(bytes, ctx).map();
    }

    // -------------------------------------------------------------------------
    // Value serialization helpers
    // -------------------------------------------------------------------------

    /**
     * Writes a type tag followed by the value's bytes.
     *
     * @throws IllegalArgumentException if the value type is not supported
     */
    private static void writeValue(DataOutputStream dos, Object value) throws IOException {
        if (value instanceof String v) {
            dos.writeByte(TAG_STRING);
            dos.writeUTF(v);

        } else if (value instanceof Integer v) {
            dos.writeByte(TAG_INTEGER);
            dos.writeInt(v);

        } else if (value instanceof Long v) {
            dos.writeByte(TAG_LONG);
            dos.writeLong(v);

        } else if (value instanceof Double v) {
            dos.writeByte(TAG_DOUBLE);
            dos.writeDouble(v);

        } else if (value instanceof Boolean v) {
            dos.writeByte(TAG_BOOLEAN);
            dos.writeBoolean(v);

        } else if (value instanceof Location v) {
            dos.writeByte(TAG_LOCATION);
            writeLocation(dos, v);

        } else if (value instanceof ItemStack v) {
            dos.writeByte(TAG_ITEMSTACK);
            byte[] itemData = v.serializeAsBytes();
            dos.writeInt(itemData.length);
            dos.write(itemData);

        } else {
            throw new IllegalArgumentException(
                    "Unsupported map value type: " +
                            (value == null ? "null" : value.getClass().getName()) +
                            ". Supported: String, Integer, Long, Double, Boolean, Location, ItemStack");
        }
    }

    /**
     * Writes a {@link Location}: world UUID (nullable), x, y, z, yaw, pitch.
     */
    private static void writeLocation(DataOutputStream dos, Location loc) throws IOException {
        World world = loc.getWorld();
        dos.writeBoolean(world != null);
        if (world != null) {
            dos.writeLong(world.getUID().getMostSignificantBits());
            dos.writeLong(world.getUID().getLeastSignificantBits());
        }
        dos.writeDouble(loc.getX());
        dos.writeDouble(loc.getY());
        dos.writeDouble(loc.getZ());
        dos.writeFloat(loc.getYaw());
        dos.writeFloat(loc.getPitch());
    }

    /**
     * Reads a {@link Location} written by {@link #writeLocation}.
     * If the world UUID is present but the world is not loaded, the Location's
     * world will be {@code null}.
     */
    private static Location readLocation(DataInputStream dis) throws IOException {
        World world = null;
        if (dis.readBoolean()) {
            UUID worldUUID = new UUID(dis.readLong(), dis.readLong());
            world = Bukkit.getWorld(worldUUID); // null if not loaded
        }
        double x     = dis.readDouble();
        double y     = dis.readDouble();
        double z     = dis.readDouble();
        float  yaw   = dis.readFloat();
        float  pitch = dis.readFloat();
        return new Location(world, x, y, z, yaw, pitch);
    }

    // -------------------------------------------------------------------------
    // Internal deserializer (surfaces metadata for load())
    // -------------------------------------------------------------------------

    private static final PersistentDataType<byte[], InternalResult> INTERNAL_TYPE =
            new PersistentDataType<>() {
                @Override public @NonNull Class<byte[]>         getPrimitiveType() { return byte[].class; }
                @Override public @NonNull Class<InternalResult> getComplexType()   { return InternalResult.class; }

                @Override
                public byte @NonNull [] toPrimitive(@NonNull InternalResult value,
                                                    @NonNull PersistentDataAdapterContext ctx) {
                    throw new UnsupportedOperationException(
                            "Use HashMapPersistentDataType for writing");
                }

                @Override
                public @NonNull InternalResult fromPrimitive(byte @NonNull [] bytes,
                                                             @NonNull PersistentDataAdapterContext ctx) {
                    // 0xAC = ObjectOutputStream magic → legacy v1
                    if ((bytes[0] & 0xFF) == 0xAC) {
                        try {
                            return readV1Legacy(bytes);
                        } catch (Exception e) {
                            throw new RuntimeException(
                                    "Failed to deserialize legacy HashMap (v1): " + e.getMessage(), e);
                        }
                    } else {
                        try {
                            return readV2(bytes);
                        } catch (Exception e) {
                            throw new RuntimeException(
                                    "Failed to deserialize HashMap (v2): " + e.getMessage(), e);
                        }
                    }
                }
            };

    // ------------------------------------------------------------------
    // v2 reader
    // ------------------------------------------------------------------

    private static InternalResult readV2(byte[] bytes) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {

            byte version = dis.readByte();
            if (version != FORMAT_VERSION) {
                throw new IOException(
                        "Unexpected version byte: " + version + " (expected " + FORMAT_VERSION + ")");
            }

            UUID holderUUID = null;
            if (dis.readBoolean()) {
                holderUUID = new UUID(dis.readLong(), dis.readLong());
            }

            String label = null;
            if (dis.readBoolean()) {
                label = dis.readUTF();
            }

            int size = dis.readInt();
            if (size < 0 || size > 10_000) {
                throw new IOException("Suspicious map size: " + size);
            }

            Map<String, Object> map = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                String key   = dis.readUTF();
                Object value = readValue(dis);
                map.put(key, value);
            }

            return new InternalResult(map, holderUUID, label);
        }
    }

    /**
     * Reads one tagged value from the stream.
     */
    private static Object readValue(DataInputStream dis) throws IOException {
        byte tag = dis.readByte();
        return switch (tag) {
            case TAG_STRING    -> dis.readUTF();
            case TAG_INTEGER   -> dis.readInt();
            case TAG_LONG      -> dis.readLong();
            case TAG_DOUBLE    -> dis.readDouble();
            case TAG_BOOLEAN   -> dis.readBoolean();
            case TAG_LOCATION  -> readLocation(dis);
            case TAG_ITEMSTACK -> {
                int len      = dis.readInt();
                byte[] data  = new byte[len];
                dis.readFully(data);
                yield ItemStack.deserializeBytes(data);
            }
            default -> throw new IOException("Unknown value type tag: " + tag);
        };
    }

    // ------------------------------------------------------------------
    // v1 legacy reader  (ObjectInputStream – handles OOS block-data framing)
    // ------------------------------------------------------------------

    /**
     * Reads data produced by any original implementation that used
     * {@link ObjectOutputStream} to write primitives.
     *
     * <p>Expected write order (mirror this exactly in any v1 writer you had):</p>
     * <pre>
     *   boolean hasHolder
     *   if hasHolder: long mostSigBits, long leastSigBits
     *   boolean hasLabel
     *   if hasLabel:  UTF label
     *   int     size
     *   for each entry:
     *     UTF   key
     *     byte  typeTag
     *     ...   value bytes (same layout as v2)
     * </pre>
     *
     * <p>If your original format differed, adjust the read order here to match.</p>
     */
    private static InternalResult readV1Legacy(byte[] bytes) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {

            UUID holderUUID = null;
            if (ois.readBoolean()) {
                holderUUID = new UUID(ois.readLong(), ois.readLong());
            }

            String label = null;
            if (ois.readBoolean()) {
                label = ois.readUTF();
            }

            int size = ois.readInt();
            if (size < 0 || size > 10_000) {
                throw new IOException("Suspicious map size: " + size);
            }

            Map<String, Object> map = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                String key = ois.readUTF();
                byte   tag = ois.readByte();

                Object value = switch (tag) {
                    case TAG_STRING   -> ois.readUTF();
                    case TAG_INTEGER  -> ois.readInt();
                    case TAG_LONG     -> ois.readLong();
                    case TAG_DOUBLE   -> ois.readDouble();
                    case TAG_BOOLEAN  -> ois.readBoolean();
                    case TAG_LOCATION -> {
                        World world = null;
                        if (ois.readBoolean()) {
                            UUID wid = new UUID(ois.readLong(), ois.readLong());
                            world = Bukkit.getWorld(wid);
                        }
                        double x = ois.readDouble();
                        double y = ois.readDouble();
                        double z = ois.readDouble();
                        float yaw   = ois.readFloat();
                        float pitch = ois.readFloat();
                        yield new Location(world, x, y, z, yaw, pitch);
                    }
                    case TAG_ITEMSTACK -> {
                        int len     = ois.readInt();
                        byte[] data = new byte[len];
                        ois.readFully(data);
                        yield ItemStack.deserializeBytes(data);
                    }
                    default -> throw new IOException("Unknown value type tag in v1 data: " + tag);
                };

                map.put(key, value);
            }

            return new InternalResult(map, holderUUID, label);
        }
    }

    // -------------------------------------------------------------------------
    // Internal result record
    // -------------------------------------------------------------------------

    private record InternalResult(Map<String, Object> map, UUID holderUUID, String label) {}
}