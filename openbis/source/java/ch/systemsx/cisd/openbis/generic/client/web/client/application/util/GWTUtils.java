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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import com.google.gwt.user.client.ui.ListBox;

/**
 * Some utility methods around <i>GWT</i>.
 * 
 * @author Christian Ribeaud
 */
public final class GWTUtils
{

    private GWTUtils()
    {
        // Can not be instantiated.
    }

    private static boolean testing = false;

    /**
     * Whether we are in testing mode.
     */
    public static boolean isTesting()
    {
        return testing;
    }

    /**
     * Sets <code>testing</code> flag to <code>true</code>.
     */
    public static void testing()
    {
        testing = true;
    }

    /**
     * Selects given <var>value</var> of given <var>listBox</var>.
     */
    public final static void setSelectedItem(final ListBox listBox, final String value)
    {
        assert listBox != null : "Unspecified list box.";
        assert value != null : "Unspecified value.";
        for (int index = 0; index < listBox.getItemCount(); index++)
        {
            if (listBox.getItemText(index).equals(value))
            {
                listBox.setSelectedIndex(index);
                return;
            }
        }
        throw new IllegalArgumentException("Given value '" + value
                + "' not found in given list box.");
    }

}
