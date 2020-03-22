package com.jlll.metrocard.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.Objects;

import me.abhinay.input.CurrencyEditText;

public class CardInfo extends AppCompatActivity {
    EditText name, expDate;
    CurrencyEditText amount;
    Button button, btnR, btnT;
    TextView lblAmount, numberRides;
    LinearLayout llBtns;
    int mCardId;
    Intent intent;
    private AppDatabase mDb;
    final Context context = this;
    DatePickerDialog picker;

    Calendar cal = Calendar.getInstance();
    int cDay = cal.get(Calendar.DAY_OF_MONTH);
    int cMonth = cal.get(Calendar.MONTH) + 1;
    int cYear = cal.get(Calendar.YEAR);

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
                dcrBalanceOneTrip(card.getID(), v);
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

    private void dcrBalanceOneTrip(final int idCard, final View v){
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Card card = mDb.cardDao().loadCardById(idCard);

                float cAmount = card.getAmount();
                cAmount -= 2.75;

                if(cAmount < 0f){ Snackbar.make(v, "Insufficient funds", BaseTransientBottomBar.LENGTH_LONG).show(); }
                else {
                    card.setAmount(cAmount);

                    mDb.cardDao().updateCard(card);

                    finish();
                }
            }
        });
    }

    private void initViews(){
        name = findViewById(R.id.txtName);
        expDate = findViewById(R.id.txtExp);
        expDate.setInputType(InputType.TYPE_NULL);

        amount = findViewById(R.id.txtIni);
        amount.setCurrency("$");
        amount.setSpacing(true);
        numberRides = findViewById(R.id.numberRides);
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                double dAmnt = amount.getCleanDoubleValue();
                double rides = dAmnt / 2.75;
                double rmd = dAmnt % 2.75;
                int numRides = (int) Math.floor(rides);

                String nmRds = "Number of Rides: ";
                nmRds += Integer.toString(numRides);
                if(rmd > 0) { nmRds += " + $ " + rmd; }

                numberRides.setText(nmRds);
            }
        });

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
                } else {
                    String date = expDate.getText().toString();
                    String[] date_parts = date.split("/", 3);
                    int m = Integer.parseInt(date_parts[0]);
                    int d = Integer.parseInt(date_parts[1]);
                    int y = Integer.parseInt(date_parts[2]);

                    toSave = true;

                    if(y <= cYear){
                        if(m <= cMonth){
                            if(d <= cDay){
                                toSave = false;
                                msg = "Select another date";
                            }
                        }
                    }
                }

                if(toSave) { onSaveButtonClicked(v); }
                else { Snackbar.make(v, msg, BaseTransientBottomBar.LENGTH_LONG).show(); }
            }
        });

        llBtns = findViewById(R.id.llBtns);
        lblAmount = findViewById(R.id.lblIni);

        expDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);

                picker = new DatePickerDialog(CardInfo.this, android.R.style.Theme_Holo_Dialog,
                        new DatePickerDialog.OnDateSetListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                expDate.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });
        expDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);

                picker = new DatePickerDialog(CardInfo.this, android.R.style.Theme_Holo_Dialog,
                        new DatePickerDialog.OnDateSetListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                expDate.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });
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
