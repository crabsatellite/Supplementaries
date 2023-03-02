package net.mehvahdjukaar.supplementaries.integration;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.plugin.common.displays.DefaultTillingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.item.Items;

public class REICompat implements REIClientPlugin {


    @Override
    public void registerDisplays(DisplayRegistry registry) {
        SpecialRecipeDisplays.registerRecipes(l -> l.forEach(r -> registry.add(DefaultCraftingDisplay.of(r))));
        registry.add(new DefaultTillingDisplay(EntryStack.of(VanillaEntryTypes.ITEM, Items.GRAVEL.getDefaultInstance()),
                EntryStack.of(VanillaEntryTypes.ITEM, ModRegistry.RAKED_GRAVEL.get().asItem().getDefaultInstance())));
    }
}
