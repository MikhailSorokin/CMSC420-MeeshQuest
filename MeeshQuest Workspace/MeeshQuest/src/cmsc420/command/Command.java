package cmsc420.command;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.drawing.CanvasPlus;
import cmsc420.geom.Circle2D;
import cmsc420.geom.Inclusive2DIntersectionVerifier;
import cmsc420.geom.Shape2DDistanceCalculator;
import cmsc420.geometry.Airport;
import cmsc420.geometry.City;
import cmsc420.geometry.CityLocationComparator;
import cmsc420.geometry.Geometry;
import cmsc420.geometry.Metropole;
import cmsc420.geometry.Road;
import cmsc420.geometry.RoadAdjacencyList;
import cmsc420.geometry.Terminal;
import cmsc420.mxquadtree.MXQuadtree;
import cmsc420.pmquadtree.AddOutOfBoundsThrowable;
import cmsc420.pmquadtree.AirportDoesNotExistThrowable;
import cmsc420.pmquadtree.AirportNotSameMetropoleThrowable;
import cmsc420.pmquadtree.CityDoesNotExistThrowable;
import cmsc420.pmquadtree.CityNotMappedThrowable;
import cmsc420.pmquadtree.DuplicateCoordinateThrowable;
import cmsc420.pmquadtree.DuplicateNameThrowable;
import cmsc420.pmquadtree.EndDoesNotExistThrowable;
import cmsc420.pmquadtree.NotSameMetropoleThrowable;
import cmsc420.pmquadtree.OutOfBoundsThrowable;
import cmsc420.pmquadtree.PM1Quadtree;
import cmsc420.pmquadtree.PM3Quadtree;
import cmsc420.pmquadtree.PMQuadtree;
import cmsc420.pmquadtree.PMQuadtree.Black;
import cmsc420.pmquadtree.PMQuadtree.Gray;
import cmsc420.pmquadtree.PMQuadtree.Node;
import cmsc420.pmquadtree.PMRuleViolationThrowable;
import cmsc420.pmquadtree.RoadAlreadyExistsThrowable;
import cmsc420.pmquadtree.RoadIntersectingThrowable;
import cmsc420.pmquadtree.StartDoesNotExistThrowable;
import cmsc420.sortedmap.GuardedAvlGTree;

/**
 * Processes each command in the MeeshQuest program. Takes in an XML command
 * node, processes the node, and outputs the results.
 */
public class Command {
	/** output DOM Document tree */
	protected Document results;

	/** root node of results document */
	protected Element resultsNode;

	/**
	 * stores created cities sorted by their names (used with listCities
	 * command)
	 */
	protected GuardedAvlGTree<String, City> citiesByName;
	
	protected Map<String, Airport> airportsByName;
	
	protected Map<String, Terminal> terminalsByName;
	
	protected Map<Metropole, PMQuadtree> remotetoLocalMap;
	
	/**
	 * stores created cities sorted by their locations (used with listCities
	 * command)
	 */
	protected final TreeSet<City> citiesByLocation = new TreeSet<City>(
			new CityLocationComparator());
	
	private final RoadAdjacencyList roads = new RoadAdjacencyList();

	/** stores mapped cities in a spatial data structure */
	public static MXQuadtree remoteSpatialMap;

	/** order of the PM Quadtree */
	protected int pmOrder;

	/** spatial width of the metropoles */
	protected static int localSpatialWidth;

	/** spatial height of the metropoles */
	protected static int localSpatialHeight;
	
	/** spatial width of the cities */
	public static int remoteSpatialWidth;

	/** spatial height of the PM Quadtree */
	public static int remoteSpatialHeight;
	
	/**
	 * Set the DOM Document tree to send the results of processed commands to.
	 * Creates the root results node.
	 * 
	 * @param results
	 *            DOM Document tree
	 */
	public void setResults(Document results) {
		this.results = results;
		resultsNode = results.createElement("results");
		results.appendChild(resultsNode);
	}

	/**
	 * Creates a command result element. Initializes the command name.
	 * 
	 * @param node
	 *            the command node to be processed
	 * @return the results node for the command
	 */
	private Element getCommandNode(final Element node) {
		final Element commandNode = results.createElement("command");
		commandNode.setAttribute("name", node.getNodeName());
		
		if (node.hasAttribute("id")) {
		    commandNode.setAttribute("id", node.getAttribute("id"));
		}
		return commandNode;
	}

