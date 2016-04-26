package com.legalmonkeys.test;

import com.legalmonkeys.test.annotation.Feature;
import com.legalmonkeys.test.annotation.Scenario;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class AbstractCucumberTest {

    private static final String CUCUMBER_STEPS_PACKAGE_PROPERTY = "cucumber.steps.package";
    private static final String STEPS_PACKAGE = ResourceBundle.getBundle("cucumber").getString(CUCUMBER_STEPS_PACKAGE_PROPERTY);
    private static final String RESOURCES =
            "src" + File.separator + "test" + File.separator + "resources" + File.separator;
    private static final String BUILD_TEST_RESULTS = "build/test-results/";
    private ClassLoader classLoader = getClass().getClassLoader();
    private MultiLoader multiLoader = new MultiLoader(classLoader);

    private static final int RETRY = Integer.parseInt(System.getProperty("retry.tests", "0"));

    @SuppressWarnings("unused")
    public void run() {
        run(0);
    }

    private void run(int retry) {
        // setup
        RuntimeOptions runtimeOptions = new RuntimeOptions(new Properties());
        runtimeOptions.glue.add(STEPS_PACKAGE);
        runtimeOptions.strict = true;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String scenarioName = null;
        String featureName = null;
        Class<?> featureClass = null;
        System.out.println("stacktrace:");
        for (StackTraceElement stackTraceElement : stackTrace) {
            System.out.println(stackTraceElement.toString());
            try {
                featureClass = classLoader.loadClass(stackTraceElement.getClassName());
                Feature annotation = featureClass.getAnnotation(Feature.class);
                if (annotation != null) {
                    featureName = annotation.value();
                    scenarioName = featureClass.getMethod(stackTraceElement.getMethodName()).getAnnotation(Scenario.class)
                            .value();
                    break;
                }
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        if (featureClass == null || scenarioName == null || featureName == null) {
            throw new RuntimeException("cannot find test implementation");
        }
        System.out.println("Try(" + retry + ") Running test featureName=" + featureName + "; scenario=" +
                scenarioName);
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
        if (runtime.exitStatus() != 0) {
            if (runtime.getErrors().size() == 0 && runtime.getSnippets().size() > 0) {
                throw new RuntimeException(runtime.getSnippets().get(0));
            }
            printErrors(runtime, featureName, scenarioName);
            if (retry < RETRY) {
                run(retry + 1);
            } else {
                if (runtime.getErrors().size() > 0) {
                    throw new RuntimeException(runtime.getErrors().get(0));
                }
            }
        }
    }

    private void printErrors(Runtime runtime, String featureName, String scenarioName) {
        //noinspection ResultOfMethodCallIgnored
        new File(BUILD_TEST_RESULTS).mkdirs();
        File destFile = new File(BUILD_TEST_RESULTS + "tests-running.log.txt");
        try (PrintWriter output = new PrintWriter(new FileWriter(destFile, true))) {
            output.println(String.format("feature: %s , scenario: %s", featureName, scenarioName));
            for (Throwable throwable : runtime.getErrors()) {
                output.println(throwable.getMessage());
                throwable.printStackTrace(output);
            }
        } catch (Exception ignored) {
        }
    }
}
