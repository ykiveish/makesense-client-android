package com.example.yevgeniy.makesensesrv;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MSService extends AppCompatActivity {
    private static final String TAG = "MSService";
    ServiceRequest service = null;
    MSServiceState serviceState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msservice);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LocalRepo.settings = getSharedPreferences(TAG, 0);
        LocalRepo.telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        LocalRepo.settings.edit().remove("UUID").commit();

        if (LocalRepo.GetDeviceUUID()) {
            Log.e(TAG, "[INFO] Device ID = " + LocalRepo.DeviceUUID);
        } else {
            System.exit(0);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        service = new ServiceRequest();
        Thread serviceThread = new Thread(service);
        serviceThread.start();

        serviceState = new MSServiceState(service);
        Thread serviceStateThread = new Thread(serviceState);
        serviceStateThread.start();

        serviceState.SetState(ServiceState.HAS_API_UUID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceState.StopService();
        service.StopService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
