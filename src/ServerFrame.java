import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ServerFrame extends JFrame implements Observer {
	private JTextArea textArea;

	public ServerFrame (int width, int height) {
		
		setTitle("Server");
		setSize(width, height);
		setLayout(new BorderLayout());

		JPanel panelwest = new JPanel();
		add(panelwest, BorderLayout.WEST);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
        textArea.setBackground(panelwest.getBackground());
        panelwest.add(textArea);
		
		
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public void update(Observable src, Object arg) {
		if(src instanceof User || src instanceof Server && arg instanceof String) {
			// System.out.println("hej" + (String)arg);
			textArea.setText((String)arg);
		}
	}
}
