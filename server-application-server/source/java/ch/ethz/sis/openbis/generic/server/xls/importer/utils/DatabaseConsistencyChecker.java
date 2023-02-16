/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.importer.utils;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.server.xls.importer.enums.ImportTypes;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DatabaseConsistencyChecker
{
    private final String sessionToken;

    private final IApplicationServerApi api;

    private final Map<String, Integer> versions;

    public DatabaseConsistencyChecker(String sessionToken, IApplicationServerApi api, Map<String, Integer> versions)
    {
        this.sessionToken = sessionToken;
        this.api = api;
        this.versions = versions;
    }

    public void checkVersionsOnDataBase()
    {
        int numberOfTypesInJSON = 0;
        List<String> missing = new ArrayList<>();

        for (String version : this.versions.keySet())
        {
            numberOfTypesInJSON++;

            int indexOfSeparator = version.indexOf('-');
            String code = version.substring(indexOfSeparator + 1);

            if (version.startsWith(ImportTypes.DATASET_TYPE.getType()))
            {
                EntityTypePermId id = new EntityTypePermId(code);
                if (api.getDataSetTypes(sessionToken, Arrays.asList(id), new DataSetTypeFetchOptions()).isEmpty())
                {
                    missing.add(version);
                }
            } else if (version.startsWith(ImportTypes.EXPERIMENT_TYPE.getType()))
            {
                EntityTypePermId id = new EntityTypePermId(code);
                if (api.getExperimentTypes(sessionToken, Arrays.asList(id), new ExperimentTypeFetchOptions()).isEmpty())
                {
                    missing.add(version);
                }
            } else if (version.startsWith(ImportTypes.PROPERTY_TYPE.getType()))
            {
                PropertyTypePermId propertyTypePermId = new PropertyTypePermId(code);

                PropertyTypeFetchOptions fetchOptions = new PropertyTypeFetchOptions();
                fetchOptions.withVocabulary().withTerms().withVocabulary();

                if (api.getPropertyTypes(sessionToken, Arrays.asList(propertyTypePermId), fetchOptions).isEmpty())
                {
                    missing.add(version);
                }
            } else if (version.startsWith(ImportTypes.SAMPLE_TYPE.getType()))
            {
                EntityTypePermId id = new EntityTypePermId(code);
                if (api.getSampleTypes(sessionToken, Arrays.asList(id), new SampleTypeFetchOptions()).isEmpty())
                {
                    missing.add(version);
                }
            } else if (version.startsWith(ImportTypes.VOCABULARY_TERM.getType()))
            {
                /*
                 *  The "-" is a valid code character and also the separator between vocabulary and term code.
                 *  This lead to situations where is not possible to know where the vocabulary and term code starts/ends
                 *  The only backwards compatible solution is to search for all possible permutations
                 */
                String[] codeParts = code.split("-");
                List<VocabularyTermPermId> possiblePermIds = new ArrayList<>();
                for (int idx = 0; idx < codeParts.length; idx++) {
                    StringBuilder vocabularyCode = new StringBuilder();
                    for (int idx2 = 0; idx2 <= idx; idx2++) {
                        if (vocabularyCode.length() > 0) {
                            vocabularyCode.append("-");
                        }
                        vocabularyCode.append(codeParts[idx2]);
                    }
                    StringBuilder termCode = new StringBuilder();
                    for (int idx2 = idx + 1; idx2 < codeParts.length; idx2++) {
                        if (termCode.length() > 0) {
                            termCode.append("-");
                        }
                        termCode.append(codeParts[idx2]);
                    }
                    if (vocabularyCode.length() > 0 && termCode.length() > 0) {
                        possiblePermIds.add(new VocabularyTermPermId(termCode.toString(), vocabularyCode.toString()));
                    }
                }
                VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
                fetchOptions.withVocabulary();
                if (api.getVocabularyTerms(sessionToken, possiblePermIds, fetchOptions).isEmpty())
                {
                    missing.add(version);
                }
            } else if (version.startsWith(ImportTypes.VOCABULARY_TYPE.getType()))
            {
                VocabularyPermId vocabularyPermId = new VocabularyPermId(code);

                VocabularyFetchOptions fetchOptions = new VocabularyFetchOptions();
                fetchOptions.withTerms().withVocabulary();

                if (api.getVocabularies(sessionToken, Arrays.asList(vocabularyPermId), fetchOptions).isEmpty())
                {
                    missing.add(version);
                }
            } else
            {
                throw new UserFailureException("Unknown type in xls-import-version-info.json " + version);
            }
        }

        if (numberOfTypesInJSON > 0 && missing.size() == numberOfTypesInJSON)
        {
            throw new UserFailureException("All types from xls-import-version-info.json do not exist in the database. Missing: " + missing
                    + " The database may have been deleted. Please delete xls-import-version-info.json too and restart the app.");
        }
    }
}
