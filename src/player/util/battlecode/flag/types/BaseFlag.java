package player.util.battlecode.flag.types;

import java.util.ArrayList;
import java.util.List;

import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlag;
import player.util.battlecode.flag.util.UtilFlag.FlagOpCode;
import player.util.battlecode.flag.util.UtilFlag.IFlagField;
import player.util.battlecode.flag.util.UtilFlag.IFlagFieldFactory;

abstract class BaseFlag implements IFlag {
	
	protected abstract List<IFlagField> getOrderedFlagFieldList();
	
	public int encode() {
		FlagWalker flagWalker = new FlagWalker(0);
		for (IFlagField flagField : this.getOrderedFlagFieldList()) {
			flagWalker.writeBits(flagField.numBits(), flagField.encode());
		}
		return flagWalker.getAllBits();
	}
	
	static List<IFlagField> decodeFields(int rawFlag, List<IFlagFieldFactory> factoryList) {
		List<IFlagField> fieldList = new ArrayList<>();
		FlagWalker flagWalker = new FlagWalker(rawFlag);
		for (IFlagFieldFactory factory : factoryList) {
			int bits = flagWalker.readBits(factory.numBits());
			fieldList.add(factory.decode(bits));
		}
		return fieldList;
	}
}
