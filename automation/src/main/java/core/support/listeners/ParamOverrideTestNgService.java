package core.support.listeners;

import java.util.Calendar;
import java.util.function.Supplier;

import org.testng.util.Strings;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.testng.TestNGService;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;

import core.support.configReader.Config;

	public class ParamOverrideTestNgService extends TestNGService {
		
		public static String ENDPOINT = "rp.endpoint";
		public static String UUID = "rp.uuid";
		public static String LAUNCH = "rp.launch";
		public static String PROJECT = "rp.project";
		public static String REPORT_PORTAL_ENABLE = "rp.enable";
		public static String DESCRIPTION = "rp.description";
		public static String CONVERT_IMAGE = "rp.convertimage";
		public static String BATCH_SIZE = "rp.batch.size.logs";
		
		public ParamOverrideTestNgService() {
			super(getLaunchOverriddenProperties());
		}

		private static Supplier<Launch> getLaunchOverriddenProperties() {
			ListenerParameters parameters = new ListenerParameters(PropertiesLoader.load());
			
			parameters.setBaseUrl(Config.getValue(ENDPOINT));
			parameters.setApiKey(Config.getValue(UUID));
			parameters.setLaunchName(Config.getValue(LAUNCH));
			parameters.setProjectName(Config.getValue(PROJECT));
			parameters.setEnable(Config.getBooleanValue(REPORT_PORTAL_ENABLE));
			parameters.setDescription(Config.getValue(DESCRIPTION));
			parameters.setConvertImage(Config.getBooleanValue(CONVERT_IMAGE));
			parameters.setBatchLogsSize(Config.getIntValue(BATCH_SIZE));

			
			ReportPortal reportPortal = ReportPortal.builder().withParameters(parameters).build();
			StartLaunchRQ rq = buildStartLaunch(reportPortal.getParameters());
			return new Supplier<Launch>() {
				@Override
				public Launch get() {
					return reportPortal.newLaunch(rq);
				}
			};
		}

		private static StartLaunchRQ buildStartLaunch(ListenerParameters parameters) {
			StartLaunchRQ rq = new StartLaunchRQ();
			rq.setName(parameters.getLaunchName());
			rq.setStartTime(Calendar.getInstance().getTime());
			rq.setAttributes(parameters.getAttributes());
			rq.setMode(parameters.getLaunchRunningMode());
			if (!Strings.isNullOrEmpty(parameters.getDescription())) {
				rq.setDescription(parameters.getDescription());
			}

			return rq;
		}
	}