package fi.oulu.tol.vote50;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

public class DeviceOrientationRecognizer implements
		android.hardware.SensorEventListener {

	private DeviceOrientationHandler handler;

	float gravity[] = new float[3];
	float linear_acceleration[] = new float[3];

	private long timeStamp; 
	private final long INITIAL_RECOGNITION_DELAY = 200 * 1000 * 1000; 
	private final long SUBSEQUENT_RECOGNITION_DELAY = 500 * 1000 * 1000; 
	private final long DELAY_TO_SWITCH_BACK_TO_INITIAL_DELAY = 1000 * 1000 * 1000;
	private long currentDelay = INITIAL_RECOGNITION_DELAY;

	private static final String TAG = "DeviceOrientationRecognizer";

	/************* start to implement ******************/

	public DeviceOrientationRecognizer(DeviceOrientationHandler h) {
		// set voteactivity as device orientation recognizer's oberver
		handler = h;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;

		long tick = event.timestamp - timeStamp;
		if (tick < currentDelay) {
			if (tick >= DELAY_TO_SWITCH_BACK_TO_INITIAL_DELAY) {
				currentDelay = INITIAL_RECOGNITION_DELAY;
			}
			return;
		}
		timeStamp = event.timestamp;
		final float alpha = 0.8f;
		gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
		gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
		gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
		// Log.d(TAG, "gravity: " + gravity[0] + "   " + gravity[1] + "  " +
		// gravity[2]);

		if (gravity[0] > 3) {
			Log.d(TAG, "Tilting left");
			handler.tiltedLeft();
			currentDelay = SUBSEQUENT_RECOGNITION_DELAY;
		} else if (gravity[0] < -3) {
			Log.d(TAG, "Tilting right");
			handler.tiltedRight();
			currentDelay = SUBSEQUENT_RECOGNITION_DELAY;
		} else if (gravity[1] > 3) {
			Log.d(TAG, "Tilting towards");
			handler.tiltedTowards();
			currentDelay = SUBSEQUENT_RECOGNITION_DELAY;
		} else if (gravity[1] < -1.5) {
			Log.d(TAG, "Tilting away");
			handler.tiltedAway();
			currentDelay = SUBSEQUENT_RECOGNITION_DELAY;
		} else {
			// Log.d(TAG, "Not tilting to any direction.");
		}
	}

}
