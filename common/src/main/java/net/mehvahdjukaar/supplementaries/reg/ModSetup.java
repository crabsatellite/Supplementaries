package net.mehvahdjukaar.supplementaries.reg;


import com.google.common.base.Stopwatch;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.events.ItemsOverrideHandler;
import net.mehvahdjukaar.supplementaries.common.items.loot.CurseLootFunction;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.common.utils.FlowerPotHandler;
import net.mehvahdjukaar.supplementaries.common.world.data.map.WeatheredMap;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.ComposterBlock;

import java.util.ArrayList;
import java.util.List;

//Merge with supplementaries main class
public class ModSetup {

    //damn I hate this. If setup fails forge doesn't do anything, and it keeps on going quietly
    private static boolean hasFinishedSetup = false;
    private static int setupStage = 0;
    public static boolean firstTagLoad = false;

    private static final List<Runnable> MOD_SETUP_WORK = List.of(
            CurseLootFunction::setup,
            CompatHandler::setup,
            FlowerPotHandler::setup,
            WeatheredMap::setup,
            NetworkHandler::registerMessages,
            LootTablesInjects::init,
            ModSetup::registerCompostables,
            ModSetup::registerMobFoods,
            CauldronRegistry::registerInteractions,
            PresentRegistry::registerBehaviors
    );

    public static void setup() {
        try {
            Stopwatch watch = Stopwatch.createStarted();

            for (int i = 0; i < MOD_SETUP_WORK.size(); i++) {
                setupStage = i;
                MOD_SETUP_WORK.get(i).run();
            }
            hasFinishedSetup = true;

            Supplementaries.LOGGER.info("Finished mod setup in: {} ms", watch.elapsed().toMillis());

        } catch (Exception e) {
            Supplementaries.LOGGER.error(e);
            terminateWhenSetupFails();
        }
    }

    private static void terminateWhenSetupFails() {
        //if setup fails crash the game. idk why it doesn't do that on its own wtf
        //Supplementaries.LOGGER.throwing(e);
        throw new IllegalStateException("Mod setup has failed to complete (" + setupStage + ").\n" +
                " This might be due to some mod incompatibility or outdated dependencies (check if everything is up to date).\n" +
                " Refusing to continue loading with a broken modstate. Next step: crashing this game, no survivors");
    }

    private static void registerMobFoods() {
        List<ItemStack> chickenFood = new ArrayList<>(List.of(Chicken.FOOD_ITEMS.getItems()));
        chickenFood.add(new ItemStack(ModRegistry.FLAX_SEEDS_ITEM.get()));
        Chicken.FOOD_ITEMS = Ingredient.of(chickenFood.stream());

        List<ItemStack> horseFood = new ArrayList<>(List.of(new ItemStack(ModRegistry.FLAX_ITEM.get()), new ItemStack(ModRegistry.FLAX_BLOCK.get())));
        horseFood.addAll(List.of(AbstractHorse.FOOD_ITEMS.getItems()));
        AbstractHorse.FOOD_ITEMS = Ingredient.of(horseFood.stream());
    }

    private static void registerCompostables() {
        ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_SEEDS_ITEM.get(), 0.3F);
        ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_ITEM.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_WILD.get(), 0.65F);
        ComposterBlock.COMPOSTABLES.put(ModRegistry.FLAX_BLOCK.get(), 1);
    }

    //events on setup. fire on world load
    //TODO: add
    public static void tagDependantSetup() {
        if (!firstTagLoad) {
            firstTagLoad = true;
            if (!hasFinishedSetup) {
                //if mod setup fails (without throwing errors) we try to replicate what caused it to crash and printing that error
                try {
                    Supplementaries.LOGGER.error("Something went wrong during mod setup, exiting");
                    MOD_SETUP_WORK.get(setupStage).run();
                    Supplementaries.LOGGER.error("No error found. Weird");
                } catch (Exception e) {
                    Supplementaries.LOGGER.error(e);
                }
                terminateWhenSetupFails();
            }
            //using this as a post setup event that can access tags
            Stopwatch watch = Stopwatch.createStarted();

            //stuff that needs tags
            DispenserRegistry.registerBehaviors();
            ItemsOverrideHandler.registerOverrides();

            Supplementaries.LOGGER.info("Finished additional setup in {} seconds", watch.elapsed().toSeconds());
        }
    }

}
