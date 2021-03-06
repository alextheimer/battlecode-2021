package player.handlers.robots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import player.RobotPlayer;
import player.handlers.common.HandlerCommon;
import player.handlers.common.PredicateFactories;
import player.util.battlecode.UtilBattlecode;
import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.types.AttackAssignmentFlag;
import player.util.battlecode.flag.types.EnemySightedFlag;
import player.util.battlecode.flag.types.PatrolAssignmentFlag;
import player.util.battlecode.flag.types.TargetMissingFlag;
import player.util.general.UtilGeneral;
import player.util.math.UtilMath;

class Target implements Comparable<Target> {

	public final RobotType robotType;
	public final MapLocation mapLoc;

	/**
	 * Immutable struct-like storage for an Enlightenment Center target.
	 */
	public Target(final RobotType robotType, final MapLocation mapLoc) {
		this.robotType = robotType;
		this.mapLoc = mapLoc;
	}

	@Override
	public int compareTo(final Target other) {
		// Enlightenment Centers are sorted before all other types.
		if (this.robotType == other.robotType) {
			return 0;
		} else if (this.robotType == RobotType.ENLIGHTENMENT_CENTER){
			return -1;
		} else if (other.robotType == RobotType.ENLIGHTENMENT_CENTER) {
			return 1;
		} else {
			return 0;
		}
	}

}

/**
 * Priority queue for Targets, where each pushed Target "claims" its MapLocation.
 * No two Targets can claim the same MapLocation.
 */
class TargetQueue {

	private final Queue<Target> pQueue = new PriorityQueue<>();
	private final Map<MapLocation, Target> mapLocMap = new HashMap<>();  // supports remove()

	/**
	 * Claims the Target's MapLocation, then pushes it onto the queue.
	 *
	 * @param target must specify an unclaimed MapLocation.
	 */
	public void push(final Target target) {
		assert !this.mapLocMap.containsKey(target.mapLoc) : "MapLocation already claimed: " + target.mapLoc;
		this.pQueue.add(target);
		this.mapLocMap.put(target.mapLoc, target);
	}

	/**
	 * Returns the Target at the head of the queue.
	 * Queue must be non-emepty.
	 */
	public Target peek() {
		assert this.pQueue.size() > 0 : "cannot peek on empty queue";
		return this.pQueue.peek();
	}

	/**
	 * Pops and returns the Target from the head of the queue.
	 * Unclaims the Target's MapLocation.
	 */
	public Target pop() {
		assert this.pQueue.size() > 0 : "cannot pop from an empty queue";
		final Target popped = this.pQueue.remove();
		this.mapLocMap.remove(popped.mapLoc);
		return popped;
	}

	/**
	 * Returns true iff the MapLocation is claimed.
	 */
	public boolean mapLocClaimed(final MapLocation mapLoc) {
		return this.mapLocMap.containsKey(mapLoc);
	}

	/**
	 * Unclaims the MapLocation and returns its associated Target.
	 * @param mapLoc must be claimed.
	 */
	public Target remove(final MapLocation mapLoc) {
		assert this.mapLocMap.containsKey(mapLoc) : "unclaimed MapLocation: " + mapLoc;
		final Target removed = this.mapLocMap.get(mapLoc);
		this.mapLocMap.remove(mapLoc);
		this.pQueue.remove(removed);
		return removed;
	}

	/**
	 * Returns the size of the queue.
	 */
	public int size() {
		return this.pQueue.size();
	}

}

/**
 * Handles Enlightenment Center RobotControllers.
 */
public class EnlightenmentCenterHandler implements RobotPlayer.IRobotHandler {

	// number of rounds to TODO
	private static final int FLAG_COOLDOWN_START = 1;

	// >0 indicates a flag needs to remain posted
	private int flagCooldown = 0;
	private final TargetQueue targetQueue = new TargetQueue();
	// stores all currently-known teammate Id's
	private final Set<Integer> friendlyIdSet = new HashSet<>();
	private int numUnitsBuilt = 0;

