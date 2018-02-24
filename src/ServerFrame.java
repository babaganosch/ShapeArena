import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;

public class ServerFrame extends JFrame implements Observer {
	
	private static final long serialVersionUID = -2942135701993632695L;

	public ServerFrame (int width, int height) {
		this.setLayout(null);
		this.setLocationRelativeTo(null);
		this.setSize(width, height);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public void update(Observable o, Object arg) {
		
	}
	
	
}
