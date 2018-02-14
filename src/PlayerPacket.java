
public class PlayerPacket extends Packet {

	private static final long serialVersionUID = 5058081065501838682L;
	private int score;
	
	public PlayerPacket(int id, int x, int y, int score) {
		super(id, x, y);
		this.score = score;
	}
	
	public int getScore() {
		return this.score;
	}
}
