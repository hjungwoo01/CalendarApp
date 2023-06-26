package com.hjungwoo01.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class OwnerSelectionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static String selectedOwner;
    public static String selectedActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_selection);
        Button selectButton = findViewById(R.id.select_button);
        Spinner ownerSpinner = findViewById(R.id.owner_spinner);
        Spinner activitySpinner = findViewById(R.id.activity_spinner);

        ArrayAdapter<CharSequence> ownerAdapter = ArrayAdapter.createFromResource(this, R.array.owners, android.R.layout.simple_spinner_item);
        ownerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ownerSpinner.setAdapter(ownerAdapter);
        ownerSpinner.setOnItemSelectedListener(this);

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.owner_spinner) {
            selectedOwner = parent.getItemAtPosition(position).toString();
        } else if (parent.getId() == R.id.activity_spinner) {
            selectedActivity = parent.getItemAtPosition(position).toString();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedOwner = "Person1";
        selectedActivity = "Scheduler";
    }

    public static String getSelectedOwner() {
        return selectedOwner;
    }

}
