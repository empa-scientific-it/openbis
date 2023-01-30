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

package ch.systemsx.cisd.openbis.uitest.rmi;

import java.util.ArrayList;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.uitest.dsl.Command;
import ch.systemsx.cisd.openbis.uitest.dsl.Inject;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;

/**
 * @author anttil
 */
public class CreateSampleTypeRmi implements Command<SampleType>
{
    @Inject
    private String session;

    @Inject
    private ICommonServer commonServer;

    private SampleType type;

    public CreateSampleTypeRmi(SampleType type)
    {
        this.type = type;
    }

    @Override
    public SampleType execute()
    {
        commonServer.registerSampleType(session, convert(type));
        return type;
    }

    private ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType convert(SampleType sampleType)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType result =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType();

        result.setCode(sampleType.getCode());
        result.setContainerHierarchyDepth(0);

        result.setDescription(sampleType.getDescription());
        result.setGeneratedCodePrefix(sampleType.getGeneratedCodePrefix());
        result.setListable(sampleType.isListable());
        result.setGeneratedFromHierarchyDepth(0);
        result.setSampleTypePropertyTypes(new ArrayList<SampleTypePropertyType>());

        return result;
    }

}
