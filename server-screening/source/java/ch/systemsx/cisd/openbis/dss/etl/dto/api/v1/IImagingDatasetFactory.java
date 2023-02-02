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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.v1.DataSetRegistrationService;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.IFeaturesBuilder;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureVectorDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;

/**
 * Factory for defining image and feature vector datasets.
 * 
 * @author Tomasz Pylak
 */
public interface IImagingDatasetFactory
{
    /**
     * Creates image dataset details. Used if <br>
     * 1. more then one dataset is created in one transaction and using {@link #registerImageDataset} is not sufficient or <br>
     * 2. {@link SimpleImageDataConfig} is not sufficient to describe the dataset and further modifications on the dataset detail object are required.
     */
    @Deprecated
    DataSetRegistrationDetails<ImageDataSetInformation> createImageRegistrationDetails(
            SimpleImageDataConfig imageDataSet, File incomingDatasetFolder);

    /**
     * A convenience method to register the specified image dataset in a separate transaction.
     * 
     * @param imageDataSet simple dataset specification
     */
    boolean registerImageDataset(SimpleImageDataConfig imageDataSet, File incomingDatasetFolder,
            DataSetRegistrationService<ImageDataSetInformation> service);

    /**
     * A convenience method to register the specified image dataset in a separate transaction.
     * 
     * @param imageDatasetDetails advanced dataset specification
     */
    @Deprecated
    boolean registerImageDataset(
            DataSetRegistrationDetails<ImageDataSetInformation> imageDatasetDetails,
            File incomingDatasetFolder, DataSetRegistrationService<ImageDataSetInformation> service);

    /**
     * Allows to define feature vectors of one image analysis dataset. Used to define a dataset details with
     * {@link #createFeatureVectorDatasetDetails(IFeaturesBuilder)}.
     */
    IFeaturesBuilder createFeaturesBuilder();

    /**
     * Creates feature vector dataset details by using the specified builder.
     */
    DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorDatasetDetails(
            IFeaturesBuilder featureBuilder);

    /**
     * Creates feature vector dataset details by parsing the specified CSV file.
     * <p>
     * CSV format can be configured with following properties:
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
    DataSetRegistrationDetails<FeatureVectorDataSetInformation> createFeatureVectorDatasetDetails(
            String csvFilePath, Properties properties) throws IOException;

    /**
     * Utility method to find out the plate geometry by looking for which wells images are available.
     * 
     * @return a constant which can be used as a vocabulary term value for $PLATE_GEOMETRY property of a plate/
     * @throws UserFailureException if all available geometries in openBIS are too small (there is a well outside).
     */
    String figureGeometry(DataSetRegistrationDetails<ImageDataSetInformation> registrationDetails);

}