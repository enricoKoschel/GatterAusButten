package com.partnerundpartner;

import processing.core.PApplet;

import java.util.ArrayList;

public class Window extends PApplet {
	private enum GameState {
		OwnTurn,
		EnemyTurn,
		PickShips
	}

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
	GameState currentState = GameState.PickShips;
	int remaining1Long = 4;
	int remaining2Long = 3;
	int remaining3Long = 2;
	int selectedShipLength = 0;
	Ship.Orientation selectedShipOrientation = Ship.Orientation.Horizontal;

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
		drawPlayField(0, ownField.getMap());

		if (currentState == GameState.PickShips) {
			drawShipList();
			drawShipPlaceholder();
		} else {
			text = "Gegnerisches Feld";
			text(text, 1240 - textWidth(text) / 2, 80);
			drawPlayField(960, enemyField.getMap());

			text = "Info";
			text(text, 760 - textWidth(text) / 2, 80);
			drawInfoSection();

			drawInfoText();
		}
	}

	private void drawPlayField(int startX, Ship.State[][] map) {
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
				rect(startX + x * scale, 100 + y * scale, scale, scale);
			}
		}

		popStyle();
	}

	private void drawInfoSection() {
		pushStyle();
		fill(24, 24, 24);

		rect(560, 100, 5 * scale, 7 * scale);

		popStyle();
	}

	private void drawInfoText() {
		pushStyle();
		fill(255);
		textSize(30);

		for (int i = 0; i < infoText.size(); i++) {
			String text = infoText.get(infoText.size() - 1 - i);

			int y = 100 + 30 + i * 30;

			//Remove text from list if offscreen
			if (y > height - 30) {
				infoText.remove(infoText.size() - 1 - i);
				i--;
			}

			text(text, 760 - textWidth(text) / 2, y);
		}

		popStyle();
	}

	private void drawShipList() {
		//3 long
		int xOffset3 = scale;
		rect(560 + xOffset3, 100, scale, scale);
		rect(560 + xOffset3 + scale, 100, scale, scale);
		rect(560 + xOffset3 + scale * 2, 100, scale, scale);
		text("x" + remaining3Long, 560 + xOffset3 + scale * 3, 100 + scale);

		//2 long
		int xOffset2 = scale * 6;
		rect(560 + xOffset2, 100, scale, scale);
		rect(560 + xOffset2 + scale, 100, scale, scale);
		text("x" + remaining2Long, 560 + xOffset2 + scale * 2, 100 + scale);

		//1 long
		int xOffset1 = scale * 10;
		rect(560 + xOffset1, 100, scale, scale);
		text("x" + remaining1Long, 560 + xOffset1 + scale, 100 + scale);
	}

	private void drawShipPlaceholder() {
		int cellX = snapDown(mouseX, scale) / scale;
		int cellY = snapDown(mouseY - 20, scale) / scale;

		int x = cellX * scale;
		int y = cellY * scale + 20;

		fill(128, 128, 128);

		if (mouseX > 0 && mouseX < 560 && mouseY > 100 && selectedShipLength > 0) {

			for (int i = 0; i < selectedShipLength; i++) {
				if (selectedShipOrientation == Ship.Orientation.Horizontal) {
					rect(x + scale * i, y, scale, scale);
				} else {
					rect(x, y + scale * i, scale, scale);
				}
			}
		}
	}

	@Override
	public void mouseWheel() {
		selectedShipOrientation = selectedShipOrientation == Ship.Orientation.Horizontal ?
				Ship.Orientation.Vertical : Ship.Orientation.Horizontal;
	}

	@Override
	public void mouseReleased() {
		if (currentState == GameState.PickShips) {
			int topY = 100;
			int bottomY = 100 + scale;

			int leftX3Long = scale * 8;
			int rightX3Long = leftX3Long + scale * 3;

			int leftX2Long = scale * 13;
			int rightX2Long = leftX2Long + scale * 2;

			int leftX1Long = scale * 17;
			int rightX1Long = leftX1Long + scale;

			fill(255, 0, 0);

			if (isInsideRect(mouseX, mouseY, leftX3Long, topY, rightX3Long, bottomY)) {
				//Mouse inside 3 long ship
				selectedShipLength = 3;
			} else if (isInsideRect(mouseX, mouseY, leftX2Long, topY, rightX2Long, bottomY)) {
				//Mouse inside 2 long ship
				selectedShipLength = 2;
			} else if (isInsideRect(mouseX, mouseY, leftX1Long, topY, rightX1Long, bottomY)) {
				//Mouse inside 1 long ship
				selectedShipLength = 1;
			}
		} else {
			int cellY = snapDown(mouseY - 100, scale) / scale;

			if (mouseX > 0 && mouseX < 560 && mouseY > 100) {
				//Own field
				int cellX = snapDown(mouseX, scale) / scale;

				ownField.getShotAt(cellX, cellY);
			} else if (mouseX > 0 && mouseX > 960 && mouseY > 100) {
				//Enemy field
				int cellX = snapDown(mouseX - 960, scale) / scale;

				enemyField.getShotAt(cellX, cellY);
			}
		}
	}

	private int snapDown(double value, int multiple) {
		return (int)(Math.floor(value / multiple) * multiple);
	}

	private boolean isInsideRect(int x, int y, int topLeftX, int topLeftY, int bottomRightX, int bottomRightY) {
		return (x > topLeftX && x < bottomRightX) && (y > topLeftY && y < bottomRightY);
	}
}