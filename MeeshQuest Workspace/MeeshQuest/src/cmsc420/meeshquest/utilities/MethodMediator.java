package cmsc420.meeshquest.utilities;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Element;

import cmsc420.meeshquest.citymapobjects.City;
import cmsc420.meeshquest.datastructures.AvlGTree;
import cmsc420.meeshquest.datastructures.MXQuadtree;
import cmsc420.meeshquest.datastructures.PMQuadtree;

public class MethodMediator {

	// DataStructures
	private Map<String, City> nameToCity = new TreeMap<String, City>(new Comparator<String>() {

		@Override
		public int compare(String name1, String name2) {
			return name2.compareTo(name1);
		}

	});

	/**
	 * TODO: Make sure this implementation is correct.
	 */
	private TreeMap<Point2D.Float, City> coordinatesToCity = new TreeMap<Point2D.Float, City>(
			new Comparator<Point2D.Float>() {

				@Override
				public int compare(Float point1, Float point2) {
					int compareVal = 0;
					if (point1.getY() < point2.getY()) {
						compareVal = -1;
					} else if (point1.getY() > point2.getY()) {
						compareVal = 1;
					} else {
						if (point1.getX() < point2.getX()) {
							compareVal = -1;
						} else if (point1.getX() > point2.getX()) {
							compareVal = 1;
						} else {
							compareVal = 0;
						}
					}
					return compareVal;
				}

			});

	protected final AvlGTree<City, Integer> avlTree = new AvlGTree<City, Integer>(new Comparator<City>() {

		@Override
		public int compare(City o1, City o2) {
				return o2.getName().compareTo(o1.getName());
			}
	}, 1);
	
	private MXQuadtree mxQuadtree = new MXQuadtree();
	private PMQuadtree pmQuadtree = new PMQuadtree();

	private void CreateCityErrorOutput(String errorType, String name, int x, int y, int radius, String color) {
		Element errorElement = XmlParser.results.createElement("error");
		errorElement.setAttribute("type", errorType);
		XmlParser.currElement.appendChild(errorElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "createCity");
		errorElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		errorElement.appendChild(parametersElement);

		Element nameElement = XmlParser.results.createElement("name");
		nameElement.setAttribute("value", name);
		parametersElement.appendChild(nameElement);

		Element xCoordElement = XmlParser.results.createElement("x");
		xCoordElement.setAttribute("value", Integer.toString(x));
		parametersElement.appendChild(xCoordElement);

		Element yCoordElement = XmlParser.results.createElement("y");
		yCoordElement.setAttribute("value", Integer.toString(y));
		parametersElement.appendChild(yCoordElement);

		Element radiusElement = XmlParser.results.createElement("radius");
		radiusElement.setAttribute("value", Integer.toString(radius));
		parametersElement.appendChild(radiusElement);

		Element colorElement = XmlParser.results.createElement("color");
		colorElement.setAttribute("value", color);
		parametersElement.appendChild(colorElement);
	}

	/**
	 * Creates a city based on the parameters, which are given by the
	 * 
	 * @param name
	 * @param x
	 * @param y
	 * @param radius
	 * @param color
	 */
	public void CreateCity(String id, String name, int x, int y, int radius, String color) {
		City city = new City(name, x, y, radius, color);
		
		if (!coordinatesToCity.containsKey(new Point2D.Float(x, y))) {
			if (!nameToCity.containsKey(name)) {
				coordinatesToCity.put(city.getCoordinates(), city);
				nameToCity.put(name, city);
				avlTree.put(city, city.getRadius());
			} else {
				CreateCityErrorOutput("duplicateCityName", name, x, y, radius, color);
				return;
			}
		} else {
			CreateCityErrorOutput("duplicateCityCoordinates", name, x, y, radius, color);
			return;
		}

		Element successElement = XmlParser.results.createElement("success");
		XmlParser.currElement.appendChild(successElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "createCity");
		if (!id.equals("")) {
			commandElement.setAttribute("id", id);
		}
		successElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		successElement.appendChild(parametersElement);

		Element nameElement = XmlParser.results.createElement("name");
		nameElement.setAttribute("value", name);
		parametersElement.appendChild(nameElement);

		Element xCoordElement = XmlParser.results.createElement("x");
		xCoordElement.setAttribute("value", Integer.toString(x));
		parametersElement.appendChild(xCoordElement);

		Element yCoordElement = XmlParser.results.createElement("y");
		yCoordElement.setAttribute("value", Integer.toString(y));
		parametersElement.appendChild(yCoordElement);

		Element radiusElement = XmlParser.results.createElement("radius");
		radiusElement.setAttribute("value", Integer.toString(radius));
		parametersElement.appendChild(radiusElement);

		Element colorElement = XmlParser.results.createElement("color");
		colorElement.setAttribute("value", color);
		parametersElement.appendChild(colorElement);

		Element outputElement = XmlParser.results.createElement("output");
		successElement.appendChild(outputElement);

	}

