package com.hjungwoo01.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.hjungwoo01.calendarapp.model.User;
import com.hjungwoo01.calendarapp.retrofit.RetrofitService;
import com.hjungwoo01.calendarapp.retrofit.UserApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OwnerSelectionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static String selectedOwner;
    public static String selectedActivity;
    private Button selectButton;
    private Spinner ownerSpinner;
    private Spinner activitySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_selection);
        selectButton = findViewById(R.id.select_button);
        ownerSpinner = findViewById(R.id.owner_spinner);
        activitySpinner = findViewById(R.id.activity_spinner);

        fetchUsers();

        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(this, R.array.activities, android.R.layout.simple_spinner_item);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        activitySpinner.setAdapter(activityAdapter);
        activitySpinner.setOnItemSelectedListener(this);

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedActivity.equals("Scheduler")) {
                    Intent intent = new Intent(OwnerSelectionActivity.this, MainActivity.class);
                    intent.putExtra("selected_owner", selectedOwner);
                    startActivity(intent);
                } else if (selectedActivity.equals("Memo")) {
                    Intent intent = new Intent(OwnerSelectionActivity.this, MemoActivity.class);
                    intent.putExtra("memo_owner", selectedOwner);
                    startActivity(intent);
                }
                finish();
            }
        });
    }

    private void fetchUsers() {
        RetrofitService retrofitService = new RetrofitService();
        UserApi userApi = retrofitService.getRetrofit().create(UserApi.class);

        userApi.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful()) {
                    User.setUsersList(response.body());
                    Log.d("UserList", User.getUsersList().toString());

                    UserAdapter userAdapter = new UserAdapter(OwnerSelectionActivity.this, User.getUsersList());
                    userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    ownerSpinner.setAdapter(userAdapter);
                    ownerSpinner.setOnItemSelectedListener(OwnerSelectionActivity.this);
                } else {
                    Toast.makeText(OwnerSelectionActivity.this, "No users.", Toast.LENGTH_SHORT).show();
                    User.setUsersList(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Toast.makeText(OwnerSelectionActivity.this, "Failed to fetch users.", Toast.LENGTH_SHORT).show();
                Log.e("OwnerSelectionActivity", "Error occurred: " + t.getMessage());
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.owner_spinner) {
            selectedOwner = User.getUsersList().get(position).getName();
        } else if (parent.getId() == R.id.activity_spinner) {
            selectedActivity = parent.getItemAtPosition(position).toString();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedOwner = User.getUsersList().get(0).getName();
        selectedActivity = "Scheduler";
    }

    public static String getSelectedOwner() {
        return selectedOwner;
    }

}
