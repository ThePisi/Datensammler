package com.example.pisi.datensammler;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button btnStart;
    Switch switchfast;
    TextView tvAcc, tvGyro, tvGPS;
    SensorManager sensorMan;
    boolean started = false;
    String content;
    String filename = "SavedData.txt", sAcc, sGyro, sGPS;
    File fsaved;
    FileOutputStream fos;
    LocationManager locationMan;
    SensorEventListener sensorEventListener;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fsaved = new File(getFilesDir(), filename);

        switchfast = findViewById(R.id.switchfast);
        btnStart = findViewById(R.id.btnStart);
        tvAcc = findViewById(R.id.tvAcc);
        tvGyro = findViewById(R.id.tvGyro);
        tvGPS = findViewById(R.id.tvGPS);

        //Check for permission for GPS
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Berechtigung erteilt!", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        btnStart.setText("START");

        sensorMan = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                        sAcc = ("Beschleunigung\nX: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                        tvAcc.setText(sAcc);
                        break;
                    case Sensor.TYPE_GYROSCOPE:
                        sGyro = ("Gyroscope\nX: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                        tvGyro.setText(sGyro);
                        break;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        locationMan = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                sGPS = ("GPS:\nLongitude: " + location.getLongitude() + "\nLatitude: " + location.getLatitude());
                tvGPS.setText(sGPS);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!started) {
                    btnStart.setText("STOP");
                    started = true;
                    if(switchfast.isChecked()) {
                        sensorMan.registerListener(sensorEventListener, sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
                        sensorMan.registerListener(sensorEventListener, sensorMan.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
                    }else{
                        sensorMan.registerListener(sensorEventListener, sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
                        sensorMan.registerListener(sensorEventListener, sensorMan.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
                    }
                    tvGPS.setText("Warte auf GPS Signal...");

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        tvGPS.setText("Keine Berechtigung!");
                        return;
                    }
                    locationMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }else{
                    content = sAcc+"\n\n"+sGyro+"\n\n"+sGPS;
                    btnStart.setText("START");
                    try {
                        fos = openFileOutput(filename, Context.MODE_PRIVATE);
                        fos.write(content.getBytes());
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    started = false;
                    sensorMan.unregisterListener(sensorEventListener);
                    locationMan.removeUpdates(locationListener);
                }
            }
        });
    }
}
