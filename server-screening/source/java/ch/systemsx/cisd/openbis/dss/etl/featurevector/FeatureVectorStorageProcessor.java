/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.featurevector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.AbstractDelegatingStorageProcessor;
import ch.systemsx.cisd.etlserver.AbstractDelegatingStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.DispatcherStorageProcessor.IDispatchableStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.etl.HCSContainerDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureDefinition;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureVectorDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Extract features from the file and store them in the database.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class FeatureVectorStorageProcessor extends AbstractDelegatingStorageProcessor implements
        IDispatchableStorageProcessor
{

    private static final String ORIGINAL_DIR = ScreeningConstants.ORIGINAL_DATA_DIR;

    private final FeatureVectorStorageProcessorConfiguration configuration;

    private final DataSource dataSource;

    private final IEncapsulatedOpenBISService openBisService;

    public FeatureVectorStorageProcessor(Properties properties)
    {
        super(properties);
        this.configuration = new FeatureVectorStorageProcessorConfiguration(properties);
        this.dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
        this.openBisService = ServiceProvider.getOpenBISService();
    }

    /**
     * Accepts all non-image datasets (and assumes they are single CSV files or FeatureVectorDataSetInformation).
     */
    @Override
    public boolean accepts(DataSetInformation dataSetInformation, File incomingDataSet)
    {
        return dataSetInformation instanceof ImageDataSetInformation == false
                && (dataSetInformation instanceof FeatureVectorDataSetInformation || dataSetInformation
                        .getDataSetType().getCode().toUpperCase()
                        .matches(ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN));
    }

    @Override
    public IStorageProcessorTransaction createTransaction(
            StorageProcessorTransactionParameters parameters)
    {
        final IStorageProcessorTransaction superTransaction = super.createTransaction(parameters);
        return new FeatureVectorStorageProcessorTransaction(parameters, superTransaction, this);
    }

    private static final class FeatureVectorStorageProcessorTransaction extends
            AbstractDelegatingStorageProcessorTransaction
    {
        private static final long serialVersionUID = 1L;

        private transient IImagingQueryDAO dataAccessObject = null;

        private final transient FeatureVectorStorageProcessor processor;

        private FeatureVectorStorageProcessorTransaction(
                StorageProcessorTransactionParameters parameters,
                IStorageProcessorTransaction transaction, FeatureVectorStorageProcessor processor)
        {
            super(parameters, transaction);
            this.processor = processor;
        }

        @Override
        protected File executeStoreData(ITypeExtractor typeExtractor, IMailClient mailClient)
        {
            nestedTransaction.storeData(typeExtractor, mailClient, incomingDataSetDirectory);

            dataAccessObject = processor.createDAO();
            File parent = new File(nestedTransaction.getStoredDataDirectory(), ORIGINAL_DIR);
            File dataSet = new File(parent, incomingDataSetDirectory.getName());
            try
            {
                processor.loadDataSetIntoDatabase(dataAccessObject, dataSet, dataSetInformation);
            } catch (IOException ex)
            {
                throw new IOExceptionUnchecked(ex);
            }
            return nestedTransaction.getStoredDataDirectory();
        }

        @Override
        protected void executeCommit()
        {
            nestedTransaction.commit();

            if (null == dataAccessObject)
            {
                return;
            }

            dataAccessObject.commit();
            closeDataAccessObject();
        }

        @Override
        protected UnstoreDataAction executeRollback(Throwable ex)
        {
            // Delete the data from the database
            if (null != dataAccessObject)
            {
                dataAccessObject.rollback();
                closeDataAccessObject();
            }

            return nestedTransaction.rollback(ex);
        }

        /**
         * Close the DAO and set it to null to make clear that it is not initialized.
         */
        private void closeDataAccessObject()
        {
            dataAccessObject.close();
            dataAccessObject = null;
        }
    }

    private void loadDataSetIntoDatabase(IImagingQueryDAO dataAccessObject, File dataSet,
            DataSetInformation dataSetInformation) throws IOException
    {
        HCSContainerDatasetInfo datasetInfo = createScreeningDatasetInfo(dataSetInformation);

        List<CanonicalFeatureVector> fvecs =
                extractCanonicalFeatureVectors(dataSet, dataSetInformation,
                        datasetInfo.getContainerGeometry());

        FeatureVectorUploader uploader =
                new FeatureVectorUploader(dataAccessObject, datasetInfo,
                        dataSetInformation.getDataSetCode());
        uploader.uploadFeatureVectors(fvecs);
    }

    private List<CanonicalFeatureVector> extractCanonicalFeatureVectors(File dataSet,
            DataSetInformation dataSetInformation, Geometry plateGeometry) throws IOException
    {
        List<FeatureDefinition> featureDefinitions;
        if (dataSetInformation instanceof FeatureVectorDataSetInformation)
        {
            featureDefinitions =
                    ((FeatureVectorDataSetInformation) dataSetInformation).getFeatures();
        } else
        {
            featureDefinitions = CsvFeatureVectorParser.parse(dataSet, configuration);
        }
        return extractCanonicalFeatureVectors(featureDefinitions, plateGeometry);
    }

    private static List<CanonicalFeatureVector> extractCanonicalFeatureVectors(
            List<FeatureDefinition> featuresDefinitions, Geometry plateGeometry)
    {
        List<CanonicalFeatureVector> canonicalFeatureVectors =
                new ArrayList<CanonicalFeatureVector>();
        for (FeatureDefinition featureDefinitionValues : featuresDefinitions)
        {
            CanonicalFeatureVector canonicalFeatureVector =
                    featureDefinitionValues.getCanonicalFeatureVector(plateGeometry);
            canonicalFeatureVectors.add(canonicalFeatureVector);
        }
        return canonicalFeatureVectors;
    }

    private HCSContainerDatasetInfo createScreeningDatasetInfo(DataSetInformation dataSetInformation)
    {
        Sample sampleOrNull = tryFindSampleForDataSet(dataSetInformation);
        return HCSContainerDatasetInfo.createScreeningDatasetInfoWithSample(dataSetInformation,
                sampleOrNull);
    }

    private Sample tryFindSampleForDataSet(DataSetInformation dataSetInformation)
    {
        Sample sampleOrNull = dataSetInformation.tryToGetSample();
        if (null == sampleOrNull)
        {
            // check the parent data sets for a sample
            List<String> parentDataSetCodes = dataSetInformation.getParentDataSetCodes();
            for (String dataSetCode : parentDataSetCodes)
            {
                AbstractExternalData externalData = openBisService.tryGetDataSet(dataSetCode);
                if (externalData == null)
                {
                    throw new UserFailureException("Cannot find a parent dataset in openBIS: "
                            + dataSetCode);
                }
                if (externalData.getSample() != null)
                {
                    sampleOrNull = externalData.getSample();
                    break;
                }
            }
        }
        return sampleOrNull;
    }

    private IImagingQueryDAO createDAO()
    {
        return QueryTool.getQuery(dataSource, IImagingQueryDAO.class);
    }

}
