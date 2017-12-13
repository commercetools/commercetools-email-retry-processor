package com.commercetools.emailprocessor.testUtils;

public final class TestUtils {

    public static String getValidProjectConfigurationFilePath() {
        return TestUtils.class.getClassLoader().getResource("validProjectConfiguration.json").getFile();
    }

    public static String getInvalidProjectConfigurationFilePath() {
        return TestUtils.class.getClassLoader().getResource("invalidProjectConfiguration.json").getFile();
    }

    private TestUtils() {
    }
}
