package core.support.listeners;

import com.epam.reportportal.testng.BaseTestNGListener;

import core.support.logger.TestLog;


public class ReportPortalListener extends BaseTestNGListener {
	
	 private static ParamOverrideTestNgService ReportPortal() {
		 	TestLog.setupLog4j();
		    return new ParamOverrideTestNgService();
		  }
	 
		public ReportPortalListener() {
			super(ReportPortal());
		}
	}

