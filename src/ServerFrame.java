import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ServerFrame extends JFrame implements Observer, MouseListener, MouseMotionListener {
	
	private static final long serialVersionUID = -3024245174916363989L;
	
	private JTextArea textAreaConnections;
	private JTextArea textAreaFooter;
	private JTextArea textAreaBloat;
	private JPanel topBar;
	private JLabel Title;
	
	private Color backgroundColor = new Color(50, 50, 50);
	private Color foregroundColor = new Color(249, 65, 32);
	private Color textColor = new Color(205, 205, 205);
	private Font font = new Font("Arial", Font.BOLD, 12);
	
	private int lastX, lastY;
	
	public ServerFrame (int x, int y) {
		
		setTitle("Server");
		setSize(300, 300);
		setLayout(new BorderLayout());
		setUndecorated(true);
		
		topBar = new JPanel(new BorderLayout());
		topBar.addMouseListener(this);
		topBar.addMouseMotionListener(this);
		
		Title = new JLabel("Server");
		Title.setHorizontalAlignment(JLabel.CENTER);
		Title.setFont(new Font("Arial", Font.BOLD, 20));
		topBar.setBackground(foregroundColor);
		topBar.add(Title, BorderLayout.CENTER);
		this.add(topBar, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		JPanel southPanel = new JPanel(new GridLayout(2, 1));
		centerPanel.setBackground(backgroundColor);
		southPanel.setBackground(backgroundColor);
		
		add(southPanel, BorderLayout.SOUTH);
		add(centerPanel, BorderLayout.CENTER);
		
		textAreaBloat = new JTextArea();
		textAreaBloat.setEditable(false);
		textAreaBloat.setFont(font);
		textAreaBloat.setOpaque(false);
		textAreaBloat.setForeground(textColor);
		southPanel.add(textAreaBloat);
		
		textAreaFooter = new JTextArea();
		textAreaFooter.setEditable(false);
		textAreaFooter.setFont(font);
		textAreaFooter.setOpaque(false);
		textAreaFooter.setForeground(textColor);
		southPanel.add(textAreaFooter);
		
		
		textAreaConnections = new JTextArea();
		textAreaConnections.setEditable(false);
		textAreaConnections.setFont(font);
		textAreaConnections.setOpaque(false);
		textAreaConnections.setForeground(textColor);
		centerPanel.add(textAreaConnections);
		
		textAreaConnections.setText("Server is empty.");
		
		setVisible(true);
	}

	public void update(Observable src, Object arg) {
		if (src instanceof ClientHandler || src instanceof Server && arg instanceof String) {
			textAreaConnections.setText((String) arg);
			
			if (textAreaConnections.getText().equals("")) {
				textAreaConnections.setText("Server is empty.");
			}
		} else if (src instanceof Server && arg instanceof Integer) {
			textAreaFooter.setText("Server status: online" + System.lineSeparator() + "Listening on port: " + arg);
		} else if (src instanceof PacketHandler && arg instanceof Integer) {
			if ((int) arg < 10) {
				textAreaBloat.setText("Server packet bloat: " + arg);
			} else {
				textAreaBloat.setText("Server packet bloat: " + arg + "  WARNING !!");
			}
			
		}
	}

	public void actionPerformed(ActionEvent aE) {
	}
	public void mouseReleased(MouseEvent mE) {
	}
	public void mouseDragged(MouseEvent mE) {
		if ((JPanel) mE.getSource() == topBar) {
			int x = mE.getXOnScreen();
			int y = mE.getYOnScreen();
			setLocation(getLocationOnScreen().x + x - lastX, getLocationOnScreen().y + y - lastY);
			lastX = x;
			lastY = y;
		}
	}
	public void mouseMoved(MouseEvent mE) {
	}
	public void mouseClicked(MouseEvent mE) {
	}
	public void mousePressed(MouseEvent mE) {
		Object object = mE.getSource();
		if (object instanceof JPanel) {
			if ((JPanel) mE.getSource() == topBar) {
				lastX = mE.getXOnScreen();
				lastY = mE.getYOnScreen();
			}
		}
	}
	public void mouseEntered(MouseEvent mE) {
	}
	public void mouseExited(MouseEvent mE) {
	}
}