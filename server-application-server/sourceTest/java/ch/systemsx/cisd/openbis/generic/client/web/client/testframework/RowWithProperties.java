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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util.GridTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.Row;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * This Row extension allows expected cell values to be entity property columns.
 * 
 * @author Tomasz Pylak
 */
public class RowWithProperties extends Row
{
    /**
     * Creates a {@link Row} with given <var>propertyCode</var> associated to given <i>value</i>.
     */
    public final RowWithProperties withUserPropertyCell(final String propertyCode,
            final Object value)
    {
        return property(propertyCode, value, false);
    }

    /**
     * Creates a {@link Row} with given <var>propertyCode</var> associated to given <i>value</i>.
     * <p>
     * Note that we assume that computed {@link PropertyType} is from internal namespace.
     * </p>
     */
    public final RowWithProperties withInternalPropertyCell(final String propertyCode,
            final Object value)
    {
        return property(propertyCode, value, true);
    }

    private final RowWithProperties property(final String propertyCode, final Object value,
            boolean managedInternally)
    {
        final String propertyIdentifier =
                GridTestUtils.getPropertyColumnIdentifier(propertyCode, managedInternally);
        withCell(propertyIdentifier, value);
        return this;
    }

}
