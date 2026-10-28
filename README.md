# Smart Pantry Manager

An Android app written in Java that helps reduce food waste. The user records the ingredients they have at home, and the app suggests only the recipes they can cook right now, using strictly what is already in their pantry. No shopping trip is needed.

Built for Mobile App Development 700 (Richfield Graduate Institute of Technology).

Author: Mlondi Nyawo | Student number: 402410436

## Key feature: strict matching

A recipe is shown as "suggested" only if every ingredient it needs is in the pantry in at least the required quantity.

- If a recipe needs 5 ingredients and the pantry has 4, it is NOT suggested.
- Names are matched ignoring case and plurals ("Tomatoes" matches "tomato").
- Units are converted before comparing (1 kg of flour covers a 200 g requirement; 1 l of milk covers 300 ml).
- If nothing matches, the app shows a message instead of a blank screen.

## Features

- Add, edit and delete pantry items (name, quantity, unit, optional expiry date)
- Pantry list screen (RecyclerView with a custom adapter) bound to the database
- 18 pre-loaded recipes, seeded on first run
- Suggested Recipes screen using the strict-matching rule
- Recipe detail screen with the full ingredient list and method
- Settings screen: highlight items expiring within 3 days, and choose the default unit
- Input validation on the add/edit form
- Toolbar menu for navigation between screens
- Data persists after the app is closed and reopened

## Screens

1. Pantry List (launcher screen)
2. Add / Edit Ingredient
3. Suggested Recipes
4. Recipe Detail
5. Settings

## Database choice: SQLite

I chose SQLite, using SQLiteOpenHelper, because:
[Replace this with your own reasons. Possible points: it runs locally on the device so the app works offline, it needs no server or account, it suits a small single-user app like a home pantry, and it is the approach covered in the module's persistent data chapter.]

Tables:
- pantry (id, name, quantity, unit, expiry)
- recipes (id, name, steps)
- recipe_ingredients (id, recipe_id, name, quantity, unit)

The recipes are inserted when the database is first created. The pantry table holds the user's own data (create, read, update, delete).

## Tech

- Language: Java (no Kotlin)
- IDE: Android Studio
- Minimum SDK: API 24
- UI: XML layouts, Material components, RecyclerView
- Storage: SQLite, plus SharedPreferences for settings
- Tested on a Pixel 8 Pro emulator running Android 15 (API 35)

The app does not use Google Maps, any mapping SDK, or location/GPS.

## Project structure

app/src/main/java/com/example/smartpantry/
- PantryListActivity, AddEditItemActivity, SuggestedRecipesActivity, RecipeDetailActivity, SettingsActivity: the five screens
- BaseActivity: shared toolbar and menu
- PantryAdapter, RecipeAdapter: RecyclerView adapters
- PantryDbHelper: creates the database and provides the CRUD methods
- RecipeMatcher: the strict-matching logic
- PantryItem, Recipe, RecipeIngredient: model classes

app/src/main/res/: layouts and the navigation menu

## Setup and run

1. Install Android Studio.
2. Clone the repository:
   git clone https://github.com/MlondiNyawo/SmartPantryManager.git
3. Open the project folder in Android Studio and wait for Gradle to finish syncing.
4. Create an emulator (Tools > Device Manager > Create Virtual Device, for example a Pixel with API 34 or 35), or connect an Android phone with USB debugging turned on.
5. Select the device and click the green Run button.

No API keys, accounts or special permissions are needed.

## How to try the strict matching

1. Tap + and add Eggs (3 pcs), Cheese (50 g) and Butter (10 g).
2. Open the three-dot menu and choose Suggested Recipes. Cheese Omelette appears.
3. Go back and delete the Butter.
4. Open Suggested Recipes again. Cheese Omelette is gone, because one ingredient is missing.

## Possible improvements

- An "Almost There" list for recipes missing only one ingredient
- More recipes and ingredient synonyms
- Search and filtering in the pantry list
