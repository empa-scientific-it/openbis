/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.service;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.IImportService;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.ICustomImportService;
import ch.systemsx.cisd.openbis.generic.shared.IEntityImportService;
import ch.systemsx.cisd.openbis.generic.shared.ResourceNames;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * @author pkupczyk
 */
@Component(value = ResourceNames.IMPORT_SERVICE)
public class ImportService implements IImportService
{

    @Autowired
    private IEntityImportService entityImportService;

    @Autowired
    private ICustomImportService customImportService;

    @Override
    public String createExperiments(String sessionToken, String uploadKey, String experimentTypeCode, boolean async, String userEmail)
    {
        check(sessionToken, uploadKey, experimentTypeCode, async, userEmail);

        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode(experimentTypeCode);

        List<BatchRegistrationResult> results = entityImportService.registerExperiments(experimentType, uploadKey, async, userEmail);
        return translateResults(results);
    }

    @Override
    public String updateExperiments(String sessionToken, String uploadKey, String experimentTypeCode, boolean async, String userEmail)
    {
        check(sessionToken, uploadKey, experimentTypeCode, async, userEmail);

        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode(experimentTypeCode);

        List<BatchRegistrationResult> results = entityImportService.updateExperiments(experimentType, uploadKey, async, userEmail);
        return translateResults(results);
    }

    @Override
    public String createSamples(String sessionToken, String uploadKey, String sampleTypeCode, String defaultSpaceIdentifier,
            String spaceIdentifierOverride, String experimentIdentifierOverride, boolean updateExisting, boolean async, String userEmail)
    {
        check(sessionToken, uploadKey, sampleTypeCode, async, userEmail);

        SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypeCode);

        List<BatchRegistrationResult> results = entityImportService.registerSamplesWithSilentOverrides(sampleType, spaceIdentifierOverride,
                experimentIdentifierOverride, uploadKey, async, userEmail, defaultSpaceIdentifier, updateExisting);
        return translateResults(results);
    }

    @Override
    public String updateSamples(String sessionToken, String uploadKey, String sampleTypeCode, String defaultSpaceIdentifier,
            String spaceIdentifierOverride, String experimentIdentifierOverride, boolean async, String userEmail)
    {
        check(sessionToken, uploadKey, sampleTypeCode, async, userEmail);

        SampleType sampleType = new SampleType();
        sampleType.setCode(sampleTypeCode);

        List<BatchRegistrationResult> results = entityImportService.updateSamplesWithSilentOverrides(sampleType, spaceIdentifierOverride,
                experimentIdentifierOverride, uploadKey, async, userEmail, defaultSpaceIdentifier);
        return translateResults(results);
    }

    @Override
    public String updateDataSets(String sessionToken, String uploadKey, String dataSetTypeCode, boolean async, String userEmail)
    {
        check(sessionToken, uploadKey, dataSetTypeCode, async, userEmail);

        DataSetType dataSetType = new DataSetType();
        dataSetType.setCode(dataSetTypeCode);

        List<BatchRegistrationResult> results = entityImportService.updateDataSets(dataSetType, uploadKey, async, userEmail);
        return translateResults(results);
    }

    @Override
    public String createMaterials(String sessionToken, String uploadKey, String materialTypeCode, boolean updateExisting, boolean async,
            String userEmail)
    {
        check(sessionToken, uploadKey, materialTypeCode, async, userEmail);

        MaterialType materialType = new MaterialType();
        materialType.setCode(materialTypeCode);

        List<BatchRegistrationResult> results = entityImportService.registerMaterials(materialType, updateExisting, uploadKey, async, userEmail);
        return translateResults(results);
    }

    @Override
    public String updateMaterials(String sessionToken, String uploadKey, String materialTypeCode, boolean ignoreUnregistered, boolean async,
            String userEmail)
    {
        check(sessionToken, uploadKey, materialTypeCode, async, userEmail);

        MaterialType materialType = new MaterialType();
        materialType.setCode(materialTypeCode);

        List<BatchRegistrationResult> results = entityImportService.updateMaterials(materialType, uploadKey, ignoreUnregistered, async, userEmail);
        return translateResults(results);
    }

    @Override
    public String generalImport(String sessionToken, String uploadKey, String defaultSpaceIdentifier, boolean updateExisting, boolean async,
            String userEmail)
    {
        check(sessionToken, uploadKey, async, userEmail);

        List<BatchRegistrationResult> results =
                entityImportService.registerOrUpdateSamplesAndMaterials(uploadKey, defaultSpaceIdentifier, updateExisting, async, userEmail);
        return translateResults(results);
    }

    @Override
    public String customImport(String sessionToken, String uploadKey, String customImportCode, boolean async, String userEmail)
    {
        check(sessionToken, uploadKey, async, userEmail);

        if (customImportCode == null)
        {
            throw new UserFailureException("Custom import code cannot be null");
        }

        List<BatchRegistrationResult> results =
                customImportService.performCustomImport(uploadKey, customImportCode, async, userEmail);
        return translateResults(results);
    }

    protected void check(String sessionToken, String uploadKey, String typeCode, boolean async, String userEmail)
    {
        check(sessionToken, uploadKey, async, userEmail);

        if (typeCode == null)
        {
            throw new UserFailureException("Type code cannot be null");
        }
    }

    protected void check(String sessionToken, String uploadKey, boolean async, String userEmail)
    {
        if (sessionToken == null)
        {
            throw new UserFailureException("Session token cannot be null");
        }
        if (uploadKey == null)
        {
            throw new UserFailureException("Upload key cannot be null");
        }
        if (async && userEmail == null)
        {
            throw new UserFailureException("User email cannot be null for an asynchronous import");
        }
    }

    protected String translateResults(List<BatchRegistrationResult> results)
    {
        StringBuilder message = new StringBuilder();
        Iterator<BatchRegistrationResult> iter = results.iterator();

        while (iter.hasNext())
        {
            BatchRegistrationResult result = iter.next();
            if (result != null)
            {
                message.append(result.getMessage());
                if (iter.hasNext())
                {
                    message.append("\n");
                }
            }
        }

        return message.toString();
    }

}
