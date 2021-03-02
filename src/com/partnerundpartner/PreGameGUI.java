package com.partnerundpartner;

import javax.swing.*;

public class PreGameGUI extends JFrame {
	private JPanel mainPanel;
	private JLabel lblTitle;
	private JLabel lblDifficulty;
	private JComboBox cbxDifficulty;
	private JButton btnStartGame;

	public boolean gameStarted;
	public final Object lock = new Object();

	public PreGameGUI(String title) {
		super(title);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		setContentPane(mainPanel);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		pack();

		//Component listeners
		btnStartGame.addActionListener(e -> {
			//Signal to main() that the game has started
			synchronized (lock) {
				gameStarted = true;
				lock.notifyAll();
			}

			//Close the window
			setVisible(false);
			dispose();
		});
	}
}