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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Extension of {@link DefaultDataSetInfoExtractor} which allows to choose a parent dataset(s) without specifying dataset codes.
 * 
 * @author Tomasz Pylak
 */
public class SmartParentDataSetInfoExtractor extends DefaultDataSetInfoExtractor
{
    /**
     * The value should be a java reqular expression with a parent dataset type code pattern. If specified the parent dataset will be chosen in a
     * smart way from all datasets of the specified type connected to the sample (or experiment if sample is not specified).<br>
     * Optional property, can be specified only if {@link #INDEX_OF_PARENT_DATA_SET_CODES} is not given and {@link #INDEX_OF_SAMPLE_CODE} and/or
     * {@link #INDEX_OF_EXPERIMENT_IDENTIFIER} is given.
     */
    @Private
    static final String SMART_PARENT_DATA_SET_RESOLUTION_DATASET_TYPE =
            "smart-parent-data-set-resolution-dataset-type";

    /**
     * Specifies the behavior of smart parent resolution when many parent dataset candidates are found. Cannot be specified if
     * {@link #SMART_PARENT_DATA_SET_RESOLUTION_DATASET_TYPE} is not given. Possible values:
     * <ul>
     * <li>CHOOSE_YOUNGEST - the dataset with the most recent registration date will be chosen as a parent
     * <li>CHOOSE_ALL - all datasets will become parents
     * <li>CHOOSE_NONE - child dataset will have no parents
     * <li>FAIL - dataset will not be registered and error will be reported
     * </ul>
     * Optional, FAIL by default.
     */
    @Private
    static final String SMART_PARENT_DATA_SET_RESOLUTION_MANY_PARENTS_MODE =
            "smart-parent-data-set-resolution-many-parents-mode";

    /**
     * Specifies if the registration should fail if no parents can be found during the smart parent resolution. Optional boolean property, true by
     * default. Cannot be specified if {@link #SMART_PARENT_DATA_SET_RESOLUTION_DATASET_TYPE} is not given.
     */
    @Private
    static final String SMART_PARENT_DATA_SET_RESOLUTION_FAIL_IF_MISSING =
            "smart-parent-data-set-resolution-fail-if-missing";

    private static enum ManyParentsModeEnum
    {
        CHOOSE_YOUNGEST, CHOOSE_ALL, CHOOSE_NONE, FAIL
    }

    // ----

    private final String datasetTypePatternOrNull;

    private final ManyParentsModeEnum manyParentsMode;

    private final boolean failWhenParentMissing;

    public SmartParentDataSetInfoExtractor(Properties properties)
    {
        super(properties);
        this.datasetTypePatternOrNull =
                properties.getProperty(SMART_PARENT_DATA_SET_RESOLUTION_DATASET_TYPE);
        if (datasetTypePatternOrNull != null && indexOfParentDataSetCodes.isUndefined() == false)
        {
            throw new ConfigurationFailureException(String.format(
                    "'%s' and '%s' specified at the same time.",
                    SMART_PARENT_DATA_SET_RESOLUTION_DATASET_TYPE, INDEX_OF_PARENT_DATA_SET_CODES));
        }
        String manyMode =
                properties.getProperty(SMART_PARENT_DATA_SET_RESOLUTION_MANY_PARENTS_MODE,
                        ManyParentsModeEnum.FAIL.name());
        this.manyParentsMode = ManyParentsModeEnum.valueOf(manyMode);
        this.failWhenParentMissing =
                PropertyUtils.getBoolean(properties,
                        SMART_PARENT_DATA_SET_RESOLUTION_FAIL_IF_MISSING, true);
    }

