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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.springframework.util.StopWatch;

import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.LoadImageConfiguration;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * A test class which tests the performance of loading thumbnails.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class LoadEmptyImagesTest
{
    public static void main(String[] args) throws IOException
    {
        if (args.length != 3)
        {
            System.err.println("Usage: <user> <password> <openbis-server-url>");
            System.err.println("Example parameters: test-user my-password http://localhost:8888");
            System.exit(1);
            return;
        }

        configureLogging();

        String userId = args[0];
        String userPassword = args[1];
        String serverUrl = args[2];

        print(String.format("Connecting to the server '%s' as a user '%s.", serverUrl, userId));
        IScreeningOpenbisServiceFacade facade =
                ScreeningOpenbisServiceFacadeFactory.tryCreate(userId, userPassword, serverUrl);
        if (facade == null)
        {
            System.err.println("Authentication failed: check the user name and password.");
            System.exit(1);
            return;
        }

        LoadEmptyImagesTest newMe = new LoadEmptyImagesTest(facade);

        newMe.runTest();

        newMe.logout();
    }

    private final IScreeningOpenbisServiceFacade facade;

    private LoadEmptyImagesTest(IScreeningOpenbisServiceFacade facade)
    {
        this.facade = facade;
    }

    public void runTest() throws IOException
    {

        List<PlateImageReference> imageReferences = findPlateImagesToLoad(1000);

        /*
         * try { runAndTime(imageReferences, 0); throw new RuntimeException("Expected exception not thrown"); } catch (Exception e) { }
         */
        try
        {
            runAndTime(imageReferences, 1);
            throw new RuntimeException("Expected exception not thrown");
        } catch (Exception e)
        {

        }

        try
        {
            runAndTime(imageReferences, 2);
            throw new RuntimeException("Expected exception not thrown");
        } catch (Exception e)
        {

        }
    }

    private void runAndTime(List<PlateImageReference> imageReferences, int whichMethod)
            throws IOException
    {
        print("Retrieving images...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        LoadImageConfiguration config = new LoadImageConfiguration();
        config.setDesiredImageSize(new ImageSize(256, 256));
        switch (whichMethod)
        {
            case 0:
                loadThumbnailImageWellCaching(imageReferences);
                break;
            case 1:
                config.setDesiredImageSize(null);
                loadImageWellCaching(imageReferences, config);
                //$FALL-THROUGH$
            case 2:
                config.setDesiredImageSize(null);
                loadImages(imageReferences, config);
                break;
            default:
                break;
        }
        stopWatch.stop();
        long timeInMs = stopWatch.getLastTaskTimeMillis();
        double timeInSec = (timeInMs / 1000.0);
        print("Retrieving " + imageReferences.size() + " images took " + timeInMs + " ms = "
                + timeInSec + " sec");
        print("Test done.");
    }

    private void loadImageWellCaching(List<PlateImageReference> imageReferences,
            LoadImageConfiguration config) throws IOException
    {
        for (PlateImageReference imageReference : imageReferences)
        {
            facade.loadImageWellCaching(imageReference, config.getDesiredImageSize());
        }
    }

    private void loadThumbnailImageWellCaching(List<PlateImageReference> imageReferences)
            throws IOException
    {
        for (PlateImageReference imageReference : imageReferences)
        {
            facade.loadThumbnailImageWellCaching(imageReference);
        }
    }

    private void loadImages(List<PlateImageReference> imageReferences, LoadImageConfiguration config)
            throws IOException
    {
        facade.loadImages(imageReferences, config, new IPlateImageHandler()
            {
                @Override
                public void handlePlateImage(PlateImageReference plateImageReference,
                        byte[] imageFileBytes)
                {
                    // do nothing
                    System.out.println("Image size: " + imageFileBytes.length);
                }

            });
    }

    private List<PlateImageReference> findPlateImagesToLoad(int numberOfImagesDesired)
    {
        print("Looking for images to load...");

        ArrayList<PlateImageReference> plateImages = new ArrayList<PlateImageReference>();
        // Try to get numberOfImagesDesired plate images
        List<Plate> plates = facade.listPlates();

        // Restrict to the plates we know have some wells with no images
        ArrayList<Plate> platesOfInterest = new ArrayList<Plate>();
        for (Plate plate : plates)
        {
            if (plate.getAugmentedCode().equals("/DEMO/ALL"))
            {
                platesOfInterest.add(plate);
            }
        }

        List<ImageDatasetReference> imageDatasets = facade.listRawImageDatasets(platesOfInterest);
        if (imageDatasets.size() == 0)
        {
            return plateImages;
        }

        for (ImageDatasetReference imageDataset : imageDatasets)
        {

            addAllPlateImagesWithNoImagesFromDataset(plateImages, imageDataset,
                    numberOfImagesDesired);
        }
        print("...found " + plateImages.size() + " images.");
        return plateImages;
    }

    private void addAllPlateImagesWithNoImagesFromDataset(
            ArrayList<PlateImageReference> plateImages, ImageDatasetReference imageDataset,
            int numberOfImagesDesired)
    {
        ImageDatasetMetadata metadata = facade.listImageMetadata(imageDataset);
        int numberOfRows = metadata.getTilesRows();
        int numberOfCols = metadata.getTilesCols();
        // List<String> channels = metadata.getChannelCodes();
        List<String> channels = Arrays.asList(ScreeningConstants.MERGED_CHANNELS);
        int numberOfTileRows = metadata.getTilesRows();
        int numberOfTileCols = metadata.getTilesCols();
        // Note the non-C-like numbering of the rows and columns
        for (int row = 1; row <= numberOfRows; ++row)
        {
            for (int col = 1; col <= numberOfCols; ++col)
            {
                // 1,1 is the only well with images
                // if (1 == row && 1 == col)
                // {
                // continue;
                // }
                WellPosition well = new WellPosition(row, col);
                for (String channel : channels)
                {
                    for (int tileRow = 0; tileRow < numberOfTileRows; ++tileRow)
                    {
                        for (int tileCol = 0; tileCol < numberOfTileCols; ++tileCol)
                        {
                            int tile = (tileRow * numberOfTileCols) + tileCol;
                            PlateImageReference imageRef =
                                    new PlateImageReference(tile, channel, well, imageDataset);
                            plateImages.add(imageRef);
                        }
                    }
                }
            }
        }
    }

    public void logout()
    {
        facade.logout();
    }

    private static void print(String msg)
    {
        System.out.println(new Date() + "\t" + msg);
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
