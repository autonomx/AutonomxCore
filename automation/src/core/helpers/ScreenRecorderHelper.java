package core.helpers;

import static org.monte.media.AudioFormatKeys.ByteOrderKey;
import static org.monte.media.AudioFormatKeys.ChannelsKey;
import static org.monte.media.AudioFormatKeys.ENCODING_QUICKTIME_TWOS_PCM;
import static org.monte.media.AudioFormatKeys.SampleRateKey;
import static org.monte.media.AudioFormatKeys.SampleSizeInBitsKey;
import static org.monte.media.AudioFormatKeys.SignedKey;
import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.FormatKeys.MimeTypeKey;
import static org.monte.media.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.VideoFormatKeys.DepthKey;
import static org.monte.media.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.VideoFormatKeys.QualityKey;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.codec.binary.Base64;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.VideoFormatKeys;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

import core.support.configReader.Config;
import core.support.logger.ExtentManager;
import core.support.logger.TestLog;
import core.support.objects.TestObject;
import io.appium.java_client.android.AndroidStartScreenRecordingOptions;
import io.appium.java_client.ios.IOSStartScreenRecordingOptions;
import io.appium.java_client.ios.IOSStartScreenRecordingOptions.VideoQuality;
import io.appium.java_client.ios.IOSStartScreenRecordingOptions.VideoType;

public class ScreenRecorderHelper {
	
	public static final String RECORDER_ENABLE_RECORDING = "recorder.enableRecording";
	public static final String RECORDER_ON_FAIL_TEST_ONLY = "recorder.onFailedTestsOnly";

	public static final String RECORDER_MAX_TIME_SECONDS = "recorder.maxTimeInSeconds";
	public static final String RECORDER_ANDROID_VIDEO_SIZE = "recorder.android.videoSize";
	public static final String RECORDER_IOS_QUALITY = "recorder.ios.quality";
	public static final String RECORDER_IOS_TYPE = "recorder.ios.type";

	
	/**
	 * start screen recording
	 * works for ios, android and web
	 */
	public static void startRecording() {
		
		// return if enableRecording is false
		if(!Config.getBooleanValue(RECORDER_ENABLE_RECORDING))
			return;
		
		TestLog.ConsoleLog("starting screen recording");
		
		int maxTime = Config.getIntValue(RECORDER_MAX_TIME_SECONDS);
		String androidVideoSize = Config.getValue(RECORDER_ANDROID_VIDEO_SIZE);
		VideoQuality iosQuality = VideoQuality.valueOf(Config.getValue(RECORDER_IOS_QUALITY));

		if(Helper.mobile.isAndroid()) {
			AndroidStartScreenRecordingOptions record = new AndroidStartScreenRecordingOptions();
			record.withTimeLimit(Duration.ofSeconds(maxTime)); 
			record.withVideoSize(androidVideoSize);
			Helper.mobile.getAndroidDriver().startRecordingScreen(record);
		}else if(Helper.mobile.isIOS()) {
			IOSStartScreenRecordingOptions record = new IOSStartScreenRecordingOptions();
			record.withTimeLimit(Duration.ofSeconds(maxTime));
			record.withVideoQuality(iosQuality);
			record.withVideoType(VideoType.H264);
			Helper.mobile.getiOSDriver().startRecordingScreen(record);
		}else if(Helper.mobile.isWebDriver()) {
			startWebScreenRecording();
		}
	}
	
	/**
	 * stops screen recording
	 * works for ios, android and web
	 */
	public static void stopRecording() {

		// return if enableRecording is false or record on fail test only is true
		if (!Config.getBooleanValue(RECORDER_ENABLE_RECORDING) || Config.getBooleanValue(RECORDER_ON_FAIL_TEST_ONLY))
			return;

		TestLog.ConsoleLog("stopping screen recording");

		if (Helper.mobile.isMobile()) {
			stopMobileScreenRecorder();
		} else if (Helper.mobile.isWebDriver()) {
			stopWebScreenRecorder();
		}
	}
	
