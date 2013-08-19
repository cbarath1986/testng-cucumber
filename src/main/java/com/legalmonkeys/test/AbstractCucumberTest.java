package com.legalmonkeys.test;

import java.io.File;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.legalmonkeys.test.annotation.Feature;
import com.legalmonkeys.test.annotation.Scenario;

import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;

public class AbstractCucumberTest {

    public static final String CUCUMBER_STEPS_PACKAGE_PROPERTY = "cucumber.steps.package";
    protected static final String STEPS_PACKAGE = ResourceBundle.getBundle("cucumber").getString(CUCUMBER_STEPS_PACKAGE_PROPERTY);
    protected static final String RESOURCES =
            "src" + File.separator + "test" + File.separator + "resources" + File.separator;
    private ClassLoader classLoader = getClass().getClassLoader();
    private MultiLoader multiLoader = new MultiLoader(classLoader);

    public void run() {
        // setup
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Properties());
        runtimeOptions.glue.add(STEPS_PACKAGE);
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        String scenarioName;
        String featureName;
        Class<?> featureClass;
        try {
            featureClass = classLoader.loadClass(stackTraceElement.getClassName());
            featureName = featureClass.getAnnotation(Feature.class).value();
            scenarioName = featureClass.getMethod(stackTraceElement.getMethodName()).getAnnotation(Scenario.class)
                    .value();
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        runtimeOptions.formatters.clear();
        runtimeOptions.formatters.add(new CucumberTestNgFormatter(System.out));
        runtimeOptions.filters.add(Pattern.compile(scenarioName + "$"));
        getClass().getPackage().getName();
        runtimeOptions.featurePaths
                .add(RESOURCES + featureClass.getPackage().getName().replace(".", File.separator) + File.separator
                        + featureName + ".feature");
        Runtime runtime = new Runtime(multiLoader, classLoader, runtimeOptions);

        // act
        runtime.run();

        // verify
        if (runtime.getErrors().size() > 0) {
            throw new RuntimeException(runtime.getErrors().get(0));
        }
    }
}
