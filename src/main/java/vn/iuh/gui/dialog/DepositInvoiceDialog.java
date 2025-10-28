package vn.iuh.gui.dialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Map;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import net.miginfocom.swing.MigLayout;
import vn.iuh.constraint.Fee;
import vn.iuh.constraint.PaymentMethod;
import vn.iuh.constraint.PaymentStatus;
import vn.iuh.dao.HoaDonDAO;
import vn.iuh.dao.LoaiPhongDAO;
import vn.iuh.dto.repository.ThongTinPhuPhi;
import vn.iuh.dto.response.DepositInvoiceResponse;
import vn.iuh.entity.ChiTietDatPhong;
import vn.iuh.entity.ChiTietHoaDon;
import vn.iuh.entity.PhongDungDichVu;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.service.LoaiPhongService;
import vn.iuh.service.impl.LoaiPhongServiceImpl;
import vn.iuh.util.PriceFormat;
import vn.iuh.util.TimeFormat;

public class DepositInvoiceDialog extends JDialog {
    private DepositInvoiceResponse response;
    private JTable tblPhong, tblDichVu;
    private JLabel lblTitle;
    private JPanel pnlTitle;
    private DefaultTableModel modelPhong;
    private JPanel pnlSouth;
    private HoaDonDAO hoaDonDAO;
    private JComboBox<String> cmbPaymentMethod;

    public DepositInvoiceDialog(DepositInvoiceResponse response) {
        super((Frame) null, "Hóa đơn đặt cọc", true);
        this.response = response;
        getContentPane().setBackground(Color.WHITE);
        setSize(800, 700);
        setLocationRelativeTo(null);
        setModal(true);
        initComponents();
        this.hoaDonDAO = new HoaDonDAO();
    }

