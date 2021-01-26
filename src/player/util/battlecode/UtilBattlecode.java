package player.util.battlecode;

import java.util.Iterator;
import java.util.function.Predicate;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/**
 * Contains constants/types/functions related to Battlecode.
 */
public class UtilBattlecode {
	
	@FunctionalInterface
	public static interface GameActionPredicate<T> {
		public boolean test(T t) throws GameActionException;
	}
	@FunctionalInterface
	public static interface GameActionFunction<T, R> {
		public R apply(T t) throws GameActionException;
	}
	public static final int MAX_WORLD_WIDTH = 64;
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
	public static Iterator<MapLocation> makeAdjacentMapLocIterator(MapLocation mapLoc) {
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
	public static <T> Predicate<T> wrapGameActionPredicate(GameActionPredicate<T> pred) {
		return new Predicate<T>() {
			@Override
			public boolean test(T t) {
				try {
					return pred.test(t);
				} catch (GameActionException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
					throw new RuntimeException("Use of this function implies this should never happen!");
				}
			}
			
		};
	}

}
