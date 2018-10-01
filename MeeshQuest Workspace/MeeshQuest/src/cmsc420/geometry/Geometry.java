package cmsc420.geometry;

import cmsc420.geom.Geometry2D;

public abstract class Geometry implements Geometry2D, Comparable<Geometry> {
	@Override
	public int compareTo(Geometry o) {
		if (this.isCity()) {
			if (o.isCity()) {
				// both are cities
				return ((City) o).getName().compareTo(((City) this).getName());
			} else {
				return -1;
			} 
		} else if (this.isAirport()) {
			if (o.isAirport()) {
				// both are cities
				return ((Airport) o).getName().compareTo(((Airport) this).getName());
			} else {
				return -1;
			}
		} else {
			// this is a road
			if (o.isCity() || o.isAirport()) {
				// o is a city
				return 1;
			} else {
				if (this.isTerminal()) {
					if (o.isTerminal()) {
						// o is a terminal
						if (((Terminal) this).getTerminalName()
								.compareTo(((Terminal) o).getTerminalName()) == 0) {
							// start names are the same so compare end names
							return ((Terminal) o).getEnd().getName()
									.compareTo(((Terminal) this).getEnd().getName());
						} else {
							// start names are different; compare start names
							return ((Terminal) o).getTerminalName()
									.compareTo(((Terminal) this).getTerminalName());
						}
					} else {
						return -1;
					}
				} else {
					if (o.isRoad()) {
						// o is a road
						final String thisStartName = ((Road) this).getStart() != null ? ((Road) this).getStart().getName() : ((Road) this).getStartTerminal().getTerminalName();
						final String oStartName = ((Road) o).getStart() != null ? ((Road) o).getStart().getName() : ((Road) o).getStartTerminal().getTerminalName();

						if (thisStartName
								.compareTo(oStartName) == 0) {
							// start names are the same so compare end names
							final String thisEndName = ((Road) this).getEnd() != null ? ((Road) this).getEnd().getName() : ((Road) this).getEndTerminal().getTerminalName();
							final String oEndName = ((Road) o).getEnd() != null ? ((Road) o).getEnd().getName() : ((Road) o).getEndTerminal().getTerminalName();
							
							return (oEndName
									.compareTo(thisEndName));
						} else {
							/* start names are different; compare start names */
							return (oStartName
									.compareTo(thisStartName));
						}
					} else {
						return 1;
					}
				}
			}
		}
	}
	
	public boolean isRoad() {
		return getType() == Geometry2D.SEGMENT;
	}

	public boolean isCity() {
		return getType() == Geometry2D.POINT;
	}

	//Ugly stupid shit. But HOW else do I differntiate as of right now?...
	public boolean isAirport() {
		return getType() == Geometry2D.CIRCLE;
	}

	public boolean isTerminal() {
		return getType() == Geometry2D.RECTANGLE;
	}

}
