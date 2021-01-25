package player.handlers;

import battlecode.common.*;
import player.util.Flag;
import player.util.Util;
import player.util.UtilMath;
import player.util.Flag.AttackAssignmentFlag;
import player.util.Flag.EnemySightedFlag;
import player.util.Flag.FollowerClaimFlag;
import player.util.Flag.OpCode;
import player.util.Flag.PatrolAssignmentFlag;
import player.util.UtilMath.DoubleVec2D;
import player.util.UtilMath.IntVec2D;
import player.util.UtilMath.Line2D;

import static player.handlers.HandlerCommon.*;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import player.handlers.LinearMoverHandler;

interface IAssignmentHandler {
	public IAssignmentHandler handle(RobotController rc) throws GameActionException;
}

public class PoliticianHandler implements IRobotHandler {
	
	IAssignmentHandler assignmentHandler = new UnassignedAssignmentHandler();
	
	class UnassignedAssignmentHandler implements IAssignmentHandler {
		
		private Optional<Integer> findAssignmentFlag(RobotController rc) throws GameActionException {
			Optional<SimpleImmutableEntry<RobotInfo, Integer>> entryOpt = HandlerCommon.findFirstMatchingTeamFlag(
					rc,
					rc.senseNearbyRobots(HandlerCommon.MAX_DIST_SQUARED_ADJACENT, rc.getTeam()),
					(robotInfo, rawFlag) -> ((Flag.getOpCode(rawFlag) == OpCode.ASSIGN_PATROL) || (Flag.getOpCode(rawFlag) == OpCode.ASSIGN_ATTACK))
			);
			if (entryOpt.isPresent()) {
				return Optional.of(entryOpt.get().getValue());
			} else {
				return Optional.empty();
			}
		}
		
		private IAssignmentHandler makeAssignedHandler(RobotController rc, int rawFlag) {
			IAssignmentHandler handler;
			OpCode opCode = Flag.getOpCode(rawFlag);
			switch(opCode) {
			case ASSIGN_PATROL:
				{
					PatrolAssignmentFlag flag = PatrolAssignmentFlag.decode(rawFlag);
					DoubleVec2D vec = UtilMath.degreesToVec(flag.getOutboundDegrees());
					DoubleVec2D origin = new DoubleVec2D(rc.getLocation().x, rc.getLocation().y);
					Line2D line = UtilMath.Line2D.make(vec, origin);
					handler = new PatrolAssignmentHandler(line, vec);
				}
				break;
			case ASSIGN_ATTACK:
				{
					AttackAssignmentFlag flag = AttackAssignmentFlag.decode(rawFlag);
					MapLocation targetMapLoc = HandlerCommon.offsetToMapLocation(flag.getCoord(), rc.getLocation());
					handler = new AttackAssignmentHandler(targetMapLoc);
				}
				break;
			default:
				throw new RuntimeException("illegal OpCode: " + opCode);
			}
			return handler;
			
		}
		
		private IAssignmentHandler makeDefaultHandler(RobotController rc) {
			Random rand = new Random();
			int degrees = rand.nextInt(UtilMath.MAX_DEGREES);
			MapLocation mapLoc = rc.getLocation();
			DoubleVec2D vec = UtilMath.degreesToVec(degrees);
			DoubleVec2D currCoord = new DoubleVec2D(mapLoc.x, mapLoc.y);
			Line2D line = Line2D.make(vec, currCoord);
			return new PatrolAssignmentHandler(line, vec);
		}
		
		@Override
		public IAssignmentHandler handle(RobotController rc) throws GameActionException {
			IAssignmentHandler assignedHandler;
			Optional<Integer> flagOpt = this.findAssignmentFlag(rc);
			if (flagOpt.isPresent()) {
				assignedHandler = this.makeAssignedHandler(rc, flagOpt.get());
				System.out.println("initialized via flag");
			} else {
				assignedHandler = this.makeDefaultHandler(rc);
				System.out.println("initialized via self");
			}
			assignedHandler.handle(rc);
			return assignedHandler;
		}
		
	}

	class PatrolAssignmentHandler implements IAssignmentHandler {

		LinearMoverHandler moveHandler;
		
		public PatrolAssignmentHandler(Line2D line, DoubleVec2D vec) {
			this.moveHandler = new LinearMoverHandler(line, vec);
		}
		
		private boolean patrolStep(RobotController rc) throws GameActionException {
			if (this.moveHandler.atEndOfLine() || this.moveHandler.blocked()) {
				this.moveHandler.reverse();
			}
			return this.moveHandler.step(rc);
		}
		
