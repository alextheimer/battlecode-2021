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
	
	/**
	 * =======================================
	 * Justification for NUM_BITS_PER_DIM
	 * =======================================
	 * 
	 * All values of each MapLocation dimension lie on
	 * 
	 * [ origin, origin + {0 < dim_size < MAX_WORLD_WIDTH} )
	 * 
	 * Hence, for each dimension, we need to use at most log2(MAX_WORLD_WIDTH) bits to create a perfect
	 * matching between bit permutations and values of the dimension. Namely, the final (at most)
	 * log2(MAX_WORLD_WIDTH) bits of each dimension would be sufficient to reproduce a MapLocation
	 * as long as we know the origin.
	 * 
	 * We don't have the origin, however. Suppose, regardless, we stored only these bits (per dimension)
	 * to encode MapLocations. Maybe we could use a reference MapLocation to reproduce the encoded MapLocation?
	 * 
	 * In the worst case, dim_dize = MAX_WORLD_WIDTH, so we store log2(MAX_WORLD_WIDTH) bits. Suppose an
	 * encoded dimension of the MapLocation equals log2(MAX_WORLD_WIDTH)/2. Now, a reference
	 * MapLocation is useless. The final bits of each dimension are at most log2(MAX_WORLD_WIDTH)/2
	 * in either direction, so we're not sure whether-or-not to add or subtract from the
	 * reference dimensions.
	 * 
	 * If we store one additional bit, we now know the correct direction; one direction
	 * gives an absolute difference greater than MAX_WORLD_WIDTH.
	 * 
	 * TODO(theimer): explain the above better.
	 */
	
	static {
		// TODO(theimer): confirm this
		assert UtilMath.isPow2(UtilBattlecode.MAX_WORLD_WIDTH) : 
			"+1 in NUM_BITS_PER_DIM applies only when MAX_WORLD_WIDTH is a power of two";
	}
	
	// we store 1 additional bit beyond the number of bits required to represent MAX_WORLD_WIDTH values.
	private static final int NUM_BITS_PER_DIM = UtilMath.log2Ceil(UtilBattlecode.MAX_WORLD_WIDTH) + 1;
	public static final int NUM_BITS = NUM_BITS_PER_DIM * 2;
	
	// these are the final NUM_BITS_PER_DIM bits of each mapLocaiton dimension
	private int xSuffixBits;
	private int ySuffixBits;
	
	/**
	 * Stores a MapLocation.
	 * 
	 * @param mapLoc must be valid (i.e. lie on the map). 
	 */
	public MapLocField(MapLocation mapLoc) {
		// MapLocations shouldn't allow this...
		assert mapLoc.x >= 0 : "this should never fail";
		assert mapLoc.y >= 0 : "this should never fail";
		
		// store the final bits of each dimension
		this.xSuffixBits = getSuffixBits(mapLoc.x);
		this.ySuffixBits = getSuffixBits(mapLoc.y);
	}
	
	/**
	 * See {@link MapLocField#MapLocField(MapLocation)} TODO(theimer): make sure this worked
	 * 
	 * @param xSuffixBits must lie on [0, 2**NUM_BITS_PER_DIM)
	 * @param ySuffixBits must lie on [0, 2**NUM_BITS_PER_DIM)
	 */
	private MapLocField(int xSuffixBits, int ySuffixBits) {
		assert (xSuffixBits >= 0) && (xSuffixBits < (1 << NUM_BITS_PER_DIM)) : "xSuffixBits: " + xSuffixBits;
		assert (ySuffixBits >= 0) && (ySuffixBits < (1 << NUM_BITS_PER_DIM)) : "ySuffixBits: " + ySuffixBits;
		this.xSuffixBits = xSuffixBits;
		this.ySuffixBits = ySuffixBits;
	}
	
	/**
	 * Returns the final NUM_BITS_PER_DIM bits of a value.
	 */
	private static int getSuffixBits(int val) {
		final int mask = (1 << NUM_BITS_PER_DIM) - 1;  // TODO(theimer): just store this?
		return val & mask;
	}
	
	/**
	 * Decodes the bits of an encoded MapLocField into a MapLocField.
	 */
	public static MapLocField decode(int bits) {
		FlagWalker flagWalker = new FlagWalker(bits);
		int xSuffixBits = flagWalker.readBits(NUM_BITS_PER_DIM);
		int ySuffixBits = flagWalker.readBits(NUM_BITS_PER_DIM);
		return new MapLocField(xSuffixBits, ySuffixBits);
	}
	
	/**
	 * Returns the stored MapLocation.
	 * 
	 * @param referenceMapLoc must be a valid mapLocation (on the map).
	 */
	public MapLocation getMapLocation(MapLocation referenceMapLoc) {
		// MapLocations shouldn't allow this...
		assert referenceMapLoc.x >= 0 : "this should never fail";
		assert referenceMapLoc.y >= 0 : "this should never fail";
		
		final int maxSuffixValue = 1 << NUM_BITS_PER_DIM;  // TODO(theimer): store this elsewhere
		int xReferenceSuffixBits = getSuffixBits(referenceMapLoc.x);
		int yReferenceSuffixBits = getSuffixBits(referenceMapLoc.y);
		
		// get the diff of smallest absolute value such that: reference_dim + diff = mapLoc_dim
		// Note: one possible diff will have absolute value > MAX_WORLD_WIDTH (and is therefore impossible).
		// See diffMod javadoc for more details.
		int xDiff = UtilMath.diffMod(this.xSuffixBits, xReferenceSuffixBits, maxSuffixValue);
		int yDiff = UtilMath.diffMod(this.ySuffixBits, yReferenceSuffixBits, maxSuffixValue);
		
		// make sure the diffs we're found could possibly be correct
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
				return MapLocField.decode(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
		};
	}
}