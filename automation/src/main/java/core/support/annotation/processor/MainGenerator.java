/**
 * @author ehsan matean
 *
 */

package core.support.annotation.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.apache.commons.lang.StringUtils;

import com.google.auto.service.AutoService;

import core.helpers.Helper;
import core.support.annotation.helper.FileCreatorHelper;
import core.support.annotation.helper.Logger;
import core.support.annotation.helper.annotationMap.AnnotationObject;
import core.support.annotation.helper.annotationMap.ModuleMapHelper;
import core.support.annotation.template.config.ConfigManager;
import core.support.annotation.template.config.ConfigVariableGenerator;
import core.support.annotation.template.dataObject.CsvDataObject;
import core.support.annotation.template.dataObject.DataClass;
import core.support.annotation.template.dataObject.ModuleClass;
import core.support.annotation.template.manager.ModuleBase;
import core.support.annotation.template.manager.ModuleManager;
import core.support.annotation.template.manager.PanelManagerGenerator;
import core.support.annotation.template.manager.sourceChangeDetector;
import core.support.annotation.template.service.Service;
import core.support.annotation.template.service.ServiceClass;
import core.support.annotation.template.service.ServiceData;
import core.support.annotation.template.service.ServiceRunner;
import core.support.configReader.PropertiesReader;


@SupportedAnnotationTypes(value = { "core.support.annotation.Module"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@AutoService(javax.annotation.processing.Processor.class)
public class MainGenerator extends AbstractProcessor {

	private static boolean isAnnotationRun = false;
	public static ProcessingEnvironment PROCESS_ENV;
	public static String ANNOATION_WORKING_DIR = StringUtils.EMPTY;

	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		
		PROCESS_ENV = processingEnv;
		
		// set working directory
		setRootWorkingDirectory(processingEnv);
		
		return runAnnotation();
	}
	
	public static boolean runAnnotation() {
		if (!isAnnotationRun) {
			isAnnotationRun = true;
			

			Logger.debug("Annotation called");

			// disable console logging
			disableConsoleLogging();
			
			// map of modules and class with @Panel annotation
			AnnotationObject annotation = new AnnotationObject().panel();
			Map<String, List<String>> panelMap = ModuleMapHelper.getModuleMap(annotation);

			// map of modules and classes with @Data annotation
			annotation = new AnnotationObject().data();
			Map<String, List<String>> dataMap = ModuleMapHelper.getModuleMap(annotation);

			// map of modules and classes with @Service annotation
			annotation = new AnnotationObject().service();
			Map<String, List<String>> serviceMap = ModuleMapHelper.getModuleMap(annotation);

			// print out the map
			for (Entry<String, List<String>> entry : dataMap.entrySet()) {
				Logger.debug("module map: module: " + entry.getKey());
				Logger.debug("module map: paths: " + Arrays.toString(entry.getValue().toArray())) ;

			}

			// generate managers
			PanelManagerGenerator.writePanelManagerClass(panelMap);
			ModuleManager.writeModuleManagerClass(panelMap);
			ModuleBase.writeModuleBaseClass(panelMap);

			// generate data objects
			CsvDataObject.writeCsvDataClass();
			ModuleClass.writeModuleClass(dataMap);
			DataClass.writeDataClass(dataMap);

			// generate service objects
			Service.writeServiceClass();
			ServiceData.writeServiceDataClass();
			ServiceRunner.writeServiceClass(serviceMap);

			// generate config objects
			ConfigManager.writeConfigManagerClass();
			ConfigVariableGenerator.writeConfigVariableClass();
			
			sourceChangeDetector.writeModuleBaseClass();
			
			// generate service test csv to class files
			ServiceClass.writeServiceGenerationClass();
			
			// create marker class
			createMarkerClass();

			System.out.println("Annotation generation complete");
		}
		return true;
	}
	

	/**
	 * files created: src_dir.txt, marker.marker
	 * a marker class is to indicate when the generated files have been created used
	 * for comparison with the class files. if class files are newer, than the
	 * marker class, then regenerate the code
	 */
	protected static void createMarkerClass() {
		try {
			createFileList("src" + File.separator + "main", "src_dir", false);
			createFileList("resources", "src_dir", true);

			JavaFileObject fileObject = FileCreatorHelper.createMarkerFile();
			BufferedWriter bw = new BufferedWriter(fileObject.openWriter());

			bw.append("/**Auto generated code,don't modify it. */ \n");
			bw.append("package marker;");
			bw.append("public class marker {}");
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * creates text file with the list of files in sourceDir used for file change
	 * detection
	 * 
	 * @param sourceDir
	 * @param fileName
	 */
	private static void createFileList(String sourceDir, String fileName, boolean isAppend) {
		File Directory = new File(Helper.getFullPath(sourceDir));
		ArrayList<String> fileList = PropertiesReader.getAllFiles(Directory);
		String listString = String.join(",", fileList);
		if (isAppend)
			Helper.appendToFile("," + listString, "target/generated-sources", fileName, "txt");
		else
			Helper.writeFile(listString, "target/generated-sources", fileName, "txt");
	}
	
	
	private static void setRootWorkingDirectory(ProcessingEnvironment processingEnv) {
		if(!ANNOATION_WORKING_DIR.isEmpty())
			return;
		
		Filer filer = processingEnv.getFiler();
		try {
			FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "tmp", (Element[]) null);
			Path projectPath = Paths.get(resource.toUri()).getParent().getParent();
			resource.delete();
			File workingDir = getRootPath_reverseNavigation(projectPath.toFile(), "pom.xml");
			ANNOATION_WORKING_DIR = workingDir.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * navigates backwards from dir location to find the directory where file name is located
	 * @param dir
	 * @param name
	 * @return
	 */
	public static File getRootPath_reverseNavigation(File dir, String name) {
		boolean isFound = false;
		
		do {
			if(dir == null)
				return null;
		
			if(dir.isFile() && dir.getName().contains(name)) 
				return dir;
			
			
			File[] files = dir.listFiles();
			if(files == null || files.length == 0) {
				dir = dir.getParentFile();
				continue;
			}
			
			for (File file : files) {
				if (file.getName().contains(name))
					return dir;
			}
			dir = dir.getParentFile();
		}while(!isFound);
		
		return null;
	}
	
	/**
	 * disable console log for annotation generation 
	 * if annotations are running, without compilation error, then console log will be disabled
	 */
	public static void disableConsoleLogging() {
		File disabledLog = new File(Helper.getFullPath(".externalToolBuilders" +  File.separator + "annotation_generator_disableLog.launch"));
		File log = new File(Helper.getFullPath(".externalToolBuilders" +  File.separator + "annotation_generator.launch"));

		if(!disabledLog.exists() || !log.exists())
			return;
		
		Path from = disabledLog.toPath(); //convert from File to Path
		Path to = log.toPath(); //convert from String to Path
		
		try {
			Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}