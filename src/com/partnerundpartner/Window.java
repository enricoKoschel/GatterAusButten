package com.partnerundpartner;

import processing.core.PApplet;

import java.util.ArrayList;

public class Window extends PApplet {
	private final int width;
	private final int height;
	private final int scale = 80;

	public Window(String title, int width, int height) {
		this.width = width;
		this.height = height;

		//Open window
		String[] processingArgs = {title};
		PApplet.runSketch(processingArgs, this);
	}

	//Game variables
	PlayField ownField = new PlayField(7);
	PlayField enemyField = new PlayField(7);
	ArrayList<String> infoText = new ArrayList<>();

	@Override
	public void settings() {
		//Called once at program start
		size(width, height);

		infoText.add("1. Player hit A7");
		infoText.add("2. CPU hit B6");
		infoText.add("3. Player missed");

		ownField.addShip(0, 0, 3, Ship.Orientation.Horizontal);
		enemyField.addShip(1, 1, 4, Ship.Orientation.Horizontal);
	}

	@Override
	public void draw() {
		//Called 60 times per second
		background(24, 24, 24);
		textSize(50);
		fill(255);
		stroke(48, 48, 48);

		String text = "Eigenes Feld";
		text(text, 280 - textWidth(text) / 2, 80);
		drawPlayField(0, 100, ownField.getMap());

		text = "Gegnerisches Feld";
		text(text, 1240 - textWidth(text) / 2, 80);
		drawPlayField(960, 100, enemyField.getMap());

		text = "Info";
		text(text, 760 - textWidth(text) / 2, 80);
		drawInfoSection(560, 100, 5, 7);

		drawInfoText(30);
	}

	private void drawPlayField(int startX, int startY, Ship.State[][] map) {
		pushStyle();
		int size = map.length;

		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				switch (map[x][y]) {
					case Alive_Ship_Horizontal:
					case Alive_Ship_Vertical:
						fill(0, 255, 0);
						break;
					case Hit_Ship:
						fill(255, 0, 0);
						break;
					case Water:
						fill(0, 0, 255);
						break;
					case Miss:
						fill(128);
						break;
				}
				rect(startX + x * scale, startY + y * scale, scale, scale);
			}
		}

		popStyle();
	}

	private void drawInfoSection(int x, int y, int xSize, int ySize) {
		pushStyle();
		fill(24, 24, 24);

		rect(x, y, xSize * scale, ySize * scale);

		popStyle();
	}

	private void drawInfoText(int size) {
		pushStyle();
		fill(255);
		textSize(size);

		for (int i = 0; i < infoText.size(); i++) {
			String text = infoText.get(infoText.size() - 1 - i);

			int y = 100 + size + i * size;

			//Remove text from list if offscreen
			if (y > height - size) {
				infoText.remove(infoText.size() - 1 - i);
				i--;
			}

			text(text, 760 - textWidth(text) / 2, y);
		}

		popStyle();
	}

	@Override
	public void mouseReleased() {
		if (mouseX < 560 && mouseY > 100) {
			//Own field
			int cellX = snapDown(mouseX, scale) / scale;
			int cellY = snapDown(mouseY - 100, scale) / scale;

			ownField.getShotAt(cellX, cellY);
		} else if (mouseX > 960 && mouseY > 100) {
			//Enemy field
			int cellX = snapDown(mouseX - 960, scale) / scale;
			int cellY = snapDown(mouseY - 100, scale) / scale;

			enemyField.getShotAt(cellX, cellY);
		}
	}

	private int snapDown(double value, int multiple) {
		return (int)(Math.floor(value / multiple) * multiple);
	}
}