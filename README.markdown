LogWrapper
==========

_"Log anything anywhere"_


What is the goal?
-----------------

LogWrapper is a package that aimed at making easier the log into any class of any package, by reflection, using [CGLIB](http://cglib.sourceforge.net) in Java SE 6.

The primary objective is to log third-party classes packages, in which I have no access to the source code.


What does it do?
----------------

LogWrapper is able to wrapper a specific instance class, giving it the ability to log both the call of all its methods (like the parameters passed) and the return (if any).

If the logged method throws an exception, it will also be logged, with the Stacktrace.


How can I use it?
-----------------

Is very simple begin to log a instance, just do it:

	ClassToTest t = LogWrapper.enableTrace(new ClassToTest(), log, Level.INFO);

For example, logging the ArrayList:

	List<String> myList = LogWrapper.enableTrace(new ArrayList<String>(), log, Level.INFO);

	myList.add("It's a test");
	myList.add("and another test");

This code will generate this log:

	[Thread[main,5,main]] java.util.ArrayList.add("It's a test")	  # add method call (with argument)
	[Thread[main,5,main]] java.util.ArrayList.ensureCapacity(1)		  # ensureCapacity call (called internally by add)
	[Thread[main,5,main]] return: [null]							  # return of ensureCapacity (void)
	[Thread[main,5,main]] return: [true]							  # return of add (true)
	[Thread[main,5,main]] java.util.ArrayList.add("and another test") # the same process again...
	[Thread[main,5,main]] java.util.ArrayList.ensureCapacity(2)
	[Thread[main,5,main]] return: [null]
	[Thread[main,5,main]] return: [true]