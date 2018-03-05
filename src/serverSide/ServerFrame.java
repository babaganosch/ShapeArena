package serverSide;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * This is the Server Frame, a GUI for the server.
 * It acts as a Observer for Server, ClientHandler and PacketHandler.
 * @author André Höjmark
 * @version 2018-03-05
 */
public class ServerFrame extends JFrame implements Observer, MouseListener, MouseMotionListener {

	private static final long serialVersionUID = -3024245174916363989L;

	private JTextArea textAreaConnections;
	private JTextField textFieldFooter;
	private JTextField textFieldBloat;
	private JPanel topBar;

	// Changed the color-theme to a less shrieking color, left the old color-codes though.
	private Color backgroundColor = new Color(70, 70, 70); // (50, 50, 50) <- Dark Gray
	private Color topBarColor = new Color(50, 45, 45); // (249, 65, 32) <- Orange
	private Color textColor = new Color(205, 205, 205);
	private Font font = new Font("Arial", Font.BOLD, 12);

	private int lastX, lastY;

	/**
	 * Creates a new ServerFrame object.
	 */
	public ServerFrame() {

		setTitle("Server");
		setSize(300, 300);
		setLayout(new BorderLayout());
		setUndecorated(true);
		setLocationRelativeTo(null);

		topBar = new JPanel(new BorderLayout());
		topBar.addMouseListener(this);
		topBar.addMouseMotionListener(this);

		JLabel close = new JLabel("×");
		close.setBorder(new EmptyBorder(0, 0, 0, 10));
		close.setFont(new Font("Arial", Font.BOLD, 20));
		close.setForeground(Color.GRAY);
		close.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				System.exit(0);
			}

			public void mouseEntered(MouseEvent e) {
				close.setForeground(Color.WHITE);
			}

			public void mouseExited(MouseEvent e) {
				close.setForeground(Color.GRAY);
			}
		});
		
		topBar.add(close, BorderLayout.EAST);
		topBar.setBackground(topBarColor);
		
		JLabel status = new JLabel("Server status: Online");
		status.setForeground(Color.white);
		status.setBorder(new EmptyBorder(0, 10, 0, 0));
		topBar.add(status, BorderLayout.CENTER);
		
		this.add(topBar, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel();
		JPanel southPanel = new JPanel(new BorderLayout());
		centerPanel.setBackground(backgroundColor);
		southPanel.setBackground(backgroundColor);

		add(southPanel, BorderLayout.SOUTH);
		add(centerPanel, BorderLayout.CENTER);

		textFieldBloat = new JTextField();
		textFieldBloat.setEditable(false);
		textFieldBloat.setFont(font);
		textFieldBloat.setOpaque(false);
		textFieldBloat.setForeground(textColor);
		textFieldBloat.setHorizontalAlignment(JLabel.CENTER);
		textFieldBloat.setBorder(null);
		southPanel.add(textFieldBloat, BorderLayout.NORTH);

		textFieldFooter = new JTextField();
		textFieldFooter.setEditable(false);
		textFieldFooter.setFont(font);
		textFieldFooter.setOpaque(false);
		textFieldFooter.setForeground(textColor);
		textFieldFooter.setHorizontalAlignment(JLabel.CENTER);
		textFieldFooter.setBorder(new EmptyBorder(10, 0, 10, 0));
		southPanel.add(textFieldFooter, BorderLayout.SOUTH);

		textAreaConnections = new JTextArea();
		textAreaConnections.setEditable(false);
		textAreaConnections.setFont(font);
		textAreaConnections.setOpaque(false);
		textAreaConnections.setForeground(textColor);
		centerPanel.add(textAreaConnections);

		textAreaConnections.setText("Server is empty.");

		setVisible(true);
	}
	
	/**
	 * Updates the textAreaConnections with connected clients and textFieldBloat with how many messagaes that are waiting to be sent and textFieldFooter with the current
	 * port the server listens on.
	 * 
	 * @param src is the observable instance that sent information to the observer.
	 * @param arg is the argument that the observable instance sent to the observer.
	 */
	public void update(Observable src, Object arg) {
		if (src instanceof ClientHandler || src instanceof Server && arg instanceof String) {
			textAreaConnections.setText((String) arg);

			if (textAreaConnections.getText().equals("")) {
				textAreaConnections.setText("Server is empty.");
			}
		} else if (src instanceof Server && arg instanceof Integer) {
			textFieldFooter.setText("Listening on port: " + arg);
		} else if (src instanceof PacketHandler && arg instanceof Integer) {
			if ((int) arg < 10) {
				textFieldBloat.setText("Server packet bloat: " + arg);
			} else {
				textFieldBloat.setText("Server packet bloat: " + arg + "  WARNING !!");
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