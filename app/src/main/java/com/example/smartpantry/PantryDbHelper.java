package com.example.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLiteOpenHelper subclass: creates the tables, seeds the recipes on first run,
 * and provides the CRUD methods for pantry items plus read methods for recipes.
 */
public class PantryDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "smartpantry.db";
    private static final int DB_VERSION = 1; // increment when the schema changes

    private static final String CREATE_PANTRY =
            "CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)";
    private static final String CREATE_RECIPES =
            "CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, steps TEXT NOT NULL)";
    private static final String CREATE_RECIPE_ING =
            "CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "recipe_id INTEGER NOT NULL, name TEXT NOT NULL, quantity REAL NOT NULL, "
                    + "unit TEXT NOT NULL, FOREIGN KEY(recipe_id) REFERENCES recipes(id))";

    private static PantryDbHelper instance;

    public static synchronized PantryDbHelper getInstance(Context context) {
        if (instance == null) instance = new PantryDbHelper(context.getApplicationContext());
        return instance;
    }

    private PantryDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PANTRY);
        db.execSQL(CREATE_RECIPES);
        db.execSQL(CREATE_RECIPE_ING);
        seedRecipes(db); // pre-load recipes on first run
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry");
        onCreate(db);
    }

    // ---------- Pantry CRUD ----------

    public long insertItem(PantryItem item) {
        return getWritableDatabase().insert("pantry", null, toValues(item));
    }

    public int updateItem(PantryItem item) {
        return getWritableDatabase().update("pantry", toValues(item), "id = ?",
                new String[]{String.valueOf(item.getId())});
    }

    public int deleteItem(long id) {
        return getWritableDatabase().delete("pantry", "id = ?", new String[]{String.valueOf(id)});
    }

    public List<PantryItem> getAllItems() {
        List<PantryItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("pantry", null, null, null, null, null,
                "name COLLATE NOCASE")) {
            while (c.moveToNext()) list.add(fromCursor(c));
        }
        return list;
    }

    public PantryItem getItem(long id) {
        try (Cursor c = getReadableDatabase().query("pantry", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues v = new ContentValues();
        v.put("name", item.getName());
        v.put("quantity", item.getQuantity());
        v.put("unit", item.getUnit());
        v.put("expiry", item.getExpiry());
        return v;
    }

    private PantryItem fromCursor(Cursor c) {
        PantryItem item = new PantryItem();
        item.setId(c.getLong(c.getColumnIndexOrThrow("id")));
        item.setName(c.getString(c.getColumnIndexOrThrow("name")));
        item.setQuantity(c.getDouble(c.getColumnIndexOrThrow("quantity")));
        item.setUnit(c.getString(c.getColumnIndexOrThrow("unit")));
        item.setExpiry(c.getString(c.getColumnIndexOrThrow("expiry")));
        return item;
    }

    // ---------- Recipes (read only) ----------

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("recipes", null, null, null, null, null, "name")) {
            while (c.moveToNext()) list.add(loadRecipe(db, c));
        }
        return list;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query("recipes", null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            return c.moveToFirst() ? loadRecipe(db, c) : null;
        }
    }

    private Recipe loadRecipe(SQLiteDatabase db, Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow("id"));
        Recipe r = new Recipe(id, c.getString(c.getColumnIndexOrThrow("name")),
                c.getString(c.getColumnIndexOrThrow("steps")));
        try (Cursor ic = db.query("recipe_ingredients", null, "recipe_id = ?",
                new String[]{String.valueOf(id)}, null, null, "id")) {
            while (ic.moveToNext()) {
                r.getIngredients().add(new RecipeIngredient(
                        ic.getString(ic.getColumnIndexOrThrow("name")),
                        ic.getDouble(ic.getColumnIndexOrThrow("quantity")),
                        ic.getString(ic.getColumnIndexOrThrow("unit"))));
            }
        }
        return r;
    }

    // ---------- Seed data ----------

    /** ingredient format: "name|quantity|unit" */
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", steps);
        long recipeId = db.insert("recipes", null, rv);
        for (String s : ingredients) {
            String[] p = s.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", p[0]);
            iv.put("quantity", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert("recipe_ingredients", null, iv);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Cheese Omelette",
                "1. Beat the eggs.\n2. Melt butter in a pan.\n3. Pour in eggs, add cheese, fold and cook 2 minutes.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g");
        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta.\n2. Fry chopped onion in oil.\n3. Add chopped tomatoes and simmer 10 minutes.\n4. Toss with pasta.",
                "pasta|200|g", "tomato|3|pcs", "onion|1|pcs", "olive oil|15|ml");
        addRecipe(db, "Egg Fried Rice",
                "1. Fry chopped onion.\n2. Scramble the eggs in the pan.\n3. Add cooked rice and soy sauce, stir-fry 5 minutes.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "soy sauce|15|ml");
        addRecipe(db, "Pancakes",
                "1. Whisk flour, milk, egg and sugar into a batter.\n2. Fry ladles of batter in a hot pan until golden on both sides.",
                "flour|200|g", "milk|300|ml", "egg|2|pcs", "sugar|20|g");
        addRecipe(db, "Garlic Butter Toast",
                "1. Mix crushed garlic into butter.\n2. Spread on bread.\n3. Toast until golden.",
                "bread|4|pcs", "butter|30|g", "garlic|2|pcs");
        addRecipe(db, "Mashed Potatoes",
                "1. Peel and boil the potatoes until soft.\n2. Drain and mash with butter and warm milk.",
                "potato|500|g", "butter|30|g", "milk|100|ml");
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Toast the bread.\n2. Scramble eggs in melted butter.\n3. Serve on the toast.",
                "egg|3|pcs", "bread|2|pcs", "butter|10|g");
        addRecipe(db, "Chicken Rice Bowl",
                "1. Cook the rice.\n2. Fry sliced chicken with onion until cooked through.\n3. Serve over rice.",
                "chicken breast|300|g", "rice|200|g", "onion|1|pcs");
        addRecipe(db, "Vegetable Stir-fry",
                "1. Slice the vegetables.\n2. Stir-fry onion and carrot for 3 minutes.\n3. Add cabbage and soy sauce, cook 4 minutes.",
                "carrot|2|pcs", "onion|1|pcs", "cabbage|200|g", "soy sauce|15|ml");
        addRecipe(db, "Grilled Cheese Sandwich",
                "1. Butter the bread.\n2. Place cheese between slices.\n3. Fry both sides until golden.",
                "bread|2|pcs", "cheese|40|g", "butter|10|g");
        addRecipe(db, "Banana Pancakes",
                "1. Mash the bananas.\n2. Mix with eggs and flour.\n3. Fry small pancakes until golden.",
                "banana|2|pcs", "egg|2|pcs", "flour|100|g");
        addRecipe(db, "Tuna Pasta",
                "1. Boil the pasta.\n2. Fry onion in oil, add tuna.\n3. Toss with pasta.",
                "pasta|200|g", "tuna|1|pcs", "onion|1|pcs", "olive oil|15|ml");
        addRecipe(db, "Potato Omelette",
                "1. Fry sliced potato and onion in oil until soft.\n2. Add beaten eggs and cook until set.\n3. Flip once.",
                "potato|300|g", "egg|4|pcs", "onion|1|pcs", "olive oil|30|ml");
        addRecipe(db, "Tomato Soup",
                "1. Fry onion and garlic in butter.\n2. Add chopped tomatoes and 500 ml water.\n3. Simmer 20 minutes and blend.",
                "tomato|6|pcs", "onion|1|pcs", "garlic|2|pcs", "butter|20|g");
        addRecipe(db, "Rice Pudding",
                "1. Simmer rice with milk and sugar for 30 minutes, stirring often.",
                "rice|100|g", "milk|500|ml", "sugar|40|g");
        addRecipe(db, "Lentil Curry",
                "1. Fry onion and garlic.\n2. Add curry powder, tomatoes and lentils with water.\n3. Simmer 25 minutes.",
                "lentil|200|g", "onion|1|pcs", "tomato|2|pcs", "garlic|2|pcs", "curry powder|10|g");
        addRecipe(db, "French Toast",
                "1. Whisk eggs, milk and sugar.\n2. Dip the bread.\n3. Fry until golden.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|10|g");
        addRecipe(db, "Egg Noodles",
                "1. Boil the noodles.\n2. Scramble the eggs in a pan.\n3. Add noodles and soy sauce, toss.",
                "noodle|200|g", "egg|2|pcs", "soy sauce|15|ml");
    }
}
