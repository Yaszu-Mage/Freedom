package xyz.yaszu.freedom.Util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A PersistentDataType for storing and retrieving Inventory objects
 * Uses serialization to convert inventory data to/from byte arrays
 */
public class InventoryPersistentDataType implements PersistentDataType<byte[], Inventory> {

    private static final InventoryPersistentDataType INSTANCE = new InventoryPersistentDataType();

    /**
     * Get the singleton instance of this data type
     */
    public static InventoryPersistentDataType get() {
        return INSTANCE;
    }

    @Override
    @NonNull
    public Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    @NonNull
    public Class<Inventory> getComplexType() {
        return Inventory.class;
    }

    /**
     * Convert an Inventory to a byte array for storage
     */
    @Override
    public byte @NonNull [] toPrimitive(@NonNull Inventory inventory, @NonNull PersistentDataAdapterContext context) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            // Write inventory size
            oos.writeInt(inventory.getSize());

            // Write each itemstack
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null) {
                    oos.writeBoolean(false);
                } else {
                    oos.writeBoolean(true);
                    oos.writeObject(item);
                }
            }

            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize inventory", e);
        }
    }

    /**
     * Convert a byte array back to an Inventory
     */
    @Override
    @NonNull
    public Inventory fromPrimitive(byte @NonNull [] primitive, @NonNull PersistentDataAdapterContext context) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(primitive);
             ObjectInputStream ois = new ObjectInputStream(bais)) {

            // Read inventory size
            int size = ois.readInt();

            // Create a generic inventory
            Inventory inventory = org.bukkit.Bukkit.createInventory(null, size);

            // Read each itemstack
            for (int i = 0; i < size; i++) {
                boolean hasItem = ois.readBoolean();
                if (hasItem) {
                    ItemStack item = (ItemStack) ois.readObject();
                    inventory.setItem(i, item);
                }
            }

            return inventory;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize inventory", e);
        }
    }
}

