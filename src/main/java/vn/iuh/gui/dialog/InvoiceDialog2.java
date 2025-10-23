package vn.iuh.gui.dialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import vn.iuh.constraint.FeeValue;
import vn.iuh.constraint.PaymentMethod;
import vn.iuh.constraint.PaymentStatus;
import vn.iuh.dao.HoaDonDAO;
import vn.iuh.dto.event.create.InvoiceCreationEvent;
import vn.iuh.dto.response.InvoiceResponse;
import vn.iuh.entity.ChiTietHoaDon;
import vn.iuh.entity.PhongDungDichVu;
import vn.iuh.entity.PhongTinhPhuPhi;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.util.PriceFormat;

import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;

public class InvoiceDialog2 extends JDialog {
    private InvoiceResponse response;
    private JTable tblPhong, tblDichVu;
    private JTable tblPhuPhi;
    private JLabel lblTitle;
    private JPanel pnlTitle;
    private DefaultTableModel modelPhong;
    private JPanel pnlSouth;
    private HoaDonDAO hoaDonDAO;
    private JComboBox<String> cmbPaymentMethod;

    public InvoiceDialog2(InvoiceResponse response) {
        super((Frame) null, "Hóa đơn", true);
        this.response = response;
        getContentPane().setBackground(Color.WHITE);
        setSize(800, 700);
        setLocationRelativeTo(null);
        setModal(true);
        initComponents();
        this.hoaDonDAO = new HoaDonDAO();
    }

    private void initComponents() {
        lblTitle = new JLabel("Hóa đơn thanh toán");
        lblTitle.setFont(CustomUI.bigFont);
        pnlTitle  = new JPanel();
        pnlTitle.add(lblTitle);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 10, 5));

        JLabel lblKhachSanTitle = new JLabel("Đơn vị: ");
        lblKhachSanTitle.setFont(CustomUI.smallFont);
        JLabel lblKhachSanValue = new JLabel("Khách sạn Hai Quân Đức Thịnh");
        JPanel pnlKhachSan = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlKhachSan.add(lblKhachSanValue);
        pnlKhachSan.add(lblKhachSanValue);

        JLabel lblKhachHangTitle = new JLabel("Khách hàng: ");
        lblKhachHangTitle.setFont(CustomUI.smallFont);
        JLabel lblKhachHangValue = new JLabel(response.getKhachHang().getTenKhachHang());
        JPanel pnlKhachHang = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlKhachHang.add(lblKhachHangTitle);
        pnlKhachHang.add(lblKhachHangValue);

// --- CCCD ---
        JLabel lblCCCDTitle = new JLabel("CCCD: ");
        lblCCCDTitle.setFont(CustomUI.smallFont);
        JLabel lblCCCDValue = new JLabel(response.getKhachHang().getCCCD());
        JPanel pnlCCCD = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlCCCD.add(lblCCCDTitle);
        pnlCCCD.add(lblCCCDValue);

// --- SĐT ---
        JLabel lblSDTTitle = new JLabel("SĐT: ");
        lblSDTTitle.setFont(CustomUI.smallFont);
        JLabel lblSDTValue = new JLabel(response.getKhachHang().getSoDienThoai());
        JPanel pnlSDT = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlSDT.add(lblSDTTitle);
        pnlSDT.add(lblSDTValue);

// --- Ngày tạo ---
        JLabel lblNgayTaoTitle = new JLabel("Ngày tạo: ");
        lblNgayTaoTitle.setFont(CustomUI.smallFont);
        JLabel lblNgayTaoValue = new JLabel(new Timestamp(System.currentTimeMillis()).toString());
        JPanel pnlNgayTao = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlNgayTao.add(lblNgayTaoTitle);
        pnlNgayTao.add(lblNgayTaoValue);

