package ut.com.segurosbolivar.plugins;

import org.junit.Test;
import com.segurosbolivar.plugins.api.MyPluginComponent;
import com.segurosbolivar.plugins.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}