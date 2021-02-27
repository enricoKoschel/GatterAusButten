package com.partnerundpartner;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		PreGameGUI gui = new PreGameGUI("Einstellungen");
		gui.setVisible(true);

		synchronized (gui.lock) {
			while (!gui.gameStarted) gui.lock.wait();
		}

		new Game(1920, 7, true);
	}
}