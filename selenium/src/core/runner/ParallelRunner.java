package core.runner;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
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

import core.logger.PropertiesReader;

public class ParallelRunner extends BlockJUnit4ClassRunner {
	
	private static final int NUM_THREADS = PropertiesReader.getParallelTests();
	private static final AtomicInteger numTestsWaitingToStart = new AtomicInteger(0);
	
	static ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
	
	private static CompletionService<Void> completionService = new ExecutorCompletionService<Void>(executorService);
	private static Queue<Future<Void>> tasks = new LinkedList<Future<Void>>();
	
	public ParallelRunner(final Class<?> klass) throws InitializationError {
		super(klass);
		DOMConfigurator.configure("properties/log4j.xml");

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
						while (!tasks.isEmpty())
							tasks.poll().cancel(true);
						launchReportAfterTest();
						executorService.shutdownNow();
						
					}
				}
		 	}
		});
	}
	
	/**
	 * launches the report html page after test run
	 */
	public  void launchReportAfterTest()
	{
		String htmlFilePath = "extent.html"; // path to your new file
		File htmlFile = new File(htmlFilePath);

		// open the default web browser for the HTML page
		try {
			Desktop.getDesktop().browse(htmlFile.toURI());
		} catch (IOException e) {
			e.getMessage();
		}
	}
}