	private void NoCitiesToListError(String sortMethod) {
		Element errorElement = XmlParser.results.createElement("error");
		errorElement.setAttribute("type", "noCitiesToList");
		XmlParser.currElement.appendChild(errorElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "listCities");
		errorElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		errorElement.appendChild(parametersElement);

		Element sortByElement = XmlParser.results.createElement("sortBy");
		sortByElement.setAttribute("value", sortMethod);
		parametersElement.appendChild(sortByElement);
	}

	/**
	 * A method to sort all of the cities in order. One can specify how they
	 * would like to sort the elements.
	 * 
	 * @param sortMethod
	 */
	public void ListCities(String id, String sortMethod) {

		if (nameToCity.size() >= 1) {

			Element successElement = XmlParser.results.createElement("success");
			XmlParser.currElement.appendChild(successElement);

			Element commandElement = XmlParser.results.createElement("command");
			commandElement.setAttribute("name", "listCities");
			if (!id.equals("")) {
				commandElement.setAttribute("id", id);
			}
			successElement.appendChild(commandElement);

			Element parametersElement = XmlParser.results.createElement("parameters");
			successElement.appendChild(parametersElement);

			Element sortByElement = XmlParser.results.createElement("sortBy");

			if (sortMethod.equals("name")) {
				sortByElement.setAttribute("value", "name");
				parametersElement.appendChild(sortByElement);

				Element outputElement = XmlParser.results.createElement("output");
				successElement.appendChild(outputElement);

				Element cityListElement = XmlParser.results.createElement("cityList");
				outputElement.appendChild(cityListElement);

				for (String entry : nameToCity.keySet()) {
					City cityFromKey = nameToCity.get(entry);

					Element cityElement = XmlParser.results.createElement("city");
					cityElement.setAttribute("color", cityFromKey.getColor());
					cityElement.setAttribute("name", cityFromKey.getName());
					cityElement.setAttribute("radius", Integer.toString(cityFromKey.getRadius()));
					cityElement.setAttribute("x", Integer.toString((int) cityFromKey.getX()));
					cityElement.setAttribute("y", Integer.toString((int) cityFromKey.getY()));

					cityListElement.appendChild(cityElement);
				}

			} else if (sortMethod.equals("coordinate")) {
				sortByElement.setAttribute("value", "coordinate");
				parametersElement.appendChild(sortByElement);

				Element outputElement = XmlParser.results.createElement("output");
				successElement.appendChild(outputElement);

				Element cityListElement = XmlParser.results.createElement("cityList");
				outputElement.appendChild(cityListElement);

				for (Point2D.Float entry : coordinatesToCity.keySet()) {
					City cityFromKey = coordinatesToCity.get(entry);

					Element cityElement = XmlParser.results.createElement("city");
					cityElement.setAttribute("color", cityFromKey.getColor());
					cityElement.setAttribute("name", cityFromKey.getName());
					cityElement.setAttribute("radius", Integer.toString(cityFromKey.getRadius()));
					cityElement.setAttribute("x", Integer.toString((int) cityFromKey.getX()));
					cityElement.setAttribute("y", Integer.toString((int) cityFromKey.getY()));

					cityListElement.appendChild(cityElement);
				}
			}
		} else {
			NoCitiesToListError(sortMethod);
		}
	}

