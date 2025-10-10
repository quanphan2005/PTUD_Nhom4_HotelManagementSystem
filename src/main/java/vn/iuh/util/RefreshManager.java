package vn.iuh.util;

import vn.iuh.constraint.PanelName;
import vn.iuh.gui.base.GridRoomPanel;
import vn.iuh.gui.base.Main;
import vn.iuh.gui.panel.booking.ReservationFormManagementPanel;
import vn.iuh.gui.panel.booking.ReservationFormSearchPanel;
import vn.iuh.gui.panel.booking.ReservationManagementPanel;

public class RefreshManager {
    private static ReservationManagementPanel reservationManagementPanel;
    private static ReservationFormManagementPanel reservationFormManagementPanel;
    private static ReservationFormSearchPanel reservationFormSearchPanel;
    private static GridRoomPanel gridRoomPanel;

    // Registration methods
    public static void setReservationManagementPanel(ReservationManagementPanel panel) {
        reservationManagementPanel = panel;
        System.out.println("RefreshManager: ReservationManagementPanel registered");
    }

    public static void setReservationFormManagementPanel(ReservationFormManagementPanel panel) {
        reservationFormManagementPanel = panel;
        System.out.println("RefreshManager: ReservationFormManagementPanel registered");
    }

    public static void setReservationFormSearchPanel(ReservationFormSearchPanel panel) {
        reservationFormSearchPanel = panel;
        System.out.println("RefreshManager: ReservationFormSearchPanel registered");
    }

    public static void setGridRoomPanel(GridRoomPanel panel) {
        gridRoomPanel = panel;
        System.out.println("RefreshManager: GridRoomPanel registered");
    }

    // Individual refresh methods
    public static void refreshReservationManagementPanel() {
        if (reservationManagementPanel != null)
            reservationManagementPanel.refreshPanel();
    }

    public static void refreshReservationFormManagementPanel() {
        if (reservationFormManagementPanel != null)
            reservationFormManagementPanel.refreshPanel();
    }

    public static void refreshReservationFormSearchPanel() {
        if (reservationFormSearchPanel != null)
            reservationFormSearchPanel.refreshPanel();
    }

    // Comprehensive refresh method
    public static void refreshAll() {
        System.out.println("RefreshManager: Refreshing all panels...");
        refreshReservationManagementPanel();
        refreshReservationFormManagementPanel();
    }

    // Method specifically for after booking operations
    public static void refreshAfterBooking() {
        System.out.println("RefreshManager: Refreshing after booking operation...");
        refreshReservationManagementPanel();
        refreshReservationFormManagementPanel();
        refreshReservationFormSearchPanel();
    }

    public static void refreshAfterCancelReservation() {
        System.out.println("RefreshManager: Refreshing after cancel reservation...");
        refreshReservationManagementPanel();
        refreshReservationFormManagementPanel();
        refreshReservationFormSearchPanel();
    }
}