package com.enderio.machines.common.recipe;

import com.enderio.api.machines.recipes.MachineRecipe;
import com.enderio.api.recipe.CountedIngredient;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.List;

public interface IAlloySmeltingRecipe extends MachineRecipe<IAlloySmeltingRecipe.Container> {
    /**
     * Get the inputs for the alloy smelting recipe.
     */
    List<CountedIngredient> getInputs();

    /**
     * Get the amount of experience generated by the recipe.
     */
    float getExperience();

    /**
     * The recipe container.
     * This acts as additional context.
     */
    class Container extends RecipeWrapper {

        private int inputsTaken;

        public Container(IItemHandlerModifiable inv) {
            super(inv);
        }

        public int getInputsTaken() {
            return inputsTaken;
        }

        public void setInputsTaken(int inputsTaken) {
            this.inputsTaken = inputsTaken;
        }
    }
}
