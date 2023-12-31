/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.ISampleTypeImmutable;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IScriptImmutable;

/**
 * @author Kaloyan Enimanev
 */
public class SampleTypeImmutable implements ISampleTypeImmutable
{
    private final ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType sampleType;

    SampleTypeImmutable(String code)
    {
        this(new ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType());
        getSampleType().setCode(code);
    }

    SampleTypeImmutable(ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType sampleType)
    {
        this.sampleType = sampleType;
    }

    ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType getSampleType()
    {
        return sampleType;
    }

    @Override
    public String getCode()
    {
        return getSampleType().getCode();
    }

    @Override
    public String getDescription()
    {
        return getSampleType().getDescription();
    }

    @Override
    public boolean isListable()
    {
        return getSampleType().isListable();
    }

    @Override
    public boolean isShowContainer()
    {
        return getSampleType().isShowContainer();
    }

    @Override
    public boolean isShowParents()
    {
        return getSampleType().isShowParents();
    }

    @Override
    public boolean isSubcodeUnique()
    {
        return getSampleType().isSubcodeUnique();
    }

    @Override
    public boolean isAutoGeneratedCode()
    {
        return getSampleType().isAutoGeneratedCode();
    }

    @Override
    public String getGeneratedCodePrefix()
    {
        return getSampleType().getGeneratedCodePrefix();
    }

    @Override
    public boolean isShowParentMetadata()
    {
        return getSampleType().isShowParentMetadata();
    }

    @Override
    public EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    public IScriptImmutable getValidationScript()
    {
        return ScriptHelper.getScriptImmutable(getSampleType());
    }
}
