package com.ennjapps.smsit.Activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ennjapps.smsit.AlarmReceiver;
import com.ennjapps.smsit.DbHelper;
import com.ennjapps.smsit.MyPreferences;
import com.ennjapps.smsit.R;
import com.ennjapps.smsit.SmsModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;

public class AddSmsActivity extends AppCompatActivity implements View.OnClickListener {

    //Time and DatePicker Initialization
    ImageView btnDatePicker, btnTimePicker;
    EditText formDate, formTime;
    private int mYear, mMonth, mDay, mHour, mMinute;

    DatePickerDialog datePickerDialog;

    //Permissions required for App
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    final private String[] permissionsRequired = new String[]{Manifest.permission.SEND_SMS,Manifest.permission.READ_CONTACTS};
    private SmsModel sms;
    private ArrayList<String> permissionsGranted = new ArrayList<String>();

    //Time and Date picker handled
    @Override
    public void onClick(View v) {
        if (v == btnDatePicker) {

            // Get Current Date
            final Calendar c = getInstance();
            mYear = c.get(YEAR);
            mMonth = c.get(MONTH);
            mDay = c.get(DAY_OF_MONTH);

            // Launch Time Picker Dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            formDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }

        if (v == btnTimePicker) {

            // Get Current Time
            final Calendar c = getInstance();
            mHour = c.get(HOUR_OF_DAY);
            mMinute = c.get(MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            formTime.setText(hourOfDay + ":" + minute);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_sms);

        //Checking and displaying corresponding message wheteher user is logged in first time
        MyPreferences pref = new MyPreferences(AddSmsActivity.this);
        if (pref.isFirstTime()) {
            Toast.makeText(AddSmsActivity.this, "Hi" + pref.getUsername(), Toast.LENGTH_LONG);
            pref.setOld(true);
        } else {
            Toast.makeText(AddSmsActivity.this, "Welcome back"+" " + pref.getUsername(), Toast.LENGTH_LONG).show();
        }

        //Initialize Time and Date picker view
        btnDatePicker = (ImageView) findViewById(R.id.btn_date);
        btnTimePicker = (ImageView) findViewById(R.id.btn_time);

        //Initialize Message field
        formDate = (EditText) findViewById(R.id.form_date);

         //Set clicklistener for time and date picker
        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);


        // Filling existing sms info if possible
        long smsId = getSmsId(savedInstanceState);
        if (smsId > 0) {
            sms = DbHelper.getDbHelper(this).get(smsId);
        } else {
            sms = new SmsModel();
        }
    }

     // Inflating menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Handling menu options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivityForResult(new Intent(this, SmsSchedulerPreferenceActivity.class), 1);
                break;
        }
        return true;
    }

    //Called when user is interacting with UI
    @Override
    protected void onResume() {
        super.onResume();
        if (permissionsGranted()) {
            buildForm();
        }
    }

    private void buildForm() {
        final Calendar timeScheduled = getInstance();

        //Empty fields when activity starts
        String recipient = "", message = "";

        //Initiliaize fields
        final AutoCompleteTextView formContact = (AutoCompleteTextView) findViewById(R.id.form_input_contact);
        final EditText formMessage = (EditText) findViewById(R.id.form_input_message);
        formTime = (EditText) findViewById(R.id.form_time);
        formDate = (EditText) findViewById(R.id.form_date);
        final Button buttonCancel = (Button) findViewById(R.id.button_cancel);

        // Filling form with data if provided
        if (sms.getTimestampCreated() > 0) {
            timeScheduled.setTimeInMillis(sms.getTimestampScheduled());
            recipient = sms.getRecipientName().length() > 0
                    ? getString(R.string.contact_format, sms.getRecipientName(), sms.getRecipientNumber())
                    : sms.getRecipientNumber()
            ;
            message = sms.getMessage();
        }

        formContact.setText(recipient);
        formMessage.setText(message);
        buttonCancel.setVisibility(sms.getTimestampCreated() > 0 ? View.VISIBLE : View.GONE);
        int stringId = sms.getStatus().contentEquals(sms.STATUS_PENDING)
                ? R.string.form_button_cancel
                : R.string.form_button_delete;
        buttonCancel.setText(getString(stringId));

        // Filling contacts list
        new Thread(new Runnable() {
            @Override
            public void run() {
                final SimpleAdapter adapter = new SimpleAdapter(
                        getApplicationContext(),
                        getContacts(),
                        R.layout.item_contact,
                        new String[]{"Name", "Phone"},
                        new int[]{R.id.account_name, R.id.account_number}
                );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        formContact.setAdapter(adapter);
                    }
                });
            }
        }).start();
        formContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> recipient = (HashMap<String, String>) parent.getItemAtPosition(position);
                String name = recipient.get("Name"), phone = recipient.get("Phone");
                formContact.setText(getString(R.string.contact_format, name, phone));
                sms.setRecipientName(name);
                sms.setRecipientNumber(phone);
            }
        });
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!formContact.isPerformingCompletion()) {
                    sms.setRecipientName("");
                    sms.setRecipientNumber(String.valueOf(s));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        formContact.addTextChangedListener(watcher);

        // Adding emptiness checks
        TextWatcher watcherEmptiness = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final Button button = (Button) findViewById(R.id.button_add);
                button.setEnabled(formContact.getText().length() > 0 && formMessage.getText().length() > 0);
            }
        };
        formContact.addTextChangedListener(watcherEmptiness);
        formMessage.addTextChangedListener(watcherEmptiness);


    }
    public void scheduleSms(View view) {
        final Calendar timeScheduled = getInstance();
        String year = String.valueOf(mYear);
        String month = String.valueOf(mMonth);
        String day = String.valueOf(mDay);


        timeScheduled.set(Calendar.YEAR, Integer.parseInt(year));
        timeScheduled.set(Calendar.YEAR, Integer.parseInt(month));
        timeScheduled.set(Calendar.YEAR, Integer.parseInt(day));





        if (timeScheduled.getTimeInMillis() < getInstance().getTimeInMillis()) {
            Toast.makeText(getApplicationContext(), getString(R.string.form_validation_datetime), Toast.LENGTH_SHORT).show();
            return;
        }
        sms.setTimestampScheduled(timeScheduled.getTimeInMillis());

        EditText formMessage = (EditText) findViewById(R.id.form_input_message);
        sms.setMessage(formMessage.getText().toString());

        sms.setStatus(SmsModel.STATUS_PENDING);

        DbHelper.getDbHelper(this).save(sms);

        scheduleAlarm(sms);

        goToSmsList();
    }

    public void unscheduleSms(View view) {
        DbHelper.getDbHelper(this).delete(sms.getTimestampCreated());

        unscheduleAlarm(sms);

        goToSmsList();
    }

    private void goToSmsList() {
        Intent goToNextActivity = new Intent(getApplicationContext(), SmsListActivity.class);
        startActivity(goToNextActivity);
    }

    private List<? extends HashMap<String, ?>> getContacts() {
        ArrayList<HashMap<String, String>> contacts = new ArrayList<>();
        HashMap<String, String> names = new HashMap<>();

        // Getting contact names
        String[] projectionPeople = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };
        Cursor people = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                projectionPeople,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );
        if (null != people) {
            int columnIndexId = people.getColumnIndex(ContactsContract.Contacts._ID);
            int columnIndexName = people.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            while (people.moveToNext()) {
                names.put(people.getString(columnIndexId), people.getString(columnIndexName));
            }
            people.close();
        }

        // Getting phones
        String[] projectionPhones = new String[] {
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        Cursor phones = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projectionPhones,
                null,
                null,
                null
        );
        if (null != phones) {
            int columnIndexId = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
            int columnIndexPhone = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while (phones.moveToNext()) {
                String contactId = phones.getString(columnIndexId);
                String phoneNumber = phones.getString(columnIndexPhone);
                HashMap<String, String> NamePhoneType = new HashMap<String, String>();
                NamePhoneType.put("Name", names.get(contactId));
                NamePhoneType.put("Phone", phoneNumber);
                contacts.add(NamePhoneType);
            }
            phones.close();
        }

        return contacts;
    }

    private long getSmsId(Bundle savedInstanceState) {
        String smsId = "0";
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                smsId = extras.getString(DbHelper.COLUMN_TIMESTAMP_CREATED);
            }
        } else {
            smsId = (String) savedInstanceState.getSerializable(DbHelper.COLUMN_TIMESTAMP_CREATED);
        }
        return Long.parseLong(smsId);
    }

    private void scheduleAlarm(SmsModel sms) {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, sms.getTimestampScheduled(), getAlarmPendingIntent(sms));
    }

    private void unscheduleAlarm(SmsModel sms) {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(getAlarmPendingIntent(sms));
    }

    private PendingIntent getAlarmPendingIntent(SmsModel sms) {
        Intent intent = new Intent(AlarmReceiver.INTENT_FILTER);
        intent.putExtra(DbHelper.COLUMN_TIMESTAMP_CREATED, sms.getTimestampCreated());
        return PendingIntent.getBroadcast(
                this,
                sms.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT & Intent.FILL_IN_DATA
        );
    }

    //Checking Permission Granted for Android>=M
    private boolean permissionsGranted() {
        boolean granted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissionsNotGranted = new ArrayList<String>();
            for (int i = 0; i < this.permissionsRequired.length; i++) {
                if (checkSelfPermission(this.permissionsRequired[i]) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNotGranted.add(this.permissionsRequired[i]);
                } else {
                    this.permissionsGranted.add(this.permissionsRequired[i]);
                }
            }
            if (permissionsNotGranted.size() > 0) {
                granted = false;
                String[] notGrantedArray = permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]);
                requestPermissions(notGrantedArray, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        }
        return granted;
    }

    //After granting permisssion
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                List<String> requiredPermissions = Arrays.asList(this.permissionsRequired);
                String permission;
                for (int i = 0; i < permissions.length; i++) {
                    permission = permissions[i];
                    if (requiredPermissions.contains(permission)
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        this.permissionsGranted.add(permission);
                    }
                }
                if (this.permissionsGranted.size() == this.permissionsRequired.length) {
                    buildForm();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}