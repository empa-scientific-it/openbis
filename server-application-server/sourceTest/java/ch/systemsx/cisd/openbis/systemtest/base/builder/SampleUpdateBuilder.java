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

import static ch.systemsx.cisd.openbis.systemtest.base.BaseTest.id;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;

public class SampleUpdateBuilder extends UpdateBuilder<SampleUpdatesDTO>
{
    private TechId sampleId;

    private String sampleCode;

    private ExperimentIdentifier experimentId;

    private int version;

    private SampleIdentifier sampleIdentifier;

    private List<Sample> parents;

    private Sample container;

    public SampleUpdateBuilder(ICommonServerForInternalUse commonServer,
            IGenericServer genericServer, Sample sample)
    {
        super(commonServer, genericServer);

        this.sampleId = new TechId(sample.getId());
        this.sampleCode = sample.getCode();
        if (sample.getExperiment() != null)
        {
            this.experimentId = new ExperimentIdentifier(sample.getExperiment());
        }
        this.version = sample.getVersion();
        this.sampleIdentifier = id(sample);
        this.parents = null;
        this.container = sample.getContainer();
    }

    public SampleUpdateBuilder toExperiment(Experiment experiment)
    {
        Project project = experiment.getProject();
        sampleIdentifier = new SampleIdentifier(new ProjectIdentifier(project.getSpace().getCode(), project.getCode()), this.sampleCode);
        this.experimentId = new ExperimentIdentifier(experiment);
        return this;
    }

    public SampleUpdateBuilder removingExperiment()
    {
        this.experimentId = null;
        return this;
    }

    public SampleUpdateBuilder removingSpace()
    {
        this.experimentId = null;
        this.sampleIdentifier =
                new SampleIdentifier(this.sampleCode);
        return this;
    }

    public SampleUpdateBuilder toSpace(Space space)
    {
        this.experimentId = null;
        this.sampleIdentifier =
                new SampleIdentifier(new SpaceIdentifier(
                        space.getCode()), this.sampleCode);
        return this;
    }

    public SampleUpdateBuilder toHaveParent(Sample sample)
    {
        return this.toHaveParents(sample);
    }

    public SampleUpdateBuilder toHaveParents(Sample... samples)
    {
        if (this.parents == null)
        {
            this.parents = new ArrayList<Sample>();
        }
        for (Sample parent : samples)
        {
            this.parents.add(parent);
        }
        return this;
    }

    public SampleUpdateBuilder toHaveContainer(Sample sample)
    {
        this.container = sample;
        return this;
    }

    public SampleUpdateBuilder removingContainer()
    {
        this.container = null;
        return this;
    }

    /*
     * public SampleUpdatesDTO(TechId sampleId, List<IEntityProperty> properties, ExperimentIdentifier experimentIdentifierOrNull,
     * Collection<NewAttachment> attachments, Date version, SampleIdentifier sampleIdentifier, String containerIdentifierOrNull, String[]
     * modifiedParentCodesOrNull)
     */

    @Override
    public SampleUpdatesDTO create()
    {
        String[] parentIdentifiers = null;
        if (this.parents != null)
        {
            parentIdentifiers = new String[this.parents.size()];
            for (int i = 0; i < this.parents.size(); i++)
            {
                parentIdentifiers[i] = this.parents.get(i).getIdentifier();
            }
        }
        return new SampleUpdatesDTO(this.sampleId, new ArrayList<IEntityProperty>(),
                this.experimentId, null, new ArrayList<NewAttachment>(), this.version,
                this.sampleIdentifier, this.container != null ? this.container.getIdentifier()
                        : null, parentIdentifiers);
    }

    @Override
    public void perform()
    {
        commonServer.updateSample(this.sessionToken, this.create());
    }

}