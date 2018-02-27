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
	private int maxPlayers = 10;
	private int maxFood = 20;

	// Player related
	private int[] x = new int[maxPlayers];
	private int[] y = new int[maxPlayers];
	private int[] size = new int[maxPlayers];
	private int playerID;

	// Food related
	private int[] foodX = new int[maxFood];
	private int[] foodY = new int[maxFood];

	public Canvas() {
		setVisible(true);
		setBackground(Color.WHITE);
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public void updateCoordinates(int pid, int x, int y, int score) {
		this.x[pid] = x;
		this.y[pid] = y;
		this.size[pid] = score;
	}
	
	public void setFood(HashMap<Integer, Food> foodList) {
		for (Integer i : foodList.keySet()) {
			this.foodX[i] = foodList.get(i).getX();
			this.foodY[i] = foodList.get(i).getY();
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
		g2.drawString("" + size[playerID], 70, 200);
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paintComponent(g);

		// Paint all the players
		for (int i = 0; i < maxPlayers; i++) {
			setPlayerColor(g2, i);
			g2.fillOval(x[i], y[i], size[i], size[i]);
		}

		// Paint your score
		paintScore(g);

		// Paint the food
		g2.setColor(Color.red);
		for (int i = 0; i < maxFood; i++) {
			g2.fillOval(foodX[i], foodY[i], 5, 5);
		}
	}
}