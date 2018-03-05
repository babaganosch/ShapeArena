package serverSide;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * This is the HighscoreHandler class.
 * Responisble for writing a new highscore to a local file on the server.
 * @author Aron Bengtsson
 * @version 2018-03-05
 */
public class HighscoreHandler extends Thread {

	private int topScore;
	private File storedFile;

	/**
	 * Creates a HighscoreHandler object.
	 */
	public HighscoreHandler() {
		this.topScore = 0;
		start();
	}

	/**
	 * Updates the topScore variable if what has been received
	 * being more than what's stored.
	 * @param inScore The score to update with.
	 */
	public synchronized void setTopScore(int inScore) {
		// If score in to method is higher then stored score, update it
		if (inScore > this.topScore) {
			this.topScore = inScore;
		}
	}

	/**
	 * Returns the top score of the server.
	 * @return Returns the topScore variable.
	 */
	public int getTopScore() {
		// Take my money!!
		return topScore;
	}

	/**
	 * Opens the file highscore.txt and creates it
	 * if it doesn't exist.
	 * @throws IOException Throws the IOException if something went wrong.
	 */
	public void openFile() throws IOException {
		// Create the file highscore.txt
		storedFile = new File("highscore.txt");
		storedFile.createNewFile();
	}
	
	/**
	 * Returns the stored highscore from a file.
	 * @return Returns the score from the highscore.txt
	 * @throws FileNotFoundException Throws exception if it can't open the file.
	 */
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

	/**
	 * Prints a new highscore to the file.
	 * @param newHighscore The score we would like to print to the file.
	 * @throws FileNotFoundException	Throws exception if we can't find the file.
	 */
	public void updateStoredHighscore(int newHighscore) throws FileNotFoundException {
		
		// Open up a PrintWriter so we can write to the file
		PrintWriter writer = new PrintWriter(storedFile);
		
		// Write the score that's given to the method
		writer.print("" + newHighscore);
		writer.close();
	}
	
	/**
	 * Every second checks if current highscore is higher than stored highscore if it is then it updates the highscore.
	 */
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
