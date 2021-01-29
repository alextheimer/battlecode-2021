package player.util.battlecode.flag.util;

import player.util.battlecode.flag.Flag;

public class FlagWalker {
	
	// the flag bits we're reading/modifying
	private int flagBits;  
	// the next unread & unwritten bit index; indices increase from right to left.
	private int nextBitIndex;
	
	/**
	 * Sequentially writes / reads (i.e. "walk") a flag.
	 * Flags are built/read from the least-to-most significant bits.
	 * 
	 * @param flagBits the bits of the flag to walk.
	 * 	   Must be non-negative.
	 *     Must be less than 2**MAX_NUM_BITS.
	 */
	public FlagWalker(int flagBits) {
		assert flagBits >= 0 : "flagBits must be non-negative: " + flagBits;
		assert flagBits < (1 << Flag.MAX_NUM_BITS) : "flagBits must be less than 2**MAX_NUM_BITS: " + flagBits;
		
		this.flagBits = flagBits;
		this.nextBitIndex = 0;  
	}
	
	/**
	 * Returns the number of flag bits remaining to be written/read.
	 */
	public int numRemainingBits() {
		return Flag.MAX_NUM_BITS - this.nextBitIndex;
	}
	
	/**
	 * Writes bits to the flag such that the least significant bit of 'bits' is positioned at the
	 * next unread & unwritten bit index.
	 * 
	 * @param numBits the total number of bits to write.
	 *     Must be no greater than numRemainingBits().
	 *     Must be greater than zero.
	 * @param bits the actual bits to be written.
	 * 	   Must be non-negative.
	 *     Must be less than 2**numRemainingBits().
	 */
	public void writeBits(int numBits, int bits) {
		assert (numBits > 0) && (numBits <= this.numRemainingBits()) :
			String.format("numBits must lie on (0, numRemainingBits()]: %d, %d", numBits, this.numRemainingBits());
		assert (bits >= 0) && (bits < (1 << this.numRemainingBits())) : 
			String.format("bits must lie on [0, 1 << numRemainingBits()): %d, %d", bits, 1 << this.numRemainingBits());
		
		int mask = (1 << numBits) - 1;  // create the mask at the least-significant bits
		mask <<= this.nextBitIndex;  // shift the mask to the appropriate index
		this.flagBits &= ~mask;  // zero the appropriate bits of the flag
		this.flagBits |= (bits << this.nextBitIndex);  // place the desired bits into the flag
		this.nextBitIndex += numBits;
	}
	
	/**
	 * Reads bits from the flag such that the least significant bit of the 'numBits' bits to be read
	 * is positioned at the next unread & unwritten bit index.
	 * 
	 * @param numBits the total number of bits to write.
	 *     Must be no greater than numRemainingBits().
	 *     Must be greater than zero.
	 */
	public int readBits(int numBits) {
		assert (numBits > 0) && (numBits <= this.numRemainingBits()) :
			String.format("numBits must lie on (0, numRemainingBits()]: %d, %d", numBits, this.numRemainingBits());
		int mask = (1 << numBits) - 1;  // create the mask at the least-significant bits
		mask <<= this.nextBitIndex;  // shift the mask to the appropriate index
		int unshiftedBits = this.flagBits & mask;  // capture the bits we want to read
		int result = unshiftedBits >> this.nextBitIndex;  // shift the bits to the least significant indices
		this.nextBitIndex += numBits;
		return result;
	}
	
	/**
	 * Returns the flag.
	 * Does not affect the values returned by readBits or writeBits.
	 */
	public int getAllBits() {
		return this.flagBits;
	}
}