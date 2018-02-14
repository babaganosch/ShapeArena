import java.io.Serializable;

public abstract class Packet implements Serializable {
	
	private static final long serialVersionUID = 5058081065501838682L;
	private int x, y, id;
	
	public Packet(int id, int x, int y) {
		this.id = id;
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
}
