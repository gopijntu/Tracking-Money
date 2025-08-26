package com.example.expensetracker.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String smsBody;
    public double amount;
    public String type;
    public String merchant;
    public String category;
    public long date;

    // Room requires a public constructor
    public Transaction() {}

    public Transaction(String smsBody, double amount, String type, String merchant, String category, long date) {
        this.smsBody = smsBody;
        this.amount = amount;
        this.type = type;
        this.merchant = merchant;
        this.category = category;
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id == that.id &&
                Double.compare(that.amount, amount) == 0 &&
                date == that.date &&
                java.util.Objects.equals(smsBody, that.smsBody) &&
                java.util.Objects.equals(type, that.type) &&
                java.util.Objects.equals(merchant, that.merchant) &&
                java.util.Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, smsBody, amount, type, merchant, category, date);
    }
}
