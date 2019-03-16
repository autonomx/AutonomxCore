package core.support.annotation.helper;

import java.io.BufferedWriter;
import java.io.IOException;

import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import core.support.annotation.processor.MainGenerator;


public class FileCreatorHelper {
	
	public static JavaFileObject moduleManagerFileObject = null;
	public static JavaFileObject moduleFileObject = null;
	
	/**
	 * test file to generate
	 */
	public static void defaultCreateFile() {

		try {
			JavaFileObject fileObject = MainGenerator.PROCESS_ENV.getFiler().createSourceFile("module.appManager");
			BufferedWriter bw = new BufferedWriter(fileObject.openWriter());
			bw.append("/**app manager generated code,don't modify it.\n");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	/**
	 * create module class
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public static JavaFileObject createModuleFile() throws IOException {
		moduleFileObject = MainGenerator.PROCESS_ENV.getFiler().createSourceFile(PackageHelper.ROOT_PATH + "." + PackageHelper.MODULE_CLASS);
		return moduleFileObject;
	}

	/**
	 * create manager for all modules eg. at modules file: moduleManager.java
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public static JavaFileObject createFile(String path) throws IOException {
		moduleManagerFileObject = MainGenerator.PROCESS_ENV.getFiler().createSourceFile(PackageHelper.ROOT_PATH + "." + PackageHelper.MODULE_MANAGER_CLASS);
		return moduleManagerFileObject;
	}

	/**
	 * create file for each module eg. at module.android file: androidPanel.java
	 * 
	 * @param element
	 * @return
	 * @throws IOException
	 */
	public static synchronized JavaFileObject createFile(Element element) throws IOException {
		System.out.println("createFile: " + PackageHelper.getPackagePath(element) + "." + PackageHelper.PANEL_MANAGER_CLASS);
		JavaFileObject fileObject = MainGenerator.PROCESS_ENV.getFiler()
				.createSourceFile(PackageHelper.getPackagePath(element) + "." + PackageHelper.PANEL_MANAGER_CLASS);
		// JavaFileObject fileObject = processingEnv.getFiler()
		// .createSourceFile(getPackagePath(element) + "." + getModuleName(element) +
		// "Panel");
		return fileObject;
	}


}
