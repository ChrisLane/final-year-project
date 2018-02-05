package me.chrislane.accudrop.presenter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;

import me.chrislane.accudrop.fragment.ReplayFragment;
import me.chrislane.accudrop.task.CheckJumpExistsTask;
import me.chrislane.accudrop.task.FetchJumpIdTask;
import me.chrislane.accudrop.task.FetchJumpTask;
import me.chrislane.accudrop.viewmodel.JumpViewModel;
import me.chrislane.accudrop.viewmodel.ReplayViewModel;
import me.chrislane.accudrop.viewmodel.RouteViewModel;

public class ReplayPresenter {

    private final ReplayFragment replayFragment;
    private ReplayViewModel replayViewModel;
    private JumpViewModel jumpViewModel;
    private RouteViewModel routeViewModel;

    public ReplayPresenter(ReplayFragment replayFragment) {
        this.replayFragment = replayFragment;

        replayViewModel = ViewModelProviders.of(replayFragment).get(ReplayViewModel.class);
        jumpViewModel = ViewModelProviders.of(replayFragment).get(JumpViewModel.class);
        routeViewModel = ViewModelProviders.of(replayFragment).get(RouteViewModel.class);

        subscribeToJumpId();

        FetchJumpIdTask.Listener listener = jumpId -> {
            if (jumpId != null) {
                replayViewModel.setJumpId(jumpId);
            }
        };
        new FetchJumpIdTask(listener, jumpViewModel).execute();
    }

    /**
     * Set the route to be displayed in replay views.
     *
     * @param jumpID The jump ID to get the route from.
     */
    public void setRoute(int jumpID) {
        FetchJumpTask.FetchJumpListener listener = result -> routeViewModel.setRoute(result);
        new FetchJumpTask(listener, jumpViewModel).execute(jumpID);
    }

    /**
     * Set the new route when the jump ID changes.
     */
    private void subscribeToJumpId() {
        final Observer<Integer> jumpIdObserver = this::setRoute;
        replayViewModel.getJumpId().observe(replayFragment, jumpIdObserver);
    }

    public void prevJump() {
        Integer jumpId = replayViewModel.getJumpId().getValue();
        if (jumpId != null) {
            CheckJumpExistsTask.Listener listener = jumpExists -> {
                if (jumpExists) {
                    replayViewModel.setJumpId(jumpId - 1);
                }
            };
            new CheckJumpExistsTask(listener, jumpViewModel).execute(jumpId - 1);
        }
    }

    public void nextJump() {
        Integer jumpId = replayViewModel.getJumpId().getValue();
        if (jumpId != null) {
            CheckJumpExistsTask.Listener listener = jumpExists -> {
                if (jumpExists) {
                    replayViewModel.setJumpId(jumpId + 1);
                }
            };
            new CheckJumpExistsTask(listener, jumpViewModel).execute(jumpId + 1);
        }
    }
}