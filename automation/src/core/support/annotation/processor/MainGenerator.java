/**
 * @author ehsan matean
 *
 */

package core.support.annotation.processor;

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

import com.google.auto.service.AutoService;

import core.support.annotation.helper.PanelMapHelper;
import core.support.annotation.template.dataObject.CsvDataObject;
import core.support.annotation.template.dataObject.DataClass;
import core.support.annotation.template.dataObject.ModuleClass;
import core.support.annotation.template.manager.ModuleManager;
import core.support.annotation.template.manager.PanelManager;

@SupportedAnnotationTypes(value = { "core.support.annotation.Panel" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@AutoService(javax.annotation.processing.Processor.class)
public class MainGenerator extends AbstractProcessor {

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
				
				// generate managers
				PanelManager.writePanelManagerClass(appMap);
				ModuleManager.writeModuleManagerClass(appMap);

				// generate data objects
				CsvDataObject.writeCsvDataClass();
				ModuleClass.writeModuleClass();
				DataClass.writeDataClass();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return true;
	}
}