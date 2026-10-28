package com.example.smartpantry;

import android.os.Bundle;
import android.widget.TextView;

/** Shows the full ingredient list and method of the recipe passed in via the Intent. */
public class RecipeDetailActivity extends BaseActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        setupToolbar("Recipe", true);

        Recipe r = PantryDbHelper.getInstance(this).getRecipe(getIntent().getLongExtra(EXTRA_RECIPE_ID, -1));
        if (r == null) { finish(); return; }

        ((TextView) findViewById(R.id.tvDetailName)).setText(r.getName());
        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ing : r.getIngredients()) {
            sb.append("\u2022 ").append(PantryAdapter.formatQty(ing.quantity)).append(" ")
              .append(ing.unit).append(" ").append(ing.name).append("\n");
        }
        ((TextView) findViewById(R.id.tvDetailIngredients)).setText(sb.toString().trim());
        ((TextView) findViewById(R.id.tvDetailSteps)).setText(r.getSteps());
    }
}
