package com.commercetools.emailprocessor.utils;

import com.commercetools.emailprocessor.model.ProjectConfiguration;
import org.junit.Test;
import java.util.Optional;

import static com.commercetools.emailprocessor.utils.ConfigurationUtils.getConfigurationFromFile;
import static org.junit.Assert.assertEquals;

public class ConfigurationUtilsTest {

    @Test
    public void getConfigurationFromFilevalidConfigFileIsProvided_shouldReturnConfiguration() throws Exception {
        final String resourceFilePath = ConfigurationUtilsTest.class.getClassLoader()
            .getResource("validProjectConfiguration.json").getFile();
        Optional<ProjectConfiguration> projectConfiguration = getConfigurationFromFile(resourceFilePath);
        assertEquals(true, projectConfiguration.isPresent());
    }

    @Test
    public void getConfigurationFromFile_invalidConfigFileIsProvided_shouldReturnConfiguration() throws Exception {
        final String resourceFilePath = ConfigurationUtilsTest.class.getClassLoader()
            .getResource("invalidProjectConfiguration.json").getFile();
        Optional<ProjectConfiguration> projectConfiguration = getConfigurationFromFile(resourceFilePath);
        assertEquals(projectConfiguration.isPresent(), false);
    }

    @Test
    public void getConfigurationFromFile_invalidConfigFilePathIsProvided_shouldReturnConfiguration() throws Exception {
        final String resourceFilePath = "invalid/path";
        Optional<ProjectConfiguration> projectConfiguration = getConfigurationFromFile(resourceFilePath);
        assertEquals(projectConfiguration.isPresent(), false);
    }
}