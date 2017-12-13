package com.commercetools.emailprocessor.utils;

import com.commercetools.emailprocessor.model.ProjectConfiguration;
import org.junit.Test;

import java.util.Optional;
<<<<<<< HEAD
<<<<<<< HEAD

import static com.commercetools.emailprocessor.testUtils.TestUtils.getInvalidProjectConfigurationFilePath;
import static com.commercetools.emailprocessor.testUtils.TestUtils.getValidProjectConfigurationFilePath;
=======
>>>>>>> Remove unused imports.
=======

>>>>>>> Revert "Remove unused imports."
import static com.commercetools.emailprocessor.utils.ConfigurationUtils.getConfigurationFromFile;
import static org.junit.Assert.assertEquals;

public class ConfigurationUtilsTest {

    @Test
<<<<<<< HEAD
<<<<<<< HEAD
    public void configurationUtils_validConfigFileIsProvided_shouldReturnConfiguration() throws Exception {
        Optional<ProjectConfiguration> projectConfiguration = getConfigurationFromFile(getValidProjectConfigurationFilePath());
=======
    public void getConfigurationFromFilevalidConfigFileIsProvided_shouldReturnConfiguration() throws Exception {
=======
    public void configurationUtils_validConfigFileIsProvided_shouldReturnConfiguration() throws Exception {
>>>>>>> Revert "Remove unused imports."
        final String resourceFilePath = ConfigurationUtilsTest.class.getClassLoader()
            .getResource("validProjectConfiguration.json").getFile();
        Optional<ProjectConfiguration> projectConfiguration = getConfigurationFromFile(resourceFilePath);
>>>>>>> Remove unused imports.
        assertEquals(true, projectConfiguration.isPresent());
    }

    @Test
<<<<<<< HEAD
<<<<<<< HEAD
    public void configurationUtils_invalidConfigFileIsProvided_shouldReturnConfiguration() throws Exception {
        Optional<ProjectConfiguration> projectConfiguration = getConfigurationFromFile(getInvalidProjectConfigurationFilePath());
=======
    public void getConfigurationFromFile_invalidConfigFileIsProvided_shouldReturnConfiguration() throws Exception {
=======
    public void configurationUtils_invalidConfigFileIsProvided_shouldReturnConfiguration() throws Exception {
>>>>>>> Revert "Remove unused imports."
        final String resourceFilePath = ConfigurationUtilsTest.class.getClassLoader()
            .getResource("invalidProjectConfiguration.json").getFile();
        Optional<ProjectConfiguration> projectConfiguration = getConfigurationFromFile(resourceFilePath);
>>>>>>> Remove unused imports.
        assertEquals(projectConfiguration.isPresent(), false);
    }

    @Test
    public void configurationUtils_invalidConfigFilePathIsProvided_shouldReturnConfiguration() throws Exception {
        final String resourceFilePath = "invalid/path";
        Optional<ProjectConfiguration> projectConfiguration = getConfigurationFromFile(resourceFilePath);
        assertEquals(projectConfiguration.isPresent(), false);
    }
}