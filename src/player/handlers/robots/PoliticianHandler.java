package player.handlers.robots;

import battlecode.common.*;
import player.RobotPlayer;
import player.RobotPlayer.IRobotHandler;
import player.handlers.common.HandlerCommon;
import player.handlers.common.LinearMoverHandler;
import player.util.battlecode.UtilBattlecode;
import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.types.AttackAssignmentFlag;
import player.util.battlecode.flag.types.EnemySightedFlag;
import player.util.battlecode.flag.types.PatrolAssignmentFlag;
import player.util.battlecode.flag.types.TargetMissingFlag;
import player.util.battlecode.flag.util.UtilFlag.FlagOpCode;
import player.util.general.UtilGeneral;
import player.util.math.DoubleVec2D;
import player.util.math.IntVec2D;
import player.util.math.Line2D;
import player.util.math.UtilMath;

import static player.handlers.common.HandlerCommon.*;

import java.util.ArrayDeque;
import java.util.Arrays;
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

interface IAssignmentHandler {
	public IAssignmentHandler handle(RobotController rc) throws GameActionException;
}

public class PoliticianHandler implements RobotPlayer.IRobotHandler {
	
	IAssignmentHandler assignmentHandler = new UnassignedAssignmentHandler();
	
	class UnassignedAssignmentHandler implements IAssignmentHandler {
		
		private IAssignmentHandler makeAssignedHandler(RobotController rc, IFlag flag) {
			IAssignmentHandler handler;
			if (flag instanceof PatrolAssignmentFlag) {
					PatrolAssignmentFlag patrolAssignmentFlag = (PatrolAssignmentFlag)flag;
					DoubleVec2D vec = UtilMath.degreesToVec(patrolAssignmentFlag.getOutboundDegrees());
					DoubleVec2D origin = new DoubleVec2D(rc.getLocation().x, rc.getLocation().y);
					Line2D line = new Line2D(vec, origin);
					handler = new PatrolAssignmentHandler(line, vec);
			} else if (flag instanceof AttackAssignmentFlag) {
					AttackAssignmentFlag attackAssignmentFlag = (AttackAssignmentFlag)flag;
					MapLocation targetMapLoc = attackAssignmentFlag.getMapLoc(rc.getLocation());
					handler = new AttackAssignmentHandler(targetMapLoc);
			} else {
				throw new RuntimeException("illegal flag type: " + flag.getClass());
			}

			return handler;
			
		}
		
		private IAssignmentHandler makeDefaultHandler(RobotController rc) {
			Random rand = new Random();
			int degrees = rand.nextInt(UtilMath.CIRCLE_DEGREES);
			MapLocation mapLoc = rc.getLocation();
			DoubleVec2D vec = UtilMath.degreesToVec(degrees);
			DoubleVec2D currCoord = new DoubleVec2D(mapLoc.x, mapLoc.y);
			Line2D line = new Line2D(vec, currCoord);
			return new PatrolAssignmentHandler(line, vec);
		}
		
		@Override
		public IAssignmentHandler handle(RobotController rc) throws GameActionException {
			IAssignmentHandler assignedHandler;
			Optional<SimpleImmutableEntry<RobotInfo, IFlag>> flagOpt = HandlerCommon.getAnyAdjacentAssignmentFlag(rc, rc.senseNearbyRobots());
			if (flagOpt.isPresent()) {
				assignedHandler = this.makeAssignedHandler(rc, flagOpt.get().getValue());
				UtilBattlecode.log("initialized via flag");
			} else {
				assignedHandler = this.makeDefaultHandler(rc);
				UtilBattlecode.log("initialized via self");
			}
			assignedHandler.handle(rc);
			return assignedHandler;
		}
		
	}

	class PatrolAssignmentHandler implements IAssignmentHandler {

		private static final int MAX_REVERSE_COUNT = 3;
		
		LinearMoverHandler moveHandler;
		int reverseCount;
		
		public PatrolAssignmentHandler(Line2D line, DoubleVec2D vec) {
			this.moveHandler = new LinearMoverHandler(line, vec);
			this.reverseCount = 0;
		}
		
		private boolean patrolStep(RobotController rc) throws GameActionException {
			if (this.moveHandler.atEndOfLine() || this.moveHandler.blocked()) {
				this.moveHandler.reverse();
				this.reverseCount++;
			}
			return this.moveHandler.step(rc);
		}
		
