package com.partnerundpartner;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.HashMap;

public class Game extends PApplet {
	private enum GameState {
		OwnTurn,
		EnemyTurn,
		PickShips
	}

	private final int width;
	private final int height;

	private final float bigTextSize;
	private final float smallTextSize;

	private final float edgeMargin;

	private final float cellSize;

	private final float playFieldSize;

	private final float ownPlayFieldXPosition;
	private final float ownPlayFieldYPosition;

	private final float enemyPlayFieldXPosition;
	private final float enemyPlayFieldYPosition;

	private final float middleSectionXPosition;
	private final float middleSectionYPosition;
	private final float middleSectionWidth;
	private final float middleSectionHeight;

	private final float shipListXPosition;
	private final float shipListYPosition;

	private final int scale = 80;


	public Game(int width, int playFieldCells) {
		this.width = width;
		this.playFieldCells = playFieldCells;

		//Set height according to 16/9 aspect ratio
		height = width * 9 / 16;

		playFieldSize = (float)(width * 0.35);

		cellSize = playFieldSize / playFieldCells;

		edgeMargin = (float)(width * 0.05);

		ownPlayFieldXPosition = edgeMargin;
		ownPlayFieldYPosition = edgeMargin;

		middleSectionXPosition = (float)(width * 0.4);
		middleSectionYPosition = edgeMargin;
		middleSectionWidth = (float)(width * 0.2);
		middleSectionHeight = playFieldSize;

		enemyPlayFieldXPosition = (float)(width * 0.6);
		enemyPlayFieldYPosition = edgeMargin;

		shipListXPosition = (float)(width * 0.46);
		shipListYPosition = edgeMargin;

		bigTextSize = width / 40f;
		smallTextSize = (float)(bigTextSize * 0.7);

		ownField = new PlayField(playFieldCells);
		enemyField = new PlayField(playFieldCells);

		//Open window
		String[] processingArgs = {"Schiffe Versenken"};
		PApplet.runSketch(processingArgs, this);
	}

	//Game variables
	private final int playFieldCells;
	private final PlayField ownField;
	private final PlayField enemyField;

	private final ArrayList<String> infoText = new ArrayList<>();

	private GameState currentState = GameState.PickShips;

	private final HashMap<Ship.Type, Integer> remainingShipsToSelect = new HashMap<>();

	//Dummy ship for placing new ships
	private final Ship selectedShip = new Ship(0, 0, 0, Ship.Orientation.Horizontal);

	@Override
	public void settings() {
		//Called once at program start
		size(width, height);

		remainingShipsToSelect.put(Ship.Type.OneLong, 3);
		remainingShipsToSelect.put(Ship.Type.TwoLong, 2);
		remainingShipsToSelect.put(Ship.Type.ThreeLong, 2);

		infoText.add("1. Player hit A7");
		infoText.add("2. CPU hit B6");
		infoText.add("3. Player missed");

		//enemyField.addShip(1, 1, 6, Ship.Orientation.Horizontal);
	}

	@Override
	public void draw() {
		//Called 60 times per second
		background(24, 24, 24);

		switch (currentState) {
			case PickShips:
				drawPlayField("Eigenes Feld", ownPlayFieldXPosition, ownPlayFieldYPosition, ownField.getMap());

				drawShipList(shipListXPosition, shipListYPosition);
				drawShipPlaceholder();
				break;

			case EnemyTurn:
			case OwnTurn:
				drawPlayField("Eigenes Feld", ownPlayFieldXPosition, ownPlayFieldYPosition, ownField.getMap());

				drawMiddleSection("Verlauf", middleSectionXPosition, middleSectionYPosition);

				drawPlayField("Gegnerisches Feld", enemyPlayFieldXPosition, enemyPlayFieldYPosition, enemyField.getMap());

				drawMiddleSectionContents(middleSectionXPosition, middleSectionYPosition);
				break;
		}
	}