	private void DeleteCityError(String name) {
		Element errorElement = XmlParser.results.createElement("error");
		errorElement.setAttribute("type", "cityDoesNotExist");
		XmlParser.currElement.appendChild(errorElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "deleteCity");
		errorElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		errorElement.appendChild(parametersElement);

		Element nameElement = XmlParser.results.createElement("name");
		nameElement.setAttribute("value", name);
		parametersElement.appendChild(nameElement);

	}

	public void DeleteCity(String name) {
		if (nameToCity.containsKey(name)) {

			Element successElement = XmlParser.results.createElement("success");
			XmlParser.currElement.appendChild(successElement);

			Element commandElement = XmlParser.results.createElement("command");
			commandElement.setAttribute("name", "deleteCity");
			successElement.appendChild(commandElement);

			Element parametersElement = XmlParser.results.createElement("parameters");
			successElement.appendChild(parametersElement);

			Element deletedElement = XmlParser.results.createElement("name");
			deletedElement.setAttribute("value", name);
			parametersElement.appendChild(deletedElement);

			Element outputElement = XmlParser.results.createElement("output");
			successElement.appendChild(outputElement);

			// FIXED: FIRST. If a city is mapped in MXQuadTree structure, remove
			// it from the structure!
			// After that, remove from the dictionary
			if (mxQuadtree.contains(name)) {
				mxQuadtree.delete(name);
				Element cityUnmappedElement =
				XmlParser.results.createElement("cityUnmapped");
				cityUnmappedElement.setAttribute("name", name);
				cityUnmappedElement.setAttribute("x", Integer.toString((int)
				nameToCity.get(name).getX()));
				cityUnmappedElement.setAttribute("y", Integer.toString((int)
				nameToCity.get(name).getY()));
				cityUnmappedElement.setAttribute("color",
				nameToCity.get(name).getColor());
				cityUnmappedElement.setAttribute("radius",
				Integer.toString(nameToCity.get(name).getRadius()));
				outputElement.appendChild(cityUnmappedElement);
			}

			Point2D.Float coordinate = nameToCity.get(name).getCoordinates();
			nameToCity.remove(name);
			coordinatesToCity.remove(coordinate);
		} else {
			DeleteCityError(name);
		}

	}

	private void AVLTreeError() {
		Element errorElement = XmlParser.results.createElement("error");
		errorElement.setAttribute("type", "emptyTree");
		XmlParser.currElement.appendChild(errorElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "printAvlTree");
		errorElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		errorElement.appendChild(parametersElement);
	}

	public void PrintAVLTree() {
		if (!avlTree.isEmpty()) {

			Element successElement = XmlParser.results.createElement("success");
			XmlParser.currElement.appendChild(successElement);

			Element commandElement = XmlParser.results.createElement("command");
			commandElement.setAttribute("name", "printAvlTree");
			successElement.appendChild(commandElement);

			Element parametersElement = XmlParser.results.createElement("parameters");
			successElement.appendChild(parametersElement);

			Element outputElement = XmlParser.results.createElement("output");
			successElement.appendChild(outputElement);

			Element avlTreeElement = XmlParser.results.createElement("AvlGTree");
			outputElement.appendChild(avlTreeElement);
			avlTreeElement.setAttribute("cardinality", Integer.toString(avlTree.size()));
			avlTreeElement.setAttribute("height", Integer.toString(avlTree.height()));

			avlTreeElement.setAttribute("maxImbalance", Integer.toString(XmlParser.maxImbalance));

			avlTree.preorder(avlTreeElement); // DONE: Access the XmlParser
												// results in that area
		} else {
			AVLTreeError();
		}

	}

	/**
	 * Clears the MX QuadTree, Dictionary and AVLTree data structures. There are
	 * no error outputs for this method
	 */
	public void ClearAll() {
		nameToCity.clear();
		coordinatesToCity.clear();
		avlTree.clear();
		mxQuadtree.makeEmpty();
		pmQuadtree.makeEmpty();
		
		Element successElement = XmlParser.results.createElement("success");
		XmlParser.currElement.appendChild(successElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "clearAll");
		successElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		successElement.appendChild(parametersElement);

		Element outputElement = XmlParser.results.createElement("output");
		successElement.appendChild(outputElement);
	}

