package clientSide;

public interface ClientConstants {

	// Constants
	static final int roomSize = 1000;
	static final int maxFoods = 20;
	static final int maxScore = 200;
	static final float maxAcceleration = 0.45f;
	static final float lowestFriction = 0.3f;
	static final int X = 0, Y = 1;
	static final int RIGHT = 0, LEFT = 1, DOWN = 2, UP = 3;
	static final int maxSpeed = 4;
	
	// Int array indexes
	static final int playerID = 0;
	static final int currentMaxSpeed = 1;
	static final int invincibilityTimer = 2;
	static final int score = 3;
	static final int shrinkTimer = 4;
	static final int growth = 5;
	static final int screenWidth = 6;
	static final int screenHeight = 7;
	static final int slowDownSending = 8;
	static final int lastX = 9;
	static final int lastY = 10;
		
}