	/**
	 * Processes an integer attribute for a command. Appends the parameter to
	 * the parameters node of the results. Should not throw a number format
	 * exception if the attribute has been defined to be an integer in the
	 * schema and the XML has been validated beforehand.
	 * 
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            integer attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return integer attribute value
	 */
	private int processIntegerAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add the parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the integer value */
		return Integer.parseInt(value);
	}

	/**
	 * Processes a string attribute for a command. Appends the parameter to the
	 * parameters node of the results.
	 * 
	 * @param commandNode
	 *            node containing information about the command
	 * @param attributeName
	 *            string attribute to be processed
	 * @param parametersNode
	 *            node to append parameter information to
	 * @return string attribute value
	 */
	private String processStringAttribute(final Element commandNode,
			final String attributeName, final Element parametersNode) {
		final String value = commandNode.getAttribute(attributeName);

		if (parametersNode != null) {
			/* add parameters to results */
			final Element attributeNode = results.createElement(attributeName);
			attributeNode.setAttribute("value", value);
			parametersNode.appendChild(attributeNode);
		}

		/* return the string value */
		return value;
	}

	/**
	 * Reports that the requested command could not be performed because of an
	 * error. Appends information about the error to the results.
	 * 
	 * @param type
	 *            type of error that occurred
	 * @param command
	 *            command node being processed
	 * @param parameters
	 *            parameters of command
	 */
	private void addErrorNode(final String type, final Element command,
			final Element parameters) {
		final Element error = results.createElement("error");
		error.setAttribute("type", type);
		error.appendChild(command);
		error.appendChild(parameters);
		resultsNode.appendChild(error);
	}

	/**
	 * Reports that a command was successfully performed. Appends the report to
	 * the results.
	 * 
	 * @param command
	 *            command not being processed
	 * @param parameters
	 *            parameters used by the command
	 * @param output
	 *            any details to be reported about the command processed
	 */
	private Element addSuccessNode(final Element command,
			final Element parameters, final Element output) {
		final Element success = results.createElement("success");
		success.appendChild(command);
		success.appendChild(parameters);
		success.appendChild(output);
		resultsNode.appendChild(success);
		return success;
	}

	/**
	 * Processes the commands node (root of all commands). Gets the spatial
	 * width and height of the map and send the data to the appropriate data
	 * structures.
	 * 
	 * @param node
	 *            commands node to be processed
	 */
	public void processCommands(final Element node) {
		localSpatialWidth = Integer.parseInt(node.getAttribute("localSpatialWidth"));
		localSpatialHeight = Integer.parseInt(node.getAttribute("localSpatialHeight"));
		remoteSpatialWidth = Integer.parseInt(node.getAttribute("remoteSpatialWidth"));
		remoteSpatialHeight = Integer.parseInt(node.getAttribute("remoteSpatialHeight"));
		
		remoteSpatialMap = new MXQuadtree();
		remoteSpatialMap.setRange(remoteSpatialWidth, remoteSpatialHeight);
		
		pmOrder = Integer.parseInt(node.getAttribute("pmOrder"));

        citiesByName = new GuardedAvlGTree<String, City>(new Comparator<String>() {
        	
    		@Override
    		public int compare(String o1, String o2) {
    			return o2.compareTo(o1);
    		}
    		
    	},
                Integer.parseInt(node.getAttribute("g")));
        
        airportsByName = new TreeMap<String, Airport>(new Comparator<String>() {
        	@Override
    		public int compare(String o1, String o2) {
    			return o2.compareTo(o1);
    		}
        });
        
        terminalsByName = new TreeMap<String, Terminal>(new Comparator<String>() {
        	@Override
    		public int compare(String o1, String o2) {
    			return o2.compareTo(o1);
    		}
        });
        
        remotetoLocalMap = new HashMap<Metropole, PMQuadtree>();
	}

	/**
	 * Processes a createCity command. Creates a city in the dictionary (Note:
	 * does not map the city). An error occurs if a city with that name or
	 * location is already in the dictionary.
	 * 
	 * @param node
	 *            createCity node to be processed
	 */
	public void processCreateCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);
		final int localX = processIntegerAttribute(node, "localX", parametersNode);
		final int localY = processIntegerAttribute  (node, "localY", parametersNode);
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		final int radius = processIntegerAttribute(node, "radius", parametersNode);
		final String color = processStringAttribute(node, "color", parametersNode);

		/* create the city */
		final City city = new City(name, localX, localY, remoteX, remoteY, radius, color);
		
		Metropole newRemoteCoordinate = new Metropole(name, remoteX, remoteY);
		if (remotetoLocalMap.get(newRemoteCoordinate) == null) {
			PMQuadtree pmQuadtree = null;
			if (pmOrder == 3) {
				pmQuadtree = new PM3Quadtree(localSpatialWidth, localSpatialHeight);
			} else if (pmOrder == 1) {
				pmQuadtree = new PM1Quadtree(localSpatialWidth, localSpatialHeight);
			}
			remotetoLocalMap.put(newRemoteCoordinate, pmQuadtree);
		} 
	
		
		remoteSpatialMap.add(city);

		if (hasSameLocation(localX, localY, remoteX, remoteY)) {
			addErrorNode("duplicateCityCoordinates", commandNode, parametersNode);
		} else if (citiesByName.containsKey(name)) {
			addErrorNode("duplicateCityName", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");

			/* add city to dictionary */
			citiesByName.put(name, city);
			citiesByLocation.add(city);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}
	
	private boolean hasSameLocation(int getLocalX, int getLocalY, int getRemoteX, int getRemoteY) {
		for (City city : citiesByName.values()) {
			if (city.getLocalX() == getLocalX
					&& city.getLocalY() == getLocalY
					&& city.getRemoteX() == getRemoteX
					&& city.getRemoteY() == getRemoteY) {
				return true;
			}
		}
		
		for (Airport airport : airportsByName.values()) {
			if (airport.getLocalX() == getLocalX
					&& airport.getLocalY() == getLocalY
					&& airport.getRemoteX() == getRemoteX
					&& airport.getRemoteY() == getRemoteY) {
				return true;
			}
		}
		
		for (Terminal terminal : terminalsByName.values()) {
			if (terminal.getLocalX() == getLocalX
					&& terminal.getLocalY() == getLocalY
					&& terminal.getRemoteX() == getRemoteX
					&& terminal.getRemoteY() == getRemoteY) {
				return true;
			}
		}
		
		return false;		
	}


	/**
	 * Processes a deleteCity command. Deletes a city in the dictionary (Note:
	 * does not map the city). An error occurs if a city with that name or
	 * location is already in the dictionary.
	 * 
	 * @param node
	 *            deleteCity node to be processed
	 */
	public void processDeleteCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		if (citiesByName.get(name) == null) {
			addErrorNode("cityDoesNotExist", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");
			
			PMQuadtree localPMToUse = null;
			
			//FIXED: Need to remove from pm quadtree as well
			for (Metropole metropole : remotetoLocalMap.keySet()) {
				if (metropole.getX() == citiesByName.get(name).getRemoteX() &&
						metropole.getY() == citiesByName.get(name).getRemoteY()) {
					localPMToUse = remotetoLocalMap.get(metropole);
				}
			}
			
			if (localPMToUse.containsCity(name)) {
				localPMToUse.deleteGeometry(citiesByName.get(name));
				final Element cityUnmappedNode = results
						.createElement("cityUnmapped");
				cityUnmappedNode.setAttribute("name", name);
				cityUnmappedNode.setAttribute("localX", Integer.toString((int) citiesByName.get(name).getLocalX()));
				cityUnmappedNode.setAttribute("localY", Integer.toString((int) citiesByName.get(name).getLocalY()));
				cityUnmappedNode.setAttribute("remoteX", Integer.toString((int) citiesByName.get(name).getRemoteX()));
				cityUnmappedNode.setAttribute("remoteY", Integer.toString((int) citiesByName.get(name).getRemoteY()));
				cityUnmappedNode.setAttribute("color", citiesByName.get(name).getColor());
				cityUnmappedNode.setAttribute("radius", Integer.toString((int) citiesByName.get(name).getRadius()));
				outputNode.appendChild(cityUnmappedNode);
			}
			
			TreeSet<Road> order = new TreeSet<Road>();
			TreeSet<Road> adjacentRoads = roads.getRoadSet(citiesByName.get(name));

			for (Road adjRoad : adjacentRoads) {
				try {
					localPMToUse.deleteGeometry(adjRoad);
					order.add(adjRoad);
				} catch (StartDoesNotExistThrowable | EndDoesNotExistThrowable e) {
					e.printStackTrace();
				}
			}
			
			HashSet<Terminal> terminalsToEliminate = new HashSet<Terminal>();
			for (Terminal terminal : terminalsByName.values()) {
				if (terminal.getEnd() != null && terminal.getEnd().equals(citiesByName.get(name))) {
					localPMToUse.deleteGeometry(terminal, false);
					terminalsToEliminate.add(terminal);
					order.add(new Road(terminal));
				}
			}
			
			for (Terminal temp : terminalsToEliminate) {
				terminalsByName.remove(temp.getTerminalName());
			}
			
			for (Road r : order) {
				final Element roadUnmappedNode = results.createElement("roadUnmapped");
				City c1 = ((Road) r).getStart();
				City c2 = ((Road) r).getEnd();
				roadUnmappedNode.setAttribute("start", c1 != null ? c1.getName() : ((Road) r).getStartTerminal().getTerminalName());
				roadUnmappedNode.setAttribute("end", c2 != null ? c2.getName() : ((Road) r).getEndTerminal().getTerminalName());
				if (adjacentRoads.contains(r))
					adjacentRoads.remove(r);
				
				outputNode.appendChild(roadUnmappedNode);
			}

			/* remove city from this dictionary */
			citiesByName.remove(name);
			
			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}
	
	/**
	 * Clears all the data structures do there are not cities or roads in
	 * existence in the dictionary or on the map.
	 * 
	 * @param node
	 *            clearAll node to be processed
	 */
	public void processClearAll(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* clear data structures */
		citiesByName.clear();
		citiesByLocation.clear();
		remoteSpatialMap.clear();
		airportsByName.clear();
		terminalsByName.clear();
		roads.clear();
		remotetoLocalMap.clear();
		
		/* clear canvas */
		// canvas.clear();
		/* add a rectangle to show where the bounds of the map are located */
		// canvas.addRectangle(0, 0, spatialWidth, spatialHeight, Color.BLACK,
		// false);
		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Lists all the cities, either by name or by location.
	 * 
	 * @param node
	 *            listCities node to be processed
	 */
	public void processListCities(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String sortBy = processStringAttribute(node, "sortBy",
				parametersNode);

		if (citiesByName.isEmpty()) {
			addErrorNode("noCitiesToList", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");
			final Element cityListNode = results.createElement("cityList");
			
			Collection<City> cityCollection = null;
			if (sortBy.equals("name")) {
				cityCollection = citiesByName.values();
			} else if (sortBy.equals("coordinate")) {
				cityCollection = citiesByLocation;
			} else {
				/* XML validator failed */
				System.exit(-1);
			}

			for (City c : cityCollection) {
				addCityNode(cityListNode, c);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param cityNodeName
	 *            name of city node
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final String cityNodeName,
			final City city) {
		final Element cityNode = results.createElement(cityNodeName);
		cityNode.setAttribute("name", city.getName());
		cityNode.setAttribute("localX", Integer.toString((int) city.getLocalX()));
		cityNode.setAttribute("localY", Integer.toString((int) city.getLocalY()));
		cityNode.setAttribute("remoteX", Integer.toString((int) city.getRemoteX()));
		cityNode.setAttribute("remoteY", Integer.toString((int) city.getRemoteY()));
		cityNode.setAttribute("color", city.getColor());
		cityNode.setAttribute("radius", Integer.toString((int) city.getRadius()));
		node.appendChild(cityNode);
	}

	private void addCityNode(final Element node, final City city) {
		addCityNode(node, "city", city);
	}

    public void processPrintAvlTree(Element node) {
        final Element commandNode = getCommandNode(node);
        final Element parametersNode = results.createElement("parameters");
        final Element outputNode = results.createElement("output");

        if (citiesByName.isEmpty()) {
            addErrorNode("emptyTree", commandNode, parametersNode);
        } else {
            outputNode.appendChild(citiesByName.createXml(outputNode));
            addSuccessNode(commandNode, parametersNode, outputNode);
        }
    }

	public void processMapRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String start = processStringAttribute(node, "start",
				parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);

		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(start)) {
			addErrorNode("startPointDoesNotExist", commandNode, parametersNode);
			return;
		} else if (!citiesByName.containsKey(end)) {
			addErrorNode("endPointDoesNotExist", commandNode, parametersNode);
			return;
		} else if (start.equals(end)) {
			addErrorNode("startEqualsEnd", commandNode, parametersNode);
			return;
		} else {
			PMQuadtree localPMToUse = null;
			Road road = null;
			try {
				road = new Road((City) citiesByName.get(start),
						(City) citiesByName.get(end));
				// add to spatial structure
				
				for (Metropole metropole : remotetoLocalMap.keySet()) {
					if (metropole.getX() == road.getStart().getRemoteX() &&
							metropole.getY() == road.getStart().getRemoteY()
							&& metropole.getX() == road.getEnd().getRemoteX() 
							&& metropole.getY() == road.getEnd().getRemoteY()) {
						localPMToUse = remotetoLocalMap.get(metropole);
					}
				}
				
				if (localPMToUse == null) {
					addErrorNode("roadNotInOneMetropole", commandNode, parametersNode);
					return;
				}
				
				localPMToUse.addRoad(road);
				if (Inclusive2DIntersectionVerifier.intersects(citiesByName
						.get(start).toPoint2D(), new Rectangle2D.Float(0, 0,
								localSpatialWidth, localSpatialHeight))
						&& Inclusive2DIntersectionVerifier.intersects(
								citiesByName.get(end).toPoint2D(),
								new Rectangle2D.Float(0, 0, localSpatialWidth,
										localSpatialHeight))) {
					// add to adjacency list
					roads.addRoad((City) citiesByName.get(start),
							(City) citiesByName.get(end));
				}
				// create roadCreated element
				final Element roadCreatedNode = results
						.createElement("roadCreated");
				roadCreatedNode.setAttribute("start", start);
				roadCreatedNode.setAttribute("end", end);
				outputNode.appendChild(roadCreatedNode);
				// add success node to results
				addSuccessNode(commandNode, parametersNode, outputNode);
			} catch (OutOfBoundsThrowable e) {
				addErrorNode("roadOutOfBounds", commandNode, parametersNode);
			} catch (RoadAlreadyExistsThrowable e) {
				addErrorNode("roadAlreadyMapped", commandNode, parametersNode);
			} catch (RoadIntersectingThrowable e) {
				addErrorNode("roadIntersectsAnotherRoad", commandNode, parametersNode);
			} catch (PMRuleViolationThrowable e) {
				try {
					localPMToUse.deleteGeometry(road);
				} catch (StartDoesNotExistThrowable | EndDoesNotExistThrowable lee) {
					addErrorNode("roadViolatesPMRules", commandNode, parametersNode);
					return;
				}
				addErrorNode("roadViolatesPMRules", commandNode, parametersNode);
				return;
			}
		}
	}
	
	public void processMapAirport(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String airportName = processStringAttribute(node, "name", parametersNode);
		final int localX = processIntegerAttribute(node, "localX", parametersNode);
		final int localY = processIntegerAttribute(node, "localY", parametersNode);
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		final String terminalName = processStringAttribute(node, "terminalName", parametersNode);
		final int terminalX = processIntegerAttribute(node, "terminalX", parametersNode);
		final int terminalY = processIntegerAttribute(node, "terminalY", parametersNode);
		final String terminalCityName = processStringAttribute(node, "terminalCity", parametersNode);
		
		final Element outputNode = results.createElement("output");

		if (airportsByName.containsKey(airportName)) {
			addErrorNode("duplicateAirportName", commandNode, parametersNode);
		} else if (hasSameLocation(localX, localY, remoteX, remoteY)) {
			addErrorNode("duplicateAirportCoordinates", commandNode, parametersNode);
		} else {
			//Terminal should have same remote but different local
			Terminal t = new Terminal(airportName, terminalName, terminalX, terminalY, remoteX, remoteY, citiesByName.get(terminalCityName)); 
			Airport a = new Airport(airportName, localX, localY, remoteX, remoteY);
			
			PMQuadtree localPMToUse = null;
			for (Metropole metropole : remotetoLocalMap.keySet()) {
				if (metropole.getX() == a.getRemoteX() &&
						metropole.getY() == a.getRemoteY()) {
					localPMToUse = remotetoLocalMap.get(metropole);
				}
			}

			if (a.remotePoint2D().getX() < 0 || a.remotePoint2D().getY() < 0
					|| a.remotePoint2D().getX() >= remoteSpatialWidth || a.remotePoint2D().getY() >= remoteSpatialHeight) {
				addErrorNode("airportOutOfBounds", commandNode, parametersNode);	
				return;
			}
			
			if (localPMToUse != null) {
				try {
					localPMToUse.addAirport(a, t, terminalsByName.containsKey(terminalName), 
							hasSameLocation(terminalX, terminalY, remoteX, remoteY),
							citiesByName.get(terminalCityName));
				} catch (OutOfBoundsThrowable e) {
					addErrorNode("airportOutOfBounds", commandNode, parametersNode);
					return;
				} catch (DuplicateNameThrowable e) {
					addErrorNode("duplicateTerminalName", commandNode, parametersNode);
					return;
				} catch (DuplicateCoordinateThrowable e) {
					addErrorNode("duplicateTerminalCoordinates", commandNode, parametersNode);
					return;
				}  catch (AddOutOfBoundsThrowable e) {
					addErrorNode("terminalOutOfBounds", commandNode, parametersNode);
					return;
				} catch (CityDoesNotExistThrowable e) {
					addErrorNode("connectingCityDoesNotExist", commandNode, parametersNode);
					return;
				} catch (NotSameMetropoleThrowable e) {
					addErrorNode("connectingCityNotInSameMetropole", commandNode, parametersNode);
					return;
				}  catch (PMRuleViolationThrowable e) {
					localPMToUse.deleteGeometry(a);
					addErrorNode("airportViolatesPMRules", commandNode, parametersNode);
					return;
				}
			
				
				try {
					localPMToUse.addTerminal(t, citiesByName.get(terminalCityName));
				} catch (CityNotMappedThrowable e) {
					addErrorNode("connectingCityNotMapped", commandNode, parametersNode);
					return;
				} catch (RoadIntersectingThrowable e) {
					addErrorNode("roadIntersectsAnotherRoad", commandNode, parametersNode);
					return;
				} catch (PMRuleViolationThrowable e) {
					localPMToUse.deleteGeometry(t, false);
					addErrorNode("terminalViolatesPMRules", commandNode, parametersNode);
					return;
				}
			
			} else {
				addErrorNode("connectingCityNotInSameMetropole", commandNode, parametersNode);
				return;
			}
			
			//FIXED: Need a connectingCityNotInSameMetropole error detection
			airportsByName.put(airportName, a);
			terminalsByName.put(terminalName, t);
			
			addSuccessNode(commandNode, parametersNode, outputNode);

		}
	}

	public void processMapTerminal(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);
		final int localX = processIntegerAttribute(node, "localX", parametersNode);
		final int localY = processIntegerAttribute(node, "localY", parametersNode);
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		final String terminalCityName = processStringAttribute(node, "cityName", parametersNode);
		final String airportName = processStringAttribute(node, "airportName", parametersNode);
		
		final Element outputNode = results.createElement("output");
		
		if (terminalsByName.containsKey(name)) {
			addErrorNode("duplicateTerminalName", commandNode, parametersNode);
			return;
		} else if (hasSameLocation(localX, localY, remoteX, remoteY)) {
			addErrorNode("duplicateTerminalCoordinates", commandNode, parametersNode);
			return;
		} 

		Terminal t = new Terminal(airportName, name, localX, localY, remoteX, remoteY, citiesByName.get(terminalCityName)); 

		PMQuadtree localPMToUse = null;
		
		if (t.remotePoint2D().getX() < 0 || t.remotePoint2D().getY() < 0
				|| t.remotePoint2D().getX() >= remoteSpatialWidth || t.remotePoint2D().getY() >= remoteSpatialHeight) {
			addErrorNode("terminalOutOfBounds", commandNode, parametersNode);	
			return;
		}
		
		for (Metropole metropole : remotetoLocalMap.keySet()) {
			//System.out.println("Metropole X: " + metropole + ", Terminal: " + "(" + t.getRemoteX() + ", " + t.getRemoteY() + ")");
			if (metropole.getX() == t.getRemoteX() &&
					metropole.getY() == t.getRemoteY()) {
				localPMToUse = remotetoLocalMap.get(metropole);
			}
		}
		
		
		try {
			localPMToUse.addTerminal(t, airportsByName.get(airportName)
					, citiesByName.get(terminalCityName));
		} catch (OutOfBoundsThrowable e) {
			addErrorNode("terminalOutOfBounds", commandNode, parametersNode);
			return;
		} catch (AirportDoesNotExistThrowable e) {
			addErrorNode("airportDoesNotExist", commandNode, parametersNode);
			return;
		} catch (AirportNotSameMetropoleThrowable e) {
			addErrorNode("airportNotInSameMetropole", commandNode, parametersNode);
			return;
		} catch (CityDoesNotExistThrowable e) {
			addErrorNode("connectingCityDoesNotExist", commandNode, parametersNode);
			return;
		} catch (NotSameMetropoleThrowable e) {
			addErrorNode("connectingCityNotInSameMetropole", commandNode, parametersNode);
			return;
		} catch (CityNotMappedThrowable e) {
			addErrorNode("connectingCityNotMapped", commandNode, parametersNode);
			return;
		} catch (RoadIntersectingThrowable e) {
			addErrorNode("roadIntersectsAnotherRoad", commandNode, parametersNode);
			return;
		} catch (PMRuleViolationThrowable e) {
			localPMToUse.deleteGeometry(t, false);
			addErrorNode("terminalViolatesPMRules", commandNode, parametersNode);
			return;
		}
		
		//FIXED: Need a connectingCityNotInSameMetropole error detection
		terminalsByName.put(terminalCityName, t);
		
		addSuccessNode(commandNode, parametersNode, outputNode);

	}
	
	public void processUnmapRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String start = processStringAttribute(node, "start",
				parametersNode);
		final String end = processStringAttribute(node, "end", parametersNode);

		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(start)) {
			addErrorNode("startPointDoesNotExist", commandNode, parametersNode);
		} else if (!citiesByName.containsKey(end)) {
			addErrorNode("endPointDoesNotExist", commandNode, parametersNode);
		} else if (start.equals(end)) {
			addErrorNode("startEqualsEnd", commandNode, parametersNode);
		} else {
			Road roadToRemove = null;
			boolean hasRoadBetweenStartEnd = false;
			
			TreeSet<Road> adjacentRoads = roads.getRoadSet(citiesByName.get(start));

			for (Road adjRoad : adjacentRoads) {
				if (adjRoad.getStart().equals(citiesByName.get(end)) || adjRoad.getEnd().equals(citiesByName.get(end))) {
					roadToRemove = adjRoad;
					hasRoadBetweenStartEnd = true;
				}
			}
			
			if (!hasRoadBetweenStartEnd) {
				addErrorNode("roadNotMapped", commandNode, parametersNode);
				return;
			}
			
			PMQuadtree localPMToUse = null;
			for (Metropole metropole : remotetoLocalMap.keySet()) {
				if (metropole.getX() == roadToRemove.getStart().getRemoteX() &&
						metropole.getY() == roadToRemove.getStart().getRemoteY()
						&& metropole.getX() == roadToRemove.getEnd().getRemoteX() 
						&& metropole.getY() == roadToRemove.getEnd().getRemoteY()) {
					localPMToUse = remotetoLocalMap.get(metropole);
				}
			}
			
			// add to spatial structure
			try {
				localPMToUse.deleteGeometry(roadToRemove);
			} catch (StartDoesNotExistThrowable e) {
				addErrorNode("startPointDoesNotExist", commandNode, parametersNode);
				return;
			} catch (EndDoesNotExistThrowable e) {
				addErrorNode("endPointDoesNotExist", commandNode, parametersNode);
				return;
			}
			
			adjacentRoads.remove(roadToRemove);
			
			// create roadCreated element
			final Element roadUnmappedNode = results
					.createElement("roadDeleted");
			roadUnmappedNode.setAttribute("start", start);
			roadUnmappedNode.setAttribute("end", end);
			outputNode.appendChild(roadUnmappedNode);
			// add success node to results
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}
	
	public void processUnmapAirport(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);
		
		final Element outputNode = results.createElement("output");
		
		if (!airportsByName.containsKey(name)) {
			addErrorNode("airportDoesNotExist", commandNode, parametersNode);
			return;
		} else {
			PMQuadtree localPMToUse = null;
			for (Metropole metropole : remotetoLocalMap.keySet()) {
				if (metropole.getX() == airportsByName.get(name).getRemoteX() &&
						metropole.getY() == airportsByName.get(name).getRemoteY()) {
					localPMToUse = remotetoLocalMap.get(metropole);
				}
			}
			// add to spatial structure
			localPMToUse.deleteGeometry(airportsByName.get(name));			
			airportsByName.remove(name);

			//FIXED: Remove all terminals associated with this airport
			
			HashSet<Terminal> terminalsToEliminate = new HashSet<Terminal>();
			for (Terminal terminal : terminalsByName.values()) {
				if (terminal.getAirportName().equals(name)) {
					localPMToUse.deleteGeometry(terminal, false);
					//TODO: Uhh... don't know if I should delete a city from here but who knows...
					/*localPMToUse.deleteGeometry(terminal.getEnd());
					citiesByName.remove(terminal.getEnd().getName());*/
					
					terminalsToEliminate.add(terminal);
					
					final Element terminalUnmappedNode = results.createElement("terminalUnmapped");
					terminalUnmappedNode.setAttribute("airportName", terminal.getAirportName());
					terminalUnmappedNode.setAttribute("cityName", terminal.getEnd().getName());
					terminalUnmappedNode.setAttribute("localX", Integer.toString(terminal.getLocalX()));
					terminalUnmappedNode.setAttribute("localY", Integer.toString(terminal.getLocalY()));
					terminalUnmappedNode.setAttribute("name", terminal.getTerminalName());
					terminalUnmappedNode.setAttribute("remoteX", Integer.toString(terminal.getRemoteX()));
					terminalUnmappedNode.setAttribute("remoteY", Integer.toString(terminal.getRemoteY()));
					
					outputNode.appendChild(terminalUnmappedNode);
				}
			}
			
			for (Terminal temp : terminalsToEliminate) {
				terminalsByName.remove(temp.getTerminalName());
			}

		}
		
		addSuccessNode(commandNode, parametersNode, outputNode);
	}
	
	public void processUnmapTerminal(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);
		
		final Element outputNode = results.createElement("output");
		
		if (!terminalsByName.containsKey(name)) {
			addErrorNode("terminalDoesNotExist", commandNode, parametersNode);
			return;
		} else {
			PMQuadtree localPMToUse = null;
			for (Metropole metropole : remotetoLocalMap.keySet()) {
				if (metropole.getX() == terminalsByName.get(name).getRemoteX() &&
						metropole.getY() == terminalsByName.get(name).getRemoteY()) {
					localPMToUse = remotetoLocalMap.get(metropole);
				}
			}
			
			// add to spatial structure
			localPMToUse.deleteGeometry(terminalsByName.get(name), false);
			//TODO: Remove all airports associated with terminal
			
			int airportOfTerminalNum = 0;
			for (Terminal terminal : terminalsByName.values()) {
				if (terminal.getAirportName().equals(terminalsByName.get(name).getAirportName())) {
					airportOfTerminalNum++;
				}
			}
			
			if (airportOfTerminalNum == 0) {
				Airport airport = airportsByName.get(terminalsByName.get(name).getAirportName());
				localPMToUse.deleteGeometry(airport);
				airportsByName.remove(airport.getName());
				final Element airportUnmappedNode = results.createElement("airportUnmapped");
				airportUnmappedNode.setAttribute("name", airport.getName());
				airportUnmappedNode.setAttribute("localX", Integer.toString(airport.getLocalX()));
				airportUnmappedNode.setAttribute("localY", Integer.toString(airport.getLocalY()));
				airportUnmappedNode.setAttribute("remoteX", Integer.toString(airport.getRemoteX()));
				airportUnmappedNode.setAttribute("remoteY", Integer.toString(airport.getRemoteY()));
				
				outputNode.appendChild(airportUnmappedNode);
			}
			
			
			terminalsByName.remove(name);
		}
		
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Processes a saveMap command. Saves the graphical map to a given file.
	 * 
	 * @param node
	 *            saveMap command to be processed
	 * @throws IOException
	 *             problem accessing the image file
	 */
	public void processSaveMap(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		final String name = processStringAttribute(node, "name", parametersNode);

		PMQuadtree localPMToUse = null;
		for (Metropole metropole : remotetoLocalMap.keySet()) {
			if (metropole.getX() == remoteX &&
					metropole.getY() == remoteY) {
				localPMToUse = remotetoLocalMap.get(metropole);
			}
		}
		
		if (remoteX < 0 || remoteY < 0
				|| remoteX >= Command.remoteSpatialWidth || remoteY >= Command.remoteSpatialHeight) {
			addErrorNode("metropoleOutOfBounds", commandNode, parametersNode);
			return;
		} else if (localPMToUse == null || localPMToUse.isEmpty()) {
			/* empty PR Quadtree */
			addErrorNode("metropoleIsEmpty", commandNode, parametersNode);
			return;
		} 
		
		final Element outputNode = results.createElement("output");

		CanvasPlus canvas = drawPMQuadtree();

		/* save canvas to '(name).png' */
		canvas.save(name);

		canvas.dispose();

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	private CanvasPlus drawPMQuadtree() {
		final CanvasPlus canvas = new CanvasPlus("MeeshQuest");

		/* initialize canvas */
		canvas.setFrameSize(localSpatialWidth, localSpatialHeight);

		/* add a rectangle to show where the bounds of the map are located */
		canvas.addRectangle(0, 0, localSpatialWidth, localSpatialHeight, Color.BLACK,
				false);

		/* draw PM Quadtree */
		//drawPMQuadtreeHelper(pmQuadtree.getRoot(), canvas);

		return canvas;
	}

	/**
	 * Prints out the structure of the PM Quadtree in an XML format.
	 * 
	 * @param node
	 *            printPMQuadtree command to be processed
	 */

	public void processPrintPMQuadtree(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		
		final Element outputNode = results.createElement("output");

		PMQuadtree localPMToUse = null;
		for (Metropole metropole : remotetoLocalMap.keySet()) {
			if (metropole.getX() == remoteX &&
					metropole.getY() == remoteY) {
				localPMToUse = remotetoLocalMap.get(metropole);
			}
		}
		
		if (remoteX < 0 || remoteY < 0
				|| remoteX > Command.remoteSpatialWidth || remoteY > Command.remoteSpatialHeight) {
			addErrorNode("metropoleOutOfBounds", commandNode, parametersNode);
			return;
		} else if (localPMToUse == null || localPMToUse.isEmpty()) {
			/* empty PR Quadtree */
			addErrorNode("metropoleIsEmpty", commandNode, parametersNode);
			return;
		} else {
			/* print PR Quadtree */
			final Element quadtreeNode = results.createElement("quadtree");
			quadtreeNode.setAttribute("order", Integer.toString(pmOrder));
			printPMQuadtreeHelper(localPMToUse.getRoot(), quadtreeNode);

			outputNode.appendChild(quadtreeNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Traverses each node of the PR Quadtree.
	 * 
	 * @param currentNode
	 *            PR Quadtree node being printed
	 * @param xmlNode
	 *            XML node representing the current PR Quadtree node
	 */

	private void printPMQuadtreeHelper(final Node currentNode,
			final Element xmlNode) {
		if (currentNode.getType() == Node.WHITE) {
			Element white = results.createElement("white");
			xmlNode.appendChild(white);
		} else if (currentNode.getType() == Node.BLACK) {
			Black currentLeaf = (Black) currentNode;
			Element blackNode = results.createElement("black");
			blackNode.setAttribute("cardinality",
					Integer.toString(currentLeaf.getGeometry().size()));
			for (Geometry g : currentLeaf.getGeometry()) {
				if (g.isCity()) {
					City c = (City) g;
					Element city = results.createElement("city");
					city.setAttribute("name", c.getName());
					city.setAttribute("localX", Integer.toString((int) c.getLocalX()));
					city.setAttribute("localY", Integer.toString((int) c.getLocalY()));
					city.setAttribute("radius", Integer.toString((int) c.getRadius()));
					city.setAttribute("color", c.getColor());
					city.setAttribute("remoteX", Integer.toString((int) c.getRemoteX()));
					city.setAttribute("remoteY", Integer.toString((int) c.getRemoteY()));
					blackNode.appendChild(city);
				} else if (g.isAirport()) {
					Airport a = (Airport) g;
					Element airport = results.createElement("airport");
					airport.setAttribute("localX", Integer.toString((int) a.getLocalX()));
					airport.setAttribute("localY", Integer.toString((int) a.getLocalY()));
					airport.setAttribute("name", a.getName());
					airport.setAttribute("remoteX", Integer.toString((int) a.getRemoteX()));
					airport.setAttribute("remoteY", Integer.toString((int) a.getRemoteY()));
					blackNode.appendChild(airport);
				} else if (g.isTerminal()) {
					Terminal t = (Terminal) g;
					Element terminal = results.createElement("terminal");
					terminal.setAttribute("airportName", t.getAirportName());
					terminal.setAttribute("cityName", t.getEnd().getName());
					terminal.setAttribute("localX", Integer.toString((int) t.getLocalX()));
					terminal.setAttribute("localY", Integer.toString((int) t.getLocalY()));
					terminal.setAttribute("name", t.getTerminalName());
					terminal.setAttribute("remoteX", Integer.toString((int) t.getRemoteX()));
					terminal.setAttribute("remoteY", Integer.toString((int) t.getRemoteY()));
					blackNode.appendChild(terminal);
				} else if (g.isRoad()) {
					City c1 = ((Road) g).getStart();
					City c2 = ((Road) g).getEnd();
					Element road = results.createElement("road");
					road.setAttribute("start", c1 != null ? c1.getName() : ((Road) g).getStartTerminal().getTerminalName());
					road.setAttribute("end", c2 != null ? c2.getName() : ((Road) g).getEndTerminal().getTerminalName());
					blackNode.appendChild(road);
				}
			}
			xmlNode.appendChild(blackNode);
		} else {
			final Gray currentInternal = (Gray) currentNode;
			final Element gray = results.createElement("gray");
			gray.setAttribute("x",
					Integer.toString((int) currentInternal.getCenterX()));
			gray.setAttribute("y",
					Integer.toString((int) currentInternal.getCenterY()));
			for (int i = 0; i < 4; i++) {
				printPMQuadtreeHelper(currentInternal.getChild(i), gray);
			}
			xmlNode.appendChild(gray);
		}
	}

	/**
	 * Finds the mapped cities within the range of a given point.
	 * 
	 * @param node
	 *            rangeCities command to be processed
	 * @throws IOException
	 */
	public void processGlobalRangeCities(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);
		
		final TreeSet<Geometry> citiesInRange = new TreeSet<Geometry>();
		
		PMQuadtree localPMToUse = null;
		for (Metropole metropole : remotetoLocalMap.keySet()) {
			localPMToUse = remotetoLocalMap.get(metropole);
			rangeHelper(new Circle2D.Double(remoteX, remoteY, radius), localPMToUse.getRoot(),
					citiesInRange, false, true);
		}

		/* print out cities within range */
		if (citiesInRange.isEmpty()) {
			addErrorNode("noCitiesExistInRange", commandNode, parametersNode);
		} else {
			/* get city list */
			final Element cityListNode = results.createElement("cityList");
			for (Geometry g : citiesInRange) {
				addCityNode(cityListNode, (City) g);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}
	
	private void rangeHelper(final Circle2D.Double range, final Node node,
			final TreeSet<Geometry> gInRange, final boolean includeRoads,
			final boolean includeCities) {
		if (node.getType() == Node.BLACK) {
			final Black leaf = (Black) node;
			for (Geometry g : leaf.getGeometry()) {
				if (includeCities
						&& g.isCity()
						&& !gInRange.contains(g)
						&& Inclusive2DIntersectionVerifier.intersects(
								new Point2D.Float(((City) g).getRemoteX(), ((City) g).getRemoteY()), range)) {
					gInRange.add(g);
				}
				if (includeRoads
						&& g.isRoad()
						&& !gInRange.contains(g)
						&& (((Road) g).toLine2D().ptSegDist(range.getCenter()) <= range
								.getRadius())) {
					gInRange.add(g);
				}
			}
		} else if (node.getType() == Node.GRAY) {
			final Gray internal = (Gray) node;
			for (int i = 0; i < 4; i++) {
				rangeHelper(range, internal.getChild(i), gInRange,
							includeRoads, includeCities);
			}
		}
	}

	public void processNearestCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int localX = processIntegerAttribute(node, "localX", parametersNode);
		final int localY = processIntegerAttribute(node, "localY", parametersNode);
		final int remoteX = processIntegerAttribute(node, "remoteX", parametersNode);
		final int remoteY = processIntegerAttribute(node, "remoteY", parametersNode);
		
		final Point2D.Float point = new Point2D.Float(localX, localY);

		PMQuadtree localPMToUse = null;
		for (Metropole metropole : remotetoLocalMap.keySet()) {
			if (metropole.getX() == remoteX &&
					metropole.getY() == remoteY) {
				localPMToUse = remotetoLocalMap.get(metropole);
			}
		}
		
		if (localPMToUse == null || (localPMToUse.getNumCities() - localPMToUse.getNumIsolatedCities() == 0)) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
		} else {
			addCityNode(outputNode, nearestCityHelper(point, false, localPMToUse));
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	private City nearestCityHelper(Point2D.Float point,
			boolean isNearestIsolatedCity, PMQuadtree pmQuadtree) {
		Node n = pmQuadtree.getRoot();
		PriorityQueue<NearestSearchRegion> nearCities = new PriorityQueue<NearestSearchRegion>();

		if (n.getType() == Node.BLACK) {
			Black b = (Black) n;
			
			if (b.getCity() != null) {
				return b.getCity();
			}
		}

		while (n.getType() == Node.GRAY) {
			Gray g = (Gray) n;
			Node kid;
			
			for (int i = 0; i < 4; i++) {
				kid = g.getChild(i);
				
				if (kid.getType() == Node.BLACK) {
					Black b = (Black) kid;
					City c = b.getCity();
					
					if (c != null) {
						double dist = point.distance(c.toPoint2D());
						nearCities.add(new NearestSearchRegion(kid, dist, c));
					}
				} else if (kid.getType() == Node.GRAY) {
					double dist = Shape2DDistanceCalculator.distance(point,
							g.getChildRegion(i));
					nearCities.add(new NearestSearchRegion(kid, dist, null));
				}
			}
			
			try {
				n = nearCities.remove().node;
			} catch (Exception ex) {
				throw new IllegalStateException();
			}
		}
		return ((Black) n).getCity();
	}

	/**
	 * Helper class for nearest everything (city/road/etc)
	 */
	private class NearestSearchRegion implements
			Comparable<NearestSearchRegion> {
		private Node node;
		private double distance;
		private Geometry g;

		public NearestSearchRegion(Node node, double distance, Geometry g) {
			this.node = node;
			this.distance = distance;
			this.g = g;
		}

		public int compareTo(NearestSearchRegion o) {
			if (distance == o.distance) {
				if (node.getType() == Node.BLACK
						&& o.node.getType() == Node.BLACK) {
					return g.compareTo(o.g);
				} else if (node.getType() == Node.BLACK
						&& o.node.getType() == Node.GRAY) {
					return 1;
				} else if (node.getType() == Node.GRAY
						&& o.node.getType() == Node.BLACK) {
					return -1;
				} else {
					return ((Gray) node).hashCode()
							- ((Gray) o.node).hashCode();
				}
			}
			return (distance < o.distance) ? -1 : 1;
		}
	}

}