	private void drawPlayField(String title, float x, float y, Ship.State[][] map) {
		pushStyle();

		textSize(bigTextSize);
		fill(255);

		text(title, x + playFieldSize / 2f - textWidth(title) / 2, y - width / 100f);

		for (int cellY = 0; cellY < playFieldCells; cellY++) {
			for (int cellX = 0; cellX < playFieldCells; cellX++) {
				switch (map[cellX][cellY]) {
					case Living_Ship:
						fill(0, 255, 0);
						break;
					case Sunk_Ship:
						fill(0);
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

				rect(x + cellX * cellSize, y + cellY * cellSize, cellSize, cellSize);
			}
		}

		popStyle();
	}

	private void drawMiddleSection(String title, float x, float y) {
		pushStyle();

		textSize(bigTextSize);
		fill(255);

		text(title, x + middleSectionWidth / 2f - textWidth(title) / 2, y - width / 100f);

		fill(24, 24, 24);

		rect(x, y, middleSectionWidth, middleSectionHeight);

		popStyle();
	}

	private void drawMiddleSectionContents(float x, float y) {
		pushStyle();

		fill(255);
		textSize(smallTextSize);

		for (int i = 0; i < infoText.size(); i++) {
			String text = infoText.get(infoText.size() - 1 - i);

			double textY = y + width / 50f + i * bigTextSize;

			//Remove text from list if offscreen
			if (textY >= height - cellSize * 1.5) {
				infoText.remove(infoText.size() - 1 - i);
				i--;
			}

			text(text, x + middleSectionWidth / 2f - textWidth(text) / 2, (float)textY);
		}

		popStyle();
	}

	private void drawShipList(float x, float y) {
		//3 long
		pushStyle();

		//Ship list screen uses different cell size
		int cellSize = (int)(width * 0.06);

		if (selectedShip.getLength() == 3) {
			fill(128, 128, 128);
		} else {
			fill(255);
		}

		rect(x, y, cellSize, cellSize);
		rect(x + cellSize, y, cellSize, cellSize);
		rect(x + cellSize * 2, y, cellSize, cellSize);

		fill(255);
		text("x" + remainingShipsToSelect.get(Ship.Type.ThreeLong), x + cellSize * 3, y + cellSize);

		//2 long
		if (selectedShip.getLength() == 2) {
			fill(128, 128, 128);
		}

		int xPos2Long = (int)(width * 0.7);
		rect(xPos2Long, y, cellSize, cellSize);
		rect(xPos2Long + cellSize, y, cellSize, cellSize);

		fill(255);
		text("x" + remainingShipsToSelect.get(Ship.Type.TwoLong), xPos2Long + cellSize * 2, y + cellSize);

		//1 long
		if (selectedShip.getLength() == 1) {
			fill(128, 128, 128);
		}

		int xPos1Long = (int)(width * 0.88);
		rect(xPos1Long, y, cellSize, cellSize);

		fill(255);
		text("x" + remainingShipsToSelect.get(Ship.Type.OneLong), xPos1Long + cellSize, y + cellSize);

		popStyle();
	}

	private void drawShipPlaceholder() {
		int cellX = snapDown(mouseX - scale, scale) / scale;
		int cellY = snapDown(mouseY - scale, scale) / scale;

		int x = cellX * scale + scale;
		int y = cellY * scale + scale;

		fill(128, 128, 128);

		if (mouseX > scale && mouseX < scale * (playFieldCells + 1) && mouseY > scale && mouseY < scale * (playFieldCells + 1) && selectedShip.getLength() > 0) {
			for (int i = 0; i < selectedShip.getLength(); i++) {
				if (selectedShip.getOrientation() == Ship.Orientation.Horizontal) {
					if (x + scale * i < scale * (playFieldCells + 1)) {
						rect(x + scale * i, y, scale, scale);
					}
				} else {
					if (y + scale * i < scale * (playFieldCells + 1)) {
						rect(x, y + scale * i, scale, scale);
					}
				}
			}
		}
	}

	@Override
	public void mouseWheel() {
		selectedShip.setOrientation(selectedShip.getOrientation() == Ship.Orientation.Horizontal ?
				Ship.Orientation.Vertical : Ship.Orientation.Horizontal);
	}

	@Override
	public void mouseReleased() {
		if (mouseButton == CENTER) {
			currentState = GameState.OwnTurn;
			return;
		}


		if (currentState == GameState.PickShips) {
			if (mouseButton == RIGHT) {
				//Deselect selected ship
				selectedShip.setLength(0);
			} else if (mouseButton == LEFT) {
				if (mouseX > 0 && mouseX < 560 && mouseY > 100 && selectedShip.getLength() > 0) {
					//Add ship to map
					int cellX = snapDown(mouseX, scale) / scale;
					int cellY = snapDown(mouseY - 100, scale) / scale;

					if (ownField.addShip(cellX, cellY, selectedShip.getLength(), selectedShip.getOrientation())) {
						switch (selectedShip.getLength()) {
							case 1:
								remainingShipsToSelect.put(Ship.Type.OneLong, remainingShipsToSelect.get(Ship.Type.OneLong) - 1);
								break;
							case 2:
								remainingShipsToSelect.put(Ship.Type.TwoLong, remainingShipsToSelect.get(Ship.Type.TwoLong) - 1);
								break;
							case 3:
								remainingShipsToSelect.put(Ship.Type.ThreeLong, remainingShipsToSelect.get(Ship.Type.ThreeLong) - 1);
								break;
						}

						selectedShip.setLength(0);

						int totalNumberOfShipsRemaining = 0;
						for (int remaining : remainingShipsToSelect.values()) {
							totalNumberOfShipsRemaining += remaining;
						}

						if (totalNumberOfShipsRemaining == 0) {
							currentState = GameState.OwnTurn;
						}

						System.out.println("ship added");
					} else {
						System.out.println("error");
					}
				} else {
					//Select ship from list
					selectedShip.setOrientation(Ship.Orientation.Horizontal);

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
						//Place 3 long ship
						if (remainingShipsToSelect.get(Ship.Type.ThreeLong) > 0) {
							selectedShip.setLength(selectedShip.getLength() == 3 ? 0 : 3);
						}
					} else if (isInsideRect(mouseX, mouseY, leftX2Long, topY, rightX2Long, bottomY)) {
						//Place 2 long ship
						if (remainingShipsToSelect.get(Ship.Type.TwoLong) > 0) {
							selectedShip.setLength(selectedShip.getLength() == 2 ? 0 : 2);
						}
					} else if (isInsideRect(mouseX, mouseY, leftX1Long, topY, rightX1Long, bottomY)) {
						//Place 1 long ship
						if (remainingShipsToSelect.get(Ship.Type.OneLong) > 0) {
							selectedShip.setLength(selectedShip.getLength() == 1 ? 0 : 1);
						}
					}
				}
			}
		} else {
			int cellY = snapDown(mouseY - 100, scale) / scale;

			if (mouseX > 0 && mouseX < 560 && mouseY > 100) {
				//Own field
				int cellX = snapDown(mouseX, scale) / scale;

				ownField.getShotAt(cellX, cellY);
			} else if (mouseX > 960 && mouseX < 1520 && mouseY > 100) {
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