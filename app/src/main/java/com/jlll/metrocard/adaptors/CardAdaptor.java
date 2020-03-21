package com.jlll.metrocard.adaptors;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jlll.metrocard.R;
import com.jlll.metrocard.activities.CardInfo;
import com.jlll.metrocard.constants.Constants;
import com.jlll.metrocard.database.AppDatabase;
import com.jlll.metrocard.model.Card;

import java.text.DecimalFormat;
import java.util.List;

public class CardAdaptor extends RecyclerView.Adapter<CardAdaptor.MyViewHolder> {
    private Context context;
    private List<Card> mCardList;

    public CardAdaptor(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardAdaptor.MyViewHolder myViewHolder, int i) {
        myViewHolder.name.setText(mCardList.get(i).getName());
        myViewHolder.expDate.setText(mCardList.get(i).getExpDate());

        float number = mCardList.get(i).getAmount();
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String numberAsString = decimalFormat.format(number);
        myViewHolder.amount.setText(numberAsString);
    }

    @Override
    public int getItemCount(){
        if(mCardList == null) { return 0; }
        return mCardList.size();
    }

    public void setTasks(List<Card> cardList){
        mCardList = cardList;
        notifyDataSetChanged();
    }

    public List<Card> getTasks(){
        return mCardList;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView name, amount, expDate;
        ImageView editCard;
        AppDatabase mDb;

        MyViewHolder(@NonNull final View itemView){
            super(itemView);

            mDb = AppDatabase.getInstance(context);
            name = itemView.findViewById(R.id.cardName);
            amount = itemView.findViewById(R.id.cardAmount);
            expDate = itemView.findViewById(R.id.cardExp);

            editCard = itemView.findViewById(R.id.editCard);
            editCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int elementId = mCardList.get(getAdapterPosition()).getID();
                    Intent i = new Intent(context, CardInfo.class);
                    i.putExtra(Constants.UPDATE_Card_Id, elementId);
                    context.startActivity(i);
                }
            });
        }
    }
}