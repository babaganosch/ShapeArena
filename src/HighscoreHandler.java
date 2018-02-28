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
		// If score in to method is higher then stored score, update it
		if (inScore > this.topScore) {
			this.topScore = inScore;
		}
	}

	public int getTopScore() {
		// Take my money!!
		return topScore;
	}

	public void openFile() throws IOException {
		// Create the file highscore.txt
		storedFile = new File("highscore.txt");
		storedFile.createNewFile();
	}
	
	public int getStoredHighscore() throws FileNotFoundException {
		
		// Create scanner and scan through our highscore.txt
		Scanner sc = new Scanner(storedFile);
		int maxHighscore = 0;
		
		// Update maxHighscore with what's in the file
		while(sc.hasNextInt()) {
			maxHighscore = sc.nextInt();
		}
		
		// Close the scanner return what we found in the file
		sc.close();
		return maxHighscore;
	}

	public void updateStoredHighscore(int newHighscore) throws FileNotFoundException {
		
		// Open up a PrintWriter so we can write to the file
		PrintWriter writer = new PrintWriter(storedFile);
		
		// Write the score that's given to the method
		writer.print("" + newHighscore);
		writer.close();
	}
	
	public void run() {
		while (true) {
			
			try {
				// Create the file highscore.txt and update it with what's written in it.
				openFile();
				int storedHighscore = getStoredHighscore();
				// If we're given a higher topScore, update highscore.txt with it.
				if (storedHighscore < topScore) {
					updateStoredHighscore(topScore);
					System.out.println("Printing " + topScore + " to file.");
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				
			}
			
			// Sleep
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
