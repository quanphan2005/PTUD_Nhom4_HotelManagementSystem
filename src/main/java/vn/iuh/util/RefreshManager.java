package vn.iuh.util;

import vn.iuh.gui.panel.booking.PreReservationManagementPanel;
import vn.iuh.gui.panel.booking.PreReservationSearchPanel;
import vn.iuh.gui.panel.booking.BookingManagementPanel;
import vn.iuh.gui.panel.booking.ReservationManagementPanel;

public class RefreshManager {
    private static BookingManagementPanel bookingManagementPanel;
    private static ReservationManagementPanel reservationManagementPanel;
    private static PreReservationManagementPanel preReservationManagementPanel;
    private static PreReservationSearchPanel preReservationSearchPanel;

    // Registration methods
    public static void setBookingManagementPanel(BookingManagementPanel panel) {
        bookingManagementPanel = panel;
        System.out.println("RefreshManager: setBookingManagementPanel registered");
    }

    public static void setReservationManagementPanel(ReservationManagementPanel panel) {
        reservationManagementPanel = panel;
        System.out.println("RefreshManager: ReservationManagementPanel registered");
    }

    public static void setPreReservationManagementPanel(PreReservationManagementPanel panel) {
        preReservationManagementPanel = panel;
        System.out.println("RefreshManager: PreReservationManagementPanel registered");
    }

    public static void setPreReservationSearchPanel(PreReservationSearchPanel panel) {
        preReservationSearchPanel = panel;
        System.out.println("RefreshManager: PreReservationSearchPanel registered");
    }

    // Individual refresh methods
    public static void refreshBookingManagementPanel() {
        if (bookingManagementPanel != null)
            bookingManagementPanel.refreshPanel();
    }

    public static void refreshReservationManagementPanel() {
        if (reservationManagementPanel != null)
            reservationManagementPanel.refreshPanel();
    }

    public static void refreshPreReservationManagementPanel() {
        if (preReservationManagementPanel != null)
            preReservationManagementPanel.refreshPanel();
    }

    public static void refreshPreReservationSearchPanel() {
        if (preReservationSearchPanel != null)
            preReservationSearchPanel.refreshPanel();
    }

    // Comprehensive refresh method
    public static void refreshAll() {
        System.out.println("RefreshManager: Refreshing all panels...");
        refreshBookingManagementPanel();
        refreshReservationManagementPanel();
        refreshPreReservationManagementPanel();
        refreshPreReservationSearchPanel();
    }

    // Method specifically for after booking operations
    public static void refreshAfterBooking() {
        System.out.println("RefreshManager: Refreshing after booking operation...");
        refreshBookingManagementPanel();
        refreshReservationManagementPanel();
        refreshPreReservationManagementPanel();
        refreshPreReservationSearchPanel();
    }

    public static void refreshAfterCancelReservation() {
        System.out.println("RefreshManager: Refreshing after cancel reservation...");
        refreshBookingManagementPanel();
        refreshReservationManagementPanel();
        refreshPreReservationManagementPanel();
        refreshPreReservationSearchPanel();
    }

    public static void refreshAfterCleaning() {
        System.out.println("RefreshManager: Refreshing after cleaning operation...");
        refreshBookingManagementPanel();
    }
}