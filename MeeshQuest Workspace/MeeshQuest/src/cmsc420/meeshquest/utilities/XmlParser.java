package cmsc420.meeshquest.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cmsc420.xml.XmlUtility;

/**
 * 
 * This class will take in a XML file and read,
 * line by line, the command that is in the XML
 * file and see if it is first, a valid command,
 * and if so, execute it.
 * 
 * @author Mikhail Sorokin
 *
 */
public class XmlParser {
	
	public static Document results = null;
	public static Element currElement = null;
	public boolean aboutToSubmit = false; //use for submit server stuff!
	public static int spatialWidth;
	public static int spatialHeight;
	public static int maxImbalance;
	
	private String path;
	private MethodMediator methodMediator = new MethodMediator(); //TODO: Maybe make this class static?
	
	public XmlParser(String filePath) {
		path = filePath;
	}
	
	public void LoadXMLFile() {
		try {
			Document doc = null;
			results = XmlUtility.getDocumentBuilder().newDocument();
			if (!aboutToSubmit) {
				File filePath = new File(path);
				System.setIn(new FileInputStream(filePath));
				doc = XmlUtility.parse(filePath);
			} else {
				doc = XmlUtility.validateNoNamespace(System.in);
			}
			//something wrong with location of xchema. Must be relative to location or sumthing.

			Element commandNode = doc.getDocumentElement();
			spatialWidth = Integer.parseInt(commandNode.getAttribute("spatialWidth"));
			spatialHeight = Integer.parseInt(commandNode.getAttribute("spatialHeight"));
			maxImbalance = Integer.parseInt(commandNode.getAttribute("g"));
			
			Element resultsTag = results.createElement("results");
			results.appendChild(resultsTag);
			
			currElement = resultsTag;
			
			final NodeList childNodes = commandNode.getChildNodes();
			for (int childInd = 0; childInd < childNodes.getLength(); childInd++) {
				if (childNodes.item(childInd).getNodeType() == Document.ELEMENT_NODE) {
					ExecuteCommand((Element)childNodes.item(childInd));
				}
			}
			
		} catch (SAXException | IOException | ParserConfigurationException e) {
			Element fatalErrorElem = results.createElement("fatalError");
			results.appendChild(fatalErrorElem);
		}
		finally {
            try {
				XmlUtility.print(results);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void ExecuteCommand(Element command) {
		//Execute CreateCity command
		if (command.getNodeName().equals("createCity")) {
			String id = command.getAttribute("id");
			String cityName = command.getAttribute("name");
			int cityXCoord = Integer.parseInt(command.getAttribute("x"));
			int cityYCoord = Integer.parseInt(command.getAttribute("y"));
			int radius = Integer.parseInt(command.getAttribute("radius"));
			String color = command.getAttribute("color");
			methodMediator.CreateCity(id, cityName, cityXCoord, cityYCoord, radius, color);
		} 
		//Execute DeleteCity command
		else if (command.getNodeName().equals("deleteCity")) {
			String cityName = command.getAttribute("name");
			methodMediator.DeleteCity(cityName);
		} 
		//Execute ClearAll command
		else if (command.getNodeName().equals("clearAll")) {
			methodMediator.ClearAll();
		} 
		//Execute ListCities command
		else if (command.getNodeName().equals("listCities")) {
			String sortMethod = command.getAttribute("sortBy");
			String id = command.getAttribute("id");
			methodMediator.ListCities(id, sortMethod);
		} 
		//Execute MapCity command
		else if (command.getNodeName().equals("mapCity")) {
			String cityName = command.getAttribute("name");
			methodMediator.MapCity(cityName);
		} 
		//Execute UnmapCity command
		else if (command.getNodeName().equals("unmapCity")) {
			String cityName = command.getAttribute("name");
			methodMediator.UnmapCity(cityName);
		} 
		//Execute printMXQuadtree command
		else if (command.getNodeName().equals("printMXQuadtree")) {
			methodMediator.PrintMXQuadtree();
		} 
		//Execute saveMap command
		else if (command.getNodeName().equals("saveMap")) {
			String mapName = command.getAttribute("name");
			methodMediator.SaveMap(mapName);
		} 
		//Execute rangeCities command
		else if (command.getNodeName().equals("rangeCities")) {
			int cityXCoord = Integer.parseInt(command.getAttribute("x"));
			int cityYCoord = Integer.parseInt(command.getAttribute("y"));
			int radius = Integer.parseInt(command.getAttribute("radius"));
			String saveMap = "";
			if (!command.getAttribute("saveMap").equals("")) {
				saveMap = command.getAttribute("saveMap");
			}
			methodMediator.RangeCities(cityXCoord, cityYCoord, radius, saveMap);
		} 
		//Execute nearestCity command
		else if (command.getNodeName().equals("nearestCity")) {
			int cityXCoord = Integer.parseInt(command.getAttribute("x"));
			int cityYCoord = Integer.parseInt(command.getAttribute("y"));
			methodMediator.NearestCity(cityXCoord, cityYCoord);
		} 
		//Execute printAvlTree command
		else if (command.getNodeName().equals("printAvlTree")) {
			methodMediator.PrintAVLTree();
		} 
		//Execute printAvlTree command
		else if (command.getNodeName().equals("printPMQuadtree")) {
			methodMediator.PrintPMQuadtree();
		} else if (command.getNodeName().equals("mapRoad")) {
			String startVertex = command.getAttribute("start");
			String endVertex = command.getAttribute("end");
			methodMediator.MapRoad(startVertex, endVertex);
		} else if (command.getNodeName().equals("mapCity")) {
			String nameVertex = command.getAttribute("name");
			methodMediator.MapCity(nameVertex);
		}
		//Write a failure command
		else {
			Element undefinedErrorElem = results.createElement("fatalError");
			currElement.appendChild(undefinedErrorElem);
		}
	}
	
}