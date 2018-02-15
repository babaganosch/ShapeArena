import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

class Canvas extends JPanel {

	private static final long serialVersionUID = 756982496934224900L;
	private int[] x = new int[10];
	private int[] y = new int[10];
	private int[] size = new int[10];
	private int playerID;
	// Food related
	private int[] foodX = new int[20];
	private int[] foodY = new int[20];

	public Canvas() {
		setVisible(true);
		setBackground(Color.WHITE);
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public void updateCordinates(int pid, int x, int y, int score) {
		this.x[pid] = x;
		this.y[pid] = y;
		this.size[pid] = score;
	}

	public void paintFood(int i, int foodX, int foodY) {
		this.foodX[i] = foodX;
		this.foodY[i] = foodY;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (int i = 0; i < 10; i++) {
			// Player colors:
			switch (i) {
			case 0:
				g.setColor(Color.blue);
				break;
			case 1:
				g.setColor(Color.red);
				break;
			case 2:
				g.setColor(Color.green);
				break;
			case 3:
				g.setColor(Color.orange);
				break;
			case 4:
				g.setColor(Color.lightGray);
				break;
			default:
				g.setColor(Color.black);
				break;
			}
			g.fillOval(x[i], y[i], size[i], size[i]);
		}

		Graphics2D g2 = (Graphics2D) g;
		Font currentFont = g2.getFont();
		Font newFont = currentFont.deriveFont(currentFont.getSize() * 5.0F);
		g2.setFont(newFont);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawString("" + size[playerID], 70, 200);

		// Foods
		g.setColor(Color.red);
		for (int i = 0; i < 20; i++) {
			g.fillOval(foodX[i], foodY[i], 5, 5);
		}
	}
}