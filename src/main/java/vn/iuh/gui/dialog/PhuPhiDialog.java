package vn.iuh.gui.dialog;

import com.formdev.flatlaf.FlatClientProperties;
import vn.iuh.constraint.Fee;
import vn.iuh.dao.GiaPhuPhiDAO;
import vn.iuh.dao.NhanVienDAO;
import vn.iuh.dao.PhuPhiDAO;
import vn.iuh.dto.repository.ThongTinPhuPhi;
import vn.iuh.dto.response.HistoryFeeResponse;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.Main;
import vn.iuh.util.FeeValue;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;


public class PhuPhiDialog extends JDialog {

    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 14);
    private static final Dimension FIELD_SIZE = new Dimension(400, 40);

    private ThongTinPhuPhi thongTinPhuPhi;
    private PhuPhiDAO phuPhiDAO;
    private NhanVienDAO nhanVienDAO;
    private JTextField txtMaPhuPhi;
    private JTextField txtTenPhuPhi;
    private JTextField txtDonViTinh;
    private JTextField txtGiaHienTai;
    private JButton btnLuu;
    private JButton btnHuyBo;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JPanel pnlNorth;
    private JPanel pnlTitle;
    private final GiaPhuPhiDAO giaPhuPhiDAO;

    public PhuPhiDialog(Window owner, ThongTinPhuPhi thongTinPhuPhi) {
        super(owner, "Chỉnh sửa thông tin phụ phí", ModalityType.APPLICATION_MODAL);
        this.giaPhuPhiDAO = new GiaPhuPhiDAO();
        this.thongTinPhuPhi = thongTinPhuPhi;
        this.phuPhiDAO = new PhuPhiDAO();
        this.nhanVienDAO = new NhanVienDAO();
        setSize(500, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CustomUI.white);

        initUI();
        addListeners();
        loadData();
        loadHistoryData();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);

        pnlNorth = new JPanel();
        pnlNorth.setBackground(CustomUI.blue);
        pnlNorth.setLayout(new BoxLayout(pnlNorth, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel(getTitle(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setOpaque(true);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBackground(CustomUI.blue);
        lblTitle.setPreferredSize(new Dimension(0, 50));
        pnlNorth.add(lblTitle);


        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CustomUI.white);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblMaPhuPhi = createLabel("Mã phụ phí");
        JLabel lblTenPhuPhi = createLabel("Tên phụ phí");
        JLabel lblDonViTinh = createLabel("Đơn vị tính");
        JLabel lblGiaHienTai = createLabel("Giá hiện tại");

        txtMaPhuPhi = createTextField(false);
        txtTenPhuPhi = createTextField(false);
        txtDonViTinh = createTextField(false);
        txtGiaHienTai = createTextField(true);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(lblMaPhuPhi, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(txtMaPhuPhi, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formPanel.add(lblTenPhuPhi, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(txtTenPhuPhi, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(lblDonViTinh, gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(txtDonViTinh, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(lblGiaHienTai, gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(txtGiaHienTai, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setOpaque(false);


        btnLuu = styleButton("Lưu", CustomUI.darkGreen);
        btnHuyBo = styleButton("Hủy bỏ", CustomUI.red);

        buttonPanel.add(btnLuu);
        buttonPanel.add(btnHuyBo);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(buttonPanel, gbc);
        formPanel.setBackground(CustomUI.white);

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setOpaque(false);
        historyPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel lblHistoryTitle = new JLabel("Lịch sử thay đổi giá", SwingConstants.CENTER);
        lblHistoryTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblHistoryTitle.setForeground(Color.WHITE);
        lblHistoryTitle.setOpaque(true);
        lblHistoryTitle.setBackground(new Color(0, 100, 0));
        lblHistoryTitle.setPreferredSize(new Dimension(0, 40));
        historyPanel.add(lblHistoryTitle, BorderLayout.NORTH);

        String[] historyColumns = {"Ngày chỉnh sửa", "Đơn giá","Nhân viên" ,"Mã nhân viên"};
        historyTableModel = new DefaultTableModel(historyColumns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        historyTable = new JTable(historyTableModel);

        historyTable.setRowHeight(25);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 14));
        historyTable.setGridColor(Color.LIGHT_GRAY);
        JTableHeader header = historyTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setBackground(CustomUI.blue);
        header.setForeground(Color.WHITE);

        JScrollPane historyScrollPane = new JScrollPane(historyTable);
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);

        pnlNorth.add(formPanel);
        mainPanel.add(pnlNorth, BorderLayout.NORTH);
        mainPanel.add(historyPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(Color.GRAY);
        return label;
    }

    private JTextField createTextField(boolean editable) {
        JTextField textField = new JTextField(20);
        textField.setFont(FONT_LABEL);
        textField.setPreferredSize(FIELD_SIZE);
        textField.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        if (editable) {
            textField.setEditable(true);
        } else {
            textField.setFocusable(false);
            textField.setEditable(false);
        }
        return textField;
    }

    private JButton styleButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        button.setFont(FONT_LABEL);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return button;
    }

    private void loadData() {
        txtMaPhuPhi.setText(thongTinPhuPhi.getMaPhuPhi());
        txtTenPhuPhi.setText(thongTinPhuPhi.getTenPhuPhi());
        txtGiaHienTai.setText(String.format("%.0f", thongTinPhuPhi.getGiaHienTai().doubleValue()));

        try {
            txtDonViTinh.setText(thongTinPhuPhi.isLaPhanTram() ? "%" : "VND");
        } catch (Exception e) {
            txtDonViTinh.setText("N/A");
            e.printStackTrace();
        }
    }

    private void loadHistoryData() {
        historyTableModel.setRowCount(0);
        try {
            List<HistoryFeeResponse> danhSachThayDoi = giaPhuPhiDAO.getHistoryByMaPhuPhi(thongTinPhuPhi.getMaPhuPhi());
            for(HistoryFeeResponse tp : danhSachThayDoi){
                historyTableModel.addRow(tp.getObject());
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải lịch sử giá", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void addListeners() {
        btnHuyBo.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> saveChanges());
    }

    private void saveChanges() {
        String giaMoiStr = txtGiaHienTai.getText().trim();
        double giaMoi;
        try {
            giaMoi = Double.parseDouble(giaMoiStr);
            // validate input
            if (giaMoi < 0) {
                throw new NumberFormatException();
            }
            else if(giaMoi > 1000 && this.thongTinPhuPhi.isLaPhanTram()){
                throw new IllegalArgumentException("Phụ phí theo phần trăm không được lớn hơn 1000");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá hiện tại không hợp lệ. Vui lòng nhập một số dương.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IllegalArgumentException e){
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn cập nhật giá cho '" + thongTinPhuPhi.getTenPhuPhi() + "' không?",
                "Xác nhận cập nhật",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            switch (thongTinPhuPhi.getTenPhuPhi().trim()) {
                case "Check-out trễ" -> FeeValue.getInstance().updateFee(Fee.CHECK_OUT_TRE, giaMoi);
                case "Check-in sớm" -> FeeValue.getInstance().updateFee(Fee.CHECK_IN_SOM, giaMoi);
                case "Thuế giá trị gia tăng" -> FeeValue.getInstance().updateFee(Fee.THUE, giaMoi);
                case "Đổi phòng" -> FeeValue.getInstance().updateFee(Fee.DOI_PHONG, giaMoi);
                default -> throw new IllegalArgumentException("Không xác định được loại phụ phí.");
            }
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}



