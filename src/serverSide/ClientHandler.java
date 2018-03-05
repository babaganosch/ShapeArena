package serverSide;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

import packets.Food;
import packets.Packet;
import packets.PlayerInitializationPacket;
import packets.PlayerPacket;

/**
 * This is the ClientHandler class. There should be a ClientHandler for every
 * Client connected to the server. Every ClientHandler is a thread on the Server
 * side to handle all the writing and reading on the Object Streams to and from
 * the Client. The ClientHandler is observed by the ServerFrame.
 * 
 * @author Hasse Aro
 * @version 2018-03-xx
 */
public class ClientHandler extends Observable implements Runnable {

	// Thread
	private Thread activity = new Thread(this);

	// Global
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private ClientHandler[] clientHandler;
	private PacketHandler packetHandler;
	private boolean isReady = false;

	// Score
	HighscoreHandler highscoreHandler;
	private int topScore;

	// Player related
	private int playerID;

	/**
	 * Creates the ClientHandler.
	 * 
	 * @param socket
	 *            The socket it should listen on.
	 * @param user
	 *            A ClientHandler array containing all the ClientHandlers.
	 * @param pid
	 *            The PlayerID the Client should have.
	 * @param scorehandler
	 *            A reference to the HighscoreHandler.
	 * @param packetHandler
	 *            A reference to the PacketHandler.
	 * @param serverFrame
	 *            The observer.
	 * @throws Exception
	 *             Throws an exception if something went wrong creating the
	 *             ClientHandler.
	 */
	public ClientHandler(Socket socket, ClientHandler[] user, int pid, HighscoreHandler scorehandler,
			PacketHandler packetHandler, Observer serverFrame) throws Exception {

		this.clientHandler = user;
		this.playerID = pid;
		this.socket = socket;
		this.highscoreHandler = scorehandler;
		this.packetHandler = packetHandler;
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.out.flush();
		this.in = new ObjectInputStream(socket.getInputStream());
		addObserver(serverFrame);

		activity.start();
	}

	/**
	 * Getter for the ObjectOutputStream.
	 * 
	 * @return Returns the ObjectOutputStream.
	 */
	public ObjectOutputStream getObjectOutputStream() {
		return out;
	}

	/**
	 * Tells the caller if the ClientHandler is ready.
	 * 
	 * @return Returns a boolean representing the ready-status of the ClientHandler.
	 */
	public boolean isReady() {
		return isReady;
	}

	/**
	 * Tells the Client who it is, sending out a PlayerInitializationPacket with the
	 * playerID. After that it waits for 32ms and sets the ready boolean to true.
	 */
	public synchronized void initializeClient() {
		// Send out playerID as a packet so the client know who he is.
		try {
			out.writeObject(new PlayerInitializationPacket(playerID));
			out.flush();
		} catch (IOException e) {
			System.out.println("Failed to send playerID");
		}

		try {
			Thread.sleep(32);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		isReady = true;
	}

	/**
	 * Getter for the socket.
	 * 
	 * @return Returns the Socket.
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * Getter for the playerID.
	 * 
	 * @return Returns an int representing the playerID.
	 */
	public int getId() {
		return playerID;
	}

	/**
	 * Shows how many clients are connected to the server, and some information
	 * about them.
	 * 
	 * @return Returns a String containing information about every Client connected
	 *         to the Server.
	 */
	public String getConnectedUsers() {
		String message = "";
		for (ClientHandler i : clientHandler) {
			if (i != null) {
				message += "Connected: " + i.getSocket().getInetAddress().getHostAddress() + " with ID: " + i.playerID
						+ System.lineSeparator();
			}
		}
		return message;
	}

	public void run() {

		// Tell the client what playerID it is.
		initializeClient();

		while (true) {

			// Highscore handling
			highscoreHandler.setTopScore(topScore);

			try {

				Packet packet = null;

				// Receive packet
				try {
					packet = (Packet) in.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				if (packet instanceof Food) {

					// Unpack the packet
					Food food = (Food) packet;
					packetHandler.updateFood(food);

				}
				// Forward the packet to the other Users
				if (packet instanceof PlayerPacket) {
					PlayerPacket playerPacket = (PlayerPacket) packet;
					int score = playerPacket.getScore();
					if (score > topScore) {
						topScore = score;
					}
					packetHandler.addPacket(playerPacket);
				}

			} catch (IOException e) {

				// Disconnect
				clientHandler[playerID] = null;
				System.out.println("Disconnection from: " + socket.getInetAddress() + ", with a PID: " + playerID);

				setChanged();
				notifyObservers(getConnectedUsers());

				break;
			}
		}
	}
}