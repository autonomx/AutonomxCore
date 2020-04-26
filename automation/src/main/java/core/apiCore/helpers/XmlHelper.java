package core.apiCore.helpers;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
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
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import core.helpers.Helper;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;

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
			factory.setNamespaceAware(true);
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
		Document document = readFile(filePath.toString());
		return convertDocumentToString(document);
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
	 * get xpath value form xml string test out xpath values at:
	 * https://www.freeformatter.com/xpath-tester.html#ad-output
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
				valueList.add(nodes.item(i).getTextContent());
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

	/**
	 * if request body is empty, return xml template string if request body contains
	 * xml tag, replace tag with value eg. "soi:EquipmentID:1:equip_<@_TIME_17>"
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static String getRequestBodyFromXmlTemplate(ServiceObject serviceObject) {
		Path templatePath = DataHelper.getTemplateFilePath(serviceObject.getTemplateFile());
		String xmlFileValue = convertXmlFileToString(templatePath);
		xmlFileValue = DataHelper.replaceParameters(xmlFileValue);

		if (serviceObject.getRequestBody().isEmpty()) {
			return xmlFileValue;
		} else {
			return replaceRequestTagValues(serviceObject);
		}
	}

	/**
	 * replace tag value in xml string at index
	 * 
	 * @param position: starts at 1
	 * @param tag
	 * @param value
	 * @return
	 */
	public static String replaceTagValue(String xml, String tag, String value, int position) {
		xml = replaceGroup("<" + tag + ">(.*?)<\\/" + tag + ">", xml, 1, position, value);
		return xml;
	}

	/**
	 * replace tag value in xml for all occurrences
	 * 
	 * @param xml
	 * @param tag
	 * @param value
	 * @return
	 */
	public static String replaceTagValue(String xml, String tag, String value) {
		xml = xml.replaceAll("<" + tag + ">(.*?)<\\/" + tag + ">", "<" + tag + ">" + value + "</" + tag + ">");
		return xml;
	}

	/**
	 * generic replace value using regex using index
	 * 
	 * @param regex
	 * @param source
	 * @param groupToReplace
	 * @param groupOccurrence
	 * @param replacement
	 * @return
	 */
	public static String replaceGroup(String regex, String source, int groupToReplace, int groupOccurrence,
			String replacement) {

		if (groupOccurrence == 0)
			Helper.assertFalse("position starts at 1");

		Matcher m = Pattern.compile(regex).matcher(source);
		for (int i = 0; i < groupOccurrence; i++)
			if (!m.find())
				return source; // pattern not met, may also throw an exception here
		return new StringBuilder(source).replace(m.start(groupToReplace), m.end(groupToReplace), replacement)
				.toString();
	}

	/**
	 * replaces request header tag values eg. "soi:EquipmentID:1:<@_TIME_16>" tag:
	 * soi:EquipmentID, position: 1, value: <@_TIME_16>
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static String replaceRequestTagValues(ServiceObject serviceObject) {
		String xmlString = DataHelper.getServiceObjectTemplateString(serviceObject);
		Helper.assertTrue("xml string is empty", !xmlString.isEmpty());

		// replace parameters
		xmlString = DataHelper.replaceParameters(xmlString);

		// if request is valid xml, do not update. only key value pair
		if (isValidXmlString(serviceObject.getRequestBody())) {
			return serviceObject.getRequestBody();
		}

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getRequestBody());
		for (KeyValue keyword : keywords) {
			if(keyword.value.toString().isEmpty()) continue;
			
			xmlString = replaceTagValue(xmlString, keyword.key, keyword.value.toString(),
					Integer.valueOf(keyword.position));
		}
		return xmlString;
	}

	/**
	 * get first matching tag value
	 * 
	 * @param requestBody
	 * @param tag
	 * @return
	 */
	public static String getXmlTagValue(String value, String tag) {
		return getXmlTagValue(value, tag, 1);
	}

	/**
	 * get tag value
	 * 
	 * @param source
	 * @param tag
	 * @param position: starts at 1
	 * @return
	 */
	public static String getXmlTagValue(String source, String tag, int position) {
		List<String> values = new ArrayList<String>();
		try {
			String patternString = "<" + tag + ">(.*?)<\\/" + tag + ">";
			final Pattern pattern = Pattern.compile(patternString);
			final Matcher matcher = pattern.matcher(source);
			while (matcher.find()) {
				values.add(matcher.group(1));
			}
		} catch (Exception e) {
			e.getMessage();
		}
		if (position == 0)
			Helper.assertFalse("position starts at 1. your position: " + position + ".");
		if (values.isEmpty())
			Helper.assertFalse(
					"tag value: " + tag + " at position: " + position + " was not found at xml: \n" + source + " \n\n");
		if (position > values.size())
			Helper.assertFalse("position is greater than response size. position: " + position + ". response size: "
					+ values.size() + ". values: " + Arrays.toString(values.toArray()));
		return values.get(position - 1);
	}

	/**
	 * return true if file is xml file
	 * 
	 * @param filename
	 * @return
	 */
	public static boolean isXmlFile(String filename) {
		if (filename.toLowerCase().endsWith("xml"))
			return true;
		return false;
	}

	/**
	 * validates if xml string is valid xml
	 * 
	 * @param xmlString
	 * @return
	 */
	public static boolean isValidXmlString(String xmlString) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setValidating(false);
			dbFactory.setSchema(null);

			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dBuilder.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException exception) throws SAXException {
				}

				@Override
				public void error(SAXParseException exception) throws SAXException {
				}

				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
				}
			});
			dBuilder.parse(new InputSource(new StringReader(xmlString)));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String prettyFormat(String input, int indent) {
		try {
			Source xmlInput = new StreamSource(new StringReader(input));
			StringWriter stringWriter = new StringWriter();
			StreamResult xmlOutput = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute("indent-number", indent);
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);
			return xmlOutput.getWriter().toString();
		} catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please review it
		}
	}
}
