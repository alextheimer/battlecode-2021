package player.util.battlecode.flag.fields;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleImmutableEntry;

import battlecode.common.Direction;
import player.util.battlecode.flag.util.FlagWalker;

public class DirectionField {
	public static final int NUM_BITS = 3;
	
	private static Map<Direction, Integer> dirToIndexMap;
	private static Map<Integer, Direction> indexToDirMap;
	
	static {
		List<SimpleImmutableEntry<Direction, Integer>> dirIndexPairs = Arrays.asList(
				new SimpleImmutableEntry<>(Direction.NORTH, 0),
				new SimpleImmutableEntry<>(Direction.NORTH, 1),
				new SimpleImmutableEntry<>(Direction.NORTH, 2),
				new SimpleImmutableEntry<>(Direction.NORTH, 3),
				new SimpleImmutableEntry<>(Direction.NORTH, 4),
				new SimpleImmutableEntry<>(Direction.NORTH, 5),
				new SimpleImmutableEntry<>(Direction.NORTH, 6),
				new SimpleImmutableEntry<>(Direction.NORTH, 7)
		);
		for (SimpleImmutableEntry<Direction, Integer> entry : dirIndexPairs) {
			dirToIndexMap.put(entry.getKey(), entry.getValue());
			indexToDirMap.put(entry.getValue(), entry.getKey());
		}
	}
	
	private Direction direction;
	
	public DirectionField(Direction direction) {
		// TODO(theimer): assertions
		this.direction = direction;
	}
	
	public int toBits() {
		FlagWalker flagWalker = new FlagWalker(0);
		flagWalker.writeBits(NUM_BITS, DirectionField.dirToIndexMap.get(this.direction));
		return flagWalker.getAllBits();
	}
	
	public static DirectionField fromBits(int bits) {
		FlagWalker flagWalker = new FlagWalker(bits);
		int index = flagWalker.readBits(NUM_BITS);
		return new DirectionField(DirectionField.indexToDirMap.get(index));
	}
	
	public Direction value() {
		return this.direction;
	}
}