	private void MapCityErrorOutput(String errorType, String name) {
		Element errorElement = XmlParser.results.createElement("error");
		errorElement.setAttribute("type", errorType);
		XmlParser.currElement.appendChild(errorElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "mapCity");
		errorElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		errorElement.appendChild(parametersElement);

		Element nameElement = XmlParser.results.createElement("name");
		nameElement.setAttribute("value", name);
		parametersElement.appendChild(nameElement);
	}

	public void MapCity(String cityName) {
		// TODO: Need case for when city has already been mapped
		if (nameToCity.get(cityName) == null) {
			MapCityErrorOutput("nameNotInDictionary", cityName);
		} else if (mxQuadtree.contains(cityName)) {
			avlTree.put(nameToCity.get(cityName), nameToCity.get(cityName).getRadius());
			MapCityErrorOutput("cityAlreadyMapped", cityName); 
		} else if (nameToCity.get(cityName).getX() < 0 || nameToCity.get(cityName).getY() < 0
				|| nameToCity.get(cityName).getX() >= XmlParser.spatialWidth
				|| nameToCity.get(cityName).getY() >= XmlParser.spatialHeight) {
			MapCityErrorOutput("cityOutOfBounds", cityName); // TODO: See if
																// works
		} else {
			avlTree.put(nameToCity.get(cityName), nameToCity.get(cityName).getRadius());
			pmQuadtree.insert(nameToCity.get(cityName));

			Element successElement = XmlParser.results.createElement("success");
			XmlParser.currElement.appendChild(successElement);

			Element commandElement = XmlParser.results.createElement("command");
			commandElement.setAttribute("name", "mapCity");
			successElement.appendChild(commandElement);

			Element parametersElement = XmlParser.results.createElement("parameters");
			successElement.appendChild(parametersElement);

			Element mappedCityElement = XmlParser.results.createElement("name");
			mappedCityElement.setAttribute("value", cityName);
			parametersElement.appendChild(mappedCityElement);

			Element outputElement = XmlParser.results.createElement("output");
			successElement.appendChild(outputElement);
		}
	}

	private void EmptyMXTreeError() {
		Element errorElement = XmlParser.results.createElement("error");
		errorElement.setAttribute("type", "mapIsEmpty");
		XmlParser.currElement.appendChild(errorElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "printMXQuadtree");
		errorElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		errorElement.appendChild(parametersElement);
	}

	public void PrintMXQuadtree() {
		if (!mxQuadtree.isEmpty()) {

			Element successElement = XmlParser.results.createElement("success");
			XmlParser.currElement.appendChild(successElement);

			Element commandElement = XmlParser.results.createElement("command");
			commandElement.setAttribute("name", "printMXQuadtree");
			successElement.appendChild(commandElement);

			Element parametersElement = XmlParser.results.createElement("parameters");
			successElement.appendChild(parametersElement);

			Element outputElement = XmlParser.results.createElement("output");
			successElement.appendChild(outputElement);

			Element quadTreeElement = XmlParser.results.createElement("quadtree");
			outputElement.appendChild(quadTreeElement);

			mxQuadtree.inorder(quadTreeElement); // DONE: Access the XmlParser
													// results in that area
		} else {
			EmptyMXTreeError(); // DONE: Made a method to handle empty tree
								// error.
		}
	}

	public void SaveMap(String mapName) {
		Element successElement = XmlParser.results.createElement("success");
		XmlParser.currElement.appendChild(successElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "saveMap");
		successElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		successElement.appendChild(parametersElement);

		Element mapNameElement = XmlParser.results.createElement("name");
		mapNameElement.setAttribute("value", mapName);
		parametersElement.appendChild(mapNameElement);

		Element outputElement = XmlParser.results.createElement("output");
		successElement.appendChild(outputElement);
	}

	private void UnmapCityErrorOutput(String errorType, String name) {
		Element errorElement = XmlParser.results.createElement("error");
		errorElement.setAttribute("type", errorType);
		XmlParser.currElement.appendChild(errorElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "unmapCity");
		errorElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		errorElement.appendChild(parametersElement);

		Element nameElement = XmlParser.results.createElement("name");
		nameElement.setAttribute("value", name);
		parametersElement.appendChild(nameElement);
	}

