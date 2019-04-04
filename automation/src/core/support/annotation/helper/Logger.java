package core.support.annotation.helper;

public class Logger {
	
	public static boolean IS_DEBUG = true;

	// enable debug
	public static void debug(String value) {
		if(IS_DEBUG)
			System.out.println(value);
	}
}
