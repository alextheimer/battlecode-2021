package player.handlers.robots;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import player.RobotPlayer;
import player.handlers.common.HandlerCommon;
import player.handlers.common.LinearMoverHandler;
import player.handlers.common.PredicateFactories;
import player.util.battlecode.UtilBattlecode;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.types.AttackAssignmentFlag;
import player.util.battlecode.flag.types.PatrolAssignmentFlag;
import player.util.battlecode.flag.types.TargetMissingFlag;
import player.util.general.UtilGeneral;
import player.util.math.DoubleVec2D;
import player.util.math.Line2D;
import player.util.math.UtilMath;

/**
 * State / method requirements are different for each of the Politician's different assignments,
 * so each unique set of state/methods are handled by one of these.
 */
interface IAssignmentHandler {
	/**
	 * Handles one of the Politician's assignments.
	 *
	 * @param rc the RobotController for the current round.
	 */
	public IAssignmentHandler handle(RobotController rc);
}

public class PoliticianHandler implements RobotPlayer.IRobotHandler {

	// assignment handler for the Politician's current Enlightenment-Center-given assignment
	IAssignmentHandler assignmentHandler;

	public PoliticianHandler(final RobotController rc) {
		final List<RobotInfo> sensedRobots = Arrays.asList(rc.senseNearbyRobots());
		// get the assigning robot/flag pair (if one is senseable)
		final Optional<SimpleImmutableEntry<RobotInfo, IFlag>> flagEntryOpt = HandlerCommon.getAnyAdjacentAssignmentFlag(rc, sensedRobots);
		if (flagEntryOpt.isPresent()) {
			this.assignmentHandler = this.makeHandlerFromAssignmentFlag(rc, flagEntryOpt.get().getValue());
			UtilBattlecode.log("initialized via flag");
		} else {
			this.assignmentHandler = this.makeDefaultHandler(rc);
			UtilBattlecode.log("initialized via self");
		}
	}

	/**
	 * Returns the IAssignmentHandler to use when no assignment flag is detected.
	 * Used when Slanderers change into Politicians after 300 rounds.
	 *
	 * @param rc the RobotController for the current round.
	 */
	private IAssignmentHandler makeDefaultHandler(final RobotController rc) {
		// just pick a random direction to patrol
		return new PatrolAssignmentHandler(LinearMoverHandler.randomThruMapLocation(rc.getLocation()));
	}

	/**
	 * Returns an IAssignmentHandler according to the assignment flag.
	 *
	 * @param rc the RobotController for the current round.
	 * @param flag must be an assignment flag.
	 */
	private IAssignmentHandler makeHandlerFromAssignmentFlag(final RobotController rc, final IFlag flag) {
		IAssignmentHandler handler;
		if (flag instanceof PatrolAssignmentFlag) {
				final PatrolAssignmentFlag patrolAssignmentFlag = (PatrolAssignmentFlag)flag;
				// build the patrol line and initial direction vector
				final DoubleVec2D outboundVec = UtilMath.degreesToVec(patrolAssignmentFlag.getOutboundDegrees());
				final DoubleVec2D pointOnLine = UtilBattlecode.mapLocToVec(rc.getLocation());
				final Line2D patrolLine = new Line2D(outboundVec, pointOnLine);
				handler = new PatrolAssignmentHandler(patrolLine, outboundVec);
		} else if (flag instanceof AttackAssignmentFlag) {
				final AttackAssignmentFlag attackAssignmentFlag = (AttackAssignmentFlag)flag;
				final MapLocation targetMapLoc = attackAssignmentFlag.getMapLoc(rc.getLocation());
				handler = new AttackAssignmentHandler(targetMapLoc);
		} else {
			// TODO(theimer): change all RuntimeExceptions to something more appropriate.
			throw new RuntimeException("unrecognized flag type: " + flag.getClass());
		}
		return handler;
	}

	private class PatrolAssignmentHandler implements IAssignmentHandler {

		// the maximum number of reverse() calls the Politician can make on
		// moverHandler before a new moverHandler is instantiated.
		private static final int MAX_REVERSE_COUNT = 3;

		// handles the Politician's movement (back-and-forth along a line)
		LinearMoverHandler moverHandler;
		// the number of reverse() calls made on moverHandler
		int reverseCount;

		/**
		 * Moves the robot back-and-forth along a line.
		 * The robot attemtps to empower a non-teammate as soon as one is in-range.
		 *
		 * @param patrolLine the line to patrol.
		 * @param outboundVec the initial direction to travel along the line.
		 */
		public PatrolAssignmentHandler(final Line2D patrolLine, final DoubleVec2D outboundVec) {
			this.moverHandler = new LinearMoverHandler(patrolLine, outboundVec);
			this.setMoverHandler(this.moverHandler);
			this.assertValidRep();
		}

		/**
		 * See {@link PatrolAssignmentHandler#PatrolAssignmentHandler(Line2D, DoubleVec2D)}.
		 */
		public PatrolAssignmentHandler(final LinearMoverHandler moverHandler) {
			this.setMoverHandler(moverHandler);
			this.assertValidRep();
		}

