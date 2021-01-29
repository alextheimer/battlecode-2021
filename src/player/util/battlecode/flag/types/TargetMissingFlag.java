package player.util.battlecode.flag.types;

import java.util.Arrays;
import java.util.List;

import battlecode.common.MapLocation;
import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagField;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagFieldFactory;
import player.util.battlecode.flag.types.base.fields.MapLocField;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagFactory;
import player.util.battlecode.flag.util.UtilFlag.FlagOpCode;
import player.util.math.IntVec2D;

public class TargetMissingFlag extends BaseFlag {
	
	private static List<BaseFlag.IFlagFieldFactory> fieldFactories = Arrays.asList(
			MapLocField.getFactory()
	);
	
	private static int NUM_BITS = fieldFactories.stream().mapToInt(factory -> factory.numBits()).sum();
	
	private MapLocField coord;
	
	public TargetMissingFlag(MapLocation mapLoc) {
		this.coord = new MapLocField(mapLoc);
	}
	public TargetMissingFlag(List<BaseFlag.IFlagField> fields) {
		this.coord = (MapLocField)fields.get(0);
	}
	public static TargetMissingFlag decode(int rawFlag) {
		List<BaseFlag.IFlagField> fields = BaseFlag.decodeFields(rawFlag, fieldFactories);
		return new TargetMissingFlag(fields);
	}

	public MapLocation getMapLoc(MapLocation referenceMapLoc) {
		return this.coord.getMapLocation(referenceMapLoc);
	}
	
	@Override
	protected List<BaseFlag.IFlagField> getOrderedFlagFieldList() {
		return Arrays.asList(this.coord);
	}
	public static IFlagFactory getFactory() {
		return new IFlagFactory() {

			@Override
			public Flag.IFlag decode(int bits) {
				return TargetMissingFlag.decode(bits);
			}

			@Override
			public int numBits() {
				return NUM_BITS;
			}
			
		};	
	}
	
	@Override
	public int numBits() {
		return NUM_BITS;
	}
}