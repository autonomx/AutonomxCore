package core.support.annotation.template.manager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import core.helpers.Helper;
import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.PackageHelper;
import core.support.configReader.Config;
import core.support.objects.DriverObject;

public class PanelManagerGenerator {
	
	public static void writePanelManagerClass(Map<String, List<Element>> panelMap) {
		try {
			writePanelManagerClassImplementation(panelMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

		

	public static void writePanelManagerClassImplementation(Map<String, List<Element>> panelMap) throws IOException {

		Logger.debug("<<<<start generating panel manager class>>>>");

		Logger.debug("writePanelManagerClass: panelManagers: " + panelMap.size());
		for (Entry<String, List<Element>> entry : panelMap.entrySet()) {

			Element firstElement = entry.getValue().get(0);

			Logger.debug("panel name: " + entry.getKey());
			Logger.debug("panel value: " + entry.getValue().get(0).asType().toString());

			JavaFileObject fileObject = FileCreatorHelper.createFile(firstElement);

			BufferedWriter bw = new BufferedWriter(fileObject.openWriter());
			Date currentDate = new Date();
			bw.append("/**Auto generated code,don't modify it.\n");
			bw.append("* Author             ---- > Auto Generated.\n");
			bw.append("* Date  And Time     ---- > " + currentDate.toString() + "\n");
			bw.append("*");
			bw.append("**/\n\n\n\n");
			bw.append("package " + PackageHelper.getPackagePath(firstElement) + ";\n");
			bw.newLine();
			bw.newLine();

			// add the panel imports
			for (Element element : entry.getValue()) {
				String panelPath = element.asType().toString();
				bw.append("import " + panelPath + ";\n");
			}
			bw.append("import core.support.configReader.Config;\n");
			bw.append("import core.support.objects.DriverObject;\n");
			bw.append("import core.helpers.Helper;\n");
			
			bw.newLine();
			bw.newLine();

			bw.append("public class " + "PanelManager {\n\n\n");

			// add panel declarations
			for (Element element : entry.getValue()) {
				String panelName = element.getSimpleName().toString();

				bw.append("public " + panelName + " " + panelName.replace("Panel", "").toLowerCase() + " = new "
						+ panelName + "(this);\n");
			}
			bw.newLine();

			writeDrivers(PackageHelper.getModuleName(firstElement), bw);

			bw.append("}\n");

			bw.flush();
			bw.close();
			Logger.debug("completed writing panel manager: " + entry.getKey());
		}
		Logger.debug("<<<<complete generating panel manager class>>>>>");
	}
	
	/**
	 * public DriverObject getWebDriver() { return new
	 * DriverObject().withWebDriver("webApp", Config.getValue("webApp")); }
	 * 
	 * public DriverObject getWebDriver(String url) { return new
	 * DriverObject().withWebDriver("webApp", url); }
	 * 
	 * public DriverObject getIosTabletDriver() { return new
	 * DriverObject().withiOSDriver("ios.tablet"); }
	 * 
	 * public DriverObject getIosMobileDriver() { return new
	 * DriverObject().withiOSDriver("ios.mobile"); }
	 * 
	 * public DriverObject getAndroidTabletDriver() { return new
	 * DriverObject().withiOSDriver("android.tablet"); }
	 * 
	 * public DriverObject getAndroidMobileDriver() { return new
	 * DriverObject().withiOSDriver("android.mobile"); }
	 * 
	 * public DriverObject getWinAppDriver() { return new
	 * DriverObject().withWinDriver(); }
	 * 
	 * public DriverObject getApiDriver() { return new
	 * DriverObject().withDriverType(DriverType.API); }
	 * 
	 * public DriverObject getGenericDriver() { return new
	 * DriverObject().withDriverType(DriverType.API); }
	 
	public DriverObject getHybridDriver() {
		return getHybridDriver(Config.getValue("webApp"));
	}
	
	public DriverObject getHybridDriver(String url) {
		String hybridDriver = Config.getValue("appium.hybrid.driver");
		if(hybridDriver.equals("WEB"))
			return getWebDriver(url);
		else if(hybridDriver.equals("ANDROID_MOBILE"))
			return getAndroidMobileDriver();
		else if(hybridDriver.equals("ANDROID_TABLET"))
			return getAndroidTabletDriver();
		else if(hybridDriver.equals("IOS_MOBILE"))
			return getIosMobileDriver();
		else if(hybridDriver.equals("IOS_TABLET"))
			return getIosTabletDriver();
		else if(hybridDriver.equals("WINAPP"))
			return getWinAppDriver();
		Helper.assertFalse("Correct driver not selected at appium.hybrid.driver option at appium.property ");
		return null;
	}
	
	return new DriverObject().withGenericDriver("webApp");
}
	 
	 * @throws IOException
	 */

	private static void writeDrivers(String moduleName, BufferedWriter bw) throws IOException {

		// web driver
		bw.append("	public DriverObject getWebDriver() {\n");
		bw.append("		return new DriverObject().withWebDriver(\"" + moduleName + "\", Config.getValue(\"" + moduleName
				+ "\"));\n");
		bw.append("	}\n");

		// web driver
		bw.append("	public DriverObject getWebDriver(String url) {\n");
		bw.append("		return new DriverObject().withWebDriver(\"" + moduleName + "\", url);\n");
		bw.append("	}\n");

		// ios tablet driver
		bw.append("	public DriverObject getIosTabletDriver() {\n");
		bw.append("		return new DriverObject().withiOSDriver(\"" + moduleName + "\",\"ios.tablet\");\n");
		bw.append("	}\n");

		// ios mobile driver
		bw.append("	public DriverObject getIosMobileDriver() {\n");
		bw.append("		return new DriverObject().withiOSDriver(\"" + moduleName + "\",\"ios.mobile\");\n");
		bw.append("	}\n");

		// andorid tablet driver
		bw.append("	public DriverObject getAndroidTabletDriver() {\n");
		bw.append("		return new DriverObject().withAndroidDriver(\"" + moduleName + "\",\"android.tablet\");\n");
		bw.append("	}\n");

		// android mobile driver
		bw.append("	public DriverObject getAndroidMobileDriver() {\n");
		bw.append("		return new DriverObject().withAndroidDriver(\"" + moduleName + "\",\"android.mobile\");\n");
		bw.append("	}\n");

		// windows driver
		bw.append("	public DriverObject getWinAppDriver() {\n");
		bw.append("		return new DriverObject().withWinDriver(\"" + moduleName + "\");\n");
		bw.append("	}\n");

		// api driver
		bw.append("	public DriverObject getApiDriver() {\n");
		bw.append("		return new DriverObject().withApiDriver(\"" + moduleName + "\");\n");
		bw.append("	}\n");

		// generic driver
		bw.append("	public DriverObject getGenericDriver() {\n");
		bw.append("		return new DriverObject().withGenericDriver(\"" + moduleName + "\");\n");
		bw.append("	}\n");
	
		// hybrid driver
		bw.append("	public DriverObject getHybridDriver() {" + "\n");
		bw.append("		return getHybridDriver(Config.getValue(\"webApp\"));" + "\n");
		bw.append("	}" + "\n" );
		
		// hybrid driver
		bw.append("	public DriverObject getHybridDriver(String url) {" + "\n");
		bw.append("		String hybridDriver = Config.getValue(\"appium.hybrid.driver\");" + "\n");
		bw.append("		if(hybridDriver.equals(\"WEB\"))" + "\n" );
		bw.append("			return getWebDriver(url);" + "\n" );
		bw.append("		else if(hybridDriver.equals(\"ANDROID_MOBILE\"))" + "\n" );
		bw.append("			return getAndroidMobileDriver();" + "\n" );
		bw.append("		else if(hybridDriver.equals(\"ANDROID_TABLET\"))" + "\n" );
		bw.append("			return getAndroidTabletDriver();" + "\n" );
		bw.append("		else if(hybridDriver.equals(\"IOS_MOBILE\"))" + "\n" );
		bw.append("			return getIosMobileDriver();" + "\n" );
		bw.append("		else if(hybridDriver.equals(\"IOS_TABLET\"))" + "\n" );
		bw.append("			return getIosTabletDriver();" + "\n" );
		bw.append("		else if(hybridDriver.equals(\"WINAPP\"))" + "\n" );
		bw.append("			return getWinAppDriver();" + "\n" );
		bw.append("		Helper.assertFalse(\"Correct driver not selected at appium.hybrid.driver option at appium.property \");" + "\n" );
		bw.append("		return null;" + "\n" );
		bw.append("	}" + "\n" );
	}
}
