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

package ch.systemsx.cisd.openbis.systemtest.base.builder;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class SampleBuilder extends Builder<Sample>
{
    private static int number;

    private Experiment experiment;

    private Space space;

    private List<Sample> parents;

    private Sample container;

    private String id;

    public SampleBuilder(ICommonServerForInternalUse commonServer, IGenericServer genericServer)
    {
        super(commonServer, genericServer);
        this.parents = new ArrayList<Sample>();
        this.container = null;
        this.id = "S" + number++;
    }

    @SuppressWarnings("hiding")
    public SampleBuilder inExperiment(Experiment experiment)
    {
        this.experiment = experiment;
        return this;
    }

    public SampleBuilder withCode(String code)
    {
        this.id = code;
        return this;
    }

    @SuppressWarnings("hiding")
    public SampleBuilder inSpace(Space space)
    {
        this.space = space;
        return this;
    }

    public SampleBuilder inContainer(Sample sample)
    {
        this.container = sample;
        return this;
    }

    public SampleBuilder withParent(Sample parent)
    {
        return this.withParents(parent);
    }

    public SampleBuilder withParents(Sample... samples)
    {
        for (Sample parent : samples)
        {
            this.parents.add(parent);
        }
        return this;
    }

    @Override
    public Sample create()
    {

        SampleType sampleType = new SampleType();
        sampleType.setCode("ST" + number++);
        sampleType.setContainerHierarchyDepth(0);

        sampleType.setDescription("description");
        sampleType.setGeneratedCodePrefix("prefix");
        sampleType.setListable(true);
        sampleType.setGeneratedFromHierarchyDepth(0);
        sampleType.setSampleTypePropertyTypes(new ArrayList<SampleTypePropertyType>());
        commonServer.registerSampleType(sessionToken, sampleType);

        String identifier;
        if (this.experiment != null)
        {
            
            Project project = this.experiment.getProject();
            identifier = (SamplePE.projectSamplesEnabled ? project.getIdentifier() 
                                                         : "/" + project.getSpace().getCode()) 
                    + "/" + this.id.toUpperCase();
        } else if (this.space != null)
        {
            identifier = "/" + this.space.getCode() + "/" + this.id.toUpperCase();
        } else
        {
            identifier = "/" + this.id.toUpperCase();
        }

        NewSample data = new NewSample();
        data.setIdentifier(identifier);
        data.setAttachments(new ArrayList<NewAttachment>());
        data.setContainerIdentifier(this.container != null ? this.container.getIdentifier() : null);
        // data.setCurrentContainerIdentifier(null);
        if (this.experiment != null)
        {
            data.setExperimentIdentifier(this.experiment.getIdentifier());
        }

        String[] parentIds = new String[this.parents.size()];
        int i = 0;
        for (Sample s : this.parents)
        {
            parentIds[i] = s.getIdentifier();
            i++;
        }

        data.setParentsOrNull(parentIds);
        data.setProperties(new IEntityProperty[0]);
        data.setSampleType(sampleType);

        genericServer.registerSample(sessionToken, data, new ArrayList<NewAttachment>());

        BasicEntityDescription info = new BasicEntityDescription(EntityKind.SAMPLE, identifier);
        IEntityInformationHolderWithPermId holder =
                commonServer.getEntityInformationHolder(sessionToken, info);
        return commonServer.getSampleInfo(sessionToken, new TechId(holder.getId())).getParent();
    }
}