    private void initComponents() {
        lblTitle = new JLabel("Hóa đơn đặt cọc");
        lblTitle.setFont(CustomUI.bigFont);
        pnlTitle = new JPanel();
        pnlTitle.add(lblTitle);
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        JPanel infoPanel = new JPanel(new GridLayout(6, 2, 10, 5));

        JLabel lblKhachSanTitle = new JLabel("Đơn vị: ");
        lblKhachSanTitle.setFont(CustomUI.smallFont);
        JLabel lblKhachSanValue = new JLabel("Khách sạn Hai Quân Đức Thịnh");
        JPanel pnlKhachSan = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlKhachSan.add(lblKhachSanTitle);
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
        JLabel lblNgayTaoValue = new JLabel(TimeFormat.formatTime(new Timestamp(System.currentTimeMillis())));
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
        JLabel lblNgayDenValue = new JLabel(TimeFormat.formatTime(response.getDonDatPhong().getTgNhanPhong()));
        JPanel pnlNgayDen = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlNgayDen.add(lblNgayDenTitle);
        pnlNgayDen.add(lblNgayDenValue);

        // --- Ngày đi ---
        JLabel lblNgayDiTitle = new JLabel("Ngày đi: ");
        lblNgayDiTitle.setFont(CustomUI.smallFont);
        JLabel lblNgayDiValue = new JLabel(TimeFormat.formatTime(response.getDonDatPhong().getTgTraPhong()));
        JPanel pnlNgayDi = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlNgayDi.add(lblNgayDiTitle);
        pnlNgayDi.add(lblNgayDiValue);

        // --- Tình trạng thanh toán ---
        JLabel lblTinhTrang = new JLabel("Tình trạng thanh toán: ");
        lblTinhTrang.setFont(CustomUI.smallFont);
        JLabel lblTinhTrangValue = new JLabel(response.getHoaDon().getTinhTrangThanhToan());
        JPanel pnlTinhTrang = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTinhTrang.add(lblTinhTrang);
        pnlTinhTrang.add(lblTinhTrangValue);

        // Thêm các label vào panel
        infoPanel.add(pnlKhachSan);
        infoPanel.add(pnlKhachHang);
        infoPanel.add(pnlNgayTao);
        infoPanel.add(pnlSDT);
        infoPanel.add(pnlNhanVien);
        infoPanel.add(pnlCCCD);
        infoPanel.add(pnlNgayDen);
        infoPanel.add(pnlNgayDi);
        infoPanel.add(pnlTinhTrang);

        if (PaymentStatus.PAID.getStatus().equalsIgnoreCase(response.getHoaDon().getTinhTrangThanhToan())) {
            // --- Phương thức thanh toán ---
            JLabel lblPhuongThuc = new JLabel("Phương thức thanh toán: ");
            lblPhuongThuc.setFont(CustomUI.smallFont);
            JLabel lblPhuongThucValue = new JLabel(response.getHoaDon().getPhuongThucThanhToan());
            JPanel pnlPhuongThuc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            pnlPhuongThuc.add(lblPhuongThuc);
            pnlPhuongThuc.add(lblPhuongThucValue);
            infoPanel.add(pnlPhuongThuc);
        }

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(pnlTitle, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ===== Bảng chi tiết phòng =====
        String[] colPhong = {"Tên phòng", "Đơn giá", "Thời gian", "Thành tiền"};
        DefaultTableModel modelPhong = new DefaultTableModel(colPhong, 0);
        tblPhong = new JTable(modelPhong);
        fillRoomTable(modelPhong);

        JPanel roomPanel = new JPanel(new BorderLayout());
        roomPanel.setBorder(BorderFactory.createTitledBorder("Chi tiết phòng"));
        roomPanel.add(new JScrollPane(tblPhong), BorderLayout.CENTER);

        // ===== Bảng dịch vụ =====
        String[] colDichVu = {"Phòng", "Tên dịch vụ ", "Đơn giá", "Số lượng", "Thành tiền", "Ghi chú"};
        DefaultTableModel modelDichVu = new DefaultTableModel(colDichVu, 0);
        tblDichVu = new JTable(modelDichVu);
        fillServiceTable(modelDichVu);

        JPanel servicePanel = new JPanel(new BorderLayout());
        servicePanel.setBorder(BorderFactory.createTitledBorder("Dịch vụ đã sử dụng"));
        servicePanel.add(new JScrollPane(tblDichVu), BorderLayout.CENTER);

        // ===== Tổng tiền (rút gọn) =====
        // ===== Tổng tiền (rút gọn) =====
        pnlSouth = new JPanel();
        pnlSouth.setLayout(new MigLayout(
                "wrap 3, insets 10",
                "[grow,fill][grow,fill][grow,fill]",
                "[][][][]"
        ));

        JLabel lblTotalTitle = new JLabel("Tổng tiền dự tính: ");
        lblTotalTitle.setFont(CustomUI.smallFont);
        JLabel lblTotalValue = new JLabel(formatCurrency(response.getHoaDon().getTongTien()));
        lblTotalValue.setFont(CustomUI.smallFont);

        JLabel lblDeposit = new JLabel("Tiền cọc: ");
        lblDeposit.setFont(CustomUI.smallFont);
        JLabel lblDepositValue = new JLabel(formatCurrency(BigDecimal.valueOf(response.getDonDatPhong().getTienDatCoc())));
        lblDepositValue.setFont(CustomUI.smallFont);

// --- Thuế VAT ---
        double taxPercent = getTaxPrice();
        JLabel lblTaxFeeTitle = new JLabel("Thuế VAT(" + taxPercent + "%): ");
        lblTaxFeeTitle.setFont(CustomUI.smallFont);
        JLabel lblTaxFeeValue = new JLabel(formatCurrency(response.getHoaDon().getTienThue()));
        lblTaxFeeValue.setFont(CustomUI.smallFont);

// --- Tổng hóa đơn ---
        JLabel lblTotalInvoiceTitle = new JLabel("Tổng hóa đơn sau thuế: ");
        lblTotalInvoiceTitle.setFont(CustomUI.smallFont);
        JLabel lblTotalInvoiceValue = new JLabel(formatCurrency(response.getHoaDon().getTongHoaDon()));
        lblTotalInvoiceValue.setFont(CustomUI.smallFont);

// Chọn phương thức thanh toán
        cmbPaymentMethod = new JComboBox<>();
        for (PaymentMethod method : PaymentMethod.values()) {
            cmbPaymentMethod.addItem(method.getMethod());
        }

// --- Border có tiêu đề ---
        TitledBorder border = BorderFactory.createTitledBorder("Chọn phương thức thanh toán");
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 13));
        cmbPaymentMethod.setBorder(border);

