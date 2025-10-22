package vn.iuh.gui.panel;

import vn.iuh.constraint.RoomStatus;
import vn.iuh.dao.ChiTietDatPhongDAO;
import vn.iuh.dao.LoaiPhongDAO;
import vn.iuh.dto.response.BookingResponse;
import vn.iuh.entity.LoaiPhong;
import vn.iuh.gui.base.GridRoomPanel;
import vn.iuh.gui.base.RoomItem;
import vn.iuh.service.DoiPhongService;
import vn.iuh.service.impl.DoiPhongServiceImpl;
import vn.iuh.util.RefreshManager;
import vn.iuh.util.TimeFormat;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DoiPhongDiaLog extends JPanel {
    private final BookingResponse currentBooking; // Đơn đặt phòng hiện tại đang cần đổi phòng
    private GridRoomPanel gridRoomPanel;
    private JLabel infoLabel;
    private JLabel subInfoLabel;
    private JLabel countLabel; // Hiển thị số phòng tìm được
    private final DateTimeFormatter formatter = TimeFormat.getFormatter();
    private List<BookingResponse> originalCandidates; // Danh sách các phòng phù hợp
    private JTextField txtSearch;
    private JButton btnConfirm; // Chỉ sáng khi có phòng được chọn
    private JCheckBox chkApplyFee; // checkbox "Tính phụ phí"

    private ChangeRoomCallback callback;

    private final DoiPhongService doiPhongService;

    // Callback để thông báo khi người dùng xác nhận đổi phòng
    public interface ChangeRoomCallback {
        void onChangeRoom(String oldRoomId, BookingResponse newRoom, boolean applyFee);

        default void onCancel() {}
    }

    // Giao diện đổi phòng
    public DoiPhongDiaLog(BookingResponse currentBooking, List<BookingResponse> candidateRooms) {
        this.currentBooking = currentBooking;
        this.doiPhongService = new DoiPhongServiceImpl();
        this.originalCandidates = candidateRooms == null ? new ArrayList<>() : new ArrayList<>(candidateRooms);
        initUI(this.originalCandidates);
    }

    private void initUI(List<BookingResponse> candidateRooms) {
        setLayout(new BorderLayout(12, 12));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("ĐỔI PHÒNG", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        add(title, BorderLayout.NORTH);

        JPanel topBox = new JPanel(new BorderLayout(8, 8));
        topBox.setBackground(Color.WHITE);
        topBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE6E9EE), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        // Left
        JPanel leftBox = new JPanel();
        leftBox.setLayout(new BoxLayout(leftBox, BoxLayout.Y_AXIS));
        leftBox.setBackground(Color.WHITE);

        // Dòng đầu tiên: Thông tin chính + checkbox tính phụ phí
        JPanel mainInfoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        mainInfoRow.setBackground(Color.WHITE);

        String roomType = currentBooking != null && currentBooking.getRoomType() != null
                ? currentBooking.getRoomType().toUpperCase() : "";

        infoLabel = new JLabel(String.format("<html><b>Danh sách phòng có loại phòng: %s</b></html>", roomType.isEmpty() ? "Tất cả" : roomType));
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Checkbox đặt ngay bên cạnh dòng tiêu đề
        chkApplyFee = new JCheckBox("Tính phụ phí");
        chkApplyFee.setToolTipText("Đánh dấu nếu đổi phòng cần tính phụ phí cho khách");

        mainInfoRow.add(infoLabel);
        mainInfoRow.add(Box.createRigidArea(new Dimension(8,0)));
        mainInfoRow.add(chkApplyFee);

        // Second row: sub info (số người cần + checkout)
        JPanel subInfoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        subInfoRow.setBackground(Color.WHITE);

        // Lấy số người cần từ thuộc tính so_luong_khach của LoaiPhong
        int neededPersons = 1;
        try {
            if (roomType != null && !roomType.isEmpty()) {
                LoaiPhongDAO lpDao = new LoaiPhongDAO();
                List<LoaiPhong> all = lpDao.layTatCaLoaiPhong();
                if (all != null) {
                    for (LoaiPhong lp : all) {
                        if (lp.getTenLoaiPhong() != null && lp.getTenLoaiPhong().equalsIgnoreCase(roomType)) {
                            neededPersons = lp.getSoLuongKhach();
                            break;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            neededPersons = 1;
        }

        String checkout = "-";
        try {
            if (currentBooking != null && currentBooking.getTimeOut() != null)
                checkout = currentBooking.getTimeOut().toLocalDateTime().format(formatter);
        } catch (Exception ignored) { }

        subInfoLabel = new JLabel(String.format("Số người cần: %d    —    Còn trống đến: %s", neededPersons, checkout));
        subInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subInfoLabel.setForeground(new Color(0x555555));

        subInfoRow.add(subInfoLabel);

        leftBox.add(mainInfoRow);
        leftBox.add(subInfoRow);

        topBox.add(leftBox, BorderLayout.WEST);

        // RIGHT: Đếm số lượng phòng + khung tìm kiếm (tên phòng, mã phòng, mã loại phòng)
        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setBackground(Color.WHITE);

        //Đếm số lượng phòng đựoc tìm thấy và hiển thị
        countLabel = new JLabel(buildCountText(candidateRooms.size()), SwingConstants.RIGHT);
        countLabel.setFont(new Font("Arial", Font.BOLD, 12));
        rightPanel.add(countLabel, BorderLayout.NORTH);

        JPanel searchRow = new JPanel(new BorderLayout(6, 6));
        searchRow.setBackground(Color.WHITE);

        //Cho phép tìm theo mã phòng, mã loại phòng, tên phòng
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(220, 30));
        txtSearch.setToolTipText("Tìm theo tên phòng / mã / loại...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterAndRefresh(); }
            @Override public void removeUpdate(DocumentEvent e) { filterAndRefresh(); }
            @Override public void changedUpdate(DocumentEvent e) { filterAndRefresh(); }
        });

        // Xóa các thông tin đã được nhập vào khung tìm kiếm
        JButton btnClear = new JButton("Xóa");
        btnClear.setPreferredSize(new Dimension(60, 30));
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            filterAndRefresh();
        });

        searchRow.add(txtSearch, BorderLayout.CENTER);
        searchRow.add(btnClear, BorderLayout.EAST);

        rightPanel.add(searchRow, BorderLayout.SOUTH);

        topBox.add(rightPanel, BorderLayout.EAST);

        add(topBox, BorderLayout.BEFORE_FIRST_LINE);

        // Phần BOTTOM (hint + button cancel and confirm)
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Left: hint
        JPanel leftHint = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftHint.setBackground(Color.WHITE);
        JLabel hint = new JLabel("Chọn 1 phòng rồi nhấn Xác nhận hoặc nhấp đúp vào phòng để chọn nhanh.");
        hint.setFont(new Font("Arial", Font.ITALIC, 12));
        hint.setForeground(new Color(0x666666));
        leftHint.add(hint);
        bottom.add(leftHint, BorderLayout.WEST);

        // Right: action buttons (cancel and confirm)
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setBackground(Color.WHITE);

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setPreferredSize(new Dimension(120, 36));
        btnCancel.setFocusPainted(false);
        btnCancel.addActionListener(e -> {
            if (callback != null) {
                callback.onCancel();
            } else {
                // Nếu không có callback, đóng chính dialog đang chứa panel này (nếu có)
                Window w = SwingUtilities.getWindowAncestor(DoiPhongDiaLog.this);
                if (w instanceof JDialog) ((JDialog) w).dispose();
            }
        });

        btnConfirm = new JButton("Xác nhận đổi phòng");
        btnConfirm.setPreferredSize(new Dimension(170, 36));
        btnConfirm.setFocusPainted(false);
        btnConfirm.setEnabled(false);

        btnConfirm.addActionListener(e -> {
            BookingResponse chosen = null;
            for (RoomItem item : gridRoomPanel.getRoomItems()) {
                if (item.isSelected()) {
                    chosen = item.getBookingResponse();
                    break;
                }
            }

            if (chosen == null) {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(DoiPhongDiaLog.this),
                        "Vui lòng chọn 1 phòng để đổi.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Dialog tự xử lý đổi phòng (gọi service)
            performChangeRoom(chosen);
        });

        rightBtns.add(btnCancel);
        rightBtns.add(btnConfirm);
        bottom.add(rightBtns, BorderLayout.EAST);

        rebuildGrid(candidateRooms);

        add(bottom, BorderLayout.SOUTH);

        // tăng chiều rộng để RoomItem rộng hơn
        setPreferredSize(new Dimension(1100, 650));
    }

    // Hiển thị số lượng phòng tìm thấy
    private String buildCountText(int count) {
        return String.format("<html><span style='color:#333333'>Phòng tìm thấy: <b>%d</b></span></html>", count);
    }

    //Tạo GridRoomPanel chứa thông các phong phù hợp để đổi phòng
    private void rebuildGrid(List<BookingResponse> danhSachPhong) {
        SwingUtilities.invokeLater(() -> {
            if (gridRoomPanel != null) {
                Component toRemove = null;
                for (Component c : getComponents()) {
                    if (c instanceof JScrollPane) {
                        toRemove = c;
                        break;
                    }
                }
                if (toRemove != null) remove(toRemove);
            }

            List<RoomItem> roomItems = new ArrayList<>();
            for (BookingResponse br : danhSachPhong) {
                // Kiểm tra phòng hiện có đang trống hay không
                boolean isEmpty = br.getRoomStatus() != null && br.getRoomStatus()
                        .equalsIgnoreCase(RoomStatus.ROOM_EMPTY_STATUS.getStatus());

                RoomItem ri = new RoomItem(br, isEmpty);

                //Cho phép click chọn
                ri.setMultiBookingMode(true);

                ri.setSelectionCallback((bookingResponse, selected) -> {
                    if (selected) {
                        for (RoomItem other : roomItems) {
                            if (other != ri && other.isSelected()) other.setSelected(false);
                        }
                    }
                    // Bật sáng nút xác nhận đổi phòng khi có phòng được chọn
                    boolean anySelected = roomItems.stream().anyMatch(RoomItem::isSelected);
                    if (btnConfirm != null) btnConfirm.setEnabled(anySelected);
                });

                ri.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {

                            for (RoomItem other : roomItems) {
                                other.setSelected(other == ri);
                            }

                            if (btnConfirm != null) btnConfirm.setEnabled(true);

                            // Thay vì đẩy cho callback, dialog tự handle việc gọi service
                            performChangeRoom(ri.getBookingResponse());
                        }
                    }
                });

                roomItems.add(ri);
            }

            gridRoomPanel = new GridRoomPanel(roomItems);

            boolean anySelected = roomItems.stream().anyMatch(RoomItem::isSelected);
            if (btnConfirm != null) btnConfirm.setEnabled(anySelected);

            JScrollPane scroll = new JScrollPane(gridRoomPanel,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUnitIncrement(16);

            add(scroll, BorderLayout.CENTER);
            revalidate();
            repaint();
        });
    }

    // Thay đổi thông tin các phòng tìm được dưới GridRoomPanel khi điền thông tin tìm kiếm vào khung tìm kiếm
    private void filterAndRefresh() {
        String q = txtSearch.getText();
        List<BookingResponse> filtered;
        if (q == null || q.trim().isEmpty()) {
            filtered = new ArrayList<>(originalCandidates);
        } else {
            String low = q.trim().toLowerCase();
            filtered = originalCandidates.stream()
                    .filter(b -> (b.getRoomName() != null && b.getRoomName().toLowerCase().contains(low))
                            || (b.getRoomId() != null && b.getRoomId().toLowerCase().contains(low))
                            || (b.getRoomType() != null && b.getRoomType().toLowerCase().contains(low)))
                    .collect(Collectors.toList());
        }

        countLabel.setText(buildCountText(filtered.size()));
        rebuildGrid(filtered);
    }

    public void setChangeRoomCallback(ChangeRoomCallback callback) {
        this.callback = callback;
    }

    private void performChangeRoom(BookingResponse chosen) {
        if (chosen == null) return;

        if (doiPhongService == null) {
            // Nếu service chưa được khai báo thì báo lỗi
            JOptionPane.showMessageDialog(this,
                    "Service đổi phòng chưa được khởi tạo. Vui lòng kiểm tra cấu hình.",
                    "Lỗi cấu hình", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 1) Hỏi tính phụ phí đổi phòng
        boolean applyFee = chkApplyFee != null && chkApplyFee.isSelected();
        if (!applyFee) {
            int feeChoice = JOptionPane.showConfirmDialog(this,
                    "Bạn có muốn tính phụ phí đổi phòng 100.000 VND không?",
                    "Phụ phí đổi phòng", JOptionPane.YES_NO_OPTION);
            applyFee = (feeChoice == JOptionPane.YES_OPTION);
        }

        // 2) Xác nhận tính phụ phí
        String feeText = applyFee ? "\n(Phụ phí 100.000 VND sẽ được tính.)" : "";
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xác nhận đổi phòng từ " + (currentBooking != null ? currentBooking.getRoomName() : "") +
                        " sang " + chosen.getRoomName() + "?" + feeText,
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // 3) Gọi service
        boolean success = false;
        try {
            // Tìm mã đơn đặt phòng
            String maDonDatPhongToSend = null;
            if (currentBooking != null) {

                try {
                    String maChiTietDatPhongToFind = currentBooking.getMaChiTietDatPhong();
                    maDonDatPhongToSend = doiPhongService.layMaDonDatPhong(maChiTietDatPhongToFind);
                } catch (NoSuchMethodError | RuntimeException ignored) { }

                // 2) Nếu vẫn null thì cố gắng tìm từ chiTietDatPhong
                if ((maDonDatPhongToSend == null || maDonDatPhongToSend.isEmpty())
                        && currentBooking.getMaChiTietDatPhong() != null) {
                    maDonDatPhongToSend = currentBooking.getMaChiTietDatPhong();
                }
            }

            String oldRoomId = currentBooking != null ? currentBooking.getRoomId() : null;
            String newRoomId = chosen.getRoomId();

            // Nếu vẫn null thì báo lỗi
            if (maDonDatPhongToSend == null) {
                JOptionPane.showMessageDialog(this,
                        "Không xác định được mã đơn đặt phòng (ma_don_dat_phong). Không thể đổi phòng.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            success = doiPhongService.changeRoom(maDonDatPhongToSend, oldRoomId, newRoomId, applyFee);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 4) Hậu xử lý
        if (success) {
            JOptionPane.showMessageDialog(this, "Đổi phòng thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            // refresh UI
            RefreshManager.refreshAfterCancelReservation();
            // thông báo callback
            if (callback != null) callback.onChangeRoom(currentBooking != null ? currentBooking.getRoomId() : null, chosen, applyFee);
            // đóng dialog
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof JDialog) ((JDialog) w).dispose();
        } else {
            String err = null;
            try { err = doiPhongService.getLastError(); } catch (Exception ignored) {}
            if (err == null || err.trim().isEmpty()) err = "Đổi phòng thất bại, vui lòng thử lại.";
            JOptionPane.showMessageDialog(this, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

}
