package com.hjungwoo01.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hjungwoo01.calendarapp.model.Memo;
import com.hjungwoo01.calendarapp.retrofit.MemoApi;
import com.hjungwoo01.calendarapp.retrofit.RetrofitService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewMemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memo);
        initializeComponents();
    }


    private void initializeComponents() {
        TextInputEditText inputEditMemoReceiver = findViewById(R.id.form_textFieldReceiver);
        TextInputEditText inputEditMemoName = findViewById(R.id.form_textFieldMemoName);
        TextInputEditText inputEditMemo = findViewById(R.id.form_textFieldMemo);
        MaterialButton buttonSave = findViewById(R.id.form_buttonSave);

        RetrofitService retrofitService = new RetrofitService();
        MemoApi memoApi = retrofitService.getRetrofit().create(MemoApi.class);

        buttonSave.setOnClickListener(view -> {

            String receiver = String.valueOf(inputEditMemoReceiver.getText());
            String memoName = String.valueOf(inputEditMemoName.getText());
            String memo = String.valueOf(inputEditMemo.getText());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.KOREA);
            String date = sdf.format(new Date());

            Memo newMemo = new Memo();
            newMemo.setOwner(OwnerSelectionActivity.getSelectedOwner());
            newMemo.setReceiver(receiver);
            newMemo.setMemoName(memoName);
            newMemo.setMemo(memo);
            newMemo.setDate(date);

            memoApi.save(newMemo).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(NewMemoActivity.this, "Save successful.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(NewMemoActivity.this, MemoActivity.class));
                        finish();
                    } else {
                        Toast.makeText(NewMemoActivity.this, "Save failed.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(NewMemoActivity.this, "Save failed.", Toast.LENGTH_SHORT).show();
                    Log.e("NewMemoActivity", "Error occurred: " + t.getMessage());
                }
            });
        });
    }

    public void backToMemoMain(View view) {
        startActivity(new Intent(NewMemoActivity.this, MemoActivity.class));
    }

}