        if (PaymentStatus.PAID.getStatus().equalsIgnoreCase(response.getHoaDon().getTinhTrangThanhToan())) {
            cmbPaymentMethod.setSelectedItem(response.getHoaDon().getPhuongThucThanhToan());
            cmbPaymentMethod.setEnabled(false);
            cmbPaymentMethod.setRequestFocusEnabled(false);
        }

// ===== Nút in hóa đơn =====
        JButton btnPrint = new JButton("In hóa đơn");
        btnPrint.setBackground(CustomUI.blue);
        btnPrint.setForeground(CustomUI.white);
        btnPrint.setFont(CustomUI.verySmallFont);
        btnPrint.addActionListener(e -> exportInvoiceToPDF());

        JButton btnConfirm = new JButton("Xác nhận thanh toán");
        btnConfirm.setBackground(CustomUI.darkGreen);
        btnConfirm.setForeground(CustomUI.white);
        btnConfirm.setFont(CustomUI.verySmallFont);
        if (!PaymentStatus.UNPAID.getStatus().equalsIgnoreCase(this.response.getHoaDon().getTinhTrangThanhToan()) &&
                this.response.getHoaDon().getTinhTrangThanhToan() != null) {
            btnConfirm.setEnabled(false);
        }
        btnConfirm.addActionListener(e -> {
            var result = this.confirmPayment();
            if (result) {
                JOptionPane.showMessageDialog(null, "Thanh toán thành công");
                dispose();
            } else {
                JOptionPane.showMessageDialog(null, "Thanh toán thất bại");
            }
        });

        pnlSouth.add(lblTotalTitle, "cell 0 0");
        pnlSouth.add(lblTotalValue, "cell 1 0");
        pnlSouth.add(cmbPaymentMethod, "cell 2 0 1 2, growx, aligny top");
        pnlSouth.add(lblDeposit, "cell 0 1");
        pnlSouth.add(lblDepositValue, "cell 1 1");
        pnlSouth.add(lblTaxFeeTitle, "cell 0 2");
        pnlSouth.add(lblTaxFeeValue, "cell 1 2");
        pnlSouth.add(btnPrint, "cell 2 2, growx, height 40!");
        pnlSouth.add(lblTotalInvoiceTitle, "cell 0 3");
        pnlSouth.add(lblTotalInvoiceValue, "cell 1 3");
        pnlSouth.add(btnConfirm, "cell 2 3, growx, height 40!");

