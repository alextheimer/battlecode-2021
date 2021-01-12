package util;

import static org.junit.Assert.*;
import util.Flag.*;

import org.junit.Test;

import player.handlers.HandlerCommon.*;

public class FlagTest {
	
	/**
	 * ~~~ Test Partitions ~~~
	 * encode() (all flag types)
	 *     -TODO
	 * decode() (all flag types)
	 *     -TODO
	 */
	
	@Test
	public void squadAssign() {
		SquadType squadType = SquadType.PATROL;
		int degrees = 67;  // Just some random value.
		SquadAssignFlag flag = new SquadAssignFlag(squadType, degrees);
		// Make sure getters are working as intended.
		assertEquals(degrees, flag.getOutboundDegrees());
		assertEquals(squadType, flag.getSquadType());
		// Make sure encode / decode are working as intended.
		int rawFlag = flag.encode();
		SquadAssignFlag parsedFlag = SquadAssignFlag.decode(rawFlag);
		assertEquals("rawFlag: " + rawFlag, OpCode.SQUAD_ASSIGN, Flag.getOpCode(rawFlag));
		assertEquals(squadType, parsedFlag.getSquadType());
		assertEquals(degrees, parsedFlag.getOutboundDegrees());
	}

}