		@Override
		public IAssignmentHandler handle(RobotController rc) throws GameActionException {
			final int mask = (2 * UtilBattlecode.MAX_WORLD_WIDTH) - 1;
			Optional<RobotInfo> calloutOpt = PoliticianHandler.this.senseHighestPriorityNonTeammate(rc);
			if (calloutOpt.isPresent() && rc.canSetFlag(Flag.EMPTY_FLAG)) {
				RobotInfo robotInfo = calloutOpt.get();
				MapLocation mapLoc = robotInfo.getLocation(); 
				EnemySightedFlag flag = new EnemySightedFlag(robotInfo.getType(), mapLoc);
				rc.setFlag(Flag.encode(flag));
			}
			if (!PoliticianHandler.this.attemptEmpowerNearestEnemy(rc)) {
				this.patrolStep(rc);
				if (this.reverseCount >= MAX_REVERSE_COUNT) {
					return new UnassignedAssignmentHandler().handle(rc);
				}
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
					if (robotInfo.getLocation().equals(this.targetMapLoc) && robotInfo.getTeam() != rc.getTeam()) {
						sensedTarget = true;
						break;
					}
				}
				
				if (!sensedTarget) {
					UtilBattlecode.log("no target sensed; unassigned handler");
					TargetMissingFlag flag = new TargetMissingFlag(targetMapLoc);
					rc.setFlag(Flag.encode(flag));
					Clock.yield();
					Clock.yield();
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
		UtilBattlecode.log("empower attempt: " + empowerSuccessful);
		return empowerSuccessful;
	}
		
	private boolean attemptEmpowerNearestEnemy(RobotController rc) throws GameActionException {
		Optional<RobotInfo> enemyInfoOpt = HandlerCommon.getNearestNonTeamRobot(rc, rc.senseNearbyRobots());
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
		
		Iterator<RobotInfo> sensedNonTeammateIterator = HandlerCommon.getRobotsNotOnTeamStream(rc.getTeam(), rc.senseNearbyRobots()).iterator();
		if (sensedNonTeammateIterator.hasNext()) {
			return Optional.of(UtilGeneral.findLeastCostLinear(sensedNonTeammateIterator, costFunc));
		} else {
			return Optional.empty();
		}
		
	}
	
	@FunctionalInterface
	private static interface GameActionFunction<T, R> {
		public R apply(T arg0) throws GameActionException;
	}
	
	private static <T, R> Function<T, R> wrapGameActionFunctionEmergency(GameActionFunction<T, R> gafunc) {
		return new Function<T, R>() {
			@Override
			public R apply(T arg0) {
				try {
					return gafunc.apply(arg0);					
				} catch (GameActionException e) {
					throw new RuntimeException("this should never happen TODO");
				}
			}
			
		};
	}
	
	private boolean attemptMoveCloser(RobotController rc, MapLocation targetLoc) throws GameActionException {
		Iterator<MapLocation> adjacentIterator = UtilBattlecode.makeAdjacentMapLocIterator(rc.getLocation());
		int currDistSquared = targetLoc.distanceSquaredTo(rc.getLocation());
		Function<MapLocation, Double> costFunc = wrapGameActionFunctionEmergency(mapLoc -> -rc.sensePassability(mapLoc)*100000 + mapLoc.distanceSquaredTo(targetLoc));
		Stream<MapLocation> filteredStream = UtilGeneral.streamifyIterator(adjacentIterator)
			.filter(mapLoc -> mapLoc.distanceSquaredTo(targetLoc) < currDistSquared)
			.filter(UtilBattlecode.silenceGameActionPredicate(mapLoc -> rc.onTheMap(mapLoc) && !rc.isLocationOccupied(mapLoc)));
		Iterator<MapLocation> streamIterator = filteredStream.iterator();
		if (streamIterator.hasNext()) {
			MapLocation moveTo = UtilGeneral.findLeastCostLinear(streamIterator, costFunc);
			Direction dir = rc.getLocation().directionTo(moveTo);
			if (rc.canMove(dir)) {
				rc.move(dir);
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public RobotPlayer.IRobotHandler handle(RobotController rc) throws GameActionException {
		this.assignmentHandler = this.assignmentHandler.handle(rc);
		return this;
	}

}
