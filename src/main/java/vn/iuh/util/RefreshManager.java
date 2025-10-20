package vn.iuh.util;

import vn.iuh.gui.base.GridRoomPanel;
import vn.iuh.gui.panel.booking.PreReservationManagementPanel;
import vn.iuh.gui.panel.booking.PreReservationSearchPanel;
import vn.iuh.gui.panel.booking.BookingManagementPanel;

public class RefreshManager {
    private static BookingManagementPanel bookingManagementPanel;
    private static PreReservationManagementPanel preReservationManagementPanel;
    private static PreReservationSearchPanel preReservationSearchPanel;
    private static GridRoomPanel gridRoomPanel;

    // Registration methods
    public static void setReservationManagementPanel(BookingManagementPanel panel) {
        bookingManagementPanel = panel;
        System.out.println("RefreshManager: ReservationManagementPanel registered");
    }

    public static void setReservationFormManagementPanel(PreReservationManagementPanel panel) {
        preReservationManagementPanel = panel;
        System.out.println("RefreshManager: ReservationFormManagementPanel registered");
    }

    public static void setReservationFormSearchPanel(PreReservationSearchPanel panel) {
        preReservationSearchPanel = panel;
        System.out.println("RefreshManager: ReservationFormSearchPanel registered");
    }

    public static void setGridRoomPanel(GridRoomPanel panel) {
        gridRoomPanel = panel;
        System.out.println("RefreshManager: GridRoomPanel registered");
    }

    // Individual refresh methods
    public static void refreshReservationManagementPanel() {
        if (bookingManagementPanel != null)
            bookingManagementPanel.refreshPanel();
    }

    public static void refreshReservationFormManagementPanel() {
        if (preReservationManagementPanel != null)
            preReservationManagementPanel.refreshPanel();
    }

    public static void refreshReservationFormSearchPanel() {
        if (preReservationSearchPanel != null)
            preReservationSearchPanel.refreshPanel();
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

    public static void refreshAfterCleaning() {
        System.out.println("RefreshManager: Refreshing after cleaning operation...");
        refreshReservationManagementPanel();
        refreshReservationFormManagementPanel();
        refreshReservationFormSearchPanel();
    }
}