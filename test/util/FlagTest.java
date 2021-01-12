package util;

import static org.junit.Assert.*;
import util.Flag.*;

import org.junit.Test;

import player.handlers.HandlerCommon.*;

public class FlagTest {
	
	/**
	 * ~~~ Test Partitions ~~~
	 * aStar:
	 *     encode() (all flag types)
	 *         -TODO
	 *     decode() (all flag types)
	 *         -TODO
	 */
	
	@Test
	public void squadAssign() {
		SquadType squadType = SquadType.PATROL;
		int degrees = 67;
		SquadAssignFlag flag = new SquadAssignFlag(squadType, degrees);
		assertEquals(degrees, flag.getOutboundDegrees());
		assertEquals(squadType, flag.getSquadType());
		int rawFlag = flag.encode();
		SquadAssignFlag parsedFlag = SquadAssignFlag.decode(rawFlag);
		assertEquals("rawFlag: " + rawFlag, OpCode.SQUAD_ASSIGN, Flag.getOpCode(rawFlag));
		assertEquals(degrees, parsedFlag.getOutboundDegrees());
		assertEquals(squadType, parsedFlag.getSquadType());
	}

}
