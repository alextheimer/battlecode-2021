package player.util.battlecode.flag.fields;

public class IdField {
	public static final int NUM_BITS = 15;
	
	private int id;
	
	public IdField(int id) {
		// TODO(theimer): assertions
		this.id = id;
	}
	
	public int toBits() {
		return this.id;
	}
	
	public static IdField fromBits(int bits) {
		// TODO(theimer): !!!!!!!!!!
		return new IdField(bits);
	}
	
	public int value() {
		return this.id;
	}
}