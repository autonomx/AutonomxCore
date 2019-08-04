package core.apiCore.helpers;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import core.support.logger.TestLog;

public class XmlHelper {
	/**
	 * Convert a contents of a Document to a String
	 * 
	 * @param doc
	 * @return String
	 */
	public static String convertDocumentToString(Document doc) {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (IllegalArgumentException | TransformerException ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
	}

	/**
	 * Convert a String in XML format to a Document
	 * 
	 * @param xmlStr
	 * @return Document
	 */
	public static Document convertXmlStringToDocument(String xmlStr) {
		Document doc = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			doc = builder.parse(new InputSource(new StringReader(xmlStr)));
			doc.getDocumentElement().normalize();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}

	/**
	 * Convert contents of an XML file to a string
	 * 
	 * @param filePath
	 * @return String
	 */
	public static String convertXmlFileToString(Path filePath) {
		return convertDocumentToString(readFile(filePath.toString()));
	}

	/**
	 * Read a XML file And get a Document retrying And catching interruptions from
	 * other threads.
	 * 
	 * @param inputFilePath
	 * @return Document
	 */
	public synchronized static Document readFile(String inputFilePath) {
		Document doc = null;
		int retry = 3;
		do {
			try {
				retry--;

				String inputFileContents = new String(Files.readAllBytes(Paths.get(inputFilePath)));
				inputFileContents = DataHelper.replaceParameters(inputFileContents);
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(new InputSource(new StringReader(inputFileContents)));
				doc.getDocumentElement().normalize();
			} catch (Exception ex) {
				Thread.interrupted();
			}
		} while (doc == null && retry > 0);
		return doc;
	}

	/**
	 * Get text value of the node specified by xpath
	 * 
	 * @param xpathString
	 * @param xmlString
	 * @return String
	 */
	public static String getNodeValue(String xpathString, String xmlString) {
		return getNodeList(xpathString, xmlString).item(0).getTextContent();
	}

	/**
	 * Get text value of the node specified by xpath
	 * 
	 * @param xpathString
	 * @param xmlString
	 * @return String
	 */
	public static NodeList getNodeList(String xpathString, String xmlString) {
		try {
			Document doc = convertXmlStringToDocument(xmlString);
			XPath xpath = XPathFactory.newInstance().newXPath();
			NodeList nodeList = (NodeList) xpath.evaluate(xpathString, doc, XPathConstants.NODESET);

			if (nodeList == null || nodeList.getLength() == 0) {
				TestLog.ConsoleLog("No node found for xpath value: {0}", xpathString);
			} else {
				return nodeList;
			}
		} catch (XPathExpressionException ex) {
			TestLog.logWarning("Exception encountered for xpath value: " + xpathString, ex);
		}
		return null;
	}

	/**
	 * In outputParams get the params with syntax <$> in responseBody And Then add
	 * them to ConfigurationParams
	 *
	 * @param outputParams
	 * @param responseBody
	 */
	public static void addOutputParamValuesToConfig(String outputParams, String responseBody) {

	}

	/**
	 * Get the list of xPaths corresponding to the input XML String
	 * 
	 * @param xmlString
	 * @return ArrayList
	 */
	public static ArrayList<String> getXPaths(String xmlString, String xpath) {

		ArrayList<String> xPathList = null;
		return xPathList;
	}

	/**
	 * converts json string to xml and gets xpath value from it
	 * 
	 * @param json
	 * @param xpath
	 * @return result list as string separated by ","
	 */
	public static String getXpathValueFromJson(String json, String xpath) {
		String value = StringUtils.EMPTY;

		// convert xml string to json string
		String xml = jsonToXml(json);

		// get xpath value form xml string
		value = getXpathFromXml(xml, xpath);

		return value;
	}

	/**
	 * get xpath value form xml string
	 * 
	 * @param xml
	 * @param xpath
	 * @return result list as string separated by ","
	 */
	public static String getXpathFromXml(String xml, String xpath) {
		List<String> valueList = new ArrayList<String>();
		// convert xml string to doc
		Document doc = convertXmlStringToDocument(xml);

		XPathFactory xPathFactory = XPathFactory.newInstance();

		// Create XPath object from XPathFactory
		XPath xpathObject = xPathFactory.newXPath();

		try {
			// Compile the XPath expression for getting all brands
			XPathExpression xPathEnvelopeExpr = xpathObject.compile(xpath);

			Object result = xPathEnvelopeExpr.evaluate(doc, XPathConstants.NODESET);

			NodeList nodes = (NodeList) result;
			valueList = new ArrayList<String>();
			for (int i = 0; i < nodes.getLength(); i++) {
				valueList.add(nodes.item(i).getNodeValue());
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return DataHelper.listToString(valueList);
	}

	/**
	 * convert json string to xml string
	 * 
	 * @param json
	 * @return
	 */
	public static String jsonToXml(String json) {
		String xml = StringUtils.EMPTY;
		try {
			JSONObject jsonObject = new JSONObject(json);
			xml = XML.toString(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return xml;
	}
}
