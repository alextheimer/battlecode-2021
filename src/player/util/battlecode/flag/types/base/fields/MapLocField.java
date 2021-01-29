package player.util.battlecode.flag.types.base.fields;

import battlecode.common.MapLocation;
import player.util.battlecode.UtilBattlecode;
import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagField;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagFieldFactory;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.math.IntVec2D;
import player.util.math.UtilMath;

public class MapLocField implements BaseFlag.IFlagField {
	
	static {
		// TODO(theimer): confirm this
		assert UtilMath.isPow2(UtilBattlecode.MAX_WORLD_WIDTH) : 
			"+1 in NUM_BITS_PER_DIM applies only when MAX_WORLD_WIDTH is a power of two";
	}
	
	// TODO(theimer): explain
	private static final int NUM_BITS_PER_DIM = UtilMath.log2Ceil(UtilBattlecode.MAX_WORLD_WIDTH) + 1;
	public static final int NUM_BITS = NUM_BITS_PER_DIM * 2;
	
	private int xSuffixBits;
	private int ySuffixBits;
	
	public MapLocField(MapLocation mapLoc) {
		assert mapLoc.x >= 0 : "this should never fail";
		assert mapLoc.y >= 0 : "this should never fail";
		
		this.xSuffixBits = getSuffixBits(mapLoc.x);
		this.ySuffixBits = getSuffixBits(mapLoc.y);
	}
	
	private MapLocField(int xSuffixBits, int ySuffixBits) {
		assert xSuffixBits >= 0 : "xSuffixBits: " + xSuffixBits;
		assert ySuffixBits >= 0 : "ySuffixBits: " + ySuffixBits;
		this.xSuffixBits = xSuffixBits;
		this.ySuffixBits = ySuffixBits;
	}
	
	private static int getSuffixBits(int val) {
		final int mask = (1 << NUM_BITS_PER_DIM) - 1;
		return val & mask;
	}
	
	// TODO(theimer): rename all of these to decode() for consistency's sake
	public static MapLocField fromBits(int bits) {
		FlagWalker flagWalker = new FlagWalker(bits);
		int xSuffixBits = flagWalker.readBits(NUM_BITS_PER_DIM);
		int ySuffixBits = flagWalker.readBits(NUM_BITS_PER_DIM);
		return new MapLocField(xSuffixBits, ySuffixBits);
	}
	
	public MapLocation getMapLocation(MapLocation referenceMapLoc) {
		assert referenceMapLoc.x >= 0 : "this should never fail";
		assert referenceMapLoc.y >= 0 : "this should never fail";
		
		final int modVal = 1 << NUM_BITS_PER_DIM;
		int xReferenceSuffixBits = getSuffixBits(referenceMapLoc.x);
		int yReferenceSuffixBits= getSuffixBits(referenceMapLoc.y);
		int xDiff = UtilMath.diffMod(this.xSuffixBits, xReferenceSuffixBits, modVal);
		int yDiff = UtilMath.diffMod(this.ySuffixBits, yReferenceSuffixBits, modVal);
		
		assert (Math.abs(xDiff) >= 0) && (Math.abs(xDiff) < UtilBattlecode.MAX_WORLD_WIDTH);
		assert (Math.abs(yDiff) >= 0) && (Math.abs(yDiff) < UtilBattlecode.MAX_WORLD_WIDTH);
		
		return new MapLocation(referenceMapLoc.x + xDiff, referenceMapLoc.y + yDiff);
	}

	@Override
	public int encode() {
		FlagWalker flagWalker = new FlagWalker(0);
		flagWalker.writeBits(NUM_BITS_PER_DIM, this.xSuffixBits);
		flagWalker.writeBits(NUM_BITS_PER_DIM, this.ySuffixBits);
		return flagWalker.getAllBits();
	}

	@Override
	public int numBits() {
		return NUM_BITS;
	}
	
	public static BaseFlag.IFlagFieldFactory getFactory() {
		return new BaseFlag.IFlagFieldFactory() {

			@Override
			public BaseFlag.IFlagField decode(int bits) {
				return MapLocField.fromBits(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
			
		};
	}
}