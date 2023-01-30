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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.columns;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util.GridTestUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalDataGridColumnIDs;
import ch.systemsx.cisd.openbis.generic.shared.basic.Row;

/**
 * Allows to define data set search row expectations.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetSearchRow extends Row
{

    public DataSetSearchRow()
    {
    }

    public DataSetSearchRow(final String code)
    {
        withCell(ExternalDataGridColumnIDs.CODE, code);
    }

    /**
     * Creates a {@link Row} with given <var>propertyCode</var> associated to given <i>value</i>.
     */
    public final DataSetSearchRow withPropertyCell(final String propertyCode, final Object value)
    {
        return property(propertyCode, value, false);

    }

    private final DataSetSearchRow property(final String propertyCode, final Object value,
            boolean internalNamespace)
    {
        final String propertyIdentifier =
                GridTestUtils.getDataSetPropertyColumnIdentifier(propertyCode, internalNamespace);
        withCell(propertyIdentifier, value);
        return this;
    }

}
