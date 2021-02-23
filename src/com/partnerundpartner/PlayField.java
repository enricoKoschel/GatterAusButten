package com.partnerundpartner;

import java.util.ArrayList;

public class PlayField {
	private final int size;
	private final Ship.State[][] map;
	private final ArrayList<Ship> livingShips;

	public PlayField(int size) {
		this.size = size;

		livingShips = new ArrayList<>();
		map = new Ship.State[size][size];

		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) map[x][y] = Ship.State.Water;
		}
	}

	public Ship.State stateAt(int x, int y) {
		return map[x][y];
	}

	public void changeStateAt(int x, int y, Ship.State state) {
		map[x][y] = state;
	}

	private void damageShipAt(int x, int y) {
		for (Ship ship : livingShips) {
			for (int i = 0; i < ship.getLength(); i++) {
				if (ship.getOrientation() == Ship.Orientation.Vertical) {
					if (ship.getX() == x && ship.getY() + i == y) {
						ship.getDamaged();
					}
				} else {
					if (ship.getX() + i == x && ship.getY() == y) {
						ship.getDamaged();
					}
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
					if (orientation == Ship.Orientation.Vertical) {
						changeStateAt(shipX, shipY + i, Ship.State.Sunk_Ship);
					} else {
						changeStateAt(shipX + i, shipY, Ship.State.Sunk_Ship);
					}
				}
			}
		}

		livingShips.removeIf(ship -> ship.getHealth() <= 0);
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
		//-1,0 0,0 +1,0
		//X,X  0,+1 X,X

		return isInvalidSingleShipPositionNoEdges(x, y - 1) || isInvalidSingleShipPositionNoEdges(x - 1, y)
				|| isInvalidSingleShipPositionNoEdges(x, y) || isInvalidSingleShipPositionNoEdges(x + 1, y)
				|| isInvalidSingleShipPositionNoEdges(x, y + 1);
	}

	private boolean isInvalidSingleShipPosition(int x, int y) {
		return x < 0 || x >= size || y < 0 || y >= size || map[x][y] != Ship.State.Water;
	}

	private boolean isInvalidSingleShipPositionNoEdges(int x, int y) {
		if (x < 0 || x >= size || y < 0 || y >= size) return false;

		return map[x][y] != Ship.State.Water;
	}

	public boolean getShotAt(int x, int y) {
		switch (stateAt(x, y)) {
			case Living_Ship:
				damageShipAt(x, y);
				return true;
			case Water:
				changeStateAt(x, y, Ship.State.Miss);
				return true;
			case Miss:
			case Hit_Ship:
				return false;
		}

		return false;
	}

	public Ship.State[][] getMap() {
		return map;
	}

	public int getNumberOfLivingShips() {
		return livingShips.size();
	}
}