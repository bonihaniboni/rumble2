package com.rumble.rumble;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionSupport {

    private Context context;
    private Activity activity;

    private String[] permissions = {
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private List permissionList;

    private final int MULTIPLE_PERMISSIONS = 1023;

    public PermissionSupport(Activity _activity, Context _context) {
        this.activity = _activity;
        this.context = _context;
    }

    public boolean checkPermission() {
        int result;
        permissionList = new ArrayList<String>();

        for(String pm : permissions) {
            result = ContextCompat.checkSelfPermission(context, pm);
            if(result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
                Log.d("start_test", "" + pm);
            }
        }

        Log.d("start_test", "empty : " + permissionList.isEmpty());

        return permissionList.isEmpty();
    }

    public void requestPermission() {
        Log.d("start_test", "request");
        if(!checkPermission()) {
            ActivityCompat.requestPermissions(activity,
                    (String[]) permissionList.toArray(new String[permissionList.size()]),
                    MULTIPLE_PERMISSIONS);
        }
    }

    public boolean permissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == MULTIPLE_PERMISSIONS && (grantResults.length > 0)) {
            for(int i=0; i<grantResults.length; i++) {
                if(grantResults[i] == -1) {
                    return false;
                }
            }
        }
        return true;
    }
}
