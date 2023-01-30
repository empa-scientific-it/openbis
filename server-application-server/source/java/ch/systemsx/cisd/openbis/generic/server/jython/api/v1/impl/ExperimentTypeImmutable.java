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
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExperimentTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IScriptImmutable;

/**
 * @author Kaloyan Enimanev
 */
public class ExperimentTypeImmutable implements IExperimentTypeImmutable
{

    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType expType;

    ExperimentTypeImmutable(String code)
    {
        this(new ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType());
        getExperimentType().setCode(code);
    }

    ExperimentTypeImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType expType)
    {
        this.expType = expType;
    }

    ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType getExperimentType()
    {
        return expType;
    }

    @Override
    public String getCode()
    {
        return getExperimentType().getCode();
    }

    @Override
    public String getDescription()
    {
        return getExperimentType().getDescription();
    }

    @Override
    public EntityKind getEntityKind()
    {
        return EntityKind.EXPERIMENT;
    }

    @Override
    public IScriptImmutable getValidationScript()
    {
        return ScriptHelper.getScriptImmutable(getExperimentType());
    }
}
