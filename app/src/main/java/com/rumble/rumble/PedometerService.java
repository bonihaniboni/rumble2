package com.rumble.rumble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
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
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class PedometerService extends Service implements SensorEventListener {

    private int number = 0;
    public static Context mContext;

    static final String CHANNEL_ID = "channelId";

    // 만보기 관련 변수
    private TextView textViewWalk;
    public int countWalk = 0;
    private SensorManager sensorManager;
    private Sensor stepCountSensor;

    NotificationCompat.Builder notification;

    private BroadcastReceiver mReciver;


    public PedometerService() {
        mContext = this;
        Log.d("PedometerService", "" + mContext);
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
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void setReceiver() {
        //mReciver = new PedometerReceiver();
        mReciver = new InnerCReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        //filter.addAction(Intent.ACTION_POWER_CONNECTED);
        registerReceiver(mReciver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext = this;
        setReceiver();

        createNotificationChannel();


        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("걸음 수")
                .setContentText(countWalk + "걸음")
              .setSmallIcon(R.mipmap.ic_main_round)
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

    @Override
    public void onDestroy(){
        Log.d("FallService", "PedometerService destroy");
        //unregisterReceiver(mReciver);
        stopForeground(true);
        stopSelf();
        unregisterReceiver(mReciver);

        super.onDestroy();
    }

    private void sendAlarmOnCountWalk() {
        String content = "오늘 하루 건강하게 마무리하셨습니다!";
        if (countWalk < 4000) {
            content = "내일은 더 많이 걸으셔야해요!";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_main_round)
                .setContentTitle("오늘 총 걸음 수는 " + countWalk + "걸음")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(3, builder.build());
    }

    public class InnerCReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_DATE_CHANGED.equals(intent.getAction())) {
                sendAlarmOnCountWalk();

                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        countWalk = 0;
                        notification.setContentText(Integer.toString(countWalk));
                        startForeground(1, notification.build());
                    }
                }, 2000);
            }

            if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
                //sendAlarmOnCountWalk();
            }
        }
    }
}