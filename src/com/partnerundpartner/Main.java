package com.partnerundpartner;

import java.util.HashMap;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		while (true) {
			PreGameGUI gui = new PreGameGUI("Einstellungen");
			gui.setVisible(true);

			synchronized (gui.lock) {
				while (!gui.isGameStarted()) gui.lock.wait();
			}

			int width = gui.getSelectedWindowSize();
			boolean forceAspectRatio = gui.getSelectedForceRatio();
			int playFieldSize = gui.getSelectedPlayFieldSize();
			AI.Difficulty difficulty = gui.getSelectedDifficulty();
			boolean turnOrder = gui.getSelectedTurnOrder();
			HashMap<Integer, Integer> shipAmounts = gui.getSelectedShipAmounts();

			Game game = new Game(width, forceAspectRatio, playFieldSize, difficulty, turnOrder, shipAmounts);

			synchronized (game.lock) {
				while (!game.getExitToSettings()) game.lock.wait();
			}

			//FIXME game does not get cleaned up correctly, memory leak
			System.gc();
		}
	}
}