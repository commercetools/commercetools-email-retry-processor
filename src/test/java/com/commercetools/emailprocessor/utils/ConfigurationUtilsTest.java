package com.commercetools.emailprocessor.utils;

import com.commercetools.emailprocessor.model.ProjectConfiguration;
import org.junit.Test;

import java.util.Optional;

import static com.commercetools.emailprocessor.testUtils.TestUtils.getInvalidProjectConfigurationFilePath;
import static com.commercetools.emailprocessor.testUtils.TestUtils.getValidProjectConfigurationFilePath;
import static com.commercetools.emailprocessor.utils.ConfigurationUtils.getConfigurationFromFile;
import static org.junit.Assert.assertEquals;

public class ConfigurationUtilsTest {

    @Test
    public void configurationUtils_validConfigFileIsProvided_shouldReturnConfiguration() throws Exception {
        Optional<ProjectConfiguration> projectConfiguration = getConfigurationFromFile(getValidProjectConfigurationFilePath());
        assertEquals(true, projectConfiguration.isPresent());
    }

    @Test
    public void configurationUtils_invalidConfigFileIsProvided_shouldReturnConfiguration() throws Exception {
        Optional<ProjectConfiguration> projectConfiguration = getConfigurationFromFile(getInvalidProjectConfigurationFilePath());
        assertEquals(projectConfiguration.isPresent(), false);
    }

    @Test
    public void configurationUtils_invalidConfigFilePathIsProvided_shouldReturnConfiguration() throws Exception {
        final String resourceFilePath = "invalid/path";
        Optional<ProjectConfiguration> projectConfiguration = getConfigurationFromFile(resourceFilePath);
        assertEquals(projectConfiguration.isPresent(), false);
    }
}