/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.systemtest.base.matcher;

import java.util.Collection;
import java.util.Collections;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * @author Franz-Josef Elmer
 */
public class ExternalDataHasComponentsMatcher extends ExternalDataHasChildrenMatcher
{

    public ExternalDataHasComponentsMatcher(AbstractExternalData first, AbstractExternalData[] rest)
    {
        super(first, rest);
    }

    @Override
    protected String getSubDataSetType()
    {
        return "components";
    }

    @Override
    protected Collection<AbstractExternalData> getSubDataSets(AbstractExternalData actual)
    {
        if (actual.isContainer() == false)
        {
            return Collections.emptySet();
        }
        return actual.tryGetAsContainerDataSet().getContainedDataSets();
    }

}
