import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class ServerFrame extends JFrame implements Observer {
	
	private static final long serialVersionUID = -2942135701993632695L;
	
	private JTextArea textArea;

	public ServerFrame (int width, int height) {
		this.setLayout(null);
		this.setLocationRelativeTo(null);
		this.setSize(width, height);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
		
		this.textArea = new JTextArea(120,120);
		
	}

	public void update(Observable o, Object arg) {
		if(o instanceof Server && arg instanceof String) {
			textArea.setText((String)arg);
		}
	}
	
	
}
