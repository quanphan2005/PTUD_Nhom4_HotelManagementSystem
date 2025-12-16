package vn.iuh.gui.panel;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatIntelliJLaf;
import vn.iuh.constraint.Fee;
import vn.iuh.dto.repository.ThongTinPhuPhi;
import vn.iuh.dto.repository.WarningReservation;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.dialog.PhuPhiDialog;
import vn.iuh.service.WarningReservationService;
import vn.iuh.service.impl.WarningReservationImpl;
import vn.iuh.util.BackupDatabase;
import vn.iuh.util.FeeValue;
import vn.iuh.util.RefreshManager;
import vn.iuh.util.RestoreDataBase;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SystemConfigPanel extends JPanel {
    private JTextField vatRateField;
    private JTextField backupDirField;
    private JTextField backupNameField;
    private JTextField restoreDirField;
    private JComboBox<String> backupTypeCombo;
    private JRadioButton autoBackupRadio;
    private JRadioButton warningBackupRadio;
    private JRadioButton noBackupRadio;
    private DefaultTableModel tableModel;
    private JTable fileTable;
    private JLabel fileCountLabel;
    private final java.util.List<File> selectedFiles = new ArrayList<>();
    private JButton restoreBtn;
    private WarningReservationService warningReservationService;



    private JPanel createTopPanel() {
        JPanel pnlTop = new JPanel();
        JLabel lblTop = new JLabel("THIẾT LẬP HỆ THỐNG", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.bigFont);

        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop);

        pnlTop.setPreferredSize(new Dimension(0, 40));
        pnlTop.setMinimumSize(new Dimension(0, 40));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, " arc: 10");

        return pnlTop;
    }
    private void init(){
        this.warningReservationService = new WarningReservationImpl();
    }


    public SystemConfigPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        // Main content panel with vertical layout
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(Color.WHITE);

        // Top section - VAT Rate
        JPanel topPanel = createVATPanel();
        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        contentPanel.add(topPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Middle section - Backup settings (2 columns)
        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 15, 0));
        middlePanel.setOpaque(false);
        middlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        middlePanel.add(createAutoBackupPanel());
        middlePanel.add(createManualBackupPanel());
        contentPanel.add(middlePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Bottom section - File recovery (takes remaining space)
        JPanel bottomPanel = createRecoveryPanel();
        contentPanel.add(bottomPanel);

        // Wrap in scroll pane for responsive design
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);


        JPanel pnlNorth = createTopPanel();

        add(scrollPane, BorderLayout.CENTER);
        add(pnlNorth, BorderLayout.NORTH);
        init();
    }

    private JPanel createVATPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(CustomUI.tableBorder, 2),
                        "Thuế giá trị gia tăng",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        CustomUI.verySmallFont,
                        CustomUI.bluePurple
                ),
                new EmptyBorder(10, 15, 15, 15)
        ));

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);

        // Label
        JLabel vatLabel = new JLabel("Thuế GTGT (%):");
        vatLabel.setFont(CustomUI.verySmallFont);
        vatLabel.setPreferredSize(new Dimension(120, 35));

        // Text field
        ThongTinPhuPhi fee = FeeValue.getInstance().get(Fee.THUE);
        vatRateField = new JTextField(fee.getGiaHienTai().toString());
        vatRateField.setEditable(false);
        vatRateField.setFocusable(false);
        vatRateField.setFont(CustomUI.verySmallFont);
        vatRateField.setPreferredSize(new Dimension(100, 35));
        vatRateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonsPanel.setOpaque(false);

        ImageIcon historyIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/clock.png")));
        JButton historyBtn = createIconButton(historyIcon);
        historyBtn.addActionListener((e) -> {
            Window owner = SwingUtilities.getWindowAncestor(SystemConfigPanel.this);
            PhuPhiDialog dialog = new PhuPhiDialog(owner, fee);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    ThongTinPhuPhi fee = FeeValue.getInstance().get(Fee.THUE);
                    vatRateField.setText(fee.getGiaHienTai().toString());
                }
            });
            dialog.setVisible(true);
        });

        historyBtn.setToolTipText("Xem lịch sử thay đổi");

