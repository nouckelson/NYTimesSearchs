package com.example.cwong.nytimessearch.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.cwong.nytimessearch.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by cwong on 8/10/16.
 */
public class SettingsFragment extends android.support.v4.app.DialogFragment {
    EditText etDate;
    Button btnSave;
    Spinner spinner;
    ArrayList<String> checkedValues;
    CheckBox checkArts;
    CheckBox checkFashionStyle;
    CheckBox checkSports;
    String date;
    String sortOrder;
    ArrayList<String> newsDeskValues;

    public SettingsFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }
    public static SettingsFragment newInstance(String date, String sortOrder, ArrayList<String> newsDeskValues) {
        SettingsFragment frag = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString("date", date);
        args.putString("sortOrder", sortOrder);
        args.putStringArrayList("newsDeskValues", newsDeskValues);
        frag.setArguments(args);
        return frag;
    }
    public interface SettingsDialogListener {
        void onFinishSettingsDialog(String date, String sortOrder, ArrayList<String> newsDeskValues);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        etDate = (EditText) view.findViewById(R.id.etDate);
        btnSave = (Button) view.findViewById(R.id.btnSaveSettings);
        spinner = (Spinner) view.findViewById(R.id.spinnerSort);
        checkArts = (CheckBox) view.findViewById(R.id.cbArts);
        checkFashionStyle = (CheckBox) view.findViewById(R.id.cbFashionStyle);
        checkSports = (CheckBox) view.findViewById(R.id.cbSports);
        checkedValues = new ArrayList<>();

        setupCheckboxes();
        date = getArguments().getString("date");
        sortOrder = getArguments().getString("sortOrder");
        newsDeskValues = getArguments().getStringArrayList("newsDeskValues");

        if (date.length() > 0) {
            etDate.setText(date);
        }
        spinner.setSelection(((ArrayAdapter<String>)spinner.getAdapter()).getPosition(sortOrder));

        if (newsDeskValues.contains(checkArts.getText().toString())) {
            checkArts.setChecked(true);
        }
        if (newsDeskValues.contains(checkFashionStyle.getText().toString())) {
            checkFashionStyle.setChecked(true);
        }
        if (newsDeskValues.contains(checkSports.getText().toString())) {
            checkSports.setChecked(true);
        }

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsDialogListener listener = (SettingsDialogListener) getActivity();
                sortOrder = spinner.getSelectedItem().toString();
                listener.onFinishSettingsDialog(date, sortOrder, checkedValues);
                // Close the dialog and return back to the parent activity
                dismiss();
            }
        });

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }


    public void onFinishDatePicking(int year, int month, int day) {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        String format = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        date = sdf.format(c.getTime());
        etDate.setText(date);
    }
    public void showDatePickerDialog(View v) {
        String format = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        final Calendar c = Calendar.getInstance();

        try {
            c.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Activity needs to implement this interface
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                onFinishDatePicking(year, monthOfYear, dayOfMonth);
            }
        };

        DatePickerDialog dg = new DatePickerDialog(getActivity(), listener, year, month, day);
        dg.show();
    }

    public void setupCheckboxes() {
        // Fires every time a checkbox is checked or unchecked
        CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean checked) {
                // compoundButton is the checkbox
                // boolean is whether or not checkbox is checked
                // Check which checkbox was clicked
                switch(view.getId()) {
                    case R.id.cbArts:
                        if (checked) {
                            checkedValues.add(checkArts.getText().toString());
                        } else {
                            checkedValues.remove(checkArts.getText().toString());
                        }
                        break;
                    case R.id.cbFashionStyle:
                        if (checked) {
                            checkedValues.add(checkFashionStyle.getText().toString());
                        } else {
                            checkedValues.remove(checkFashionStyle.getText().toString());
                        }
                        break;
                    case R.id.cbSports:
                        if (checked) {
                            checkedValues.add(checkSports.getText().toString());
                        } else {
                            checkedValues.remove(checkSports.getText().toString());
                        }
                        break;
                }
            }
        };
        checkArts.setOnCheckedChangeListener(checkListener);
        checkFashionStyle.setOnCheckedChangeListener(checkListener);
        checkSports.setOnCheckedChangeListener(checkListener);
    }
}