		/**
		 * Asserts that the PatrolAssignmentHandler's rep is valid.
		 */
		private void assertValidRep() {
			// note: == is fine; patrolStep handles this case.
			assert this.reverseCount <= PatrolAssignmentHandler.MAX_REVERSE_COUNT : "reverseCount: " + this.reverseCount;
		}

		/**
		 * Set the LinearMoverHandler and reset the reverse() count to zero.
		 */
		private void setMoverHandler(final LinearMoverHandler moverHandler) {
			this.moverHandler = moverHandler;
			this.reverseCount = 0;
			this.assertValidRep();
		}

		/**
		 * Reverse the MoverHandler.
		 */
		private void reverse() {
			this.moverHandler.reverse();
			this.reverseCount++;
			this.assertValidRep();
		}

		/**
		 * Attempts a step if the moverHandler doesn't indicate the robot:
		 *     (1) has reached the end of the line (edge of the world), or
		 *     (2) cannot move into any candidate MapLocation (all are occupied).
		 *
		 * If either of the two above conditions are met
		 *
		 * @param rc the RobotController for the current round.
		 * @return true iff a step is attempted (not necessarily completed); else false.
		 */
		private boolean patrolStep(final RobotController rc) {
			if (this.moverHandler.endOfLine() || this.moverHandler.blocked()) {
				if (this.reverseCount < PatrolAssignmentHandler.MAX_REVERSE_COUNT) {
					this.reverse();
				} else {
					// randomize the line/direction!
					this.setMoverHandler(LinearMoverHandler.randomThruMapLocation(rc.getLocation()));
				}
			}
			this.assertValidRep();
			return this.moverHandler.attemptStep(rc);
		}

		@Override
		public IAssignmentHandler handle(final RobotController rc) {
			final List<RobotInfo> sensedRobots = Arrays.asList(rc.senseNearbyRobots());

			// call out an enemy to listening enlightenment centers
			HandlerCommon.setHighestPriorityEnemySightedFlag(rc, sensedRobots);

			// Attempt to empower any enemies in-range.
			if (!PoliticianHandler.this.attemptEmpowerNearestNonTeammate(rc, sensedRobots)) {
				// Empowerment failed! (none are in-range, or cooldown exists).
				// Just patrol...
				this.patrolStep(rc);
			}
			this.assertValidRep();
			return this;
		}
	}

	class AttackAssignmentHandler implements IAssignmentHandler {

		// Number of rounds the Politician must wait before posting any new flags
		// such that all other teammates have time to see a posted TargetMissingFlag.
		private static final int TARGET_MISSING_COOLDOWN_START = 3;

		// MapLocation of the assigned target.
		MapLocation targetMapLoc;
		// If > 0, the TargetMissingFlag has been posted.
		int targetMissingCooldown;

		/**
		 * Guides the Politician to its target and empowers as soon as possible.
		 *
		 * @param targetMapLoc MapLocation of the assigned target.
		 */
		public AttackAssignmentHandler(final MapLocation targetMapLoc) {
			this.targetMapLoc = targetMapLoc;
			this.targetMissingCooldown = 0;
		}

		@Override
		public IAssignmentHandler handle(final RobotController rc) {
			final List<RobotInfo> sensedRobots = Arrays.asList(rc.senseNearbyRobots());

			// Arbitrarily assume the handler will not change.
			IAssignmentHandler nextHandler = this;
			// Empower any other nearby bots.
			if (!PoliticianHandler.this.attemptEmpowerNearestNonTeammate(rc, sensedRobots)) {
				// No nearby bots to empower!
				if (this.targetMissingCooldown > 0) {
					// We've already established that the target is missing from its location
					// and posted the TargetMissingFlag. Continue the cooldown process so our origin
					// enlightenment center has time to see the flag.
					this.targetMissingCooldown--;
					if (this.targetMissingCooldown == 0) {
						// Cooldown done! Start patrolling in a random direction.
						nextHandler = new PatrolAssignmentHandler(LinearMoverHandler.randomThruMapLocation(rc.getLocation()));
						nextHandler.handle(rc);
					}
				} else if (rc.canSenseLocation(this.targetMapLoc)) {
					// Sensors are in-range of the target location!
					// Try to sense it...
					final Optional<RobotInfo> targetRobotOpt = sensedRobots.stream()
							// at the target location?
							.filter(PredicateFactories.robotAtMapLocation(this.targetMapLoc))
							// not a teammate? (note that empowered robots can change teams)
							.filter(PredicateFactories.robotNonTeam(rc.getTeam()))
							.findAny();
					if (targetRobotOpt.isPresent()) {
						// Target found! Move into empower range.
						PoliticianHandler.this.attemptMoveCloser(rc, this.targetMapLoc);
					} else {
						// target not found :(
						// post the TargetMissingFlag to inform our origin Enlightenment Center
						final TargetMissingFlag flag = new TargetMissingFlag(this.targetMapLoc);
						HandlerCommon.setFlag(rc, flag);
						this.targetMissingCooldown = AttackAssignmentHandler.TARGET_MISSING_COOLDOWN_START;
					}
				}
			}
			return nextHandler;
		}
	}