	public void UnmapCity(String cityName) {
		// TODO: Need case for when city has already been mapped
		if (nameToCity.get(cityName) == null) {
			UnmapCityErrorOutput("nameNotInDictionary", cityName);
		} else if (!mxQuadtree.contains(cityName)) {
			UnmapCityErrorOutput("cityNotMapped", cityName); //TODO: See if
		} else {
			mxQuadtree.delete(cityName);

			Element successElement = XmlParser.results.createElement("success");
			XmlParser.currElement.appendChild(successElement);

			Element commandElement = XmlParser.results.createElement("command");
			commandElement.setAttribute("name", "unmapCity");
			successElement.appendChild(commandElement);

			Element parametersElement = XmlParser.results.createElement("parameters");
			successElement.appendChild(parametersElement);

			Element mappedCityElement = XmlParser.results.createElement("name");
			mappedCityElement.setAttribute("value", cityName);
			parametersElement.appendChild(mappedCityElement);

			Element outputElement = XmlParser.results.createElement("output");
			successElement.appendChild(outputElement);
		}
	}
	
	private void NearestCityError(int x, int y) {
		Element errorElement = XmlParser.results.createElement("error");
		errorElement.setAttribute("type", "cityNotFound");
		XmlParser.currElement.appendChild(errorElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "nearestCity");
		errorElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		errorElement.appendChild(parametersElement);
		
		Element xElement = XmlParser.results.createElement("x");
		xElement.setAttribute("value", Integer.toString(x));
		parametersElement.appendChild(xElement);
		
		Element yElement = XmlParser.results.createElement("y");
		yElement.setAttribute("value", Integer.toString(y));
		parametersElement.appendChild(yElement);
	}

	public void NearestCity(int cityXCoord, int cityYCoord) {
		if (!mxQuadtree.isEmpty()) {

			Element successElement = XmlParser.results.createElement("success");
			XmlParser.currElement.appendChild(successElement);

			Element commandElement = XmlParser.results.createElement("command");
			commandElement.setAttribute("name", "nearestCity");
			successElement.appendChild(commandElement);

			Element parametersElement = XmlParser.results.createElement("parameters");
			successElement.appendChild(parametersElement);

			Element xElement = XmlParser.results.createElement("x");
			xElement.setAttribute("value", Integer.toString(cityXCoord));
			parametersElement.appendChild(xElement);

			Element yElement = XmlParser.results.createElement("y");
			yElement.setAttribute("value", Integer.toString(cityYCoord));
			parametersElement.appendChild(yElement);

			Element outputElement = XmlParser.results.createElement("output");
			successElement.appendChild(outputElement);

			String cityName = pmQuadtree.findClosestPoint(cityXCoord, cityYCoord);
			
			Element cityUnmappedElement = XmlParser.results.createElement("city");
			cityUnmappedElement.setAttribute("name", cityName);
			cityUnmappedElement.setAttribute("x", Integer.toString((int) nameToCity.get(cityName).getX()));
			cityUnmappedElement.setAttribute("y", Integer.toString((int) nameToCity.get(cityName).getY()));
			cityUnmappedElement.setAttribute("color", nameToCity.get(cityName).getColor());
			cityUnmappedElement.setAttribute("radius", Integer.toString(nameToCity.get(cityName).getRadius()));
			outputElement.appendChild(cityUnmappedElement);

		} else {
			NearestCityError(cityXCoord, cityYCoord); // DONE: Made a method to handle empty tree
								// error.
		}
	}

