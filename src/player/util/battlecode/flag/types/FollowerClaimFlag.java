package player.util.battlecode.flag.types;

import java.util.Arrays;
import java.util.List;

import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagField;
import player.util.battlecode.flag.util.UtilFlag.OpCode;

public class FollowerClaimFlag extends BaseFlag {
	public static OpCode opCode = OpCode.FOLLOWER_CLAIM;
	public static FollowerClaimFlag decode(int rawFlag) {return new FollowerClaimFlag();}
	@Override
	public UtilFlag.OpCode getOpCode() {
		return opCode;
	}
	@Override
	protected List<IFlagField> getOrderedFlagFieldList() {
		return Arrays.asList();
	}
}