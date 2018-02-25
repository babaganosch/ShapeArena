import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class HighscoreHandler extends Thread {

	private int topScore;
	private File storedFile;

	public HighscoreHandler() {
		this.topScore = 0;
		start();
	}

	public synchronized void setTopScore(int inScore) {
		if (inScore > this.topScore) {
			this.topScore = inScore;
		}
	}

	public int getTopScore() {
		return topScore;
	}

	public void openFile() throws IOException {
		storedFile = new File("highscore.txt");
		storedFile.createNewFile();
	}
	
	public int getStoredHighscore() throws FileNotFoundException {
		Scanner sc = new Scanner(storedFile);
		int maxHighscore = 0;
		while(sc.hasNextInt()) {
			maxHighscore = sc.nextInt();
		}
		sc.close();
		return maxHighscore;
	}

	public void updateStoredHighscore(int newHighscore) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(storedFile);
		writer.print("" + newHighscore);
		writer.close();
	}
	
	public void run() {
		while (true) {
			//System.out.println("Highscore: " + topScore);
			
			try {
				openFile();
				int storedHighscore = getStoredHighscore();
				if (storedHighscore < topScore) {
					updateStoredHighscore(topScore);
					System.out.println("Printing " + topScore + " to file.");
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			/*
			 * 1. Check if file exists, else create one
			 * 2. Read from file
			 * 3. Print to file
			 */
		}
	}

}
