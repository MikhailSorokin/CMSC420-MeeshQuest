package cmsc420.meeshquest.part1;

import cmsc420.meeshquest.utilities.MeeshQuestGUI;
import cmsc420.meeshquest.utilities.XmlParser;

/**
 * This class should read in commands from the XML file and 
 * interpret them / call necessary methods. 
 * 
 * This should be used as a general implementation 
 * for all three parts of the project.
 * 
 * @author Mikhail Sorokin
 *
 */
public class MeeshQuest {

	public static String fileName = "src/Inputs/part1.fatalerror.input.xml";
	public static boolean shouldDrawMap = false; //specify if you want GUI to be used
	
	public static void main(String[] args) {
		if (shouldDrawMap) {		
			MeeshQuestGUI.DrawGUI();
		}
		//Probably should make this static
		XmlParser xmlParser = new XmlParser(fileName);
		xmlParser.LoadXMLFile();
	}

}
