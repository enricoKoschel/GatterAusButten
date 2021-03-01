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

	public static void shootAt(PlayField playField) {
		shootAt(playField, difficulty);
	}

	public static void shootAt(PlayField playField, Difficulty difficulty) {
		switch (difficulty) {
			case Easy:
				shootEasy(playField);
				break;
			case Middle:
				shootMiddle(playField);
				break;
		}
	}

	private static void shootEasy(PlayField playField) {
		//Shoot at random cell
		do {
			lastHitX = getRandomCell(playField);
			lastHitY = getRandomCell(playField);

			lastHitShotType = playField.getShotAt(lastHitX, lastHitY);
		} while (lastHitShotType == PlayField.ShotType.Invalid);
	}

	private static void shootMiddle(PlayField playField) {
		//Shoot at random cell where it is not impossible for a ship to be
		do {
			lastHitX = getRandomCell(playField);
			lastHitY = getRandomCell(playField);

			if (isShotPositionInvalid(lastHitX, lastHitY, playField)) {
				lastHitShotType = PlayField.ShotType.Invalid;
				continue;
			}

			lastHitShotType = playField.getShotAt(lastHitX, lastHitY);
		} while (lastHitShotType == PlayField.ShotType.Invalid);
	}

	private static boolean isShotPositionInvalid(int x, int y, PlayField playField) {
		//Surrounding ships (diagonals are ignored)
		//X,X  0,-1 X,X
		//-1,0 0,0 +1,0
		//X,X  0,+1 X,X

		return isSunkShipNoEdges(x, y - 1, playField) || isSunkShipNoEdges(x - 1, y, playField)
				|| isSunkShipNoEdges(x, y, playField) || isSunkShipNoEdges(x + 1, y, playField)
				|| isSunkShipNoEdges(x, y + 1, playField) || areSurroundingCellsInvalid(x, y, playField);
	}

	private static boolean areSurroundingCellsInvalid(int x, int y, PlayField playField) {
		//If there are two immediately adjacent cells, in the 8 surrounding cells, with hit ships in them, a ship in this position is impossible
		if (playField.getStateAtOutOfBoundsWater(x, y - 1) == Ship.State.Hit_Ship) {
			return playField.getStateAtOutOfBoundsWater(x - 1, y - 1) == Ship.State.Hit_Ship
					|| playField.getStateAtOutOfBoundsWater(x + 1, y - 1) == Ship.State.Hit_Ship;

		} else if (playField.getStateAtOutOfBoundsWater(x + 1, y) == Ship.State.Hit_Ship) {
			return playField.getStateAtOutOfBoundsWater(x + 1, y - 1) == Ship.State.Hit_Ship
					|| playField.getStateAtOutOfBoundsWater(x + 1, y + 1) == Ship.State.Hit_Ship;

		} else if (playField.getStateAtOutOfBoundsWater(x, y + 1) == Ship.State.Hit_Ship) {
			return playField.getStateAtOutOfBoundsWater(x - 1, y + 1) == Ship.State.Hit_Ship
					|| playField.getStateAtOutOfBoundsWater(x + 1, y + 1) == Ship.State.Hit_Ship;

		} else if (playField.getStateAtOutOfBoundsWater(x - 1, y) == Ship.State.Hit_Ship) {
			return playField.getStateAtOutOfBoundsWater(x - 1, y - 1) == Ship.State.Hit_Ship
					|| playField.getStateAtOutOfBoundsWater(x - 1, y + 1) == Ship.State.Hit_Ship;
		}

		return false;
	}

	private static boolean isSunkShipNoEdges(int x, int y, PlayField playField) {
		if (x < 0 || x >= playField.getSize() || y < 0 || y >= playField.getSize()) return false;

		return playField.getStateAt(x, y) == Ship.State.Sunk_Ship;
	}

	private static int getRandomCell(PlayField playField) {
		return (int)(Math.random() * playField.getSize());
	}

	public static void setDifficulty(Difficulty difficulty) {
		AI.difficulty = difficulty;
	}

	public static int getLastHitX() {
		return lastHitX;
	}

	public static int getLastHitY() {
		return lastHitY;
	}

	public static PlayField.ShotType getLastHitShotType() {
		return lastHitShotType;
	}
}