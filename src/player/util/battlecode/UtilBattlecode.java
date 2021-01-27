package player.util.battlecode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.function.Predicate;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import player.util.general.UtilGeneral;

/**
 * Contains constants/types/functions related to Battlecode.
 */
public class UtilBattlecode {
	
	// max size of any dimension of a Battlecode world
	public static final int MAX_WORLD_WIDTH = 64;	
	
	// all non-Center Directions
	public static final Direction OFF_CENTER_DIRECTIONS[] = {
			Direction.NORTH,
			Direction.NORTHEAST,
			Direction.EAST,
			Direction.SOUTHEAST,
			Direction.SOUTH,
			Direction.SOUTHWEST,
			Direction.WEST,
			Direction.NORTHWEST
	};
	
	@FunctionalInterface
	public static interface GameActionPredicate<T> {
		public boolean test(T t) throws GameActionException;
	}
	
	@FunctionalInterface
	public static interface GameActionFunction<T, R> {
		public R apply(T t) throws GameActionException;
	}

	/**
	 * Returns an Iterator that iterates through *all* (on the board,
	 * off the board, occupied, etc) adjacent MapLocations.
	 * 
	 * @param mapLoc the location around which the Iterator will return adjacent locations.
	 */
	public static Iterator<MapLocation> makeAdjacentMapLocIterator(MapLocation mapLoc) {
		// TODO(theimer): worth giving this its own class?
		return new Iterator<MapLocation>() {
			
			private int dirIndex = 0;
			
			@Override
			public boolean hasNext() {
				return (dirIndex < OFF_CENTER_DIRECTIONS.length);
			}
	
			@Override
			public MapLocation next() {
				Direction dir = OFF_CENTER_DIRECTIONS[dirIndex];
				this.dirIndex++;
				return mapLoc.add(dir);
			}
		};
	}
	
	/**
	 * ***WARNING***
	 * This should only ever be used when 100% certain a GameActionException will not be thrown.
	 * ***WARNING***
	 * 
	 * Wraps a GameActionPredicate in an anonymous Predicate that does not throw a GameActionException.
	 * The anonymous Predicate will throw a RuntimeException if the wrapped Predicate throws a GameActionException.
	 */
	public static <T> Predicate<T> silenceGameActionPredicate(GameActionPredicate<T> pred) {
		return new Predicate<T>() {
			@Override
			public boolean test(T t) {
				try {
					return pred.test(t);
				} catch (GameActionException e) {
					throw new RuntimeException("A GameActionException occurred inside a silenced GameActionPredicate.\n" +
											   "Note: Use of this wrapper implies this should never happen!\n" +
							                   "Begin GameActionException stack trace:\n" + UtilGeneral.stringifyStackTrace(e));
				}
			}
			
		};
	}

}
