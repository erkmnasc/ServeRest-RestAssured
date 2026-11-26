package br.com.erkmnasc.serverest.bdd;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Roda as features de src/test/resources/features pelo JUnit Platform.
 * Glue e plugins ficam em src/test/resources/junit-platform.properties.
 */
@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
public class RunCucumberTest {
}
