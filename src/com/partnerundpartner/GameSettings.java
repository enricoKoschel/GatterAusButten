package com.partnerundpartner;

import java.util.HashMap;

public class GameSettings {
	public int width;
	public boolean forceAspectRatio;
	public int playFieldSize;
	public AI.Difficulty difficulty;
	public boolean turnOrder;
	public HashMap<Integer, Integer> shipAmounts;

	public GameSettings(int width, boolean forceAspectRatio, int playFieldSize, AI.Difficulty difficulty,
						boolean turnOrder, HashMap<Integer, Integer> shipAmounts) {
		this.width = width;
		this.forceAspectRatio = forceAspectRatio;
		this.playFieldSize = playFieldSize;
		this.difficulty = difficulty;
		this.turnOrder = turnOrder;
		this.shipAmounts = shipAmounts;
	}
}