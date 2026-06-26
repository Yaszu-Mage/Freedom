package xyz.yaszu.freedom.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.yaszu.freedom.Blocks.BaseBlock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A {@link PersistentDataType} for storing and retrieving {@code Map<Location, BaseBlock>}.
 *
 * <p>Each entry stores:</p>
 * <ul>
 *   <li>{@link Location} — world UUID (nullable), x, y, z, yaw, pitch</li>
 *   <li>{@link BaseBlock} — fully-qualified class name; reconstructed via public no-arg constructor</li>
 * </ul>
 *
 * <h2>Writing:</h2>
 * <pre>{@code
 *   Map<Location, BaseBlock> blocks = new HashMap<>();
 *   blocks.put(someLocation, someBaseBlock);
 *
 *   pdc.set(key, BlockMapPersistentDataType.INSTANCE, blocks);
 * }</pre>
 *
 * <h2>Reading:</h2>
 * <pre>{@code
 *   Map<Location, BaseBlock> blocks = pdc.get(key, BlockMapPersistentDataType.INSTANCE);
 *   if (blocks != null) {
 *       BaseBlock block = blocks.get(someLocation);
 *   }
 * }</pre>
 *
 * <h2>BaseBlock contract:</h2>
 * <p>Every concrete {@link BaseBlock} implementation <strong>must</strong> expose a
 * public no-arg constructor. The class is identified by its fully-qualified name;
 * renaming or moving the class will break existing stored data.</p>
 */
public final class BlockMapPersistentDataType
        implements PersistentDataType<byte[], Map<Location, BaseBlock>> {

    public static final BlockMapPersistentDataType INSTANCE = new BlockMapPersistentDataType();

    private static final byte FORMAT_VERSION = 1;

    private BlockMapPersistentDataType() {}

    // -------------------------------------------------------------------------
    // PersistentDataType contract
    // -------------------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull Class<Map<Location, BaseBlock>> getComplexType() {
        return (Class<Map<Location, BaseBlock>>) (Class<?>) Map.class;
    }

    @Override
    public @NonNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    // -------------------------------------------------------------------------
    // Serialization
    // -------------------------------------------------------------------------

    @Override
    public byte @NonNull [] toPrimitive(@NonNull Map<Location, BaseBlock> map,
                                        @NonNull PersistentDataAdapterContext ctx) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            dos.writeByte(FORMAT_VERSION);
            dos.writeInt(map.size());

            for (Map.Entry<Location, BaseBlock> entry : map.entrySet()) {
                writeLocation(dos, entry.getKey());
                writeBaseBlock(dos, entry.getValue());
            }

            dos.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize Map<Location, BaseBlock>", e);
        }
    }

    // -------------------------------------------------------------------------
    // Deserialization
    // -------------------------------------------------------------------------

    @Override
    public @NonNull Map<Location, BaseBlock> fromPrimitive(byte @NonNull [] bytes,
                                                           @NonNull PersistentDataAdapterContext ctx) {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {

            byte version = dis.readByte();
            if (version != FORMAT_VERSION) {
                throw new IOException(
                        "Unexpected version byte: " + version + " (expected " + FORMAT_VERSION + ")");
            }

            int size = dis.readInt();
            if (size < 0 || size > 100_000) {
                throw new IOException("Suspicious map size: " + size);
            }

            Map<Location, BaseBlock> map = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                Location  loc   = readLocation(dis);
                BaseBlock block = readBaseBlock(dis);
                map.put(loc, block);
            }

            return map;

        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize Map<Location, BaseBlock>", e);
        }
    }

    // -------------------------------------------------------------------------
    // Convenience: read directly from a PersistentDataContainer
    // -------------------------------------------------------------------------

    /**
     * @return {@code null} if the key is absent
     */
    public static Map<Location, BaseBlock> load(PersistentDataContainer pdc, NamespacedKey key) {
        return pdc.get(key, INSTANCE);
    }

    // -------------------------------------------------------------------------
    // Location helpers
    // -------------------------------------------------------------------------

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
    // BaseBlock helpers
    // -------------------------------------------------------------------------

    private static void writeBaseBlock(DataOutputStream dos, BaseBlock block) throws IOException {
        dos.writeUTF(block.getClass().getName());
    }

    private static BaseBlock readBaseBlock(DataInputStream dis) throws IOException {
        String className = dis.readUTF();
        try {
            Class<?> clazz = Class.forName(className);
            if (!BaseBlock.class.isAssignableFrom(clazz)) {
                throw new IOException(
                        "Class '" + className + "' does not implement BaseBlock");
            }
            return (BaseBlock) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new IOException(
                    "BaseBlock class not found: '" + className + "'. " +
                            "Was the class renamed or moved?", e);
        } catch (NoSuchMethodException e) {
            throw new IOException(
                    "BaseBlock class '" + className + "' has no public no-arg constructor.", e);
        } catch (Exception e) {
            throw new IOException(
                    "Failed to instantiate BaseBlock class '" + className + "': " + e.getMessage(), e);
        }
    }
}