package com.tinkerpop.rexster.kibbles.frames;

import org.apache.commons.configuration.HierarchicalConfiguration;
import com.tinkerpop.rexster.extension.ExtensionConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class FramesExtensionTest {

    @Test
    public void isConfigurationValidNullConfiguration() {
        Assert.assertFalse(new FramesExtension().isConfigurationValid(null));
    }

    @Test
    public void isConfigurationValidEmptyConfiguration() {
        HierarchicalConfiguration xmlConfig = new HierarchicalConfiguration();
        ExtensionConfiguration configuration = new ExtensionConfiguration("ns", "name", xmlConfig);
        Assert.assertFalse(new FramesExtension().isConfigurationValid(configuration));
    }

    @Test
    public void isConfigurationValidNiceConfiguration() {
        HierarchicalConfiguration hc = new HierarchicalConfiguration();
        hc.addProperty("key1", "value1");
        hc.addProperty("key2", "value2");
        ExtensionConfiguration configuration = new ExtensionConfiguration("ns", "name", hc);
        Assert.assertTrue(new FramesExtension().isConfigurationValid(configuration));
    }
}
