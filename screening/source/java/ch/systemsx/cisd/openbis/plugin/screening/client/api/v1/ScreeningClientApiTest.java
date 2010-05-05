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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * A test class which shows how to use API.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningClientApiTest
{
    public static void main(String[] args) throws IOException
    {
        if (args.length != 3)
        {
            System.err.println("Usage: <user> <password> <openbis-server-url>");
            System.err
                    .println("Example parameters: test-user my-password http://localhost:8888/openbis/openbis");
            return;
        }
        configureLogging();

        String userId = args[0];
        String userPassword = args[1];
        String serverUrl = args[2];

        System.out.println(String.format("Connecting to the server '%s' as a user '%s.", serverUrl,
                userId));
        ScreeningOpenbisServiceFacade facade =
                ScreeningOpenbisServiceFacade.tryCreate(userId, userPassword, serverUrl);
        if (facade == null)
        {
            System.err.println("Authentication failed: check the user name and password.");
            return;
        }
        List<Plate> plates = facade.listPlates();
        System.out.println("Plates: " + plates);
        List<ImageDatasetReference> imageDatasets = facade.listImageDatasets(plates);
        System.out.println("Image datasets: " + imageDatasets);
        List<FeatureVectorDatasetReference> featureVectorDatasets =
                facade.listFeatureVectorDatasets(plates);
        System.out.println("Feature vector datasets: " + featureVectorDatasets);

        // test for feature vector dataset
        String featureVectorDatasetCode = featureVectorDatasets.get(0).getDatasetCode(); // feature
        // vector
        IDatasetIdentifier datasetIdentifier =
                getDatasetIdentifier(facade, featureVectorDatasetCode);
        loadImages(facade, datasetIdentifier);

        String imageDatasetCode = imageDatasets.get(0).getDatasetCode(); // image
        datasetIdentifier = getDatasetIdentifier(facade, imageDatasetCode);
        loadImages(facade, datasetIdentifier);

        List<String> featureNames = facade.listAvailableFeatureNames(featureVectorDatasets);
        System.out.println("Feature names: " + featureNames);
        List<FeatureVectorDataset> features =
                facade.loadFeatures(featureVectorDatasets, featureNames);
        System.out.println("Features: " + features);

        List<ImageDatasetMetadata> imageMetadata = facade.listImageMetadata(imageDatasets);
        System.out.println("Image metadata: " + imageMetadata);

        facade.logout();
    }

    private static IDatasetIdentifier getDatasetIdentifier(ScreeningOpenbisServiceFacade facade,
            String datasetCode)
    {
        IDatasetIdentifier datasetIdentifier =
                facade.getDatasetIdentifiers(Arrays.asList(datasetCode)).get(0);
        return datasetIdentifier;
    }

    private static void loadImages(ScreeningOpenbisServiceFacade facade,
            IDatasetIdentifier datasetIdentifier) throws FileNotFoundException, IOException
    {
        File dir = new File(datasetIdentifier.getDatasetCode());
        dir.mkdir();

        List<PlateImageReference> imageRefs = new ArrayList<PlateImageReference>();
        List<File> imageFiles = new ArrayList<File>();
        for (int wellRow = 1; wellRow <= 5; wellRow++)
        {
            for (int wellCol = 1; wellCol <= 5; wellCol++)
            {
                for (int channel = 1; channel <= 2; channel++)
                {
                    for (int tile = 1; tile <= 1; tile++)
                    {

                        PlateImageReference imageRef =
                                new PlateImageReference(wellRow, wellCol, tile, channel,
                                        datasetIdentifier);
                        imageRefs.add(imageRef);
                        imageFiles.add(new File(dir, createImageFileName(imageRef)));
                    }
                }
            }
        }
        facade.loadImages(imageRefs, imageFiles);
    }

    private static String createImageFileName(PlateImageReference image)
    {
        WellPosition well = image.getWellPosition();
        return "img_row" + well.getWellRow() + "_col" + well.getWellColumn() + "_channel"
                + image.getChannel() + "_tile" + image.getTile() + ".png";
    }

    private static void configureLogging()
    {
        Properties props = new Properties();
        props.put("log4j.appender.STDOUT", "org.apache.log4j.ConsoleAppender");
        props.put("log4j.appender.STDOUT.layout", "org.apache.log4j.PatternLayout");
        props.put("log4j.appender.STDOUT.layout.ConversionPattern", "%d %-5p [%t] %c - %m%n");
        props.put("log4j.rootLogger", "INFO, STDOUT");
        PropertyConfigurator.configure(props);
    }
}
