/**
 * @author ehsan matean
 *
 */

package core.support.annotation.processor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import com.google.auto.service.AutoService;

import core.support.annotation.helper.PanelMapHelper;
import core.support.annotation.template.ModuleManager;
import core.support.annotation.template.PanelManager;

@SupportedAnnotationTypes(value = { "core.support.annotation.Panel" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@AutoService(javax.annotation.processing.Processor.class)
public class MainGenerator extends AbstractProcessor {
	
	JavaFileObject moduleManagerFileObject = null;
	JavaFileObject moduleFileObject = null;

	String ROOT_PATH = "moduleManager";
	public static String MODULE_MANAGER_CLASS = "ModuleManager";
	public static String PANEL_MANAGER_CLASS = "PanelManager";

	public static String MODULE_CLASS = "ModuleBase";

	private static boolean isAnnotationRun = false;
	public static ProcessingEnvironment PROCESS_ENV;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		if (!isAnnotationRun) {
			isAnnotationRun = true;
			
			PROCESS_ENV = processingEnv;

			System.out.println("Annotation called");
			Map<String, List<Element>> appMap = PanelMapHelper.getPanelMap(roundEnv);

			try {
				PanelManager.writePanelManagerClass(appMap);
				ModuleManager.writeModuleManagerClass(appMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return true;
	}

	
}