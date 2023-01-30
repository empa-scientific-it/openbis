/*
 * Copyright 2011 ETH Zuerich, CISD
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
 * @author Kaloyan Enimanev
 */
public class DataSetType extends DataSetTypeImmutable implements IDataSetType
{
    DataSetType(String code)
    {
        super(code);
    }

    @Override
    public void setDescription(String description)
    {
        getDataSetType().setDescription(description);
    }

    @Override
    public void setMainDataSetPattern(String mainDataSetPattern)
    {
        getDataSetType().setMainDataSetPattern(mainDataSetPattern);
    }

    @Override
    public void setMainDataSetPath(String mainDataSetPath)
    {
        getDataSetType().setMainDataSetPath(mainDataSetPath);
    }

    @Override
    public void setDeletionDisallowed(boolean deletionDisallowed)
    {
        getDataSetType().setDeletionDisallow(deletionDisallowed);
    }

    @Override
    public void setValidationScript(ScriptImmutable validationScript)
    {
        getDataSetType().setValidationScript(validationScript.script);
    }
}
