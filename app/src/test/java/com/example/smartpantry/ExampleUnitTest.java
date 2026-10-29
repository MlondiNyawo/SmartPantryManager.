package com.example.smartpantry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for application logic that does not require the Android runtime.
 */
public class ExampleUnitTest {
    @Test
    public void normaliseName_handlesCaseSpacingAndPlural() {
        assertEquals("tomato", RecipeMatcher.normaliseName("  TOMATOES  "));
    }

    @Test
    public void suggest_matchesEquivalentMassUnits() {
        Recipe recipe = new Recipe(1, "Soup", "");
        recipe.getIngredients().add(new RecipeIngredient("carrots", 1, "kg"));

        PantryItem pantryItem = new PantryItem();
        pantryItem.setName("carrot");
        pantryItem.setQuantity(1000);
        pantryItem.setUnit("g");

        assertTrue(RecipeMatcher.suggest(
                java.util.Collections.singletonList(recipe),
                java.util.Collections.singletonList(pantryItem)).contains(recipe));
    }

    @Test
    public void suggest_excludesInsufficientOrIncompatibleStock() {
        Recipe recipe = new Recipe(2, "Bread", "");
        recipe.getIngredients().add(new RecipeIngredient("flour", 500, "g"));

        PantryItem pantryItem = new PantryItem();
        pantryItem.setName("flour");
        pantryItem.setQuantity(1);
        pantryItem.setUnit("kg");
        assertTrue(RecipeMatcher.suggest(
                java.util.Collections.singletonList(recipe),
                java.util.Collections.singletonList(pantryItem)).contains(recipe));

        pantryItem.setQuantity(400);
        pantryItem.setUnit("g");
        assertFalse(RecipeMatcher.suggest(
                java.util.Collections.singletonList(recipe),
                java.util.Collections.singletonList(pantryItem)).contains(recipe));

        pantryItem.setQuantity(1);
        pantryItem.setUnit("pcs");
        assertFalse(RecipeMatcher.suggest(
                java.util.Collections.singletonList(recipe),
                java.util.Collections.singletonList(pantryItem)).contains(recipe));
    }
}