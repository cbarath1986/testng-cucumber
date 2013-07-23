#Cucumber-TestNg

##If you ever dreamt about running cucumber test with testng look into this project :).

#Configuration:
You need to put configuration file in your project like:

<b>src/test/resources/cucumber.properties</b> with content:

<pre><code>
# java package with steps classes in your project
cucumber.steps.package=com.yourpackage
</code></pre>

To check you didn't forget to implement TestNG @Test add such additional test:

<pre><code>
public class StateTest {

    @Test(priority = -1)
    public void testState() {
        // setup

        // act
        String result = CucumberTestsStateUtil.checkTests();

        // verify
        Assert.assertNull(result, result);
    }
}
</code></pre>