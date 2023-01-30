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

package ch.systemsx.cisd.openbis.generic.shared.dto.builders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PropertyBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public class SampleUpdatesDTOBuilder
{
    private TechId sampleId;

    private List<IEntityProperty> properties = new ArrayList<IEntityProperty>();

    private ExperimentIdentifier experimentIdentifierOrNull;

    private Collection<NewAttachment> attachments = new ArrayList<NewAttachment>();

    private int version;

    private SampleIdentifier sampleIdentifier;

    private String containerIdentifierOrNull;

    private List<String> parentCodes = new ArrayList<String>();

    private List<String> metaprojects = new ArrayList<String>();

    public SampleUpdatesDTOBuilder(Sample sample)
    {
        this(sample.getId());
        version = sample.getVersion();
        sampleIdentifier = SampleIdentifierFactory.parse(sample);
    }

    public SampleUpdatesDTOBuilder(long sampleId)
    {
        this.sampleId = new TechId(sampleId);
    }

    public SampleUpdatesDTOBuilder identifier(String identifier)
    {
        sampleIdentifier = SampleIdentifierFactory.parse(identifier);
        return this;
    }

    public SampleUpdatesDTOBuilder experiment(String experimentIdentifier)
    {
        experimentIdentifierOrNull = ExperimentIdentifierFactory.parse(experimentIdentifier);
        return this;
    }

    public SampleUpdatesDTOBuilder container(String identifier)
    {
        containerIdentifierOrNull = identifier;
        return this;
    }

    public SampleUpdatesDTOBuilder parent(String parentCode)
    {
        parentCodes.add(parentCode);
        return this;
    }

    public SampleUpdatesDTOBuilder property(String key, String value)
    {
        properties.add(new PropertyBuilder(key).value(value).getProperty());
        return this;
    }

    public SampleUpdatesDTOBuilder metaProject(String metaProjectCode)
    {
        metaprojects.add(metaProjectCode);
        return this;
    }

    public SampleUpdatesDTOBuilder attachment(NewAttachment attachment)
    {
        attachments.add(attachment);
        return this;
    }

    public SampleUpdatesDTO get()
    {
        SampleUpdatesDTO sampleUpdate =
                new SampleUpdatesDTO(sampleId, properties, experimentIdentifierOrNull, null, attachments,
                        version, sampleIdentifier, containerIdentifierOrNull,
                        parentCodes.toArray(new String[0]));
        sampleUpdate.setMetaprojectsOrNull(metaprojects.toArray(new String[0]));
        return sampleUpdate;
    }
}
