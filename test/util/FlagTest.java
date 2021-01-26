package util;

import static org.junit.Assert.*;

import org.junit.Test;

import player.handlers.common.HandlerCommon.*;
import player.util.battlecode.Flag;
import player.util.battlecode.Flag.*;

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
//		AssignmentType squadType = AssignmentType.PATROL;
//		int degrees = 67;  // Just some random value.
//		PatrolAssignmentFlag flag = new PatrolAssignmentFlag(squadType, degrees);
//		// Make sure getters are working as intended.
//		assertEquals(degrees, flag.getOutboundDegrees());
//		assertEquals(squadType, flag.getAssignmentType());
//		// Make sure encode / decode are working as intended.
//		int rawFlag = flag.encode();
//		PatrolAssignmentFlag parsedFlag = PatrolAssignmentFlag.decode(rawFlag);
//		assertEquals("rawFlag: " + rawFlag, OpCode.ASSIGNMENT, Flag.getOpCode(rawFlag));
//		assertEquals(squadType, parsedFlag.getAssignmentType());
//		assertEquals(degrees, parsedFlag.getOutboundDegrees());
	}
	
	@Test
	public void attackTarget() {
//		IntVec2D diffVec = new IntVec2D(3, -4);
//		AttackTargetFlag flag = new AttackTargetFlag(diffVec.x, diffVec.y);
//		// Make sure getters are working as intended.
//		assertEquals(diffVec, flag.getDiffVec());
//		// Make sure encode / decode are working as intended.
//		int rawFlag = flag.encode();
//		AttackTargetFlag parsedFlag = AttackTargetFlag.decode(rawFlag);
//		assertEquals("rawFlag: " + rawFlag, OpCode.ATTACK_TARGET, Flag.getOpCode(rawFlag));
//		assertEquals(diffVec, parsedFlag.getDiffVec());
	}

}
