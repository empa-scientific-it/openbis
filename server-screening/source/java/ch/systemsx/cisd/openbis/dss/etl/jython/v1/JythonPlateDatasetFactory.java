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

package ch.systemsx.cisd.openbis.dss.etl.jython.v1;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.etlserver.registrator.v1.AbstractOmniscientTopLevelDataSetRegistrator.OmniscientTopLevelDataSetRegistratorState;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.v1.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.v1.JythonTopLevelDataSetHandler.ProgrammableDropboxObjectFactory;
import ch.systemsx.cisd.openbis.dss.etl.PlateGeometryOracle;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IFeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureDefinition;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureVectorDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImagingDatasetFactory;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CsvFeatureVectorParser;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * @author jakubs
 */

public class JythonPlateDatasetFactory extends ProgrammableDropboxObjectFactory<DataSetInformation>
        implements IImagingDatasetFactory
{
    final IDataSetRegistrationDetailsFactory<ImageDataSetInformation> imageDatasetFactory;

    final IDataSetRegistrationDetailsFactory<DataSetInformation> imageContainerDatasetFactory;

    final IDataSetRegistrationDetailsFactory<DataSetInformation> featureVectorContainerDatasetFactory;

    final IDataSetRegistrationDetailsFactory<FeatureVectorDataSetInformation> featureVectorDatasetFactory;

    public JythonPlateDatasetFactory(OmniscientTopLevelDataSetRegistratorState registratorState,
            DataSetInformation userProvidedDataSetInformationOrNull)
    {
        super(registratorState, userProvidedDataSetInformationOrNull);
        this.imageContainerDatasetFactory =
                new JythonImageContainerDataSetRegistrationFactory(this.registratorState,
                        this.userProvidedDataSetInformationOrNull);
        this.imageDatasetFactory =
                new JythonImageDataSetRegistrationFactory(this.registratorState,
                        this.userProvidedDataSetInformationOrNull);
        this.featureVectorDatasetFactory =
                new ProgrammableDropboxObjectFactory<FeatureVectorDataSetInformation>(
                        this.registratorState, this.userProvidedDataSetInformationOrNull)
                    {
                        @Override
                        protected FeatureVectorDataSetInformation createDataSetInformation()
                        {
                            return new FeatureVectorDataSetInformation();
                        }
                    };

        this.featureVectorContainerDatasetFactory =
                new FeatureVectorContainerDataSetRegistrationFactory(this.registratorState,
                        this.userProvidedDataSetInformationOrNull);
    }

    /** By default a standard dataset is created. */
    @Override
    protected DataSetInformation createDataSetInformation()
    {
        return new DataSetInformation();
    }

    @Override
    public DataSetRegistrationDetails<ImageDataSetInformation> createImageRegistrationDetails(
            SimpleImageDataConfig imageDataSet, File incomingDatasetFolder)
    {
        return SimpleImageDataSetRegistrator.createImageDatasetDetails(imageDataSet,
                incomingDatasetFolder, imageDatasetFactory);
    }

    /** a simple method to register the described image dataset in a separate transaction */
    @Override
    public boolean registerImageDataset(SimpleImageDataConfig imageDataSet,
            File incomingDatasetFolder, DataSetRegistrationService<ImageDataSetInformation> service)
    {
        DataSetRegistrationDetails<ImageDataSetInformation> imageDatasetDetails =
                createImageRegistrationDetails(imageDataSet, incomingDatasetFolder);
        return registerImageDataset(imageDatasetDetails, incomingDatasetFolder, service);
    }

    @Override
    public boolean registerImageDataset(
            DataSetRegistrationDetails<ImageDataSetInformation> imageDatasetDetails,
            File incomingDatasetFolder, DataSetRegistrationService<ImageDataSetInformation> service)
    {
        DataSetRegistrationTransaction<ImageDataSetInformation> transaction =
                service.transaction(incomingDatasetFolder,
                        service.getDataSetRegistrationDetailsFactory());
        IDataSet newDataset = transaction.createNewDataSet(imageDatasetDetails);
        transaction.moveFile(incomingDatasetFolder.getPath(), newDataset);
        return transaction.commit();
    }

    /**
     * @return a constant which can be used as a vocabulary term value for $PLATE_GEOMETRY property of a plate/
     * @throws UserFailureException if all available geometries in openBIS are too small (there is a well outside).
     */
    @Override
    public String figureGeometry(
            DataSetRegistrationDetails<ImageDataSetInformation> registrationDetails)
    {
        IEncapsulatedOpenBISService openBisService =
                registratorState.getGlobalState().getOpenBisService();
        return PlateGeometryOracle.figureGeometry(registrationDetails, openBisService);
    }

    // ----

    @Override
    public IFeaturesBuilder createFeaturesBuilder()
    {
        return new FeaturesBuilder();
    }

    @Override
    public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorDatasetDetails(
            IFeaturesBuilder featureBuilder)
    {
        FeaturesBuilder myFeatureBuilder = (FeaturesBuilder) featureBuilder;
        List<FeatureDefinition> featureDefinitions =
                myFeatureBuilder.getFeatureDefinitionValuesList();
        return createFeatureVectorRegistrationDetails(featureDefinitions);
    }

    /**
     * Parses the feature vectors from the specified CSV file. CSV format can be configured with following properties:
     * 
     * <pre>
     *   # Separator character between headers and row cells.
     *   separator = ,
     *   ignore-comments = true
     *   # Header of the column denoting the row of a well.
     *   well-name-row = row
     *   # Header of the column denoting the column of a well.
     *   well-name-col = col
     *   well-name-col-is-alphanum = true
     * </pre>
     * 
     * @throws IOException if file cannot be parsed
     */
    @Override
    public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorDatasetDetails(
            String csvFilePath, Properties properties) throws IOException
    {
        List<FeatureDefinition> featureDefinitions =
                CsvFeatureVectorParser.parse(new File(csvFilePath), properties);
        return createFeatureVectorRegistrationDetails(featureDefinitions);
    }

    public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorRegistrationDetails(
            List<FeatureDefinition> featureDefinitions)
    {
        DataSetRegistrationDetails<FeatureVectorDataSetInformation> registrationDetails =
                featureVectorDatasetFactory.createDataSetRegistrationDetails();
        FeatureVectorDataSetInformation featureVectorDataSet =
                registrationDetails.getDataSetInformation();
        featureVectorDataSet.setFeatures(featureDefinitions);
        registrationDetails.setDataSetType(ScreeningConstants.DEFAULT_ANALYSIS_WELL_DATASET_TYPE);
        registrationDetails.setDataSetKind(DataSetKind.PHYSICAL);
        registrationDetails.setMeasuredData(false);
        if (false == featureVectorDataSet.isNotEmpty())
        {
            throw new UserFailureException(
                    "The feature vector dataset does not contain any features.");
        }
        return registrationDetails;
    }

    // -------- backward compatibility methods

    /**
     * This method exists just for backward compatibility. It used to have the second parameter, which is now ignored.
     * 
     * @deprecated use {@link #createFeatureVectorDatasetDetails(IFeaturesBuilder)} instead.
     */
    @Deprecated
    public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorRegistrationDetails(
            IFeaturesBuilder featureBuilder, Object incomingDatasetFolder)
    {
        return createFeatureVectorDatasetDetails(featureBuilder);
    }

    /**
     * @deprecated Changed to {@link #createFeatureVectorDatasetDetails(String, Properties)} due to naming convention change.
     */
    @Deprecated
    public DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorRegistrationDetails(
            String csvFilePath, Properties properties) throws IOException
    {
        return createFeatureVectorDatasetDetails(csvFilePath, properties);
    }

}
