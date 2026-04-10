package xyz.yaszu.freedom.Information.Alchemy;

import net.kyori.adventure.inventory.Book;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import xyz.yaszu.freedom.Information.BaseInformation;
import xyz.yaszu.freedom.Util.Util;

public class Circle extends Util implements BaseInformation {
    @Override
    public ItemStack information() {
        ItemStack book = ItemStack.of(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("Rituals for Stupid Stupid know nothing dummies");
        meta.setAuthor("B.L. Fegore");
        meta.pages(
              dess("Rituals for Stupid Stupid know nothing dummies" + "\n-------------------" + "Thanks to my patron S.A. Tan. You will be forever remembered in our hearts."),
                dess(
                        "What are rituals?"
                        + "\n-------------------" +
                                "Rituals are player made incantations that can range from group teleportation, all the way towards a remotely triggered explosive attack."
                        + "To construct a ritual, place a lectern in the center, with torches 3 blocks away on all sides, then 4 more torches around them forming a ring. Next, 2 blocks away from the original 4 torches, place Deepslate Brick Blocks with Item Frames on top of them."
                        + "Two blocks diagonally from the outside of the corner ring torches, place Deepslate Brick Blocks with item frames on the top"
                ),
                dess("How rituals Work." + "\n-------------------" + "Rituals are setup using Spell types and keywords where Spell type is first followed by keywords followed by additional spells(optional)" + "For example: " + "destruction location -1528 68 147" + "The list of action keywords are as follows,"),
                dess("[teleport, destruction, area, effect, thundering, rain, sun, day, night, shock, blast]"),
                dess("How Addatives work" + "\n-------------------" + "addatives are an addition that is added behind the spell type to further specify how the spell functions." + "For example: " + "destruction location -1528 68 147 range 25 delay 20" + "in this the addatives are range and delay making the spell have more effect and a 20 second delay" + "a list of the addatives are, " + "location, range, regeneration, haste, speed, jump, poison, wither, strength, weakness, "));
        book.setItemMeta(meta);
        return book;
    }
}
