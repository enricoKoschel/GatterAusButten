package com.partnerundpartner;

import java.util.ArrayList;

public class PlayField {
	private final int size;
	ArrayList<Ship> ships;

	public PlayField(int size) {
		ships = new ArrayList<Ship>();

		this.size = size;
	}

	public Ship.State stateAt(int x, int y) {
		for (Ship ship : ships) {
			if (ship.getOrientation() == Ship.Orientation.Vertical) {
				for (int i = 0; i < ship.getLength(); i++) {
					if (ship.getX() == x && ship.getY() + i == y) {
						return ship.partAt(y - ship.getY());
					}
				}
			} else {
				for (int i = 0; i < ship.getLength(); i++) {
					if (ship.getX() + i == x && ship.getY() == y) {
						return ship.partAt(x - ship.getX());
					}
				}
			}
		}
		return Ship.State.Water;
	}

	public void changeState(int x, int y, Ship.State state) {

	}

	public void addShip(int x, int y, int length, Ship.Orientation orientation) {
		ships.add(new Ship(x, y, length, orientation));
	}

	public void display() {
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
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