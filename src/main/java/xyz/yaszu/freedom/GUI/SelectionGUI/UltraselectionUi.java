package xyz.yaszu.freedom.GUI.SelectionGUI;

import io.papermc.paper.connection.PlayerGameConnection;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import xyz.yaszu.freedom.Freedom;
import xyz.yaszu.freedom.Soul.Base_Soul;
import xyz.yaszu.freedom.Soul.Ultra.*;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class UltraselectionUi extends Util implements Listener {

    Plugin freedom = Freedom.get_plugin();
    public ArrayList active_souls(){

        List active_souls_in_config = freedom.getConfig().getStringList("Active_Souls");
        ArrayList active_souls = new ArrayList<Base_Soul>();
        if (active_souls_in_config.contains("Red")) {
            active_souls.add(new Red());
        }
        if (active_souls_in_config.contains("Purple")) {
            active_souls.add(new Purple());
        }
        if (active_souls_in_config.contains("Blue")) {
            active_souls.add(new Mocha());
        }
        if (active_souls_in_config.contains("Green")) {
            active_souls.add(new Green());
        }
        if (active_souls_in_config.contains("Black")) {
            active_souls.add(new Black());
        }
        if (active_souls_in_config.contains("Yellow")) {
            active_souls.add(new Cafe());
        }
        if (active_souls_in_config.contains("None")) {
            active_souls.add(new None());
        }
        if (active_souls_in_config.contains("Orange")) {
            active_souls.add(new Orange());
        }
        return active_souls;
    }



    public Base_Soul get_next_soul(Player player, Base_Soul soul_name, boolean is_forward) {
        int max_size = active_souls().size();
        int location = 0;
        for (int i = 0; i < active_souls().size(); i++) {
            Base_Soul soul = (Base_Soul) active_souls().get(i);
            if (soul.Name_For_Container().equals(soul_name.Name_For_Container())) {
                location = i;
                break;
            }

        }
        if (is_forward) {
            if (location + 1 >= max_size) {
                Base_Soul base = (Base_Soul) active_souls().get(0);
                return base;
            } else {
                Base_Soul base = (Base_Soul) active_souls().get(location + 1);
                return base;
            }
        } else {
            if (location - 1 <= 0) {
                Base_Soul base = (Base_Soul) active_souls().get(max_size - 1);
                return base;
            } else {
                Base_Soul base = (Base_Soul) active_souls().get(location - 1);
                return base;
            }
        }
    }


    public static HashMap<UUID, Base_Soul> soul_selection_map = new HashMap<UUID, Base_Soul>();

    @EventHandler
    void handleDialog(PlayerCustomClickEvent event) throws MineSkinException, DataRequestException {
        Player player;
        if (event.getCommonConnection() instanceof PlayerGameConnection conn) {
            player = conn.getPlayer();

        } else {
            return;
        }
        if (event.getIdentifier().equals(Key.key("papermc:user_input/select2"))) {
            ItemStack item = player.getInventory().getItemInMainHand();
                if (item != null) {
                    if (item.getPersistentDataContainer().has(keygen("item_id"))) {
                        String itemid = item.getPersistentDataContainer().get(keygen("item_id"), PersistentDataType.STRING);
                        switch (itemid) {
                            case "evolutionstone":
                                if (player.getInventory().getItemInMainHand().getAmount() > 1) {
                                    player.getInventory().getItemInMainHand().subtract(1);
                                } else {
                                    player.getInventory().setItemInMainHand(ItemStack.of(Material.AIR));
                                }
                                break;


                        }
                    }
                }
            ItemStack offitem = player.getInventory().getItemInOffHand();
            if (offitem != null) {
                if (offitem.getPersistentDataContainer().has(keygen("item_id"))) {
                    String itemid = offitem.getPersistentDataContainer().get(keygen("item_id"), PersistentDataType.STRING);
                    switch (itemid) {
                        case "evolutionstone":
                            if (player.getInventory().getItemInOffHand().getAmount() > 1) {
                                player.getInventory().getItemInOffHand().subtract(1);
                            } else {
                                player.getInventory().setItemInOffHand(ItemStack.of(Material.AIR));
                            }

                            break;


                    }
                }
            }
            currentsouls.get(player.getUniqueId());
            player.getPersistentDataContainer().set(keygen("soul"), PersistentDataType.STRING,currentsouls.get(player.getUniqueId()).Name_For_Container());
            Black.join(player);
            Black.join(player);
            if (currentsouls.get(player.getUniqueId()).Name_For_Container() == "Blue" || currentsouls.get(player.getUniqueId()).Name_For_Container() == "Yellow") {
                Mocha.init(player);
                Mocha.init(player);
            }
        }

    }

    public static ItemStack emptyItem(ItemStack item) {
        ItemMeta workingMeta = item.getItemMeta();
        workingMeta.displayName(dess("\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33\uD83D\uDD33"));
        item.setItemMeta(workingMeta);
        return item;
    }

    public static ItemStack Checkmark(ItemStack item) {
        ItemMeta workingMeta = item.getItemMeta();
        workingMeta.displayName(dess("✅"));
        workingMeta.setItemModel(NamespacedKey.minecraft("checkmark"));
        item.setItemMeta(workingMeta);
        return item;
    }
    public static Component line = dess("----------");
    public static Base_Soul currentsoul = new Red();
    public static HashMap<UUID,Base_Soul> currentsouls = new HashMap<>();
    public static void open_UI(Player player, Base_Soul red) {
        currentsouls.put(player.getUniqueId(),red);
        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(red.Name()).canCloseWithEscape(false).externalTitle(dess("I AM TITLE"))
                        .body(List.of(
                                DialogBody.item(red.Icon()).build(),
                                DialogBody.plainMessage(red.Description()),
                                DialogBody.plainMessage(line),
                                DialogBody.item(
                                        Checkmark(ItemStack.of(Material.REDSTONE)),
                                        DialogBody.plainMessage(red.AbilityOneName()),
                                        true,
                                        false,
                                        16,
                                        16
                                ),
                                DialogBody.plainMessage(red.AbilityOneDescription())
                                ,DialogBody.item(
                                        Checkmark(ItemStack.of(Material.REDSTONE)),
                                        DialogBody.plainMessage(red.AbilityTwoName()),
                                        true,
                                        false,
                                        16,
                                        16
                                ),
                                        DialogBody.plainMessage(red.AbilityTwoDescription()),
                                        DialogBody.plainMessage(line),
                                        DialogBody.plainMessage(red.Passive_Description()),
                                DialogBody.item(red.Related_Item(),DialogBody.plainMessage(
                                        red.ActivePassive_Description()),
                                        true,
                                        true,
                                        16,
                                        16)
                        )
                        )
                        .build())
                .type(DialogType.multiAction(List.of(

                        ActionButton.builder(dess("Select")).action(DialogAction.customClick(Key.key("papermc:user_input/select2"),null)).build()))
                        .build())

        );
        player.showDialog(dialog);
    }
}
