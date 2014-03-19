package com.cohesionforce.test.search;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * The AllTests suite is run with the maven build to execute the
 * unit tests in the order provided below.
 * 
 * @author jlangley
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ TestParseCommand.class, 
		TestCreateIndex.class, TestTypeSearchTime.class
		 })
public class AllTests {

}
