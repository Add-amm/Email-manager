package com.java.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.java.App.App;

@SuppressWarnings("serial")
public class LoginWindow extends JFrame implements ActionListener{
	
	JButton GmailButton;
	JButton YahooButton;
	JButton OutlookButton;
	boolean selected = false;
	
	public LoginWindow() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBackground(Color.WHITE);
		
		
		// Charger le logo
        ImageIcon imageIcon = new ImageIcon("icons/Logos/MailStormWithoutBackground.png");

        // Redimensionner l'image
        Image image = imageIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(image);

        // Ajout
        JLabel imageLabel = new JLabel(resizedIcon);
        imageLabel.setBackground(Color.WHITE);
        
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(imageLabel);
        p.add(Box.createVerticalStrut(15));
		
		GmailButton = new JButton("Continuer avec Gmail");
		GmailButton.addActionListener(this);
		GmailButton.setFocusable(false);
		YahooButton = new JButton("Continuer avec Yahoo");
		YahooButton.setFocusable(false);
		YahooButton.addActionListener(this);
		OutlookButton = new JButton("Continuer avec Outlook");
		OutlookButton.setFocusable(false);
		GmailButton.addActionListener(this);
		
		p.add(GmailButton);
		p.add(Box.createVerticalStrut(15));
		p.add(YahooButton);
		p.add(Box.createVerticalStrut(15));
		p.add(OutlookButton);
		
		setTitle("MailStorm");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 560);
		setResizable(false);
		setLocationRelativeTo(null); // Centrer la fenetre principal
		setVisible(true);
	
		setContentPane(p);
		
		while (!selected) {
			// bloquer l'interface ici pour pas continuer comme si tu es authentifier
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==GmailButton) {
			try {
				App.Authentification("Gmail");
				selected = true;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		if (e.getSource()==YahooButton) {
			try {
				App.Authentification("Yahoo");
				selected = true;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		if (e.getSource()==OutlookButton) {
			try {
				App.Authentification("Outlook");
				selected = true;
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}	
}