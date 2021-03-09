package com.partnerundpartner;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		//Create new game with settings from settings gui
		Game game = new Game(openSettingsGui());

		//noinspection InfiniteLoopStatement
		while (true) {
			//Wait for main game to signal that the settings should be opened
			synchronized (game.lock) {
				while (!game.getExitToSettings()) game.lock.wait();
			}

			//Reset game with settings from settings gui
			game.reset(openSettingsGui());
		}
	}

	private static GameSettings openSettingsGui() throws InterruptedException {
		//Open settings gui
		PreGameGUI gui = new PreGameGUI("Einstellungen");
		gui.setVisible(true);

		//Wait for settings gui to signal that the game has started
		synchronized (gui.lock) {
			while (!gui.isGameStarted()) gui.lock.wait();
		}

		return gui.getSelectedSettings();
	}
}