// --- Nhân viên ---
        JLabel lblNhanVienTitle = new JLabel("Nhân viên: ");
        lblNhanVienTitle.setFont(CustomUI.smallFont);
        JLabel lblNhanVienValue = new JLabel(response.getTenNhanVien().getTenNhanVien());
        JPanel pnlNhanVien = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlNhanVien.add(lblNhanVienTitle);
        pnlNhanVien.add(lblNhanVienValue);

        // --- Ngày đến ---
        JLabel lblNgayDenTitle = new JLabel("Ngày đến: ");
        lblNgayDenTitle.setFont(CustomUI.smallFont);
        JLabel lblNgayDenValue = new JLabel(response.getDonDatPhong().getTgNhanPhong().toString());
        JPanel pnlNgayDen = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlNgayDen.add(lblNgayDenTitle);
        pnlNgayDen.add(lblNgayDenValue);

        // --- Ngày đi ---
        JLabel lblNgayDiTitle = new JLabel("Ngày đi: ");
        lblNgayDiTitle.setFont(CustomUI.smallFont);
        JLabel lblNgayDiValue = new JLabel(response.getDonDatPhong().getTgTraPhong().toString());
        JPanel pnlNgayDi = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlNgayDi.add(lblNgayDiTitle);
        pnlNgayDi.add(lblNgayDiValue);

        // Thêm các label vào panel

        infoPanel.add(pnlKhachSan);
        infoPanel.add(pnlKhachHang);

        infoPanel.add(pnlNgayTao);
        infoPanel.add(pnlSDT);

        infoPanel.add(pnlCCCD);
        infoPanel.add(pnlNhanVien);

        infoPanel.add(pnlNgayDen);
        infoPanel.add(pnlNgayDi);


        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(pnlTitle, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ===== Bảng chi tiết phòng =====
        String[] colPhong = {"Tên phòng", "Đơn giá" ,"Thời gian", "Thành tiền"};
        DefaultTableModel modelPhong = new DefaultTableModel(colPhong, 0);
        tblPhong = new JTable(modelPhong);
        fillRoomTable(modelPhong);

        JPanel roomPanel = new JPanel(new BorderLayout());
        roomPanel.setBorder(BorderFactory.createTitledBorder("Chi tiết phòng"));
        roomPanel.add(new JScrollPane(tblPhong), BorderLayout.CENTER);

        // ===== Bảng dịch vụ và phụ phí =====
        String[] colDichVu = {"Phòng", "Tên dịch vụ ", "Đơn giá", "Số lượng","Thành tiền", "Ghi chú"};
        DefaultTableModel modelDichVu = new DefaultTableModel(colDichVu, 0);
        tblDichVu = new JTable(modelDichVu);
        fillServiceTable(modelDichVu);

        JPanel servicePanel = new JPanel(new BorderLayout());
        servicePanel.setBorder(BorderFactory.createTitledBorder("Dịch vụ đã sử dụng"));
        servicePanel.add(new JScrollPane(tblDichVu), BorderLayout.CENTER);

        String[] colPhuPhi = {"Phòng", "Tên phụ phí", "Đơn giá"};
        DefaultTableModel modelPhuPhi = new DefaultTableModel(colPhuPhi, 0);
        tblPhuPhi = new JTable(modelPhuPhi);
        fillFeeTable(modelPhuPhi);

        JPanel feePanel = new JPanel(new BorderLayout());
        feePanel.setBorder(BorderFactory.createTitledBorder("Phụ phí phát sinh"));
        feePanel.add(new JScrollPane(tblPhuPhi), BorderLayout.CENTER);

        // ===== Tổng tiền =====
        pnlSouth = new JPanel();
        pnlSouth.setLayout(new GridLayout(5,2));

        JLabel lblTotalTitle = new JLabel("Tổng tiền: ");
        lblTotalTitle.setFont(CustomUI.smallFont);
        JLabel lblTotalValue = new JLabel(getStringPrice(response.getHoaDon().getTongTien().add(response.getTienCoc())));
        lblTotalValue.setFont(CustomUI.smallFont);

        JLabel lblDeposit = new JLabel("Tiền cọc: ");
        lblDeposit.setFont(CustomUI.smallFont);
        JLabel lblDepositValue = new JLabel("-".concat(getStringPrice(response.getTienCoc())));
        lblDepositValue.setFont(CustomUI.smallFont);

        JLabel lblRealTotal = new JLabel("Tiền phải thanh toán: ");
        lblRealTotal.setFont(CustomUI.smallFont);
        JLabel lblRealTotalValue = new JLabel(getStringPrice(response.getHoaDon().getTongTien()));
        lblRealTotalValue.setFont(CustomUI.smallFont);

// --- Thuế VAT ---
        double taxPercent = FeeValue.TAX.getValue() * 100;
        JLabel lblTaxFeeTitle = new JLabel("Thuế VAT(" + taxPercent + "%): ");
        lblTaxFeeTitle.setFont(CustomUI.smallFont);
        JLabel lblTaxFeeValue = new JLabel(getStringPrice(response.getHoaDon().getTienThue()));
        lblTaxFeeValue.setFont(CustomUI.smallFont);

// --- Tổng hóa đơn ---
        JLabel lblTotalInvoiceTitle = new JLabel("Tổng hóa đơn: ");
        lblTotalInvoiceTitle.setFont(CustomUI.smallFont);
        JLabel lblTotalInvoiceValue = new JLabel(getStringPrice(response.getHoaDon().getTongHoaDon()));
        lblTotalInvoiceValue.setFont(CustomUI.smallFont);

        //Chọn phương thức thanh toán
        cmbPaymentMethod = new JComboBox<>();
        for(PaymentMethod method : PaymentMethod.values()){
            cmbPaymentMethod.addItem(method.getMethod());
        }

        // --- Border có tiêu đề ---
        TitledBorder border = BorderFactory.createTitledBorder("Chọn phương thức thanh toán");
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 13));
        cmbPaymentMethod.setBorder(border);

        // --- Thêm vào panel ---
        add(cmbPaymentMethod);

        // --- Bắt sự kiện chọn ---
        cmbPaymentMethod.addActionListener(e -> {
            String selected = (String) cmbPaymentMethod.getSelectedItem();
        });


        // ===== Nút in hóa đơn =====
        JButton btnPrint = new JButton("In hóa đơn");
        btnPrint.setBackground(CustomUI.blue);
        btnPrint.setForeground(CustomUI.white);
        btnPrint.setFont(CustomUI.verySmallFont);
        btnPrint.addActionListener(e -> JOptionPane.showMessageDialog(this, "in hóa đơn"));

        JButton btnConfirm = new JButton("Xác nhận thanh toán");
        btnConfirm.setBackground(CustomUI.darkGreen);
        btnConfirm.setForeground(CustomUI.white);
        btnConfirm.setFont(CustomUI.verySmallFont);
        if(!PaymentStatus.UNPAID.getStatus().equalsIgnoreCase(this.response.getHoaDon().getTinhTrangThanhToan()) &&
                this.response.getHoaDon().getTinhTrangThanhToan() != null
        ){
            btnConfirm.setEnabled(false);
        }
        btnConfirm.addActionListener(e -> {
            var result = this.confirmPayment();
            if(result){
                JOptionPane.showMessageDialog(null, "Thanh toán thành công");
                dispose();
            }
            else {
                JOptionPane.showMessageDialog(null, "Thanh toán thất bại");
            }
        });

        pnlSouth.add(lblTotalTitle);
        pnlSouth.add(lblTotalValue);
        pnlSouth.add(cmbPaymentMethod);
        pnlSouth.add(lblDeposit);
        pnlSouth.add(lblDepositValue);
        pnlSouth.add(Box.createHorizontalGlue());
        pnlSouth.add(lblRealTotal);
        pnlSouth.add(lblRealTotalValue);
        pnlSouth.add(btnPrint);
        pnlSouth.add(lblTaxFeeTitle);
        pnlSouth.add(lblTaxFeeValue);
        pnlSouth.add(Box.createHorizontalGlue());
        pnlSouth.add(lblTotalInvoiceTitle);
        pnlSouth.add(lblTotalInvoiceValue);
        pnlSouth.add(btnConfirm);


        // ===== Combine center =====
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        centerPanel.add(roomPanel);
        centerPanel.add(servicePanel);
        centerPanel.add(feePanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(pnlSouth, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private String getStringPrice(BigDecimal price){
        return PriceFormat.lamTronDenHangNghin(price).toString();
    }


    private void fillRoomTable(DefaultTableModel model) {
        for (ChiTietHoaDon cthd : response.getChiTietHoaDonList()) {
            double thoiGianSuDung = cthd.getThoiGianSuDung();
            int soNgay = (int) (thoiGianSuDung / 24); // số ngày
            double phanDuSauNgay = thoiGianSuDung % 24;

            int soGio = (int) phanDuSauNgay; // số giờ
            double phanDuSauGio = (phanDuSauNgay - soGio) * 60;

            int soPhut = (int) phanDuSauGio; // số phút

            StringBuilder builder = new StringBuilder();
            if(soNgay > 0){
                builder.append(soNgay);
                builder.append(" Ngày ");
            }

            if(soGio > 0){
                builder.append(soGio);
                builder.append(" Giờ ");
            }

            if(soPhut > 0){
                builder.append(soPhut);
                builder.append(" Phút ");
            }

            model.addRow(new Object[]{
                    cthd.getTenPhong(),
                    formatCurrency(cthd.getDonGiaPhongHienTai()),
                    builder.toString(),
                    formatCurrency(cthd.getTongTien())
            });
        }
    }

    private void fillServiceTable(DefaultTableModel model) {
        if (response.getPhongDungDichVuList() != null) {
            for (PhongDungDichVu pddv : response.getPhongDungDichVuList()) {
                model.addRow(pddv.getSimpleObject());
            }
        }
    }

    private void fillFeeTable(DefaultTableModel model) {
        if (response.getPhongTinhPhuPhiList() != null) {
            for (PhongTinhPhuPhi pp : response.getPhongTinhPhuPhiList()) {
                model.addRow(new Object[]{
                        pp.getTenPhong(),
                        pp.getTenPhuPhi(),
                        formatCurrency(pp.getTongTien())
                });
            }
        }
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0 VND";
        DecimalFormat df = new DecimalFormat("#,### VND");
        return df.format(value);
    }

    private boolean confirmPayment(){
        String phuongThucThanhToan = (String) cmbPaymentMethod.getSelectedItem();
        this.response.getHoaDon().setPhuongThucThanhToan(phuongThucThanhToan);
        this.response.getHoaDon().setTinhTrangThanhToan(PaymentStatus.PAID.getStatus());
        return hoaDonDAO.updateTinhTrangThanhToan(this.response.getHoaDon());
    }
}

