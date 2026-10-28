package com.example.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** RecyclerView adapter for the Suggested Recipes list. */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener { void onRecipeClick(Recipe recipe); }

    private List<Recipe> recipes = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) { this.listener = listener; }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvInfo;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvRecipeName);
            tvInfo = v.findViewById(R.id.tvRecipeInfo);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final Recipe r = recipes.get(position);
        h.tvName.setText(r.getName());
        h.tvInfo.setText(r.getIngredients().size() + " ingredients - tap for method");
        h.itemView.setOnClickListener(v -> listener.onRecipeClick(r));
    }

    @Override
    public int getItemCount() { return recipes.size(); }
}
