package com.partnerundpartner;

public final class AI {
	enum Difficulty {
		Easy,
		Middle,
		Hard,
		Impossible
	}

	private static Difficulty difficulty = Difficulty.Easy;
	private static int lastHitX = -1;
	private static int lastHitY = -1;
	private static PlayField.ShotType lastHitShotType = PlayField.ShotType.Invalid;

	private AI() {
		throw new UnsupportedOperationException("AI class cannot be instantiated!");
	}

	public static void shootAt(PlayField playField){
		shootAt(playField, difficulty);
	}

	public static void shootAt(PlayField playField, Difficulty difficulty) {
		switch (difficulty){
			case Easy:
				do {
					lastHitX = (int)(Math.random() * playField.getSize());
					lastHitY = (int)(Math.random() * playField.getSize());

					lastHitShotType = playField.getShotAt(lastHitX, lastHitY);
				} while (lastHitShotType == PlayField.ShotType.Invalid);
				break;
		}
	}

	public static void setDifficulty(Difficulty difficulty){
		AI.difficulty = difficulty;
	}

	public static int getLastHitX(){
		return lastHitX;
	}

	public static int getLastHitY(){
		return lastHitY;
	}

	public static PlayField.ShotType getLastHitShotType(){
		return lastHitShotType;
	}
}