package player.util.battlecode.flag.types.base;

import java.util.ArrayList;
import java.util.List;

import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagField;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagFieldFactory;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag.FlagOpCode;

public abstract class BaseFlag implements Flag.IFlag {
	
	public interface IFlagFieldFactory {
		public BaseFlag.IFlagField decode(int bits);
		public int numBits();
	}

	public interface IFlagField {
		public int encode();
		public int numBits();
	}

	protected abstract List<BaseFlag.IFlagField> getOrderedFlagFieldList();
	
	public int encode() {
		FlagWalker flagWalker = new FlagWalker(0);
		for (BaseFlag.IFlagField flagField : this.getOrderedFlagFieldList()) {
			flagWalker.writeBits(flagField.numBits(), flagField.encode());
		}
		return flagWalker.getAllBits();
	}
	
	protected static List<BaseFlag.IFlagField> decodeFields(int rawFlag, List<BaseFlag.IFlagFieldFactory> factoryList) {
		List<BaseFlag.IFlagField> fieldList = new ArrayList<>();
		FlagWalker flagWalker = new FlagWalker(rawFlag);
		for (BaseFlag.IFlagFieldFactory factory : factoryList) {
			int bits = flagWalker.readBits(factory.numBits());
			fieldList.add(factory.decode(bits));
		}
		return fieldList;
	}
}