	private static void startWebScreenRecording() {
		

		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration();

		// Create a instance of ScreenRecorder with the required configurations
		try {
            TestObject.getTestInfo().screenRecorder = new ScreenRecorder(gc, new Format(
            		MediaTypeKey, MediaType.FILE, MimeTypeKey, FormatKeys.MIME_QUICKTIME),
            		new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey,
            		VideoFormatKeys.ENCODING_QUICKTIME_ANIMATION,
            		CompressorNameKey,
            		ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey,
            		24, FrameRateKey, Rational.valueOf(15), QualityKey,
            		1.0f, KeyFrameIntervalKey, 15 * 60), new Format(
            		MediaTypeKey, MediaType.VIDEO, EncodingKey,
            		"black", FrameRateKey, Rational.valueOf(30)), new Format(MediaTypeKey, MediaType.AUDIO,
            		EncodingKey, ENCODING_QUICKTIME_TWOS_PCM,
            		FrameRateKey, new Rational(48000, 1),
            		SampleSizeInBitsKey, 16,
            		ChannelsKey, 2, SampleRateKey, new Rational(48000, 1),
            		SignedKey, true, ByteOrderKey, ByteOrder.BIG_ENDIAN));
			
//            TestObject.getTestInfo().screenRecorder = new ScreenRecorder(gc, null,
//                    // the file format
//                    new Format(MediaTypeKey, MediaType.FILE,
//                    MimeTypeKey, FormatKeys.MIME_QUICKTIME),
//                    //
//                    // the output format for screen capture
//                    new Format(MediaTypeKey, MediaType.VIDEO,
//                    EncodingKey, VideoFormatKeys.ENCODING_QUICKTIME_ANIMATION,
//                    CompressorNameKey, VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_ANIMATION,
//                    DepthKey, 24, FrameRateKey, new Rational(15, 1)),
//                    //
//                    // the output format for mouse capture 
//                    new Format(MediaTypeKey, MediaType.VIDEO,
//                    EncodingKey, "black",
//                    FrameRateKey, new Rational(30, 1)),
//                    //
//                    // the output format for audio capture 
//                    new Format(MediaTypeKey, MediaType.AUDIO,
//                    EncodingKey, ENCODING_QUICKTIME_TWOS_PCM,
//                    FrameRateKey, new Rational(48000, 1),
//                    SampleSizeInBitsKey, 16,
//                    ChannelsKey, 2, SampleRateKey, new Rational(48000, 1),
//                    SignedKey, true, ByteOrderKey, ByteOrder.BIG_ENDIAN),
//                    null);
			
//            TestObject.getTestInfo().screenRecorder = new ScreenRecorder(gc,
//                    new Format(MediaTypeKey, FormatKeys.MediaType.FILE,
//                            MimeTypeKey, MIME_AVI),
//                    new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
//                            EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
//                            CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
//                            DepthKey, 24,
//                            FrameRateKey, Rational.valueOf(15),
//                            QualityKey, 1.0f,
//                            KeyFrameIntervalKey, (15 * 60)),
//                    new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
//                            EncodingKey, "black",
//                            FrameRateKey, Rational.valueOf(30)),
//                    null);
            
            TestObject.getTestInfo().screenRecorder.start();

        } catch (Exception e) {
            TestLog.ConsoleLog("Recorder Object cannot be intialized " + e);
        }

	}
	
	private static void stopMobileScreenRecorder() {
		String recording = null;
		if (Helper.mobile.isAndroid())
			recording = Helper.mobile.getAndroidDriver().stopRecordingScreen();
		else if (Helper.mobile.isIOS()) 
			recording = Helper.mobile.getiOSDriver().stopRecordingScreen();
		
			// Decode String To Video With mig Base64.
			byte[] decodedBytes = Base64.decodeBase64(recording.getBytes());

			try {
				String mediaName = getMediaName() + ".mp4";
				
				// relative path for extent report attachment
				String extentMediaRelativePathFromReport = ExtentManager.getMediaFolderRelativePathFromHtmlReport()
						+ mediaName;
				
				// full path for file creation
				String extentReportImageFullPath = ExtentManager.getMediaFolderFullPath()
						+ mediaName;
				
				File media = new File(extentReportImageFullPath);

				// create directories and files in path
				Helper.createFileFromPath(media.getAbsolutePath());
				
				// save to file
				FileOutputStream out = new FileOutputStream(media, false);
				out.write(decodedBytes);
				out.close();
				TestLog.ConsoleLog("Test Recording is saved at: " + extentReportImageFullPath);
				
				// attach video to extent report
				TestLog.attachVideoLog(extentMediaRelativePathFromReport, true);

			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	/**
	 * if screen recorder is active, stop recording
	 */
	private static void stopWebScreenRecorder() {
		if (TestObject.getTestInfo().screenRecorder != null) {
			try {
				TestObject.getTestInfo().screenRecorder.stop();

				List<File> createdMovieFiles = TestObject.getTestInfo().screenRecorder.getCreatedMovieFiles();
				

				for (File movie : createdMovieFiles) {
	
					File dir = new File(ExtentManager.getMediaFolderFullPath()) ;
					dir.mkdirs(); // if dir already exists will do nothing
					
					String mediaName = getMediaName() + ".mov";
					try {
						Path path = Files.move(movie.toPath(), dir.toPath().resolve(mediaName),
								StandardCopyOption.REPLACE_EXISTING);
						TestLog.ConsoleLog("Test Recording is saved at: " + path);
						
						String extentReportImageRelativePath = ExtentManager.getMediaFolderRelativePathFromHtmlReport()
								+ mediaName;
						
						// attach video to extent report
						TestLog.attachVideoLog(extentReportImageRelativePath, true);

					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {

			}
		}
	}
	
	/**
	 * gets the video name
	 * @return
	 */
	private static String getMediaName() {
		String format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss", Locale.ENGLISH).format(new Date());
		String fileName = TestObject.getTestInfo().testName + "-" + format;
		return fileName;
	}
}