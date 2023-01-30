/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property.PropertyGrid;

/**
 * Manager for checking properties based on {@link IValueAssertion} objects.
 * 
 * @author Franz-Josef Elmer
 */
public class PropertyCheckingManager extends Assert
{
    @SuppressWarnings("rawtypes")
    private final Map<String, IValueAssertion> expectedProperties =
            new HashMap<String, IValueAssertion>();

    /**
     * Adds for the property with specified name the specified value assertion.
     */
    public void addExcpectedProperty(final String name, final IValueAssertion<?> valueAssertion)
    {
        expectedProperties.put(name, valueAssertion);
    }

    /**
     * Checks the assertion for the properties of a {@link PropertyGrid} with specified widget ID.
     */
    public void assertPropertiesOf(final String widgetID)
    {
        final Widget widget = GWTTestUtil.getWidgetWithID(widgetID);
        assertTrue("Expected PropertyGrid instead of " + widget.getClass(),
                widget instanceof PropertyGrid);
        assertProperties(((PropertyGrid) widget).getProperties());
    }

    /**
     * Checks the assertion for the specified properties.
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    public void assertProperties(final Map<String, ?> actualProperties)
    {
        for (final Map.Entry<String, IValueAssertion> expectedProperty : expectedProperties
                .entrySet())
        {
            final String key = expectedProperty.getKey();
            assertTrue("Expected property not found: " + key, actualProperties.containsKey(key));
            final Object value = actualProperties.get(key);
            try
            {
                expectedProperty.getValue().assertValue(value);
            } catch (final ClassCastException e)
            {
                throw new IllegalArgumentException("Property '" + key
                        + "' is not of expected type: " + value);
            }
        }
    }
}
