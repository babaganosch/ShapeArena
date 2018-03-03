import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class Launcher extends JFrame implements MouseListener, MouseMotionListener, ActionListener {

	
	private static final long serialVersionUID = 1L;
	private Color backgroundColor = new Color(50, 50, 50);
	private Color foregroundColor = new Color(249, 65, 32);
	private Border border = BorderFactory.createMatteBorder(20, 20, 20, 20, backgroundColor);
	private Font font = new Font("Arial", Font.BOLD, 20);
	private JPanel topBar;
	private JTextField ip;
	private JButton startServer, connect;

	private int lastX, lastY, width = 200, height = 190;

	public Launcher() {
		this.setTitle("Launcher");
		this.setSize(width, height);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setUndecorated(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.getContentPane().setBackground(backgroundColor);
		this.setLayout(new BorderLayout());
		this.setupComponents();
		this.setVisible(true);
	}
	
	//Sets up all the buttons and the textArea for the IP
	public void setupComponents() {
		topBar = new JPanel(new BorderLayout());
		topBar.addMouseListener(this);
		topBar.addMouseMotionListener(this);

		JLabel close = new JLabel("×");
		close.setBorder(new EmptyBorder(0, 0, 0, 20));
		close.setFont(new Font("Arial", Font.BOLD, 20));
		close.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				System.exit(0);
			}

			public void mouseEntered(MouseEvent e) {
				close.setForeground(Color.WHITE);
			}

			public void mouseExited(MouseEvent e) {
				close.setForeground(backgroundColor);
			}
		});
		JLabel minimize = new JLabel("-");
		minimize.setBorder(new EmptyBorder(0, 0, 0, 20));
		minimize.setFont(new Font("Arial", Font.BOLD, 40));
		minimize.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				System.exit(0);
			}

			public void mouseEntered(MouseEvent e) {
				close.setForeground(Color.WHITE);
			}

			public void mouseExited(MouseEvent e) {
				close.setForeground(backgroundColor);
			}
		});
		topBar.setBackground(foregroundColor);
		topBar.add(close, BorderLayout.EAST);
		this.add(topBar, BorderLayout.NORTH);

		JPanel wrapper = new JPanel(new GridLayout(3, 1, 0, 20));
		wrapper.setBorder(border);
		wrapper.setBackground(backgroundColor);

		ip = new JTextField("localhost");
		ip.setFont(font);
		ip.setHorizontalAlignment(JLabel.CENTER);
		ip.setBorder(null);
		ip.setBackground(new Color(205, 205, 205));
		wrapper.add(ip);

		connect = new JButton("Connect");
		connect.setFont(font);
		connect.setBackground(foregroundColor);
		connect.setBorder(null);
		connect.setFocusPainted(false);
		connect.addActionListener(this);
		wrapper.add(connect);

		startServer = new JButton("Start Server");
		startServer.setFont(font);
		startServer.setBackground(foregroundColor);
		startServer.setBorder(null);
		startServer.setFocusPainted(false);
		startServer.addActionListener(this);
		wrapper.add(startServer);

		this.add(wrapper, BorderLayout.CENTER);
	}
	
	
	public static void main(String arg[]) {
		new Launcher();
	}
	
	//To be able to drag the window around
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

	public void mouseEntered(MouseEvent mE) {
	}

	public void mouseExited(MouseEvent mE) {
	}

	public void mousePressed(MouseEvent mE) {
		if ((JPanel) mE.getSource() == topBar) {
			lastX = mE.getXOnScreen();
			lastY = mE.getYOnScreen();
		}
	}

	public void actionPerformed(ActionEvent aE) {
		JButton button = (JButton) aE.getSource();

		if (button == startServer) {
			try {
				new Server(this.getLocationOnScreen().x + this.width + 20, this.getLocationOnScreen().y);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (button == connect)
			new Client(ip.getText());
	}

	public void mouseReleased(MouseEvent e) {
	}
}