    @Override
    public DataSetInformation getDataSetInformation(final File incomingDataSetPath,
            IEncapsulatedOpenBISService openbisService) throws EnvironmentFailureException,
            UserFailureException
    {
        DataSetInformation dataSetInformation =
                super.getDataSetInformation(incomingDataSetPath, openbisService);
        if (datasetTypePatternOrNull != null)
        {
            List<AbstractExternalData> datasets =
                    tryFetchParentCandidates(openbisService, dataSetInformation);
            if (datasets != null)
            {
                datasets = filterByTypePattern(datasets, datasetTypePatternOrNull);
                List<String> parentDatasetCodes =
                        getParentDatasetCodes(dataSetInformation, datasets);
                dataSetInformation.setParentDataSetCodes(parentDatasetCodes);
            }
        }
        return dataSetInformation;
    }

    private static List<AbstractExternalData> filterByTypePattern(List<AbstractExternalData> datasets,
            String datasetTypePatternOrNull)
    {
        List<AbstractExternalData> filtered = new ArrayList<AbstractExternalData>();
        for (AbstractExternalData dataset : datasets)
        {
            if (dataset.getEntityType().getCode().matches(datasetTypePatternOrNull))
            {
                filtered.add(dataset);
            }
        }
        return filtered;
    }

    private List<AbstractExternalData> tryFetchParentCandidates(IEncapsulatedOpenBISService openbisService,
            DataSetInformation dataSetInformation)
    {
        List<AbstractExternalData> datasets = null;
        SampleIdentifier sampleIdentifier = dataSetInformation.getSampleIdentifier();
        if (sampleIdentifier != null)
        {
            Sample sample = openbisService.tryGetSampleWithExperiment(sampleIdentifier);
            datasets = openbisService.listDataSetsBySampleID(sample.getId(), true);
        } else
        {
            Experiment experiment = dataSetInformation.tryToGetExperiment();
            if (experiment != null)
            {
                datasets = openbisService.listDataSetsByExperimentID(experiment.getId());
            }
        }
        return datasets;
    }

    private List<String> getParentDatasetCodes(DataSetInformation dataSetInformation,
            List<AbstractExternalData> datasets)
    {
        if (datasets.size() == 0)
        {
            if (failWhenParentMissing)
            {
                throw UserFailureException
                        .fromTemplate(
                                "No parent datasets of the type '%s' connected to the same sample/experiment could be found.",
                                datasetTypePatternOrNull);
            } else
            {
                return new ArrayList<String>(); // no parents will be set
            }
        } else if (datasets.size() > 1)
        {
            if (manyParentsMode == ManyParentsModeEnum.FAIL)
            {
                throw UserFailureException
                        .fromTemplate("More than one parent dataset of the type '%s' connected to the same sample/experiment has been found.");
            } else if (manyParentsMode == ManyParentsModeEnum.CHOOSE_NONE)
            {
                return new ArrayList<String>(); // no parents will be set
            } else if (manyParentsMode == ManyParentsModeEnum.CHOOSE_ALL)
            {
                return extractCodes(datasets);
            } else if (manyParentsMode == ManyParentsModeEnum.CHOOSE_YOUNGEST)
            {
                return Arrays.asList(selectLastRegistered(datasets).getCode());
            } else
            {
                throw new IllegalStateException("Unhandled mode " + manyParentsMode);
            }
        } else
        {
            // exactly one
            return Arrays.asList(datasets.get(0).getCode());
        }
    }

    private AbstractExternalData selectLastRegistered(List<AbstractExternalData> datasets)
    {
        AbstractExternalData lastRegistered = null;
        for (AbstractExternalData dataset : datasets)
        {
            if (lastRegistered == null
                    || (dataset.getRegistrationDate().getTime() > lastRegistered
                            .getRegistrationDate().getTime()))
            {
                lastRegistered = dataset;
            }
        }
        return lastRegistered;
    }

    private static List<String> extractCodes(List<AbstractExternalData> datasets)
    {
        List<String> codes = new ArrayList<String>();
        for (AbstractExternalData dataset : datasets)
        {
            codes.add(dataset.getCode());
        }
        return codes;
    }
}
