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

            String code = version.split("-")[1];

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
                String vocabularyCode = version.split("-")[1];
                String termCode = version.split("-")[2];
                VocabularyTermPermId termId = new VocabularyTermPermId(termCode, vocabularyCode);

                VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
                fetchOptions.withVocabulary();

                if (api.getVocabularyTerms(sessionToken, Arrays.asList(termId), fetchOptions).isEmpty())
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
            throw new UserFailureException("All types from xls-import-version-info.json does not exist in the database. Missing: " + missing
                    + " The database may have been deleted. Please delete xls-import-version-info.json too and restart the app.");
        }
    }
}