		@Override
		public IAssignmentHandler handle(RobotController rc) throws GameActionException {
			final int mask = (2 * HandlerCommon.MAX_WORLD_WIDTH) - 1;
			Optional<RobotInfo> calloutOpt = PoliticianHandler.this.senseHighestPriorityNonTeammate(rc);
			if (calloutOpt.isPresent() && rc.canSetFlag(Flag.EMPTY_FLAG)) {
				RobotInfo robotInfo = calloutOpt.get();
				MapLocation mapLoc = robotInfo.getLocation(); 
				EnemySightedFlag flag = new EnemySightedFlag(robotInfo.getType(), mapLoc.x & mask, mapLoc.y & mask);
				rc.setFlag(flag.encode());
			}
			if (!PoliticianHandler.this.attemptEmpowerNearestEnemy(rc)) {
				this.patrolStep(rc);
			}
			return this;
		}
		
	}

	class AttackAssignmentHandler implements IAssignmentHandler {

		MapLocation targetMapLoc;
		
		public AttackAssignmentHandler(MapLocation targetMapLoc) {
			this.targetMapLoc = targetMapLoc;
		}
		
		@Override
		public IAssignmentHandler handle(RobotController rc) throws GameActionException{
			if (rc.canSenseLocation(this.targetMapLoc)) {
				RobotInfo[] sensedRobots = rc.senseNearbyRobots();
				boolean sensedTarget = false;
				for (RobotInfo robotInfo : sensedRobots) {
					if (robotInfo.getLocation().equals(this.targetMapLoc)) {
						sensedTarget = true;
						break;
					}
				}
				
				if (!sensedTarget) {
					// post flag TODO!!!!
					System.out.println("no target sensed; unassigned handler");
					IAssignmentHandler nextHandler = new UnassignedAssignmentHandler();
					nextHandler.handle(rc);
					return nextHandler;
				}
			}
			
			if (!PoliticianHandler.this.attemptEmpowerNearestEnemy(rc)) {
				PoliticianHandler.this.attemptMoveCloser(rc, this.targetMapLoc);				
			}
			return this;
		}
		
	}
	
	private boolean attemptEmpower(RobotController rc, int radiusSquared) throws GameActionException {
		assert radiusSquared <= RobotType.POLITICIAN.actionRadiusSquared : "radius squared exceeds action radius";
		boolean empowerSuccessful;
		if (rc.canEmpower(radiusSquared)) {
			rc.empower(radiusSquared);
			empowerSuccessful = true;
		} else {
			empowerSuccessful = false;
		}
		System.out.println("empower attempt: " + empowerSuccessful);
		return empowerSuccessful;
	}
		
	private boolean attemptEmpowerNearestEnemy(RobotController rc) throws GameActionException {
		Optional<RobotInfo> enemyInfoOpt = HandlerCommon.senseNearestNonTeam(rc, rc.senseNearbyRobots());
		if (enemyInfoOpt.isPresent()) {
			RobotInfo enemyInfo = enemyInfoOpt.get();
			int enemyDistSquared = rc.getLocation().distanceSquaredTo(enemyInfo.getLocation());
			if (enemyDistSquared <= rc.getType().actionRadiusSquared) {
				return attemptEmpower(rc, enemyDistSquared);				
			}
		}
		return false;
	}
	
	private Optional<RobotInfo> senseHighestPriorityNonTeammate(RobotController rc) {
		Function<RobotInfo, Double> costFunc = new Function<RobotInfo, Double>(){

			@Override
			public Double apply(RobotInfo robotInfo) {
				switch(robotInfo.getType()) {
					case POLITICIAN: return -1.0;
					case MUCKRAKER: return -2.0;
					case SLANDERER: return -3.0;
					case ENLIGHTENMENT_CENTER: return -4.0;
					default: throw new RuntimeException("unrecognized RobotInfo");
				}
			}
		};
		
		Collection<RobotInfo> sensedNonTeammates = HandlerCommon.senseAllNonTeam(rc);
		if (sensedNonTeammates.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(Util.findLeastCostLinear(sensedNonTeammates.iterator(), costFunc));
	}

	private boolean attemptMoveCloser(RobotController rc, MapLocation targetLoc) throws GameActionException {
		Iterator<MapLocation> adjacentIterator = HandlerCommon.getAdjacentIterator(rc.getLocation());
		int currDistSquared = targetLoc.distanceSquaredTo(rc.getLocation());
		Function<MapLocation, Double> costFunc = mapLoc -> (double)mapLoc.distanceSquaredTo(targetLoc);
		Stream<MapLocation> filteredStream = Util.streamifyIterator(adjacentIterator)
			.filter(mapLoc -> mapLoc.distanceSquaredTo(targetLoc) < currDistSquared)
			.filter(HandlerCommon.wrapGameActionPredicate(mapLoc -> !rc.isLocationOccupied(mapLoc) && rc.onTheMap(mapLoc)));
		if (filteredStream.findAny().isPresent()) {
			MapLocation moveTo = Util.findLeastCostLinear(filteredStream.iterator(), costFunc);
			Direction dir = rc.getLocation().directionTo(moveTo);
			if (rc.canMove(dir)) {
				rc.move(dir);
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public IRobotHandler handle(RobotController rc) throws GameActionException {
		this.assignmentHandler = this.assignmentHandler.handle(rc);
		return this;
	}

}
