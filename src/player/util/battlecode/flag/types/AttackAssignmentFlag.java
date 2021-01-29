package player.util.battlecode.flag.types;

import java.util.Arrays;
import java.util.List;

import player.util.battlecode.flag.Flag;
import player.util.battlecode.flag.Flag.IFlag;
import player.util.battlecode.flag.types.base.BaseFlag;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagField;
import player.util.battlecode.flag.types.base.BaseFlag.IFlagFieldFactory;
import player.util.battlecode.flag.types.base.fields.CoordField;
import player.util.battlecode.flag.types.base.fields.DegreesField;
import player.util.battlecode.flag.util.FlagWalker;
import player.util.battlecode.flag.util.UtilFlag;
import player.util.battlecode.flag.util.UtilFlag.IFlagFactory;
import player.util.battlecode.flag.util.UtilFlag.FlagOpCode;
import player.util.math.IntVec2D;

public class AttackAssignmentFlag extends BaseFlag {
	
	private static List<BaseFlag.IFlagFieldFactory> fieldFactories = Arrays.asList(
			CoordField.getFactory()
	);
	private static int NUM_BITS = fieldFactories.stream().mapToInt(factory -> factory.numBits()).sum();
	
	private CoordField coord;
	
	public AttackAssignmentFlag(int x, int y) {
		this.coord = new CoordField(x, y);
	}
	
	private AttackAssignmentFlag(List<BaseFlag.IFlagField> flagFields) {
		this.coord = (CoordField)flagFields.get(0);
	}
	
	public static AttackAssignmentFlag decode(int rawFlag) {
		List<BaseFlag.IFlagField> fields = BaseFlag.decodeFields(rawFlag, fieldFactories);
		return new AttackAssignmentFlag(fields);
	}
	
	public IntVec2D getCoord() {
		return this.coord.value();
	}

	@Override
	protected List<BaseFlag.IFlagField> getOrderedFlagFieldList() {
		return Arrays.asList(this.coord);
	}
	
	public static IFlagFactory getFactory() {
		return new IFlagFactory() {

			@Override
			public Flag.IFlag decode(int bits) {
				return AttackAssignmentFlag.decode(bits);
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