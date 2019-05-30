package core.helpers;

import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.FormatKeys.MIME_AVI;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.FormatKeys.MimeTypeKey;
import static org.monte.media.VideoFormatKeys.COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.VideoFormatKeys.DepthKey;
import static org.monte.media.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.VideoFormatKeys.QualityKey;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

import core.support.logger.TestLog;
import io.appium.java_client.android.AndroidStartScreenRecordingOptions;
import io.appium.java_client.ios.IOSStartScreenRecordingOptions;
import io.appium.java_client.ios.IOSStartScreenRecordingOptions.VideoQuality;
import io.appium.java_client.ios.IOSStartScreenRecordingOptions.VideoType;

public class ScreenRecorderHelper {
	
	// should not be static. for prototyping only
	static ScreenRecorder screenRecorder = null;
	
	
	/**
	 * start screen recording
	 * works for ios, android and web
	 */
	public void startRecording() {
		TestLog.ConsoleLog("starting android screen recording");
		if(Helper.mobile.isAndroid()) {
			AndroidStartScreenRecordingOptions record = new AndroidStartScreenRecordingOptions();
			record.withTimeLimit(Duration.ofSeconds(180));
			record.withVideoSize("1024x768");
			Helper.mobile.getAndroidDriver().startRecordingScreen(record);
		}else if(Helper.mobile.isIOS()) {
			IOSStartScreenRecordingOptions record = new IOSStartScreenRecordingOptions();
			record.withTimeLimit(Duration.ofSeconds(180));
			record.withVideoQuality(VideoQuality.MEDIUM);
			record.withVideoType(VideoType.H264);
			Helper.mobile.getiOSDriver().startRecordingScreen(record);
		}else if(Helper.mobile.isWebDriver()) {
			startWebScreenRecording();
		}
	}
	
	public static void startWebScreenRecording() {
		

		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();

		// Create a instance of ScreenRecorder with the required configurations
		try {
            screenRecorder = new ScreenRecorder(gc,
                    new Format(MediaTypeKey, FormatKeys.MediaType.FILE,
                            MimeTypeKey, MIME_AVI),
                    new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
                            EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                            CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                            DepthKey, 24,
                            FrameRateKey, Rational.valueOf(15),
                            QualityKey, 1.0f,
                            KeyFrameIntervalKey, (15 * 60)),
                    new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
                            EncodingKey, "black",
                            FrameRateKey, Rational.valueOf(30)),
                    null);
            
    		screenRecorder.start();

        } catch (Exception e) {
            System.out.println("Recorder Object cannot be intialized");
        }

	}

	public void stopWebScreenRecorder() {
		if (screenRecorder != null) {
			try {
				screenRecorder.stop();
			} catch (IOException e) {
			}

			List<File> createdMovieFiles = screenRecorder.getCreatedMovieFiles();

			for (File movie : createdMovieFiles) {

				File dir = new File(Helper.getCurrentDir());
				Path path;
				try {
					path = Files.move(movie.toPath(), dir.toPath().resolve("webVideo" + ".avi"),
							StandardCopyOption.REPLACE_EXISTING);
					System.out.println("Test Recording is saved in " + path);
				} catch (IOException e) {

				}
			}
		}
	}

	/**
	 * stops screen recording
	 * works for ios, android and web
	 */
	public void stopRecording() {
		TestLog.ConsoleLog("stopping screen recording");
		if(Helper.mobile.isAndroid()) {
			String recording = Helper.mobile.getAndroidDriver().stopRecordingScreen();
			
			//Decode String To Video With mig Base64.
	        byte[] decodedBytes = Base64.decodeBase64(recording.getBytes());

	        try {
	        	
	        	File yourFile = new File(Helper.getCurrentDir() + "/Convert.mp4");
	        	yourFile.createNewFile(); // if file already exists will do nothing 
	        	FileOutputStream out = new FileOutputStream(yourFile, false); 
	            out.write(decodedBytes);
	            out.close();
	        } catch (Exception e) {
	            // TODO: handle exception
	            e.printStackTrace();

	        }
			
		}else if(Helper.mobile.isIOS()) {
			 String recording = Helper.mobile.getiOSDriver().stopRecordingScreen();
			
			//Decode String To Video With mig Base64.
	        byte[] decodedBytes = Base64.decodeBase64(recording.getBytes());

	        try {
	        	
	        	File yourFile = new File(Helper.getCurrentDir() + "/Convert.mp4");
	        	yourFile.createNewFile(); // if file already exists will do nothing 
	        	FileOutputStream out = new FileOutputStream(yourFile, false); 
	            out.write(decodedBytes);
	            out.close();
	        } catch (Exception e) {
	            // TODO: handle exception
	            e.printStackTrace();

	        }	
		}else if(Helper.mobile.isWebDriver()) {
			stopWebScreenRecorder();
		}
	}

}