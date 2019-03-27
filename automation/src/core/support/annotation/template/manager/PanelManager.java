package core.support.annotation.template.manager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.PackageHelper;

public class PanelManager {

	public static void writePanelManagerClass(Map<String, List<Element>> panelMap) throws IOException {

		System.out.println("writePanelManagerClass: panelManagers: " + panelMap.size());
		for (Entry<String, List<Element>> entry : panelMap.entrySet()) {

			Element firstElement = entry.getValue().get(0);

			System.out.println("panel name: " + entry.getKey());
			System.out.println("panel value: " + entry.getValue().get(0).asType().toString());

			JavaFileObject fileObject = FileCreatorHelper.createFile(firstElement);

			BufferedWriter bw = new BufferedWriter(fileObject.openWriter());
			Date currentDate = new Date();
			bw.append("/**Auto generated code,don't modify it.\n");
			bw.append("* Author             ---- > Auto Generated.\n");
			bw.append("* Date  And Time     ---- > " + currentDate.toString() + "\n");
			bw.append("* Source             -----> " + fileObject.getName() + "\n");
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
			System.out.println("completed: " + entry.getKey());
		}
	}
	
	/**
	 * public DriverObject getWebDriver() { return new
	 * DriverObject().withWebDriver("webApp", Config.getValue("webApp")); }
	 * 
	 * public DriverObject getWebDriver(String url) { return new
	 * DriverObject().withWebDriver("webApp", Config.getValue(url)); }
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
	 * 
	 * @throws IOException
	 */

	public static void writeDrivers(String moduleName, BufferedWriter bw) throws IOException {

		// web driver
		bw.append("public DriverObject getWebDriver() {\n");
		bw.append("	return new DriverObject().withWebDriver(\"" + moduleName + "\", Config.getValue(\"" + moduleName
				+ "\"));\n");
		bw.append("}\n");

		// web driver
		bw.append("public DriverObject getWebDriver(String url) {\n");
		bw.append("	return new DriverObject().withWebDriver(\"" + moduleName + "\", Config.getValue(url));\n");
		bw.append("}\n");

		// ios tablet driver
		bw.append("public DriverObject getIosTabletDriver() {\n");
		bw.append("	return new DriverObject().withiOSDriver(\"" + moduleName + "\",\"ios.tablet\");\n");
		bw.append("}\n");

		// ios mobile driver
		bw.append("public DriverObject getIosMobileDriver() {\n");
		bw.append("	return new DriverObject().withiOSDriver(\"" + moduleName + "\",\"ios.mobile\");\n");
		bw.append("}\n");

		// andorid tablet driver
		bw.append("public DriverObject getAndroidTabletDriver() {\n");
		bw.append("	return new DriverObject().withAndroidDriver(\"" + moduleName + "\",\"android.tablet\");\n");
		bw.append("}\n");

		// android mobile driver
		bw.append("public DriverObject getAndroidMobileDriver() {\n");
		bw.append("	return new DriverObject().withAndroidDriver(\"" + moduleName + "\",\"android.mobile\");\n");
		bw.append("}\n");

		// windows driver
		bw.append("public DriverObject getWinAppDriver() {\n");
		bw.append("	return new DriverObject().withWinDriver(\"" + moduleName + "\");\n");
		bw.append("}\n");

		// api driver
		bw.append("public DriverObject getApiDriver() {\n");
		bw.append("	return new DriverObject().withApiDriver(\"" + moduleName + "\");\n");
		bw.append("}\n");

		// generic driver
		bw.append("public DriverObject getGenericDriver() {\n");
		bw.append("	return new DriverObject().withGenericDriver(\"" + moduleName + "\");\n");
		bw.append("}\n");
	}

}
