package com.java.App;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.java.form.App;
import com.java.form.ButtonPanel;
import com.java.form.GestionnaireNote;
import com.java.form.LabeledTextField;

@SuppressWarnings("serial")
public class App extends JFrame implements ActionListener {
	
	boolean EnterAct = false;
	ButtonPanel listOfButtons;
	LabeledTextField tf1;
	LabeledTextField tf2;
	LabeledTextField tf3;
	JLabel Statut = new JLabel("");
	private GestionnaireNote g = new GestionnaireNote();
	
	public App() {
		exp03();
		setTitle("Mon formulaire");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1100, 800);
		setMinimumSize(new Dimension(730, 600));
		setLocationRelativeTo(null); // Centrer la fenetre principal
		setVisible(true);
	}

	public static void main(String[] args) {
		new App();
	}