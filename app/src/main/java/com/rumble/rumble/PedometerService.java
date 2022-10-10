package com.rumble.rumble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class PedometerService extends Service implements SensorEventListener {

    private int number = 0;
    public static Context mContext;

    static final String CHANNEL_ID = "channelId";

    // 만보기 관련 변수
    private TextView textViewWalk;
    private int countWalk = 0;
    private SensorManager sensorManager;
    private Sensor stepCountSensor;

    NotificationCompat.Builder notification;


    public PedometerService() {
        mContext = this;
        Log.d("PedometerService", "PedometerService init");
    }


    // 센서 변화 감지
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            ++countWalk;
            //Log.d("test",Integer.toString(countWalk));
            Log.d("WalkingChecker", "sensor : " + sensorEvent.toString());
            Log.d("WalkingChecker", "before command : " + countWalk);

            notification.setContentText(Integer.toString(countWalk));
            startForeground(1, notification.build());

            processCommand();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();


        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("걸음 수")
                .setContentText("0 걸음")
              .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                ;



        startForeground(1, notification.build());

        // 센서 등록
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        if (stepCountSensor == null) {
            Toast.makeText(this, "센서 없음", Toast.LENGTH_LONG).show();
        }

        sensorManager.registerListener(this, stepCountSensor, SensorManager.SENSOR_DELAY_FASTEST);


        processCommand(); // 인텐트로 값 전달 시도

        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel =
                    new NotificationChannel(CHANNEL_ID, "알림 설정 모드 타이틀", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            //assert manager != null;
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // 인텐트로 값 전달 시도
    private void processCommand() {
        //Log.d("WalkingChecker", "intent : " + intent.toString());

        Log.d("test","값 전송");
        String name = "ab";
        String show = Integer.toString(countWalk);

        Intent showIntent = new Intent(getApplicationContext(), MainActivity.class);

        showIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);

        showIntent.putExtra("command", show);
        //Log.d("test",showIntent);
        showIntent.putExtra("name", name + " from service.");
        startActivity(showIntent); // Service에서 Activity로 데이터를 전달
    }

    @Override
    public void onDestroy(){
        Log.d("PedometerService", "PedometerService");
        stopForeground(true);
        stopSelf();

        super.onDestroy();
    }
}