	/**
	 * Attempts to build a robot with all remaining influence and post its assignment flag.
	 * The operation fails iff:
	 *     (1) a positive flagCooldown exists, or
	 *     (2) the unit is not buildable in any adjacent space.
	 *
	 * @param rc the RobotController for the current round.
	 * @param robotType the RobotType to build.
	 * @param assignmentFlag the assignment flag to post. // TODO(theimer): add a set of assignment types in Flag.java.
	 * @return true iff the operation succeeds.
	 */
    private boolean attemptBuild(final RobotController rc, final RobotType robotType, final Flag.IFlag assignmentFlag) {
    	assert this.flagCooldown == 0 : "flagCooldown != 0 : " + this.flagCooldown;
    	final int influence = rc.getInfluence() - 1;  // -1 so we don't use all the influence and die
    	// arbitrarily assume the build fails
    	boolean buildSuccess = false;
    	// attempt to build in every direction
    	for (final Direction dir : UtilBattlecode.OFF_CENTER_DIRECTIONS) {
    		if (rc.canBuildRobot(robotType, dir, influence)) {
    			try {
    				rc.buildRobot(robotType, dir, influence);
    			} catch (final GameActionException e) {
    				// should never happen; canBuildRobot is a precondition
    				throw new UtilBattlecode.IllegalGameActionException(e);
    			}
    			HandlerCommon.setFlag(rc, assignmentFlag);
    			this.flagCooldown = EnlightenmentCenterHandler.FLAG_COOLDOWN_START;
    			buildSuccess = true;
    			this.numUnitsBuilt++;
    			break;
    		}
    	}
    	UtilBattlecode.log("Build attempted; success: " + buildSuccess);
    	return buildSuccess;
    }

    /**
     * Stores senseable teammate ID's and removes unsenseable ones.
     *
     * @param sensedRobots a Collection of robots sensed during the current round.
     *     Note: filters are applied automatically in this function; the caller
     *     doesn't necessarily need to apply any themselves.
     * @param rc the RobotController for the current round.
     */
    private void updateKnownFriendlyIds(final Collection<RobotInfo> sensedRobots, final RobotController rc) {
    	// add all new ids
    	sensedRobots.stream()
    		.filter(PredicateFactories.robotSameTeam(rc.getTeam()))
    		.map(robotInfo -> robotInfo.getID())
    		// make sure we don't already know about the id
    		.filter(robotId -> !this.friendlyIdSet.contains(robotId))
    		.forEach(robotId -> this.friendlyIdSet.add(robotId));

    	// remove unsenseable (i.e. dead/converted) ids (E-Centers can sense robots at any distance)
    	final Iterator<Integer> idIterator = this.friendlyIdSet.iterator();
    	while (idIterator.hasNext()) {
    		final int id = idIterator.next();
    		if (!rc.canSenseRobot(id)) {
    			idIterator.remove();
    		}
    	}
    }

    /**
     * Updates the target queue to reflect any new info in the EnemySightedFlag.
     *
     * @param rc the RobotController for the current round.
     */
    private void readEnemySightedFlag(final RobotController rc, final EnemySightedFlag flag) {
    	final MapLocation mapLoc = flag.getMapLoc(rc.getLocation());
    	final Target target = new Target(flag.getRobotType(), mapLoc);
    	// is the MapLocation already associated with a target?
    	if (this.targetQueue.mapLocClaimed(mapLoc)) {
    		// assume the latest flag is more up-to-date and remove the old target
    		this.targetQueue.remove(mapLoc);
    	}
    	this.targetQueue.push(target);
    }

    /**
     * Updates the target queue to reflect any new info in the TargetMissingFlag.
     *
     * @param rc the RobotController for the current round.
     */
    private void readTargetMissingFlag(final RobotController rc, final TargetMissingFlag flag) {
    	final MapLocation mapLoc = flag.getMapLoc(rc.getLocation());
    	// remove any targets we thought existed there
    	if (this.targetQueue.mapLocClaimed(mapLoc)) {
    		this.targetQueue.remove(mapLoc);
    	}
    }

