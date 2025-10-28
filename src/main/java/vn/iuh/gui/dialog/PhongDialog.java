package vn.iuh.gui.dialog;

import vn.iuh.entity.CongViec;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.entity.Phong;
import vn.iuh.dto.repository.RoomFurnitureItem;
import vn.iuh.service.RoomService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

// Dialog chi tiết thông tin của một phòng
public class PhongDialog extends JDialog {
    private final RoomService roomService;
    private final Phong phong;

    // Các thông tin của phòng
    private final JLabel lblName = new JLabel();
    private final JLabel lblCode = new JLabel();
    private final JLabel lblType = new JLabel();
    private final JLabel lblPeople = new JLabel();
    private final JLabel lblStatus = new JLabel();
    private final JLabel lblPriceHour = new JLabel();
    private final JLabel lblPriceDay = new JLabel();

    // Mô tả / ghi chú
    private final JTextArea txtDesc = new JTextArea();

    private final DefaultListModel<String> furnitureModel = new DefaultListModel<>();
    private final JList<String> listFurniture = new JList<>(furnitureModel);

    public PhongDialog(Window owner, Phong phong, RoomService roomService) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.roomService = roomService;
        this.phong = phong;
        initialize();
        loadData();
    }

    private void initialize() {
        setTitle("Chi tiết phòng - " + (phong != null ? phong.getTenPhong() : ""));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(640, 460);
        setMinimumSize(new Dimension(520, 380));
        setLayout(new BorderLayout(12, 12));

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(12,12,12,12));
        add(content, BorderLayout.CENTER);

        // Panel chứa thông tin phòng
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        content.add(infoPanel, BorderLayout.CENTER);

        // Tên phòng
        lblName.setFont(new Font("Arial", Font.BOLD, 20));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lblName);
        infoPanel.add(Box.createVerticalStrut(10));

        // Mã phòng
        lblCode.setFont(new Font("Arial", Font.PLAIN, 14));
        lblCode.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lblCode);
        infoPanel.add(Box.createVerticalStrut(8));

        // Loại phòng
        lblType.setFont(new Font("Arial", Font.PLAIN, 14));
        lblType.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lblType);
        infoPanel.add(Box.createVerticalStrut(8));

        // Số người
        lblPeople.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPeople.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lblPeople);
        infoPanel.add(Box.createVerticalStrut(8));

        // Trạng thái
        lblStatus.setFont(new Font("Arial", Font.BOLD, 14));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lblStatus);
        infoPanel.add(Box.createVerticalStrut(8));

        // Giá theo giờ
        lblPriceHour.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPriceHour.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lblPriceHour);
        infoPanel.add(Box.createVerticalStrut(6));

        // Giá theo ngày
        lblPriceDay.setFont(new Font("Arial", Font.PLAIN, 14));
        lblPriceDay.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lblPriceDay);
        infoPanel.add(Box.createVerticalStrut(12));

        // Khung danh sách nội thất
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setEditable(false);
        txtDesc.setFont(new Font("Arial", Font.PLAIN, 13));
        txtDesc.setBorder(BorderFactory.createTitledBorder("Mô tả / Ghi chú"));
        txtDesc.setBackground(getBackground());

        JScrollPane descScroll = new JScrollPane(txtDesc);
        descScroll.setPreferredSize(new Dimension(320, 120));
        descScroll.setBorder(null);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(descScroll);

        // Thông tin danh sách các nội thất
        JPanel right = new JPanel(new BorderLayout(6,6));
        right.setPreferredSize(new Dimension(240, 0));
        right.setOpaque(false);
        right.setBorder(BorderFactory.createTitledBorder("Nội thất"));

        listFurniture.setVisibleRowCount(10);
        JScrollPane furnitureScroll = new JScrollPane(listFurniture);
        right.add(furnitureScroll, BorderLayout.CENTER);
        content.add(right, BorderLayout.EAST);

        // Các nút ở phần bottom
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
//        JButton btnEdit = new JButton("Sửa");
//        JButton btnDelete = new JButton("Xóa");
        JButton btnClose = new JButton("Đóng");
