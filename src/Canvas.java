import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

class Canvas extends JPanel {

	private static final long serialVersionUID = 756982496934224900L;

	// Constants
	private int screenWidth, screenHeight;
	private int mapSize;
	private static final int maxFood = 20;
	private static final int X = 0;
	private static final int Y = 1;
	private static final int SIZE = 2;

	// Score Frame
	// private ImageIcon img = new ImageIcon(getClass().getResource("test.png"));
	// private Image bImg = img.getImage();

	// Colors
	private Color cBackground = new Color(180, 180, 180);
	private Color cWorld = new Color(205, 205, 205);
	private Color cPlayer0 = new Color(170, 85, 85); // Red
	private Color cPlayer1 = new Color(120, 170, 85); // Green
	private Color cPlayer2 = new Color(85, 130, 170); // Blue
	private Color cPlayer3 = new Color(210, 220, 105); // Yellow
	private Color cPlayer4 = new Color(135, 95, 180); // Purple
	private Color cText = Color.WHITE;
	private Color cFood = new Color(110, 47, 47);

	// Player related
	private HashMap<Integer, int[]> players = new HashMap<Integer, int[]>();
	private int playerID;
	private int speed = 0;

	// Food related
	private int[][] foodPositions = new int[maxFood][2];

	public Canvas(int screenWidth, int screenHeight, int mapSize) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.mapSize = mapSize;
		setVisible(true);
		setBackground(cBackground);
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public void setScreen(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;
	}

	public void updateCoordinates(int pid, int x, int y, int score) {
		int[] info = { x, y, score };
		players.put(pid, info);

	}

	public void setFood(HashMap<Integer, Food> foodList) {
		for (Integer i : foodList.keySet()) {
			this.foodPositions[i][X] = foodList.get(i).getX();
			this.foodPositions[i][Y] = foodList.get(i).getY();
		}
	}

	public void setPlayerColor(Graphics g, int i) {
		switch (i) {
		case 0:
			g.setColor(cPlayer0);
			break;
		case 1:
			g.setColor(cPlayer1);
			break;
		case 2:
			g.setColor(cPlayer2);
			break;
		case 3:
			g.setColor(cPlayer3);
			break;
		case 4:
			g.setColor(cPlayer4);
			break;
		default:
			g.setColor(Color.black);
			break;
		}
	}

	public void paintScore(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setFont(new Font("Arial", Font.PLAIN, 12));
		Font currentFont = g2.getFont();
		Font newFont = currentFont.deriveFont(currentFont.getSize() * 1.0F);
		g2.setFont(newFont);
		g2.setColor(cText);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawString("player", 17, 26);
		g2.drawString("score", 17, 40);
		g2.drawString("speed", 17, 54);

		setPlayerColor(g2, playerID);
		g2.drawString("" + playerID, 80, 26);
		g2.drawString("" + players.get(playerID)[SIZE], 80, 40);
		g2.drawString("" + speed, 80, 54);
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paintComponent(g);
		int[] player = players.get(playerID);

		// Paint playable area
		g2.setColor(cWorld);
		if (player != null) {
			g2.fillRoundRect(-player[X] + (screenWidth / 2) - player[SIZE] / 2,
					-player[Y] + (screenHeight / 2) - player[SIZE] / 2, mapSize, mapSize, 25, 25);
		}

		// Paint all other players
		for (int i = 0; i < players.size(); i++) {
			if (i != playerID) {
				int[] otherPlayer = players.get(i);
				setPlayerColor(g2, i);
				if (otherPlayer != null) {
					g2.fillOval(otherPlayer[X] - player[X] + screenWidth / 2 - player[SIZE] / 2,
							otherPlayer[Y] - player[Y] + screenHeight / 2 - player[SIZE] / 2, otherPlayer[SIZE],
							otherPlayer[SIZE]);
				}
			}
		}

		// Paint your player
		setPlayerColor(g2, playerID);
		if (player != null) {
			g2.fillOval(screenWidth / 2 - player[SIZE] / 2, screenHeight / 2 - player[SIZE] / 2, player[SIZE],
					player[SIZE]);
		}

		// Paint the food
		g2.setColor(cFood);
		if (player != null) {
			for (int i = 0; i < maxFood; i++) {
				g2.fillOval(foodPositions[i][X] - player[X] + screenWidth / 2 - player[SIZE] / 2,
						foodPositions[i][Y] - player[Y] + screenHeight / 2 - player[SIZE] / 2, 5, 5);
			}
		}

		// Paint the score frame
		g2.setColor(new Color(0, 0, 0, 170));
		g2.fillRoundRect(10, 10, 130, 80, 15, 15);
		// g2.drawImage(bImg, 10, 10, this);

		// Paint your score
		paintScore(g);
	}
}