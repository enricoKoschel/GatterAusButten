package com.partnerundpartner;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.HashMap;

public class Game extends PApplet {
	private enum GameState {
		OwnTurn,
		EnemyTurn,
		PickShips,
		Won,
		Lost
	}

	private final int startingWidth;
	private final int startingHeight;

	private float veryBigTextSize;
	private float bigTextSize;
	private float middleTextSize;

	private float edgeMargin;

	private float cellSize;
	private float smallCellSize;

	private float playFieldSizeInPixels;

	private float ownPlayFieldXPosition;
	private float ownPlayFieldYPosition;

	private float enemyPlayFieldXPosition;
	private float enemyPlayFieldYPosition;

	private float middleSectionXPosition;
	private float middleSectionYPosition;
	private float middleSectionWidth;
	private float middleSectionHeight;

	private float rulesTextXPosition;
	private float rulesTextYPosition;

	private float shipListXPosition;
	private float shipListYPosition;

	//See updateLayout()
	private final boolean funkyResize;

	public Game(int width, int numOfPlayFieldCells, boolean funkyResize) {
		this.numOfPlayFieldCells = numOfPlayFieldCells;
		this.funkyResize = funkyResize;

		startingWidth = width;

		//Set height according to 16:9 aspect ratio
		startingHeight = startingWidth * 9 / 16;

		ownField = new PlayField(numOfPlayFieldCells);
		enemyField = new PlayField(numOfPlayFieldCells);

		remainingShipsToSelect = ownField.getRemainingShipsToSelect();

		//Open window
		String[] processingArgs = {""};
		PApplet.runSketch(processingArgs, this);
		surface.setTitle("Schiffe versenken");
	}

	//Game variables
	private final int numOfPlayFieldCells;
	private final PlayField ownField;
	private final PlayField enemyField;

	private final ArrayList<String> infoText = new ArrayList<>();

	private GameState currentState = GameState.PickShips;

	private HashMap<Integer, Integer> remainingShipsToSelect;

	//Dummy ship for placing new ships
	private final Ship selectedShip = new Ship(0, 0, 0, Ship.Orientation.Horizontal);

