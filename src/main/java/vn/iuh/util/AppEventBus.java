package vn.iuh.util;

import javax.swing.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AppEventBus {
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<Runnable>> listeners = new ConcurrentHashMap<>();

    private AppEventBus() {}

    public static void subscribe(String event, Runnable handler) {
        listeners.computeIfAbsent(event, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public static void unsubscribe(String event, Runnable handler) {
        var list = listeners.get(event);
        if (list != null) list.remove(handler);
    }

    public static void publish(String event) {
        var list = listeners.get(event);
        if (list == null) return;
        for (Runnable h : list) {
            // ensure handler runs on EDT
            SwingUtilities.invokeLater(h);
        }
    }
}
