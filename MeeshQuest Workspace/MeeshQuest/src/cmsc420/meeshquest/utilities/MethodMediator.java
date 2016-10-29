package cmsc420.meeshquest.utilities;

import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmsc420.geom.Circle2D;
import cmsc420.geom.Geometry2D;
import cmsc420.geom.Shape2DDistanceCalculator;
import cmsc420.meeshquest.citymapobjects.City;
import cmsc420.meeshquest.citymapobjects.CityLocationComparator;
import cmsc420.meeshquest.citymapobjects.CityNameComparator;
import cmsc420.meeshquest.citymapobjects.Line;
import cmsc420.meeshquest.citymapobjects.Point;
import cmsc420.meeshquest.datastructures.BlackNode;
import cmsc420.meeshquest.datastructures.Graph;
import cmsc420.meeshquest.datastructures.GreyNode;
import cmsc420.meeshquest.datastructures.GuardedAvlGTree;
import cmsc420.meeshquest.datastructures.MXQuadtree;
import cmsc420.meeshquest.datastructures.Node;
import cmsc420.meeshquest.datastructures.PMQuadtree;
import cmsc420.meeshquest.exception.CityOutOfBoundsException;
import cmsc420.meeshquest.exception.RoadOutOfBoundsException;

/**
 * @(#)Command.java        1.1 
 * 
 * 2014/09/09
 *
 * @author Ruofei Du, Ben Zoller (University of Maryland, College Park), 2014
 * 
 * All rights reserved. Permission is granted for use and modification in CMSC420 
 * at the University of Maryland.
 */

/**
 * Processes each command in the MeeshQuest program. Takes in an XML command
 * node, processes the node, and outputs the results.
 * 
 * @author Ben Zoller
 * @version 2.0, 23 Jan 2007
 */
public class MethodMediator {
	/** output DOM Document tree */
	protected Document results;

	/** root node of results document */
	protected Element resultsNode;

