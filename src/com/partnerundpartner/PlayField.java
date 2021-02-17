package com.partnerundpartner;

import java.util.ArrayList;

public class PlayField {
	private final int size;
	private final Ship.State[][] map;
	private final ArrayList<Ship> livingShips;

	public PlayField(int size) {
		livingShips = new ArrayList<>();
		map = new Ship.State[size][size];

		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) map[x][y] = Ship.State.Water;
		}

		this.size = size;
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

		for(Ship ship : livingShips){
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
		//if(){
		//
		//}

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

	public Ship.State getShotAt(int x, int y) {
		switch (stateAt(x, y)) {
			case Living_Ship:
				damageShipAt(x, y);
				return Ship.State.Living_Ship;
			case Hit_Ship:
				return Ship.State.Hit_Ship;
			case Water:
				changeStateAt(x, y, Ship.State.Miss);
				return Ship.State.Water;
			case Miss:
				return Ship.State.Miss;
		}

		return Ship.State.Water;
	}

	public Ship.State[][] getMap() {
		return map;
	}

	public int getNumberOfAliveShips() {
		return livingShips.size();
	}
}