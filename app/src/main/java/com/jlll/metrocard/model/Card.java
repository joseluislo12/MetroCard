package com.jlll.metrocard.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "card")
public class Card {
    @PrimaryKey(autoGenerate = true)
    public int id;
    String name;
    float amount;
    String expDate;

    @Ignore
    public Card(String name, float amount, String expDate){
        this.name = name;
        this.amount = amount;
        this.expDate = expDate;
    }

    public Card(int id, String name, float amount, String expDate){
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.expDate = expDate;
    }

    public int getID() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public float getAmount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }

    public String getExpDate() { return expDate; }
    public void setExpDate(String expDate) { this.expDate = expDate; }
}