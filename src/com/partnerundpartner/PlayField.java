package com.partnerundpartner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayField {
	enum ShotType {
		Miss,
		Hit,
		Sunk,
		Invalid
	}

	private final int size;
	private final Ship.State[][] map;

	private final ArrayList<Ship> livingShips;
	private HashMap<Integer, Integer> remainingShipsToSelect;

	private int missedShots;
	private int hitShots;

	public PlayField(int size) {
		this.size = size;

		remainingShipsToSelect = new HashMap<>();

		livingShips = new ArrayList<>();
		map = new Ship.State[size][size];

		reset();
	}

	public void reset() {
		//Placeable ship amounts
		remainingShipsToSelect.put(1, 3);
		remainingShipsToSelect.put(2, 2);
		remainingShipsToSelect.put(3, 2);

		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) map[x][y] = Ship.State.Water;
		}

		missedShots = 0;
		hitShots = 0;

		livingShips.clear();
	}

	public Ship.State stateAt(int x, int y) {
		return map[x][y];
	}

	public void changeStateAt(int x, int y, Ship.State state) {
		map[x][y] = state;
	}

	private boolean damageShipAt(int x, int y) {
		for (Ship ship : livingShips) {
			for (int i = 0; i < ship.getLength(); i++) {
				if (ship.getOrientation() == Ship.Orientation.Horizontal) {
					if (ship.getX() + i == x && ship.getY() == y) ship.getDamaged();
				} else {
					if (ship.getX() == x && ship.getY() + i == y) ship.getDamaged();
				}
			}

			changeStateAt(x, y, Ship.State.Hit_Ship);
		}

		for (Ship ship : livingShips) {
			if (ship.getHealth() <= 0) {
				int shipX = ship.getX();
				int shipY = ship.getY();
				int length = ship.getLength();
				Ship.Orientation orientation = ship.getOrientation();

				for (int i = 0; i < length; i++) {
					if (orientation == Ship.Orientation.Vertical) changeStateAt(shipX, shipY + i, Ship.State.Sunk_Ship);
					else changeStateAt(shipX + i, shipY, Ship.State.Sunk_Ship);
				}
			}
		}

		return livingShips.removeIf(ship -> ship.getHealth() <= 0);
	}

	public boolean addShip(int x, int y, int length, Ship.Orientation orientation) {
		if (!isValidShipPosition(x, y, length, orientation)) return false;

		livingShips.add(new Ship(x, y, length, orientation));

		for (int i = 0; i < length; i++) {
			if (orientation == Ship.Orientation.Horizontal) {
				map[x + i][y] = Ship.State.Living_Ship;
			} else {
				map[x][y + i] = Ship.State.Living_Ship;
			}
		}

		return true;
	}

	public boolean isValidShipPosition(int x, int y, int length, Ship.Orientation orientation) {
		for (int i = 0; i < length; i++) {
			if (orientation == Ship.Orientation.Horizontal) {
				if (isInvalidSingleShipPosition(x + i, y) || areSurroundingShipsInvalid(x + i, y)) return false;
			} else {
				if (isInvalidSingleShipPosition(x, y + i) || areSurroundingShipsInvalid(x, y + i)) return false;
			}
		}

		return true;
	}

	private boolean areSurroundingShipsInvalid(int x, int y) {
		//Surrounding ships (diagonals are ignored)
		//X,X  0,-1 X,X
		//-1,0 X,X +1,0
		//X,X  0,+1 X,X

		return isInvalidSingleShipPositionNoEdges(x, y - 1) || isInvalidSingleShipPositionNoEdges(x - 1, y)
				|| isInvalidSingleShipPositionNoEdges(x + 1, y) || isInvalidSingleShipPositionNoEdges(x, y + 1);
	}

	private boolean isInvalidSingleShipPosition(int x, int y) {
		return x < 0 || x >= size || y < 0 || y >= size || map[x][y] != Ship.State.Water;
	}

	private boolean isInvalidSingleShipPositionNoEdges(int x, int y) {
		if (x < 0 || x >= size || y < 0 || y >= size) return false;

		return map[x][y] != Ship.State.Water;
	}

	public ShotType getShotAt(int x, int y) {
		switch (stateAt(x, y)) {
			case Living_Ship:
				hitShots++;
				return damageShipAt(x, y) ? ShotType.Sunk : ShotType.Hit;
			case Water:
				missedShots++;
				changeStateAt(x, y, Ship.State.Miss);
				return ShotType.Miss;
			case Miss:
			case Hit_Ship:
				return ShotType.Invalid;
		}

		return ShotType.Invalid;
	}

	public void placeShipsRandomly() {
		int totalNumberOfShipsRemaining;

		do {
			totalNumberOfShipsRemaining = 0;
			for (int remaining : remainingShipsToSelect.values()) totalNumberOfShipsRemaining += remaining;
			if (totalNumberOfShipsRemaining <= 0) return;

			int cellX = (int)(Math.random() * size);
			int cellY = (int)(Math.random() * size);

			AtomicInteger maxRemainingShipLength = new AtomicInteger();
			remainingShipsToSelect.forEach((key, value) -> {
				if (value > 0 && key > maxRemainingShipLength.get()) maxRemainingShipLength.set(key);
			});

			Ship.Orientation orientation = Math.random() > 0.5 ? Ship.Orientation.Horizontal : Ship.Orientation.Vertical;

			if (addShip(cellX, cellY, maxRemainingShipLength.get(), orientation))
				remainingShipsToSelect.merge(maxRemainingShipLength.get(), -1, Integer::sum);
		} while (true);
	}

	public HashMap<Integer, Integer> getRemainingShipsToSelect() {
		return remainingShipsToSelect;
	}

	public void setRemainingShipsToSelect(HashMap<Integer, Integer> remainingShipsToSelect) {
		this.remainingShipsToSelect = remainingShipsToSelect;
	}

	public Ship.State[][] getMap() {
		return map;
	}

	public int getNumberOfLivingShips() {
		return livingShips.size();
	}

	public ArrayList<Ship> getLivingShips() {
		return livingShips;
	}

	public int getMissedShots() {
		return missedShots;
	}

	public int getHitShots() {
		return hitShots;
	}

	public int getSize() {
		return size;
	}
}