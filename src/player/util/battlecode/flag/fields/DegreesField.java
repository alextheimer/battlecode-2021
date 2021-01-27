package player.util.battlecode.flag.fields;

public class DegreesField {
	public static final int NUM_BITS = 9;
	
	private int degrees;
	
	public DegreesField(int degrees) {
		// TODO(theimer): assertions
		this.degrees = degrees;
	}
	
	public int toBits() {
		return this.degrees;
	}
	
	public static DegreesField fromBits(int bits) {
		return new DegreesField(bits);
	}
	
	public int value() {
		return this.degrees;
	}
}