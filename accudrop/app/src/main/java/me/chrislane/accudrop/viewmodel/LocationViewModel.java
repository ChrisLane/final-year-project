package me.chrislane.accudrop.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class LocationViewModel extends ViewModel implements LocationListener {
    private GoogleApiClient googleApiClient;
    private MutableLiveData<Location> lastLocation = new MutableLiveData<>();

    public LocationViewModel() {
        Location loc = new Location("");
        loc.setLatitude(51.52);
        loc.setLongitude(0.08);
        lastLocation.setValue(loc);
    }

    /**
     * Send a request for location updates to the GoogleApiClient.
     *
     * @param googleApiClient The GoogleApiClient to request updates from.
     */
    public void startLocationUpdates(GoogleApiClient googleApiClient) {
        this.googleApiClient = googleApiClient;

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    /**
     * Tell the GoogleApiClient to stop giving location updates.
     */
    public void stopLocationUpdates() {
        Log.d("LocMgr", "Stopping location updates");

        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);

    }

    /**
     * Get a LatLng object from a Location object.
     *
     * @param location The location to get latitude and longitude from.
     * @return A LatLng object with latitude and longitude of the given location.
     */
    public LatLng getLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    /**
     * Get the device location for the last location update.
     *
     * @return The last known device location.
     */
    public LiveData<Location> getLastLocation() {
        return lastLocation;
    }

    /**
     * Called to notify the app of a location change.
     *
     * @param location The new location of the device.
     */
    @Override
    public void onLocationChanged(Location location) {
        lastLocation.setValue(location);
    }
}