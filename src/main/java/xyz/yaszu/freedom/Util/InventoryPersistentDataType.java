package xyz.yaszu.freedom.Util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.UUID;

/**
 * A stateless PersistentDataType for storing and retrieving Inventory objects.
 *
 * <p>Context (holder UUID, inventory name) is passed explicitly via {@link SaveContext}
 * rather than being stored as mutable singleton state, making this safe to use
 * across concurrent operations.</p>
 *
 * <h3>Writing:</h3>
 * <pre>{@code
 *   SaveContext ctx = new SaveContext(playerUUID, viewTitle);
 *   meta.getPersistentDataContainer().set(key, InventoryPersistentDataType.get(ctx), inventory);
 * }</pre>
 *
 * <h3>Reading:</h3>
 * <pre>{@code
 *   LoadResult result = InventoryPersistentDataType.load(pdc, key);
 *   if (result != null) {
 *       Inventory inv  = result.inventory();
 *       UUID     owner = result.holderUUID();    // may be null
 *       Component name = result.inventoryName(); // may be null
 *   }
 * }</pre>
 */
public final class InventoryPersistentDataType implements PersistentDataType<byte[], Inventory> {

    // -------------------------------------------------------------------------
    // Public API types
    // -------------------------------------------------------------------------

    /**
     * Metadata to embed when saving an inventory.
     *
     * @param holderUUID    the owning player's UUID, or {@code null}
     * @param inventoryName the inventory title as a Component, or {@code null}
     */
    public record SaveContext(UUID holderUUID, Component inventoryName) {
        public SaveContext(UUID holderUUID, String inventoryName) {
            this(holderUUID, inventoryName != null ? Component.text(inventoryName) : null);
        }
    }

    /** The deserialized inventory together with any metadata embedded at save time. */
    public record LoadResult(Inventory inventory, UUID holderUUID, Component inventoryName) {}

    // -------------------------------------------------------------------------
    // Factory / convenience
    // -------------------------------------------------------------------------

    private static final InventoryPersistentDataType NO_CONTEXT = new InventoryPersistentDataType(null);

    /**
     * Returns a type instance that embeds the supplied context when serializing.
     * Create it inline at the call site; do not cache it.
     */
    public static InventoryPersistentDataType get(SaveContext context) {
        return new InventoryPersistentDataType(context);
    }

    /** Returns a shared, context-free instance suitable for reading. */
    public static InventoryPersistentDataType get() {
        return NO_CONTEXT;
    }

    /**
     * Convenience: read directly from a PersistentDataContainer and return
     * inventory + decoded metadata as a {@link LoadResult}.
     *
     * @return {@code null} if the key is absent
     */
    public static LoadResult load(
            org.bukkit.persistence.PersistentDataContainer pdc,
            org.bukkit.NamespacedKey key) {

        InternalResult r = pdc.get(key, INTERNAL_TYPE);
        if (r == null) return null;
        return new LoadResult(r.inventory, r.holderUUID, r.inventoryName);
    }

    // -------------------------------------------------------------------------
    // Implementation
    // -------------------------------------------------------------------------

    /**
     * First byte of every v2-format byte array.
     * 0xAC is the first byte of an ObjectOutputStream stream (legacy format), so any
     * value that is not 0xAC unambiguously identifies a v2 payload.
     */
    private static final byte FORMAT_VERSION = 2;

    private final SaveContext context;

    private InventoryPersistentDataType(SaveContext context) {
        this.context = context;
    }

    @Override public @NonNull Class<byte[]>    getPrimitiveType() { return byte[].class; }
    @Override public @NonNull Class<Inventory> getComplexType()   { return Inventory.class; }

    // ------------------------------------------------------------------
    // Serialization  (v2 – plain DataOutputStream, no OOS framing)
    // ------------------------------------------------------------------

    @Override
    public byte @NonNull [] toPrimitive(@NonNull Inventory inventory,
                                        @NonNull PersistentDataAdapterContext ctx) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(FORMAT_VERSION);

            UUID uid = (context != null) ? context.holderUUID() : null;
            dos.writeBoolean(uid != null);
            if (uid != null) {
                dos.writeLong(uid.getMostSignificantBits());
                dos.writeLong(uid.getLeastSignificantBits());
            }

            Component name = (context != null) ? context.inventoryName() : null;
            dos.writeBoolean(name != null);
            if (name != null) {
                dos.writeUTF(JSONComponentSerializer.json().serialize(name));
            }

