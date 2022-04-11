package core.support.listeners;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.testng.util.Strings;

import com.epam.reportportal.listeners.ListenerParameters;
import com.epam.reportportal.service.Launch;
import com.epam.reportportal.service.ReportPortal;
import com.epam.reportportal.testng.TestNGService;
import com.epam.reportportal.utils.properties.PropertiesLoader;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;

import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.TestLog;

	public class ParamOverrideTestNgService extends TestNGService {
		
		public static String ENDPOINT = "rp.endpoint";
		public static String UUID = "rp.uuid";
		public static String LAUNCH = "rp.launch";
		public static String PROJECT = "rp.project";
		public static String REPORT_PORTAL_ENABLE = "rp.enable";
		public static String DESCRIPTION = "rp.description";
		public static String HTTP_PROXY = "rp.proxy";
		public static String CONVERT_IMAGE = "rp.convertimage";
		public static String BATCH_SIZE = "rp.batch.size.logs";
		public static String ATTRIBUTES = "rp.attributes";
		public static String LAUNCH_UUID = "rp.launcherUUID";
		public static String LAUNCH_ID = "rp.launcherId";

		public ParamOverrideTestNgService() {
			super(getLaunchOverriddenProperties());
		}

		private static Supplier<Launch> getLaunchOverriddenProperties() {
			ListenerParameters parameters = new ListenerParameters(PropertiesLoader.load());
			
			parameters.setBaseUrl(Config.getGlobalValue(ENDPOINT));
			parameters.setApiKey(Config.getGlobalValue(UUID));
			parameters.setLaunchName(Config.getGlobalValue(LAUNCH));
			parameters.setProjectName(Config.getGlobalValue(PROJECT));
			parameters.setEnable(Config.getGlobalBooleanValue(REPORT_PORTAL_ENABLE));
			if(!Config.getGlobalValue(DESCRIPTION).isEmpty())
				parameters.setDescription(Config.getGlobalValue(DESCRIPTION));
			parameters.setConvertImage(Config.getGlobalBooleanValue(CONVERT_IMAGE));
			parameters.setBatchLogsSize(Config.getGlobalIntValue(BATCH_SIZE));	
			if(!Config.getGlobalValue(HTTP_PROXY).isEmpty())
				parameters.setProxyUrl(Config.getGlobalValue(HTTP_PROXY));
			parameters = setAttributes(parameters);
			
			// disable report portal if server is down
			if(Config.getBooleanValue(REPORT_PORTAL_ENABLE) && !Helper.isServerOnline(parameters.getBaseUrl(), parameters.getProxyUrl())) {
				parameters.setEnable(false);
				TestLog.ConsoleLog("report portal server not reachable at url: " + parameters.getBaseUrl() + " with proxy: " + parameters.getProxyUrl());
				Config.putValue(REPORT_PORTAL_ENABLE, "false");
			}
			
			ReportPortal reportPortal = ReportPortal.builder().withParameters(parameters).build();
			StartLaunchRQ rq = buildStartLaunch(reportPortal.getParameters());
			return new Supplier<Launch>() {
				@Override
				public Launch get() {
					Launch launch = reportPortal.newLaunch(rq);
					if(Config.getGlobalBooleanValue(REPORT_PORTAL_ENABLE)) {
						try {
							Object proxyOrigin = FieldUtils.readField(launch, "uuid", true);
							String uuid = proxyOrigin.toString();
							Config.setGlobalValue(LAUNCH_UUID,uuid);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return launch;
				}
			};
		}
		
		/**
		 * convert attribute string list to Set<ItemAttributesRQ> for report portal
		 * @return
		 */
		private static ListenerParameters setAttributes(ListenerParameters parameters) {
			String attributeString = Config.getValue(ATTRIBUTES);
			
			if(attributeString.isEmpty()) {
				return parameters;
			}
			
			String[] attributes = attributeString.split(";");			
			Set<ItemAttributesRQ> attributeSet = new HashSet<>();
			
			ItemAttributesRQ items = null;
			for(String attribute: attributes) {
				 String[] keyValue = attribute.split(":");
				 String key = keyValue[0];
				 if(keyValue.length == 2) {
					 String value = keyValue[1];
					 items = new ItemAttributesRQ(key,value);

				 }else
					 items = new ItemAttributesRQ(key);
				 attributeSet.add(items);
			}
			parameters.setAttributes(attributeSet);
			return parameters;
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