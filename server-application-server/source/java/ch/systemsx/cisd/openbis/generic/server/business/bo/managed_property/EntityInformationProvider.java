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

package ch.systemsx.cisd.openbis.generic.server.business.bo.managed_property;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.EntityLinkElementTranslator;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityLinkElement;

/**
 * @author Piotr Buczek
 */
@Component(value = ResourceNames.ENTITY_INFORMATION_PROVIDER)
public class EntityInformationProvider implements IEntityInformationProvider
{
    private final IDAOFactory daoFactory;

    @Autowired
    public EntityInformationProvider(IDAOFactory daoFactory)
    {
        assert daoFactory != null;
        this.daoFactory = daoFactory;
    }

    @Override
    public String getIdentifier(IEntityLinkElement entityLink)
    {
        final EntityKind entityKind =
                EntityLinkElementTranslator.translate(entityLink.getEntityLinkKind());
        final String permId = entityLink.getPermId();
        return getIdentifier(entityKind, permId);
    }

    private String getIdentifier(EntityKind entityKind, String permId)
    {
        IIdentifierHolder identifierHolderOrNull = null;
        switch (entityKind)
        {
            case EXPERIMENT:
                identifierHolderOrNull = daoFactory.getExperimentDAO().tryGetByPermID(permId);
                break;
            case SAMPLE:
                identifierHolderOrNull = daoFactory.getSampleDAO().tryToFindByPermID(permId);
                break;
            case DATA_SET:
                identifierHolderOrNull = daoFactory.getDataDAO().tryToFindDataSetByCode(permId);
                break;
            case MATERIAL:
                MaterialIdentifier idOrNull = MaterialIdentifier.tryParseIdentifier(permId);
                if (idOrNull == null)
                {
                    return null;
                } else
                {
                    final MaterialPE materialOrNull =
                            daoFactory.getMaterialDAO().tryFindMaterial(idOrNull);
                    if (materialOrNull == null)
                    {
                        return null;
                    } else
                    {
                        identifierHolderOrNull = new IIdentifierHolder()
                            {

                                @Override
                                public String getIdentifier()
                                {
                                    return new MaterialIdentifier(materialOrNull.getCode(),
                                            materialOrNull.getEntityType().getCode()).print();
                                }
                            };
                    }
                }
        }
        return identifierHolderOrNull == null ? null : identifierHolderOrNull.getIdentifier();
    }

    private SpacePE tryGetSpaceByCode(String spaceCode)
    {
        SpacePE space =
                daoFactory.getSpaceDAO().tryFindSpaceByCode(spaceCode);
        if (space == null)
        {
            throw UserFailureException.fromTemplate("space '%s' doesn't exist ", spaceCode);
        }

        return space;
    }
    
    private ProjectPE tryGetProjectByCode(String spaceCode, String projectCode)
    {
        ProjectPE project = daoFactory.getProjectDAO().tryFindProject(spaceCode, projectCode);
        if (project == null)
        {
            throw new UserFailureException("Project /" + spaceCode + "/" + projectCode + " doesn't exist.");
        }
        return project;
    }

    @Override
    public String getSamplePermId(String spaceCode, String sampleCode)
    {
        SpacePE space = tryGetSpaceByCode(spaceCode);
        SamplePE sample = daoFactory.getSampleDAO().tryFindByCodeAndSpace(sampleCode, space);
        return (sample != null) ? sample.getPermId() : null;
    }

    @Override
    public String getProjectSamplePermId(String spaceCode, String projectCode, String sampleCode)
    {
        
        ProjectPE project = tryGetProjectByCode(spaceCode, projectCode);
        SamplePE sample = daoFactory.getSampleDAO().tryfindByCodeAndProject(sampleCode, project);
        return (sample != null) ? sample.getPermId() : null;
    }

    @Override
    public String getSamplePermId(String sampleIdentifier)
    {
        SampleIdentifier identifier = SampleIdentifierFactory.parse(sampleIdentifier);
        String sampleCode = identifier.getSampleCode();
        if (identifier.isProjectLevel())
        {
            ProjectIdentifier projectIdentifier = identifier.getProjectLevel();
            String spaceCode = projectIdentifier.getSpaceCode();
            String projectCode = projectIdentifier.getProjectCode();
            return getProjectSamplePermId(spaceCode, projectCode, sampleCode);
        }
        if (identifier.isSpaceLevel())
        {
            String spaceCode = identifier.getSpaceLevel().getSpaceCode();
            return getSamplePermId(spaceCode, sampleCode);
        } else
        {
            SamplePE sample =
                    daoFactory.getSampleDAO().tryFindByCodeAndDatabaseInstance(sampleCode);
            return (sample != null) ? sample.getPermId() : null;
        }
    }

    private List<String> getSamplesPermIds(List<SamplePE> samples)
    {
        if (samples == null)
        {
            return new ArrayList<String>(0);
        }

        List<String> samplePermIds = new ArrayList<String>(samples.size());
        for (SamplePE sample : samples)
        {
            samplePermIds.add(sample.getPermId());
        }
        return samplePermIds;
    }

    private SamplePE getSampleByPermId(String permId)
    {
        SamplePE sample = daoFactory.getSampleDAO().tryToFindByPermID(permId);

        if (sample == null)
        {
            throw UserFailureException.fromTemplate("sample '%s' doesn't exist", permId);
        }

        return sample;
    }

    @Override
    public List<String> getSampleParentPermIds(String spaceCode, String sampleCode)
    {
        SpacePE space = tryGetSpaceByCode(spaceCode);
        SamplePE sample = daoFactory.getSampleDAO().tryFindByCodeAndSpace(sampleCode, space);

        if (sample == null)
        {
            throw UserFailureException.fromTemplate("sample '%s' doesn't exist in space %s",
                    sampleCode, spaceCode);
        }

        return getSamplesPermIds(sample.getParents());
    }

    @Override
    public List<String> getProjectSampleParentPermIds(String spaceCode, String projectCode, String sampleCode)
    {
        ProjectPE project = tryGetProjectByCode(spaceCode, projectCode);
        SamplePE sample = daoFactory.getSampleDAO().tryfindByCodeAndProject(sampleCode, project);
        if (sample == null)
        {
            throw new UserFailureException("Sample /" + spaceCode + "/" + projectCode + "/" 
                    + sampleCode + " doesn't exist.");
        }
        return getSamplesPermIds(sample.getParents());
    }

    @Override
    public List<String> getSampleParentPermIds(String permId)
    {
        return getSamplesPermIds(getSampleByPermId(permId).getParents());
    }

    @Override
    public String getSamplePropertyValue(String permId, String propertyCode)
    {
        SamplePE sample = getSampleByPermId(permId);
        for (SamplePropertyPE property : sample.getProperties())
        {
            if (propertyCode.equalsIgnoreCase(property.getEntityTypePropertyType()
                    .getPropertyType().getCode()))
            {
                return property.getValue();
            }
        }
        return "";
    }
}
