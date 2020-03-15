package core.support.annotation.helper.annotationMap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
