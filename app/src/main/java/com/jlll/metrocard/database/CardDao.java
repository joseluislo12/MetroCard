package com.jlll.metrocard.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.jlll.metrocard.model.Card;

import java.util.List;

@Dao
public interface CardDao {
    @Query("SELECT * FROM CARD ORDER BY ID")
    List<Card> loadAllCards();

    @Insert
    void insertCard(Card card);

    @Update
    void updateCard(Card card);

    @Delete
    void delete(Card card);

    @Query("SELECT * FROM CARD WHERE id  = :id")
    Card loadCardById(int id);
}