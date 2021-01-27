package player.util.battlecode.flag.types;

import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlag;
import player.util.battlecode.flag.util.UtilFlag.OpCode;

public class FollowerClaimFlag implements UtilFlag.IFlag {
	public static FollowerClaimFlag decode(int rawFlag) {return new FollowerClaimFlag();}
	public int encode() {return UtilFlag.OpCode.FOLLOWER_CLAIM.ordinal();}
	@Override
	public UtilFlag.OpCode getOpCode() {
		return UtilFlag.OpCode.FOLLOWER_CLAIM;
	}
}