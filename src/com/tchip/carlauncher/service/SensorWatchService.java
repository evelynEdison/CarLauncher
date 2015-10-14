package com.tchip.carlauncher.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.tchip.carlauncher.Constant;
import com.tchip.carlauncher.MyApplication;
import com.tchip.carlauncher.R;
import com.tchip.carlauncher.util.MyLog;
import com.tchip.carlauncher.util.SettingUtil;

/**
 * Created by AlexZhou on 2015/3/26. 11:06
 */
public class SensorWatchService extends Service {
	private final static int AXIS_X = 0;
	private final static int AXIS_Y = 1;
	private final static int AXIS_Z = 2;

	private float valueX = 0f;
	private float valueY = 0f;
	private float valueZ = 0f;

	private float LIMIT_X, LIMIT_Y, LIMIT_Z;

	private int[] crashFlag = { 0, 0, 0 }; // {X-Flag, Y-Flag, Z-Flag}
	private boolean isCrash = false;

	private SensorManager sensorManager;
	private SensorEventListener sensorEventListener;

	@Override
	public void onCreate() {
		super.onCreate();
		SharedPreferences sharedPreferences = getSharedPreferences(
				Constant.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// Using TYPE_ACCELEROMETER first if exit, then TYPE_GRAVITY
		Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
			sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		sensorEventListener = new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent event) {
				if (MyApplication.isSleeping) {
					stopSelf();
					MyLog.v("[SensorWatchService]stopSelf in case of isSleeping = true");
					return;
				}

				if (MyApplication.isCrashOn) {
					LIMIT_X = LIMIT_Y = LIMIT_Z = SettingUtil
							.getGravityVauleBySensitive(MyApplication.crashSensitive);
					valueX = event.values[AXIS_X];
					valueY = event.values[AXIS_Y];
					valueZ = event.values[AXIS_Z];

					if (valueX > LIMIT_X || valueX < -LIMIT_X) {
						crashFlag[0] = 1;
						isCrash = true;
					}
					// if (valueY > LIMIT_Y || valueY < -LIMIT_Y) {
					// crashFlag[1] = 1;
					// isCrash = true;
					// }
					if (valueZ > LIMIT_Z || valueZ < -LIMIT_Z) {
						crashFlag[2] = 1;
						isCrash = true;
					}
					if (isCrash) {
						// 当前录制视频加锁
						if (MyApplication.isVideoReording
								&& !MyApplication.isVideoLock) {
							MyApplication.isVideoLock = true;
							MyApplication.isCrashed = true;
							startSpeak(getResources().getString(
									R.string.lock_video_in_case_crash));
							MyLog.v("[SensorWarchService] Crashed -> isVideoLock = true;X:"
									+ valueX + ",Y:" + valueY + ",Z:" + valueZ);
						}
						// 重置碰撞标志位
						isCrash = false;
					}
				}
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		// 0:000000μs-SENSOR_DELAY_FASTEST
		// 1:020000μs-SENSOR_DELAY_GAME
		// 2:060000μs-SENSOR_DELAY_UI
		// 3:200000μs-SENSOR_DELAY_NORMAL
		// 尽量使用比较低的传感器采样率，这样可以让系统的负荷比较小，同时可以省电
		sensorManager.registerListener(sensorEventListener, sensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		sensorManager.unregisterListener(sensorEventListener);
		super.onDestroy();
	}

	private void startSpeak(String content) {
		Intent intent = new Intent(getApplicationContext(), SpeakService.class);
		intent.putExtra("content", content);
		startService(intent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public String getTime() {
		long nowTime = System.currentTimeMillis();
		Date date = new Date(nowTime);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return sdf.format(date);
	}

	public float getValue(int axis) {
		switch (axis) {
		case AXIS_X:
			return valueX;
		case AXIS_Y:
			return valueY;
		case AXIS_Z:
			return valueZ;
		default:
			return 0f;
		}
	}
}
