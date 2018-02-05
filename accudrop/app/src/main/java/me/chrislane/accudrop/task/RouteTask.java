package me.chrislane.accudrop.task;

import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import me.chrislane.accudrop.RouteCalculator;

/**
 * Calculate a route for a given target argument.
 */
public class RouteTask extends AsyncTask<LatLng, Void, List<Location>> {

    private final RouteTaskListener listener;
    private final WindTask.WindTuple windTuple;

    public RouteTask(RouteTaskListener listener, WindTask.WindTuple windTuple) {
        this.listener = listener;
        this.windTuple = windTuple;
    }

    @Override
    protected List<Location> doInBackground(LatLng... Latlngs) {
        LatLng target = Latlngs[0];
        RouteCalculator routeCalculator = new RouteCalculator(windTuple, target);
        return routeCalculator.calcRoute();
    }

    @Override
    protected void onPostExecute(List<Location> route) {
        super.onPostExecute(route);
        listener.onFinished(route);
    }

    public interface RouteTaskListener {
        void onFinished(List<Location> route);
    }
}
