package com.mcaminecolonies.client.gui;

/**
 * Global client-side interaction state tracking for MineColonies visitor windows.
 */
public final class VisitorInteractionState {

    private static boolean s_ShowStatsRequested = false;

    private VisitorInteractionState() {
    }

    public static boolean isShowStatsRequested() {
        return s_ShowStatsRequested;
    }

    public static void setShowStatsRequested(boolean requested) {
        s_ShowStatsRequested = requested;
    }
}

