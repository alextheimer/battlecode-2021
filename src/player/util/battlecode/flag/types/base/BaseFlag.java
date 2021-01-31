package player.util.battlecode.flag.types.base;

import java.util.ArrayList;
import java.util.List;

import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagField;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagFieldFactory;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag.FlagOpCode;

/**
 * Contains common methods / interfaces used by flags that can be encoded/decoded
 * as a sequence of encode-/decode-able fields.
 */
public abstract class BaseFlag implements Flag.IFlag {
	
	/* TODO(theimer): there are a number of common methods (decode, numBits, etc)
	 * that would DRY things out if they were implemented here (at the expense of should-be-static
	 * variables becoming non-static.*/
	
	/**
	 * Field of a BaseFlag-derived class.
	 */
	public interface IFlagField {
		/**
		 * Returns the IFlagField as an encoded sequence of bits.
		 */
		public int encode();
		/**
		 * Returns the number of bits used to encode the IFlagField.
		 */
		public int numBits();
	}
	
	/**
	 * Decodes the bits of an encoded flag field into a FlagField instance.
	 */
	public interface IFlagFieldFactory {
		/**
		 * Returns an IFlagField instance as encoded within the argument bits.
		 * @param bits must be non-negative and representable by no more than than numBits() bits.
		 */
		public BaseFlag.IFlagField decode(int bits);
		/**
		 * Returns the number of bits used to encode the IFlagField.
		 */
		public int numBits();
	}

	/**
	 * Returns a List of IFlagFields sufficient to reconstruct the BaseFlag instance.
	 */
	protected abstract List<BaseFlag.IFlagField> getOrderedFlagFieldList();
	
	/**
	 * Returns the BaseFlag as an encoded sequence of bits.
	 */
	public int encode() {
		FlagWalker flagWalker = new FlagWalker(0);
		// sequentially write the encoded bits of each field into the FlagWalker
		for (BaseFlag.IFlagField flagField : this.getOrderedFlagFieldList()) {
			flagWalker.writeBits(flagField.numBits(), flagField.encode());
		}
		return flagWalker.getAllBits();
	}
	
	/**
	 * Converts a BaseFlag's encoded bits into a sequence of FlagFields.
	 * 
	 * @param rawFlag a BaseFlag's encoded bits.
	 * @param factoryList TODO
	 */
	protected static List<BaseFlag.IFlagField> decodeFields(int rawFlag, List<BaseFlag.IFlagFieldFactory> factoryList) {
		List<BaseFlag.IFlagField> fieldList = new ArrayList<>();
		FlagWalker flagWalker = new FlagWalker(rawFlag);
		// Decode 'chunks' of bits sequentially from the FlagWalker.
		// Each i'th chunk is decoded by the i'th factory in factoryList.
		for (BaseFlag.IFlagFieldFactory factory : factoryList) {
			int bits = flagWalker.readBits(factory.numBits());
			fieldList.add(factory.decode(bits));
		}
		return fieldList;
	}
}
