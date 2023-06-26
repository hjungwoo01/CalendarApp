package com.hjungwoo01.calendarapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

public class MemoDetailsActivity extends AppCompatActivity {
    private Memo memo;
    private TextInputEditText inputEditMemoReceiver;
    private TextInputEditText inputEditMemoName;
    private TextInputEditText inputEditMemo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_details);
        initWidgets();

        long memoId = getIntent().getLongExtra("memoId", -1);

        if (memoId != -1) {
            fetchMemoDetails(memoId);

            MaterialButton updateButton = findViewById(R.id.form_buttonUpdate);

            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String receiver = String.valueOf(inputEditMemoReceiver.getText());
                    String memoName = String.valueOf(inputEditMemoName.getText());
                    String memo = String.valueOf(inputEditMemo.getText());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.KOREA);
                    String date = sdf.format(new Date());
                    if(!receiver.isEmpty()) {
                        Memo updatedMemo = new Memo();
                        updatedMemo.setOwner(OwnerSelectionActivity.getSelectedOwner());
                        updatedMemo.setReceiver(receiver);
                        updatedMemo.setMemoName(memoName);
                        updatedMemo.setMemo(memo);
                        updatedMemo.setDate(date);

                        updateMemo(memoId, updatedMemo);
                    } else {
                        Toast.makeText(MemoDetailsActivity.this, "Invalid input. Please check your inputs and try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            MaterialButton deleteButton = findViewById(R.id.form_buttonDelete);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteConfirmationDialog(memoId);
                }
            });
        } else {
            Toast.makeText(this, "Memo ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initWidgets() {
        inputEditMemoReceiver = findViewById(R.id.form_textFieldReceiver);
        inputEditMemoName = findViewById(R.id.form_textFieldMemoName);
        inputEditMemo = findViewById(R.id.form_textFieldMemo);
    }

    private void fetchMemoDetails(long memoId) {
        RetrofitService retrofitService = new RetrofitService();
        MemoApi memoApi = retrofitService.getRetrofit().create(MemoApi.class);

        memoApi.getMemo(memoId).enqueue(new Callback<Memo>() {
            @Override
            public void onResponse(@NonNull Call<Memo> call, @NonNull Response<Memo> response) {
                if (response.isSuccessful()) {
                    memo = response.body();
                    if (memo != null) {
                        displayMemoDetails();
                    } else {
                        Toast.makeText(MemoDetailsActivity.this, "Memo not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(MemoDetailsActivity.this, "Failed to fetch memo details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Memo> call, @NonNull Throwable t) {
                Toast.makeText(MemoDetailsActivity.this, "Failed to fetch memo details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayMemoDetails() {
        if(memo.getReceiver() != null) {
            inputEditMemoReceiver.setText(memo.getReceiver());
        }
        if(memo.getMemoName() != null) {
            inputEditMemoName.setText(memo.getMemoName());
        }
        if(memo.getMemo() != null) {
            inputEditMemo.setText(memo.getMemo());
        }
    }

    private void updateMemo(long memoId, Memo memo) {
        RetrofitService retrofitService = new RetrofitService();
        MemoApi memoApi = retrofitService.getRetrofit().create(MemoApi.class);

        memoApi.updateMemo(memoId, memo).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MemoDetailsActivity.this, "Update successful.", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(MemoDetailsActivity.this, MemoActivity.class));
                    finish();
                } else {
                    Toast.makeText(MemoDetailsActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(MemoDetailsActivity.this, "Update failed.", Toast.LENGTH_SHORT).show();
                Log.e("MemoDetailsActivity", "Error occurred: " + t.getMessage());
            }
        });
    }

    private void showDeleteConfirmationDialog(long memoId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Memo");
        builder.setMessage("Are you sure you want to delete this memo?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteMemo(memoId);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteMemo(long memoId) {
        RetrofitService retrofitService = new RetrofitService();
        MemoApi memoApi = retrofitService.getRetrofit().create(MemoApi.class);
        memoApi.deleteMemo(memoId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MemoDetailsActivity.this, "Memo deleted.", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(MemoDetailsActivity.this, MemoActivity.class));
                    finish();
                } else {
                    Toast.makeText(MemoDetailsActivity.this, "Failed to delete memo.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(MemoDetailsActivity.this, "Failed to delete memo.", Toast.LENGTH_SHORT).show();
                Log.e("MemoDetailsActivity", "Error occurred: " + t.getMessage());
            }
        });
    }

    public void backToMemoMain(View view) {
        startActivity(new Intent(MemoDetailsActivity.this, MemoActivity.class));
    }
}
