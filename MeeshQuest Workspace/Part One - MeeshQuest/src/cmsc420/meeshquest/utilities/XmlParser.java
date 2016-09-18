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
			String cityName = command.getAttribute("name");
			int cityXCoord = Integer.parseInt(command.getAttribute("x"));
			int cityYCoord = Integer.parseInt(command.getAttribute("y"));
			int radius = Integer.parseInt(command.getAttribute("radius"));
			String color = command.getAttribute("color");
			methodMediator.CreateCity(cityName, cityXCoord, cityYCoord, radius, color);
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
			methodMediator.ListCities(sortMethod);
		} 
		//Execute MapCity command
		else if (command.getNodeName().equals("mapCity")) {
			Element elt = results.createElement("success");
			elt.setAttribute("test", "5");
			results.appendChild(elt);
		} 
		//Execute UnmapCity command
		else if (command.getNodeName().equals("unmapCity")) {
			Element elt = results.createElement("success");
			elt.setAttribute("test", "5");
			results.appendChild(elt);
		} 
		//Execute printMXQuadtree command
		else if (command.getNodeName().equals("printMXQuadtree")) {
			Element elt = results.createElement("success");
			elt.setAttribute("test", "5");
			results.appendChild(elt);
		} 
		//Execute saveMap command
		else if (command.getNodeName().equals("saveMap")) {
			Element elt = results.createElement("success");
			elt.setAttribute("test", "5");
			results.appendChild(elt);
		} 
		//Execute rangeCities command
		else if (command.getNodeName().equals("rangeCities")) {
			Element elt = results.createElement("success");
			elt.setAttribute("test", "5");
			results.appendChild(elt);
		} 
		//Execute nearestCity command
		else if (command.getNodeName().equals("nearestCity")) {
			Element elt = results.createElement("success");
			elt.setAttribute("test", "5");
			results.appendChild(elt);
		} 
		//Execute printAvlTree command
		else if (command.getNodeName().equals("printAvlTree")) {
			methodMediator.PrintAVLTree();
		} 
		//Write a failure command
		else {
			Element undefinedErrorElem = results.createElement("fatalError");
			currElement.appendChild(undefinedErrorElem);
		}
	}
	
}