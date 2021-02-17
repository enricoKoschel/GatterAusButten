package com.partnerundpartner;

public class Ship {
	public enum State {
		Living_Ship,
		Sunk_Ship,
		Hit_Ship,
		Water,
		Miss
	}

	public enum Orientation {
		Horizontal,
		Vertical
	}

	private final int x;
	private final int y;
	private final int length;
	private final Orientation orientation;
	private int health;

	public Ship(int x, int y, int length, Orientation orientation) {
		this.x = x;
		this.y = y;
		this.length = length;
		this.orientation = orientation;

		health = length;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getLength() {
		return length;
	}

	public void getDamaged(){
		health--;
	}

	public int getHealth(){
		return health;
	}

	public Orientation getOrientation() {
		return orientation;
	}
}