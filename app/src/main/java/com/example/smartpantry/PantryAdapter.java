package com.example.smartpantry;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Custom RecyclerView adapter that binds pantry rows from the database to item_pantry.xml. */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(PantryItem item);
        void onDelete(PantryItem item);
    }

    private List<PantryItem> items = new ArrayList<>();
    private boolean expiryAlerts = true;
    private final Listener listener;

    public PantryAdapter(Listener listener) { this.listener = listener; }

    public void setItems(List<PantryItem> items, boolean expiryAlerts) {
        this.items = items;
        this.expiryAlerts = expiryAlerts;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvQty, tvExpiry;
        final Button btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvItemName);
            tvQty = v.findViewById(R.id.tvItemQty);
            tvExpiry = v.findViewById(R.id.tvItemExpiry);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        final PantryItem item = items.get(position);
        h.tvName.setText(item.getName());
        h.tvQty.setText(formatQty(item.getQuantity()) + " " + item.getUnit());

        String expiry = item.getExpiry();
        if (expiry == null || expiry.isEmpty()) {
            h.tvExpiry.setText("No expiry date");
            h.tvExpiry.setTextColor(Color.GRAY);
        } else {
            h.tvExpiry.setText("Expires: " + expiry);
            h.tvExpiry.setTextColor(Color.GRAY);
            if (expiryAlerts) {
                long days = daysUntil(expiry);
                if (days < 0) {
                    h.tvExpiry.setText("EXPIRED (" + expiry + ")");
                    h.tvExpiry.setTextColor(Color.RED);
                } else if (days <= 3) {
                    h.tvExpiry.setText("Expires in " + days + " day(s) (" + expiry + ")");
                    h.tvExpiry.setTextColor(Color.rgb(230, 120, 0));
                }
            }
        }
        h.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static String formatQty(double q) {
        return q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q);
    }

    private long daysUntil(String yyyyMMdd) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date target = f.parse(yyyyMMdd);
            Date today = f.parse(f.format(new Date()));
            return (target.getTime() - today.getTime()) / (24L * 60 * 60 * 1000);
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }
}
