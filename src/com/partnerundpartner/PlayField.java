package com.partnerundpartner;

import java.util.ArrayList;

public class PlayField {
	private final int size;
	private final Ship.State[][] map;
	private final ArrayList<Ship> livingShips;

	public PlayField(int size) {
		livingShips = new ArrayList<Ship>();
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

			if(ship.getHealth() <= 0) livingShips.remove(ship);
		}

		changeStateAt(x, y, Ship.State.Hit_Ship);
	}

	public void addShip(int x, int y, int length, Ship.Orientation orientation) {
		livingShips.add(new Ship(x, y, length, orientation));

		for (int i = 0; i < length; i++) {
			if (orientation == Ship.Orientation.Horizontal) {
				map[x + i][y] = Ship.State.Alive_Ship_Horizontal;
			} else {
				map[x][y + i] = Ship.State.Alive_Ship_Vertical;
			}
		}
	}

	public void display() {
		System.out.print(" ");
		for (int i = 1; i <= size; i++) System.out.print(" " + i);
		System.out.println();

		for (int y = 0; y < size; y++) {
			System.out.print((char)('A' + y));

			for (int x = 0; x < size; x++) {
				System.out.print(" ");

				switch (stateAt(x, y)) {
					case Alive_Ship_Horizontal:
						System.out.print("━");
						break;
					case Alive_Ship_Vertical:
						System.out.print("┃");
						break;
					case Hit_Ship:
						System.out.print("X");
						break;
					case Water:
						System.out.print("·");
						break;
					case Miss:
						System.out.print("O");
						break;
				}
			}
			System.out.println();
		}
	}

	public Ship.State getShotAt(int x, int y) {
		switch (stateAt(x, y)) {
			case Alive_Ship_Horizontal:
			case Alive_Ship_Vertical:
				damageShipAt(x, y);
				return Ship.State.Alive_Ship_Horizontal;
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
}