//        buttonsPanel.add(editBtn);
        buttonsPanel.add(historyBtn);

        contentPanel.add(vatLabel);
        contentPanel.add(vatRateField);
        contentPanel.add(buttonsPanel);

        panel.add(contentPanel);
        return panel;
    }
    private double validateInputVAT(String input){
        try {
            return Double.parseDouble(input);
        }catch (Exception e){
            return 0;
        }
    }

    private JPanel createAutoBackupPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(CustomUI.tableBorder, 2),
                        "Sao lưu dữ liệu tự động",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        CustomUI.verySmallFont,
                        CustomUI.bluePurple
                ),
                new EmptyBorder(10, 15, 15, 15)
        ));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Backup directory section
        JPanel dirPanel = new JPanel(new BorderLayout(10, 0));
        dirPanel.setOpaque(false);
        dirPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel dirLabel = new JLabel("Thư mục sao lưu");
        dirLabel.setFont(CustomUI.TABLE_FONT);
        dirLabel.setPreferredSize(new Dimension(120, 35));

        backupDirField = new JTextField("D:\\");
        backupDirField.setFont(CustomUI.TABLE_FONT);
        backupDirField.setPreferredSize(new Dimension(0, 35));
        backupDirField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        ImageIcon folderIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/folder.png")));
        JButton browseDirBtn = createFolderIconButton(folderIcon,backupDirField);

        dirPanel.add(dirLabel, BorderLayout.WEST);
        dirPanel.add(backupDirField, BorderLayout.CENTER);
        dirPanel.add(browseDirBtn, BorderLayout.EAST);

        contentPanel.add(dirPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Radio buttons panel with left alignment
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        radioPanel.setOpaque(false);
        radioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        ButtonGroup group = new ButtonGroup();

        autoBackupRadio = new JRadioButton("Tự động sao lưu, khi kết thúc chương trình");
        autoBackupRadio.setFont(CustomUI.TABLE_FONT);
        autoBackupRadio.setOpaque(false);
        autoBackupRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(autoBackupRadio);
        radioPanel.add(autoBackupRadio);
        radioPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        warningBackupRadio = new JRadioButton("Cảnh báo sao lưu khi kết thúc chương trình");
        warningBackupRadio.setFont(CustomUI.TABLE_FONT);
        warningBackupRadio.setOpaque(false);
        warningBackupRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(warningBackupRadio);
        radioPanel.add(warningBackupRadio);
        radioPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        noBackupRadio = new JRadioButton("Không sao lưu");
        noBackupRadio.setFont(CustomUI.TABLE_FONT);
        noBackupRadio.setSelected(true);
        noBackupRadio.setOpaque(false);
        noBackupRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(noBackupRadio);
        radioPanel.add(noBackupRadio);

        contentPanel.add(radioPanel);

        panel.add(contentPanel, BorderLayout.NORTH);
        return panel;
    }

    private String generateFileName(String backupType){
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        String fileName = "HotelBackup-" + today.format(formatter);

        if ("Ngày hôm nay".equalsIgnoreCase(backupType)) {
            fileName += "-DIF";
        } else if ("Toàn bộ".equalsIgnoreCase(backupType)) {
            fileName += "-FULL";
        }

        return fileName+".bak";
    }

    private JPanel createManualBackupPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(CustomUI.tableBorder, 2),
                        "Sao lưu dữ liệu thủ công:",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        CustomUI.verySmallFont,
                        CustomUI.bluePurple
                ),
                new EmptyBorder(10, 15, 15, 15)
        ));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Backup type
        JPanel typePanel = new JPanel(new BorderLayout(10, 0));
        typePanel.setOpaque(false);
        typePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel typeLabel = new JLabel("Hình thức sao lưu");
        typeLabel.setFont(CustomUI.TABLE_FONT);
        typeLabel.setPreferredSize(new Dimension(120, 35));

        backupTypeCombo = new JComboBox<>(new String[]{"Ngày hôm nay", "Toàn bộ"});
        backupTypeCombo.setFont(CustomUI.TABLE_FONT);
        backupTypeCombo.setPreferredSize(new Dimension(0, 35));
        backupTypeCombo.addActionListener((e) -> {
            String selected = (String) backupTypeCombo.getSelectedItem();
            String fileName = generateFileName(selected);
            backupNameField.setText(fileName);
        });

        typePanel.add(typeLabel, BorderLayout.WEST);
        typePanel.add(backupTypeCombo, BorderLayout.CENTER);

        contentPanel.add(typePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Backup name
        JPanel namePanel = new JPanel(new BorderLayout(10, 0));
        namePanel.setOpaque(false);
        namePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel nameLabel = new JLabel("Tên tệp dữ liệu");
        nameLabel.setFont(CustomUI.TABLE_FONT);
        nameLabel.setPreferredSize(new Dimension(120, 35));

        backupNameField = new JTextField();
        backupNameField.setFont(CustomUI.TABLE_FONT);
        backupNameField.setPreferredSize(new Dimension(0, 35));
        backupNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        backupNameField.setEditable(false);
        backupNameField.setFocusable(false);

        backupNameField.setText(generateFileName("Ngày hôm nay"));


        namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(backupNameField, BorderLayout.CENTER);

        contentPanel.add(namePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Restore directory
        JPanel restorePanel = new JPanel(new BorderLayout(10, 0));
        restorePanel.setOpaque(false);
        restorePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel restoreLabel = new JLabel("Thư mục sao lưu");
        restoreLabel.setFont(CustomUI.TABLE_FONT);
        restoreLabel.setPreferredSize(new Dimension(120, 35));

        restoreDirField = new JTextField();
        restoreDirField.setFont(CustomUI.TABLE_FONT);
        restoreDirField.setPreferredSize(new Dimension(0, 35));
        restoreDirField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));


        ImageIcon folderIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/folder.png")));
        JButton browseDirBtn = createFolderIconButton(folderIcon,restoreDirField);

        restorePanel.add(restoreLabel, BorderLayout.WEST);
        restorePanel.add(restoreDirField, BorderLayout.CENTER);
        restorePanel.add(browseDirBtn, BorderLayout.EAST);

        contentPanel.add(restorePanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Backup button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton backupBtn = new JButton("Sao lưu");
        backupBtn.setFont(CustomUI.verySmallFont);
        backupBtn.setPreferredSize(new Dimension(120, 38));
        backupBtn.setBackground(CustomUI.darkBlue);
        backupBtn.setForeground(Color.WHITE);
        backupBtn.setFocusPainted(false);
        backupBtn.setBorderPainted(false);
        backupBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        backupBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                backupBtn.setBackground(new Color(51, 122, 183));
            }
            public void mouseExited(MouseEvent e) {
                backupBtn.setBackground(new Color(66, 139, 202));
            }
        });

        backupBtn.addActionListener((e) ->{
            String fileName = backupNameField.getText();
            String dirPath = restoreDirField.getText();
            String backupType = (String) backupTypeCombo.getSelectedItem();
            if(!isValidFileName(fileName)){
                JOptionPane.showMessageDialog(null, "Tên file chỉ bao gồm chữ, số, gạch nối, gạch dưới", "Sai định dạng tên file", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if(!isValidDirPath(dirPath)){
                JOptionPane.showMessageDialog(null, "Không tồn tại đường dẫn", "Sai định dạng thư mục lưu trữ", JOptionPane.ERROR_MESSAGE);
                return;
            }

            handleBackupData(dirPath + "\\" + fileName, backupType, (success) -> {
                if (!success) {
                    JOptionPane.showMessageDialog(null, "Sao lưu thất bại", "", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Sao lưu thành công", "Đã sao lưu dữ liệu", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        });

        buttonPanel.add(backupBtn);
        contentPanel.add(buttonPanel);

        panel.add(contentPanel, BorderLayout.NORTH);
        return panel;
    }

    private void handleBackupData(String filePath, String backupType, Consumer<Boolean> callback) {
        JDialog loadingDialog = new JDialog();
        loadingDialog.add(new JLabel("Đang xử lý..."));
        loadingDialog.setSize(200, 100);
        loadingDialog.setLocationRelativeTo(null);
        loadingDialog.setTitle("Sao lưu dữ liệu");
        loadingDialog.setFont(CustomUI.TABLE_FONT);

        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));

        CompletableFuture.supplyAsync(() -> {
            try {
                if ("Toàn bộ".equalsIgnoreCase(backupType))
                    return BackupDatabase.backupFullDatabase(filePath);
                else
                    return BackupDatabase.backupDifDatabase(filePath);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }).thenAccept(result -> {
            loadingDialog.dispose();
            callback.accept(result);
        });
    }

    public boolean isValidDirPath(String path) {
        if (path == null || path.trim().isEmpty()) return false;

        String regex = "^[A-Za-z]:\\\\([^\\\\/:*?\"<>|]+\\\\)*[^\\\\/:*?\"<>|]*$";
        return path.matches(regex);
    }

    private boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) return false;

        System.out.println(fileName);
        // Chỉ chấp nhận chữ, số, gạch nối, gạch dưới
        String regex = "^[A-Za-z0-9-_]+\\.bak$";

        return fileName.matches(regex);
    }

    private JPanel createRecoveryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(CustomUI.tableBorder, 2),
                        "Khôi phục dữ liệu:",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        CustomUI.verySmallFont,
                        CustomUI.bluePurple
                ),
                new EmptyBorder(10, 15, 15, 15)
        ));

        // Top section with directory selection
        JPanel topSection = new JPanel(new BorderLayout(10, 0));
        topSection.setOpaque(false);

        JLabel selectLabel = new JLabel("Chọn thư mục dữ liệu");
        selectLabel.setFont(CustomUI.TABLE_FONT);
        selectLabel.setPreferredSize(new Dimension(150, 35));

        JTextField selectDirField = new JTextField("D:\\Backup");
        selectDirField.setFont(CustomUI.TABLE_FONT);
        selectDirField.setPreferredSize(new Dimension(0, 35));
        selectDirField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));


        ImageIcon folderIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/folder.png")));
        JButton browseDirBtn = createChooseFileIconButton(folderIcon,selectDirField);
        ImageIcon rfIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icons/refresh.png")));
        JButton refreshBtn = createIconButton(rfIcon);
        refreshBtn.addActionListener((e) ->{
            selectedFiles.clear();
            tableModel.setRowCount(0);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(browseDirBtn);
        buttonPanel.add(refreshBtn);

        topSection.add(selectLabel, BorderLayout.WEST);
        topSection.add(selectDirField, BorderLayout.CENTER);
        topSection.add(buttonPanel, BorderLayout.EAST);

        // Table
        String[] columns = {"Tên dữ liệu", "Ngày tạo", "Định dạng", "Kích thước", "Kiểu sao lưu"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // toàn bộ cell không editable (không có cột thao tác)
                return false;
            }
        };

        fileTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(CustomUI.TABLE_FONT);

                if (isRowSelected(row)) {
                    c.setBackground(CustomUI.ROW_SELECTED_COLOR);
                    c.setForeground(CustomUI.black);
                } else {
                    if (row % 2 == 0) {
                        c.setBackground(CustomUI.ROW_EVEN != null ? CustomUI.ROW_EVEN : Color.WHITE);
                    } else {
                        c.setBackground(CustomUI.ROW_ODD != null ? CustomUI.ROW_ODD : new Color(0xF7F9FB));
                    }
                    c.setForeground(CustomUI.black);
                }

                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                }
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, CustomUI.tableBorder));
                return c;
            }
        };

        fileTable.setRowHeight(36);
        fileTable.getTableHeader().setPreferredSize(new Dimension(fileTable.getWidth(), 36));
        fileTable.getTableHeader().setFont(CustomUI.HEADER_FONT);
        fileTable.getTableHeader().setBackground(CustomUI.blue);
        fileTable.getTableHeader().setForeground(CustomUI.white);
        fileTable.getTableHeader().setOpaque(true);

        fileTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(BorderFactory.createMatteBorder(0,0,1,1, CustomUI.tableBorder));
                comp.setFont(CustomUI.TABLE_FONT);
                return comp;
            }
        });
        fileTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        fileTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        fileTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        fileTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        fileTable.getColumnModel().getColumn(4).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(fileTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        fileTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int selectedRow = fileTable.getSelectedRow();

                // Nếu không có row nào được chọn → bỏ qua
                if (selectedRow == -1) return;

                // Nếu nhấn Backspace hoặc Delete
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {

                    // Xóa file khỏi global list (nếu lưu theo index)
                    if (selectedRow < selectedFiles.size()) {
                        selectedFiles.remove(selectedRow);
                    }

                    // Xóa row khỏi table
                    tableModel.removeRow(selectedRow);
                }
            }
        });

        // Bottom section with file count and restore button
        JPanel bottomSection = new JPanel(new BorderLayout());
        bottomSection.setOpaque(false);
        bottomSection.setBorder(new EmptyBorder(10, 0, 0, 0));

        fileCountLabel = new JLabel("Số lượng file : " +  this.selectedFiles.size());
        fileCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fileCountLabel.setForeground(new Color(100, 100, 100));

        // Restore button
        restoreBtn = new JButton("Khôi phục");
        restoreBtn.setFont(CustomUI.verySmallFont);
        restoreBtn.setPreferredSize(new Dimension(120, 38));
        restoreBtn.setBackground(new Color(66, 139, 202));
        restoreBtn.setForeground(Color.WHITE);
        restoreBtn.setFocusPainted(false);
        restoreBtn.setBorderPainted(false);
        restoreBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        restoreBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                restoreBtn.setBackground(new Color(51, 122, 183));
            }
            public void mouseExited(MouseEvent e) {
                restoreBtn.setBackground(new Color(66, 139, 202));
            }
        });

        restoreBtn.addActionListener((e) -> {
            handleRestoreBtn();
        });

        bottomSection.add(fileCountLabel, BorderLayout.WEST);
        bottomSection.add(restoreBtn, BorderLayout.EAST);

        panel.add(topSection, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomSection, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createChooseFileIconButton(ImageIcon icon, JTextField txt) {
        JButton btn = new JButton(icon);
        btn.setPreferredSize(CustomUI.BUTTON_SIZE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(CustomUI.mine);
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(230, 230, 230));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(240, 240, 240));
            }
        });

        btn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("D:/ba"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int result = chooser.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selected = chooser.getSelectedFile();
                String fileName = selected.getName();
                if(selectedFiles.isEmpty() && !fileName.contains("FULL")){
                    JOptionPane.showMessageDialog(
                            null,
                            "Vui lòng chọn 1 file backup toàn bộ (FULL) trước",
                            "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                if(selectedFiles.size() == 1 && !fileName.contains("DIF")){
                    JOptionPane.showMessageDialog(
                            null,
                            "Vui lòng chọn 1 file backup theo ngày (DIF)",
                            "Cảnh báo",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                // Giới hạn tối đa 2 file
                if (selectedFiles.size() >= 2) {
                    JOptionPane.showMessageDialog(this,
                            "Bạn chỉ có thể chọn tối đa 2 file!",
                            "Giới hạn", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                //Kiểm tra trùng file
                if (selectedFiles.stream().anyMatch(f -> f.getAbsolutePath().equals(selected.getAbsolutePath()))) {
                    JOptionPane.showMessageDialog(this,
                            "File này đã được chọn trước đó!",
                            "Trùng file", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Thêm vào danh sách global
                selectedFiles.add(selected);

                // Update lên JTextField nếu cần
                txt.setText(selected.getAbsolutePath());

                // Đẩy lên table
                tableModel.insertRow(0, getFileInfo(selected));
            }
        });

        return btn;
    }

    private Object[] getFileInfo(File file) {

        // Tên dữ liệu
        String fileName = file.getName();

        // Ngày tạo
        String createdDate = new java.text.SimpleDateFormat("HH:mm:ss dd/MM/yyyy ")
                .format(new java.util.Date(file.lastModified()));

        // Loại file
        String fileType = "Không xác định";
        if (fileName.contains(".")) {
            fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        }

        // Kích thước
        String fileSize = file.length() / 1024 + " KB";

        //Kiểu file
        String fileFormat = "Toàn bộ";
        if(fileName.contains("DIF")){
            fileFormat = "Theo ngày";
        }

        // File
        return new Object[]{file, createdDate, fileType, fileSize, fileFormat};
    }

    private JButton createIconButton(ImageIcon icon) {
        JButton btn = new JButton(icon);
        btn.setPreferredSize(CustomUI.BUTTON_SIZE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(CustomUI.mine);
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(230, 230, 230));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(240, 240, 240));
            }
        });

        return btn;
    }

    private JButton createFolderIconButton(ImageIcon icon, JTextField txt) {
        JButton btn = new JButton(icon);
        btn.setPreferredSize(CustomUI.BUTTON_SIZE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(CustomUI.mine);
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(230, 230, 230));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(240, 240, 240));
            }
        });

        btn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                txt.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        return btn;
    }

    public static void main(String[] args) {
        FlatIntelliJLaf.setup();

        JFrame f = new JFrame("Thiết lập sao lưu");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(900, 650);
        f.setLocationRelativeTo(null);
        f.setContentPane(new SystemConfigPanel());
        f.setVisible(true);
    }

    private void handleRestoreBtn() {
        // 1. Kiểm tra số lượng file
        if (selectedFiles.size() != 2) {
            JOptionPane.showMessageDialog(
                    null,
                    "Cần chọn đúng 2 file (1 FULL + 1 DIFF)!",
                    "Thiếu file",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String s0FileName = selectedFiles.getFirst().getName();
        String s1FileName = selectedFiles.get(1).getName();

        File fullFile;
        File diffFile;
        if(s0FileName.contains("FULL") && s1FileName.contains("DIF")){
            fullFile = selectedFiles.get(0);
            diffFile = selectedFiles.get(1);
        }
        else {
            fullFile = selectedFiles.get(1);
            diffFile = selectedFiles.get(0);
        }

        // 2. Kiểm tra file tồn tại
        if (!fullFile.exists() || !diffFile.exists()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Một trong hai file không tồn tại!",
                    "Lỗi file",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // 3. Xác nhận restore
        int choice = JOptionPane.showConfirmDialog(
                null,
                "Bạn có chắc chắn muốn khôi phục database?\n"
                        + "Dữ liệu hiện tại sẽ bị ghi đè hoàn toàn!",
                "Xác nhận khôi phục",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) return;

        // 4. Disable UI trong lúc restore
        restoreBtn.setEnabled(false);

        // 5. Chạy restore không block UI
        new Thread(() -> {
            JDialog loadingDialog = new JDialog();
            loadingDialog.setTitle("Khôi phục dữ liệu");
            loadingDialog.add(new JLabel("Đang xử lý khôi phục dữ liệu"));
            loadingDialog.setSize(500, 100);
            loadingDialog.setLocationRelativeTo(null);

            SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
            try {
                RestoreDataBase.restoreFullAndDiff(fullFile.getAbsolutePath(), diffFile.getAbsolutePath());

                warningReservationService.excute();
                tableModel.setRowCount(0);
                this.selectedFiles.clear();
                RefreshManager.refreshAll();

                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(
                                null,
                                "Khôi phục dữ liệu thành công!",
                                "Thành công",
                                JOptionPane.INFORMATION_MESSAGE
                        )
                );

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(
                                null,
                                "Khôi phục thất bại: " + ex.getMessage(),
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE
                        )
                );
            } finally {
                SwingUtilities.invokeLater(() -> restoreBtn.setEnabled(true));
                loadingDialog.dispose();
                restoreDirField.setText("");
            }

        }).start();
    }


}