	public void RangeCities(int cityXCoord, int cityYCoord, int radius, String saveMap) {
		ArrayList<String> cityNames = mxQuadtree.findRangeValues(cityXCoord, cityYCoord, radius);
		
		if (cityNames.size() > 0) {
			Element successElement = XmlParser.results.createElement("success");
			XmlParser.currElement.appendChild(successElement);
	
			Element commandElement = XmlParser.results.createElement("command");
			commandElement.setAttribute("name", "rangeCities");
			successElement.appendChild(commandElement);
	
			Element parametersElement = XmlParser.results.createElement("parameters");
			successElement.appendChild(parametersElement);
	
			Element xElement = XmlParser.results.createElement("x");
			xElement.setAttribute("value", Integer.toString(cityXCoord));
			parametersElement.appendChild(xElement);
	
			Element yElement = XmlParser.results.createElement("y");
			yElement.setAttribute("value", Integer.toString(cityYCoord));
			parametersElement.appendChild(yElement);
	
			Element radiusElement = XmlParser.results.createElement("radius");
			radiusElement.setAttribute("value", Integer.toString(radius));
			parametersElement.appendChild(radiusElement);
	
			if (!saveMap.equals("")) {
				Element saveMapElem = XmlParser.results.createElement("saveMap");
				saveMapElem.setAttribute("value", saveMap);
				parametersElement.appendChild(saveMapElem);
			}
	
			Element outputElement = XmlParser.results.createElement("output");
			successElement.appendChild(outputElement);
	
			Element cityListElement = XmlParser.results.createElement("cityList");
			outputElement.appendChild(cityListElement);
	
			Collections.sort(cityNames, new Comparator<String>() {
				@Override
				public int compare(String firstCity, String secondCity) {
	
					return secondCity.compareTo(firstCity);
				}
			});
	
	
			for (int cityInd = 0; cityInd < cityNames.size(); cityInd++) {
				Element cityUnmappedElement = XmlParser.results.createElement("city");
				cityUnmappedElement.setAttribute("name", cityNames.get(cityInd));
				cityUnmappedElement.setAttribute("x",
						Integer.toString((int) nameToCity.get(cityNames.get(cityInd)).getX()));
				cityUnmappedElement.setAttribute("y",
						Integer.toString((int) nameToCity.get(cityNames.get(cityInd)).getY()));
				cityUnmappedElement.setAttribute("color", nameToCity.get(cityNames.get(cityInd)).getColor());
				cityUnmappedElement.setAttribute("radius",
						Integer.toString(nameToCity.get(cityNames.get(cityInd)).getRadius()));
				cityListElement.appendChild(cityUnmappedElement);
			}

		} else {
			NoCitiesExistError(cityXCoord, cityYCoord, radius, saveMap);
		}
	}

	private void NoCitiesExistError(int cityXCoord, int cityYCoord, int radius, String mapName) {
		Element errorElement = XmlParser.results.createElement("error");
		errorElement.setAttribute("type", "noCitiesExistInRange");
		XmlParser.currElement.appendChild(errorElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "rangeCities");
		errorElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		errorElement.appendChild(parametersElement);

		Element xCoordElement = XmlParser.results.createElement("x");
		xCoordElement.setAttribute("value", Integer.toString(cityXCoord));
		parametersElement.appendChild(xCoordElement);

		Element yCoordElement = XmlParser.results.createElement("y");
		yCoordElement.setAttribute("value", Integer.toString(cityYCoord));
		parametersElement.appendChild(yCoordElement);

		Element radiusElement = XmlParser.results.createElement("radius");
		radiusElement.setAttribute("value", Integer.toString(radius));
		parametersElement.appendChild(radiusElement);
		
		if (!mapName.equals("")) {
			Element saveMapElem = XmlParser.results.createElement("saveMap");
			saveMapElem.setAttribute("value", mapName);
			parametersElement.appendChild(saveMapElem);
		}
	}

	public void PrintPMQuadtree() {
		if (!pmQuadtree.isEmpty()) {

			Element successElement = XmlParser.results.createElement("success");
			XmlParser.currElement.appendChild(successElement);

			Element commandElement = XmlParser.results.createElement("command");
			commandElement.setAttribute("name", "printPMQuadtree");
			successElement.appendChild(commandElement);

			Element parametersElement = XmlParser.results.createElement("parameters");
			successElement.appendChild(parametersElement);

			Element outputElement = XmlParser.results.createElement("output");
			successElement.appendChild(outputElement);

			Element quadTreeElement = XmlParser.results.createElement("quadtree");
			quadTreeElement.setAttribute("order", "3");
			outputElement.appendChild(quadTreeElement);

			pmQuadtree.inorder(quadTreeElement); // DONE: Access the XmlParser
													// results in that area
		} else {
			EmptyPMTreeError(); // TODO: Make a method to handle empty tree
								// error.
		}
	}

	private void EmptyPMTreeError() {
		// TODO Auto-generated method stub
		
	}

