import java.io.Serializable;

public abstract class Packet implements Serializable {
	
	private static final long serialVersionUID = -8229001052268205876L;
	private int id;
	
	public Packet(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
}
