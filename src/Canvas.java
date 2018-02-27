import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;

import javax.swing.JPanel;

class Canvas extends JPanel {

	private static final long serialVersionUID = 756982496934224900L;

	// Global
	private int maxFood = 20;
	private int screenWidth, screenHeight;
	private Color backgroundColor = new Color(150, 150, 150);
	private Color worldColor = new Color(200, 200, 200);
	private int mapSize;
	private static final int X = 0;
	private static final int Y = 1;
	private static final int SIZE = 2;

	// Player related
	private HashMap<Integer, int[]> players = new HashMap<Integer, int[]>();
	private int playerID;

	// Food related
	private int[][] foodPositions = new int[maxFood][2];

	public Canvas(int screenWidth, int screenHeight, int mapSize) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.mapSize = mapSize;
		setVisible(true);
		setBackground(backgroundColor);
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
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
		case 1:
			g.setColor(Color.blue);
			break;
		case 2:
			g.setColor(Color.red);
			break;
		case 3:
			g.setColor(Color.green);
			break;
		case 4:
			g.setColor(Color.orange);
			break;
		case 5:
			g.setColor(Color.lightGray);
			break;
		default:
			g.setColor(Color.black);
			break;
		}
	}

	public void paintScore(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Font currentFont = g2.getFont();
		Font newFont = currentFont.deriveFont(currentFont.getSize() * 5.0F);
		g2.setFont(newFont);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawString("" + players.get(playerID)[2], 70, 200);
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paintComponent(g);
		int[] player = players.get(playerID);
		
		// Paint playable area
		g2.setColor(worldColor);
		g2.fillRect(-player[X] + (screenWidth / 2) - player[SIZE] / 2, -player[Y] + (screenHeight / 2) - player[SIZE] / 2, mapSize, mapSize);

		// Paint all other players
		for (int i = 0; i < players.size(); i++) {
			if (i != playerID) {
				int[] otherPlayer = players.get(i);
				setPlayerColor(g2, i);
				g2.fillOval(otherPlayer[X] - player[X] + screenWidth / 2 - player[SIZE] / 2,
						otherPlayer[Y] - player[Y] + screenHeight / 2 - player[SIZE] / 2, otherPlayer[SIZE], otherPlayer[SIZE]);
			}
		}
		// Paint your player
		setPlayerColor(g2, playerID);
		g2.fillOval(screenWidth / 2 - player[SIZE] / 2, screenHeight / 2 - player[SIZE] / 2, player[SIZE], player[SIZE]);

		// Paint your score
		paintScore(g);

		// Paint the food
		g2.setColor(Color.red);
		for (int i = 0; i < maxFood; i++) {
			g2.fillOval(foodPositions[i][X] - player[X] + screenWidth / 2 - player[SIZE] / 2,
					foodPositions[i][Y] - player[Y] + screenHeight / 2 - player[SIZE] / 2, 5, 5);
		}
	}
}