/**
 * Copyright (C) 2016 HTC Inc. 
 * All rights reserved.
 * SensorActivity.java
 *
 * Created on: 2017-04-01
 *      Author:lewis_lu
 */
package com.example.lewis.mysensors;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.os.PowerManager.*;


public class SensorActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mStepCounter;
    private Sensor mStepDetector;
    private Sensor mAccelerometer;
    private static final String TAG = "MY_SENSOR";
    private static String my_sensor_path;
    private static File my_sensor_file;
    private static FileOutputStream my_sensor_fops;
    private static final boolean step_counter_enable = true;
    private static final boolean step_detector_enable = true;
    private static final boolean accelerometer_enable = true;
    private static float last_step_counter_timestamp = 0;
    private static float last_step_detector_timestamp = 0;
    private static float last_step_counter_value = 0;
    private static long step_detector_total = 0;
    private static PowerManager.WakeLock wakelock;
    private static int totalnumber;


    private static  ArrayList<String> myStringArray = new ArrayList<String>();

    public SensorActivity(){
        Log.i(TAG, "SensorActivity start");

        Log.i(TAG, "SensorActivity end");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakelock = pm.newWakeLock(PARTIAL_WAKE_LOCK, "MySensor:WakeLock");

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if(step_counter_enable)
        {
            mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            mSensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_FASTEST, 0);
        }

        if(step_detector_enable)
        {
            mStepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            mSensorManager.registerListener(this, mStepDetector, SensorManager.SENSOR_DELAY_FASTEST, 0);
        }

        if(accelerometer_enable)
        {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST, 0);
        }

        setContentView(R.layout.activity_sensor);

        try {
            File dir_path = Environment.getExternalStorageDirectory();
            String absolutePath;
            absolutePath = getExternalFilesDir(null).getAbsolutePath();
            Log.d(TAG, "dir_path : " + dir_path.getAbsolutePath());
            Log.d(TAG, "absolutePath: " + absolutePath);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            my_sensor_path = "mysensor_" + sdf.format(new Date()) + ".txt";

            Log.i(TAG, "my_sensor_file: " + my_sensor_path);
            my_sensor_file = new File(absolutePath, my_sensor_path);

            if(!my_sensor_file.exists()) {
                try {
                    my_sensor_file.createNewFile();
                }catch (Exception e){

                }
            }

            Log.i(TAG, "my_sensor_file: " + my_sensor_path);
            my_sensor_fops = new FileOutputStream(my_sensor_file, true);

        } catch(IOException ex) {
            System.out.println(ex.getStackTrace());
            String s = String.valueOf(ex.getStackTrace());
            Log.i(TAG, s);
        }

        Log.i(TAG, "SensorActivity onCreate");
    }

    protected void onStart(){
        Log.i(TAG, "SensorActivity onStart");
        wakelock.acquire();

        super.onStart();
    }

    protected void onResume(){
        Log.i(TAG, "SensorActivity onResume start");
        super.onResume();
        Log.i(TAG, "SensorActivity onResume end");
    }

    protected void onPause(){
        Log.i(TAG, "SensorActivity onPause");
        super.onPause();
    }

    protected void onStop(){

        Log.i(TAG, "SensorActivity onStop");
        super.onStop();
        wakelock.release();


    }

    protected void onDestroy(){
        Log.i(TAG, "SensorActivity onDestroy");

        try {
            my_sensor_fops.close();
        }catch (IOException ex) {
            System.out.println(ex.getStackTrace());
        }

        if(step_counter_enable)
            mSensorManager.unregisterListener(this, mStepCounter);
        if(step_detector_enable)
            mSensorManager.unregisterListener(this, mStepDetector);

        if(accelerometer_enable)
            mSensorManager.unregisterListener(this, mAccelerometer);



        super.onDestroy();

    }


    private static int flag = 0;
    private  static final int REQUEST_IMAGE_CAPTURE = 1;
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        String OutputString = "";
        float interval_step = 0;
        boolean show_list = true;



        SimpleDateFormat cur_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        OutputString += cur_time.format(new Date()) + " ";
        OutputString += event.sensor.getName();
        for(int i = 0; i < event.values.length; i ++) {
            OutputString += ", " + event.values[i];
        }

        if(event.sensor.getName().compareTo("BMI160 Accelerometer") == 0)
        {
            totalnumber ++;
            TextView tv = (TextView) findViewById(R.id.total_number);
            tv.setText(String.valueOf(totalnumber));
        }
        if(event.sensor.getName().compareTo("BMI160 Step Detector") == 0)
        {
            float time = (float) (event.timestamp / 100000000) / 10;
            float interval_time = 0;

            interval_time = time - last_step_detector_timestamp;
            step_detector_total += event.values[0];
            OutputString += ", " + step_detector_total + ", " + time + ", " + interval_time;
            TextView textView = (TextView) findViewById(R.id.step_detector);
            textView.setText(OutputString);

            last_step_detector_timestamp = time;
            show_list = true;

        }
        else if(event.sensor.getName().compareTo("BMI160 Step Counter") == 0)
        {
            float time = 0;
            float interval_time = 0;

            Log.i(TAG, "onSensorChanged");

            time = (float)(event.timestamp / 100000000) / 10;
            interval_time = time - last_step_counter_timestamp;
            interval_step = event.values[0] - last_step_counter_value;
            OutputString += ", " + interval_step + ", " + event.timestamp + ", " + interval_time;

            TextView textView = (TextView)findViewById(R.id.step_counter);
            textView.setText(OutputString);

            last_step_counter_timestamp = time;
            last_step_counter_value = event.values[0];
            show_list = true;
        }


        if(show_list == true)
        {
            myStringArray.add(OutputString);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myStringArray);

            ListView listView = (ListView) findViewById(R.id.listview);
            listView.setAdapter(adapter);

            //save log to /data/mysensor*.log
            //Log.i(TAG, OutputString);
            try {
                StringBuffer sb = new StringBuffer();
                sb.append(OutputString + "\n");
                if(my_sensor_fops != null)
                    my_sensor_fops.write(sb.toString().getBytes("utf-8"));
                else
                {
                    Log.d(TAG, "onSensorChanged: my_sensor_fops is null");
                }
            }catch (IOException ex) {
                System.out.println(ex.getStackTrace());
            }
        }



        if(event.values[0] >= 10 && flag == 0){
            //dispatchTakePictureIntent();
            flag = 1;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public void openCamera(View view){
        Log.i(TAG, "SensorActivity openCamera");
        dispatchTakePictureIntent();
    }

    public void sensorFlush(View view){
        Log.i(TAG, "sensorFlush start");
        mSensorManager.flush(this);
        Log.i(TAG, "sensorFlush end");
    }
}