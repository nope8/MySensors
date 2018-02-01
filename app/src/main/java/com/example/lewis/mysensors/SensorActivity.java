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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SensorActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mStepCounter;
    private Sensor mStepDetector;
    private Sensor mAccelerometer;
    private static final String TAG = "MY_SENSOR";
    private static String my_sensor_path;
    private static File my_sensor_file;
    private static FileOutputStream my_sensor_fops;
    private static final boolean step_counter_enable = false;
    private static final boolean step_detector_enable = false;
    private static final boolean accelerometer_enable = true;
    private static float last_step_counter_timestamp = 0;
    private static float last_step_detector_timestamp = 0;
    private static float last_step_counter_value = 0;
    private static long step_detector_total = 0;
    private PowerManager.WakeLock wakelock;
    private String mCurrentPhotoPath;
    private ImageView mImageView;
    private VideoView mVedioView;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;
    private static  ArrayList<String> myStringArray = new ArrayList<String>();

    public SensorActivity(){
        Log.i(TAG, "SensorActivity start");

        Log.i(TAG, "SensorActivity end");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        Log.i(TAG, "SensorActivity create start");
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MySensor_WakeLock");
        mImageView = (ImageView)findViewById(R.id.imageView);
        mVedioView = (VideoView)findViewById(R.id.videoView);
        wakelock.acquire();

        if(mImageView == null){
            Log.i(TAG, "onCreate mImageView is null");
        }

        if(mVedioView == null){
            Log.i(TAG, "onCreate mVedioView is null");
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if(step_counter_enable)
        {
            mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if(mStepCounter == null)
            {
                Log.e(TAG, "Failed to get TYPE_STEP_COUNTER sensor");
            }

            boolean ret = mSensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_FASTEST, 0);
            if(ret == false)
            {
                Log.e(TAG, "Failed to register TYPE_STEP_COUNTER sensor");
            }
        }
        Log.i(TAG, "SensorActivity create start2");

        if(step_detector_enable)
        {
            mStepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            mSensorManager.registerListener(this, mStepDetector, SensorManager.SENSOR_DELAY_FASTEST, 0);
        }

        if(accelerometer_enable)
        {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            //mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL, 0);
            mSensorManager.registerListener(this, mAccelerometer, 20000, 0);

        }


        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            my_sensor_path = "mysensor_" + sdf.format(new Date()) + ".txt";

            Log.i(TAG, "my_sensor_file: " + my_sensor_path);


            //Log.i(TAG, "APP PATH:" + getApplicationContext().getFilesDir());      //APP data save path:/data/user/0/com.example.lewis.mysensors/files
            my_sensor_file = new File(getApplicationContext().getFilesDir(), my_sensor_path);

            if(!my_sensor_file.exists()) {
                my_sensor_file.createNewFile();
            }

            Log.i(TAG, "my_sensor_file: " + my_sensor_path);
            my_sensor_fops = new FileOutputStream(my_sensor_file, true);

        } catch(IOException ex) {
            System.out.println(ex.getStackTrace());
            String s = String.valueOf(ex.getStackTrace());
            Log.i(TAG, s);
        }
        wakelock.release();
        Log.i(TAG, "SensorActivity onCreate");
    }

    protected void onStart(){
        Log.i(TAG, "SensorActivity onStart");
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



    @Override
    public void onSensorChanged(SensorEvent event) {
        String OutputString = "";
        float interval_step = 0;
        boolean show_list = false;



        SimpleDateFormat cur_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        OutputString += cur_time.format(new Date()) + " ";
        OutputString += event.sensor.getName();
        for(int i = 0; i < event.values.length; i ++) {
            OutputString += ", " + event.values[i];
        }
        OutputString += ", " + event.timestamp;


        //Log.i(TAG, event.sensor.getName() + " : onSensorChanged");

        if(event.sensor.getName().contains("Step Detector") == true)
        //if(event.sensor.getName().compareTo("BMI160 Step Detector") == 0)
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
        //else if(event.sensor.getName().compareTo("BMI160 Step Counter") == 0)
        else if(event.sensor.getName().contains("Step Counter") == true)
        {
            float time = (float)(event.timestamp / 100000000) / 10;
            float interval_time = 0;

            interval_time = time - last_step_counter_timestamp;
            interval_step = event.values[0] - last_step_counter_value;
            OutputString += ", " + interval_step + ", " + time + ", " + interval_time;

            TextView textView = (TextView)findViewById(R.id.step_counter);
            textView.setText(OutputString);

            last_step_counter_timestamp = time;
            last_step_counter_value = event.values[0];
            show_list = true;
        }
        Log.i(TAG, OutputString);

        if(show_list == true)
        {
            myStringArray.add(OutputString);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myStringArray);

            ListView listView = (ListView) findViewById(R.id.listview);
            listView.setAdapter(adapter);

            //save log to /data/mysensor*.log
            Log.i(TAG, OutputString);
            try {
                StringBuffer sb = new StringBuffer();
                sb.append(OutputString + "\n");
                my_sensor_fops.write(sb.toString().getBytes("utf-8"));
            }catch (IOException ex) {
                System.out.println(ex.getStackTrace());
            }
        }



        if(event.values[0] >= 10 && flag == 0){
            //dispatchTakePictureIntent();
            flag = 1;
        }

    }

    private String sensorModeToStr(int mode)
    {
        String buf = "";

        Log.i(TAG, "reporting mode: " + mode);

        switch(mode){
            case 0:
                buf += "continuous";
                break;
            case 1:
                buf += "on-change";
                break;
            case 2:
                buf += "one-shot";
                break;
            case 3:
                buf += "special";
                break;
            default:
                buf += "wrong sensor mode";
                break;
        }
        return buf;
    }

    private void showSensorList(){
        List<Sensor> list;
        list = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor sensor : list) {
            Log.i(TAG, sensor.getName() + "\nType: " + sensor.getType() + "(" + sensor.getStringType() + ")" + "\n"
                    + "Vendor: " + sensor.getVendor() + "\n" + "Version: " + sensor.getVersion() + "\n"
                    + "Resolution: " + sensor.getResolution() + "\n" + "Min Delay: " + sensor.getMinDelay() + " us" + "\n"
                    + "Max Delay: " + sensor.getMaxDelay() + " us" + "\n" + "Max Range: " + sensor.getMaximumRange() +"\n"
                    + "isWakeUp: " + sensor.isWakeUpSensor() + "\n" + "Report Mode: " + sensorModeToStr(sensor.getReportingMode()) + "\n"
            );
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public void sensorList(View view){
        Log.i(TAG, "SensorActivity sensorList");
        showSensorList();


    }
    //camera related code
    public void openCamera(View view){
        Log.i(TAG, "SensorActivity openCamera");
        dispatchTakePictureIntent();
    }
    private static int flag = 0;
/*

    private  static final int REQUEST_IMAGE_CAPTURE = 1;
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView mImageView = (ImageView)findViewById(R.id.imageView);
            mImageView.setImageBitmap(imageBitmap);

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView mImageView = (ImageView)findViewById(R.id.imageView);
            mImageView.setImageBitmap(imageBitmap);

        }
    }
*/

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "error: failed to create image file");
            }
            Log.i(TAG, "SensorActivity dispatchTakePictureIntent");
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.lewis.mysensors.file", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
            Log.i(TAG, "SensorActivity dispatchTakePictureIntent1");
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent){

        switch(requestCode){
            case REQUEST_TAKE_PHOTO:
                if(resultCode == RESULT_OK){
                    //Bundle extras = data.getExtras();
                    //Bitmap imageBitmap = (Bitmap) extras.get("data");
                    //ImageView mImageView = (ImageView)findViewById(R.id.imageView);
                    //mImageView.setImageBitmap(imageBitmap);
                    galleryAddPic();
                    setPic();

                }
                break;
            case REQUEST_VIDEO_CAPTURE:
                if(resultCode == RESULT_OK){
                    Log.i(TAG, "onActivityResult REQUEST_VIDEO_CAPTURE");
                    //mVedioView = (VideoView)findViewById(R.id.videoView);
                    Uri videoUri = intent.getData();
                    if(mVedioView == null){
                        Log.i(TAG, "onActivityResult mVedioView is null");
                        break;
                    }

                    if(videoUri != null && mVedioView != null){
                        Log.i(TAG, "onActivityResult Play Video");
                        mVedioView.setVideoURI(videoUri);
                        mVedioView.setMediaController(new MediaController(this));

                        mVedioView.start();
                        mVedioView.setVisibility(View.VISIBLE);
                    }else {
                        Log.i(TAG, "onActivityResult videoUri is null");
                    }

                    Log.i(TAG, "onActivityResult REQUEST_VIDEO_CAPTURE1");

                    mVedioView.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.INVISIBLE);
                }
                break;
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        //ImageView mImageView = (ImageView)findViewById(R.id.imageView);
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        mVedioView.setVisibility(View.INVISIBLE);
        mImageView.setVisibility(View.VISIBLE);

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.i(TAG, "PhotoPath:" + mCurrentPhotoPath);
        return image;
    }

    //vedio relate code

    private void dispatchTakeVideoIntent() {
        Log.i(TAG, "dispatchTakeVideoIntent start");
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Log.i(TAG, "dispatchTakeVideoIntent start1");
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            Log.i(TAG, "dispatchTakeVideoIntent start2");
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            Log.i(TAG, "dispatchTakeVideoIntent start3");
        }
    }


    public void openVideo(View view){
        Log.i(TAG, "openVideo start");
        dispatchTakeVideoIntent();
    }

    public void sensorFlush(View view){
        Log.i(TAG, "sensorFlush start");
        mSensorManager.flush(this);
        Log.i(TAG, "sensorFlush end");
    }
}