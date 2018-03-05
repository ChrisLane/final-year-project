package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.chrislane.accudrop.MainActivity;
import me.chrislane.accudrop.fragment.RadarFragment;
import me.chrislane.accudrop.task.FetchUsersAndPositionsTask;
import me.chrislane.accudrop.viewmodel.DatabaseViewModel;
import me.chrislane.accudrop.viewmodel.RadarViewModel;

public class RadarPresenter {

    private static final String TAG = RadarPresenter.class.getSimpleName();
    private final RadarFragment fragment;
    private final RadarViewModel radarViewModel;
    private DatabaseViewModel databaseViewModel = null;
    // TODO #48: Move data below to the view model
    private int maxHDistance = 500; // In metres
    private int maxVDistance = 50; // In metres
    private List<Location> subjectLocs;
    private List<Pair<Float, Float>> positions = new ArrayList<>();
    private List<Double> heightDiffs = new ArrayList<>();
    private List<Location> guestLocations;
    private List<Pair<UUID, List<Location>>> guestEntries;

    public RadarPresenter(RadarFragment fragment) {
        this.fragment = fragment;

        radarViewModel = ViewModelProviders.of(fragment).get(RadarViewModel.class);
        MainActivity main = (MainActivity) fragment.getActivity();
        if (main != null) {
            databaseViewModel = ViewModelProviders.of(main).get(DatabaseViewModel.class);
        }

        // Set the current user as the subject
        SharedPreferences settings = databaseViewModel.getApplication()
                .getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String stringUuid = settings.getString("userUUID", "");
        UUID uuid = UUID.fromString(stringUuid);
        radarViewModel.setSubject(uuid);

        // Get users and positions of the last jump
        generateLastJumpPositions();
    }

    public List<Pair<UUID, List<Location>>> extractSubject(List<Pair<UUID, List<Location>>> userEntries) {
        for (int i = 0; i < userEntries.size(); i++) {
            Pair<UUID, List<Location>> userEntry = userEntries.get(i);
            if (userEntry.first != null &&
                    userEntry.first.equals(radarViewModel.getSubject().getValue())) {
                subjectLocs = userEntry.second;
                userEntries.remove(i);
            }
        }

        // None of the users were the subject.
        if (subjectLocs == null) {
            Log.e(TAG, "Subject does not exist in jump data.");
            return null;
        }

        return userEntries;
    }

    public void generateLastJumpPositions() {
        FetchUsersAndPositionsTask.Listener listener = userEntries -> {
            if (userEntries == null) {
                return;
            }

            guestEntries = extractSubject(userEntries);

            if (subjectLocs != null) {
                long startTime = subjectLocs.get(0).getTime();
                guestLocations = getGuestLocations(guestEntries, startTime);
                updateGuestRelatives(guestLocations, startTime);
            }
        };
        new FetchUsersAndPositionsTask(listener, databaseViewModel).execute();
    }

    private List<Location> getGuestLocations(List<Pair<UUID, List<Location>>> guestLocs, long time) {
        List<Location> result = new ArrayList<>();
        for (Pair<UUID, List<Location>> userEntry : guestLocs) {
            List<Location> locations = userEntry.second;
            if (locations != null && locations.size() > 0) {
                Location nearest = getLocationByTime(locations, time);

                result.add(nearest);
            }
        }
        return result;
    }

    public void updateTime(long time) {
        guestLocations = getGuestLocations(guestEntries, time);
        updateGuestRelatives(guestLocations, time);
    }

    private Location getLocationByTime(List<Location> locations, long time) {
        // Time is less than the first time in the sorted list
        if (time < locations.get(0).getTime()) {
            return locations.get(0);
        }
        // Time is greater than the last element in the sorted list
        if (time > locations.get(locations.size() - 1).getTime()) {
            return locations.get(locations.size() - 1);
        }

        int low = 0;
        int high = locations.size() - 1;

        while (low <= high) {
            int mid = (high + low) / 2;

            if (time < locations.get(mid).getTime()) {
                high = mid - 1;
            } else if (time > locations.get(mid).getTime()) {
                low = mid + 1;
            } else {
                return locations.get(mid);
            }
        }

        return (locations.get(low).getTime() - time) < (time - locations.get(high).getTime())
                ? locations.get(low) : locations.get(high);
    }

    public List<Location> getSubjectLocations() {
        return subjectLocs;
    }

    public void updateGuestRelatives(List<Location> locations, long time) {
        if (subjectLocs.isEmpty() || locations.isEmpty()) {
            return;
        }

        Location subjectLoc = null;
        for (Location loc : subjectLocs) {
            if (loc.getTime() == time) {
                subjectLoc = loc;
                break;
            }
        }

        // Check we got a value for the subject location
        if (subjectLoc == null) {
            Log.e(TAG, "No subject location matching timestamp");
            return;
        }

        positions.clear();
        heightDiffs.clear();

        // Loop over user position arrays
        for (Location guest : locations) {
            float hDistanceTo = subjectLoc.distanceTo(guest);
            double vDistanceTo = guest.getAltitude() - subjectLoc.getAltitude();
            Log.v(TAG, "Horizontal Distance: " + hDistanceTo);
            Log.v(TAG, "Vertical Distance: " + vDistanceTo);

            // Check if distance from subject further than maxHDistance
            if (maxHDistance >= hDistanceTo && maxVDistance >= vDistanceTo) {
                // Add to list of positions to draw
                float bearingTo = subjectLoc.bearingTo(guest);
                Log.v(TAG, "Bearing: " + bearingTo);
                positions.add(new Pair<>(bearingTo, hDistanceTo));
                heightDiffs.add(vDistanceTo);
            }
        }

        fragment.updateRadarPoints();
    }

    public List<Pair<Float, Float>> getPositions() {
        return positions;
    }

    public List<Double> getHeightDiffs() {
        return heightDiffs;
    }

    public int getMaxVDistance() {
        return maxVDistance;
    }

    public int getMaxHDistance() {
        return maxHDistance;
    }
}
