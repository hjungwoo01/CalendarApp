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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_selection);

        Button selectOwnerButton = findViewById(R.id.select_owner_button);
        Spinner ownerSpinner = findViewById(R.id.owner_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.owners, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ownerSpinner.setAdapter(adapter);
        ownerSpinner.setOnItemSelectedListener(this);

        selectOwnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start MainActivity and pass the selected owner ID
                Intent intent = new Intent(OwnerSelectionActivity.this, MainActivity.class);
                intent.putExtra("selected_owner", selectedOwner);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedOwner = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedOwner = "Person1";
    }

    public static String getSelectedOwner() {
        return selectedOwner;
    }
}
