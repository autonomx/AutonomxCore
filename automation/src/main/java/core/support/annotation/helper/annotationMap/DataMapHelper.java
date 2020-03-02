package core.support.annotation.helper.annotationMap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import core.support.annotation.Data;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.PackageHelper;

public class DataMapHelper {

	/**
	 * creates a map of modules with list of csv data files
	 * 
	 * @param files
	 * @return
	 */
	public static Map<String, List<File>> getDataModuleMap(List<File> files) {

		Map<String, List<File>> moduleMap = new HashMap<String, List<File>>();

		for (File file : files) {
			List<File> dataFiles = new ArrayList<File>();
			String module = PackageHelper.getModuleFromFullPath(file);

			if (moduleMap.get(module) != null) {
				dataFiles = moduleMap.get(module);
				dataFiles.add(file);
			} else {
				dataFiles.add(file);
			}
			moduleMap.put(module, dataFiles);

		}
		return moduleMap;
	}

	/**
	 * maps the module with the panels containing the Panel annotation
	 * 
	 * @param roundEnv
	 * @return
	 */
	public static Map<String, List<Element>> getDataObjectMap(RoundEnvironment roundEnv) {

		return addElementsToDataMap(roundEnv);
	}

	/**
	 * get initialized map, And fill in elements list with panel elements
	 * 
	 * @param roundEnv
	 * @return
	 */
	public static Map<String, List<Element>> addElementsToDataMap(RoundEnvironment roundEnv) {
		Map<String, List<Element>> dataMap = initializePanelMap(roundEnv);
		String moduleName = "";
		for (Entry<String, List<Element>> entry : dataMap.entrySet()) {
			List<Element> elements = new ArrayList<Element>();
			moduleName = entry.getKey();
			// This loop will process all the classes annotated with @Data
			for (Element element : roundEnv.getElementsAnnotatedWith(Data.class)) {
				if (element.getKind() == ElementKind.CLASS) {
					String currentModuleName = PackageHelper.getModuleName(element);

					if (currentModuleName.equals(entry.getKey())) {
						Logger.debug("addElementsToDataMap: module: " + currentModuleName + " adding panel: "
								+ element.asType().toString());
						elements.add(element);
					}

				}
			}
			Logger.debug("addElementsToDataMap: moduleName: " + moduleName + " panel count: " + elements.size());
			dataMap.put(moduleName, elements);

		}
		return dataMap;
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
		// This loop will process all the classes annotated with @Data
		for (Element element : roundEnv.getElementsAnnotatedWith(Data.class)) {
			if (element.getKind() == ElementKind.CLASS) {
				String moduleName = PackageHelper.getModuleName(element);
				map.put(moduleName, elements);

			}
		}
		return map;
	}

	public static Map<String, List<String>> mergeMaps(Map<String, List<String>> mapA, Map<String, List<String>> mapB) {
		Map<String, List<String>> map = new HashMap<>();
		map.putAll(mapA);

		mapB.forEach((key, value) -> {
			// Get the value for key in map.
			List<String> list = map.get(key);
			if (list == null) {
				map.put(key, value);
			} else {
				// Merge two list together
				ArrayList<String> mergedValue = new ArrayList<>(value);
				mergedValue.addAll(list);
				map.put(key, mergedValue);
			}
		});
		return map;
	}
}
