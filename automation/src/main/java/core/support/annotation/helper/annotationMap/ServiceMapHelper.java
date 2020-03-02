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

import core.support.annotation.Service;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.PackageHelper;

public class ServiceMapHelper {

	/**
	 * creates a map of modules with list of csv data files
	 * 
	 * @param files
	 * @return
	 */
	public static Map<String, List<File>> getServiceModuleMap(List<File> files) {

		Map<String, List<File>> serviceMap = new HashMap<String, List<File>>();

		for (File file : files) {
			List<File> dataFiles = new ArrayList<File>();
			String module = PackageHelper.getModuleFromFullPath(file);

			if (serviceMap.get(module) != null) {
				dataFiles = serviceMap.get(module);
				dataFiles.add(file);
			} else {
				dataFiles.add(file);
			}
			serviceMap.put(module, dataFiles);

		}
		return serviceMap;
	}

	/**
	 * maps the module with the panels containing the Panel annotation
	 * 
	 * @param roundEnv
	 * @return
	 */
	public static Map<String, List<Element>> getServiceObjectMap(RoundEnvironment roundEnv) {

		return addElementsToServiceMap(roundEnv);
	}

	/**
	 * get initialized map, And fill in elements list with panel elements
	 * 
	 * @param roundEnv
	 * @return
	 */
	public static Map<String, List<Element>> addElementsToServiceMap(RoundEnvironment roundEnv) {
		Map<String, List<Element>> serviceMap = initializeServiceMap(roundEnv);
		String serviceName = "";
		for (Entry<String, List<Element>> entry : serviceMap.entrySet()) {
			List<Element> elements = new ArrayList<Element>();
			serviceName = entry.getKey();
			// This loop will process all the classes annotated with @Serivce
			for (Element element : roundEnv.getElementsAnnotatedWith(Service.class)) {
				if (element.getKind() == ElementKind.CLASS) {
					String currentServiceName = PackageHelper.getModuleName(element);

					if (currentServiceName.equals(entry.getKey())) {
						Logger.debug("addElementsToServiceMap: module: " + currentServiceName + " adding interface: "
								+ element.asType().toString());
						elements.add(element);
					}

				}
			}
			Logger.debug("addElementsToServiceMap: serviceName: " + serviceName + " service count: " + elements.size());
			serviceMap.put(serviceName, elements);

		}
		return serviceMap;
	}

	/**
	 * creates a map of services, But does not add the elements key: module value:
	 * classes with Panel annotation
	 * 
	 * @param roundEnv
	 * @return
	 */
	public static Map<String, List<Element>> initializeServiceMap(RoundEnvironment roundEnv) {
		Map<String, List<Element>> map = new HashMap<String, List<Element>>();
		List<Element> elements = new ArrayList<Element>();
		// This loop will process all the classes annotated with @Service
		for (Element element : roundEnv.getElementsAnnotatedWith(Service.class)) {
			if (element.getKind() == ElementKind.CLASS) {
				String serviceName = PackageHelper.getModuleName(element);
				map.put(serviceName, elements);

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
