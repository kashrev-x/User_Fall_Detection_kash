package com.example.FD.User_Fall_Detection_kash;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;




public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private DecimalFormat df = new DecimalFormat("#.###");

    private Sensor sensor;
    private SensorManager sensorManager;
    private SharedPreferences sharedPreferences;

    private TextView acceleration_threshold_textView;
    private TextView net_acceleration_change_textView;
    private TextView month,day,year;

    private String threshold_key_value;
    private String phoneNumber_key_value;
    private String notification_key_value;
    private String phoneNumber ;
    
    private int accelerationThrehsold = 500;

    private double changeAccValue = 0;
    private double[] last_acceleration = new double[3];
    private double[] updated_acceleration = new double[3];

    private boolean skipFirstchange = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        month = findViewById(R.id.Month);
        day = findViewById(R.id.Day);
        year = findViewById(R.id.Year);

        threshold_key_value = getString(R.string.threshold_value);
        phoneNumber_key_value = getString(R.string.companion_phonenumber);
        notification_key_value = getString(R.string.notification_method);
        initialUI();
        initialSensor();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        accelerationThrehsold = Integer.parseInt(sharedPreferences.getString(threshold_key_value, "500"));
        phoneNumber = sharedPreferences.getString(phoneNumber_key_value, "");


        Date currentTime = Calendar.getInstance().getTime();
        String formattedDate = DateFormat.getDateInstance(DateFormat.FULL).format(currentTime);
        String[] splitDate = formattedDate.split(",");
        day.setText(splitDate[0]);
        month.setText(splitDate[1]);
        year.setText(splitDate[2]);

        if (phoneNumber.equals(""))
        {
            showSetPhoneNumberWarning();
        }

    }

    public void showSetPhoneNumberWarning() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getString(R.string.set_phone_name_warning)).setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { startActivity(new Intent(((Dialog) dialog).getContext(), SettingsActivity.class));dialog.cancel(); }
        }).show();
    }

    public void initialUI() {
        net_acceleration_change_textView = (TextView)findViewById(R.id.net_acceleration_change);
        acceleration_threshold_textView = (TextView)findViewById(R.id.acceleration_threshold);
    }

    public void initialSensor() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        last_acceleration[0] = updated_acceleration[0];
        last_acceleration[1] = updated_acceleration[1];
        last_acceleration[2] = updated_acceleration[2];

        updated_acceleration[0] = event.values[0];
        updated_acceleration[1] = event.values[1];
        updated_acceleration[2] = event.values[2];

        changeAccValue = Math.pow((updated_acceleration[0]-last_acceleration[0]), 2) +
                Math.pow((updated_acceleration[1]-last_acceleration[1]), 2) +
                Math.pow((updated_acceleration[2]-last_acceleration[2]), 2);

        updateSensorView();

        final String notificationMethod;
        accelerationThrehsold = Integer.parseInt(sharedPreferences.getString(threshold_key_value, "500"));
        if (!skipFirstchange && changeAccValue >= accelerationThrehsold) {
            phoneNumber = sharedPreferences.getString(phoneNumber_key_value, "");
            notificationMethod = sharedPreferences.getString(notification_key_value, "SMS");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (notificationMethod.equals("phone"))
                    {
                        makePhoneCall(phoneNumber);
                    }

                    else if (notificationMethod.equals("SMS"))
                    {
                        sendSMS(phoneNumber);
                    }
                }
            }, 10000);

        }
        skipFirstchange = false;
    }

    @Override
    protected void onResume() {
        sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    private void updateSensorView() {
        net_acceleration_change_textView.setText("Net acceleration change = "+df.format(changeAccValue));
        acceleration_threshold_textView.setText("Acceleration threshold value = "+accelerationThrehsold);
    }


    public void makePhoneCall(String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phoneNumber)).putExtra("sms_body", "default content"));
    }

    public void sendSMS(String phoneNumber) {
        String message ="URGENT! The user has fallen down please respond at once";
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "The companion has been alerted", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
