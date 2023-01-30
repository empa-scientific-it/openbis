/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IDataSetType;

/**
 * Wrapper of {@link DataSetTypeImmutable} as {@link IDataSetType} where setters do nothing.
 * 
 * @author Franz-Josef Elmer
 */
class DataSetTypeWrapper extends DataSetTypeImmutable implements IDataSetType
{

    DataSetTypeWrapper(DataSetTypeImmutable dataSetType)
    {
        super(dataSetType.getDataSetType());
    }

    @Override
    public void setDescription(String description)
    {
    }

    @Override
    public void setMainDataSetPattern(String mainDataSetPattern)
    {
    }

    @Override
    public void setMainDataSetPath(String mainDataSetPath)
    {
    }

    @Override
    public void setDeletionDisallowed(boolean deletionDisallowed)
    {
    }

    @Override
    public void setValidationScript(ScriptImmutable validationScript)
    {
    }

}
