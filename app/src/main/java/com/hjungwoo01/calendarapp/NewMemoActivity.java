package com.hjungwoo01.calendarapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewMemoActivity extends AppCompatActivity {
    private TextView receiversTextView;
    private boolean[] selectedReceivers;
    private List<Integer> receiversList = new ArrayList<>();
    private final String[] receiversArray = {"Person1", "Person2", "Person3", "Person4", "Person5"};
    private TextInputEditText inputEditMemoName;
    private TextInputEditText inputEditMemo;
    private MaterialButton buttonSave;
    private String receiversString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memo);
        initializeComponents();
    }

    private void initializeComponents() {
        receiversTextView = findViewById(R.id.form_receiversTextView);
        inputEditMemoName = findViewById(R.id.form_textFieldMemoName);
        inputEditMemo = findViewById(R.id.form_textFieldMemo);
        buttonSave = findViewById(R.id.form_buttonSave);

        selectedReceivers = new boolean[receiversArray.length];

        RetrofitService retrofitService = new RetrofitService();
        MemoApi memoApi = retrofitService.getRetrofit().create(MemoApi.class);

        receiversTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NewMemoActivity.this);

                builder.setTitle("Send Memo To: ");
                builder.setCancelable(false);
                builder.setMultiChoiceItems(receiversArray, selectedReceivers, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if(isChecked) {
                            receiversList.add(which);
                        } else {
                            receiversList.remove(Integer.valueOf(which));
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder sb = new StringBuilder();
                        for(int i  = 0; i < receiversList.size(); i++) {
                            sb.append(receiversArray[receiversList.get(i)]);
                            if(i != receiversList.size() - 1) {
                                sb.append(",");
                            }
                        }
                        receiversString = sb.toString();
                        receiversTextView.setText(receiversString);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i = 0; i < selectedReceivers.length; i++) {
                            selectedReceivers[i] = false;
                            receiversList.clear();
                            receiversTextView.setText("");
                        }
                    }
                });
                builder.show();
            }

        });

        buttonSave.setOnClickListener(view -> {
            String memoName = String.valueOf(inputEditMemoName.getText());
            String memo = String.valueOf(inputEditMemo.getText());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm", Locale.KOREA);
            String date = sdf.format(new Date());

            Memo newMemo = new Memo();
            newMemo.setOwner(OwnerSelectionActivity.getSelectedOwner());
            newMemo.setReceiver(receiversString);
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