	public void MapRoad(String startCityName, String endCityName) {
		// TODO: Need case for when city has already been mapped
		if (nameToCity.get(startCityName) == null) {
			DoesntExistError("startPointDoesNotExist", "", startCityName);
		} else if (nameToCity.get(endCityName) == null) {
			DoesntExistError("endPointDoesNotExist", "", endCityName);
		} else if (startCityName.equals(endCityName)) {
			EqualityError("startEqualsEnd", "", startCityName, endCityName);
		} else if (nameToCity.get(startCityName).getX() < 0 || nameToCity.get(endCityName).getY() < 0
				|| nameToCity.get(startCityName).getX() >= XmlParser.spatialWidth
				|| nameToCity.get(startCityName).getY() >= XmlParser.spatialHeight
				|| nameToCity.get(endCityName).getX() >= XmlParser.spatialWidth
				|| nameToCity.get(endCityName).getY() >= XmlParser.spatialHeight) {
			OutOfBoundsError("roadOutOfBounds", "", startCityName, endCityName);
		} else {
			Element successElement = XmlParser.results.createElement("success");
			XmlParser.currElement.appendChild(successElement);

			Element commandElement = XmlParser.results.createElement("command");
			commandElement.setAttribute("name", "mapRoad");
			successElement.appendChild(commandElement);

			Element parametersElement = XmlParser.results.createElement("parameters");
			successElement.appendChild(parametersElement);

			Element startElement = XmlParser.results.createElement("start");
			startElement.setAttribute("value", startCityName);
			parametersElement.appendChild(startElement);
			
			Element endElement = XmlParser.results.createElement("end");
			endElement.setAttribute("value", endCityName);
			parametersElement.appendChild(endElement);

			Element outputElement = XmlParser.results.createElement("output");
			successElement.appendChild(outputElement);

			pmQuadtree.insertPM(nameToCity.get(startCityName), nameToCity.get(endCityName));
			
			Element roadcreatedElement = XmlParser.results.createElement("roadCreated");
			roadcreatedElement.setAttribute("start", startCityName);
			roadcreatedElement.setAttribute("end", endCityName);
			outputElement.appendChild(roadcreatedElement);
		}
	}

	private void EqualityError(String errorName, String id, String start, String end) {
		Element errorElement = XmlParser.results.createElement("error");
		errorElement.setAttribute("type", errorName);
		XmlParser.currElement.appendChild(errorElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "mapRoad");
		//commandElement.setAttribute("id", id);
		errorElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		errorElement.appendChild(parametersElement);

		Element startElement = XmlParser.results.createElement("start");
		startElement.setAttribute("value", start);
		
		Element endElement = XmlParser.results.createElement("end");
		endElement.setAttribute("value", end);
		
		parametersElement.appendChild(startElement);
		parametersElement.appendChild(endElement);
	}

	private void DoesntExistError(String errorName, String id, String start) {
		Element errorElement = XmlParser.results.createElement("error");
		errorElement.setAttribute("type", errorName);
		XmlParser.currElement.appendChild(errorElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "mapRoad");
		//commandElement.setAttribute("id", id);
		errorElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		errorElement.appendChild(parametersElement);

		Element startElement = XmlParser.results.createElement("start");
		startElement.setAttribute("value", start);
		
		parametersElement.appendChild(startElement);
	}
	
	private void OutOfBoundsError(String errorName, String id, String start, String end) {
		Element errorElement = XmlParser.results.createElement("error");
		errorElement.setAttribute("type", errorName);
		XmlParser.currElement.appendChild(errorElement);

		Element commandElement = XmlParser.results.createElement("command");
		commandElement.setAttribute("name", "mapRoad");
		//commandElement.setAttribute("id", id);
		errorElement.appendChild(commandElement);

		Element parametersElement = XmlParser.results.createElement("parameters");
		errorElement.appendChild(parametersElement);

		Element startElement = XmlParser.results.createElement("start");
		startElement.setAttribute("value", start);
		
		Element endElement = XmlParser.results.createElement("end");
		endElement.setAttribute("value", end);
		
		parametersElement.appendChild(startElement);
		parametersElement.appendChild(endElement);
	}

}