    /**
     * Listens for any flags that might affect the Target queue.
     * Update the queue to reflect new information.
     *
     * @param rc the RobotController for the current round.
     */
    private void updateTargetQueue(final RobotController rc) {
    	final Function<Integer, IFlag> flagMapper = new Function<Integer, IFlag>() {
			@Override
			public IFlag apply(final Integer id) {
				assert rc.canSenseRobot(id);
				try {
					return Flag.decode(rc.getFlag(id));
				} catch (final GameActionException e) {
					// shouldn't happen-- canSenseRobot is a precondition.
					throw new UtilBattlecode.IllegalGameActionException(e);
				}
			}
    	};

    	// sense/store flags for all known friendly ID's
    	final List<IFlag> teammateFlagList = new ArrayList<>();
    	{
    		final Stream<IFlag> teammateFlagStream = this.friendlyIdSet.stream().map(flagMapper);
    		UtilGeneral.legalCollect(teammateFlagStream, teammateFlagList);
    	}

    	// handle each IFlag type we're listening for
    	for (final IFlag flag : teammateFlagList) {
    		if (flag instanceof EnemySightedFlag) {
    			// read non-teammate callout flag
    			this.readEnemySightedFlag(rc, (EnemySightedFlag)flag);
    		} else if (flag instanceof TargetMissingFlag) {
    			// read "target isn't where it's expected" flag
    			this.readTargetMissingFlag(rc, (TargetMissingFlag)flag);
    		}
    	}
    }

    /**
     * Plans a unit/assignment-flag pair to build/post.
     * Attempts to build the unit and post the flag.
     *
     * @param rc the RobotController for the current round.
     * @return true iff the unit is successfully built and its flag is posted.
     */
    private boolean attemptNextBuild(final RobotController rc) {
    	boolean buildSuccess;
    	// Oscillate between Slanderers and Politicians
		if ((this.numUnitsBuilt % 2) == 1) {
			// assign the slanderer to patrol in a random direction
			final int patrolDegrees = UtilGeneral.RANDOM.nextInt(UtilMath.CIRCLE_DEGREES);
			buildSuccess = this.attemptBuild(rc, RobotType.SLANDERER, new PatrolAssignmentFlag(patrolDegrees));
		} else if (this.targetQueue.size() > 0) {
			// we have a target to attack
			final Target target = this.targetQueue.peek();
			final AttackAssignmentFlag flag = new AttackAssignmentFlag(target.mapLoc);
			buildSuccess = this.attemptBuild(rc, RobotType.POLITICIAN, flag);
			if (buildSuccess) {
				this.targetQueue.pop();
			}
		} else {
			// no targets to attack; just patrol in a random direction
			final int patrolDegrees = UtilGeneral.RANDOM.nextInt(UtilMath.CIRCLE_DEGREES);
			buildSuccess = this.attemptBuild(rc, RobotType.POLITICIAN, new PatrolAssignmentFlag(patrolDegrees));
		}
		return buildSuccess;
    }

	@Override
	public RobotPlayer.IRobotHandler handle(final RobotController rc) {
		assert rc.getType() == RobotType.ENLIGHTENMENT_CENTER : "illegal controller RobotType: " + rc.getType();

		// store this; use below
		final List<RobotInfo> sensedRobots = Arrays.asList(rc.senseNearbyRobots());

		// update our known ID's
		this.updateKnownFriendlyIds(sensedRobots, rc);

		// check if we need to call for help
		// TODO(theimer): make this happen

		// look for EC's in need of help; build and deploy if deemed appropriate
		// TODO(theimer): make this happen

		// listen for TargetMissingFlags/EnemySightedFlags
		this.updateTargetQueue(rc);

		// did we recently build something?
		if (this.flagCooldown > 0) {
			// still need to keep its assignment flag up
			this.flagCooldown--;
		} else {
			this.attemptNextBuild(rc);
		}

        return this;
	}
}
