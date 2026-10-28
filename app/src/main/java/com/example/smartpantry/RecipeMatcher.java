package com.example.smartpantry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Strict-matching logic. A recipe qualifies ONLY if every ingredient is in the pantry
 * in at least the required quantity. Names are normalised (case, spacing, plural) and
 * quantities are converted to a base unit (g, ml or count) before comparing.
 */
public class RecipeMatcher {

    private static final Set<String> KEEP_AS_IS = new HashSet<>(Arrays.asList(
            "asparagus", "hummus", "couscous", "molasses", "swiss", "citrus"));

    /** Lower-case, strip punctuation, collapse spaces and singularise the last word. */
    public static String normaliseName(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase().replaceAll("[^a-z ]", " ").trim().replaceAll("\\s+", " ");
        if (s.isEmpty()) return s;
        int lastSpace = s.lastIndexOf(' ');
        String head = lastSpace < 0 ? "" : s.substring(0, lastSpace + 1);
        String last = s.substring(lastSpace + 1);
        return head + singular(last);
    }

    private static String singular(String w) {
        if (KEEP_AS_IS.contains(w) || w.length() <= 3) return w;
        if (w.endsWith("ies") && w.length() > 4) return w.substring(0, w.length() - 3) + "y";
        if (w.endsWith("oes") || w.endsWith("ches") || w.endsWith("shes")
                || w.endsWith("xes") || w.endsWith("sses")) return w.substring(0, w.length() - 2);
        if (w.endsWith("ss") || w.endsWith("us")) return w;
        if (w.endsWith("s")) return w.substring(0, w.length() - 1);
        return w;
    }

    /** Quantity converted to a base unit. dimension is "mass", "volume" or "count". */
    private static class Base {
        final String dimension;
        final double amount;
        Base(String dimension, double amount) { this.dimension = dimension; this.amount = amount; }
    }

    private static Base toBase(double qty, String unitRaw) {
        String u = unitRaw == null ? "" : unitRaw.toLowerCase().trim();
        switch (u) {
            case "g": case "gram": case "grams": return new Base("mass", qty);
            case "kg": case "kilogram": case "kilograms": return new Base("mass", qty * 1000);
            case "ml": case "millilitre": case "milliliter": return new Base("volume", qty);
            case "l": case "litre": case "liter": case "litres": case "liters":
                return new Base("volume", qty * 1000);
            case "tsp": case "teaspoon": case "teaspoons": return new Base("volume", qty * 5);
            case "tbsp": case "tablespoon": case "tablespoons": return new Base("volume", qty * 15);
            case "cup": case "cups": return new Base("volume", qty * 240);
            default: return new Base("count", qty); // pcs, piece, whole, unit, empty ...
        }
    }

    /** Totals the pantry per ingredient and dimension, e.g. "egg|count" -> 6. */
    private static Map<String, Double> buildStock(List<PantryItem> pantry) {
        Map<String, Double> stock = new HashMap<>();
        for (PantryItem p : pantry) {
            Base b = toBase(p.getQuantity(), p.getUnit());
            String key = normaliseName(p.getName()) + "|" + b.dimension;
            Double current = stock.get(key);
            stock.put(key, (current == null ? 0 : current) + b.amount);
        }
        return stock;
    }

    /** True only if EVERY ingredient is available in at least the required amount. */
    public static boolean canMake(Recipe recipe, Map<String, Double> stock) {
        for (RecipeIngredient ing : recipe.getIngredients()) {
            Base need = toBase(ing.quantity, ing.unit);
            Double have = stock.get(normaliseName(ing.name) + "|" + need.dimension);
            if (have == null || have + 1e-9 < need.amount) return false; // one miss = excluded
        }
        return true;
    }

    public static List<Recipe> suggest(List<Recipe> all, List<PantryItem> pantry) {
        Map<String, Double> stock = buildStock(pantry);
        List<Recipe> result = new ArrayList<>();
        for (Recipe r : all) if (canMake(r, stock)) result.add(r);
        return result;
    }
}
