package core.support.annotation.helper.annotationMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import core.support.annotation.Panel;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.PackageHelper;

public class PanelMapHelper {

	/**
	 * maps the module with the panels containing the Panel annotation
	 * 
	 * @param roundEnv
	 * @return
	 */
	public static Map<String, List<Element>> getPanelMap(RoundEnvironment roundEnv) {

		return addElementsToPanelMap(roundEnv);
	}

	/**
	 * get initialized map, And fill in elements list with panel elements
	 * 
	 * @param roundEnv
	 * @return
	 */
	public static Map<String, List<Element>> addElementsToPanelMap(RoundEnvironment roundEnv) {
		Map<String, List<Element>> panelMap = initializePanelMap(roundEnv);
		String moduleName = "";
		for (Entry<String, List<Element>> entry : panelMap.entrySet()) {
			List<Element> elements = new ArrayList<Element>();
			moduleName = entry.getKey();
			// This loop will process all the classes annotated with @Panel
			for (Element element : roundEnv.getElementsAnnotatedWith(Panel.class)) {
				if (element.getKind() == ElementKind.CLASS) {
					String currentModuleName = PackageHelper.getModuleName(element);

					if (currentModuleName.equals(entry.getKey())) {
						Logger.debug("addElementsToPanelMap: module: " + currentModuleName + " adding panel: "
								+ element.asType().toString());
						elements.add(element);
					}

				}
			}
			Logger.debug("addElementsToPanelMap: moduleName: " + moduleName + " panel count: " + elements.size());
			panelMap.put(moduleName, elements);

		}
		return panelMap;
	}

	/**
	 * creates a map of modules, But does not add the elements key: module value:
	 * classes with Panel annotation
	 * 
	 * @param roundEnv
	 * @return
	 */
	public static Map<String, List<Element>> initializePanelMap(RoundEnvironment roundEnv) {
		Map<String, List<Element>> map = new HashMap<String, List<Element>>();
		List<Element> elements = new ArrayList<Element>();
		// This loop will process all the classes annotated with @Panel
		for (Element element : roundEnv.getElementsAnnotatedWith(Panel.class)) {
			if (element.getKind() == ElementKind.CLASS) {
				String moduleName = PackageHelper.getModuleName(element);
				map.put(moduleName, elements);

			}
		}
		return map;
	}
}
