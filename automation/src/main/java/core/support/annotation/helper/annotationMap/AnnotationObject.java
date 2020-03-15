package core.support.annotation.helper.annotationMap;

import org.apache.commons.lang.StringUtils;

public class AnnotationObject {
	
	public String annotation = StringUtils.EMPTY;
	public String importPath = StringUtils.EMPTY;
	public String parentFolder = StringUtils.EMPTY;

	
	public AnnotationObject panel() {
		AnnotationObject panel = new AnnotationObject();
		panel.annotation = "@Panel";
		panel.importPath = "core.support.annotation.Panel";
		panel.parentFolder = "panel";
		return panel;	
	}
	
	public AnnotationObject data() {
		AnnotationObject panel = new AnnotationObject();
		panel.annotation = "@Data";
		panel.importPath = "core.support.annotation.Data";
		panel.parentFolder = "data";
		return panel;	
	}
	
	public AnnotationObject service() {
		AnnotationObject panel = new AnnotationObject();
		panel.annotation = "@Service";
		panel.importPath = "core.support.annotation.Service";
		panel.parentFolder = "interfaces";
		return panel;	
	}
}
