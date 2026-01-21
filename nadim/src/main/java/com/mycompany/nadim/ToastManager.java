package com.mycompany.nadim;

import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.TimeUnit;

public class ToastManager {
    private final long durationNanos;
    private String message;
    private long expiryNanos;

    public ToastManager(long durationMillis) {
        this.durationNanos = TimeUnit.MILLISECONDS.toNanos(durationMillis);
    }

    public synchronized void show(String msg) {
        if (msg == null || msg.isBlank()) {
            message = null;
            expiryNanos = 0L;
            return;
        }
        message = msg;
        expiryNanos = System.nanoTime() + durationNanos;
    }

    public synchronized void draw(Graphics g, int panelWidth, int panelHeight) {
        if (message == null) {
            return;
        }
        if (System.nanoTime() > expiryNanos) {
            message = null;
            expiryNanos = 0L;
            return;
        }
        g.setColor(new Color(0, 0, 0, 150));
        int y = panelHeight - 40;
        g.fillRoundRect(20, y - 20, panelWidth - 40, 30, 8, 8);
        g.setColor(Color.WHITE);
        g.drawString(message, 30, y);
    }
}
