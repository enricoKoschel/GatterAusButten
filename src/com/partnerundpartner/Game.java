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

	private final int startingWidth;
	private final int startingHeight;

	private float bigTextSize;
	private float smallTextSize;

	private float edgeMargin;

	private float cellSize;
	private float smallCellSize;

	private float playFieldSize;

	private float ownPlayFieldXPosition;
	private float ownPlayFieldYPosition;

	private float enemyPlayFieldXPosition;
	private float enemyPlayFieldYPosition;

	private float middleSectionXPosition;
	private float middleSectionYPosition;
	private float middleSectionWidth;
	private float middleSectionHeight;

	private float shipListXPosition;
	private float shipListYPosition;

	//See updateLayout()
	private final boolean funkyResize;

	public Game(int width, int playFieldCells, boolean funkyResize) {
		this.playFieldCells = playFieldCells;
		this.funkyResize = funkyResize;

		startingWidth = width;

		//Set height according to 16/9 aspect ratio
		startingHeight = startingWidth * 9 / 16;

		ownField = new PlayField(playFieldCells);
		enemyField = new PlayField(playFieldCells);

		//Open window
		String[] processingArgs = {""};
		PApplet.runSketch(processingArgs, this);
		surface.setTitle("Schiffe versenken");
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
		size(startingWidth, startingHeight);

		remainingShipsToSelect.put(Ship.Type.OneLong, 3);
		remainingShipsToSelect.put(Ship.Type.TwoLong, 2);
		remainingShipsToSelect.put(Ship.Type.ThreeLong, 2);

		infoText.add("1. Player hit A7");
		infoText.add("2. CPU hit B6");
		infoText.add("3. Player missed");

		//enemyField.addShip(1, 1, 6, Ship.Orientation.Horizontal);
	}

	@Override
	public void setup() {
		surface.setResizable(true);
	}

	@Override
	public void draw() {
		//Called 60 times per second
		background(24, 24, 24);

		updateLayout();

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

	private void updateLayout() {
		//If funky resize is enabled and the window is not currently in 16/9 aspect ratio,
		//force a resize prioritizing the width and forcing the height.
		//Resizing sometimes results in weird artifacts and window sizes, this is Processing fault.
		if(funkyResize && height != width * 9 / 16){
			surface.setSize(width , width * 9 / 16);
		}

		cellSize = width * 0.39f / playFieldCells;
		smallCellSize = width * 0.06f;

		playFieldSize = cellSize * playFieldCells;

		edgeMargin = width * 0.01f;

		ownPlayFieldXPosition = edgeMargin;
		ownPlayFieldYPosition = edgeMargin * 6;

		middleSectionXPosition = width * 0.4f;
		middleSectionYPosition = ownPlayFieldYPosition;
		middleSectionWidth = width * 0.2f;
		middleSectionHeight = playFieldSize;

		enemyPlayFieldXPosition = width * 0.6f;
		enemyPlayFieldYPosition = ownPlayFieldYPosition;

		shipListXPosition = width * 0.46f;
		shipListYPosition = ownPlayFieldYPosition;

		bigTextSize = width / 40f;
		smallTextSize = bigTextSize * 0.7f;
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

			double textY = y + width / 50f + i * (width / 45f);

			//Remove text from list if offscreen
			if (textY >= y - width / 50f + middleSectionHeight) {
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

		textSize(bigTextSize);

		if (selectedShip.getLength() == 3) {
			fill(128, 128, 128);
		} else {
			fill(255);
		}

		rect(x, y, smallCellSize, smallCellSize);
		rect(x + smallCellSize, y, smallCellSize, smallCellSize);
		rect(x + smallCellSize * 2, y, smallCellSize, smallCellSize);

		fill(255);
		text("x" + remainingShipsToSelect.get(Ship.Type.ThreeLong), x + smallCellSize * 3, y + smallCellSize);

		//2 long
		if (selectedShip.getLength() == 2) {
			fill(128, 128, 128);
		}

		float xPos2Long = width * 0.7f;
		rect(xPos2Long, y, smallCellSize, smallCellSize);
		rect(xPos2Long + smallCellSize, y, smallCellSize, smallCellSize);

		fill(255);
		text("x" + remainingShipsToSelect.get(Ship.Type.TwoLong), xPos2Long + smallCellSize * 2, y + smallCellSize);

		//1 long
		if (selectedShip.getLength() == 1) {
			fill(128, 128, 128);
		}

		float xPos1Long = width * 0.88f;
		rect(xPos1Long, y, smallCellSize, smallCellSize);

		fill(255);
		text("x" + remainingShipsToSelect.get(Ship.Type.OneLong), xPos1Long + smallCellSize, y + smallCellSize);

		popStyle();
	}

	private void drawShipPlaceholder() {
		int cellX = getSelectedCell(mouseX - ownPlayFieldXPosition, cellSize);
		int cellY = getSelectedCell(mouseY - ownPlayFieldYPosition, cellSize);

		float x = cellX * cellSize + ownPlayFieldXPosition;
		float y = cellY * cellSize + ownPlayFieldYPosition;

		fill(128, 128, 128);

		if (isInsideOwnPlayField(mouseX, mouseY) && selectedShip.getLength() > 0) {
			for (int i = 0; i < selectedShip.getLength(); i++) {
				if (selectedShip.getOrientation() == Ship.Orientation.Horizontal) {
					if (x + cellSize * i < ownPlayFieldXPosition + playFieldSize) {
						rect(x + cellSize * i, y, cellSize, cellSize);
					}
				} else {
					if (y + cellSize * i < ownPlayFieldYPosition + playFieldSize) {
						rect(x, y + cellSize * i, cellSize, cellSize);
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
		//TODO remove
		if (mouseButton == CENTER) {
			currentState = GameState.OwnTurn;
			return;
		}

		if (currentState == GameState.PickShips) {
			if (mouseButton == RIGHT) {
				//Deselect selected ship
				selectedShip.setLength(0);
			} else if (mouseButton == LEFT) {
				if (isInsideOwnPlayField(mouseX, mouseY) && selectedShip.getLength() > 0) {
					//Add ship to map
					int cellX = getSelectedCell(mouseX - ownPlayFieldXPosition, cellSize);
					int cellY = getSelectedCell(mouseY - ownPlayFieldYPosition, cellSize);

					if (ownField.addShip(cellX, cellY, selectedShip.getLength(), selectedShip.getOrientation())) {
						switch (selectedShip.getLength()) {
							case 1:
								remainingShipsToSelect.put(Ship.Type.OneLong, remainingShipsToSelect.get(Ship.Type.OneLong) - 1);

								if (remainingShipsToSelect.get(Ship.Type.OneLong) <= 0) {
									selectedShip.setLength(0);
								}
								break;
							case 2:
								remainingShipsToSelect.put(Ship.Type.TwoLong, remainingShipsToSelect.get(Ship.Type.TwoLong) - 1);

								if (remainingShipsToSelect.get(Ship.Type.TwoLong) <= 0) {
									selectedShip.setLength(0);
								}
								break;
							case 3:
								remainingShipsToSelect.put(Ship.Type.ThreeLong, remainingShipsToSelect.get(Ship.Type.ThreeLong) - 1);

								if (remainingShipsToSelect.get(Ship.Type.ThreeLong) <= 0) {
									selectedShip.setLength(0);
								}
								break;
						}

						int totalNumberOfShipsRemaining = 0;
						for (int remaining : remainingShipsToSelect.values()) {
							totalNumberOfShipsRemaining += remaining;
						}

						if (totalNumberOfShipsRemaining <= 0) {
							currentState = GameState.OwnTurn;
						}

						System.out.println("ship added");
					} else {
						System.out.println("error");
					}
				} else {
					//Select ship from list
					selectedShip.setOrientation(Ship.Orientation.Horizontal);

					float topY = shipListYPosition;
					float bottomY = shipListYPosition + smallCellSize;

					float leftX3Long = shipListXPosition;
					float rightX3Long = leftX3Long + smallCellSize * 3;

					float leftX2Long = rightX3Long + smallCellSize;
					float rightX2Long = leftX2Long + smallCellSize * 2;

					float leftX1Long = rightX2Long + smallCellSize;
					float rightX1Long = leftX1Long + smallCellSize;

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
			if (isInsideOwnPlayField(mouseX, mouseY)) {
				//Own field
				int cellX = getSelectedCell(mouseX - ownPlayFieldXPosition, cellSize);
				int cellY = getSelectedCell(mouseY - ownPlayFieldYPosition, cellSize);

				ownField.getShotAt(cellX, cellY);
			} else if (isInsideEnemyPlayField(mouseX, mouseY)) {
				//Enemy field
				int cellX = getSelectedCell(mouseX - enemyPlayFieldXPosition, cellSize);
				int cellY = getSelectedCell(mouseY - enemyPlayFieldYPosition, cellSize);

				enemyField.getShotAt(cellX, cellY);
			}
		}
	}

	private int getSelectedCell(float pos, float cellSize) {
		return (int)(pos / cellSize);
	}

	private boolean isInsideRect(float x, float y, float topLeftX, float topLeftY, float bottomRightX, float bottomRightY) {
		return (x > topLeftX && x < bottomRightX) && (y > topLeftY && y < bottomRightY);
	}

	private boolean isInsideOwnPlayField(float x, float y) {
		return (x >= ownPlayFieldXPosition && x < ownPlayFieldXPosition + playFieldSize
				&& y >= ownPlayFieldYPosition && y < ownPlayFieldYPosition + playFieldSize);
	}

	private boolean isInsideEnemyPlayField(float x, float y) {
		return (x > enemyPlayFieldXPosition && x < enemyPlayFieldXPosition + playFieldSize
				&& y > enemyPlayFieldYPosition && y < enemyPlayFieldYPosition + playFieldSize);
	}
}