/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.CodesArea;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * Abstract {@link CodesArea} extension for data sets introducing methods with convenient names.
 * 
 * @author Piotr Buczek
 */
public abstract class DataSetsArea extends CodesArea<AbstractExternalData>
{
    public DataSetsArea(String emptyTextMsg)
    {
        super(emptyTextMsg);
    }

    // delegation to abstract class methods

    /**
     * @see #tryGetModifiedItemList()
     */
    public final String[] tryGetModifiedDataSetCodes()
    {
        return tryGetModifiedItemList();
    }

    public final void setDataSets(List<AbstractExternalData> dataSets)
    {
        setCodeProviders(dataSets);
    }

    public final void setDataSetCodes(List<String> dataSetCodes)
    {
        setItems(dataSetCodes);
    }
}