	/**
	 * Attempts to use the Politician's Empower ability.
	 *
	 * @param rc the RobotController for the current round.
	 * @param radiusSquared the maximum squared radius to empower.
	 *     Must lie on (0, RobotType.POLITICIAN.actionRadiusSquared].
	 * @return true iff the empower is successful.
	 */
	private boolean attemptEmpower(final RobotController rc, final int radiusSquared) {
		assert (radiusSquared > 0) && (radiusSquared <= RobotType.POLITICIAN.actionRadiusSquared) :
			"radiusSquared must lie on (0, RobotType.POLITICIAN.actionRadiusSquared]; radiusSquared: " + radiusSquared;
		// arbitrarily assume empowerment fails.
		boolean empowerSuccessful = false;
		if (rc.canEmpower(radiusSquared)) {
			try {
				rc.empower(radiusSquared);
			} catch (final GameActionException e) {
				// should never happen; canEmpower is a precondition.
				throw new UtilBattlecode.IllegalGameActionException(e);
			}
			empowerSuccessful = true;
		}
		UtilBattlecode.log("empower attempt: " + empowerSuccessful);
		return empowerSuccessful;
	}

	/**
	 * Empowers the nearest non-teammate robot if one lies in-range.
	 * Does nothing itf no non-teammate robots lie in-range.
	 *
	 * @param rc the RobotController for the current round.
	 * @param sensedRobots a collection of robots sensed during the current round.
	 *     Note: filters are applied here while searching for enemies to empower.
	 *     No caller-side filtering is necessary.
	 * @return true iff a non-teammate is successfully Empowered.
	 */
	private boolean attemptEmpowerNearestNonTeammate(final RobotController rc, final Collection<RobotInfo> sensedRobots) {
		// find any in-range non-teammate robot
		final Optional<RobotInfo> nonTeammateRobotOpt =
				sensedRobots.stream()
				.filter(PredicateFactories.robotNonTeam(rc.getTeam()))
				.filter(PredicateFactories.robotInRange(rc.getLocation(), RobotType.POLITICIAN.actionRadiusSquared))
				.findAny();

		// arbitrarily assume the empower fails
		boolean empowerSuccess = false;
		if (nonTeammateRobotOpt.isPresent()) {
			// attempt an empower
			final RobotInfo enemyInfo = nonTeammateRobotOpt.get();
			final int enemyDistSquared = rc.getLocation().distanceSquaredTo(enemyInfo.getLocation());
			empowerSuccess = this.attemptEmpower(rc, enemyDistSquared);
		}
		return empowerSuccess;
	}

	/**
	 * Attempts to move the robot toward a target MapLocation.
	 *
	 * @param rc the RobotController for the current round.
	 * @param targetLoc the MapLocation to attempt to move toward.
	 * @return true iff a move is attempted and successful; else false.
	 */
	private boolean attemptMoveCloser(final RobotController rc, final MapLocation targetLoc) {

		final Function<MapLocation, Double> distanceCostFunc = new Function<MapLocation, Double>() {
			static final int PASSABILITY_WEIGHT = 100000;
			@Override
			public Double apply(final MapLocation mapLoc) {
				try {
					// higher passability, lower distance ----> lower cost
					return -(rc.sensePassability(mapLoc) * PASSABILITY_WEIGHT) + mapLoc.distanceSquaredTo(targetLoc);
				} catch (final GameActionException e) {
					// should never happen; passabilities are sensed only at adjacent (i.e. senseable) locations.
					throw new UtilBattlecode.IllegalGameActionException(e);
				}
			}
		};

		// only select from MapLocations that are closer to the target than rc.getLocation().
		final List<MapLocation> closerMapLocations = new ArrayList<>();
		{
			final Stream<MapLocation> closerMapLocStream =
					UtilBattlecode.makeAllAdjacentMapLocStream(rc.getLocation())
					.filter(PredicateFactories.mapLocCloser(rc.getLocation(), targetLoc));
			UtilGeneral.legalCollect(closerMapLocStream, closerMapLocations);
		}

		// arbitrarily assume the move fails
		boolean moveSuccess = false;
		if (closerMapLocations.size() > 0) {
			// find the MapLocation of least cost and attempt to move there
			final MapLocation moveToLoc = UtilGeneral.getLeastCostLinear(closerMapLocations, distanceCostFunc);
			moveSuccess = HandlerCommon.attemptMove(rc, rc.getLocation().directionTo(moveToLoc));
		}
		return moveSuccess;
	}

	@Override
	public RobotPlayer.IRobotHandler handle(final RobotController rc) {
		this.assignmentHandler = this.assignmentHandler.handle(rc);
		return this;
	}

}
