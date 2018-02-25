import java.awt.BorderLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ServerFrame extends JFrame implements Observer {
	
	private static final long serialVersionUID = -3024245174916363989L;
	
	private JTextArea textArea;
	private JTextArea textAreaHeader;
	private String serverEmpty = "Server is empty.";
	
	public ServerFrame (int width, int height) {
		
		setTitle("Server");
		setSize(width, height);
		setLayout(new BorderLayout());

		JPanel panelwest = new JPanel();
		JPanel panelnorth = new JPanel();
		add(panelnorth, BorderLayout.NORTH);
		add(panelwest, BorderLayout.WEST);
		
		textAreaHeader = new JTextArea();
		textAreaHeader.setEditable(false);
		textAreaHeader.setBackground(panelnorth.getBackground());
		panelnorth.add(textAreaHeader);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
        textArea.setBackground(panelwest.getBackground());
        panelwest.add(textArea);
        
        textArea.setText(serverEmpty);
		
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public void update(Observable src, Object arg) {
		if(src instanceof User || src instanceof Server && arg instanceof String) {
			textArea.setText((String)arg);
			
			if (textArea.getText().equals("")) {
				textArea.setText(serverEmpty);
			}
		} else if(src instanceof Server && arg instanceof Integer) {
			textAreaHeader.setText("Server status: online" + System.lineSeparator() + "Listening on port: " + arg);
		}
	}
}