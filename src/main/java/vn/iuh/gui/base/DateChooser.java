package vn.iuh.gui.base;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.Locale;

public class DateChooser extends JPanel {
    private JLabel lblToday;
    private DatePicker datePicker;

    public DateChooser() {
        setPreferredSize(new Dimension(250, 35));
        setLayout(new BorderLayout(5, 0));
        setBackground(Color.white);
        setOpaque(false);

        // Label "Hôm nay"
        lblToday = new JLabel("Hôm nay");
        lblToday.setFont(lblToday.getFont().deriveFont(Font.BOLD));
        lblToday.setBorder(new EmptyBorder(0, 8, 0, 8));
        lblToday.setForeground(Color.DARK_GRAY);
        lblToday.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        DatePickerSettings settings = new DatePickerSettings(new Locale("vi", "VN"));
        settings.setAllowKeyboardEditing(false);
        settings.setFormatForDatesCommonEra("dd 'thg' MM, yyyy");
        settings.setFontValidDate(new Font("Segoe UI", Font.PLAIN, 16));
        settings.setFontInvalidDate(new Font("Segoe UI", Font.PLAIN, 16));
        settings.setFontVetoedDate(new Font("Segoe UI", Font.PLAIN, 16));


        datePicker = new DatePicker(settings);
        datePicker.setDate(LocalDate.now());
        datePicker.getComponentDateTextField().setOpaque(false);
        datePicker.getComponentDateTextField().setBorder(null);
        datePicker.getComponentDateTextField().setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
//
        lblToday.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                datePicker.setDate(LocalDate.now());
            }
        });


        add(lblToday, BorderLayout.WEST);
        add(datePicker, BorderLayout.CENTER);
    }


    public LocalDate getDate() {
        return datePicker.getDate();
    }

    public void setDate(LocalDate date) {
        datePicker.getComponentDateTextField().setFont(new Font("Segoe UI", Font.PLAIN, 16));
        datePicker.setDate(date);
    }
    @Override
    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(CustomUI.lightBlue);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2.dispose();
        super.paintComponent(grphcs);
    }
}
