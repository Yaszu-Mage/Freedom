package xyz.yaszu.freedom.Enchantments;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import xyz.yaszu.freedom.Enchantments.Enchant.SmeltingEnchant;

import java.util.HashMap;

public class Bootstrap implements PluginBootstrap {

    public static HashMap<String, BaseEnchant> ENCHANTS = new HashMap<>();


    public void registerAll() {
        register(new SmeltingEnchant());
    }

    public void register(BaseEnchant enchant) {
        ENCHANTS.put(enchant.id(), enchant);
    }


    @Override
    public void bootstrap(BootstrapContext context) {
        registerAll();
        context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
            for (BaseEnchant enchant : ENCHANTS.values()) {
                event.registry().register(
                        EnchantmentKeys.create(Key.key(enchant.id())),
                        b -> b.description(Component.text(enchant.description()))
                                .supportedItems(event.getOrCreateTag(enchant.supportedItems()))
                                .anvilCost(enchant.anvilCost())
                                .maxLevel(enchant.maxLevel())
                                .weight(enchant.weight())
                                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(enchant.minEnchantCost(), 1))
                                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(enchant.maxEnchantCost(), 1))
                                .activeSlots(enchant.activeSlots())
                );
            }
        }));
        EnchantmentListener.update();
    }
}
