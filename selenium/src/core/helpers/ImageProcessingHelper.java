package core.helpers;

import java.io.File;

import org.sikuli.script.FindFailed;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;

import core.support.configReader.Config;
import core.uiCore.drivers.AbstractDriver;
import core.uiCore.webElement.EnhancedBy;
import net.sourceforge.tess4j.Tesseract;

// sikuli ide: https://launchpad.net/sikuli  
public class ImageProcessingHelper {
	// sikuli is desktop level and does not handle parallel run
	static final Object ai = new Object();

	public static final String IMAGE_PATH = "testImages/";
	
	/**
	 * click based on image using Sikuli path base is resource folder
	 * 
	 * @param path
	 * @param proximity
	 */
	public void clickImage(String path) {
		synchronized (ai) {
			clickImage(path, (float) 0.9);
		}
	}

	/**
	 * click based on image using Sikuli, using similarity proximity path base is
	 * resource folder sikuli ide: https://launchpad.net/sikuli
	 * 
	 * @param path
	 * @param proximity
	 */
	public void clickImage(String path, float proximity) {
		synchronized (ai) {
			Helper.page.bringPageToFront();
			path = Config.RESOURCE_PATH + path;
			Screen screen = new Screen();
			try {
				screen.wait(new Pattern(path).similar((float) proximity), AbstractDriver.TIMEOUT_SECONDS);
				screen.click(path);
			} catch (FindFailed e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * doubleclick based on image using Sikuli path base is resource folder sikuli
	 * ide: https://launchpad.net/sikuli
	 * 
	 * @param path
	 * @param proximity
	 */
	public void doubleClickImage(String path) {
		synchronized (ai) {
			doubleClickImage(path, (float) 0.9);
		}
	}

	/**
	 * double click based on image using Sikuli, using similarity proximity path
	 * base is resource folder sikuli ide: https://launchpad.net/sikuli
	 * 
	 * @param path
	 * @param proximity
	 */
	public void doubleClickImage(String path, float proximity) {
		synchronized (ai) {
			Helper.page.bringPageToFront();
			path = Config.RESOURCE_PATH + path;
			Screen screen = new Screen();
			try {
				screen.wait(new Pattern(path).similar((float) proximity), AbstractDriver.TIMEOUT_SECONDS);
				screen.doubleClick(path);
			} catch (FindFailed e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * hover based on image using Sikuli path base is resource folder sikuli ide:
	 * https://launchpad.net/sikuli
	 * 
	 * @param path
	 * @param proximity
	 */
	public void hover(String path) {
		synchronized (ai) {
			hover(path, (float) 0.9);
		}
	}

	/**
	 * hover based on image using Sikuli, using similarity proximity path base is
	 * resource folder sikuli ide: https://launchpad.net/sikuli
	 * 
	 * @param path
	 * @param proximity
	 */
	public void hover(String path, float proximity) {
		synchronized (ai) {
			Helper.page.bringPageToFront();
			path = Config.RESOURCE_PATH + path;
			Screen screen = new Screen();
			try {
				screen.wait(new Pattern(path).similar((float) proximity), AbstractDriver.TIMEOUT_SECONDS);
				screen.hover(path);
			} catch (FindFailed e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * drag and drop based on image using Sikuli path base is resource folder sikuli
	 * ide: https://launchpad.net/sikuli
	 * 
	 * @param path
	 * @param proximity
	 */
	public void dragAndDrop(String pathSource, String pathDestination) {
		synchronized (ai) {
			dragAndDrop(pathSource, pathDestination, (float) 0.9);
		}
	}

	/**
	 * drag and drop based on image, using similarity proximity path base is
	 * resource folder sikuli ide: https://launchpad.net/sikuli
	 * 
	 * @param path
	 * @param proximity
	 */
	public void dragAndDrop(String pathSource, String pathDestination, float proximity) {
		synchronized (ai) {
			Helper.page.bringPageToFront();
			pathSource = Config.RESOURCE_PATH + pathSource;
			pathSource = Config.RESOURCE_PATH + pathSource;

			Screen screen = new Screen();
			try {
				screen.wait(new Pattern(pathSource).similar((float) proximity), AbstractDriver.TIMEOUT_SECONDS);
				screen.dragDrop(pathSource, pathDestination);
			} catch (FindFailed e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * type text based on image using Sikuli path base is resource folder sikuli
	 * ide: https://launchpad.net/sikuli
	 * 
	 * @param path
	 * @param proximity
	 */
	public void typeText(String pathSource, String text) {
		synchronized (ai) {
			typeText(pathSource, text, (float) 0.9);
		}
	}

	/**
	 * type text based on image using Sikuli, using similarity proximity path base
	 * is resource folder sikuli ide: https://launchpad.net/sikuli
	 * 
	 * @param path
	 * @param proximity
	 */
	public void typeText(String pathSource, String text, float proximity) {
		synchronized (ai) {
			Helper.page.bringPageToFront();
			pathSource = Config.RESOURCE_PATH + pathSource;
			pathSource = Config.RESOURCE_PATH + pathSource;

			Screen screen = new Screen();
			try {
				screen.wait(new Pattern(pathSource).similar((float) proximity), AbstractDriver.TIMEOUT_SECONDS);
				screen.type(pathSource, text);
			} catch (FindFailed e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * get text from image image path base from resource folder using Tesseract
	 * library
	 * 
	 * @param sourcePath
	 * @return
	 */
	public String getTextFromImage(String sourcePath) {
		synchronized (ai) {
			Helper.page.bringPageToFront();
			File image = new File(Config.RESOURCE_PATH + sourcePath);
			Tesseract tessInst = new Tesseract();
			tessInst.setDatapath(Config.RESOURCE_PATH + IMAGE_PATH);
			try {
				String value = tessInst.doOCR(image).trim();
				value = Helper.stringRemoveLines(value);
				return value.trim();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}
	}

	/**
	 * get text from image element screenshot take of the element, then Tesseract is
	 * used to grab the string from the image image path base from resource folder
	 * using Tesseract library
	 * 
	 * @param sourcePath
	 * @return
	 */
	public String getTextFromElementImage(EnhancedBy element) {
		synchronized (ai) {
			Helper.page.bringPageToFront();
			// get and capture the picture of the img element used to display the barcode
			// image
			File image = UtilityHelper.captureElementPicture(element);

			Tesseract tessInst = new Tesseract();
			tessInst.setDatapath(Config.RESOURCE_PATH + IMAGE_PATH);
			try {
				String value = tessInst.doOCR(image).trim();
				value = Helper.stringRemoveLines(value);
				return value.trim();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "";
		}
	}
}