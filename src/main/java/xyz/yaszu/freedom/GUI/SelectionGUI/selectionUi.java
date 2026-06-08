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
import xyz.yaszu.freedom.Soul.*;
import xyz.yaszu.freedom.Soul.Base.*;
import xyz.yaszu.freedom.Soul.Ultra.*;
import xyz.yaszu.freedom.Util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class selectionUi extends Util implements Listener {

    Plugin freedom = Freedom.get_plugin();
    public ArrayList active_souls(){

        List active_souls_in_config = freedom.getConfig().getStringList("Active_Souls");
        ArrayList active_souls = new ArrayList<Base_Soul>();
        if (active_souls_in_config.contains("Red")) {
            active_souls.add(new BaseRed());
        }
        if (active_souls_in_config.contains("Orange")) {
            active_souls.add(new BaseOrange());
        }
        if (active_souls_in_config.contains("Yellow")) {
            active_souls.add(new BaseYellow());
        }
        if (active_souls_in_config.contains("Green")) {
            active_souls.add(new BaseGreen());
        }
        if (active_souls_in_config.contains("Blue")) {
            active_souls.add(new BaseBlue());
        }
        if (active_souls_in_config.contains("Purple")) {
            active_souls.add(new BasePurple());
        }
        if (active_souls_in_config.contains("Black")) {
            active_souls.add(new BaseBlack());
        }
        if (active_souls_in_config.contains("Cafe")) {
            active_souls.add(new BaseCafe());
        }
        if (active_souls_in_config.contains("Mocha")) {
            active_souls.add(new BaseMocha());
        }
        if (active_souls_in_config.contains("None")) {
            active_souls.add(new BaseNone());
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
            if (location - 1 < 0) {
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
        if (event.getIdentifier().equals(Key.key("papermc:user_input/back"))) {
            open_UI(player,get_next_soul(player,soul_selection_map.get(player.getUniqueId()),false));
        }
        if (event.getIdentifier().equals(Key.key("papermc:user_input/select"))) {
            player.getPersistentDataContainer().set(keygen("soul"), PersistentDataType.STRING,soul_selection_map.get(player.getUniqueId()).Name_For_Container());
            Black.join(player);
            BaseBlack.join(player);
            if (soul_selection_map.get(player.getUniqueId()).Name_For_Container() == "Blue" || soul_selection_map.get(player.getUniqueId()).Name_For_Container() == "Yellow") {
                Mocha.init(player);
                BaseMocha.init(player);
            }
        }
        if (event.getIdentifier().equals(Key.key("papermc:user_input/forward"))) {
            open_UI(player,get_next_soul(player,soul_selection_map.get(player.getUniqueId()),true));
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
    public static void open_UI(Player player, Base_Soul red) {
        soul_selection_map.put(player.getUniqueId(),red);
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
                        ActionButton.builder(dess("Back")).action(DialogAction.customClick(Key.key("papermc:user_input/back"),null)).build(),
                        ActionButton.builder(dess("Select")).action(DialogAction.customClick(Key.key("papermc:user_input/select"),null)).build(),
                        ActionButton.builder(dess("Forward")).action(DialogAction.customClick(Key.key("papermc:user_input/forward"),null)).build())).columns(3)
                        .build())

        );
        player.showDialog(dialog);
    }
}