	/**
	 * stores created cities sorted by their names (used with listCities command)
	 */
	protected final TreeMap<String, City> citiesByName = new TreeMap<String, City>(new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return o2.compareTo(o1);
		}

	});

	/**
	 * stores created cities sorted by their locations (used with listCities command)
	 */
	protected final TreeSet<City> citiesByLocation = new TreeSet<City>(
			new CityLocationComparator());

	/**
	 * stores all cities ever mapped in an Avl Tree sorted by their name (new cities with same name overwrite old ones)
	 */
	protected GuardedAvlGTree<City, Integer> allMappedCitiesByName;

	/** stores mapped cities in a spatial data structure */
	protected final MXQuadtree mxQuadtree = new MXQuadtree();
	
	/** stores mapped cities in a spatial data structure */
	protected final PMQuadtree pmQuadtree = new PMQuadtree();
	
	/** stores all cities in a Graph structure */
	protected final Graph<City> cityGraph = new Graph<City>();

	/* Mikhail Add - g and pmOrder*/
	protected int g, pmOrder;
	
	/** spatial width and height of the MX Quadtree */
	protected int spatialWidth, spatialHeight;

	/**
	 * Set the DOM Document tree to send the of processed commands to.
	 * 
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
		String idAttribute = node.getAttribute("id");
		if (!idAttribute.equals("")) {
			commandNode.setAttribute("id", idAttribute);
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
	private void addSuccessNode(final Element command,
			final Element parameters, final Element output) {
		final Element success = results.createElement("success");
		success.appendChild(command);
		success.appendChild(parameters);
		success.appendChild(output);
		resultsNode.appendChild(success);
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
		spatialWidth = Integer.parseInt(node.getAttribute("spatialWidth"));
		spatialHeight = Integer.parseInt(node.getAttribute("spatialHeight"));
		g = Integer.parseInt(node.getAttribute("g"));
		allMappedCitiesByName = new GuardedAvlGTree<City, Integer>(new Comparator<City>() {

			@Override
			public int compare(City o1, City o2) {
					return o2.getName().compareTo(o1.getName());
				}
		}, g);
		pmOrder = Integer.parseInt(node.getAttribute("pmOrder"));
		
		/* set MX Quadtree range */
		mxQuadtree.setRange(spatialWidth, spatialHeight);
		pmQuadtree.setRange(spatialWidth, spatialHeight);
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
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);
		final String color = processStringAttribute(node, "color",
				parametersNode);

		/* create the city */
		final City city = new City(name, x, y, radius, color);

		if (citiesByLocation.contains(city)) {
			addErrorNode("duplicateCityCoordinates", commandNode,
					parametersNode);
		} else if (citiesByName.containsKey(name)) {
			addErrorNode("duplicateCityName", commandNode, parametersNode);
		} else {
			final Element outputNode = results.createElement("output");

			/* add city to dictionary */
			citiesByName.put(name, city);
			citiesByLocation.add(city);
			allMappedCitiesByName.put(city, city.getRadius());

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Processes a deleteCity command. Deletes a city from the dictionary. An
	 * error occurs if the city does not exist or is currently mapped.
	 * 
	 * @param node
	 *            deleteCity node being processed
	 */
	public void processDeleteCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final String name = processStringAttribute(node, "name", parametersNode);

		if (!citiesByName.containsKey(name)) {
			/* city with name does not exist */
			addErrorNode("cityDoesNotExist", commandNode, parametersNode);
		} else {
			/* delete city */
			final Element outputNode = results.createElement("output");
			final City deletedCity = citiesByName.get(name);

			if (mxQuadtree.contains(name)) {
				/* city is mapped */
				mxQuadtree.remove(deletedCity);
				addCityNode(outputNode, "cityUnmapped", deletedCity);
			}

			citiesByName.remove(name);
			citiesByLocation.remove(deletedCity);

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
		allMappedCitiesByName.clear();
		mxQuadtree.clear();
		pmQuadtree.clear();

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
		cityNode.setAttribute("x", Integer.toString((int) city.getX()));
		cityNode.setAttribute("y", Integer.toString((int) city.getY()));
		cityNode.setAttribute("radius", Integer
				.toString((int) city.getRadius()));
		cityNode.setAttribute("color", city.getColor());
		node.appendChild(cityNode);
	}

	/**
	 * Creates a city node containing information about a city. Appends the city
	 * node to the passed in node.
	 * 
	 * @param node
	 *            node which the city node will be appended to
	 * @param city
	 *            city which the city node will describe
	 */
	private void addCityNode(final Element node, final City city) {
		addCityNode(node, "city", city);
	}
	
	private void addRoadCreatedNode(final Element node, final String roadNodeName,
			final City startCity, final City endCity) {
		final Element cityNode = results.createElement(roadNodeName);
		cityNode.setAttribute("end", endCity.getName());
		cityNode.setAttribute("start", startCity.getName());
		node.appendChild(cityNode);
	}
	
	private void addRoadCreatedNode(final Element node, final City startCity, final City endCity) {
		addRoadCreatedNode(node, "roadCreated", startCity, endCity);
	}

	/**
	 * Maps a city to the spatial map. For part two, maps an isolated city that is not an 
	 * endpoint of any roads, i.e., the city is not connected to any roads.
	 * 
	 * @param node
	 *            mapCity command node to be processed
	 */
	private ArrayList<City> isolatedCities = new ArrayList<City>();
	
	public void processMapCity(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(name)) {
			addErrorNode("nameNotInDictionary", commandNode, parametersNode);
		} else if (isolatedCities.contains(citiesByName.get(name)) || cityGraph.isVertex(citiesByName.get(name))) {
			addErrorNode("cityAlreadyMapped", commandNode, parametersNode);
		} else {
			City city = citiesByName.get(name);
			try {
				/* insert city into MX Quadtree and PM Quadtree */
				//mxQuadtree.add(city);
				pmQuadtree.add(city);
				allMappedCitiesByName.put(city, city.getRadius());
				isolatedCities.add(city);
				cityGraph.addVertex(city);
				
				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			} catch (CityOutOfBoundsException e) {
				addErrorNode("cityOutOfBounds", commandNode, parametersNode);
			}
		}
	}

	/**
	 * Removes a city from the spatial map.
	 * 
	 * @param node
	 *            unmapCity command node to be processed
	 */
	public void processUnmapCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		if (!citiesByName.containsKey(name)) {
			addErrorNode("nameNotInDictionary", commandNode, parametersNode);
		} else if (!mxQuadtree.contains(name)) {
			addErrorNode("cityNotMapped", commandNode, parametersNode);
		} else {
			City city = citiesByName.get(name);

			/* unmap the city in the MX Quadtree */
			mxQuadtree.remove(city);
			
			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
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

		final String name = processStringAttribute(node, "name", parametersNode);

		final Element outputNode = results.createElement("output");

		/* add success node to results */
		addSuccessNode(commandNode, parametersNode, outputNode);
	}

	/**
	 * Prints out the structure of the MX Quadtree in a human-readable format.
	 * 
	 * @param node
	 *            printMXQuadtree command to be processed
	 */
	public void processPrintMXQuadtree(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		if (mxQuadtree.isEmpty()) {
			/* empty MX Quadtree */
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {
			/* print MX Quadtree */
			final Element quadtreeNode = results.createElement("quadtree");
			printMXQuadtreeHelper(mxQuadtree.getRoot(), quadtreeNode);

			outputNode.appendChild(quadtreeNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}
	
	public void processPrintPMQuadtree(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		if (pmQuadtree.isEmpty()) {
			/* empty PM Quadtree */
			addErrorNode("mapIsEmpty", commandNode, parametersNode);
		} else {
			/* print PM Quadtree */
			final Element quadtreeNode = results.createElement("quadtree");
			quadtreeNode.setAttribute("order", Integer.toString(pmOrder));
			printPMQuadtreeHelper(pmQuadtree.getRoot(), quadtreeNode);

			outputNode.appendChild(quadtreeNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	public void processPrintAvlTree(final Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		if (allMappedCitiesByName.isEmpty()) {
			addErrorNode("emptyTree", commandNode, parametersNode);
		} else {
			outputNode.appendChild(allMappedCitiesByName.createXml(outputNode));
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Traverses each node of the MX Quadtree.
	 * 
	 * @param currentNode
	 *            MX Quadtree node being printed
	 * @param xmlNode
	 *            XML node representing the current MX Quadtree node
	 */
	private void printMXQuadtreeHelper(final Node currentNode,
			final Element xmlNode) {
		if (currentNode.getType() == Node.EMPTY) {
			Element white = results.createElement("white");
			xmlNode.appendChild(white);
		} else {
			if (currentNode.getType() == Node.LEAF) {
				/* leaf node */
				final BlackNode blackNode = (BlackNode) currentNode;
				final Element black = results.createElement("black");
				black.setAttribute("name", blackNode.getStartVertex().getName());
				black.setAttribute("x", Integer.toString((int) blackNode
						.getStartVertex().getX()));
				black.setAttribute("y", Integer.toString((int) blackNode
						.getStartVertex().getY()));
				xmlNode.appendChild(black);
			} else {
				/* internal node */
				final GreyNode currentInternal = (GreyNode) currentNode;
				final Element gray = results.createElement("gray");
				gray.setAttribute("x", Integer.toString((int) currentInternal
						.getCenterX()));
				gray.setAttribute("y", Integer.toString((int) currentInternal
						.getCenterY()));
				for (int i = 0; i < 4; i++) {
					printMXQuadtreeHelper(currentInternal.getChild(i), gray);
				}
				xmlNode.appendChild(gray);
			}
		}
	}
	

	public static Comparator<Geometry2D> getCompByName() {
		Comparator<Geometry2D> comp = new Comparator<Geometry2D>() {
			@Override
			public int compare(Geometry2D s1, Geometry2D s2) {
				if (s1.getType() == Geometry2D.POINT) {
					return -1;
				} else if (s2.getType() == Geometry2D.POINT) {
					return 1;
				} else {
					return 0;
				}
			}
		};
		return comp;
	}
	
	public static Comparator<? super Geometry2D> getCompByRoad() {
		Comparator<Geometry2D> comp = new Comparator<Geometry2D>() {
			@Override
			public int compare(Geometry2D s1, Geometry2D s2) {
				if (s1.getType() == Geometry2D.SEGMENT && s2.getType() == Geometry2D.SEGMENT) {
					return ((Line)s2).getStartCity().getName().compareTo(
						((Line)s1).getStartCity().getName());
				} else {
					return 0;
				}
			}
		};
		return comp;
	}
	
	/**
	 * Traverses each node of the PM Quadtree.
	 * 
	 * @param currentNode
	 *            PM Quadtree node being printed
	 * @param xmlNode
	 *            XML node representing the current PM Quadtree node
	 */
	private void printPMQuadtreeHelper(final Node currentNode,
			final Element xmlNode) {
		if (currentNode.getType() == Node.EMPTY) {
			Element white = results.createElement("white");
			xmlNode.appendChild(white);
		} else {
			if (currentNode.getType() == Node.LEAF) {
				/* leaf node */
				final BlackNode blackNode = (BlackNode) currentNode;
				final Element black = results.createElement("black");
				black.setAttribute("cardinality", Integer.toString(blackNode.getCardinality()));

				for (Geometry2D g : blackNode.getAllList()) {
					if (g.getType() == Geometry2D.SEGMENT) {
						Line road = ((Line)g);
						if (road.getStartCity().getName().toLowerCase().compareTo
								(road.getEndCity().getName().toLowerCase()) > 0) {
							City startCity = road.getStartCity();
							City endCity = road.getEndCity();
							road.setStartCity(endCity);
							road.setEndCity(startCity);
						}
					}
				}
				
				blackNode.getAllList().sort(MethodMediator.getCompByName());
				blackNode.getAllList().sort(MethodMediator.getCompByRoad());
				
				for (Geometry2D g : blackNode.getAllList()) {
					if (g.getType() == Geometry2D.POINT) {
						Point singlePoint = ((Point)g);
						if (!singlePoint.isolatedString().equals("")) 
							addCityNode(black, "isolatedCity", singlePoint.getCity());
						else addCityNode(black, singlePoint.getCity());
					} else if (g.getType() == Geometry2D.SEGMENT) {
						Line road = ((Line)g);
						addRoadCreatedNode(black, "road", road.getStartCity(), road.getEndCity());
					}
				}
				
				xmlNode.appendChild(black);
			} else {
				/* internal node */
				final GreyNode currentInternal = (GreyNode) currentNode;
				final Element gray = results.createElement("gray");
				gray.setAttribute("x", Integer.toString((int) currentInternal
						.getCenterX()));
				gray.setAttribute("y", Integer.toString((int) currentInternal
						.getCenterY()));
				for (int i = 0; i < 4; i++) {
					printPMQuadtreeHelper(currentInternal.getChild(i), gray);
				}
				xmlNode.appendChild(gray);
			}
		}
	}

	/**
	 * Finds the mapped cities within the range of a given point.
	 * 
	 * @param node
	 *            rangeCities command to be processed
	 * @throws IOException
	 */
	public void processRangeCities(final Element node) throws IOException {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final TreeSet<City> citiesInRange = new TreeSet<City>(
				new CityNameComparator());

		/* extract values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);

		String pathFile = "";
		if (node.getAttribute("saveMap").compareTo("") != 0) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}
		/* get cities within range */
		final Point2D.Double point = new Point2D.Double(x, y);
		rangeCitiesHelper(point, radius, pmQuadtree.getRoot(), citiesInRange);

		/* print out cities within range */
		if (citiesInRange.isEmpty()) {
			addErrorNode("noCitiesExistInRange", commandNode, parametersNode);
		} else {
			/* get city list */
			final Element cityListNode = results.createElement("cityList");
			for (City city : citiesInRange) {
				addCityNode(cityListNode, city);
			}
			outputNode.appendChild(cityListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	/**
	 * Determines if any cities within the MX Quadtree not are within the radius
	 * of a given point.
	 * 
	 * @param point
	 *            point from which the cities are measured
	 * @param radius
	 *            radius from which the given points are measured
	 * @param node
	 *            MX Quadtree node being examined
	 * @param citiesInRange
	 *            a list of cities found to be in range
	 */
	private void rangeCitiesHelper(final Point2D.Double point,
			final int radius, final Node node, final TreeSet<City> citiesInRange) {
		if (node.getType() == Node.LEAF) {
			final BlackNode leaf = (BlackNode) node;
			//FOR PM Quadtree, it should work like this
			
			for (Geometry2D g : leaf.getAllList()) {
				if (g.getType() == Geometry2D.POINT) {
					final double distance = point.distance(((Point) g).getPoint());
					
					if (distance <= radius) {
						/* city is in range */
						final City city = ((Point) g).getCity();
						citiesInRange.add(city);
					}
				}
			}
		} else if (node.getType() == Node.INTERNAL) {
			/* check each quadrant of internal node */
			final GreyNode internal = (GreyNode) node;

			final Circle2D.Double circle = new Circle2D.Double(point, radius);
			for (int i = 0; i < 4; i++) {
				if (pmQuadtree.intersects(circle, internal.getChildRegion(i))) {
					rangeCitiesHelper(point, radius, internal.getChild(i),
							citiesInRange);
				}
			}
		}
	}

	/**
	 * Finds the nearest city to a given point.
	 * 
	 * @param node
	 *            nearestCity command being processed
	 */
	public void processNearestCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);

		if (pmQuadtree.getRoot().getType() == Node.EMPTY) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
		} else {
			City n = pmQuadtree.findClosestPoint(x, y, false);
			if (n == null) {
				addErrorNode("cityNotFound", commandNode, parametersNode);
			} else {
				addCityNode(outputNode, n);
	
				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
		}
	}
	

	public void processNearestIsolatedCity(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);

		if (pmQuadtree.getRoot().getType() == Node.EMPTY) {
			addErrorNode("cityNotFound", commandNode, parametersNode);
		} else {
			City n = pmQuadtree.findClosestPoint(x, y, true);
			if (n == null) {
				addErrorNode("cityNotFound", commandNode, parametersNode);
			} else {
				addCityNode(outputNode, "isolatedCity", n);
	
				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
		}
	}

	class QuadrantDistance implements Comparable<QuadrantDistance> {
		public Node quadtreeNode;
		private double distance;

		public QuadrantDistance(Node node, Point2D.Float pt) {
			quadtreeNode = node;
			if (node.getType() == Node.INTERNAL) {
				GreyNode gray = (GreyNode) node;
				distance = Shape2DDistanceCalculator.distance(pt, 
						new Rectangle2D.Float(gray.origin.x, gray.origin.y, gray.width, gray.height));
			} else if (node.getType() == Node.LEAF) {
				BlackNode leaf = (BlackNode) node;

				double shortestDistance = Integer.MAX_VALUE;
				for (Geometry2D g : leaf.getAllList()) {
					if (g.getType() == Geometry2D.POINT) {
						if (distance < shortestDistance) {
							shortestDistance = pt.distance(((Point) g).getCity().pt);
						}
					} else if (g.getType() == Geometry2D.POINT) {
						if (distance < shortestDistance) {
							shortestDistance = pt.distance(((Point) g).getCity().pt);
						}
					}
				}
			} else {
				throw new IllegalArgumentException("Only leaf or internal node can be passed in");
			}
		}

		public int compareTo(QuadrantDistance qd) {
			if (distance < qd.distance) {
				return -1;
			} else if (distance > qd.distance) {
				return 1;
			} else {
				if (quadtreeNode.getType() != qd.quadtreeNode.getType()) {
					if (quadtreeNode.getType() == Node.INTERNAL) {
						return -1;
					} else {
						return 1;
					}
				} else if (quadtreeNode.getType() == Node.LEAF) {
					// both are leaves
					City qdCity = null;
					for (Geometry2D g : ((BlackNode) qd.quadtreeNode).getAllList()) {
						if (g.getType() == Geometry2D.POINT) {
							qdCity = (((Point) g).getCity());
						}
					}
					
					City thisCity = null;
					for (Geometry2D g : ((BlackNode) quadtreeNode).getAllList()) {
						if (g.getType() == Geometry2D.POINT) {
							thisCity = (((Point) g).getCity());
						}
					}
					return (qdCity.getName().compareTo(
							thisCity.getName()));
				} else {
					// both are internals
					return 0;
				}
			}
		}
	}

	public void processMapRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final String startCityName = processStringAttribute(node, "start", parametersNode);
		final String endCityName = processStringAttribute(node, "end", parametersNode);
		
		// TODO: Need case for when city has already been mapped
		if (citiesByName.get(startCityName) == null) {
			addErrorNode("startPointDoesNotExist", commandNode, parametersNode);
		} else if (citiesByName.get(endCityName) == null) {
			addErrorNode("endPointDoesNotExist", commandNode, parametersNode);
		} else if (startCityName.equals(endCityName)) {
			addErrorNode("startEqualsEnd", commandNode, parametersNode);
		} else if (cityGraph.containsEdge(citiesByName.get(startCityName), citiesByName.get(endCityName), 0)) {
			addErrorNode("roadAlreadyMapped", commandNode, parametersNode);	
		} else if (isolatedCities.contains(citiesByName.get(startCityName))
				|| isolatedCities.contains(citiesByName.get(endCityName))) {
			addErrorNode("startOrEndIsIsolated", commandNode, parametersNode);	
		} else {
			//FIXED: Rename to addRoadCreatedNode
			try {
				pmQuadtree.add(citiesByName.get(startCityName), citiesByName.get(endCityName));
				cityGraph.addEdge(citiesByName.get(startCityName), citiesByName.get(endCityName), 0);
				cityGraph.addEdge(citiesByName.get(endCityName), citiesByName.get(startCityName), 0);
				
				addRoadCreatedNode(outputNode, citiesByName.get(startCityName), citiesByName.get(endCityName));
				
				addSuccessNode(commandNode, parametersNode, outputNode);
			} catch (RoadOutOfBoundsException e) {
				addErrorNode("roadOutOfBounds", commandNode, parametersNode);
			} 
		}
	}

	public void processNearestRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);

		if (pmQuadtree.getRoot().getType() == Node.EMPTY) {
			addErrorNode("roadNotFound", commandNode, parametersNode);
		} else {
			City[] n = pmQuadtree.findClosestRoad(x, y);
			//System.out.println(n[0].getName());
			if (n.length == 0) {
				addErrorNode("roadNotFound", commandNode, parametersNode);
			} else {
		
				addRoadCreatedNode(outputNode, "road", n[0], n[1]);
	
				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
		}
	}

	public void processNearestCityToRoad(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		/* extract attribute values from command */
		final String startCityName = processStringAttribute(node, "start", parametersNode);
		final String endCityName = processStringAttribute(node, "end", parametersNode);

		if (pmQuadtree.getRoot().getType() == Node.EMPTY &&
				(!cityGraph.containsEdge(citiesByName.get(startCityName), citiesByName.get(endCityName), 0))) {
			//FIXED: Need to do a test to see if the road is contained in the graph
			addErrorNode("roadIsNotMapped", commandNode, parametersNode);
		} else {
			City n = pmQuadtree.findClosestRoad(citiesByName.get(startCityName), citiesByName.get(endCityName));
			if (n == null) {
				addErrorNode("noOtherCitiesMapped", commandNode, parametersNode);
			} else {
				addCityNode(outputNode, "city", n);
	
				/* add success node to results */
				addSuccessNode(commandNode, parametersNode, outputNode);
			}
		}
	}
	
	public static Comparator<City[]> getCompByCitieNames() {
		CityNameComparator cnc = new CityNameComparator();
		Comparator<City[]> comp = new Comparator<City[]>() {
			@Override
			public int compare(City[] road1, City[] road2) {
				if (cnc.compare(road1[0], road2[0]) < 0) {
					return -1;
				} else if (cnc.compare(road1[0], road2[0]) > 0) {
					return 1;
				} else {
				//When all the starts are equal, do the end road comparison
					if (cnc.compare(road1[1], road2[1]) < 0) {
						return -1;
					} else if (cnc.compare(road1[1], road2[1]) > 0) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		};
		return comp;
	}

	public void processRangeRoads(Element node) {
		final Element commandNode = getCommandNode(node);
		final Element parametersNode = results.createElement("parameters");
		final Element outputNode = results.createElement("output");

		final TreeSet<City[]> roadsInRange = new TreeSet<City[]>(MethodMediator.getCompByCitieNames());

		/* extract values from command */
		final int x = processIntegerAttribute(node, "x", parametersNode);
		final int y = processIntegerAttribute(node, "y", parametersNode);
		final int radius = processIntegerAttribute(node, "radius",
				parametersNode);

		String pathFile = "";
		if (node.getAttribute("saveMap").compareTo("") != 0) {
			pathFile = processStringAttribute(node, "saveMap", parametersNode);
		}
		/* get roads within range */
		final Point2D.Double point = new Point2D.Double(x, y);
		rangeRoadsHelper(point, radius, pmQuadtree.getRoot(), roadsInRange);

		/* print out cities within range */
		if (roadsInRange.isEmpty()) {
			addErrorNode("noRoadsExistInRange", commandNode, parametersNode);
		} else {
			/* get city list */
			final Element roadListNode = results.createElement("roadList");
			for (City[] city : roadsInRange) {
				addRoadCreatedNode(roadListNode, "road", city[0], city[1]);
			}
			outputNode.appendChild(roadListNode);

			/* add success node to results */
			addSuccessNode(commandNode, parametersNode, outputNode);
		}
	}

	private void rangeRoadsHelper(Double point, int radius, Node node, TreeSet<City[]> roadsInRange) {
		if (node.getType() == Node.LEAF) {
			final BlackNode leaf = (BlackNode) node;
			//FOR PM Quadtree, it should work like this
			
			for (Geometry2D g : leaf.getAllList()) {
				if (g.getType() == Geometry2D.SEGMENT) {
					Line road = (Line)g;
					//TODO: Need to calculate roads that are tangent to the circle
					final double distance = road.getLine().ptLineDist(point);
					
					if (distance <= radius) {
						/* city is in range */
						City startCity = road.getStartCity();
						City endCity = road.getEndCity();
						City temp = null;
						
						CityNameComparator nameComp = new CityNameComparator();
						if (nameComp.compare(startCity, endCity) < 0) {
							temp = endCity;
							endCity = startCity;
							startCity = temp;
						} 
						
						City[] rangeRoad = new City[2];
						rangeRoad[0] = startCity;
						rangeRoad[1] = endCity;

						roadsInRange.add(rangeRoad);
					}
				}
			}
		} else if (node.getType() == Node.INTERNAL) {
			/* check each quadrant of internal node */
			final GreyNode internal = (GreyNode) node;

			final Circle2D.Double circle = new Circle2D.Double(point, radius);
			for (int i = 0; i < 4; i++) {
				if (pmQuadtree.intersects(circle, internal.getChildRegion(i))) {
					rangeRoadsHelper(point, radius, internal.getChild(i), roadsInRange);
				}
			}
		}
	}

	//	/**
	//	 * Examines the distance from each city in a MX Quadtree node from the given
	//	 * point.
	//	 * 
	//	 * @param node
	//	 *            MX Quadtree node being examined
	//	 * @param point
	//	 *            point
	//	 * @param nearCities
	//	 *            priority queue of cities organized by how close they are to
	//	 *            the point
	//	 */
	//	private void nearestCityHelper(Node node, Point2D.Float point,
	//			PriorityQueue<NearestCity> nearCities) {
	//		if (node.getType() == Node.LEAF) {
	//			LeafNode leaf = (LeafNode) node;
	//			NearestCity nearCity = new NearestCity(leaf.getCity(), point
	//					.distance(leaf.getCity().toPoint2D()));
	//			if (nearCity.compareTo(nearCities.peek()) < 0) {
	//				nearCities.add(nearCity);
	//			}
	//		} else if (node.getType() == Node.INTERNAL) {
	//			InternalNode internal = (InternalNode) node;
	//			TreeSet<NearestQuadrant> nearestQuadrants = new TreeSet<NearestQuadrant>();
	//			for (int i = 0; i < 4; i++) {
	//				nearestQuadrants.add(new NearestQuadrant(Shape2DDistanceCalculator.distance(point, internal
	//						.getChildRegion(i)), i));
	//			}
	//			
	//			for (NearestQuadrant nearQuadrant : nearestQuadrants) {
	//				final int i = nearQuadrant.getQuadrant(); 
	//				
	//				if (Shape2DDistanceCalculator.distance(point, internal
	//						.getChildRegion(i)) <= nearCities.peek().getDistance()) {
	//
	//					nearestCityHelper(internal.getChild(i), point, nearCities);
	//				}
	//			}
	//		}
	//	}
	//	
	//	private class NearestQuadrant implements Comparable<NearestQuadrant> {
	//
	//		private double distance;
	//		
	//		private int quadrant;
	//		
	//		public NearestQuadrant(double distance, int quadrant) {
	//			this.distance = distance;
	//			this.quadrant = quadrant;
	//		}
	//
	//		public int getQuadrant() {
	//			return quadrant;
	//		}
	//
	//		public int compareTo(NearestQuadrant o) {
	//			if (distance < o.distance) {
	//				return -1;
	//			} else if (distance > o.distance) {
	//				return 1;
	//			} else {
	//				if (quadrant < o.quadrant) {
	//					return -1;
	//				} else if (quadrant > o.quadrant) {
	//					return 1;
	//				} else {
	//					return 0;
	//				}
	//			}
	//		}
	//		
	//	}
	//
	//	/**
	//	 * Used with the nearestCity command. Each NearestCity contains a city and
	//	 * the city's distance from a give point. A NearestCity is less than another
	//	 * if it's distance is smaller than the other's.
	//	 * 
	//	 * @author Ben Zoller
	//	 * @version 1.0
	//	 */
	//	private class NearestCity implements Comparable<NearestCity> {
	//		/** city */
	//		private final City city;
	//
	//		/** city's distance to a point */
	//		private final double distance;
	//
	//		/**
	//		 * Constructs a city and it's distance from a point.
	//		 * 
	//		 * @param city
	//		 *            city
	//		 * @param distance
	//		 *            distance from a point
	//		 */
	//		private NearestCity(final City city, final double distance) {
	//			this.city = city;
	//			this.distance = distance;
	//		}
	//
	//		/**
	//		 * Gets the city
	//		 * 
	//		 * @return city
	//		 */
	//		private City getCity() {
	//			return city;
	//		}
	//
	//		/**
	//		 * Compares one city to another based on their distances.
	//		 * 
	//		 * @param otherNearCity
	//		 *            other city
	//		 * @return distance comparison results
	//		 */
	//		public int compareTo(final NearestCity otherNearCity) {
	//			if (distance < otherNearCity.distance) {
	//				return -1;
	//			} else if (distance > otherNearCity.distance) {
	//				return 1;
	//			} else {
	//				return city.getName().compareTo(otherNearCity.city.getName());
	//			}
	//		}
	//
	//		/**
	//		 * Gets the distance
	//		 * 
	//		 * @return distance
	//		 */
	//		public double getDistance() {
	//			return distance;
	//		}
	//	}
}
