package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatLineBorder;
import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.NhanVienDAO;
import vn.iuh.entity.NhanVien;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.dialog.EmployeeDialog;
import vn.iuh.service.EmployeeService;
import vn.iuh.service.impl.NhanVienServiceImpl;
import vn.iuh.util.EntityUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class QuanLyNhanVienPanel extends JPanel {
    private static final int SEARCH_CONTROL_HEIGHT = 40; // chiều cao cố định cho combo, textfield, button
    private static final Dimension SEARCH_TEXT_SIZE = new Dimension(520, SEARCH_CONTROL_HEIGHT);

    private static final int SEARCH_TYPE_WIDTH = 180;
    private static final int SEARCH_BUTTON_WIDTH = 110;
    private static final Dimension SEARCH_BUTTON_SIZE = new Dimension(SEARCH_BUTTON_WIDTH, SEARCH_CONTROL_HEIGHT);
    private static final Dimension ACTION_BUTTON_SIZE = new Dimension(220, 50);
    private static final int TOP_PANEL_HEIGHT = 40;

    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 14);
    private static final Font FONT_ACTION = new Font("Arial", Font.BOLD, 18);
    private static final Font TABLE_FONT = FONT_LABEL;
    private static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 15);
    private static final Color ROW_ALT_COLOR = new Color(250, 247, 249);
    private static final Color ROW_SELECTED_COLOR = new Color(210, 230, 255);
    private final JTextField searchTextField = new JTextField();
    private final JButton searchButton = new JButton("TÌM");
    private JButton addButton;
    private JButton deleteButton;
    private JButton editButton;
    private JButton refreshButton; // Nút mới

    private JTable table;
    private DefaultTableModel tableModel;
    private NhanVienDAO nhanVienDao; // DAO
    private EmployeeService employeeService;
    private NhanVien nhanVien;
    private JComboBox<String> searchTypeComboBox;
    private JTextField nameField;
    private JTextField idField;
    private JTextField phoneField;

    public QuanLyNhanVienPanel() {
        this.nhanVienDao = new NhanVienDAO();
        employeeService = new NhanVienServiceImpl();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(CustomUI.white);
        init();
    }

    private void init() {
        initButtons();
        createTopPanel();
        add(Box.createVerticalStrut(10));
        createSearchAndActionPanel();
        add(Box.createVerticalStrut(10));
        createListNhanVienPanel();
    }

    private void initButtons() {
        configureSearchTextField(searchTextField, SEARCH_TEXT_SIZE, "Tên nhân viên");
        configureSearchButton(searchButton, SEARCH_BUTTON_SIZE);

        addButton = createActionButton("Thêm nhân viên" , ACTION_BUTTON_SIZE, "#16A34A", "#86EFAC");
        editButton = createActionButton("Sửa nhân viên", ACTION_BUTTON_SIZE, "#2563EB", "#93C5FD");

        deleteButton = createActionButton("Xóa nhân viên",  ACTION_BUTTON_SIZE, "#DC2626", "#FCA5A5");

        refreshButton = createActionButton("Làm mới",  ACTION_BUTTON_SIZE, "#0891B2", "#67E8F9"); // Màu Cyan

        refreshButton.addActionListener(e -> {
            loadDataToTable();
        });

        addButton.addActionListener(e -> {

            NhanVien nvMoiNhat = nhanVienDao.timNhanVienMoiNhat();
            String maNhanVienMoiNhat = (nvMoiNhat == null) ? null : nvMoiNhat.getMaNhanVien();

            String newMaNhanVien = EntityUtil.increaseEntityID(maNhanVienMoiNhat,
                    EntityIDSymbol.EMPLOYEE_PREFIX.getPrefix(),
                    EntityIDSymbol.EMPLOYEE_PREFIX.getLength());

            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);

            EmployeeDialog addDialog = new EmployeeDialog(owner, "Thêm nhân viên mới", newMaNhanVien);
            addDialog.setVisible(true);

            if (addDialog.isSaved()) {
                NhanVien newEmployee = addDialog.getNhanVien();

                if (employeeService.createEmployee(newEmployee) != null) {
                    JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công.");
                    loadDataToTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm thất bại .", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên để sửa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String employeeId = (String) tableModel.getValueAt(selectedRow, 0);

            NhanVien existingEmployee = employeeService.getEmployeeByID(employeeId);
            if (existingEmployee == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên với mã " + employeeId, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);

            EmployeeDialog editDialog = new EmployeeDialog(owner, "Cập nhật thông tin nhân viên", existingEmployee);
            editDialog.setVisible(true); 

            if (editDialog.isSaved()) {
                NhanVien updatedEmployee = editDialog.getNhanVien();

                if (employeeService.updateEmployee(updatedEmployee) != null) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công.");
                    loadDataToTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một nhân viên để xóa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String employeeId = (String) tableModel.getValueAt(selectedRow, 0);
            String employeeName = (String) tableModel.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa nhân viên '" + employeeName + "' (Mã: " + employeeId + ") không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = employeeService.deleteEmployeeByID(employeeId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công.");
                    loadDataToTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa nhân viên thất bại .", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        searchButton.addActionListener(e -> {handleSearch();});
    }

    private void configureSearchTextField(JTextField field, Dimension size, String placeholder) {
        field.setPreferredSize(size);
        field.setMinimumSize(new Dimension(120, size.height));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));
        field.setFont(FONT_LABEL);
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        field.setForeground(Color.GRAY);
        field.setText(placeholder);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (Objects.equals(field.getText(), placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
        field.setAlignmentY(Component.CENTER_ALIGNMENT);
    }

    private void configureSearchButton(JButton btn, Dimension size) {
        btn.setPreferredSize(size);
        btn.setMinimumSize(size);
        btn.setMaximumSize(size);
        btn.setForeground(CustomUI.white);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(Color.decode("#1D4ED8"));
        btn.setMargin(new Insets(6, 10, 6, 10));
        btn.setAlignmentY(Component.CENTER_ALIGNMENT);
    }

    private JButton createActionButton(String text, Dimension size, String bgHex, String borderHex) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setFont(FONT_ACTION);
        button.setBackground(Color.decode(bgHex));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 18; borderWidth: 2; borderColor:" + borderHex);
        return button;
    }

    private void createTopPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTop = new JLabel("Quản lý nhân viên", SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(CustomUI.normalFont != null ? CustomUI.normalFont.deriveFont(Font.BOLD, 18f) : new Font("Arial", Font.BOLD, 18));
        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop, BorderLayout.CENTER);
        pnlTop.setPreferredSize(new Dimension(0, TOP_PANEL_HEIGHT));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, TOP_PANEL_HEIGHT));
        add(pnlTop);
    }

    private void createSearchAndActionPanel() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.setBackground(CustomUI.white);

        JPanel searchPanel = createSearchPanel();
        searchPanel.setPreferredSize(new Dimension(0, 180));
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        container.add(searchPanel);
        add(container);
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBackground(CustomUI.white);
        searchPanel.setBorder(new FlatLineBorder(new Insets(12, 12, 12, 12), Color.decode("#CED4DA"), 2, 25));

        String[] searchOptions = {"Tên nhân viên", "Mã nhân viên", "Số điện thoại"};
        searchTypeComboBox = new JComboBox<>(searchOptions);
        Dimension comboSize = new Dimension(SEARCH_TYPE_WIDTH, SEARCH_CONTROL_HEIGHT);
        searchTypeComboBox.setPreferredSize(comboSize);
        searchTypeComboBox.setMinimumSize(comboSize);
        searchTypeComboBox.setMaximumSize(comboSize);
        searchTypeComboBox.setFont(FONT_LABEL);
        searchTypeComboBox.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel inputPanel = new JPanel(new CardLayout());
        inputPanel.setBackground(CustomUI.white);
        Dimension inputPreferred = new Dimension(520, SEARCH_CONTROL_HEIGHT);
        inputPanel.setPreferredSize(inputPreferred);
        inputPanel.setMinimumSize(new Dimension(200, SEARCH_CONTROL_HEIGHT));
        inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, SEARCH_CONTROL_HEIGHT));
        inputPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        nameField = new JTextField();
        configureSearchTextField(nameField, new Dimension(520, SEARCH_CONTROL_HEIGHT), "Tên nhân viên");

        idField = new JTextField();
        configureSearchTextField(idField, new Dimension(520, SEARCH_CONTROL_HEIGHT), "Mã nhân viên");

        phoneField = new JTextField();
        configureSearchTextField(phoneField, new Dimension(520, SEARCH_CONTROL_HEIGHT), "Số điện thoại");

        inputPanel.add(nameField, "Tên nhân viên");
        inputPanel.add(idField, "Mã nhân viên");
        inputPanel.add(phoneField, "Số điện thoại");

        searchTypeComboBox.addActionListener(e -> {
            CardLayout cl = (CardLayout) inputPanel.getLayout();
            cl.show(inputPanel, (String) searchTypeComboBox.getSelectedItem());
        });

        searchButton.setPreferredSize(SEARCH_BUTTON_SIZE);
        searchButton.setMinimumSize(SEARCH_BUTTON_SIZE);
        searchButton.setMaximumSize(SEARCH_BUTTON_SIZE);
        searchButton.setAlignmentY(Component.CENTER_ALIGNMENT);

        searchButton.addActionListener(e -> {
            handleSearch();
        });

        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.setBackground(CustomUI.white);
        row1.setAlignmentY(Component.CENTER_ALIGNMENT);

        searchButton.setPreferredSize(SEARCH_BUTTON_SIZE);
        searchButton.setMinimumSize(SEARCH_BUTTON_SIZE);
        searchButton.setMaximumSize(SEARCH_BUTTON_SIZE);
        searchButton.setAlignmentY(Component.CENTER_ALIGNMENT);

        row1.add(searchTypeComboBox);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(inputPanel);
        row1.add(Box.createHorizontalStrut(12));
        row1.add(searchButton);

        searchPanel.add(Box.createVerticalStrut(8));
        searchPanel.add(row1);
        searchPanel.add(Box.createVerticalStrut(14));

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        row2.setBackground(CustomUI.white);

        addButton.setPreferredSize(ACTION_BUTTON_SIZE);
        editButton.setPreferredSize(ACTION_BUTTON_SIZE);
        refreshButton.setPreferredSize(ACTION_BUTTON_SIZE);
        deleteButton.setPreferredSize(ACTION_BUTTON_SIZE);

        row2.add(addButton);
        row2.add(editButton);
        row2.add(refreshButton);
        row2.add(deleteButton);

        searchPanel.add(row2);
        searchPanel.add(Box.createVerticalStrut(8));
        return searchPanel;
    }

    private void loadDataToTable() {
        tableModel.setRowCount(0);

        List<NhanVien> dsNhanVien;
        try {
            dsNhanVien = nhanVienDao.layDanhSachNhanVien();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (NhanVien nv : dsNhanVien) {
            Object[] row = {
                    nv.getMaNhanVien(),
                    nv.getTenNhanVien(),
                    nv.getCCCD(),
                    nv.getNgaySinh(),
                    nv.getSoDienThoai()
            };
            tableModel.addRow(row);
        }
    }

    private void handleSearch() {
        String selectedType = (String) searchTypeComboBox.getSelectedItem();
        List<NhanVien> dsNhanVien = new ArrayList<>();
        String searchText = "";

        try {
            if ("Tên nhân viên".equals(selectedType)) {
                searchText = nameField.getForeground().equals(Color.GRAY) ? "" : nameField.getText().trim();
                dsNhanVien = employeeService.getEmployeeByName(searchText);
            } else if ("Mã nhân viên".equals(selectedType)) {
                searchText = idField.getForeground().equals(Color.GRAY) ? "" : idField.getText().trim();
                if (searchText.isEmpty()) {
                    dsNhanVien = employeeService.getAllEmployee();
                } else {
                    NhanVien nv = employeeService.getEmployeeByID(searchText);
                    if (nv != null) dsNhanVien.add(nv);
                }
            } else if ("Số điện thoại".equals(selectedType)) {
                searchText = phoneField.getForeground().equals(Color.GRAY) ? "" : phoneField.getText().trim();
                NhanVien nv = employeeService.getEmployeeBySDT(searchText);
                if(nv != null)  dsNhanVien.add(nv);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);

        if (dsNhanVien.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên nào phù hợp.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Luôn hiển thị 5 cột này
            for (NhanVien nv : dsNhanVien) {
                tableModel.addRow(new Object[]{
                        nv.getMaNhanVien(),
                        nv.getTenNhanVien(),
                        nv.getCCCD(),
                        nv.getNgaySinh(),
                        nv.getSoDienThoai()
                });
            }
        }
    }

    private void createListNhanVienPanel() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(CustomUI.white);
        wrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 2, true),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 750));
        wrap.setPreferredSize(new Dimension(0, 750));

        String[] columns = {"Mã nhân viên", "Tên nhân viên", "CCCD", "Ngày sinh", "Điện thoại" };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                c.setFont(TABLE_FONT);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT_COLOR);
                } else {
                    c.setBackground(ROW_SELECTED_COLOR);
                }
                return c;
            }
        };

        table.setRowHeight(48);
        table.setFont(TABLE_FONT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));
        header.setBackground(CustomUI.blue);
        header.setForeground(CustomUI.white);
        header.setFont(HEADER_FONT);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        table.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);


        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CustomUI.white);

        wrap.add(scrollPane, BorderLayout.CENTER);
        this.add(wrap);

        loadDataToTable();
    }
}