//        bottom.add(btnEdit);
//        bottom.add(btnDelete);
        bottom.add(btnClose);
        add(bottom, BorderLayout.SOUTH);

        btnClose.addActionListener(e -> dispose());
    }

    private void loadData() {
        if (phong == null) return;

        lblName.setText(phong.getTenPhong() != null ? phong.getTenPhong() : "—");
        lblCode.setText("Mã: " + (phong.getMaPhong() != null ? phong.getMaPhong() : "—"));

        // Load những thông tin liên quan đến loại phòng
        LoaiPhong lp = null;
        try {
            lp = roomService.getRoomCategoryByID(phong.getMaLoaiPhong());
        } catch (Exception ignored) {}
        if (lp != null) {
            lblType.setText("Loại: " + (lp.getTenLoaiPhong() != null ? lp.getTenLoaiPhong() : (lp.getPhanLoai() != null ? lp.getPhanLoai() : "—")));
            lblPeople.setText("Số khách: " + lp.getSoLuongKhach());
        } else {
            lblType.setText("Loại: —");
            lblPeople.setText("Số khách: —");
        }

        // Lấy trạng thái hiện tại của phòng để hiển thị
        String status = "Trống";
        try {
            CongViec cv = roomService.getCurrentJobForRoom(phong.getMaPhong());
            if (cv != null && cv.getTenTrangThai() != null && !cv.getTenTrangThai().isEmpty()) {
                status = cv.getTenTrangThai();
            } else if (!phong.isDangHoatDong()) {
                status = "Bảo trì";
            }
        } catch (Exception ignore) {
            if (!phong.isDangHoatDong()) status = "Bảo trì";
        }
        lblStatus.setText("Trạng thái: " + status);

        // Giá (ngày/giờ)
        double[] price = {0.0, 0.0};
        try { price = roomService.getLatestPriceForLoaiPhong(phong.getMaLoaiPhong()); } catch (Exception ignored) {}
        lblPriceHour.setText("Giá giờ: " + formatPrice(price.length > 1 ? price[1] : 0.0));
        lblPriceDay.setText("Giá ngày: " + formatPrice(price.length > 0 ? price[0] : 0.0));

        // Mô tả + ghi chú (nếu có)
        StringBuilder descBuilder = new StringBuilder();
        if (phong.getMoTaPhong() != null && !phong.getMoTaPhong().isBlank()) {
            descBuilder.append(phong.getMoTaPhong().trim());
        }
        if (phong.getGhiChu() != null && !phong.getGhiChu().isBlank()) {
            if (descBuilder.length() > 0) descBuilder.append("\n\n");
            descBuilder.append("Ghi chú: ").append(phong.getGhiChu().trim());
        }
        txtDesc.setText(descBuilder.length() > 0 ? descBuilder.toString() : "");

        // Danh sách nội thất
        furnitureModel.clear();
        try {
            List<RoomFurnitureItem> items = roomService.getAllFurnitureInRoom(phong.getMaPhong());
            if (items != null && !items.isEmpty()) {
                for (RoomFurnitureItem it : items) {
                    furnitureModel.addElement(it.getName() + " x" + it.getQuantity());
                }
            } else {
                furnitureModel.addElement("Không có nội thất được cấu hình");
            }
        } catch (Exception ex) {
            furnitureModel.addElement("Lỗi tải nội thất");
        }
    }

    // Format giá phòng
    private static String formatPrice(double price) {
        if (price <= 0.0) return "0 VNĐ";
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(price) + " VNĐ";
    }
    // Mở dialog
    public static void showDialog(Component parent, Phong p, RoomService roomService) {
        Window w = parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
        PhongDialog d = new PhongDialog(w, p, roomService);
        d.setLocationRelativeTo(w);
        d.setVisible(true);
    }
}
