package com.example;

import org.testng.annotations.Test;

import com.examples.test.AbstractCucumberTest;
import com.examples.test.annotation.Feature;
import com.examples.test.annotation.Scenario;

@Feature("login")
public class LoginTest extends AbstractCucumberTest {

    @Test
    @Scenario("Login with guest account")
    public void loginWithGuestAccount() {
        run();
    }
}