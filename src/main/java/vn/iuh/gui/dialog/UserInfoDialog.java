package vn.iuh.gui.dialog;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import vn.iuh.dao.NhanVienDAO;
import vn.iuh.dao.TaiKhoanDAO;
import vn.iuh.dto.repository.ThongTinNhanVien;
import vn.iuh.entity.NhanVien;
import vn.iuh.gui.base.CustomUI;
import vn.iuh.gui.base.Main;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DateFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UserInfoDialog extends JDialog {

    // --- Fonts ---
    private static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 20);
    private static final Font FONT_LABEL = new Font("Arial", Font.BOLD, 15);
    private static final Font FONT_FIELD = new Font("Arial", Font.PLAIN, 15);
    private static final Font FONT_ROLE = new Font("Arial", Font.BOLD, 16);

    // --- Components ---
    private CircularImagePanel pnlAvatar;
    private JButton btnThayAnh;
    private JLabel lblChucVu;
    private JTextField txtTen;
    private JTextField txtCCCD;
    private JTextField txtNgaySinh;
    private JTextField txtSDT;
    private JButton btnDoiMatKhau;

    private NhanVienDAO nhanVienDAO;
    private TaiKhoanDAO taiKhoanDAO;
    private NhanVien nhanVienData;
    private ThongTinNhanVien currentUser;
    private File selectedAvatarFile = null;
    private String maNhanVien;

    public UserInfoDialog(Frame parent, NhanVien nv) {
        super(parent, "Thông tin cá nhân", true);
        setSize(new Dimension(700, 450));
        this.nhanVienData = nv;
        this.maNhanVien = nv.getMaNhanVien();

        this.nhanVienDAO = new NhanVienDAO();
        this.taiKhoanDAO = new TaiKhoanDAO();

        init();
        loadCurrentUserData();
        addActions();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        //pack();
        setLocationRelativeTo(parent);
    }

    private void init() {
        setLayout(new BorderLayout(10, 10));
        setBackground(CustomUI.white);

        JPanel pnlTop = createTopPanel("Thông tin cá nhân");
        add(pnlTop, BorderLayout.NORTH);
        JPanel pnlMain = createMainContentPanel();
        add(pnlMain, BorderLayout.CENTER);
    }

    private JPanel createTopPanel(String title) {
        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTop = new JLabel(title, SwingConstants.CENTER);
        lblTop.setForeground(CustomUI.white);
        lblTop.setFont(FONT_TITLE);
        pnlTop.setBackground(CustomUI.blue);
        pnlTop.add(lblTop, BorderLayout.CENTER);
        pnlTop.setPreferredSize(new Dimension(0, 50));
        pnlTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlTop.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        return pnlTop;
    }

    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CustomUI.white);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 6;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 30);
        panel.add(createAvatarPanel(), gbc);

        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 1;
        gbcLabel.weightx = 0.0;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(8, 10, 8, 10);

        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.gridx = 2;
        gbcField.weightx = 0.7;
        gbcField.fill = GridBagConstraints.HORIZONTAL;
        gbcField.anchor = GridBagConstraints.WEST;
        gbcField.insets = new Insets(8, 10, 8, 10);


        gbcField.gridy = 0;
        gbcField.gridwidth = 2;
        lblChucVu = new JLabel("CHỨC VỤ");
        lblChucVu.setFont(FONT_ROLE);
        lblChucVu.putClientProperty(FlatClientProperties.STYLE,
                "background: #DBEAFE;" +
                        "foreground: #1E40AF;" +
                        "arc: 999;" +
                        "border: 8,12,8,12;");
        panel.add(lblChucVu, gbcField);

        gbcLabel.gridy = 1;
        panel.add(createLabel("Tên:"), gbcLabel);
        gbcField.gridy = 1;
        gbcField.gridwidth = 1;
        txtTen = createTextField();
        txtTen.setEditable(false);
        panel.add(txtTen, gbcField);

        gbcLabel.gridy = 2;
        panel.add(createLabel("CCCD:"), gbcLabel);
        gbcField.gridy = 2;
        txtCCCD = createTextField();
        txtCCCD.setEditable(false);
        panel.add(txtCCCD, gbcField);

        gbcLabel.gridy = 3;
        panel.add(createLabel("Ngày sinh:"), gbcLabel);
        gbcField.gridy = 3;
        txtNgaySinh = createTextField();
        txtNgaySinh.setEditable(false);
        txtNgaySinh.setFont(FONT_FIELD);
        panel.add(txtNgaySinh, gbcField);

        gbcLabel.gridy = 4;
        panel.add(createLabel("Số điện thoại:"), gbcLabel);
        gbcField.gridy = 4;
        txtSDT = createTextField();
        txtSDT.setEditable(false);
        panel.add(txtSDT, gbcField);

        gbcField.gridy = 5;
        gbcField.gridwidth = 2;
        gbcField.anchor = GridBagConstraints.EAST;
        gbcField.fill = GridBagConstraints.NONE;
        panel.add(createButtonPanel(), gbcField);

        return panel;
    }

    private JPanel createAvatarPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(CustomUI.white);
        panel.setOpaque(false);

        pnlAvatar = new CircularImagePanel();
        pnlAvatar.setPreferredSize(new Dimension(200, 200));

        pnlAvatar.setBackground(CustomUI.white);

        try {
            ImageIcon defaultIcon = new ImageIcon(getClass().getResource("/images/default_icon.png"));
            pnlAvatar.setImage(defaultIcon.getImage());
        } catch (Exception e) {
            pnlAvatar.setBackground(Color.LIGHT_GRAY);
        }

        btnThayAnh = new JButton("Thay ảnh");
        btnThayAnh.setFont(FONT_LABEL);
        btnThayAnh.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        panel.add(pnlAvatar, BorderLayout.CENTER);
        panel.add(btnThayAnh, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(CustomUI.white);

        btnDoiMatKhau = new JButton("Đổi mật khẩu");
        btnDoiMatKhau.setFont(FONT_LABEL);
        btnDoiMatKhau.setBackground(Color.decode("#E5E7EB"));
        btnDoiMatKhau.setForeground(Color.BLACK);
        btnDoiMatKhau.putClientProperty(FlatClientProperties.STYLE, "arc: 10");

        panel.add(btnDoiMatKhau);
        return panel;
    }
    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        return lbl;
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setFocusable(false);
        txt.setFont(FONT_FIELD);
        txt.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        return txt;
    }

    private static class CircularImagePanel extends JPanel {
        private Image image;

        public void setImage(Image image) {
            this.image = image;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                int diameter = Math.min(getWidth(), getHeight());
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Ellipse2D.Float clipShape = new Ellipse2D.Float(x, y, diameter, diameter);
                g2.setClip(clipShape);
                g2.drawImage(image, x, y, diameter, diameter, this);
                g2.setColor(Color.LIGHT_GRAY);
                g2.setStroke(new BasicStroke(2));
                g2.draw(clipShape);

                g2.dispose();
            }
        }
    }


    private void loadCurrentUserData() {
        try {
            this.currentUser = nhanVienDAO.layThongTinNV(maNhanVien);

            String tenChucVu = currentUser.getChucVu();
            tenChucVu = convertMaChucVuToTen(tenChucVu);

            if (this.currentUser != null) {
                txtTen.setText(currentUser.getTenNhanVien());
                txtCCCD.setText(currentUser.getCCCD());
                txtSDT.setText(currentUser.getSoDienThoai());

                if (currentUser.getNgaySinh() != null) {
                    LocalDate localDate = currentUser.getNgaySinh().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    DateTimeFormatter format1 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    txtNgaySinh.setText(localDate.format(format1));
                }

                lblChucVu.setText(tenChucVu.toUpperCase());

//                byte[] imageBytes = currentUser.getAnhNhanVien();
//
//                if (imageBytes != null && imageBytes.length > 0) {
//                    // Nếu có ảnh trong DB -> Tải ảnh từ mảng byte[]
//                    ImageIcon avatarIcon = new ImageIcon(imageBytes);
//                    pnlAvatar.setImage(avatarIcon.getImage());
//                } else {
//                    // Nếu không có ảnh -> Tải ảnh mặc định TỪ RESOURCES
//                    ImageIcon defaultIcon = new ImageIcon(getClass().getResource("/images/default_icon.png"));
//                    pnlAvatar.setImage(defaultIcon.getImage());
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải thông tin cá nhân.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String convertMaChucVuToTen(String maChucVu) {
        if (maChucVu == null) return "Lễ tân";
        return switch (maChucVu.trim().toUpperCase()) {
            case "CV001" -> "Lễ tân";
            case "CV002" -> "Quản lý";
            case "CV003" -> "Admin";
            default -> "Lễ tân";
        };
    }

    private void addActions() {
        btnThayAnh.addActionListener(this::onChooseImage);
        btnDoiMatKhau.addActionListener(this::DoiMatKhau);
    }

    private void onChooseImage(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn ảnh đại diện");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh (*.png, *.jpg, *.jpeg)", "png", "jpg", "jpeg"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            this.selectedAvatarFile = fileChooser.getSelectedFile();
            ImageIcon icon = new ImageIcon(selectedAvatarFile.getAbsolutePath());
            pnlAvatar.setImage(icon.getImage());
        }
    }

    private void DoiMatKhau(ActionEvent e) {
         PasswordChangeDialog dialog = new PasswordChangeDialog((Frame) SwingUtilities.getWindowAncestor(this), this.maNhanVien);
         dialog.setVisible(true);

    }

}