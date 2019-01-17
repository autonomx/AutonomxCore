package core.helpers;

import java.util.concurrent.TimeUnit;

/**
 * StopWatchHelper watch = StopWatchHelper.start(); // do something long
 * passedTimeInMs = watch.time(); long passedTimeInSeconds =
 * watch.time(TimeUnit.SECONDS);
 * 
 * @author ehsan matean
 *
 */
public class StopWatchHelper {
	long starts;

	/**
	 * StopWatchHelper watch = Helper.start(); do something long passedTimeInMs =
	 * watch.time(); long passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
	 * 
	 * @return
	 */
	public static StopWatchHelper start() {
		return new StopWatchHelper();
	}
	

	private StopWatchHelper() {
		reset();
	}

	public StopWatchHelper reset() {
		starts = System.currentTimeMillis();
		return this;
	}

	public long time() {
		long ends = System.currentTimeMillis();
		return ends - starts;
	}

	public long time(TimeUnit unit) {
		return unit.convert(time(), TimeUnit.MILLISECONDS);
	}
}