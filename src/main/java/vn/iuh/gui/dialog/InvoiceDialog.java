package vn.iuh.gui.dialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import vn.iuh.constraint.FeeValue;
import vn.iuh.dto.event.create.InvoiceCreationEvent;
import vn.iuh.entity.ChiTietHoaDon;
import vn.iuh.entity.PhongDungDichVu;
import vn.iuh.entity.PhongTinhPhuPhi;
import vn.iuh.gui.base.CustomUI;

import javax.swing.border.EmptyBorder;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;

public class InvoiceDialog extends JDialog {
    private InvoiceCreationEvent invoiceData;
    private JTable tblPhong, tblDichVu;
    private JLabel lblTotal;
    private JTable tblPhuPhi;
    private JLabel lblTitle;
    private JPanel pnlTitle;
    private JLabel lblTaxFee;
    private JLabel lblTotalInvoice;

    public InvoiceDialog(InvoiceCreationEvent invoiceCreationEvent) {
        this.invoiceData = invoiceCreationEvent;
        setSize(800, 700);
        setLocationRelativeTo(null);
        setModal(true);
        initComponents();
    }

    private void initComponents() {
        lblTitle = new JLabel("Hóa đơn thanh toán");
        lblTitle.setFont(CustomUI.bigFont);
        pnlTitle  = new JPanel();
        pnlTitle.add(lblTitle);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 10, 5));

        infoPanel.add(new JLabel("Khách hàng: "));
        infoPanel.add(new JLabel(invoiceData.getKhachHang().getTenKhachHang()));

        infoPanel.add(new JLabel("CCCD: "));
        infoPanel.add(new JLabel(invoiceData.getKhachHang().getCCCD()));

        infoPanel.add(new JLabel("SĐT: "));
        infoPanel.add(new JLabel(invoiceData.getKhachHang().getSoDienThoai()));

        infoPanel.add(new JLabel("Ngày tạo: "));
        infoPanel.add(new JLabel(new Timestamp(System.currentTimeMillis()).toString()));

        infoPanel.add(new JLabel("Nhân viên: "));
        infoPanel.add(new JLabel(invoiceData.getNhanVien().getTenNhanVien()));


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
        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.Y_AXIS));
        lblTotal = new JLabel("Tổng tiền: " + formatCurrency(invoiceData.getHoaDon().getTongTien()));
        lblTaxFee = new JLabel("Thuế VAT(" + FeeValue.TAX + "%): " + formatCurrency(invoiceData.getHoaDon().getTienThue()));
        lblTotalInvoice = new  JLabel("Tổng hóa đơn: " +  formatCurrency(invoiceData.getHoaDon().getTongHoaDon()));
        lblTotal.setFont(CustomUI.normalFont);
        lblTaxFee.setFont(CustomUI.normalFont);
        lblTotalInvoice.setFont(CustomUI.normalFont);
        totalPanel.add(lblTotal);
        totalPanel.add(lblTaxFee);
        totalPanel.add(lblTotalInvoice);


//        // ===== Nút in hóa đơn =====
//        JButton btnPrint = new JButton("In hóa đơn");
//        btnPrint.addActionListener(e -> JOptionPane.showMessageDialog(this, "in hóa đơn"));
//        totalPanel.add(btnPrint);

        // ===== Combine center =====
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        centerPanel.add(roomPanel);
        centerPanel.add(servicePanel);
        centerPanel.add(feePanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(totalPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void fillRoomTable(DefaultTableModel model) {
        for (ChiTietHoaDon cthd : invoiceData.getChiTietHoaDonList()) {
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
        if (invoiceData.getPhongDungDichVuList() != null) {
            for (PhongDungDichVu pddv : invoiceData.getPhongDungDichVuList()) {
                model.addRow(pddv.getSimpleObject());
            }
        }
    }

    private void fillFeeTable(DefaultTableModel model) {
        if (invoiceData.getPhongTinhPhuPhiList() != null) {
            for (PhongTinhPhuPhi pp : invoiceData.getPhongTinhPhuPhiList()) {
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

}

