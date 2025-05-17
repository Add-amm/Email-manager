package com.java.ui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class MailManagerFrame extends JFrame {

    private JPanel categoriesPanel;
    private JPanel mailList;
    private JLabel unreadCountLabel;

    private ArrayList<Mail> mails = new ArrayList<>();

    public MailManagerFrame() {
        setTitle("Mail Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        mails.add(new Mail("Emma White", "Hello, here is the mail...", false));
        mails.add(new Mail("John Doe", "Meeting tomorrow", true));
        mails.add(new Mail("Alice Smith", "Project update", false));
        mails.add(new Mail("Bob Brown", "Lunch plans", true));
        mails.add(new Mail("Carol Green", "Invoice attached", false));
        mails.add(new Mail("David Black", "Happy Birthday!", true));
        mails.add(new Mail("Eve White", "Check this out", false));

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ThemeManager.getBackgroundColor());

        // ----- SIDEBAR -----
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 600));
        sidebar.setBackground(ThemeManager.getSidebarColor());

        JLabel titleLabel = new JLabel("📧 Mail Manager");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 0));
        titleLabel.setForeground(ThemeManager.getTextColor());
        sidebar.add(titleLabel);

        sidebar.add(makeSidebarButton("📨 Compose"));
        sidebar.add(makeSidebarButton("📥 Inbox"));
        sidebar.add(makeSidebarButton("📤 Sent"));
        sidebar.add(makeSidebarButton("📝 Drafts"));
        sidebar.add(makeSidebarButton("🗑 Trash"));
        sidebar.add(makeSidebarButton("📦 Archived"));

        sidebar.add(Box.createVerticalStrut(20));

        JButton categoriesToggle = makeSidebarButton("📂 Categories ▸");
        sidebar.add(categoriesToggle);

        categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new BoxLayout(categoriesPanel, BoxLayout.Y_AXIS));
        categoriesPanel.setBackground(ThemeManager.getSidebarColor());
        categoriesPanel.setVisible(false);

        categoriesPanel.add(makeSidebarButton("🟢 Personal"));
        categoriesPanel.add(makeSidebarButton("🔵 Social"));
        categoriesPanel.add(makeSidebarButton("🔴 Urgent"));
        categoriesPanel.add(makeSidebarButton("🟣 Promotion"));

        sidebar.add(categoriesPanel);

        categoriesToggle.addActionListener(e -> {
            boolean visible = categoriesPanel.isVisible();
            categoriesPanel.setVisible(!visible);
            categoriesToggle.setText(visible ? "📂 Categories ▸" : "📂 Categories ▾");
            sidebar.revalidate();
        });

        mainPanel.add(sidebar, BorderLayout.WEST);

        // ----- RIGHT PANEL -----
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(ThemeManager.getBackgroundColor());

        // Top bar (Search + Theme Switch)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ThemeManager.getTopBarColor());
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setText("🔍 Search...");
        searchField.setForeground(Color.GRAY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton switchModeButton = new JButton("🎨 Mode");
        switchModeButton.addActionListener(e -> {
            ThemeManager.toggleTheme();
            dispose();
            new MailManagerFrame().setVisible(true);
        });

        topBar.add(searchField, BorderLayout.CENTER);
        topBar.add(switchModeButton, BorderLayout.EAST);

        rightPanel.add(topBar, BorderLayout.NORTH);

        // Unread count
        JPanel unreadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        unreadPanel.setBackground(ThemeManager.getTopBarColor());
        unreadPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 0));

        JLabel unreadIcon = new JLabel("📩 Unread:");
        unreadIcon.setFont(new Font("Arial", Font.BOLD, 14));
        unreadIcon.setForeground(ThemeManager.getTextColor());

        unreadCountLabel = new JLabel(String.valueOf(countUnreadMails()));
        unreadCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        unreadCountLabel.setForeground(Color.RED);

        unreadPanel.add(unreadIcon);
        unreadPanel.add(unreadCountLabel);

        rightPanel.add(unreadPanel, BorderLayout.CENTER);

        // Mail list
        mailList = new JPanel();
        mailList.setLayout(new BoxLayout(mailList, BoxLayout.Y_AXIS));
        mailList.setBackground(ThemeManager.getBackgroundColor());

        refreshMailList();

        JScrollPane scrollPane = new JScrollPane(mailList);
        scrollPane.setBorder(null);

        rightPanel.add(scrollPane, BorderLayout.SOUTH);

        mainPanel.add(rightPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private void refreshMailList() {
        mailList.removeAll();
        for (Mail mail : mails) {
            JPanel mailItem = new JPanel(new BorderLayout());
            mailItem.setBackground(ThemeManager.getItemBackground());
            mailItem.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel iconLabel = new JLabel(mail.isRead ? "✉️" : "📨");

            JLabel sender = new JLabel(mail.sender);
            sender.setFont(new Font("Arial", Font.BOLD, 14));
            sender.setForeground(ThemeManager.getTextColor());

            JLabel snippet = new JLabel(mail.snippet);
            snippet.setForeground(ThemeManager.getTextColor().darker());

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setBackground(ThemeManager.getItemBackground());
            textPanel.add(sender);
            textPanel.add(snippet);

            mailItem.add(iconLabel, BorderLayout.WEST);
            mailItem.add(textPanel, BorderLayout.CENTER);

            mailItem.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            mailItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    mail.isRead = !mail.isRead;
                    refreshMailList();
                    unreadCountLabel.setText(String.valueOf(countUnreadMails()));
                }
            });

            mailList.add(mailItem);
            mailList.add(Box.createVerticalStrut(5));
        }
        mailList.revalidate();
        mailList.repaint();
    }

    private int countUnreadMails() {
        int count = 0;
        for (Mail mail : mails) {
            if (!mail.isRead) count++;
        }
        return count;
    }

    private JButton makeSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setBackground(ThemeManager.getSidebarColor());
        button.setForeground(ThemeManager.getTextColor());
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        return button;
    }

    private static class Mail {
        String sender;
        String snippet;
        boolean isRead;

        Mail(String sender, String snippet, boolean isRead) {
            this.sender = sender;
            this.snippet = snippet;
            this.isRead = isRead;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MailManagerFrame().setVisible(true);
        });
    }
}