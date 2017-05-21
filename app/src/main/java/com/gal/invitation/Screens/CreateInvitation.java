package com.gal.invitation.Screens;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gal.invitation.R;

public class CreateInvitation extends AppCompatActivity {
    EditText date;
    EditText time;
    DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invitation);


        final Spinner eventType = (Spinner) findViewById(R.id.eventType);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(CreateInvitation.this,
                R.array.type, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        eventType.setAdapter(adapter);


        eventType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                eventSelected(eventType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;

            }
        });


        date = (EditText) findViewById(R.id.eventDate);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int eventYear = calendar.get(Calendar.YEAR);
                int eventMonth = calendar.get(Calendar.MONTH);
                int eventDay = calendar.get(Calendar.DAY_OF_MONTH);


                datePickerDialog = new DatePickerDialog(CreateInvitation.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                date.setText(day + "/" + (month + 1) + "/" + year);
                            }
                        }, eventYear, eventMonth, eventDay);
                datePickerDialog.show();
            }
        });

        time = (EditText) findViewById(R.id.eventTime);

        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar eventTime = Calendar.getInstance();
                int hour = eventTime.get(Calendar.HOUR_OF_DAY);
                int minute = eventTime.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog;
                timePickerDialog = new TimePickerDialog(CreateInvitation.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if(selectedMinute<10)
                            time.setText((selectedHour + ":" + "0" + selectedMinute));
                        else
                            time.setText((selectedHour + ":" + selectedMinute));
                    }
                }, hour, minute, true);
                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            }
        });

        final Spinner eventPlaceType = (Spinner) findViewById(R.id.eventPlaceType);

        ArrayAdapter<CharSequence> placeTypeAdapter = ArrayAdapter.createFromResource(CreateInvitation.this,
                R.array.placeType, android.R.layout.simple_spinner_item);

        placeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        eventPlaceType.setAdapter(placeTypeAdapter);


        eventPlaceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                eventPlaceTypeSelected(eventPlaceType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;

            }
        });



    }

    public void eventSelected(Spinner eventType){
        String type = eventType.getSelectedItem().toString();
        EditText weddingGroom = (EditText) findViewById(R.id.eventGroom);
        EditText weddingBride = (EditText) findViewById(R.id.eventBride);
        EditText weddingParents = (EditText) findViewById(R.id.eventParents);

        Toast.makeText(CreateInvitation.this,
                type,
                Toast.LENGTH_LONG).show();

        if (type.contentEquals("Wedding")){
            weddingGroom.setVisibility(View.VISIBLE);
            weddingBride.setVisibility(View.VISIBLE);
            weddingParents.setVisibility(View.GONE);
        }
        if (type.contentEquals("Other")) {
            weddingGroom.setVisibility(View.GONE);
            weddingBride.setVisibility(View.GONE);
            weddingParents.setVisibility(View.VISIBLE);
        }
    }

    public void eventPlaceTypeSelected(Spinner eventPlaceType){

    }
}
