import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.text.JTextComponent;

@SuppressWarnings("serial")
public class MailManagerFrame extends JFrame {
    private JPanel categoriesPanel;
    private JPanel mailList;
    private JLabel unreadCountLabel;
    private JTextField searchField;
    private JPanel accountPanel;
    private boolean accountMenuVisible = false;
    private boolean dateFilterVisible = false;
    private ArrayList<Mail> mails = new ArrayList<>();
    private String currentAccount = "firstuser@gmail.com";
    private JPanel sidebar;
    private JComboBox<String> dayBox, monthBox, yearBox;
    private JButton filterButton;
    private JButton refreshButton;

    public MailManagerFrame() {
        setTitle("Mail Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Ajouter des mails avec date
        mails.add(new Mail("Emma White", "Hello, here is the mail...", false, LocalDate.of(2025, 5, 17)));
        mails.add(new Mail("John Doe", "Meeting tomorrow", true, LocalDate.of(2025, 5, 16)));
        mails.add(new Mail("Alice Smith", "Project update", false, LocalDate.of(2025, 5, 15)));
        mails.add(new Mail("Bob Brown", "Lunch plans", true, LocalDate.of(2025, 5, 17)));
        mails.add(new Mail("Carol Green", "Invoice attached", false, LocalDate.of(2025, 5, 14)));
        mails.add(new Mail("David Black", "Happy Birthday!", true, LocalDate.of(2025, 5, 13)));
        mails.add(new Mail("Eve White", "Check this out", false, LocalDate.of(2025, 5, 16)));

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ThemeManager.getBackgroundColor());

        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 600));
        sidebar.setBackground(ThemeManager.getSidebarColor());

        JLabel titleLabel = new JLabel("\uD83D\uDCEC Mail Manager");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 0));
        titleLabel.setForeground(ThemeManager.getTextColor());
        sidebar.add(titleLabel);

        sidebar.add(makeSidebarButton("\uD83D\uDCE8 Compose", e -> showComposePanel()));
        sidebar.add(makeSidebarButton("\uD83D\uDCE5 Inbox"));
        sidebar.add(makeSidebarButton("\uD83D\uDCE4 Sent"));
        sidebar.add(makeSidebarButton("\uD83D\uDCDD Drafts"));
        sidebar.add(makeSidebarButton("\uD83D\uDDD1 Trash"));
        sidebar.add(makeSidebarButton("\uD83D\uDCE6 Archived"));

        sidebar.add(Box.createVerticalStrut(20));

        JButton categoriesToggle = makeSidebarButton("\uD83D\uDCC2 Categories \u25B8");
        sidebar.add(categoriesToggle);

        categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new BoxLayout(categoriesPanel, BoxLayout.Y_AXIS));
        categoriesPanel.setBackground(ThemeManager.getSidebarColor());
        categoriesPanel.setVisible(false);
        categoriesPanel.add(makeSidebarButton("\uD83D\uDFE2 Personal"));
        categoriesPanel.add(makeSidebarButton("\uD83D\uDD35 Social"));
        categoriesPanel.add(makeSidebarButton("\uD83D\uDD34 Urgent"));
        categoriesPanel.add(makeSidebarButton("\uD83D\uDFE3 Promotion"));
        sidebar.add(categoriesPanel);

        categoriesToggle.addActionListener(e -> {
            boolean visible = categoriesPanel.isVisible();
            categoriesPanel.setVisible(!visible);
            categoriesToggle.setText(visible ? "\uD83D\uDCC2 Categories \u25B8" : "\uD83D\uDCC2 Categories \u25BE");
            sidebar.revalidate();
        });

        sidebar.add(Box.createVerticalStrut(20));

        JButton dateToggle = makeSidebarButton("\uD83D\uDD0D Filter by date:", e -> toggleDateFilter());
        sidebar.add(dateToggle);

        dayBox = new JComboBox<>(createNumberArray(1, 31));
        monthBox = new JComboBox<>(createNumberArray(1, 12));
        yearBox = new JComboBox<>(createYearArray());

        dayBox.setMaximumSize(new Dimension(60, 25));
        monthBox.setMaximumSize(new Dimension(60, 25));
        yearBox.setMaximumSize(new Dimension(80, 25));

        filterButton = new JButton("Filter");
        filterButton.setMaximumSize(new Dimension(100, 25));
        filterButton.setVisible(false);
        filterButton.addActionListener(e -> applyDateFilter());

        refreshButton = new JButton("Refresh");
        refreshButton.setMaximumSize(new Dimension(100, 25));
        refreshButton.setVisible(false);
        refreshButton.addActionListener(e -> {
            refreshMailList(mails);
            unreadCountLabel.setText(String.valueOf(countUnreadMails()));
        });

        sidebar.add(dayBox);
        sidebar.add(monthBox);
        sidebar.add(yearBox);
        sidebar.add(filterButton);
        sidebar.add(refreshButton);

        dayBox.setVisible(false);
        monthBox.setVisible(false);
        yearBox.setVisible(false);

        mainPanel.add(sidebar, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(ThemeManager.getBackgroundColor());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ThemeManager.getTopBarColor());
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        searchField = new JTextField("\uD83D\uDD0D Search...");
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setForeground(Color.GRAY);
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("\uD83D\uDD0D Search...")) {
                    searchField.setText("");
                    searchField.setForeground(ThemeManager.getTextColor());
                }
            }

            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("\uD83D\uDD0D Search...");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        topBar.add(searchField, BorderLayout.CENTER);

        JPanel accountModePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        accountModePanel.setOpaque(false);

        JButton accountCircle = new JButton("\u26AA");
        accountCircle.setPreferredSize(new Dimension(50, 30));
        accountCircle.addActionListener(e -> toggleAccountMenu(topBar));
        accountModePanel.add(accountCircle);

        JButton switchModeButton = new JButton("\uD83C\uDFA8 Mode");
        switchModeButton.addActionListener(e -> {
            ThemeManager.toggleTheme();
            dispose();
            new MailManagerFrame().setVisible(true);
        });
        accountModePanel.add(switchModeButton);
        topBar.add(accountModePanel, BorderLayout.EAST);

        rightPanel.add(topBar, BorderLayout.NORTH);

        JPanel unreadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        unreadPanel.setBackground(ThemeManager.getTopBarColor());
        unreadPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 0));
        JLabel unreadIcon = new JLabel("\uD83D\uDCE9 Unread:");
        unreadIcon.setFont(new Font("Arial", Font.BOLD, 14));
        unreadIcon.setForeground(ThemeManager.getTextColor());
        unreadCountLabel = new JLabel(String.valueOf(countUnreadMails()));
        unreadCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        unreadCountLabel.setForeground(Color.RED);
        unreadPanel.add(unreadIcon);
        unreadPanel.add(unreadCountLabel);
        rightPanel.add(unreadPanel, BorderLayout.CENTER);

        mailList = new JPanel();
        mailList.setLayout(new BoxLayout(mailList, BoxLayout.Y_AXIS));
        mailList.setBackground(ThemeManager.getBackgroundColor());
        refreshMailList(mails);

        JScrollPane scrollPane = new JScrollPane(mailList);
        scrollPane.setBorder(null);
        rightPanel.add(scrollPane, BorderLayout.SOUTH);

        mainPanel.add(rightPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private void toggleDateFilter() {
        dateFilterVisible = !dateFilterVisible;
        dayBox.setVisible(dateFilterVisible);
        monthBox.setVisible(dateFilterVisible);
        yearBox.setVisible(dateFilterVisible);
        filterButton.setVisible(dateFilterVisible);
        refreshButton.setVisible(dateFilterVisible);
        sidebar.revalidate();
        sidebar.repaint();
    }

    private void applyDateFilter() {
        try {
            int day = Integer.parseInt((String) dayBox.getSelectedItem());
            int month = Integer.parseInt((String) monthBox.getSelectedItem());
            int year = Integer.parseInt((String) yearBox.getSelectedItem());
            LocalDate selectedDate = LocalDate.of(year, month, day);

            ArrayList<Mail> filtered = new ArrayList<>();
            for (Mail mail : mails) {
                if (mail.date.equals(selectedDate)) {
                    filtered.add(mail);
                }
            }

            refreshMailList(filtered);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Date invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String[] createNumberArray(int start, int end) {
        String[] arr = new String[end - start + 1];
        for (int i = 0; i < arr.length; i++) arr[i] = String.valueOf(start + i);
        return arr;
    }

    private String[] createYearArray() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[10];
        for (int i = 0; i < 10; i++) years[i] = String.valueOf(currentYear - i);
        return years;
    }

    private void toggleAccountMenu(JPanel parent) {
    if (accountMenuVisible) {
        parent.remove(accountPanel);
        accountPanel = null;
        accountMenuVisible = false;
        parent.revalidate();
        parent.repaint();
    } else {
        accountPanel = new JPanel();
        accountPanel.setLayout(new BoxLayout(accountPanel, BoxLayout.Y_AXIS));
        accountPanel.setBackground(ThemeManager.getTopBarColor());
        accountPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        Color textColor = ThemeManager.getTextColor();

        JLabel nameLabel = new JLabel("Nom: User");
        nameLabel.setForeground(textColor);
        accountPanel.add(nameLabel);

        JLabel emailLabel = new JLabel("Email: " + currentAccount);
        emailLabel.setForeground(textColor);
        accountPanel.add(emailLabel);

        JButton logoutBtn = new JButton("Se déconnecter");
        logoutBtn.setForeground(textColor);
        logoutBtn.setBackground(ThemeManager.getButtonColor());
        logoutBtn.addActionListener(e -> {
            currentAccount = null;
            dispose();
            new AppLoginFrame().setVisible(true);
        });
        accountPanel.add(logoutBtn);

        JButton addBtn = new JButton("Ajouter compte");
        addBtn.setForeground(textColor);
        addBtn.setBackground(ThemeManager.getButtonColor());
        addBtn.addActionListener(e -> {
            new AppLoginFrame().setVisible(true);
        });
        accountPanel.add(addBtn);

        parent.add(accountPanel, BorderLayout.AFTER_LAST_LINE);
        parent.revalidate();
        parent.repaint();
        accountMenuVisible = true;
    }
}


   private void showComposePanel() {
    JFrame composeFrame = new JFrame("Composer un mail");
    composeFrame.setSize(450, 350);
    composeFrame.setLocationRelativeTo(this);
    composeFrame.setLayout(new BorderLayout(10, 10));
    composeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    JPanel fieldsPanel = new JPanel();
    fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
    fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JTextField destField = new JTextField();
    addPlaceholder(destField, "Destinataire");
    destField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

    JTextField subjectField = new JTextField();
    addPlaceholder(subjectField, "Sujet");
    subjectField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

    JTextArea bodyArea = new JTextArea();
    addPlaceholder(bodyArea, "Objet");
    bodyArea.setLineWrap(true);
    bodyArea.setWrapStyleWord(true);
    JScrollPane scrollPane = new JScrollPane(bodyArea);
    scrollPane.setPreferredSize(new Dimension(400, 150));

    fieldsPanel.add(destField);
    fieldsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    fieldsPanel.add(subjectField);
    fieldsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    fieldsPanel.add(scrollPane);

    JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton attachBtn = new JButton("Joindre fichier");
    JButton sendBtn = new JButton("Envoyer");

    sendBtn.addActionListener(e -> {
        String to = destField.getText().trim();
        String subject = subjectField.getText().trim();

        if (to.isEmpty() || to.equals("Destinataire") || subject.isEmpty() || subject.equals("Sujet")) {
            JOptionPane.showMessageDialog(composeFrame,
                    "Veuillez remplir les champs 'Destinataire' et 'Sujet'.",
                    "Champs requis",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!to.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            JOptionPane.showMessageDialog(composeFrame,
                    "Adresse e-mail invalide.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ici on peut intégrer la logique d'envoi réelle
        JOptionPane.showMessageDialog(composeFrame,
                "Message envoyé à " + to + " !",
                "Envoi réussi",
                JOptionPane.INFORMATION_MESSAGE);

        composeFrame.dispose();
    });

    buttonsPanel.add(attachBtn);
    buttonsPanel.add(sendBtn);

    composeFrame.add(fieldsPanel, BorderLayout.CENTER);
    composeFrame.add(buttonsPanel, BorderLayout.SOUTH);
    composeFrame.setVisible(true);
}

private void addPlaceholder(JTextComponent comp, String placeholder) {
    comp.setForeground(Color.GRAY);
    comp.setText(placeholder);

    comp.addFocusListener(new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            if (comp.getText().equals(placeholder)) {
                comp.setText("");
                comp.setForeground(ThemeManager.getTextColor());
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if (comp.getText().isEmpty()) {
                comp.setForeground(Color.GRAY);
                comp.setText(placeholder);
            }
        }
    });
}


    private void refreshMailList(ArrayList<Mail> mailSource) {
        mailList.removeAll();
        for (Mail mail : mailSource) {
            JPanel mailItem = new JPanel(new BorderLayout());
            mailItem.setBackground(ThemeManager.getItemBackground());
            mailItem.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel iconLabel = new JLabel(mail.isRead ? "✉️" : "📨");
            JLabel sender = new JLabel(mail.sender);
            sender.setFont(new Font("Arial", Font.BOLD, 14));
            sender.setForeground(ThemeManager.getTextColor());

            JLabel snippet = new JLabel(mail.snippet + " (" + mail.date.toString() + ")");
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
                public void mouseClicked(MouseEvent e) {
                    if (!mail.isRead) {
                        mail.isRead = true;
                        refreshMailList(mails);
                        unreadCountLabel.setText(String.valueOf(countUnreadMails()));
                    }
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
        for (Mail mail : mails) if (!mail.isRead) count++;
        return count;
    }

    private JButton makeSidebarButton(String text) {
        return makeSidebarButton(text, null);
    }

    private JButton makeSidebarButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setBackground(ThemeManager.getSidebarColor());
        button.setForeground(ThemeManager.getTextColor());
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        if (action != null) button.addActionListener(action);
        return button;
    }

    private static class Mail {
        String sender, snippet;
        boolean isRead;
        LocalDate date;

        Mail(String sender, String snippet, boolean isRead, LocalDate date) {
            this.sender = sender;
            this.snippet = snippet;
            this.isRead = isRead;
            this.date = date;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MailManagerFrame().setVisible(true));
    }
}
