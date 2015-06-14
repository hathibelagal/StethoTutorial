package com.hathy.stethotutorial;

import android.app.Activity;
import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;

public class MainActivity extends Activity{

    private Handler handler;

    private TextView ipDisplay;
    private String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        ipDisplay = (TextView)findViewById(R.id.my_ip);
    }

    public void fetchIP(View v){

        handler = new Handler();

        // Networking operations should be run in a new thread
        new Thread(){
            public void run(){
                OkHttpClient httpClient = new OkHttpClient();

                // Add Stetho interceptor
                httpClient.networkInterceptors().add(new StethoInterceptor());

                try {
                    Response response = httpClient.newCall(
                            new Request.Builder().url("http://httpbin.org/ip").build()
                    ).execute();

                    final String data = response.body().string();

                    // UI Changes should be made in the UI thread
                    // Therefore, you should use handler.post
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            showIP(data);
                        }
                    });
                }catch(IOException ioe) {
                    Log.d("StethoTut", ioe.getMessage());
                }
            }
        }.start();
    }

    private void showIP(String data){
        try {
            JSONObject jData = new JSONObject(data);
            ip = jData.getString("origin");
            ipDisplay.setText(ip);
            saveIPInDB();
            saveIPInPreferences();
        }catch (Exception e){
            Log.d("StethoTut", e.getMessage());
        }
    }

    private void saveIPInDB(){

        // Create/open database
        SQLiteDatabase db = openOrCreateDatabase(
                "ip.db",
                MODE_PRIVATE,
                null
        );

        // Create table if it doesn't exist
        db.execSQL("create table if not exists ip_addresses(ip text, date text)");

        // Insert values into the table
        ContentValues values = new ContentValues();
        values.put("ip", ip);
        values.put("date", new Date().toString());
        db.insert("ip_addresses", null, values);

        // Close the database
        db.close();
    }

    // Save last ip and the date it was recorded
    private void saveIPInPreferences(){
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        prefs.edit().putString("ip", ip).commit();
        prefs.edit().putString("date", new Date().toString()).commit();
    }

}
