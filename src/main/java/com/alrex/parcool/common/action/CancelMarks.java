package com.alrex.parcool.common.action;

import java.util.LinkedList;

public class CancelMarks {
    public interface Marker {
        boolean remain();
    }

    private final LinkedList<Marker> jumpCancelMarks = new LinkedList<>();
    private final LinkedList<Marker> descendFromEdgeCancelMarks = new LinkedList<>();
    private final LinkedList<Marker> sneakCancelMarks = new LinkedList<>();

    public void addMarkerCancellingJump(Marker marker) {
        jumpCancelMarks.add(marker);
    }

    public void addMarkerCancellingSneak(Marker marker) {
        sneakCancelMarks.add(marker);
    }

    public void addMarkerCancellingDescendFromEdge(Marker marker) {
        descendFromEdgeCancelMarks.add(marker);
    }

    public boolean cancelJump() {
        jumpCancelMarks.removeIf(it -> !it.remain());
        return !jumpCancelMarks.isEmpty();
    }

    public boolean cancelSneak() {
        sneakCancelMarks.removeIf(it -> !it.remain());
        return !sneakCancelMarks.isEmpty();
    }

    public boolean cancelDescendFromEdge() {
        descendFromEdgeCancelMarks.removeIf(it -> !it.remain());
        return !descendFromEdgeCancelMarks.isEmpty();
    }
}
