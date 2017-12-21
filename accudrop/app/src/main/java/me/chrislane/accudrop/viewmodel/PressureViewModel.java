package me.chrislane.accudrop.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;

import me.chrislane.accudrop.listener.PressureListener;

public class PressureViewModel extends AndroidViewModel {
    private static final String TAG = PressureViewModel.class.getSimpleName();
    private static final int ONE_SECOND_DELAY = 1000000;
    private final SensorManager sensorManager;
    private final PressureListener pressureListener;
    private final MutableLiveData<Float> lastPressure = new MutableLiveData<>();
    private final MutableLiveData<Float> groundPressure = new MutableLiveData<>();
    private final MutableLiveData<Float> lastAltitude = new MutableLiveData<>();
    private Sensor barometer = null;

    public PressureViewModel(@NonNull Application application) {
        super(application);

        pressureListener = new PressureListener(this);
        sensorManager = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            barometer = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        }
    }

    public void startListening() {
        Log.d(TAG, "Listening on pressure.");
        sensorManager.registerListener(pressureListener, barometer, ONE_SECOND_DELAY);
    }

    public void stopListening() {
        Log.d(TAG, "Stopped listening on pressure.");
        sensorManager.unregisterListener(pressureListener);
    }

    public LiveData<Float> getGroundPressure() {
        return groundPressure;
    }

    private void setGroundPressure(float groundPressure) {
        this.groundPressure.setValue(groundPressure);
    }

    public LiveData<Float> getLastPressure() {
        return lastPressure;
    }

    public void setLastPressure(float lastPressure) {
        this.lastPressure.setValue(lastPressure);
        updateAltitude();
    }

    public LiveData<Float> getLastAltitude() {
        return lastAltitude;
    }

    private void setLastAltitude(float lastAltitude) {
        this.lastAltitude.setValue(lastAltitude);
    }

    public void setGroundPressure() {
        if (lastPressure.getValue() != null) {
            setGroundPressure(lastPressure.getValue());
        }
    }

    private void updateAltitude() {
        Float ground = groundPressure.getValue();
        Float last = lastPressure.getValue();
        float altitude;

        if (ground == null && last != null) {
            altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, last);
        } else if (ground != null && last != null) {
            altitude = SensorManager.getAltitude(ground, last);
        } else {
            return;
        }
        setLastAltitude(altitude);
    }
}