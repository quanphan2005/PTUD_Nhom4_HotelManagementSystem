package vn.iuh.gui.panel;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import vn.iuh.constraint.EntityIDSymbol;
import vn.iuh.dao.PhienDangNhapDAO;
import vn.iuh.dao.TaiKhoanDAO;
import vn.iuh.dto.event.create.LoginEvent;
import vn.iuh.entity.PhienDangNhap;
import vn.iuh.entity.TaiKhoan;
import vn.iuh.gui.base.Main;
import vn.iuh.service.AccountService;
import vn.iuh.service.impl.AccountServiceImpl;
import vn.iuh.util.EntityUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.Timestamp;

public class LoginPanelV2 extends JPanel implements ActionListener {
    private final Main main;
    private final JButton btnLogin;
    private final JLabel lblForgotPassword;
    private final PlaceholderTextField txtUser;
    private final PlaceholderPassword txtPass;
    private final PhienDangNhapDAO phienDangNhapDao;
    private final TaiKhoanDAO taiKhoanDAO;

    public LoginPanelV2(Main main) {
        this.main = main;
        this.phienDangNhapDao = new PhienDangNhapDAO();
        this.taiKhoanDAO = new TaiKhoanDAO();

        setLayout(new GridBagLayout());

//         Create the white card panel
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setPreferredSize(new Dimension(950, 550));

        // Top panel - Hotel name
        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));
        JLabel lblHotelName = new JLabel("Hai Quân Đức Thịnh", SwingConstants.CENTER);
        lblHotelName.setFont(new Font("Serif", Font.BOLD, 36));
        lblHotelName.setForeground(new Color(50, 50, 50));
        topPanel.add(lblHotelName);

        // Center panel - Split into left (image with decorations) and right (form)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 40, 50));

        // Left panel - Image with decorative elements
        JPanel leftPanel = new LeftImagePanel();
        leftPanel.setBackground(Color.WHITE);

        // Right panel - Login form
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Form container
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(Color.WHITE);

        // Title
        JLabel lblTitle = new JLabel("THÔNG TIN ĐĂNG NHẬP");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(new Color(50, 50, 50));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        formContainer.add(Box.createRigidArea(new Dimension(0, 30)));
        formContainer.add(lblTitle);
        formContainer.add(Box.createRigidArea(new Dimension(0, 35)));

        // Username field with icon
        JPanel userPanel = new JPanel(new BorderLayout(12, 0));
        userPanel.setBackground(new Color(240, 240, 240));
        userPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        userPanel.setMaximumSize(new Dimension(350, 50));

        JLabel userIcon = new JLabel("\u2709");
        userIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        userIcon.setForeground(new Color(150, 150, 150));

        txtUser = new PlaceholderTextField("Email");
        txtUser.setFont(new Font("Arial", Font.PLAIN, 15));
        txtUser.setBorder(BorderFactory.createEmptyBorder());
        txtUser.setBackground(new Color(240, 240, 240));

        userPanel.add(userIcon, BorderLayout.WEST);
        userPanel.add(txtUser, BorderLayout.CENTER);

        formContainer.add(userPanel);
        formContainer.add(Box.createRigidArea(new Dimension(0, 18)));

        // Password field with icon
        JPanel passPanel = new JPanel(new BorderLayout(12, 0));
        passPanel.setBackground(new Color(240, 240, 240));
        passPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        passPanel.setMaximumSize(new Dimension(350, 50));

        JLabel passIcon = new JLabel("\uD83D\uDD12");
        passIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 18));
        passIcon.setForeground(new Color(150, 150, 150));

        txtPass = new PlaceholderPassword("Password");
        txtPass.setFont(new Font("Arial", Font.PLAIN, 15));
        txtPass.setBorder(BorderFactory.createEmptyBorder());
        txtPass.setBackground(new Color(240, 240, 240));

        passPanel.add(passIcon, BorderLayout.WEST);
        passPanel.add(txtPass, BorderLayout.CENTER);

        formContainer.add(passPanel);
        formContainer.add(Box.createRigidArea(new Dimension(0, 30)));

        // Login button
        btnLogin = new RoundedButton("LOGIN");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(350, 50));
        btnLogin.setPreferredSize(new Dimension(350, 50));
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(102, 204, 102));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(this);

        // Add hover effect
        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(new Color(82, 184, 82));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(new Color(102, 204, 102));
            }
        });

        formContainer.add(btnLogin);
        formContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        // Forgot password link
        lblForgotPassword = new JLabel("Forgot Username / Password?");
        lblForgotPassword.setFont(new Font("Arial", Font.PLAIN, 12));
        lblForgotPassword.setForeground(new Color(170, 170, 170));
        lblForgotPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblForgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lblForgotPassword.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblForgotPassword.setForeground(new Color(120, 120, 120));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblForgotPassword.setForeground(new Color(170, 170, 170));
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                JOptionPane.showMessageDialog(LoginPanelV2.this,
                    "Vui lòng liên hệ quản trị viên để khôi phục mật khẩu.",
                    "Khôi phục mật khẩu",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });

        formContainer.add(lblForgotPassword);
        formContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        rightPanel.add(formContainer, BorderLayout.CENTER);

        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        cardPanel.add(topPanel, BorderLayout.NORTH);
        cardPanel.add(centerPanel, BorderLayout.CENTER);

        add(cardPanel);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Create gradient background (purple to blue)
        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, new Color(147, 112, 219), w, h, new Color(75, 101, 224));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o.equals(btnLogin)) {
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword());

            // Check if fields are empty or contain placeholder text
            if(username.isEmpty() || username.equals("Email")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đăng nhập", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if(password.isEmpty() || password.equals("Password")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LoginEvent loginEvent = new LoginEvent(username, password);
            AccountService accountService = new AccountServiceImpl();
            if(accountService.handleLogin(loginEvent)){
                PhienDangNhap phienDangNhapMoiNhat = phienDangNhapDao.timPhienDangNhapMoiNhat();
                String maPhienDangNhapMoiNhat = phienDangNhapMoiNhat.getMaPhienDangNhap();
                String newMaPhienDangNhap = EntityUtil.increaseEntityID(maPhienDangNhapMoiNhat,
                        EntityIDSymbol.LOGIN_SESSION.getPrefix(),
                        EntityIDSymbol.LOGIN_SESSION.getLength());

                TaiKhoan tk = taiKhoanDAO.timTaiKhoanBangUserName(username);
                PhienDangNhap phienDangNhap = new PhienDangNhap();
                phienDangNhap.setMaPhienDangNhap(newMaPhienDangNhap);
                phienDangNhap.setSoQuay(1);
                phienDangNhap.setTgBatDau(new Timestamp(System.currentTimeMillis()));
                phienDangNhap.setTgKetThuc(null);
                phienDangNhap.setMaTaiKhoan(tk.getMaTaiKhoan());

                phienDangNhapDao.themPhienDangNhap(phienDangNhap);
                Main.setCurrenLoginSession(newMaPhienDangNhap);

                Main.showRootCard("MainUI");
            }else{
                JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không đúng", "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Custom panel for left side with image and decorative elements
    static class LeftImagePanel extends JPanel {
        private Image userImage;

        public LeftImagePanel() {
            setLayout(new GridBagLayout());
            setBackground(Color.WHITE);

            // Try to load user icon or create placeholder
            try {
                java.net.URL imgURL = getClass().getResource("/icons/abc.png");
                if (imgURL != null) {
                    ImageIcon icon = new ImageIcon(imgURL);
                    userImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                }
            } catch (Exception e) {
                // Will draw custom icon if image not found
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            // Draw large circle background
            g2d.setColor(new Color(245, 245, 245));
            g2d.fill(new Ellipse2D.Double(centerX - 130, centerY - 130, 260, 260));

            // Draw decorative circles around
            drawDecorativeCircle(g2d, centerX - 180, centerY - 50, 15, new Color(100, 200, 255));
            drawDecorativeCircle(g2d, centerX + 165, centerY + 20, 12, new Color(100, 200, 255));
            drawDecorativeCircle(g2d, centerX - 160, centerY + 100, 10, new Color(200, 220, 100));
            drawDecorativeCircle(g2d, centerX + 150, centerY - 80, 8, new Color(150, 200, 150));

            // Draw user icon card in center
            g2d.setColor(new Color(70, 90, 110));
            g2d.fill(new RoundRectangle2D.Double(centerX - 80, centerY - 60, 160, 120, 15, 15));

            // Draw user icon
            if (userImage != null) {
                g2d.drawImage(userImage, centerX - 40, centerY - 40, 80, 80, null);
            } else {
                // Draw simple user icon
                g2d.setColor(new Color(180, 190, 200));
                // Head
                g2d.fill(new Ellipse2D.Double(centerX - 20, centerY - 30, 40, 40));
                // Body
                g2d.fill(new RoundRectangle2D.Double(centerX - 30, centerY + 15, 60, 35, 20, 20));
            }
        }

        private void drawDecorativeCircle(Graphics2D g2d, int x, int y, int size, Color color) {
            g2d.setColor(color);
            g2d.fill(new Ellipse2D.Double(x, y, size, size));
        }
    }

    // Rounded button
    static class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) {
                g2.setColor(getBackground().darker());
            } else {
                g2.setColor(getBackground());
            }

            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
            g2.dispose();

            super.paintComponent(g);
        }
    }

    // Placeholder text field
    static class PlaceholderTextField extends JTextField {
        private final String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setForeground(new Color(180, 180, 180));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (getText().equals(placeholder)) {
                        setText("");
                        setForeground(new Color(50, 50, 50));
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(new Color(180, 180, 180));
                    }
                }
            });

            setText(placeholder);
        }
    }

    // Placeholder password field
    static class PlaceholderPassword extends JPasswordField {
        private final String placeholder;

        public PlaceholderPassword(String placeholder) {
            this.placeholder = placeholder;
            setForeground(new Color(180, 180, 180));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (String.valueOf(getPassword()).equals(placeholder)) {
                        setText("");
                        setForeground(new Color(50, 50, 50));
                        setEchoChar('●');
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getPassword().length == 0) {
                        setText(placeholder);
                        setForeground(new Color(180, 180, 180));
                        setEchoChar((char) 0);
                    }
                }
            });

            setText(placeholder);
            setEchoChar((char) 0);
        }
    }
}

