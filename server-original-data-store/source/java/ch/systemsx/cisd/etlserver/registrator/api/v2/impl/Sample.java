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

package ch.systemsx.cisd.etlserver.registrator.api.v2.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.etlserver.registrator.api.v2.ISample;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.IProjectImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleBatchUpdateDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class Sample extends SampleImmutable implements ISample
{
    private final List<NewAttachment> newAttachments;

    private final SampleBatchUpdateDetails updateDetails;

    /**
     * This code is derived from {@link ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder}, which is in a test source folder.
     */
    private static ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample buildSampleWithIdentifier(
            String identifier)
    {
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample =
                new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample();
        sample.setProperties(new ArrayList<IEntityProperty>());

        sample.setIdentifier(identifier);
        SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(identifier);
        sample.setCode(sampleIdentifier.getSampleCode());
        String sampleSubCode = sampleIdentifier.getSampleSubCode();
        sample.setSubCode(sampleSubCode);
        if (sampleIdentifier.isProjectLevel())
        {
            ProjectIdentifier projectLevel = sampleIdentifier.getProjectLevel();
            Space space = new Space();
            space.setCode(projectLevel.getSpaceCode());
            Project project = new Project();
            project.setSpace(space);
            project.setCode(projectLevel.getProjectCode());
            sample.setSpace(space);
            sample.setProject(project);
        } else if (sampleIdentifier.isSpaceLevel())
        {
            Space space = new Space();
            SpaceIdentifier spaceLevel = sampleIdentifier.getSpaceLevel();
            space.setCode(spaceLevel.getSpaceCode());
            sample.setSpace(space);
        }
        return sample;
    }

    public Sample(ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample sample)
    {
        super(sample);
        updateDetails = new SampleBatchUpdateDetails();
        newAttachments = new ArrayList<NewAttachment>();
        initializeUpdateDetails();
    }

    public Sample(SampleImmutable sample)
    {
        this(sample.getSample());
    }

    public Sample(String sampleIdentifier, String permId)
    {
        super(buildSampleWithIdentifier(sampleIdentifier), false);
        getSample().setPermId(permId);
        updateDetails = new SampleBatchUpdateDetails();
        newAttachments = new ArrayList<NewAttachment>();
        initializeUpdateDetails();
    }

    private void initializeUpdateDetails()
    {
        updateDetails.setExperimentUpdateRequested(false);
        updateDetails.setContainerUpdateRequested(false);
        updateDetails.setParentsUpdateRequested(false);
        updateDetails.setPropertiesToUpdate(new HashSet<String>());
    }

    @Override
    public void setExperiment(IExperimentImmutable experiment)
    {
        ExperimentImmutable exp = (ExperimentImmutable) experiment;

        if (exp == null)
        {
            getSample().setExperiment(null);
        } else
        {
            getSample().setExperiment(exp.getExperiment());
        }

        updateDetails.setExperimentUpdateRequested(true);
    }

    @Override
    public void setProject(IProjectImmutable project)
    {
        ProjectImmutable proj = (ProjectImmutable) project;
        if (proj == null)
        {
            getSample().setProject(null);
        } else
        {
            getSample().setProject(proj.getProject());
        }
    }

    @Override
    public void setPropertyValue(String propertyCode, String propertyValue)
    {
        EntityHelper.createOrUpdateProperty(getSample(), propertyCode, propertyValue);
        Set<String> propertiesToUpdate = updateDetails.getPropertiesToUpdate();
        propertiesToUpdate.add(propertyCode);
    }

    @Override
    public void setSampleType(String type)
    {
        SampleType sampleType = new SampleType();
        sampleType.setCode(type);

        getSample().setSampleType(sampleType);
    }

    @Override
    public void setContainer(ISampleImmutable container)
    {
        SampleImmutable containerImpl = (SampleImmutable) container;
        getSample().setContainer(containerImpl.getSample());
        updateDetails.setContainerUpdateRequested(true);
    }

    @Override
    public void setParentSampleIdentifiers(List<String> parentSampleIdentifiers)
    {
        HashSet<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample> parents =
                new HashSet<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample>();
        for (String identifier : parentSampleIdentifiers)
        {
            parents.add(buildSampleWithIdentifier(identifier));
        }

        getSample().setParents(parents);
        updateDetails.setParentsUpdateRequested(true);
    }

    @Override
    public void addAttachment(String filePath, String title, String description, byte[] content)
    {
        newAttachments.add(ConversionUtils.createAttachment(filePath, title, description, content));
    }

    /**
     * For conversion to updates DTO.
     */
    List<NewAttachment> getNewAttachments()
    {
        return newAttachments;
    }

    /**
     * Package-visible accessor for use in converting the Sample to an updates DTO.
     */
    SampleBatchUpdateDetails getUpdateDetails()
    {
        return updateDetails;
    }

}
