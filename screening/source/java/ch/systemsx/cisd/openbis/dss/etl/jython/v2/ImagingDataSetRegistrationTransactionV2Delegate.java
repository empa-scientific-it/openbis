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

package ch.systemsx.cisd.openbis.dss.etl.jython.v2;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import net.lemnik.eodsql.DynamicTransactionQuery;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationContext;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSetUpdatable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperiment;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IMaterial;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IProject;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISample;
import ch.systemsx.cisd.etlserver.registrator.api.v1.ISpace;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureDefinition;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureVectorDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureVectorDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImageDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v2.IFeatureVectorDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v2.SimpleFeatureVectorDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CsvFeatureVectorParser;
import ch.systemsx.cisd.openbis.dss.etl.jython.ImagingDataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.IAuthorizationService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExternalDataManagementSystemImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IMaterialImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IProjectImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISpaceImmutable;

/**
 * @author Jakub Straszewski
 */
public class ImagingDataSetRegistrationTransactionV2Delegate implements
        IImagingDataSetRegistrationTransactionV2
{
    public ImagingDataSetRegistrationTransactionV2Delegate(
            ImagingDataSetRegistrationTransaction transaction)
    {
        this.transaction = transaction;
    }

    private final ImagingDataSetRegistrationTransaction transaction;

    @Override
    public IImageDataSet createNewImageDataSet(SimpleImageDataConfig imageDataSet,
            File incomingFolderWithImages)
    {
        return transaction.createNewImageDataSet(imageDataSet, incomingFolderWithImages);
    }

    @Override
    public IFeatureVectorDataSet createNewFeatureVectorDataSet(
            SimpleFeatureVectorDataConfig featureDataSetConfig, File featureVectorFileOrNull)
    {
        List<FeatureDefinition> featureDefinitions;
        Properties properties = featureDataSetConfig.getProperties();
        if (properties == null)
        {
            featureDefinitions =
                    ((FeaturesBuilder) featureDataSetConfig.getFeaturesBuilder())
                            .getFeatureDefinitionValuesList();
        } else
        {
            try
            {
                featureDefinitions = CsvFeatureVectorParser.parse(featureVectorFileOrNull, properties);
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
        DataSetRegistrationDetails<FeatureVectorDataSetInformation> registrationDetails =
                transaction.getFactory().createFeatureVectorRegistrationDetails(featureDefinitions);
        @SuppressWarnings("unchecked")
        DataSet<FeatureVectorDataSetInformation> dataSet =
                (DataSet<FeatureVectorDataSetInformation>) transaction
                        .createNewDataSet(registrationDetails);
        return new FeatureVectorDataSet(dataSet);
    }

    @Override
    public IDataSet createNewDataSet()
    {
        return transaction.createNewDataSet();
    }

    @Override
    public IDataSet createNewDataSet(String dataSetType)
    {
        return transaction.createNewDataSet(dataSetType);
    }

    @Override
    public IDataSet createNewDataSet(String dataSetType, String dataSetCode)
    {
        return transaction.createNewDataSet(dataSetType, dataSetCode);
    }

    @Override
    public IDataSetImmutable getDataSet(String dataSetCode)
    {
        return transaction.getDataSet(dataSetCode);
    }

    @Override
    public IDataSetUpdatable getDataSetForUpdate(String dataSetCode)
    {
        return transaction.getDataSetForUpdate(dataSetCode);
    }

    @Override
    public IDataSetUpdatable makeDataSetMutable(IDataSetImmutable dataSet)
    {
        return transaction.makeDataSetMutable(dataSet);
    }

    @Override
    public ISampleImmutable getSample(String sampleIdentifierString)
    {
        return transaction.getSample(sampleIdentifierString);
    }

    @Override
    public ISample getSampleForUpdate(String sampleIdentifierString)
    {
        return transaction.getSampleForUpdate(sampleIdentifierString);
    }

    @Override
    public ISample makeSampleMutable(ISampleImmutable sample)
    {
        return transaction.makeSampleMutable(sample);
    }

    @Override
    public ISample createNewSample(String sampleIdentifierString, String sampleTypeCode)
    {
        return transaction.createNewSample(sampleIdentifierString, sampleTypeCode);
    }

    @Override
    public IExperimentImmutable getExperiment(String experimentIdentifierString)
    {
        return transaction.getExperiment(experimentIdentifierString);
    }

    @Override
    public IExperiment getExperimentForUpdate(String experimentIdentifierString)
    {
        return transaction.getExperimentForUpdate(experimentIdentifierString);
    }

    @Override
    public IExperiment createNewExperiment(String experimentIdentifierString,
            String experimentTypeCode)
    {
        return transaction.createNewExperiment(experimentIdentifierString, experimentTypeCode);
    }

    @Override
    public IProject createNewProject(String projectIdentifier)
    {
        return transaction.createNewProject(projectIdentifier);
    }

    @Override
    public IProjectImmutable getProject(String projectIdentifier)
    {
        return transaction.getProject(projectIdentifier);
    }

    @Override
    public ISpace createNewSpace(String spaceCode, String spaceAdminUserIdOrNull)
    {
        return transaction.createNewSpace(spaceCode, spaceAdminUserIdOrNull);
    }

    @Override
    public ISpaceImmutable getSpace(String spaceCode)
    {
        return transaction.getSpace(spaceCode);
    }

    @Override
    public IMaterialImmutable getMaterial(String materialCode, String materialType)
    {
        return transaction.getMaterial(materialCode, materialType);
    }

    @Override
    public IMaterial createNewMaterial(String materialCode, String materialType)
    {
        return transaction.createNewMaterial(materialCode, materialType);
    }

    @Override
    public String moveFile(String src, IDataSet dst)
    {
        return transaction.moveFile(src, dst);
    }

    @Override
    public String moveFile(String src, IDataSet dst, String dstInDataset)
    {
        return transaction.moveFile(src, dst, dstInDataset);
    }

    @Override
    public String createNewDirectory(IDataSet dst, String dirName)
    {
        return transaction.createNewDirectory(dst, dirName);
    }

    @Override
    public String createNewFile(IDataSet dst, String fileName)
    {
        return transaction.createNewFile(dst, fileName);
    }

    @Override
    public String createNewFile(IDataSet dst, String dstInDataset, String fileName)
    {
        return transaction.createNewFile(dst, dstInDataset, fileName);
    }

    @Override
    public ISearchService getSearchService()
    {
        return transaction.getSearchService();
    }

    @Override
    public IAuthorizationService getAuthorizationService()
    {
        return transaction.getAuthorizationService();
    }

    @Override
    public DynamicTransactionQuery getDatabaseQuery(String dataSourceName)
            throws IllegalArgumentException
    {
        return transaction.getDatabaseQuery(dataSourceName);
    }

    @Override
    public DataSetRegistrationContext getRegistrationContext()
    {
        return transaction.getRegistrationContext();
    }

    @Override
    public TopLevelDataSetRegistratorGlobalState getGlobalState()
    {
        return transaction.getGlobalState();
    }

    @Override
    public File getIncoming()
    {
        return transaction.getIncoming();
    }

    @Override
    public String getUserId()
    {
        return transaction.getUserId();
    }

    @Override
    public void setUserId(String userIdOrNull)
    {
        transaction.setUserId(userIdOrNull);
    }

    @Override
    public IExternalDataManagementSystemImmutable getExternalDataManagementSystem(
            String externalDataManagementSystemCode)
    {
        return transaction.getExternalDataManagementSystem(externalDataManagementSystemCode);
    }
}
