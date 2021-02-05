package util;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.types.AttackAssignmentFlag;
import player.util.battlecode.flag.types.EnemySightedFlag;
import player.util.battlecode.flag.types.PatrolAssignmentFlag;
import player.util.battlecode.flag.types.TargetMissingFlag;

// TODO(theimer): make a separate test file for IFlagFields!!

public class FlagTest {

	/**
	 * ~~~ Test Partitions ~~~
	 * encode / decode
	 *     AttackAssignmentFlag
	 *         mapLoc
	 *     PatrolAssignmentFlag
	 *         degrees
	 *             - 0, 90, 180, 270, other
	 *     EnemySightedFlag
	 *         mapLoc
	 *         robotType
	 *             - EC, POLITICIAN, SLANDERER, MUCKRAKER
	 *     TargetMissingFlag
	 *         mapLoc
	 */

	/*
	 * Notes:
	 *     - MapLocations have dimensions between 10,000 and 30,000
	 *     - World sizes are between 32x32 and 64x64 (inclusive).
	 */

	/**
	 * Covers all AttackAssignmentFlag partitions.
	 */
	@Test
	public void attackAssignmentFlagTest() {
		final MapLocation mapLoc = new MapLocation(15076, 20875);
		final MapLocation refMapLoc = new MapLocation(15078, 20843);
		final AttackAssignmentFlag flag = new AttackAssignmentFlag(mapLoc);
		final int flagBits = Flag.encode(flag);
		final AttackAssignmentFlag decodedFlag = (AttackAssignmentFlag)Flag.decode(flagBits);
		Assert.assertEquals(mapLoc, decodedFlag.getMapLoc(refMapLoc));
	}

	/**
	 * Covers all PatrolAssignmentFlag partitions.
	 */
	@Test
	public void patrolAssignmentFlagTest() {
		final List<Integer> degreesList = Arrays.asList(0, 90, 180, 270, 359, 45);
		for (final int degrees : degreesList) {
			final PatrolAssignmentFlag flag = new PatrolAssignmentFlag(degrees);
			final int flagBits = Flag.encode(flag);
			final PatrolAssignmentFlag decodedFlag = (PatrolAssignmentFlag)Flag.decode(flagBits);
			Assert.assertEquals(degrees, decodedFlag.getOutboundDegrees());
		}
	}

	/**
	 * Covers all EnemySightedFlag partitions.
	 */
	@Test
	public void enemySightedFlagTest() {
		final MapLocation mapLoc = new MapLocation(15076, 20875);
		final MapLocation refMapLoc = new MapLocation(15078, 20843);
		for (final RobotType robotType : RobotType.values()) {
			final EnemySightedFlag flag = new EnemySightedFlag(robotType, mapLoc);
			final int flagBits = Flag.encode(flag);
			final EnemySightedFlag decodedFlag = (EnemySightedFlag)Flag.decode(flagBits);
			Assert.assertEquals(robotType,  decodedFlag.getRobotType());
			Assert.assertEquals(mapLoc, decodedFlag.getMapLoc(refMapLoc));
		}
	}

	/**
	 * Covers all TargetMissingFlag partitions.
	 */
	@Test
	public void targetMissingFlagTest() {
		final MapLocation mapLoc = new MapLocation(15076, 20875);
		final MapLocation refMapLoc = new MapLocation(15078, 20843);
		final TargetMissingFlag flag = new TargetMissingFlag(mapLoc);
		final int flagBits = Flag.encode(flag);
		final TargetMissingFlag decodedFlag = (TargetMissingFlag)Flag.decode(flagBits);
		Assert.assertEquals(mapLoc, decodedFlag.getMapLoc(refMapLoc));
	}
}
