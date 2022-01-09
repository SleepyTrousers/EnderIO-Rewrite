package com.enderio.base.datagen.recipe.standard;

import com.enderio.base.common.capability.capacitors.CapacitorData;
import com.enderio.base.common.init.EIOItems;
import com.enderio.base.common.recipe.capacitor.CapacitorDataRecipe;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CapacitorDataRecipeGenerator extends RecipeProvider {
    public CapacitorDataRecipeGenerator(DataGenerator pGenerator) {
        super(pGenerator);
    }

    @Override
    protected void buildCraftingRecipes(@Nonnull Consumer<FinishedRecipe> recipeConsumer) {
        build(1.0f, EIOItems.BASIC_CAPACITOR.get(), recipeConsumer);
        build(2.0f, EIOItems.DOUBLE_LAYER_CAPACITOR.get(), recipeConsumer);
        build(4.0f, EIOItems.OCTADIC_CAPACITOR.get(), recipeConsumer);
    }

    protected void build(float base, Item item, Consumer<FinishedRecipe> recipeConsumer) {
        Map<String, Float> specialisations = new HashMap<>();
        build(base, specialisations, item, recipeConsumer);
    }

    protected void build(float base, Map<String, Float> specialisations, Item item, Consumer<FinishedRecipe> recipeConsumer) {
        CapacitorData data = new CapacitorData();
        data.setBase(base);
        data.addAllSpecialization(specialisations);
        build(data, item, recipeConsumer);
    }

    protected void build(CapacitorData capacitorData, Item item, Consumer<FinishedRecipe> recipeConsumer) {
        build(new CapacitorDataRecipe(null, item, capacitorData), item.getRegistryName().getPath(), recipeConsumer);
    }

    protected void build(CapacitorDataRecipe recipe, String name, Consumer<FinishedRecipe> recipeConsumer) {
        recipeConsumer.accept(new CapacitorDataRecipeResult(recipe, name));
    }
}