	@Override
	public void settings() {
		//Called once at program start
		size(startingWidth, startingHeight);

		//TODO remove
		infoText.add("getroffen");
		infoText.add("Spieler schießt auf A7");
		infoText.add("");
		infoText.add("verfehlt");
		infoText.add("Gegner schießt auf C3");
		infoText.add("");
		infoText.add("versenkt");
		infoText.add("Spieler schießt auf G6");
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
				drawPickShipsScreen();
				break;
			case EnemyTurn:
			case OwnTurn:
				drawMainGameScreen();
				break;
			case Won:
				drawWinScreen();
				break;
			case Lost:
				drawLoseScreen();
				break;
		}
	}

	private void updateLayout() {
		//If funky resize is enabled and the window is not currently in 16:9 aspect ratio,
		//force a resize prioritizing the width and forcing the height.
		//Resizing sometimes results in weird artifacts and window sizes, this is Processing fault.
		if (funkyResize && height != width * 9 / 16) {
			surface.setSize(width, width * 9 / 16);
		}

		cellSize = width * 0.39f / numOfPlayFieldCells;
		smallCellSize = width * 0.06f;

		playFieldSizeInPixels = cellSize * numOfPlayFieldCells;

		edgeMargin = width * 0.01f;

		ownPlayFieldXPosition = edgeMargin;
		ownPlayFieldYPosition = edgeMargin * 6;

		middleSectionXPosition = width * 0.4f;
		middleSectionYPosition = ownPlayFieldYPosition;
		middleSectionWidth = width * 0.2f;
		middleSectionHeight = playFieldSizeInPixels;

		enemyPlayFieldXPosition = width * 0.6f;
		enemyPlayFieldYPosition = ownPlayFieldYPosition;

		shipListXPosition = width * 0.46f;
		shipListYPosition = ownPlayFieldYPosition;

		veryBigTextSize = width / 20f;
		bigTextSize = width / 40f;
		middleTextSize = bigTextSize * 0.7f;

		rulesTextXPosition = shipListXPosition;
		rulesTextYPosition = width * 0.2f;
	}

	private void drawWinScreen() {
		pushStyle();

		fill(0, 255, 0);
		drawMiddleText("Gewonnen!");

		popStyle();
	}

	private void drawLoseScreen() {
		pushStyle();

		fill(255, 0, 0);
		drawMiddleText("Verloren!");

		popStyle();
	}

	private void drawMiddleText(String text) {
		pushStyle();

		textSize(veryBigTextSize);
		text(text, width / 2f - textWidth(text) / 2, height / 2f);

		textSize(bigTextSize);
		String retryText = "Drücke 'R' um erneut zu spielen";
		text(retryText, width / 2f - textWidth(retryText) / 2, height / 2f + bigTextSize);

		popStyle();
	}

	private void drawPickShipsScreen() {
		pushStyle();

		drawPlayField("Eigenes Feld", ownPlayFieldXPosition, ownPlayFieldYPosition, ownField.getMap(), false);

		drawShipList(shipListXPosition, shipListYPosition);
		drawShipPlaceholder();

		drawRulesText(rulesTextXPosition, rulesTextYPosition);

		popStyle();
	}

	private void drawMainGameScreen() {
		pushStyle();

		drawPlayField("Eigenes Feld", ownPlayFieldXPosition, ownPlayFieldYPosition, ownField.getMap(), false);

		drawMiddleSection("Verlauf", middleSectionXPosition, middleSectionYPosition);

		drawPlayField("Gegnerisches Feld", enemyPlayFieldXPosition, enemyPlayFieldYPosition, enemyField.getMap(), true);

		drawMiddleSectionContents(middleSectionXPosition, middleSectionYPosition);

		popStyle();
	}

	private void drawPlayField(String title, float x, float y, Ship.State[][] map, boolean isEnemy) {
		pushStyle();

		textSize(bigTextSize);
		fill(255);

		text(title, x + playFieldSizeInPixels / 2f - textWidth(title) / 2, y - width / 100f);

		textSize(middleTextSize);

		for (int cellY = 0; cellY < numOfPlayFieldCells; cellY++) {
			for (int cellX = 0; cellX < numOfPlayFieldCells; cellX++) {
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

				float finalX = x + cellX * cellSize;
				float finalY = y + cellY * cellSize;

				rect(finalX, finalY, cellSize, cellSize);

				//Cell number on the bottom left of each cell
				fill(0, 0, 128);
				text((char)(65 + cellY) + "" + (cellX + 1), finalX + width * 0.001f,
						finalY + cellSize - width * 0.001f);
			}
		}

		//Text to show whose turn it is
		textSize(bigTextSize);
		fill(255);

		String turnText = "";

		if (isEnemy && currentState == GameState.EnemyTurn) turnText = "Gegner ist dran!";
		else if (!isEnemy && currentState == GameState.OwnTurn) turnText = "Du bist dran!";

		text(turnText, x + playFieldSizeInPixels / 2f - textWidth(turnText) / 2, y + width / 40f + playFieldSizeInPixels);

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
		textSize(middleTextSize);

		for (int i = 0; i < infoText.size(); i++) {
			String text = infoText.get(infoText.size() - 1 - i);

			float textY = y + width / 50f + i * (width / 45f);

			//Remove all 3 connected texts from list if one is offscreen
			if (textY >= y - width / 50f + middleSectionHeight) {
				infoText.remove(infoText.size() - 1 - i);
				infoText.remove(infoText.size() - i);
				infoText.remove(infoText.size() - i + 1);
				i--;
			}

			text(text, x + middleSectionWidth / 2f - textWidth(text) / 2, textY);
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
		text("x" + remainingShipsToSelect.get(3), x + smallCellSize * 3, y + smallCellSize);

		//2 long
		if (selectedShip.getLength() == 2) {
			fill(128, 128, 128);
		}

		float xPos2Long = width * 0.7f;
		rect(xPos2Long, y, smallCellSize, smallCellSize);
		rect(xPos2Long + smallCellSize, y, smallCellSize, smallCellSize);

		fill(255);
		text("x" + remainingShipsToSelect.get(2), xPos2Long + smallCellSize * 2, y + smallCellSize);

		//1 long
		if (selectedShip.getLength() == 1) {
			fill(128, 128, 128);
		}

		float xPos1Long = width * 0.88f;
		rect(xPos1Long, y, smallCellSize, smallCellSize);

		fill(255);
		text("x" + remainingShipsToSelect.get(1), xPos1Long + smallCellSize, y + smallCellSize);

		popStyle();
	}

	private void drawShipPlaceholder() {
		pushStyle();

		int cellX = getSelectedCell(mouseX - ownPlayFieldXPosition, cellSize);
		int cellY = getSelectedCell(mouseY - ownPlayFieldYPosition, cellSize);

		if (selectedShip.getLength() == 3) {
			if (selectedShip.getOrientation() == Ship.Orientation.Horizontal) cellX -= 1;
			else cellY -= 1;
		}

		float x = cellX * cellSize + ownPlayFieldXPosition;
		float y = cellY * cellSize + ownPlayFieldYPosition;

		fill(128, 128, 128);

		if (isInsideOwnPlayField(mouseX, mouseY) && selectedShip.getLength() > 0) {
			float finalCellX;
			float finalCellY;
			float finalTextX;
			float finalTextY;
			char finalChar;
			int finalNum;

			for (int i = 0; i < selectedShip.getLength(); i++) {
				if (selectedShip.getOrientation() == Ship.Orientation.Horizontal) {
					if (cellX + i < numOfPlayFieldCells && cellX + i >= 0) {
						finalCellX = x + cellSize * i;
						finalCellY = y;
						finalTextX = x + cellSize * i + width * 0.001f;
						finalTextY = y + cellSize - width * 0.001f;
						finalChar = (char)(65 + cellY);
						finalNum = cellX + i + 1;
					} else continue;
				} else {
					if (cellY + i < numOfPlayFieldCells && cellY + i >= 0) {
						finalCellX = x;
						finalCellY = y + cellSize * i;
						finalTextX = x + width * 0.001f;
						finalTextY = y + cellSize * (i + 1) - width * 0.001f;
						finalChar = (char)(65 + cellY + i);
						finalNum = cellX + 1;
					} else continue;
				}

				if (ownField.isValidShipPosition(cellX, cellY, selectedShip.getLength(), selectedShip.getOrientation())) {
					fill(128, 128, 128);
				} else {
					fill(255, 0, 0);
				}

				rect(finalCellX, finalCellY, cellSize, cellSize);

				//Cell number on the bottom left of each cell
				fill(0, 0, 128);
				textSize(middleTextSize);

				text(finalChar + "" + finalNum, finalTextX, finalTextY);
			}
		}

		popStyle();
	}

	private void drawRulesText(float x, float y) {
		pushStyle();

		textSize(bigTextSize);
		text("Steuerung:\n" +
				"    'S' - Platzieren überspringen\n" +
				"    Mausrad / 'R' - Schiff drehen\n" +
				"    Linksklick - Platzieren / Schießen\n" +
				"    Rechtsklick - Platzieren abbrechen\n\n" +
				"Regeln:\n" +
				"    Schiffe dürfen sich nicht berühren\n" +
				"    Nach jedem Schuss wechselt der Spieler", x, y);

		popStyle();
	}

	@Override
	public void mouseWheel() {
		rotateSelectedShip();
	}

	@Override
	public void keyPressed() {
		switch (key) {
			case 'r':
				switch (currentState){
					case PickShips:
						rotateSelectedShip();
						break;
					case Won:
					case Lost:
						restartGame();
						break;
				}
				break;
			case 's':
				if (currentState == GameState.PickShips) startMainGame();
				break;


			//TODO remove, for debug only
			case 'w':
				currentState = GameState.Won;
				break;
			case 'l':
				currentState = GameState.Lost;
				break;
		}
	}

	@Override
	public void mouseReleased() {
		//TODO remove, for debug/pre-release only
		if (mouseButton == CENTER) {
			startMainGame();
			return;
		}

		switch (currentState) {
			case OwnTurn:
				//Shoot at enemy play field if left mouse button was pressed inside it
				if (mouseButton == LEFT && isInsideEnemyPlayField(mouseX, mouseY)) {
					shootAtPlayField();
				}
				break;
			case EnemyTurn:
				//Shoot at own play field if left mouse button was pressed inside it
				if (mouseButton == LEFT && isInsideOwnPlayField(mouseX, mouseY)) {
					shootAtPlayField();
				}
				break;
			case PickShips:
				switch (mouseButton) {
					case LEFT:
						if (isInsideOwnPlayField(mouseX, mouseY) && selectedShip.getLength() > 0) {
							//Add ship to map
							addShip();
						} else {
							//Select ship from list
							selectShip();
						}
						break;
					case RIGHT:
						//Deselect selected ship
						selectedShip.setLength(0);
						break;
				}
		}
	}

	private void restartGame(){
		ownField.reset();
		enemyField.reset();

		remainingShipsToSelect = ownField.getRemainingShipsToSelect();

		selectedShip.setLength(0);
		selectedShip.setOrientation(Ship.Orientation.Horizontal);

		currentState = GameState.PickShips;
	}

	private void rotateSelectedShip() {
		selectedShip.setOrientation(selectedShip.getOrientation() == Ship.Orientation.Horizontal ?
				Ship.Orientation.Vertical : Ship.Orientation.Horizontal);
	}

	private void updateRemainingShips() {
		//Subtract one from value
		remainingShipsToSelect.merge(selectedShip.getLength(), -1, Integer::sum);

		//Deselect ship if there are no remaining ships of that length
		if (remainingShipsToSelect.get(selectedShip.getLength()) <= 0) {
			selectedShip.setLength(0);
		}

		ownField.setRemainingShipsToSelect(remainingShipsToSelect);

		int totalNumberOfShipsRemaining = 0;
		for (int remaining : remainingShipsToSelect.values()) totalNumberOfShipsRemaining += remaining;

		//If there are no more ships to select, place the enemy ships randomly and decide who starts playing first
		if (totalNumberOfShipsRemaining <= 0) startMainGame();
	}

	private void startMainGame() {
		ownField.setRemainingShipsToSelect(remainingShipsToSelect);

		//Ships only get placed randomly if they were not previously placed by hand
		ownField.placeShipsRandomly();
		enemyField.placeShipsRandomly();

		currentState = random(1) > 0.5 ? GameState.OwnTurn : GameState.EnemyTurn;
	}

	private void selectShip() {
		pushStyle();

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
			if (remainingShipsToSelect.get(3) > 0) {
				selectedShip.setLength(selectedShip.getLength() == 3 ? 0 : 3);
			}
		} else if (isInsideRect(mouseX, mouseY, leftX2Long, topY, rightX2Long, bottomY)) {
			//Place 2 long ship
			if (remainingShipsToSelect.get(2) > 0) {
				selectedShip.setLength(selectedShip.getLength() == 2 ? 0 : 2);
			}
		} else if (isInsideRect(mouseX, mouseY, leftX1Long, topY, rightX1Long, bottomY)) {
			//Place 1 long ship
			if (remainingShipsToSelect.get(1) > 0) {
				selectedShip.setLength(selectedShip.getLength() == 1 ? 0 : 1);
			}
		}

		popStyle();
	}

	private void addShip() {
		int cellX = getSelectedCell(mouseX - ownPlayFieldXPosition, cellSize);
		int cellY = getSelectedCell(mouseY - ownPlayFieldYPosition, cellSize);

		if (selectedShip.getLength() == 3) {
			if (selectedShip.getOrientation() == Ship.Orientation.Horizontal) cellX -= 1;
			else cellY -= 1;
		}

		if (ownField.addShip(cellX, cellY, selectedShip.getLength(), selectedShip.getOrientation())) {
			//If ship was added successfully, decrease remaining variable of that length
			updateRemainingShips();
		}
	}

	private void shootAtPlayField() {
		int cellX;
		int cellY;
		boolean switchTurns;

		//Determine play field to be shot at
		//Own turn -> shoot at enemy
		//Enemy turn -> shoot at player
		if (currentState == GameState.OwnTurn) {
			cellX = getSelectedCell(mouseX - enemyPlayFieldXPosition, cellSize);
			cellY = getSelectedCell(mouseY - enemyPlayFieldYPosition, cellSize);

			switchTurns = enemyField.getShotAt(cellX, cellY);
		} else {
			cellX = getSelectedCell(mouseX - ownPlayFieldXPosition, cellSize);
			cellY = getSelectedCell(mouseY - ownPlayFieldYPosition, cellSize);

			switchTurns = ownField.getShotAt(cellX, cellY);
		}

		//Switch turns
		if (switchTurns) switchTurns();

		checkWinCondition();
	}

	private void checkWinCondition() {
		if (ownField.getNumberOfLivingShips() <= 0) currentState = GameState.Lost;
		else if (enemyField.getNumberOfLivingShips() <= 0) currentState = GameState.Won;
	}

	private void switchTurns() {
		currentState = currentState == GameState.OwnTurn ? GameState.EnemyTurn : GameState.OwnTurn;
	}

	private int getSelectedCell(float pos, float cellSize) {
		return (int)(pos / cellSize);
	}

	private boolean isInsideRect(float x, float y, float topLeftX, float topLeftY, float bottomRightX, float bottomRightY) {
		return (x > topLeftX && x < bottomRightX) && (y > topLeftY && y < bottomRightY);
	}

	private boolean isInsideOwnPlayField(float x, float y) {
		return (x >= ownPlayFieldXPosition && x < ownPlayFieldXPosition + playFieldSizeInPixels
				&& y >= ownPlayFieldYPosition && y < ownPlayFieldYPosition + playFieldSizeInPixels);
	}

	private boolean isInsideEnemyPlayField(float x, float y) {
		return (x > enemyPlayFieldXPosition && x < enemyPlayFieldXPosition + playFieldSizeInPixels
				&& y > enemyPlayFieldYPosition && y < enemyPlayFieldYPosition + playFieldSizeInPixels);
	}
}