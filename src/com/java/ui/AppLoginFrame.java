package com.java.ui;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class AppLoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;

    public AppLoginFrame() {
        setTitle("Mail Manager - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 300);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(ThemeManager.getBackgroundColor());

        JLabel titleLabel = new JLabel("📧 Mail Manager");
        titleLabel.setBounds(90, 10, 200, 30);
        titleLabel.setForeground(ThemeManager.getTextColor());
        panel.add(titleLabel);

        emailField = new JTextField();
        emailField.setBounds(50, 50, 230, 30);
        emailField.setBorder(BorderFactory.createTitledBorder("Email Gmail"));
        emailField.setBackground(ThemeManager.getItemBackground());
        emailField.setForeground(ThemeManager.getTextColor());
        panel.add(emailField);

        passwordField = new JPasswordField();
        passwordField.setBounds(50, 100, 230, 30);
        passwordField.setBorder(BorderFactory.createTitledBorder("Mot de passe"));
        passwordField.setBackground(ThemeManager.getItemBackground());
        passwordField.setForeground(ThemeManager.getTextColor());
        panel.add(passwordField);

        JLabel forgotPassword = new JLabel("<HTML><U>Mot de passe ?</U></HTML>");
        forgotPassword.setBounds(50, 135, 120, 20);
        forgotPassword.setForeground(ThemeManager.getLinkColor());
        forgotPassword.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(forgotPassword);

        JButton loginButton = new JButton("Se connecter");
        loginButton.setBounds(50, 160, 230, 35);
        loginButton.setBackground(ThemeManager.getButtonColor());
        loginButton.setForeground(Color.WHITE);
        panel.add(loginButton);

        JLabel signUp = new JLabel("<HTML><U>Inscrivez-vous</U></HTML>");
        signUp.setBounds(120, 200, 100, 20);
        signUp.setForeground(ThemeManager.getLinkColor());
        signUp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(signUp);

        JButton switchModeButton = new JButton("🎨 Mode");
        switchModeButton.setBounds(250, 10, 80, 25);
        panel.add(switchModeButton);

        // 🎨 Mode clair/sombre
        switchModeButton.addActionListener(e -> {
            ThemeManager.toggleTheme();
            dispose();
            new AppLoginFrame().setVisible(true);
        });

        // 🔐 Connexion
        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } else if (!email.endsWith("@gmail.com")) {
                JOptionPane.showMessageDialog(this, "Veuillez entrer une adresse Gmail valide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } else {
                dispose();
                new MailManagerFrame().setVisible(true);
            }
        });

        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppLoginFrame().setVisible(true));
    }
}