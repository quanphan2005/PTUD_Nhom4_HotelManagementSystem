package vn.iuh.gui.panel;

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
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Timestamp;
import java.util.Arrays;

public class LoginPanel extends JPanel implements ActionListener {
    private Main main;
    private final JButton btnLogin;
    private final JButton btnExit;
    PlaceholderTextField txtUser;
    PlaceholderPassword txtPass;
    private PhienDangNhapDAO phienDangNhapDao;
    private TaiKhoanDAO taiKhoanDAO;

    public LoginPanel(Main main) {
        this.main = main;
        this.phienDangNhapDao = new PhienDangNhapDAO();
        this.taiKhoanDAO = new TaiKhoanDAO();

        setLayout(new GridLayout(1, 2));
        setBackground(Color.WHITE);

        JPanel leftPanel = new JPanel(new BorderLayout());
//        leftPanel.setBackground(new Color(230, 240, 255));
//        JLabel imageLabel = new JLabel("Ảnh", SwingConstants.CENTER);
//        imageLabel.setFont(new Font("Arial", Font.BOLD, 22));
        java.net.URL imgURL = getClass().getResource("/images/khachsan.jpg");
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);

            // Scale ảnh theo kích thước panel
            Image scaledImage = icon.getImage().getScaledInstance(600, 500, Image.SCALE_SMOOTH);

            ImageIcon scaledicon = new ImageIcon(scaledImage);

            JLabel lblImage = new JLabel(scaledicon);

            leftPanel.add(lblImage, BorderLayout.CENTER);
        }

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        // Panel label chạy chữ
        JLabel movingLabel = new JLabel("Khách sạn Hai Quân Đức Thịnh");
        movingLabel.setFont(new Font("Serif", Font.BOLD, 28));
        movingLabel.setForeground(new Color(0, 102, 204));

        JPanel movingPanel = new JPanel(null);
        movingPanel.setPreferredSize(new Dimension(450, 80));
        movingPanel.setBackground(Color.WHITE);
        movingLabel.setBounds(0, 0, 400, 30);
        movingPanel.add(movingLabel);

        Timer timer = new Timer(30, e -> {
            Point p = movingLabel.getLocation();
            p.x += 2;
            if (p.x > movingPanel.getWidth()) {
                p.x = -movingLabel.getPreferredSize().width;
            }
            movingLabel.setLocation(p);
        });
        timer.start();

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(80,100,0 ,100));
        formPanel.setBackground(Color.WHITE);

        txtUser = new PlaceholderTextField("Tên đăng nhập");
        txtUser.setMaximumSize(new Dimension(800, 40));
        txtUser.setFont(new Font("Arial", Font.PLAIN, 18));
        txtUser.setBorder(new RoundedBorder(15));

        txtPass = new PlaceholderPassword("Mật khẩu");
        txtPass.setMaximumSize(new Dimension(800, 40));
        txtPass.setFont(new Font("Arial", Font.PLAIN, 18));
        txtPass.setBorder(new RoundedBorder(15));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnLogin = new JButton("Đăng nhập");
        btnExit = new JButton("Thoát");
        btnExit.addActionListener(this);
        btnLogin.addActionListener(this);

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);

        styleButton(btnLogin);
        styleButton(btnExit);

        formPanel.setBorder(BorderFactory.createEmptyBorder(160, 100, 0, 0));
        formPanel.add(txtUser);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(txtPass);
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        formPanel.add(buttonPanel);

        rightPanel.add(movingPanel, BorderLayout.NORTH);
        rightPanel.add(formPanel, BorderLayout.CENTER);


        add(leftPanel);
        add(rightPanel);
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(new Color(0, 102, 204));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(120, 35));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if(o.equals(btnExit))   System.exit(0);
        else{
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword());
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
                JOptionPane.showMessageDialog(this, "Đăng nhập thất bại");
            }
        }
    }

    static class RoundedBorder extends AbstractBorder {
        private final int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(Color.GRAY);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

//    public static void main(String[] args) {
//        JFrame frame = new JFrame("Login UI");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
//        frame.setSize(800, 500);
//        frame.setLocationRelativeTo(null);
//        frame.add(new LoginPanel());
//        frame.setVisible(true);
//    }

    class PlaceholderTextField extends JTextField {
        private final String placeholder;

        public PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
            setForeground(Color.GRAY);

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (getText().equals(placeholder)) {
                        setText("");
                        setForeground(Color.BLACK);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                    }
                }
            });

            setText(placeholder);
        }
    }

    class PlaceholderPassword extends JPasswordField {
        private final String placeholder;

        public PlaceholderPassword (String placeholder) {
            this.placeholder = placeholder;
            setForeground(Color.GRAY);

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (String.valueOf(getPassword()).equals(placeholder)) {
                        setText("");
                        setForeground(Color.BLACK);
                        setEchoChar('●');
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getPassword().length == 0) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                        setEchoChar((char) 0);
                    }
                }
            });

            setText(placeholder);
            setEchoChar((char) 0);
        }
    }
}
