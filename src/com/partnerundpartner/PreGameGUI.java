package com.partnerundpartner;

import javax.swing.*;
import java.util.HashMap;

public class PreGameGUI extends JFrame {
	private JPanel mainPanel;
	private JLabel lblTitle;

	private JLabel lblDifficulty;
	private JComboBox<String> cbxDifficulty;

	private JLabel lblSize;
	private JSpinner spnPlayFieldSize;

	private JLabel lblAgain;
	private JCheckBox chkAgain;

	private JLabel lblForceRatio;
	private JCheckBox chkForceRatio;

	private JLabel lblWindowSize;
	private JSpinner spnWindowSize;

	private JLabel lbl3Long;
	private JSpinner spn3Long;

	private JLabel lbl2Long;
	private JSpinner spn2Long;

	private JLabel lbl1Long;
	private JSpinner spn1Long;

	private JButton btnStartGame;

	private boolean gameStarted;
	public final Object lock = new Object();

	public PreGameGUI(String title) {
		super(title);

		setResizable(false);
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

	private void createUIComponents() {
		spnPlayFieldSize = new JSpinner(new SpinnerNumberModel(7, 1, 26, 1));

		spnWindowSize = new JSpinner(new SpinnerNumberModel(1280, 250, null, 1));

		spn3Long = new JSpinner(new SpinnerNumberModel(2, 0, null, 1));
		spn2Long = new JSpinner(new SpinnerNumberModel(2, 0, null, 1));
		spn1Long = new JSpinner(new SpinnerNumberModel(3, 0, null, 1));

		cbxDifficulty = new JComboBox<>(new String[]{"Einfach", "Normal", "Schwer", "Unmöglich"});
		cbxDifficulty.setSelectedIndex(2);
	}

	public boolean isGameStarted() {
		return gameStarted;
	}

	public GameSettings getSelectedSettings() {
		return new GameSettings(getSelectedWindowSize(), getSelectedForceRatio(),
				getSelectedPlayFieldSize(), getSelectedDifficulty(), getSelectedTurnOrder(), getSelectedShipAmounts());
	}

	private AI.Difficulty getSelectedDifficulty() {
		switch (cbxDifficulty.getSelectedIndex()) {
			case 0:
				return AI.Difficulty.Easy;
			case 1:
				return AI.Difficulty.Medium;
			case 2:
				return AI.Difficulty.Hard;
			case 3:
				return AI.Difficulty.Impossible;
		}

		throw new IllegalStateException("Invalid difficulty selected!");
	}

	private int getSelectedPlayFieldSize() {
		return (int)spnPlayFieldSize.getValue();
	}

	private boolean getSelectedTurnOrder() {
		return chkAgain.isSelected();
	}

	private boolean getSelectedForceRatio() {
		return chkForceRatio.isSelected();
	}

	private int getSelectedWindowSize() {
		return (int)spnWindowSize.getValue();
	}

	private HashMap<Integer, Integer> getSelectedShipAmounts() {
		HashMap<Integer, Integer> shipAmounts = new HashMap<>();

		shipAmounts.put(1, (int)spn1Long.getValue());
		shipAmounts.put(2, (int)spn2Long.getValue());
		shipAmounts.put(3, (int)spn3Long.getValue());

		return shipAmounts;
	}
}