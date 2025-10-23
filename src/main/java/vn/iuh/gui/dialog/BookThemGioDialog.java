package vn.iuh.gui.dialog;

import vn.iuh.dto.repository.BookThemGioInfo;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.dao.ChiTietDatPhongDAO;
import vn.iuh.service.impl.BookThemGioServiceImpl;
import vn.iuh.util.TimeFormat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public class BookThemGioDialog extends JDialog {
    private final BookingResponse thongTinDatPhong;

    // Các thành phần trên UI
    private final JLabel lblThoiGianNhan = new JLabel();
    private final JLabel lblThoiGianTra = new JLabel();
    private final JLabel lblThoiGianToiDa = new JLabel();
    private final JTextField txtNhapThoiGian = new JTextField();
    private final JLabel lblHienThiChuyenDoi = new JLabel();
    private final JLabel lblThoiGianTraMoi = new JLabel();
    private final JButton btnHuy = new JButton("Hủy");
    private final JButton btnXacNhan = new JButton("Xác nhận");

    // Các nút chức năng
    private final JButton btnQuick1H = new JButton("1 giờ");
    private final JButton btnQuick3H = new JButton("3 giờ");
    private final JButton btnQuick6H = new JButton("6 giờ");
    private final JButton btnQuick12H = new JButton("12 giờ");
    private final JButton btnQuick1D = new JButton("1 ngày");

    private long thoiGianNhanHienTaiMillis = 0L;
    private long thoiGianTraHienTaiMillis = 0L;
    private long thoiGianToiDaMillis = 0L; // Thời gian tối đa có thể book thêm
    private boolean unlimited = false; // sẽ set bằng true nếu không có đơn tiếp theo

    private BookThemGioCallback callback;
    private final DateTimeFormatter formatter = TimeFormat.getFormatter();

    private final BookThemGioInfo infoFromService;

    public interface BookThemGioCallback {
        void onXacNhan(long thoiGianThemMillis);
        default void onHuy() {}
    }

    public BookThemGioDialog(Window owner, BookingResponse bookingResponse, BookThemGioInfo info) {
        super(owner, "Book thêm giờ", ModalityType.APPLICATION_MODAL);
        this.thongTinDatPhong = bookingResponse;
        this.infoFromService = info;


        styleButtons();

        initData();
        initUI();
        attachHandlers();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void styleButtons() {
        Font btnFont = new Font("Segoe UI", Font.PLAIN, 13);
        Dimension quickSize = new Dimension(80, 30);
        for (JButton b : new JButton[]{btnQuick1H, btnQuick3H, btnQuick6H, btnQuick12H, btnQuick1D}) {
            b.setFont(btnFont);
            b.setPreferredSize(quickSize);
            b.setFocusPainted(false);
        }
        btnHuy.setFont(btnFont);
        btnHuy.setPreferredSize(new Dimension(100, 36));
        btnHuy.setFocusPainted(false);

        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXacNhan.setPreferredSize(new Dimension(140, 36));
        btnXacNhan.setFocusPainted(false);
    }

    private void initData() {
        // Sử dụng info để lấy các thông tin cần thiết hiển thị lên UI
        if (infoFromService != null) {
            if (infoFromService.getTgNhanPhong() != null) thoiGianNhanHienTaiMillis = infoFromService.getTgNhanPhong().getTime();
            if (infoFromService.getTgTraPhong() != null) thoiGianTraHienTaiMillis = infoFromService.getTgTraPhong().getTime();

            int g = infoFromService.getGioToiDaChoPhep();
            if (g < 0) {
                // -1 nghĩa là không có giới hạn
                unlimited = true;
                thoiGianToiDaMillis = Long.MAX_VALUE;
            } else {
                unlimited = false;
                thoiGianToiDaMillis = (long) g * 3600L * 1000L;
            }
            return;
        }

        // Nếu không có infoFromService, chỉ dùng dữ liệu từ BookingResponse (nếu có)
        // và đặt giá trị mặc định an toàn cho thời gian tối đa (72 giờ).
        final long DEFAULT_MAX_HOURS = 72L;
        final long DEFAULT_MAX_MILLIS = DEFAULT_MAX_HOURS * 3600L * 1000L;

        try {
            if (thongTinDatPhong != null) {
                if (thongTinDatPhong.getTimeIn() != null) thoiGianNhanHienTaiMillis = thongTinDatPhong.getTimeIn().getTime();
                if (thongTinDatPhong.getTimeOut() != null) thoiGianTraHienTaiMillis = thongTinDatPhong.getTimeOut().getTime();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Vì không có thông tin từ service để biết booking phía sau, đặt giới hạn mặc định
        unlimited = false;
        thoiGianToiDaMillis = DEFAULT_MAX_MILLIS;
    }


    private void initUI() {
        // Giao diện chính
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(14, 14, 14, 14));
        content.setBackground(Color.white);

        // Tiêu đề
        JLabel title = new JLabel("Book thêm giờ", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        content.add(title, BorderLayout.NORTH);

        // Tạo 2 cột cho bảng, bên trái là thông tin thời gian checkin-checkout hiện tại
        // bên phải là các thôgn tin liên quan đến book thêm giờ
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.white);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.BOTH;

        // Cột bên trái
        JPanel left = new JPanel(new GridLayout(0, 1, 8, 8));
        left.setBackground(new Color(0xFAFBFD));
        left.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xE0E6EE)), new EmptyBorder(12, 12, 12, 12)));

        JLabel lblInfoTitle = new JLabel("Thông tin hiện tại");
        lblInfoTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        left.add(lblInfoTitle);

        String sNhan = thoiGianNhanHienTaiMillis > 0 ? new Timestamp(thoiGianNhanHienTaiMillis).toLocalDateTime().format(formatter) : "-";
        String sTra = thoiGianTraHienTaiMillis > 0 ? new Timestamp(thoiGianTraHienTaiMillis).toLocalDateTime().format(formatter) : "-";
        lblThoiGianNhan.setText("<html><b>Check-in:</b> " + sNhan + "</html>");
        lblThoiGianTra.setText("<html><b>Check-out hiện tại:</b> " + sTra + "</html>");
        lblThoiGianNhan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblThoiGianTra.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        left.add(lblThoiGianNhan);
        left.add(lblThoiGianTra);

        // Thời gian tối đa có thể book thêm cho phòng
        lblThoiGianToiDa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (unlimited) {
            lblThoiGianToiDa.setText("<html><b>Thời gian tối đa có thể book thêm:</b> <span style='color: #117a8b;'>Không có</span></html>");
        } else {
            lblThoiGianToiDa.setText("<html><b>Thời gian tối đa có thể book thêm:</b> " + hienThiKhoangThoiGian(thoiGianToiDaMillis) + "</html>");
        }
        left.add(lblThoiGianToiDa);

        // Add cột bên trái
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.45;
        gbc.weighty = 1.0;
        body.add(left, gbc);

        // Cột bên phải
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.white);
        right.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(0xE0E6EE)), new EmptyBorder(12, 12, 12, 12)));
        GridBagConstraints r = new GridBagConstraints();
        r.insets = new Insets(8, 8, 8, 8);
        r.anchor = GridBagConstraints.WEST;
        r.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblNhapTitle = new JLabel("Nhập thời gian muốn thêm");
        lblNhapTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        r.gridx = 0; r.gridy = 0; r.gridwidth = 2;
        right.add(lblNhapTitle, r);

        JLabel lblHint = new JLabel("<html><i>Hỗ trợ: '1 giờ', '3h', '1d2h', '36 giờ'</i></html>");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        r.gridy = 1; r.gridwidth = 2;
        right.add(lblHint, r);

        // Dòng để nhập thời gian
        txtNhapThoiGian.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNhapThoiGian.setPreferredSize(new Dimension(220, 34));
        r.gridy = 2; r.gridwidth = 2;
        right.add(txtNhapThoiGian, r);

        // Một vài nút để cho phép người dùng nhập nhanh
        JPanel quickRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        quickRow.setBackground(Color.white);
        quickRow.add(btnQuick1H);
        quickRow.add(btnQuick3H);
        quickRow.add(btnQuick6H);
        quickRow.add(btnQuick12H);
        quickRow.add(btnQuick1D);

        r.gridy = 3; r.gridwidth = 2;
        right.add(quickRow, r);

        // Hiển thị thời gian book thêm thực tế sau khi chuyển đổi dữ liệu từ thông tin được nhập
        lblHienThiChuyenDoi.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblHienThiChuyenDoi.setText("(Hiển thị chuyển đổi: 0 giờ)");
        r.gridy = 4; r.gridwidth = 2;
        right.add(lblHienThiChuyenDoi, r);

        lblThoiGianTraMoi.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblThoiGianTraMoi.setForeground(new Color(0x205072));
        lblThoiGianTraMoi.setText("Thời gian trả dự kiến: -");
        r.gridy = 5; r.gridwidth = 2;
        right.add(lblThoiGianTraMoi, r);

        // Nút hủy và nút xác nhận
        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelBtns.setBackground(Color.white);
        panelBtns.add(btnHuy);
        panelBtns.add(btnXacNhan);

        r.gridy = 6; r.gridwidth = 2;
        right.add(panelBtns, r);

        // Thêm cột bên phải vào dialog
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.55;
        body.add(right, gbc);

        content.add(body, BorderLayout.CENTER);
        setContentPane(content);

        // Không cho phép ấn khi chưa nhập dữ liệu
        btnXacNhan.setEnabled(false);
    }

    // Đăng ký sự kiện cho các nút chức năng
    private void attachHandlers() {
        // Mỗi nút khi nhấn sẽ gán chuỗi tương ứng vào khung text nhập thời gian
        btnQuick1H.addActionListener(e -> { txtNhapThoiGian.setText("1h"); updatePreview(); });
        btnQuick3H.addActionListener(e -> { txtNhapThoiGian.setText("3h"); updatePreview(); });
        btnQuick6H.addActionListener(e -> { txtNhapThoiGian.setText("6h"); updatePreview(); });
        btnQuick12H.addActionListener(e -> { txtNhapThoiGian.setText("12h"); updatePreview(); });
        btnQuick1D.addActionListener(e -> { txtNhapThoiGian.setText("1d"); updatePreview(); });

        // Lắng nghe sự kiện khi người dùng thay đổi dữ liệu trong ô nhập
        txtNhapThoiGian.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updatePreview(); }
            @Override public void removeUpdate(DocumentEvent e) { updatePreview(); }
            @Override public void changedUpdate(DocumentEvent e) { updatePreview(); }
        });

        // Sự kiện cho nút hủy
        btnHuy.addActionListener(e -> {
            if (callback != null) callback.onHuy();
            dispose();
        });

        // Sự kiện cho nút xác nhận
        btnXacNhan.addActionListener(e -> onXacNhanClicked());

        // Đóng cửa sổ (nút X)
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (callback != null) callback.onHuy();
                dispose();
            }
        });
    }

    // Cập nhập preview khi người dùng nhập dữ liệu vào ô text (hoặc bấm quick button)
    private void updatePreview() {
        String raw = txtNhapThoiGian.getText().trim();
        long millis = parseDurationToMillis(raw);
        if (millis < 0) {
            lblHienThiChuyenDoi.setText("(Không hiểu định dạng)");
            lblThoiGianTraMoi.setText("Thời gian trả dự kiến: -");
            btnXacNhan.setEnabled(false);
            return;
        }

        // Nếu thời gian được nhập vào lớn hơn thời gian tối đa thì mặc định
        // là lấy thời gian tối đa để book thêm
        long usedMillis = millis;
        if (!unlimited && millis > thoiGianToiDaMillis) usedMillis = thoiGianToiDaMillis;

        // Hiển thị thời gian book thêm sau khi chuyển đổi từ dữ liệu mà người dùng
        // nhập
        String pretty = hienThiKhoangThoiGian(usedMillis);
        lblHienThiChuyenDoi.setText("(Hiển thị chuyển đổi: " + pretty + ")");

        // Hiển thị thời gian trả dự kiến sau khi book thêm giờ
        if (thoiGianTraHienTaiMillis > 0 && usedMillis > 0) {
            Timestamp tgTraMoi = new Timestamp(thoiGianTraHienTaiMillis + usedMillis);
            String s = tgTraMoi.toLocalDateTime().format(formatter);
            lblThoiGianTraMoi.setText("<html><b>Thời gian trả dự kiến:</b> " + s + (unlimited ? " <i>(không giới hạn)</i>" : "") + "</html>");
        } else {
            lblThoiGianTraMoi.setText("Thời gian trả dự kiến: -");
        }

        btnXacNhan.setEnabled(usedMillis > 0);
    }

    // Hàm onXacNhanClicked khi người dùng nhấn xác nhận book thêm giờ
    // thì sẽ gọi xuống service để xử lí
    private void onXacNhanClicked() {
        String raw = txtNhapThoiGian.getText().trim();
        long millis = parseDurationToMillis(raw);
        if (millis < 0) {
            JOptionPane.showMessageDialog(this, "Không hiểu định dạng thời gian. Vui lòng nhập lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Thông báo lựa chọn khi người dùng nhập quá thời gian tối đa cho phép book thêm
        if (!unlimited && millis > thoiGianToiDaMillis) {
            String s = hienThiKhoangThoiGian(millis);
            String sMax = hienThiKhoangThoiGian(thoiGianToiDaMillis);
            int r = JOptionPane.showConfirmDialog(this,
                    "Bạn nhập " + s + " nhưng thời gian tối đa chỉ là " + sMax + ".\nBạn muốn tự động chuyển thành " + sMax + "?",
                    "Thời gian vượt quá", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
            millis = thoiGianToiDaMillis;
        }

        // Gọi service để xử lí
        BookThemGioServiceImpl service = new BookThemGioServiceImpl();
        boolean ok = false;
        try {
            String maChiTiet = (thongTinDatPhong != null) ? thongTinDatPhong.getMaChiTietDatPhong() : null;
            String maPhong = (thongTinDatPhong != null) ? thongTinDatPhong.getRoomId() : null;
            ok = service.bookThemGio(maChiTiet, maPhong, millis);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (ok) {
            JOptionPane.showMessageDialog(this, "Gia hạn thời gian thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            if (callback != null) callback.onXacNhan(millis);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Gia hạn thời gian thất bại. Vui lòng kiểm tra điều kiện (phòng đã check-in, chưa checkout, không phải checkout trễ) và thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Chuyển chuỗi do người dùng nhập (như "1d2h", "3h", "36", "1 ngày")
    // thành giá trị milliseconds
    private long parseDurationToMillis(String raw) {
        if (raw == null || raw.isEmpty()) return 0L;
        String s = raw.toLowerCase().trim();

        // Normalize
        s = s.replaceAll("ngày", "d");
        s = s.replaceAll("ng\\.?", "d");
        s = s.replaceAll("giờ", "h");
        s = s.replaceAll("gio", "h");
        s = s.replaceAll("\\s+", "");

        int days = 0;
        int hours = 0;

        try {
            if (s.matches(".*d.*h.*")) {
                String[] parts = s.split("d");
                String dpart = parts[0];
                String hpart = parts[1].replace("h", "");
                days = Integer.parseInt(dpart);
                hours = Integer.parseInt(hpart);
            } else if (s.endsWith("d")) {
                String dpart = s.replace("d", "");
                days = Integer.parseInt(dpart);
            } else if (s.endsWith("h")) {
                String hpart = s.replace("h", "");
                hours = Integer.parseInt(hpart);
            } else if (s.matches("\\d+")) {
                // plain number interpreted as hours
                hours = Integer.parseInt(s);
            } else {
                return -1L;
            }
        } catch (NumberFormatException nfe) {
            return -1L;
        }

        long totalHours = (long) days * 24L + (long) hours;
        long millis = totalHours * 3600L * 1000L;
        return millis;
    }

    // Chuyển khoảng thời gian mili giây thành chuỗi
    private String hienThiKhoangThoiGian(long millis) {
        if (millis <= 0) return "0 giờ";
        long totalHours = millis / (3600L * 1000L);
        long ngay = totalHours / 24L;
        long gio = totalHours % 24L;
        StringBuilder sb = new StringBuilder();
        if (ngay > 0) sb.append(ngay).append(" ngày");
        if (gio > 0) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(gio).append(" giờ");
        }
        if (sb.length() == 0) sb.append("0 giờ");
        return sb.toString();
    }

    public void setCallback(BookThemGioCallback callback) {
        this.callback = callback;
    }

}