        // ===== Combine center (không có phụ phí) =====
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        centerPanel.add(roomPanel);
        centerPanel.add(servicePanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(pnlSouth, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void fillRoomTable(DefaultTableModel model) {
        System.out.println("HD: " + response.getDanhSachChiTietHoaDon());;
        for (ChiTietHoaDon cthd : response.getDanhSachChiTietHoaDon()) {
            model.addRow(
                    new Object[]{
                            cthd.getTenPhong(),
                            formatCurrency(cthd.getDonGiaPhongHienTai()),
                            getStringThoiGianSuDung(cthd.getThoiGianSuDung()),
                            formatCurrency(cthd.getTongTien())
                    }
            );
        }
    }

    private String getStringThoiGianSuDung(double thoiGianSuDung) {
        int soNgay = (int) (thoiGianSuDung / 24);
        double phanDuSauNgay = thoiGianSuDung % 24;

        int soGio = (int) phanDuSauNgay;
        double phanDuSauGio = (phanDuSauNgay - soGio) * 60;

        int soPhut = (int) phanDuSauGio;

        StringBuilder builder = new StringBuilder();
        if (soNgay > 0) {
            builder.append(soNgay);
            builder.append(" Ngày ");
        }

        if (soGio > 0) {
            builder.append(soGio);
            builder.append(" Giờ ");
        }

        if (soPhut > 0) {
            builder.append(soPhut);
            builder.append(" Phút ");
        }

        return builder.toString();
    }

    private void fillServiceTable(DefaultTableModel model) {
        if (response.getDanhSachDichVu() != null) {
            for (PhongDungDichVu pddv : response.getDanhSachDichVu()) {
                model.addRow(pddv.getSimpleObject());
            }
        }
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0 VND";
        value = PriceFormat.lamTronDenHangNghin(value);
        DecimalFormat df = new DecimalFormat("#,### VND");
        return df.format(value);
    }

    private boolean confirmPayment() {
        String phuongThucThanhToan = (String) cmbPaymentMethod.getSelectedItem();
        this.response.getHoaDon().setPhuongThucThanhToan(phuongThucThanhToan);
        this.response.getHoaDon().setTinhTrangThanhToan(PaymentStatus.PAID.getStatus());
        return hoaDonDAO.updateTinhTrangThanhToan(this.response.getHoaDon());
    }


    //hàm này để in hóa đơn đặt cọc pdf
    private void exportInvoiceToPDF() {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            // ===== Đường dẫn file =====
            String filePath = System.getProperty("user.home") + "\\Documents\\HoaDonDatCoc_"
                    + response.getHoaDon().getMaHoaDon() + ".pdf";

            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // ===== Font Unicode (tiếng Việt) =====
            String fontPath = "C:\\Windows\\Fonts\\arial.ttf";
            BaseFont itextBaseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            com.itextpdf.text.Font itextTitleFont =
                    new com.itextpdf.text.Font(itextBaseFont, 18, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font itextBoldFont =
                    new com.itextpdf.text.Font(itextBaseFont, 12, com.itextpdf.text.Font.BOLD, BaseColor.BLACK);
            com.itextpdf.text.Font itextNormalFont =
                    new com.itextpdf.text.Font(itextBaseFont, 12, com.itextpdf.text.Font.NORMAL, BaseColor.BLACK);
            com.itextpdf.text.Font itextSmallGrayFont =
                    new com.itextpdf.text.Font(itextBaseFont, 10, com.itextpdf.text.Font.NORMAL, BaseColor.GRAY);

            // ===== Tiêu đề =====
            Paragraph title = new Paragraph("HÓA ĐƠN ĐẶT CỌC", itextTitleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // ===== Thông tin khách hàng =====
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingBefore(10f);
            infoTable.setSpacingAfter(10f);
            infoTable.getDefaultCell().setBorder(com.itextpdf.text.Rectangle.NO_BORDER);

            // --- Cột trái ---
            PdfPTable leftTable = new PdfPTable(1);
            leftTable.getDefaultCell().setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            leftTable.addCell(new Phrase("Đơn vị: Khách sạn Hai Quân Đức Thịnh", itextBoldFont));
            leftTable.addCell(new Phrase("Mã hóa đơn: " + response.getHoaDon().getMaHoaDon(), itextNormalFont));
            leftTable.addCell(new Phrase("Nhân viên: " + response.getTenNhanVien().getTenNhanVien(), itextNormalFont));
            leftTable.addCell(new Phrase("Ngày tạo hóa đơn: " + TimeFormat.formatTime(new Timestamp(System.currentTimeMillis())), itextNormalFont));

            if (response.getHoaDon().getTinhTrangThanhToan() != null) {
                leftTable.addCell(new Phrase("Tình trạng thanh toán: " + response.getHoaDon().getTinhTrangThanhToan(), itextNormalFont));
            }
            if (response.getHoaDon().getPhuongThucThanhToan() != null) {
                leftTable.addCell(new Phrase("Phương thức thanh toán: ".concat(response.getHoaDon().getPhuongThucThanhToan()), itextNormalFont));
            }

            // --- Cột phải ---
            PdfPTable rightTable = new PdfPTable(1);
            rightTable.getDefaultCell().setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            rightTable.addCell(new Phrase("Khách hàng: " + response.getKhachHang().getTenKhachHang(), itextNormalFont));
            rightTable.addCell(new Phrase("SĐT: " + response.getKhachHang().getSoDienThoai(), itextNormalFont));
            rightTable.addCell(new Phrase("Ngày đến: " + TimeFormat.formatTime(response.getDonDatPhong().getTgNhanPhong()), itextNormalFont));
            rightTable.addCell(new Phrase("Ngày đi: " + TimeFormat.formatTime(response.getDonDatPhong().getTgTraPhong()), itextNormalFont));

            // --- Thêm 2 cột vào bảng cha ---
            PdfPCell leftCell = new PdfPCell(leftTable);
            PdfPCell rightCell = new PdfPCell(rightTable);
            leftCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
            rightCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);

            infoTable.addCell(leftCell);
            infoTable.addCell(rightCell);

            // --- Thêm bảng thông tin vào tài liệu ---
            document.add(infoTable);

            // ===== Bảng chi tiết phòng =====
            document.add(new Paragraph("Chi tiết phòng:", itextBoldFont));
            document.add(new Paragraph("\n"));
            PdfPTable tablePhong = new PdfPTable(4);
            tablePhong.setWidthPercentage(100);
            addTableHeader(tablePhong, new String[]{"Tên phòng", "Đơn giá", "Thời gian", "Thành tiền"}, itextBoldFont);

            //  PHẦN NÀY DÙNG ĐỂ IN TABLE PHÒNG SỬ DỤNG


            //            for (ChiTietHoaDon cthd : response.getChiTietHoaDonList()) {
//                tablePhong.addCell(new PdfPCell(new Phrase(cthd.getTenPhong(), itextNormalFont)));
//                tablePhong.addCell(new PdfPCell(new Phrase(formatCurrency(cthd.getDonGiaPhongHienTai()), itextNormalFont)));
//                tablePhong.addCell(new PdfPCell(new Phrase(getStringThoiGianSuDung(cthd.getThoiGianSuDung()), itextNormalFont)));
//                tablePhong.addCell(new PdfPCell(new Phrase(formatCurrency(cthd.getTongTien()), itextNormalFont)));
//            }
            document.add(tablePhong);
            document.add(new Paragraph("\n"));

            // ===== Bảng dịch vụ =====
            document.add(new Paragraph("Dịch vụ đã sử dụng:", itextBoldFont));
            document.add(new Paragraph("\n"));
            PdfPTable tableDV = new PdfPTable(6);
            tableDV.setWidthPercentage(100);
            addTableHeader(tableDV, new String[]{"Phòng", "Tên dịch vụ", "Đơn giá", "Số lượng", "Thành tiền", "Ghi chú"}, itextBoldFont);

//            PHẦN NÀY IN TABLE DỊCH VỤ SỬ DỤNG
            //            if (response.getPhongDungDichVuList() != null) {
//                for (var pddv : response.getPhongDungDichVuList()) {
//                    Object[] data = pddv.getSimpleObject();
//                    for (Object cell : data) {
//                        tableDV.addCell(new PdfPCell(new Phrase(cell != null ? cell.toString() : "", itextNormalFont)));
//                    }
//                }
//            }
            document.add(tableDV);
            document.add(new Paragraph("\n"));

            // ===== Tổng kết (rút gọn) =====
            document.add(new Paragraph("Tổng tiền dự tính: " + formatCurrency(response.getHoaDon().getTongTien()), itextNormalFont));
            document.add(new Paragraph("Tiền cọc: " + formatCurrency(BigDecimal.valueOf(response.getDonDatPhong().getTienDatCoc())), itextNormalFont));
            document.add(new Paragraph("Thuế VAT (" + getTaxPrice() + "%): " + formatCurrency(response.getHoaDon().getTienThue()), itextNormalFont));
            document.add(new Paragraph("Tổng hóa đơn sau thuế: " + formatCurrency(response.getHoaDon().getTongHoaDon()), itextBoldFont));
            document.add(new Paragraph("\n"));

            // ===== Footer =====
            Paragraph greeting = new Paragraph("Cảm ơn quý khách đã sử dụng dịch vụ!", itextSmallGrayFont);
            greeting.setAlignment(Element.ALIGN_CENTER);
            document.add(greeting);

            document.close();

            // ===== Thông báo & tự mở file =====
            JOptionPane.showMessageDialog(this, "Xuất file PDF thành công tại:\n" + filePath);
            File file = new File(filePath);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất PDF: " + e.getMessage());
        }
    }

    private void addTableHeader(PdfPTable table, String[] headers, com.itextpdf.text.Font font) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    public double getTaxPrice() {
        ThongTinPhuPhi thue = vn.iuh.util.FeeValue.getInstance().get(Fee.THUE);
        return thue.getGiaHienTai().doubleValue();
    }
}