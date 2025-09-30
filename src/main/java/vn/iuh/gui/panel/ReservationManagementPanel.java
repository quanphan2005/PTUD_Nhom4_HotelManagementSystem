package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.GridRoomPanel;
import vn.iuh.gui.base.RoomItem;
import vn.iuh.schedule.RoomStatusHandler;
import vn.iuh.servcie.BookingService;
import vn.iuh.servcie.impl.BookingServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationManagementPanel extends JPanel {
    private GridRoomPanel gridRoomPanels;
    private final BookingService bookingService;

    // Search panel components
    private JComboBox<String> cmbRoomType;
    private JComboBox<Integer> cmbCapacity;
    private JSpinner spnCheckInDate;

    // Mode toggle components
    private boolean isMultiBookingMode = false;
    private JToggleButton btnMultiBookingToggle;
    private JLabel lblSelectedRooms;
    private JButton btnConfirmSelection;

    public ReservationManagementPanel() {
        bookingService = new BookingServiceImpl();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        init();
    }

    private void init() {
        createTopPanel();
        createSearchAndStatusPanel();
        createModeTogglePanel();
        createCenterPanel();
    }

    private void createTopPanel() {
        JPanel pnlTop = new JPanel();
        JLabel lblTop = new JLabel("Quản lý đặt phòng", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.normalFont);

        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop);
        pnlTop.setPreferredSize(new Dimension(0, 50));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, " arc: 10");

        add(pnlTop);
    }

    private void createSearchAndStatusPanel() {
        // Main container panel for search and status sections (50/50 split)
        JPanel mainContainer = new JPanel(new GridLayout(1, 2, 10, 0));
        mainContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Left side - Search panel (50% width)
        JPanel leftPanel = createSearchPanel();

        // Right side - Status panel (50% width)
        JPanel rightPanel = createStatusPanel();

        mainContainer.add(leftPanel);
        mainContainer.add(rightPanel);

        add(mainContainer);
    }

    private void createModeTogglePanel() {
        JPanel modePanel = new JPanel(new BorderLayout());
        modePanel.setBackground(Color.WHITE);
        modePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 20, 5, 20)
        ));

        // Left side - Toggle button
        ImageIcon errorIcon = new ImageIcon(getClass().getResource("/icons/error.png"));
        errorIcon = new ImageIcon(errorIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));

        btnMultiBookingToggle = new JToggleButton("Đặt nhiều phòng", errorIcon);
        btnMultiBookingToggle.setFont(CustomUI.smallFont);
        btnMultiBookingToggle.setPreferredSize(new Dimension(200, 35));
        btnMultiBookingToggle.setBackground(Color.LIGHT_GRAY);
        btnMultiBookingToggle.setForeground(Color.BLACK);
        btnMultiBookingToggle.setFocusPainted(false);
        btnMultiBookingToggle.addActionListener(e -> toggleMultiBookingMode());

        // Right side panel for the two buttons
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtonPanel.setBackground(Color.WHITE);

        // Selected rooms count label
        lblSelectedRooms = new JLabel("Phòng đã chọn: 0");
        lblSelectedRooms.setFont(CustomUI.smallFont);
        lblSelectedRooms.setPreferredSize(new Dimension(250, 35));
        lblSelectedRooms.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        lblSelectedRooms.setEnabled(false); // Initially disabled

        // Confirm selection button
        btnConfirmSelection = new JButton("Xác Nhận");
        btnConfirmSelection.setFont(CustomUI.smallFont);
        btnConfirmSelection.setPreferredSize(new Dimension(150, 35));
        btnConfirmSelection.setBackground(CustomUI.darkGreen);
        btnConfirmSelection.setForeground(Color.WHITE);
        btnConfirmSelection.setFocusPainted(false);
        btnConfirmSelection.setEnabled(false); // Initially disabled
        btnConfirmSelection.addActionListener(e -> confirmRoomSelection());

        rightButtonPanel.add(lblSelectedRooms);
        rightButtonPanel.add(btnConfirmSelection);

        modePanel.add(btnMultiBookingToggle, BorderLayout.WEST);
        modePanel.add(rightButtonPanel, BorderLayout.EAST);

        add(modePanel);
    }

    private void toggleMultiBookingMode() {
        isMultiBookingMode = btnMultiBookingToggle.isSelected();

        if (isMultiBookingMode) {
            ImageIcon checkedIcon = new ImageIcon(getClass().getResource("/icons/checked.png"));
            checkedIcon = new ImageIcon(checkedIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));

            btnMultiBookingToggle.setIcon(checkedIcon);
            btnMultiBookingToggle.setBackground(CustomUI.purple);
            btnMultiBookingToggle.setForeground(Color.WHITE);

            // Enable the two buttons on the right
            lblSelectedRooms.setEnabled(true);
            btnConfirmSelection.setEnabled(true);

            // Update label color when enabled
            lblSelectedRooms.setOpaque(true);
            lblSelectedRooms.setBackground(Color.WHITE);
        } else {
            ImageIcon errorIcon = new ImageIcon(getClass().getResource("/icons/error.png"));
            errorIcon = new ImageIcon(errorIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));

            btnMultiBookingToggle.setIcon(errorIcon);
            btnMultiBookingToggle.setBackground(Color.LIGHT_GRAY);
            btnMultiBookingToggle.setForeground(Color.BLACK);

            // Disable the two buttons on the right
            lblSelectedRooms.setEnabled(false);
            btnConfirmSelection.setEnabled(false);

            // Reset selected rooms count
            lblSelectedRooms.setText("Phòng đã chọn: 0");
            lblSelectedRooms.setOpaque(false);
        }

        // Repaint to show changes
        repaint();
    }

    private void confirmRoomSelection() {
        if (isMultiBookingMode) {
            // TODO: Implement multi-room booking confirmation logic
            JOptionPane.showMessageDialog(this,
                "Xác nhận đặt " + getSelectedRoomsCount() + " phòng",
                "Xác nhận đặt phòng",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private int getSelectedRoomsCount() {
        // TODO: Implement logic to count selected rooms
        // This would typically involve tracking which rooms are selected in the grid
        return 0;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomUI.lightBlue, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(400, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Initialize components
        initializeSearchComponents();

        // Add form rows following BookingFormPanel style
        addFormRow(panel, gbc, 0, "Loại phòng:", cmbRoomType);
        addFormRow(panel, gbc, 1, "Số người:", cmbCapacity);
        addFormRow(panel, gbc, 2, "Checkin dự kiến:", spnCheckInDate);

        return panel;
    }

    private void initializeSearchComponents() {
        // Room type dropdown - populate with actual room categories
        cmbRoomType = new JComboBox<>();
        cmbRoomType.addItem("TẤT CẢ");
        cmbRoomType.addItem("VIP");
        cmbRoomType.addItem("THƯỜNG");
        cmbRoomType.setPreferredSize(new Dimension(200, 35));
        cmbRoomType.setFont(CustomUI.smallFont);

        // Capacity dropdown
        cmbCapacity = new JComboBox<>();
        cmbCapacity.addItem(1);
        cmbCapacity.addItem(2);
        cmbCapacity.addItem(4);

        cmbCapacity.setSelectedIndex(0); // Default to 1
        cmbCapacity.setPreferredSize(new Dimension(200, 35));
        cmbCapacity.setFont(CustomUI.smallFont);

        // Check-in date spinner following BookingFormPanel style
        spnCheckInDate = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor checkInEditor = new JSpinner.DateEditor(spnCheckInDate, "dd/MM/yyyy");
        spnCheckInDate.setEditor(checkInEditor);
        spnCheckInDate.setPreferredSize(new Dimension(200, 35));
        spnCheckInDate.setFont(CustomUI.smallFont);
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CustomUI.lightGreen, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(600, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create status buttons in 2 rows as shown in the design
        createStatusButton(panel, gbc, 0, 0, "Trống (10)", CustomUI.lightGreen);
        createStatusButton(panel, gbc, 1, 0, "Đang Chờ (10)", CustomUI.cyan);
        createStatusButton(panel, gbc, 2, 0, "Kiểm tra (10)", CustomUI.lightBlue);

        createStatusButton(panel, gbc, 0, 1, "Sử dụng (10)", Color.ORANGE);
        createStatusButton(panel, gbc, 1, 1, "Trễ (10)", Color.RED);
        createStatusButton(panel, gbc, 2, 1, "Dọn dẹp (10)", new Color(144, 238, 144));

        createStatusButton(panel, gbc, 0, 2, "Đang bảo trì (10)", Color.LIGHT_GRAY);

        return panel;
    }

    private void createStatusButton(JPanel parent, GridBagConstraints gbc, int x, int y, String text, Color color) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JButton statusBtn = new JButton(text);
        statusBtn.setBackground(color);
        statusBtn.setForeground(Color.BLACK);
        statusBtn.setFont(CustomUI.smallFont);
        statusBtn.setPreferredSize(new Dimension(150, 50));
        statusBtn.setFocusPainted(false);
        statusBtn.putClientProperty(FlatClientProperties.STYLE,
                                    "arc: 10; " +
                                    "borderWidth: 1; " +
                                    "borderColor: #808080; " +
                                    "margin: 10,10,10,10"
        );


        parent.add(statusBtn, gbc);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent component) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel label = new JLabel(labelText);
        label.setFont(CustomUI.smallFont);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        component.setFont(CustomUI.smallFont);
        if (component instanceof JTextField) {
            component.setPreferredSize(new Dimension(200, 35));
            component.setMinimumSize(new Dimension(180, 35));
        } else if (component instanceof JSpinner) {
            component.setPreferredSize(new Dimension(200, 35));
            component.setMinimumSize(new Dimension(180, 35));
        } else if (component instanceof JComboBox) {
            component.setPreferredSize(new Dimension(200, 35));
            component.setMinimumSize(new Dimension(180, 35));
        }
        panel.add(component, gbc);
    }

    private void createCenterPanel() {
        List<RoomItem> roomItems = new ArrayList<>();

        List<BookingResponse> bookingResponses = bookingService.getAllBookingInfo();
        for (BookingResponse bookingResponse : bookingResponses) {
            roomItems.add(new RoomItem(bookingResponse));
        }

        gridRoomPanels = new GridRoomPanel(roomItems);
        gridRoomPanels.setBackground(Color.WHITE);
        gridRoomPanels.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(gridRoomPanels,
                                     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                     JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        setOpaque(true);
        add(scrollPane);

        // Create cron job to update room status every 1 minute
//        createUpdateRoomStatusSchedule(gridRoomPanels);
    }

//    private void search() {
//        String selectedRoomType = (String) cmbRoomType.getSelectedItem();
//        Integer selectedCapacity = (Integer) cmbCapacity.getSelectedItem();
//        java.util.Date selectedDate = (java.util.Date) spnCheckInDate.getValue();
//
//        List<RoomItem> filteredRooms = new ArrayList<>();
//
//        for (RoomItem roomItem : gridRoomPanels.getRoomItems()) {
//            boolean matches = true;
//
//            // Filter by room type
//            if (selectedRoomType != null && !selectedRoomType.equals("Tất cả các loại phòng")) {
//                matches = roomItem.getRoomCategoryName().equals(selectedRoomType);
//            }
//
//            // Filter by capacity
//            if (matches && selectedCapacity != null) {
//                matches = roomItem.getCapacity().equals(selectedCapacity);
//            }
//
//            // Filter by check-in date
//            if (matches && selectedDate != null) {
//                matches = roomItem.getAvailableDate().equals(selectedDate);
//            }
//
//            if (matches) {
//                filteredRooms.add(roomItem);
//            }
//        }
//
//        gridRoomPanels.setRoomItems(filteredRooms);
//        gridRoomPanels.revalidate();
//        gridRoomPanels.repaint();
//    }

//    private void reset() {
//        cmbRoomType.setSelectedIndex(0);
//        cmbCapacity.setSelectedIndex(0);
//        spnCheckInDate.setValue(new java.util.Date());
//
//        gridRoomPanels.setRoomItems(bookingService.getAllBookingInfo());
//        gridRoomPanels.revalidate();
//        gridRoomPanels.repaint();
//    }

    private void createUpdateRoomStatusSchedule(GridRoomPanel gridRoomPanel) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("gridRoomPanel", gridRoomPanel);

            JobDetail jobDetail = JobBuilder.newJob(RoomStatusHandler.class)
                    .withIdentity("roomStatusUpdateJob", "group1")
                    .usingJobData(jobDataMap)
                    .build();

            Trigger trigger = org.quartz.TriggerBuilder.newTrigger()
                    .withIdentity("roomStatusUpdateTrigger", "group1")
                    .withSchedule(org.quartz.SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInSeconds(10)
                            .repeatForever())
                    .build();

            scheduler.start();
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            System.out.println("Error creating schedule: " + e.getMessage());
        }
    }

}
