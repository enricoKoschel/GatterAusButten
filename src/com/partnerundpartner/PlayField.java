package com.partnerundpartner;

import java.util.ArrayList;

public class PlayField {
	private final int size;
	private final Ship.State[][] map;
	private final ArrayList<Ship> ships;

	public PlayField(int size) {
		ships = new ArrayList<Ship>();
		map = new Ship.State[size][size];

		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) map[x][y] = Ship.State.Water;
		}

		this.size = size;
	}

	public Ship.State stateAt(int x, int y) {
		return map[x][y];
	}

	public void changeState(int x, int y, Ship.State state) {

	}

	public void addShip(int x, int y, int length, Ship.Orientation orientation) {
		ships.add(new Ship(x, y, length, orientation));

		for (int i = 0; i < length; i++) {
			if (orientation == Ship.Orientation.Horizontal) {
				map[x + i][y] = Ship.State.Alive_Ship;
			} else {
				map[x][y + i] = Ship.State.Alive_Ship;
			}
		}
	}

	public void display() {
		System.out.print(" ");
		for (int i = 0; i < size; i++) System.out.print(" " + (char)('A' + i));
		System.out.println();

		for (int y = 0; y < size; y++) {
			System.out.print(y + 1);

			for (int x = 0; x < size; x++) {
				System.out.print(" ");

				switch (stateAt(x, y)) {
					case Alive_Ship:
						System.out.print("#");
						break;
					case Dead_Ship:
						System.out.print("X");
						break;
					case Water:
						System.out.print(".");
						break;
				}
			}
			System.out.println();
		}
	}

	public Ship.State getShotAt(int x, int y) {
		Ship.State state = stateAt(x, y);

		if (state == Ship.State.Alive_Ship) {
			//Ship hit
			System.out.println("HIT");
		} else if (state == Ship.State.Dead_Ship) {
			//???????????????
			System.out.println("??????");
		} else {
			//Miss
			System.out.println("MISS");
		}

		return Ship.State.Water;
	}
}