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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.PropertyConfigurator;

import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.LoadImageConfiguration;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * A test class which downloads image for one well in a data set with and without the transformation. The images are placed on the desktop.
 * <p>
 * The constructor takes two arguments, one being the permId of the plate we want to download images for.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class LoadImagesWithTransformationTest
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

        // Specify the permId of the plate we want to download images for.
        LoadImagesWithTransformationTest newMe =
                new LoadImagesWithTransformationTest(facade, "20110203103943504-79775");

        newMe.runTest();

        newMe.logout();
    }

    private final IScreeningOpenbisServiceFacade facade;

    private final String permIdOfPlateInterest;

    private LoadImagesWithTransformationTest(IScreeningOpenbisServiceFacade facade,
            String permIdOfInterest)
    {
        this.facade = facade;
        this.permIdOfPlateInterest = permIdOfInterest;
    }

    public void runTest() throws IOException
    {

        List<PlateImageReference> imageReferences = findPlateImagesToLoad();

        // Load the images without the transform
        LoadImageConfiguration config = new LoadImageConfiguration();
        config.setDesiredImageFormatPng(true);
        config.setOpenBisImageTransformationApplied(false);
        loadImages(imageReferences, config);

        // Load the images with the transform
        config.setOpenBisImageTransformationApplied(true);
        loadImages(imageReferences, config);
        print("Finished.");

    }

    private void loadImages(List<PlateImageReference> imageReferences,
            final LoadImageConfiguration config) throws IOException
    {
        facade.loadImages(imageReferences, config, new IPlateImageHandler()
            {
                @Override
                public void handlePlateImage(PlateImageReference plateImageReference,
                        byte[] imageFileBytes)
                {
                    String userHome = System.getProperty("user.home");
                    StringBuffer sb = new StringBuffer();
                    sb.append(userHome);
                    sb.append("/Desktop/");
                    sb.append("Well-");
                    sb.append(plateImageReference.getWellPosition().getWellRow());
                    sb.append("-");
                    sb.append(plateImageReference.getWellPosition().getWellColumn());
                    sb.append("-");
                    sb.append(plateImageReference.getTile());
                    sb.append("-");
                    sb.append(plateImageReference.getChannel());
                    if (config.isOpenBisImageTransformationApplied())
                    {
                        sb.append("-transformed");
                    }
                    sb.append(".png");

                    print("Writing " + sb.toString() + "...");
                    FileOutputStream fos = null;
                    try
                    {
                        fos = new FileOutputStream(sb.toString());
                        fos.write(imageFileBytes);
                        fos.close();
                    } catch (FileNotFoundException ex)
                    {
                        ex.printStackTrace();
                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                    finally
                    {
                        IOUtils.closeQuietly(fos);
                    }

                }

            });
    }

    private List<PlateImageReference> findPlateImagesToLoad()
    {
        print("Looking for images to load...");

        ArrayList<PlateImageReference> plateImages = new ArrayList<PlateImageReference>();
        // Try to get numberOfImagesDesired plate images
        List<Plate> plates = facade.listPlates();

        // We are interested in just one plate
        Plate plateOfInterest = null;
        for (Plate plate : plates)
        {
            if (plate.getPermId().equals(permIdOfPlateInterest))
            {
                plateOfInterest = plate;
                break;
            }
        }
        if (null == plateOfInterest)
        {
            return plateImages;
        }

        List<ImageDatasetReference> imageDatasets =
                facade.listRawImageDatasets(Arrays.asList(plateOfInterest));
        if (imageDatasets.size() == 0)
        {
            return plateImages;
        }

        for (ImageDatasetReference imageDataset : imageDatasets)
        {

            addAllPlateImagesFromDataset(plateImages, imageDataset, 4);

            // Stop when we have enough
            if (isNumberOfImagesSufficient(plateImages, 4))
            {
                break;
            }
        }
        print("...found " + plateImages.size() + " images.");
        return plateImages;
    }

    private void addAllPlateImagesFromDataset(ArrayList<PlateImageReference> plateImages,
            ImageDatasetReference imageDataset, int numberOfImagesDesired)
    {
        ImageDatasetMetadata metadata = facade.listImageMetadata(imageDataset);
        int numberOfRows = metadata.getTilesRows();
        int numberOfCols = metadata.getTilesCols();
        List<String> channels = metadata.getChannelCodes();
        int numberOfTileRows = metadata.getTilesRows();
        int numberOfTileCols = metadata.getTilesCols();
        // Note the non-C-like numbering of the rows and columns
        for (int row = 1; row <= numberOfRows; ++row)
        {
            for (int col = 1; col <= numberOfCols; ++col)
            {
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

                            // Stop when we have enough
                            if (isNumberOfImagesSufficient(plateImages, numberOfImagesDesired))
                            {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isNumberOfImagesSufficient(ArrayList<PlateImageReference> plateImages,
            int numberOfImagesDesired)
    {
        return plateImages.size() > numberOfImagesDesired - 1;
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
