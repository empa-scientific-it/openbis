/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.screening.systemtests;

import java.util.Collections;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractMicroscopyImageDropboxTestCase extends AbstractImageDropboxTestCase
{
    @Override
    protected void registerAdditionalOpenbisMetaData()
    {
        commonServer = (ICommonServer) applicationContext
                .getBean(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER);
        sessionToken = commonServer.tryAuthenticate("admin", "a").getSessionToken();
        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode("MICROSCOPY_EXPERIMENT");
        registerExperimentType(commonServer, experimentType);
        SampleType sampleType = new SampleType();
        sampleType.setCode("MICROSCOPY_SAMPLE");
        sampleType.setGeneratedCodePrefix("M-");
        registerSampleType(commonServer, sampleType);
        DataSetType dataSetType = new DataSetType("MICROSCOPY_IMG");
        registerDataSetType(commonServer, dataSetType);
        dataSetType = new DataSetType("MICROSCOPY_IMG_OVERVIEW");
        registerDataSetType(commonServer, dataSetType);
        dataSetType = new DataSetType("MICROSCOPY_REPRESENTATIVE_IMG");
        registerDataSetType(commonServer, dataSetType);
        dataSetType = new DataSetType("MICROSCOPY_IMG_THUMBNAIL");
        registerDataSetType(commonServer, dataSetType);
        dataSetType = new DataSetType("MICROSCOPY_IMG_CONTAINER");
        registerDataSetType(commonServer, dataSetType);
        registerProject(commonServer, "/TEST/TEST-PROJECT");
    }

    private void registerProject(ICommonServer server, String identifier)
    {
        for (Project project : server.listProjects(sessionToken))
        {
            if (project.getIdentifier().equals(identifier))
            {
                return;
            }
        }
        ProjectIdentifier projectIdentifier = ProjectIdentifierFactory.parse(identifier);
        registerSpace(server, projectIdentifier.getSpaceCode());
        server.registerProject(sessionToken, projectIdentifier, null, null, Collections.<NewAttachment> emptySet());
    }

    private void registerSpace(ICommonServer server, String spaceCode)
    {
        for (Space space : server.listSpaces(sessionToken))
        {
            if (space.getCode().equals(spaceCode))
            {
                return;
            }
        }
        server.registerSpace(sessionToken, spaceCode, null);
    }

    private void registerExperimentType(ICommonServer server, ExperimentType experimentType)
    {
        for (EntityType type : server.listExperimentTypes(sessionToken))
        {
            if (type.getCode().equals(experimentType.getCode()))
            {
                return;
            }
        }
        server.registerExperimentType(sessionToken, experimentType);
    }

    private void registerSampleType(ICommonServer server, SampleType sampleType)
    {
        for (EntityType type : server.listSampleTypes(sessionToken))
        {
            if (type.getCode().equals(sampleType.getCode()))
            {
                return;
            }
        }
        server.registerSampleType(sessionToken, sampleType);
    }

    private void registerDataSetType(ICommonServer server, DataSetType dataSetType)
    {
        for (EntityType type : server.listDataSetTypes(sessionToken))
        {
            if (type.getCode().equals(dataSetType.getCode()))
            {
                return;
            }
        }
        server.registerDataSetType(sessionToken, dataSetType);
    }

}
