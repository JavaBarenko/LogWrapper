package barenko.util;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

public class WrapperUtilsTest extends TestCase {

    @SuppressWarnings("unchecked")
    public void testAddInterface() throws IllegalInterfaceImplementationException {
	TestConcrete y = new TestConcrete();

	assertFalse(y instanceof Test);
	assertFalse(y instanceof Iterable);
	assertFalse(y instanceof TestImpl);

	y = WrapperUtils.addInterface(y, Test.class);
	y = WrapperUtils.addInterface(y, TestImpl.class);
	try {
	    y = WrapperUtils.addInterface(y, InvocationHandler.class);
	    fail("deveria ter disparado exceção");
	} catch (final IllegalInterfaceImplementationException e) {}

	assertTrue(y instanceof Test);
	assertFalse(y instanceof TestImpl);
	assertFalse(y instanceof Iterable);
    }

    public void testEnableTraceByInterfaceWithReturnInteger() {
	final MockLog log = new MockLog(Level.INFO);
	final Test t = WrapperUtils.enableTraceByInterface(new TestImpl(), log, Level.INFO);

	t.getTen();
	assertEquals("[Thread[main,5,main]] barenko.util.Test.getTen()", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [10]", log.getFirstMsg());
    }

    public void testEnableTraceByInterfaceWithReturnString() {
	final MockLog log = new MockLog(Level.INFO);
	final Test t = WrapperUtils.enableTraceByInterface(new TestImpl(), log, Level.INFO);

	t.getValue("123");
	assertEquals("[Thread[main,5,main]] barenko.util.Test.getValue(\"123\")", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [my value is 123]", log.getFirstMsg());
    }

    public void testEnableTraceByInterfaceWithReturnVarargString() {
	final MockLog log = new MockLog(Level.INFO);
	final Test t = WrapperUtils.enableTraceByInterface(new TestImpl(), log, Level.INFO);

	t.getValues(123, "one", "two", "three");
	assertEquals("[Thread[main,5,main]] barenko.util.Test.getValues(123, [\"one\", \"two\", \"three\"])", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [123one, two, three]", log.getFirstMsg());
    }

    public void testEnableTraceByInterfaceWithReturnStringArray() {
	final MockLog log = new MockLog(Level.INFO);
	final Test t = WrapperUtils.enableTraceByInterface(new TestImpl(), log, Level.INFO);

	t.getArray();
	assertEquals("[Thread[main,5,main]] barenko.util.Test.getArray()", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [[\"1\", \"2\"]]", log.getFirstMsg());
    }

    public void testEnableTraceByInterfaceWithVoidReturn() {
	final MockLog log = new MockLog(Level.INFO);
	final Test t = WrapperUtils.enableTraceByInterface(new TestImpl(), log, Level.INFO);

	t.getNothing();
	assertEquals("[Thread[main,5,main]] barenko.util.Test.getNothing()", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [null]", log.getFirstMsg());
    }

    public void testEnableTraceByInterfaceWithReturnNull() {
	final MockLog log = new MockLog(Level.INFO);
	final Test t = WrapperUtils.enableTraceByInterface(new TestImpl(), log, Level.INFO);

	t.getNull();
	assertEquals("[Thread[main,5,main]] barenko.util.Test.getNull()", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [null]", log.getFirstMsg());
    }

    public void testEnableTraceByInterfaceWithReturnThrow() {
	final MockLog log = new MockLog(Level.INFO);
	final Test t = WrapperUtils.enableTraceByInterface(new TestImpl(), log, Level.INFO);

	try {
	    t.throwsError();
	} catch (final Exception e) {}

	final String traceRegex = "(?s)\\[Thread\\[main,5,main\\]\\] throw a java.lang.Exception: My error\n        barenko.util.Test[^.]+.throwsError\\(WrapperUtilsTest.java:\\d+\\)\n        barenko.util.WrapperUtilsTest.testEnableTraceByInterfaceWithReturnThrow\\(WrapperUtilsTest.java:\\d+\\).*";
	assertEquals("[Thread[main,5,main]] barenko.util.Test.throwsError()", log.getFirstMsg());
	assertTrue(log.getFirstMsg().matches(traceRegex));
    }

    public void testEnableTraceWithReturnInteger() {
	final MockLog log = new MockLog(Level.INFO);
	final TestConcrete t = WrapperUtils.enableTrace(new TestConcrete(), log, Level.INFO);

	t.getTen();
	assertEquals("[Thread[main,5,main]] barenko.util.TestConcrete.getTen()", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [10]", log.getFirstMsg());
    }

    public void testEnableTraceWithReturnString() {
	final MockLog log = new MockLog(Level.INFO);
	final TestConcrete t = WrapperUtils.enableTrace(new TestConcrete(), log, Level.INFO);

	t.getValue("123");
	assertEquals("[Thread[main,5,main]] barenko.util.TestConcrete.getValue(\"123\")", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [my value is 123]", log.getFirstMsg());
    }

    public void testEnableTraceWithReturnVarargString() {
	final MockLog log = new MockLog(Level.INFO);
	final TestConcrete t = WrapperUtils.enableTrace(new TestConcrete(), log, Level.INFO);

	t.getValues(123, "one", "two", "three");
	assertEquals("[Thread[main,5,main]] barenko.util.TestConcrete.getValues(123, [\"one\", \"two\", \"three\"])", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [123one, two, three]", log.getFirstMsg());
    }

    public void testEnableTraceWithReturnStringArray() {
	final MockLog log = new MockLog(Level.INFO);
	final TestConcrete t = WrapperUtils.enableTrace(new TestConcrete(), log, Level.INFO);

	t.getArray();
	assertEquals("[Thread[main,5,main]] barenko.util.TestConcrete.getArray()", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [[\"1\", \"2\"]]", log.getFirstMsg());
    }

    public void testEnableTraceWithVoidReturn() {
	final MockLog log = new MockLog(Level.INFO);
	final TestConcrete t = WrapperUtils.enableTrace(new TestConcrete(), log, Level.INFO);

	t.getNothing();
	assertEquals("[Thread[main,5,main]] barenko.util.TestConcrete.getNothing()", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [null]", log.getFirstMsg());
    }

    public void testEnableTraceWithReturnNull() {
	final MockLog log = new MockLog(Level.INFO);
	final TestConcrete t = WrapperUtils.enableTrace(new TestConcrete(), log, Level.INFO);

	t.getNull();
	assertEquals("[Thread[main,5,main]] barenko.util.TestConcrete.getNull()", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [null]", log.getFirstMsg());
    }

    public void testEnableTraceWithReturnThrow() {
	final MockLog log = new MockLog(Level.INFO);
	final TestConcrete t = WrapperUtils.enableTrace(new TestConcrete(), log, Level.INFO);

	try {
	    t.throwsError();
	} catch (final Exception e) {}

	final String traceRegex = "(?s)\\[Thread\\[main,5,main\\]\\] throw a java.lang.Exception: My error\n        barenko.util.Test[^.]+.throwsError\\(WrapperUtilsTest.java:\\d+\\)\n        .*barenko.util.WrapperUtilsTest.testEnableTraceWithReturnThrow\\(WrapperUtilsTest.java:\\d+\\).*";

	assertEquals("[Thread[main,5,main]] barenko.util.TestConcrete.throwsError()", log.getFirstMsg());
	assertTrue(log.getFirstMsg().matches(traceRegex));
    }

    public void testLoggingArrayList() throws Exception {
	final MockLog log = new MockLog(Level.INFO);

	final List<String> myList = WrapperUtils.enableTrace(new ArrayList<String>(), log, Level.INFO);

	myList.add("It's a test");
	myList.add("and another test");

	assertEquals("[Thread[main,5,main]] java.util.ArrayList.add(\"It's a test\")", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] java.util.ArrayList.ensureCapacity(1)", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [null]", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [true]", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] java.util.ArrayList.add(\"and another test\")", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] java.util.ArrayList.ensureCapacity(2)", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [null]", log.getFirstMsg());
	assertEquals("[Thread[main,5,main]] return: [true]", log.getFirstMsg());
    }
}

class MockLog extends Logger {

    private final Queue<String> messages;

    public MockLog(final Level logLevel) {
	super("MockLog", null);
	setLevel(logLevel);
	this.messages = new ConcurrentLinkedQueue<String>();
    }

    public String getFirstMsg() {
	return this.messages.poll();
    }

    @Override
    public void log(final Level level, final String msg) {
	if (isLoggable(level)) this.messages.offer(msg);
	else this.messages.offer("");
    }
}

interface Test {
    Integer getTen();

    String getValue(String value);

    String getValues(int x, String... values);

    String[] getArray();

    void getNothing();

    String getNull();

    void throwsError() throws Exception;
}

class TestImpl extends TestConcrete implements Test {}

class TestConcrete {
    public Integer getTen() {
	return 10;
    }

    public String getValue(final String value) {
	return "my value is " + value;
    }

    public String getValues(final int x, final String... values) {
	final StringBuilder sb = new StringBuilder();
	for (final String value : values)
	    sb.append(value).append(", ");
	sb.setLength(sb.length() - 2);
	return x + sb.toString();
    }

    public String[] getArray() {
	return new String[] { "1", "2" };
    }

    public void getNothing() {};

    public String getNull() {
	return null;
    }

    public void throwsError() throws Exception {
	throw new Exception("My error");
    }
}
