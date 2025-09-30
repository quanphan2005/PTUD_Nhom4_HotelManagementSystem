package vn.iuh.gui.base;

import net.miginfocom.swing.MigLayout;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTargetAdapter;

import java.awt.*;

public class MenuAnimation {
    public static void showMenu(Component component, MenuItem item, MigLayout layout, boolean show) {
        int height = component.getPreferredSize().height;
        Animator animator = new Animator(300, new TimingTargetAdapter() {
            @Override
            public void timingEvent(float fraction) {
                float f = show ? fraction : 1f - fraction;
                String hStr = String.format("%.0f", height * f);  // làm tròn về integer
                layout.setComponentConstraints(component, "h " + hStr + "!");
                component.revalidate();
                item.repaint();
            }
        });
        animator.setResolution(0);
        animator.setAcceleration(.5f);
        animator.setDeceleration(.5f);
        animator.start();
    }
}