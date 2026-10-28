package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** Runs the strict-matching rule against the current pantry and lists only fully makeable recipes. */
public class SuggestedRecipesActivity extends BaseActivity implements RecipeAdapter.Listener {

    private PantryDbHelper db;
    private RecipeAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setupToolbar("Suggested Recipes", true);

        db = PantryDbHelper.getInstance(this);
        tvEmpty = findViewById(R.id.tvNoMatch);
        RecyclerView rv = findViewById(R.id.rvRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        rv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Recipe> matches = RecipeMatcher.suggest(db.getAllRecipes(), db.getAllItems());
        adapter.setRecipes(matches);
        tvEmpty.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent i = new Intent(this, RecipeDetailActivity.class);
        i.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(i);
    }
}
