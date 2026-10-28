package com.example.smartpantry;

/** Model class for one pantry row. Id of -1 means "not yet saved" (same idea as the Contact class in the study guide). */
public class PantryItem {
    private long id = -1;
    private String name;
    private double quantity;
    private String unit;
    private String expiry; // yyyy-MM-dd or empty

    public PantryItem() { }
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }
}
