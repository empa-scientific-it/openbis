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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.CodesArea;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * A text area to specify parents of a data set. Parents are specified by codes separated by commas,
 * spaces or new lines.
 * 
 * @author Piotr Buczek
 */
public final class DataSetParentsArea extends CodesArea<ExternalData>
{

    public static final String ID_SUFFIX_PARENTS = "_parents";

    public DataSetParentsArea(IMessageProvider messageProvider, String idPrefix)
    {
        super(messageProvider.getMessage(Dict.PARENTS_EMPTY));
		this.setFieldLabel(messageProvider.getMessage(Dict.PARENTS));
        setId(createId(idPrefix));
    }

    public static String createId(String idPrefix)
    {
        return idPrefix + ID_SUFFIX_PARENTS;
    }

    // delegation to abstract class methods

    // null if the area has not been modified,
    // the list of all data set parent codes otherwise
    public final String[] tryGetModifiedParentCodes()
    {
        return tryGetModifiedItemList();
    }

    public final void setParents(List<ExternalData> parents)
    {
        setCodeProviders(parents);
    }

    public final void setParentCodes(String[] parentCodes)
    {
        setCodes(parentCodes);
    }
}
