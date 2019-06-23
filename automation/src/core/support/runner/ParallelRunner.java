package core.support.runner;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;

import core.support.logger.ExtentManager;
import core.support.logger.TestLog;
import core.support.objects.DriverObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;

public class ParallelRunner extends BlockJUnit4ClassRunner {

	private static final int NUM_THREADS = CrossPlatformProperties.getParallelTests();
	private static final AtomicInteger numTestsWaitingToStart = new AtomicInteger(0);

	static ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

	private static CompletionService<Void> completionService = new ExecutorCompletionService<Void>(executorService);
	private static Queue<Future<Void>> tasks = new LinkedList<Future<Void>>();

	public ParallelRunner(final Class<?> klass) throws InitializationError {
		super(klass);
		DOMConfigurator.configure(TestLog.LOG4JPATH);
		numTestsWaitingToStart.incrementAndGet();
		setScheduler(new RunnerScheduler() {

			// @Override
			public void schedule(Runnable childStatement) {
				tasks.offer(completionService.submit(childStatement, null));
			}

			// @Override
			public void finished() {
				// waits for all threads at NUM_THREADS to start.
				if (numTestsWaitingToStart.decrementAndGet() == 0) {
					try {
						while (!tasks.isEmpty()) {
							tasks.remove(completionService.take());
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					} finally {
						while (!tasks.isEmpty()) {
							tasks.poll().cancel(true);
						}

						// set notification to slack or email if specified
						sendReport();
						// if from inside ide, shutdown executor. maven has own runner
						if (runningFromIde()) {
							DriverObject.quitAllDrivers();
							ExtentManager.printReportLink();
							ExtentManager.launchReportAfterTest();

							executorService.shutdown();
						}
					}
				}
			}
		});
	}

	/**
	 * notify slack if enabled. set in properties file send email report. set in
	 * properties file
	 */
	public void sendReport() {
		String message = "Tests are complete";
		ExtentManager.slackNotification(message);
		ExtentManager.emailTestReport(message); // send test report
	}

	/**
	 * checks to see if test is running from inside ide.
	 * 
	 * @return
	 */
	public static boolean runningFromIde() {
		String classPath = System.getProperty("java.class.path");
		boolean isEclipse = classPath.contains("eclipse");
		boolean isIntelliJ = classPath.contains("idea_rt.jar");
		return isEclipse || isIntelliJ;
	}
}
