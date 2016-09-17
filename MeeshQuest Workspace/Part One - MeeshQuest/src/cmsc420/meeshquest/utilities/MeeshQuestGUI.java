package cmsc420.meeshquest.utilities;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cmsc420.xml.XmlUtility;

/**
 * This creates the MeeshQuest GUI feature, if one
 * so desires to use one.
 * 
 * @author Mikhail Sorokin
 *
 */
public class MeeshQuestGUI {

	
	//private static int spatialWidth = 800;
	//private static int spatialHeight = 800;
	
	/**
	 * Probably don't want to make static, since there 
	 * can be multiple copies of the GUI.
	 * 
	 * TODO: Don't make this class static!
	 */
	public static void DrawGUI() {
		/*Document d = XmlUtility.parse(new File("in.xml"));
		Element docElement = d.getDocumentElement();
		NodeList nl = docElement.getChildNodes();
		for(int i = 0; i < nl.getLength(); ++i) {
			Node command = nl.item(i); // process the command here
		}*/

	}
	
}
