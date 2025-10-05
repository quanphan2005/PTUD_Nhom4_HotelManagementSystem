package vn.iuh.util;

import vn.iuh.constraint.PanelName;
import vn.iuh.gui.base.GridRoomPanel;
import vn.iuh.gui.base.Main;
import vn.iuh.gui.panel.booking.ReservationFormManagementPanel;
import vn.iuh.gui.panel.booking.ReservationManagementPanel;

public class RefreshManager {
    private static ReservationManagementPanel reservationManagementPanel;
    private static ReservationFormManagementPanel reservationFormManagementPanel;
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

    public static void setGridRoomPanel(GridRoomPanel panel) {
        gridRoomPanel = panel;
        System.out.println("RefreshManager: GridRoomPanel registered");
    }

    // Individual refresh methods
    public static void refreshReservationManagementPanel() {
        Main.removeCard(reservationManagementPanel);
        reservationManagementPanel = new ReservationManagementPanel();
        Main.addCard(reservationManagementPanel, PanelName.RESERVATION_MANAGEMENT.getName());
    }

    public static void refreshReservationFormManagementPanel() {
        Main.removeCard(reservationFormManagementPanel);
        reservationFormManagementPanel = new ReservationFormManagementPanel();
        Main.addCard(reservationFormManagementPanel, PanelName.RESERVATION_FORM_MANAGEMENT.getName());
    }

    public static void refreshGridRoomPanel() {
        if (gridRoomPanel != null) {
            System.out.println("RefreshManager: Refreshing GridRoomPanel");
            // Trigger refresh through reservation management panel since it manages the grid
            refreshReservationManagementPanel();
        } else {
            System.out.println("RefreshManager: GridRoomPanel is null - not registered properly");
        }
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
    }
}