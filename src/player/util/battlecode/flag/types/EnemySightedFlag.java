package player.util.battlecode.flag.types;

import java.util.Arrays;
import java.util.List;
import java.util.stream.BaseStream;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagField;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagFieldFactory;
import player.util.battlecode.flag.types.base.fields.MapLocField;
import player.util.battlecode.flag.types.base.fields.RobotTypeField;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagFactory;
import player.util.battlecode.flag.util.UtilFlag.FlagOpCode;
import player.util.math.IntVec2D;

public class EnemySightedFlag extends BaseFlag {
	
	public static List<BaseFlag.IFlagFieldFactory> fieldFactories = Arrays.asList(
			RobotTypeField.getFactory(),
			MapLocField.getFactory()
	);
	
	private static int NUM_BITS = fieldFactories.stream().mapToInt(factory -> factory.numBits()).sum();
	
	private static enum Field { RobotType, Coord }
	
	private RobotTypeField robotType;
	private MapLocField coord;
	
	public EnemySightedFlag(RobotType robotType, MapLocation mapLoc) {
		this.robotType = new RobotTypeField(robotType);
		this.coord = new MapLocField(mapLoc);
	}
	
	private EnemySightedFlag(List<BaseFlag.IFlagField> fieldList) {
		this.robotType = (RobotTypeField)fieldList.get(Field.RobotType.ordinal());
		this.coord = (MapLocField)fieldList.get(Field.Coord.ordinal());
	}
	
	public MapLocation getMapLoc(MapLocation referenceMapLoc) {
		return this.coord.getMapLocation(referenceMapLoc);
	}
	
	public RobotType getRobotType() {
		return this.robotType.getRobotType();
	}

	@Override
	protected List<BaseFlag.IFlagField> getOrderedFlagFieldList() {
		return Arrays.asList(this.robotType, this.coord);
	}
	
	public static EnemySightedFlag decode(int rawFlag) {
		List<BaseFlag.IFlagField> fields = BaseFlag.decodeFields(rawFlag, EnemySightedFlag.fieldFactories);
		return new EnemySightedFlag(fields);	
	}
	
	public static IFlagFactory getFactory() {
		return new IFlagFactory() {

			@Override
			public Flag.IFlag decode(int bits) {
				return EnemySightedFlag.decode(bits);
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