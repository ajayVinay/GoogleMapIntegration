package com.example.uipl.googlemap;

import android.app.Dialog;
import android.content.Intent;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Connection;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = "MainActivity";

 private static final int ERROR_DIALOG_EQUEST=9001;
 private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(isServiceOK()) {
            init();
        }
    }
    private void init() {
        button = (Button)findViewById(R.id.btnMap);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MapActivity.class);
                startActivity(intent);
            }
        });
    }
    private Boolean isServiceOK() {
        Log.d(TAG, "isServiceOK: check google service version  ");
        int available  = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available== ConnectionResult.SUCCESS) {
            // everythig is fine and user can make map request
            Log.d(TAG, "isServiceOK: Google services is working");
            return true;
        }else if (GoogleApiAvailability.getInstance().isUserResolvableError(available))
        {
            //an error occured but we can  handle it
            Log.d(TAG, "isServiceOK: an error occured but we can fix it ");
            Dialog dialog=  GoogleApiAvailability.getInstance()
                    .getErrorDialog(MainActivity.this,available,ERROR_DIALOG_EQUEST);
            dialog.show();
            }else {
            Toast.makeText(this,"we can't make this request",Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
