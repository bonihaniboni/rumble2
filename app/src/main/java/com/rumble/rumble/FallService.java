package com.rumble.rumble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FallService extends Service implements SensorEventListener {
    static final String CHANNEL_ID = "channelId";

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float x=0, y=0, z=0;

    public FallService() {

    }

    // 넘어짐 감지 시
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) return;

        float X = event.values[0];
        float Y = event.values[1];
        float Z = event.values[2];

        float acc_x = Math.abs(x - X);
        float acc_y = Math.abs(y - Y);
        float acc_z = Math.abs(z - Z);

        double angle_x = Math.atan(Math.sqrt(acc_y * acc_y + acc_z * acc_z) / acc_x) * 180 / Math.PI;
        double angle_y = Math.atan(Math.sqrt(acc_x * acc_x + acc_z * acc_z) / acc_y) * 180 / Math.PI;
        double angle_z = Math.atan(Math.sqrt(acc_x * acc_x + acc_y * acc_y) / acc_z) * 180 / Math.PI;

        double acc_svm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);


        int cnt=0;
        if (angle_x > 60) ++cnt;
        if (angle_y > 60) ++cnt;
        if (angle_z > 60) ++cnt;

        if (acc_svm > 2.5 && cnt >= 2) {

            Log.d("test","넘어짐");
            Intent intent = new Intent(getApplicationContext(), FloatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        x = X; y = Y; z = Z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();


        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("알림 타이틀")
                .setContentText("알림 설명")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();


        startForeground(1, notification);

        //선을 나타낼 수 있는 가속도 센서 선택 등록
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(FallService.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);



       /* new Thread(new Runnable() {
            @Override
            public void run() {

                f();
            }
        }).start();
        */
        // 넘어짐 감지 엑티비티

        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel =
                    new NotificationChannel(CHANNEL_ID, "알림 설정 모드 타이틀", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            //assert manager != null;
            manager.createNotificationChannel(serviceChannel);
        }
    }

    /*void f(){

        for(int i=0;i<100;i++){
            try {
                Thread.sleep(1000);
                Log.d("test","count: "+i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 명령 다 실행 시 종류
            stopSelf();
        }


    }
*/
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy(){
        stopForeground(true);
        stopSelf();

        super.onDestroy();
    }
}