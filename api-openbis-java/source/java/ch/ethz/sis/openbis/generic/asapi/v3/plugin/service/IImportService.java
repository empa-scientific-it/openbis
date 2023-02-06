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

package ch.ethz.sis.openbis.generic.asapi.v3.plugin.service;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

/**
 * @author pkupczyk
 */
@JsonIgnoreType
public interface IImportService
{

    public String createExperiments(String sessionToken, String uploadKey, String experimentTypeCode, boolean async, String userEmail);

    public String updateExperiments(String sessionToken, String uploadKey, String experimentTypeCode, boolean async, String userEmail);

    public String createSamples(String sessionToken, String uploadKey, String sampleTypeCode, String defaultSpaceIdentifier,
            String spaceIdentifierOverride, String experimentIdentifierOverride, boolean updateExisting, boolean async, String userEmail);

    public String updateSamples(String sessionToken, String uploadKey, String sampleTypeCode, String defaultSpaceIdentifier,
            String spaceIdentifierOverride, String experimentIdentifierOverride, boolean async, String userEmail);

    public String updateDataSets(String sessionToken, String uploadKey, String dataSetTypeCode, boolean async, String userEmail);

    public String createMaterials(String sessionToken, String uploadKey, String materialTypeCode, boolean updateExisting, boolean async,
            String userEmail);

    public String updateMaterials(String sessionToken, String uploadKey, String materialTypeCode, boolean ignoreUnregistered, boolean async,
            String userEmail);

    public String generalImport(String sessionToken, String uploadKey, String defaultSpaceIdentifier, boolean updateExisting, boolean async,
            String userEmail);

    public String customImport(String sessionToken, String uploadKey, String customImportCode, boolean async, String userEmail);

}
