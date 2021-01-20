package player.handlers;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import player.handlers.HandlerCommon.SquadType;
import player.util.UtilMath.DoubleVec2D;
import player.util.UtilMath.Line2D;

public class SquadState {
	public SquadType squadType;
	public int leaderID;
	public Line2D pathLine;
	public DoubleVec2D pathVec;
	public Set<Integer> squadIdSet;
	public Optional<Integer> targetIdOpt;
	
	public SquadState(SquadType squadType, int leaderID, Line2D pathLine,
			    DoubleVec2D pathVec, Set<Integer> squadIdSet, Optional<Integer> targetIdOpt) {
		this.squadType = squadType;
		this.leaderID = leaderID;
		this.pathLine = pathLine;
		this.pathVec = pathVec;
		this.squadIdSet = squadIdSet;
		this.targetIdOpt = targetIdOpt;
	}
	
	public static class Builder {
    	private Optional<SquadType> squadTypeOpt = Optional.empty();
    	private Optional<Integer> leaderIDOpt = Optional.empty();
    	private Optional<Line2D> pathLineOpt = Optional.empty();
    	private Optional<DoubleVec2D> pathVecOpt = Optional.empty();
    	private Optional<Set<Integer>> squadIdSetOpt = Optional.empty();
    	private Optional<Optional<Integer>> targetIdOptOpt = Optional.of(Optional.empty());
		
		public Builder setSquadType(SquadType squadType) {
			this.squadTypeOpt = Optional.of(squadType);
			return this;
		}
		
		public Builder setLeaderID(int leaderID) {
			this.leaderIDOpt = Optional.of(leaderID);
			return this;
		}
		
		public Builder setPathLine(Line2D pathLine) {
			this.pathLineOpt = Optional.of(pathLine);
			return this;
		}
		
		public Builder setPathVec(DoubleVec2D pathVec) {
			this.pathVecOpt = Optional.of(pathVec);
			return this;
		}
		
		public Builder setSquadIdSet(Set<Integer> squadIdSet) {
			this.squadIdSetOpt = Optional.of(squadIdSet);
			return this;
		}
		
		public Builder setTargetId(int targetId) {
			this.targetIdOptOpt = Optional.of(Optional.of(targetId));
			return this;
		}
		
		public SquadState build() {
			boolean isComplete = Stream.of(this.squadTypeOpt, this.leaderIDOpt, this.pathLineOpt,
					this.pathVecOpt, this.squadIdSetOpt, this.targetIdOptOpt)
					.allMatch(opt -> opt.isPresent());
			if (!isComplete) {
				throw new RuntimeException("SquadState missing required build info!");
			}
			return new SquadState(this.squadTypeOpt.get(),this.leaderIDOpt.get(), this.pathLineOpt.get(),
			               this.pathVecOpt.get(),this.squadIdSetOpt.get(), this.targetIdOptOpt.get());
		}
	}
};