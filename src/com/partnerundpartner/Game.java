package com.partnerundpartner;

import processing.core.PApplet;
import processing.core.PImage;

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
	private float playFieldDependentTextSize;

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

	private float statisticsXPosition;
	private float statisticsYPosition;

	private float versionXPosition;
	private float versionYPosition;

	//See updateLayout()
	private final boolean forceAspectRatio;

	private final String version = "v0.7-beta";

	public final Object lock = new Object();
	private boolean exitToSettings;

	public Game(int width, boolean forceAspectRatio, int numOfPlayFieldCells, AI.Difficulty aiDifficulty,
				boolean shootAgainAfterHit, HashMap<Integer, Integer> shipAmounts) {
		this.forceAspectRatio = forceAspectRatio;
		this.numOfPlayFieldCells = numOfPlayFieldCells;
		this.shootAgainAfterHit = shootAgainAfterHit;

		AI.setDifficulty(aiDifficulty);

		startingWidth = width;

		//Set height according to 16:9 aspect ratio
		startingHeight = startingWidth * 9 / 16;

		ownField = new PlayField(numOfPlayFieldCells, shipAmounts);
		enemyField = new PlayField(numOfPlayFieldCells, shipAmounts);

		//Open window
		String[] processingArgs = {""};
		PApplet.runSketch(processingArgs, this);
		surface.setTitle("Schiffe versenken");
		surface.setResizable(true);

		PImage icon = loadImage("res/img/icon.png");
		surface.setIcon(icon);
	}

	//Game variables
	private final int numOfPlayFieldCells;
	private final PlayField ownField;
	private final PlayField enemyField;

	private final boolean shootAgainAfterHit;

	private final ArrayList<String> infoText = new ArrayList<>();

	private GameState currentState = GameState.PickShips;

	//Dummy ship for placing new ships
	private final Ship selectedShip = new Ship(0, 0, 0, Ship.Orientation.Horizontal);

	@Override
	public void settings() {
		//Called once at program start
		size(startingWidth, startingHeight);
	}

	@Override
	public void draw() {
		//Called 60 times per second
		background(24, 24, 24);

		updateLayout();

		drawVersionNumber(versionXPosition, versionYPosition);

		switch (currentState) {
			case PickShips:
				drawPickShipsScreen();
				break;
			case EnemyTurn:
				cpuShootAtPlayer();
				drawMainGameScreen();
				break;
			case OwnTurn:
				drawMainGameScreen();
				break;
			case Won:
			case Lost:
				drawResultScreen();
				break;
		}
	}

	private void updateLayout() {
		//If funky resize is enabled and the window is not currently in 16:9 aspect ratio,
		//force a resize prioritizing the width and forcing the height.
		//Resizing sometimes results in weird artifacts and window sizes, this is Processing fault.
		if (forceAspectRatio && height != width * 9 / 16) {
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
		playFieldDependentTextSize = middleTextSize / (numOfPlayFieldCells * 0.13f);

		rulesTextXPosition = shipListXPosition;
		rulesTextYPosition = width * 0.2f;

		statisticsXPosition = width * 0.05f;
		statisticsYPosition = width * 0.4f;

		versionXPosition = width * 0.99f;
		versionYPosition = height * 0.99f;
	}

	private void drawResultScreen() {
		pushStyle();

		String text;

		if (currentState == GameState.Won) {
			fill(0, 255, 0);
			text = "Gewonnen!";
		} else if (currentState == GameState.Lost) {
			fill(255, 0, 0);
			text = "Verloren!";
		} else {
			throw new IllegalStateException("Cannot draw result screen if game has not ended!");
		}

		drawMiddleText(text);
		drawStatistics(statisticsXPosition, statisticsYPosition);

		popStyle();
	}

	private void drawMiddleText(String text) {
		pushStyle();

		textSize(veryBigTextSize);
		text(text, width / 2f - textWidth(text) / 2, height / 2f);

		textSize(bigTextSize);
		String retryText = "Drücke 'R' oder Linksklick um erneut zu spielen";
		text(retryText, width / 2f - textWidth(retryText) / 2, height / 2f + bigTextSize);

		popStyle();
	}

	private void drawStatistics(float x, float y) {
		pushStyle();

		drawStatisticsHalf(x, y, enemyField, "Du:");
		drawStatisticsHalf(x + width / 2f, y, ownField, "Gegner:");

		popStyle();
	}

	private void drawStatisticsHalf(float x, float y, PlayField playField, String title) {
		pushStyle();

		fill(255);
		textSize(bigTextSize);
		text(title, x, y);

		drawStatisticsShots(x, y, playField);
		drawStatisticsShipParts(x, y + bigTextSize * 5, playField);

		popStyle();
	}

	private void drawStatisticsShots(float x, float y, PlayField playField) {
		pushStyle();

		int totalShots = playField.getMissedShots() + playField.getHitShots();
		int totalPercentage = totalShots > 0 ? 100 : 0;

		int missedShots = playField.getMissedShots();
		int missedPercentage = totalShots > 0 ? Math.round((float)(missedShots) / totalShots * 100) : 0;

		int hitShots = playField.getHitShots();
		int hitPercentage = totalShots > 0 ? Math.round((float)(hitShots) / totalShots * 100) : 0;

		fill(255);
		textSize(bigTextSize);
		text("    Verfehlte Schüsse: " + missedShots + " --> " + missedPercentage + "%", x, y + bigTextSize);
		text("    Getroffene Schüsse: " + hitShots + " --> " + hitPercentage + "%", x, y + bigTextSize * 2);
		text("    Schüsse insgesamt: " + totalShots + " --> " + totalPercentage + "%", x, y + bigTextSize * 3);

		popStyle();
	}

	private void drawStatisticsShipParts(float x, float y, PlayField playField) {
		pushStyle();

		int missedShipParts = playField.getNumberOfRemainingShipParts();

		fill(255);
		textSize(bigTextSize);

		text("    Verbleibende Schiffsteile: " + missedShipParts, x, y + middleTextSize);

		popStyle();
	}

	private void drawPickShipsScreen() {
		pushStyle();

		drawPlayField("Eigenes Feld", ownPlayFieldXPosition, ownPlayFieldYPosition, ownField, false);

		drawShipList(shipListXPosition, shipListYPosition);
		drawShipPlaceholder();

		drawRulesText(rulesTextXPosition, rulesTextYPosition);

		popStyle();
	}

	private void drawMainGameScreen() {
		pushStyle();

		drawPlayField("Eigenes Feld", ownPlayFieldXPosition, ownPlayFieldYPosition, ownField, false);

		drawMiddleSection("Info", middleSectionXPosition, middleSectionYPosition);

		drawPlayField("Gegnerisches Feld", enemyPlayFieldXPosition, enemyPlayFieldYPosition, enemyField, true);

		drawMiddleSectionContents(middleSectionXPosition, middleSectionYPosition);

		popStyle();
	}

	private void drawPlayField(String title, float x, float y, PlayField playField, boolean isEnemy) {
		pushStyle();

		textSize(bigTextSize);
		fill(255);

		text(title, x + playFieldSizeInPixels / 2f - textWidth(title) / 2, y - width / 100f);

		textSize(middleTextSize);

		for (int cellY = 0; cellY < numOfPlayFieldCells; cellY++) {
			for (int cellX = 0; cellX < numOfPlayFieldCells; cellX++) {
				switch (playField.getStateAt(cellX, cellY)) {
					case Living_Ship:
						//If drawing enemy field, show living ships as water
						if (isEnemy) fill(0, 0, 255);
						else fill(0, 255, 0);
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

				drawCellText(cellX, cellY, finalX + width * 0.001f, finalY + cellSize - width * 0.001f);
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

		//Remove old text and only leave newest 2
		while (infoText.size() > 6) infoText.remove(0);

		for (int i = 0; i < infoText.size(); i++) {
			String text = infoText.get(infoText.size() - i - 1);

			float textY = y + width / 50f + i * (width / 45f);

			text(text, x + middleSectionWidth / 2f - textWidth(text) / 2, textY);
		}

		//Display number of remaining enemy ships
		String heading = "Lebende Schiffe";

		textSize(bigTextSize);
		text(heading, x + middleSectionWidth / 2f - textWidth(heading) / 2, width * 0.30f);

		textSize(middleTextSize);
		String oneLong = ownField.getNumberOfLivingShips(1) + " - Einer Schiffe - " + enemyField.getNumberOfLivingShips(1);
		String twoLong = ownField.getNumberOfLivingShips(2) + " - Zweier Schiffe - " + enemyField.getNumberOfLivingShips(2);
		String threeLong = ownField.getNumberOfLivingShips(3) + " - Dreier Schiffe - " + enemyField.getNumberOfLivingShips(3);

		text(oneLong, x + middleSectionWidth / 2f - textWidth(oneLong) / 2, width * 0.34f);
		text(twoLong, x + middleSectionWidth / 2f - textWidth(twoLong) / 2, width * 0.37f);
		text(threeLong, x + middleSectionWidth / 2f - textWidth(threeLong) / 2, width * 0.4f);

		popStyle();
	}

	private void drawShipList(float x, float y) {
		pushStyle();

		textSize(bigTextSize);

		//3 long
		if (ownField.getRemainingShipsToSelect(3) > 0) {
			if (selectedShip.getLength() == 3) {
				fill(128, 128, 128);
			} else {
				fill(255);
			}

			rect(x, y, smallCellSize, smallCellSize);
			rect(x + smallCellSize, y, smallCellSize, smallCellSize);
			rect(x + smallCellSize * 2, y, smallCellSize, smallCellSize);

			fill(255);
			text("x" + ownField.getRemainingShipsToSelect(3), x + smallCellSize * 3, y + smallCellSize);
		}

		//2 long
		if (ownField.getRemainingShipsToSelect(2) > 0) {
			if (selectedShip.getLength() == 2) {
				fill(128, 128, 128);
			}

			float xPos2Long = width * 0.7f;
			rect(xPos2Long, y, smallCellSize, smallCellSize);
			rect(xPos2Long + smallCellSize, y, smallCellSize, smallCellSize);

			fill(255);
			text("x" + ownField.getRemainingShipsToSelect(2), xPos2Long + smallCellSize * 2, y + smallCellSize);
		}

		//1 long
		if (ownField.getRemainingShipsToSelect(1) > 0) {
			if (selectedShip.getLength() == 1) {
				fill(128, 128, 128);
			}

			float xPos1Long = width * 0.88f;
			rect(xPos1Long, y, smallCellSize, smallCellSize);

			fill(255);
			text("x" + ownField.getRemainingShipsToSelect(1), xPos1Long + smallCellSize, y + smallCellSize);
		}

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
			int finalCellRow;
			int finalCellColumn;

			for (int i = 0; i < selectedShip.getLength(); i++) {
				if (selectedShip.getOrientation() == Ship.Orientation.Horizontal) {
					if (cellX + i < numOfPlayFieldCells && cellX + i >= 0) {
						finalCellX = x + cellSize * i;
						finalCellY = y;
						finalTextX = x + cellSize * i + width * 0.001f;
						finalTextY = y + cellSize - width * 0.001f;
						finalCellRow = cellY;
						finalCellColumn = cellX + i;
					} else continue;
				} else {
					if (cellY + i < numOfPlayFieldCells && cellY + i >= 0) {
						finalCellX = x;
						finalCellY = y + cellSize * i;
						finalTextX = x + width * 0.001f;
						finalTextY = y + cellSize * (i + 1) - width * 0.001f;
						finalCellRow = cellY + i;
						finalCellColumn = cellX;
					} else continue;
				}

				if (ownField.isValidShipPosition(cellX, cellY, selectedShip.getLength(), selectedShip.getOrientation())) {
					fill(128, 128, 128);
				} else {
					fill(255, 0, 0);
				}

				rect(finalCellX, finalCellY, cellSize, cellSize);

				drawCellText(finalCellColumn, finalCellRow, finalTextX, finalTextY);
			}
		}

		popStyle();
	}

	private void drawCellText(int column, int row, float x, float y) {
		pushStyle();

		//Cell number on the bottom left of each cell
		fill(0, 0, 128);
		textSize(playFieldDependentTextSize);

		text(getCellText(column, row), x, y);

		popStyle();
	}

	private void drawRulesText(float x, float y) {
		pushStyle();

		textSize(bigTextSize);
		text("Steuerung:\n" +
				"    Mittelklick / 'S' - Platzieren überspringen\n" +
				"    Mausrad / 'R' - Schiff drehen\n" +
				"    Linksklick - Platzieren / Schießen\n" +
				"    Rechtsklick - Platzieren abbrechen\n" +
				"    Esc - Zu den Einstellungen\n" +
				"Regeln:\n" +
				"    Schiffe dürfen sich nicht berühren\n" +
				"    Nach jedem Schuss wechselt der Spieler", x, y);

		popStyle();
	}

	private void drawVersionNumber(float x, float y) {
		pushStyle();

		textSize(middleTextSize);
		text(version, x - textWidth(version), y);

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
				switch (currentState) {
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
			case ESC:
				//Processing window closes if escape key is not reset
				key = 0;

				exitToSettings();
				break;
		}
	}

	private void exitToSettings() {
		if (currentState == GameState.PickShips) {
			synchronized (lock) {
				exitToSettings = true;
				lock.notifyAll();
			}

			//FIXME Only stops thread and hides window, memory leak
			surface.stopThread();
			surface.setVisible(false);
		}
	}

	@Override
	public void mouseReleased() {
		switch (mouseButton) {
			case LEFT:
				switch (currentState) {
					case OwnTurn:
						if (isInsideEnemyPlayField(mouseX, mouseY)) shootAtEnemy();
						break;
					case PickShips:
						if (isInsideOwnPlayField(mouseX, mouseY) && selectedShip.getLength() > 0) addShip();
						else selectShip();
						break;
					case Won:
					case Lost:
						restartGame();
						break;
				}
				break;
			case RIGHT:
				//Deselect selected ship
				if (currentState == GameState.PickShips) selectedShip.setLength(0);
				break;
			case CENTER:
				if (currentState == GameState.PickShips) startMainGame();
		}
	}

	public String getCellText(int x, int y) {
		return (char)('A' + y) + "" + (x + 1);
	}

	private void restartGame() {
		ownField.reset();
		enemyField.reset();

		selectedShip.setLength(0);
		selectedShip.setOrientation(Ship.Orientation.Horizontal);

		infoText.clear();

		currentState = GameState.PickShips;
	}

	private void rotateSelectedShip() {
		selectedShip.setOrientation(selectedShip.getOrientation() == Ship.Orientation.Horizontal ?
				Ship.Orientation.Vertical : Ship.Orientation.Horizontal);
	}

	private void updateRemainingShips() {
		//Subtract one from value
		ownField.decrementRemainingShipsToSelect(selectedShip.getLength());

		//Deselect ship if there are no remaining ships of that length
		if (ownField.getRemainingShipsToSelect(selectedShip.getLength()) <= 0) selectedShip.setLength(0);

		int totalNumberOfShipsRemaining = 0;
		for (int remaining : ownField.getRemainingShipsToSelect().values()) totalNumberOfShipsRemaining += remaining;

		//If there are no more ships to select, start the game
		if (totalNumberOfShipsRemaining <= 0) startMainGame();
	}

	private void startMainGame() {
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
			if (ownField.getRemainingShipsToSelect(3) > 0) {
				selectedShip.setLength(selectedShip.getLength() == 3 ? 0 : 3);
			}
		} else if (isInsideRect(mouseX, mouseY, leftX2Long, topY, rightX2Long, bottomY)) {
			//Place 2 long ship
			if (ownField.getRemainingShipsToSelect(2) > 0) {
				selectedShip.setLength(selectedShip.getLength() == 2 ? 0 : 2);
			}
		} else if (isInsideRect(mouseX, mouseY, leftX1Long, topY, rightX1Long, bottomY)) {
			//Place 1 long ship
			if (ownField.getRemainingShipsToSelect(1) > 0) {
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

	private void shootAtEnemy() {
		if (currentState != GameState.OwnTurn) return;

		int cellX = getSelectedCell(mouseX - enemyPlayFieldXPosition, cellSize);
		int cellY = getSelectedCell(mouseY - enemyPlayFieldYPosition, cellSize);

		PlayField.ShotType shotType = enemyField.getShotAt(cellX, cellY);

		//Switch turns
		if (shotType != PlayField.ShotType.Invalid) {
			addShotHistory(cellX, cellY, shotType, false);

			switchTurns(shotType);
		}
	}

	private void cpuShootAtPlayer() {
		if (currentState != GameState.EnemyTurn) return;

		AI.shootAt(ownField);

		addShotHistory(AI.getLastHitX(), AI.getLastHitY(), AI.getLastHitShotType(), true);

		switchTurns(AI.getLastHitShotType());
	}

	private void checkWinCondition() {
		if (ownField.getNumberOfLivingShips() <= 0) currentState = GameState.Lost;
		else if (enemyField.getNumberOfLivingShips() <= 0) currentState = GameState.Won;
	}

	private void addShotHistory(int x, int y, PlayField.ShotType shotType, boolean enemyShot) {
		infoText.add("");
		switch (shotType) {
			case Miss:
				infoText.add("verfehlt");
				break;
			case Hit:
				infoText.add("getroffen");
				break;
			case Sunk:
				infoText.add("versenkt");
				break;
		}

		String shooter = enemyShot ? "Gegner" : "Spieler";
		String cellText = getCellText(x, y);

		infoText.add(shooter + " schießt auf " + cellText);
	}

	private void switchTurns(PlayField.ShotType shotType) {
		if (!(shootAgainAfterHit && (shotType == PlayField.ShotType.Hit || shotType == PlayField.ShotType.Sunk))) {
			currentState = currentState == GameState.OwnTurn ? GameState.EnemyTurn : GameState.OwnTurn;
		}

		checkWinCondition();
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

	public boolean getExitToSettings() {
		return exitToSettings;
	}
}