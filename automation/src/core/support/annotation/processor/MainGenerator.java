/**
 * @author ehsan matean
 *
 */

package core.support.annotation.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import core.helpers.Helper;
import core.support.annotation.Panel;
import core.support.configReader.Config;
import core.support.objects.DriverObject;
import core.uiCore.driverProperties.driverType.DriverType;
    
@SupportedAnnotationTypes(value = { "core.support.annotation.Panel" })
public class MainGenerator extends AbstractProcessor {
	JavaFileObject moduleManagerFileObject = null;
	JavaFileObject moduleFileObject = null;

	String ROOT_PATH = "moduleManager";
	public static String MODULE_MANAGER_CLASS = "ModuleManager";
	public static String PANEL_MANAGER_CLASS = "PanelManager";

	public static String MODULE_CLASS = "ModuleBase";

	private static boolean isAnnotationRun = false;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		if(!isAnnotationRun) {
			isAnnotationRun = true;
			
			System.out.println("Annotation called");
			Map<String, List<Element>> appMap = getPanelMap(roundEnv);
			
			try {
				writePanelManagerClass(appMap);
				writeModuleManagerClass(appMap);
				//writeModuleClass(appMap);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return true;
	}

	/**
	 * test file to generate
	 */
	public void defaultCreateFile() {

		try {
			JavaFileObject fileObject = processingEnv.getFiler().createSourceFile("module.appManager");
			BufferedWriter bw = new BufferedWriter(fileObject.openWriter());
			bw.append("/**app manager generated code,don't modify it.\n");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void writeModuleClass(Map<String, List<Element>> panelMap) throws IOException {

		// proceed if app manager has not been created
		if(moduleFileObject != null) return;
		// returns module.android.panel
		String modulePath = getFirstModuleFullPath(panelMap);

		// returns module
		String rootModulePath = getRootPath(modulePath);

		// create file: module.appManager.java
		JavaFileObject fileObject = createModuleFile();

		BufferedWriter bw = new BufferedWriter(fileObject.openWriter());
		Date currentDate = new Date();
		bw.append("/**Auto generated code,don't modify it.\n");
		bw.append("* Author             ---- > Auto Generated.\n");
		bw.append("* Date  and Time     ---- > " + currentDate.toString() + "\n");
		bw.append("* Source             -----> " + fileObject.getName() + "\n");
		bw.append("*");
		bw.append("**/\n\n\n\n");
		bw.newLine();
		bw.append("package " + ROOT_PATH + ";\n");
		bw.newLine();
		bw.newLine();
		bw.append("public class "+ MODULE_CLASS +" {\n\n\n");
		bw.append("public " + MODULE_MANAGER_CLASS + " module = new " + MODULE_MANAGER_CLASS +"();\n");
		bw.append("}\n");

		bw.flush();
		bw.close();
	}


	public void writeModuleManagerClass(Map<String, List<Element>> panelMap) throws IOException {
		// returns module.android.panel
		String modulePath = getFirstModuleFullPath(panelMap);

		// returns module
		String rootModulePath = getRootPath(modulePath);

		// proceed if app manager has not been created
		if(moduleManagerFileObject != null) return;
		
		// create file: module.appManager.java
		JavaFileObject fileObject = createFile(rootModulePath);

		BufferedWriter bw = new BufferedWriter(fileObject.openWriter());
		Date currentDate = new Date();
		bw.append("/**Auto generated code,don't modify it.\n");
		bw.append("* Author             ---- > Auto Generated.\n");
		bw.append("* Date  and Time     ---- > " + currentDate.toString() + "\n");
		bw.append("* Source             -----> " + fileObject.getName() + "\n");
		bw.append("*");
		bw.append("**/\n\n\n\n");
		bw.append("package " + ROOT_PATH + ";\n");
		bw.newLine();

		// eg. import module.android.androidPanel
		/*
		for (Entry<String, List<Element>> entry : panelMap.entrySet()) {
			Element firstElement = entry.getValue().get(0);
			bw.append("import " + getPackagePath(firstElement) + "." + entry.getKey() + "Panel" + ";\n");
		}
*/
		bw.newLine();

		bw.append("public class ModuleManager {\n");

		// add panel declarations
		for (Entry<String, List<Element>> entry : panelMap.entrySet()) {
			Element firstElement = entry.getValue().get(0);
			bw.append("	public " + getPackagePath(firstElement) +"."+ PANEL_MANAGER_CLASS +" " + entry.getKey() + " = new " + getPackagePath(firstElement) +"."+ "PanelManager"+"();\n");
		}

		bw.append("}\n");

		bw.flush();
		bw.close();
	}

	/**
	 */
	public void writePanelManagerClass(Map<String, List<Element>> panelMap) throws IOException {

		System.out.println("writePanelManagerClass: panelManagers: " + panelMap.size());
		for (Entry<String, List<Element>> entry : panelMap.entrySet()) {

			Element firstElement = entry.getValue().get(0);

			System.out.println("panel name: " + entry.getKey());
			System.out.println("panel value: " + entry.getValue().get(0).asType().toString());

			
			JavaFileObject fileObject = createFile(firstElement);

			BufferedWriter bw = new BufferedWriter(fileObject.openWriter());
			Date currentDate = new Date();
			bw.append("/**Auto generated code,don't modify it.\n");
			bw.append("* Author             ---- > Auto Generated.\n");
			bw.append("* Date  and Time     ---- > " + currentDate.toString() + "\n");
			bw.append("* Source             -----> " + fileObject.getName() + "\n");
			bw.append("*");
			bw.append("**/\n\n\n\n");
			bw.append("package " + getPackagePath(firstElement) + ";\n");
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

				bw.append("public " + panelName + " " + panelName.replace("Panel", "").toLowerCase() + " = new " + panelName + "(this);\n");
			}
			bw.newLine();
			
			writeDrivers(getModuleName(firstElement), bw);

			bw.append("}\n");

			bw.flush();
			bw.close();
            System.out.println("completed: " +  entry.getKey());
		}
	}
	
	/**
	public DriverObject getWebDriver() {
		return new DriverObject().withWebDriver("webApp", Config.getValue("webApp"));
	}
	
		public DriverObject getWebDriver(String url) {
		return new DriverObject().withWebDriver("webApp", Config.getValue(url));
	}
	
	public DriverObject getIosTabletDriver() {
		return new DriverObject().withiOSDriver("iosTablet");
	}
	
	public DriverObject getIosMobileDriver() {
		return new DriverObject().withiOSDriver("iosMobile");
	}
	
	public DriverObject getAndroidTabletDriver() {
		return new DriverObject().withiOSDriver("androidTablet");
	}
	
	public DriverObject getAndroidMobileDriver() {
		return new DriverObject().withiOSDriver("androidMobile");
	}
	
	public DriverObject getWinAppDriver() {
		return new DriverObject().withWinDriver();
	}	
	
	public DriverObject getApiDriver() {
		return new DriverObject().withDriverType(DriverType.API);
	}
	
	public DriverObject getGenericDriver() {
		return new DriverObject().withDriverType(DriverType.API);
	}
	 * @throws IOException 
	*/
	
	public void writeDrivers(String moduleName, BufferedWriter bw) throws IOException {
		
		// web driver
		bw.append("public DriverObject getWebDriver() {\n");
		bw.append("	return new DriverObject().withWebDriver(\""+ moduleName +"\", Config.getValue(\""+ moduleName +"\"));\n");
		bw.append("}\n");
		
		// web driver
		bw.append("public DriverObject getWebDriver(String url) {\n");
		bw.append("	return new DriverObject().withWebDriver(\""+ moduleName +"\", Config.getValue(url));\n");
		bw.append("}\n");
		
		// ios tablet driver
		bw.append("public DriverObject getIosTabletDriver() {\n");
		bw.append("	return new DriverObject().withiOSDriver(\"" + moduleName + "\",\"iosTablet\");\n");
		bw.append("}\n");
		
		// ios mobile driver
		bw.append("public DriverObject getIosMobileDriver() {\n");
		bw.append("	return new DriverObject().withiOSDriver(\""+ moduleName +"\",\"iosMobile\");\n");
		bw.append("}\n");
		
		// andorid tablet driver
		bw.append("public DriverObject getAndroidTabletDriver() {\n");
		bw.append("	return new DriverObject().withAndroidDriver(\""+ moduleName +"\",\"androidTablet\");\n");
		bw.append("}\n");
		
		// android mobile driver
		bw.append("public DriverObject getAndroidMobileDriver() {\n");
		bw.append("	return new DriverObject().withAndroidDriver(\""+ moduleName +"\",\"androidMobile\");\n");
		bw.append("}\n");
		
		// windows driver
		bw.append("public DriverObject getWinAppDriver() {\n");
		bw.append("	return new DriverObject().withWinDriver(\""+ moduleName +"\");\n");
		bw.append("}\n");
		
		// api driver
		bw.append("public DriverObject getApiDriver() {\n");
		bw.append("	return new DriverObject().withApiDriver(\""+ moduleName +"\");\n");
		bw.append("}\n");
		
		// generic driver
		bw.append("public DriverObject getGenericDriver() {\n");
		bw.append("	return new DriverObject().withGenericDriver(\""+ moduleName +"\");\n");
		bw.append("}\n");
	}

	/**
	 * maps the module with the panels containing the Panel annotation
	 * 
	 * @param roundEnv
	 * @return
	 */
	public Map<String, List<Element>> getPanelMap(RoundEnvironment roundEnv) {
		
		return addElementsToPanelMap(roundEnv);
	}
	
	/**
	 * get initialized map, and fill in elements list with panel elements
	 * @param roundEnv
	 * @return
	 */
	public Map<String, List<Element>> addElementsToPanelMap(RoundEnvironment roundEnv) {
		Map<String, List<Element>> panelMap = initializePanelMap(roundEnv);
		String moduleName = "";
		for (Entry<String, List<Element>> entry : panelMap.entrySet()) {
			List<Element> elements = new ArrayList<Element>();
			moduleName = entry.getKey();
		// This loop will process all the classes annotated with @Panel
			for (Element element : roundEnv.getElementsAnnotatedWith(Panel.class)) {
				if (element.getKind() == ElementKind.CLASS) {
					String currentModuleName = getModuleName(element);
					
					if(currentModuleName.equals(entry.getKey())) {
						System.out.println("addElementsToPanelMap: module: " + currentModuleName + " adding panel: " + element.asType().toString());
						elements.add(element);
					}
	
				 }
			}
			System.out.println("addElementsToPanelMap: moduleName: " + moduleName + " panel count: " + elements.size() );
			panelMap.put(moduleName, elements);

		}
		return panelMap;
	}
	
	/**
	 * creates a map of modules, but does not add the elements 
	 * @param roundEnv
	 * @return
	 */
	public Map<String, List<Element>> initializePanelMap(RoundEnvironment roundEnv) {
		Map<String, List<Element>> map = new HashMap<String, List<Element>>();
		List<Element> elements = new ArrayList<Element>();
		// This loop will process all the classes annotated with @Panel
		for (Element element : roundEnv.getElementsAnnotatedWith(Panel.class)) {
			if (element.getKind() == ElementKind.CLASS) {
				String moduleName = getModuleName(element);
				map.put(moduleName, elements);

			}
		}
		return map;
	}
	


	/**
	 * gets module name. eg. module.android.LoginPanel with return android
	 * 
	 * @param element
	 * @return
	 */
	public String getModuleName(Element element) {
		String sourceClass = element.asType().toString();
		String module = sourceClass.split("\\.")[1];
		return module;
	}

	public String getPackagePath(Element element) {
		String sourceClass = element.asType().toString();
		String packagePath = ROOT_PATH + "." + sourceClass.split("\\.")[0] + "." + sourceClass.split("\\.")[1];
		return packagePath;
	}

	/**
	 * path: module.android.panel
	 * 
	 * @param path
	 * @return "module"
	 */
	public String getRootPath(String path) {
		return path.split("\\.")[0];
	}

	public String getAppName(Element element) {
		String sourceClass = element.asType().toString();
		String appName = sourceClass.split("\\.")[1];
		return appName;
	}

	/**
	 * gets the full path of the first module eg. module.android.panel
	 * 
	 * @param panelMap
	 * @return
	 */
	public String getFirstModuleFullPath(Map<String, List<Element>> panelMap) {
		String sourceClass = "";
		for (Entry<String, List<Element>> entry : panelMap.entrySet()) {

			Element firstElement = entry.getValue().get(0);
			sourceClass = firstElement.asType().toString();
			break;
		}
		return sourceClass;
	}
	
	/**
	 * create module class
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public JavaFileObject createModuleFile() throws IOException {
		moduleFileObject = processingEnv.getFiler().createSourceFile(ROOT_PATH + "." + MODULE_CLASS);
		return moduleFileObject;
	}

	/**
	 * create manager for all modules eg. at modules file: moduleManager.java
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public JavaFileObject createFile(String path) throws IOException {
		moduleManagerFileObject = processingEnv.getFiler().createSourceFile(ROOT_PATH + "." + MODULE_MANAGER_CLASS);
		return moduleManagerFileObject;
	}

	/**
	 * create file for each module eg. at module.android file: androidPanel.java
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public synchronized JavaFileObject createFile(Element element) throws IOException {
		System.out.println("creating Panel manager: " + getPackagePath(element) + "." + PANEL_MANAGER_CLASS);
		JavaFileObject fileObject = processingEnv.getFiler()
				.createSourceFile(getPackagePath(element) + "." + PANEL_MANAGER_CLASS);
//		JavaFileObject fileObject = processingEnv.getFiler()
//				.createSourceFile(getPackagePath(element) + "." + getModuleName(element) + "Panel");
		return fileObject;
	}

}