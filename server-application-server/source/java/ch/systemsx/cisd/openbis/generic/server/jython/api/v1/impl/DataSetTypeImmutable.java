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

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.EntityKind;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IDataSetTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IScriptImmutable;

/**
 * @author Kaloyan Enimanev
 */
public class DataSetTypeImmutable implements IDataSetTypeImmutable
{

    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType dataSetType;

    DataSetTypeImmutable(String code)
    {
        this(new ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType());
        getDataSetType().setCode(code);
    }

    DataSetTypeImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType dataSetType)
    {
        this.dataSetType = dataSetType;
    }

    ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType getDataSetType()
    {
        return dataSetType;
    }

    @Override
    public String getCode()
    {
        return getDataSetType().getCode();
    }

    @Override
    public String getDescription()
    {
        return getDataSetType().getDescription();
    }

    @Override
    public boolean isDeletionDisallowed()
    {
        return getDataSetType().isDeletionDisallow();
    }

    @Override
    public EntityKind getEntityKind()
    {
        return EntityKind.DATA_SET;
    }

    @Override
    public String getMainDataSetPattern()
    {
        return getDataSetType().getMainDataSetPattern();
    }

    @Override
    public String getMainDataSetPath()
    {
        return getDataSetType().getMainDataSetPath();
    }

    @Override
    public IScriptImmutable getValidationScript()
    {
        return ScriptHelper.getScriptImmutable(getDataSetType());
    }
}