            dos.writeInt(inventory.getSize());
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null || item.getType().isAir()) {
                    dos.writeBoolean(false);
                } else {
                    dos.writeBoolean(true);
                    byte[] itemData = item.serializeAsBytes();
                    dos.writeInt(itemData.length);
                    dos.write(itemData);
                }
            }

            dos.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize inventory", e);
        }
    }

    @Override
    public @NonNull Inventory fromPrimitive(byte @NonNull [] bytes,
                                            @NonNull PersistentDataAdapterContext ctx) {
        return INTERNAL_TYPE.fromPrimitive(bytes, ctx).inventory;
    }

    // -------------------------------------------------------------------------
    // Internal deserializer (also surfaces metadata for load())
    // -------------------------------------------------------------------------

    private static final PersistentDataType<byte[], InternalResult> INTERNAL_TYPE =
            new PersistentDataType<>() {
                @Override public @NonNull Class<byte[]>         getPrimitiveType() { return byte[].class; }
                @Override public @NonNull Class<InternalResult> getComplexType()   { return InternalResult.class; }

                @Override
                public byte @NonNull [] toPrimitive(@NonNull InternalResult value,
                                                    @NonNull PersistentDataAdapterContext ctx) {
                    throw new UnsupportedOperationException("Use InventoryPersistentDataType for writing");
                }

                @Override
                public @NonNull InternalResult fromPrimitive(byte @NonNull [] bytes,
                                                             @NonNull PersistentDataAdapterContext ctx) {
                    // Detect format by the first byte:
                    //   0xAC = ObjectOutputStream magic → legacy v1
                    //   0x02 = FORMAT_VERSION           → current v2
                    if ((bytes[0] & 0xFF) == 0xAC) {
                        try {
                            return readV1Legacy(bytes);
                        } catch (Exception v1Ex) {
                            throw new RuntimeException(
                                    "Failed to deserialize legacy inventory (v1): " + v1Ex.getMessage(), v1Ex);
                        }
                    } else {
                        try {
                            return readV2(bytes);
                        } catch (Exception v2Ex) {
                            throw new RuntimeException(
                                    "Failed to deserialize inventory (v2): " + v2Ex.getMessage(), v2Ex);
                        }
                    }
                }
            };

    // ------------------------------------------------------------------
    // v2 reader  (DataInputStream, no OOS framing)
    // ------------------------------------------------------------------

    private static InternalResult readV2(byte[] bytes) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {

            byte version = dis.readByte();
            if (version != FORMAT_VERSION) {
                throw new IOException("Unexpected version byte: " + version + " (expected " + FORMAT_VERSION + ")");
            }

            UUID holderUUID = null;
            if (dis.readBoolean()) {
                holderUUID = new UUID(dis.readLong(), dis.readLong());
            }

            Component inventoryName = null;
            if (dis.readBoolean()) {
                String json = dis.readUTF();
                try {
                    inventoryName = JSONComponentSerializer.json().deserialize(json);
                } catch (Exception e) {
                    inventoryName = Component.text(json);
                }
            }

            int size = dis.readInt();
            validateSize(size);

            Inventory inventory = inventoryName != null
                    ? Bukkit.createInventory(null, size, inventoryName)
                    : Bukkit.createInventory(null, size);

            readItems(dis, inventory, size);
            return new InternalResult(inventory, holderUUID, inventoryName);
        }
    }

    // ------------------------------------------------------------------
    // v1 legacy reader  (ObjectInputStream – handles OOS block-data framing)
    // ------------------------------------------------------------------

    /**
     * Reads data produced by the original singleton implementation, which used
     * {@link java.io.ObjectOutputStream} to write primitives.
     *
     * <p>OOS wraps every primitive in block-data envelopes and inserts extra framing
     * bytes whenever its 1024-byte internal buffer overflows mid-stream (e.g. when an
     * item's serialized bytes cross the boundary).  A plain {@link DataInputStream}
     * after naively stripping the header would treat those framing bytes as data and
     * corrupt all subsequent reads.  {@link ObjectInputStream} handles the framing
     * transparently, so we use it here.</p>
     *
     * <p>Original write order:</p>
     * <pre>
     *   boolean hasHolder
     *   if hasHolder: long mostSigBits, long leastSigBits
     *   boolean hasName
     *   if hasName:   UTF (JSON-serialized Component)
     *   int     size
     *   for each slot: boolean hasItem [ int itemLen, byte[] itemData ]
     * </pre>
     */
    private static InternalResult readV1Legacy(byte[] bytes) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {

            UUID holderUUID = null;
            if (ois.readBoolean()) {
                holderUUID = new UUID(ois.readLong(), ois.readLong());
            }

            Component inventoryName = null;
            if (ois.readBoolean()) {
                String json = ois.readUTF();
                try {
                    inventoryName = JSONComponentSerializer.json().deserialize(json);
                } catch (Exception e) {
                    inventoryName = Component.text(json);
                }
            }

            int size = ois.readInt();
            validateSize(size);

            Inventory inventory = inventoryName != null
                    ? Bukkit.createInventory(null, size, inventoryName)
                    : Bukkit.createInventory(null, size);

            for (int i = 0; i < size; i++) {
                if (ois.readBoolean()) {
                    int len = ois.readInt();
                    byte[] itemData = new byte[len];
                    ois.readFully(itemData);
                    inventory.setItem(i, ItemStack.deserializeBytes(itemData));
                }
            }

            return new InternalResult(inventory, holderUUID, inventoryName);
        }
    }

    // ------------------------------------------------------------------
    // Shared helpers
    // ------------------------------------------------------------------

    private static void validateSize(int size) throws IOException {
        if (size <= 0 || size > 54 || size % 9 != 0) {
            throw new IOException("Invalid inventory size: " + size);
        }
    }

    private static void readItems(DataInputStream dis, Inventory inventory, int size) throws IOException {
        for (int i = 0; i < size; i++) {
            if (dis.readBoolean()) {
                int len = dis.readInt();
                byte[] itemData = new byte[len];
                dis.readFully(itemData);
                inventory.setItem(i, ItemStack.deserializeBytes(itemData));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Internal result record
    // -------------------------------------------------------------------------

    private record InternalResult(Inventory inventory, UUID holderUUID, Component inventoryName) {}
}