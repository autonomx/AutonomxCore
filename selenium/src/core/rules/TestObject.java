package core.rules;

import java.util.ArrayList;

import org.junit.runner.Description;

public class TestObject {

	public boolean isTestPass = false;
	public Description description;
	public Throwable caughtThrowable = null;
	public ArrayList<String> failTrace = new ArrayList<String>();

	public TestObject withIsTestPass(boolean isTestPass) {
		this.isTestPass = isTestPass;
		return this;
	}

	public TestObject withDescription(Description description) {
		this.description = description;
		return this;
	}

	public TestObject withCaughtThrowable(Throwable caughtThrowable) {
		this.caughtThrowable = caughtThrowable;
		return this;
	}
	
	public TestObject withFailTrace(ArrayList<String> failTrace) {
		this.failTrace = failTrace;
		return this;
	}
}