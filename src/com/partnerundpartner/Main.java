package com.partnerundpartner;

public class Main {
	public static void main(String[] args) {
		PlayField ownField = new PlayField(7);
		ownField.addShip(2, 1, 2, Ship.Orientation.Vertical);
		ownField.addShip(2, 3, 3, Ship.Orientation.Horizontal);
		ownField.display();

		//PlayField enemyField = new PlayField(7);
	}
}