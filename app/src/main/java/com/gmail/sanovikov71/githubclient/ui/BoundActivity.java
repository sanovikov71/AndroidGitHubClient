package com.gmail.sanovikov71.githubclient.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.gmail.sanovikov71.githubclient.data.DataService;

public class BoundActivity extends AppCompatActivity{

    protected DataService mDataService;

    protected ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DataService.LocalBinder binder = (DataService.LocalBinder) service;
            mDataService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDataService = null;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, DataService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mDataService) {
            unbindService(mConnection);
        }
    }

}
