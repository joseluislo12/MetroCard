package com.jlll.metrocard.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.jlll.metrocard.R;
import com.jlll.metrocard.constants.Constants;
import com.jlll.metrocard.database.AppDatabase;
import com.jlll.metrocard.database.AppExecutors;
import com.jlll.metrocard.model.Card;

import java.text.DecimalFormat;
import java.util.Objects;

import me.abhinay.input.CurrencyEditText;

public class CardInfo extends AppCompatActivity {
    EditText name, expDate;
    CurrencyEditText amount;
    Button button, btnR, btnT;
    TextView lblAmount;
    LinearLayout llBtns;
    int mCardId;
    Intent intent;
    private AppDatabase mDb;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initViews();

        mDb = AppDatabase.getInstance(getApplicationContext());
        intent = getIntent();
        if((intent != null) && (intent.hasExtra(Constants.UPDATE_Card_Id))){
            button.setText(R.string.btnUpdate);

            llBtns.setVisibility(View.VISIBLE);
            lblAmount.setText(R.string.Card_Amnt);

            mCardId = intent.getIntExtra(Constants.UPDATE_Card_Id, -1);

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    Card card = mDb.cardDao().loadCardById(mCardId);
                    populateUI(card);
                }
            });
        }
    }

    private void populateUI(final Card card){
        if(card == null){ return; }

        name.setText(card.getName());
        expDate.setText(card.getExpDate());

        float number = card.getAmount();
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String numberAsString = decimalFormat.format(number);
        amount.setText(numberAsString);

        amount.setEnabled(false);
        expDate.setEnabled(false);

        btnR = findViewById(R.id.btnRch);
        btnR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Recharge Amount");

                final me.abhinay.input.CurrencyEditText input = new me.abhinay.input.CurrencyEditText(context);
                input.setCurrency("$");
                input.setSpacing(true);

                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        double dNew = input.getCleanDoubleValue();
                        float fNew = Float.parseFloat(String.valueOf(dNew));

                        incrBalance(fNew, card.getID());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        btnT = findViewById(R.id.btnTrip);
        btnT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dcrBalanceOneTrip(card.getID());
            }
        });
    }

    private void incrBalance(final float addValue, final int idCard) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Card card = mDb.cardDao().loadCardById(idCard);

                float cAmount = card.getAmount();
                cAmount += addValue;
                card.setAmount(cAmount);

                mDb.cardDao().updateCard(card);

                finish();
            }
        });
    }

    private void dcrBalanceOneTrip(final int idCard){
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Card card = mDb.cardDao().loadCardById(idCard);

                float cAmount = card.getAmount();
                cAmount -= 2.75;
                card.setAmount(cAmount);

                mDb.cardDao().updateCard(card);

                finish();
            }
        });
    }

    private void initViews(){
        name = findViewById(R.id.txtName);
        expDate = findViewById(R.id.txtExp);

        amount = findViewById(R.id.txtIni);
        amount.setCurrency("$");
        amount.setSpacing(true);

        button = findViewById(R.id.btnNew);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean toSave;
                String msg = "";

                if(name.getText().toString().isEmpty()){
                    toSave = false;
                    msg = "Type a Card's Name";
                } else if(Objects.requireNonNull(amount.getText()).toString().isEmpty()){
                    toSave = false;
                    msg = "Type a Card's Amount";
                } else if(expDate.getText().toString().isEmpty()) {
                    toSave = false;
                    msg = "Type a Card's Expiration Date";
                } else { toSave = true; }

                if(toSave) { onSaveButtonClicked(v); }
                else { Snackbar.make(v, msg, BaseTransientBottomBar.LENGTH_LONG).show(); }
            }
        });

        llBtns = findViewById(R.id.llBtns);
        lblAmount = findViewById(R.id.lblIni);
    }

    public void onSaveButtonClicked(View v){
        double dAmnt = amount.getCleanDoubleValue();
        float fAmt = Float.parseFloat(String.valueOf(dAmnt));

        final Card card = new Card(
                name.getText().toString(),
                fAmt,
                expDate.getText().toString()
        );

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if(!intent.hasExtra(Constants.UPDATE_Card_Id)){
                    mDb.cardDao().insertCard(card);
                } else{
                    card.setId(mCardId);
                    mDb.cardDao().updateCard(card);
                }
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
