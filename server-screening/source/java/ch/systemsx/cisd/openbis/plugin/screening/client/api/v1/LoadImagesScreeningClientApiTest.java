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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade.IImageOutputStreamProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * A test class which shows how to use API to load images.
 * 
 * @author Tomasz Pylak
 */
public class LoadImagesScreeningClientApiTest
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
                ScreeningOpenbisServiceFacadeFactory.INSTANCE.tryToCreate(userId, userPassword,
                        serverUrl);
        if (facade == null)
        {
            System.err.println("Authentication failed: check the user name and password.");
            System.exit(1);
            return;
        }

        // Another way: PlateIdentifier.createFromAugmentedCode("/SPACE_CODE/MY_PLATE_CODE")
        // PlateIdentifier plate = new PlateIdentifier("MY_PLATE_CODE", "SPACE_CODE", null);
        String plateCode = "PLATE-2-A";
        PlateIdentifier plate = new PlateIdentifier(plateCode, "TEST", null);
        List<ImageDatasetReference> imageDatasets =
                facade.listRawImageDatasets(Arrays.asList(plate));
        if (imageDatasets.size() == 0)
        {
            System.err.println("No image datasets connected to plate " + plate);
            System.exit(1);
        }

        printImagesMetadata(facade, imageDatasets);
        loadOneImage(facade, imageDatasets.get(0));

        facade.logout();
    }

    private static void printImagesMetadata(IScreeningOpenbisServiceFacade facade,
            List<ImageDatasetReference> imageDatasets)
    {
        List<ImageDatasetMetadata> imageMetadatas = facade.listImageMetadata(imageDatasets);
        for (ImageDatasetMetadata imageMetadata : imageMetadatas)
        {
            System.out.println(imageMetadata);
        }
    }

    private static void loadOneImage(IScreeningOpenbisServiceFacade facade,
            ImageDatasetReference imageDataset) throws IOException
    {
        // You could get to know more about the image dataset metadata with:
        // facade.listImageMetadata(imageDataset);
        // Here we will make some assumptions.

        // Note: first tile has number 0
        int tile = 0;
        String channel = "DAPI";
        // Note: first well 1 has coordinates (1,1)
        int row = 1;
        int column = 3;
        WellPosition well = new WellPosition(row, column);
        PlateImageReference imageRef = new PlateImageReference(tile, channel, well, imageDataset);
        loadImage(facade, imageRef);
    }

    private static File createImageFile(PlateImageReference imageRef)
    {
        File dir = new File(imageRef.getDatasetCode());
        dir.mkdir();
        return new File(dir, createImageFileName(imageRef));
    }

    /**
     * @throws IOException when reading images from the server or writing them to the files fails
     */
    private static void loadImage(IScreeningOpenbisServiceFacade facade,
            PlateImageReference imageReference) throws IOException
    {
        File imageOutputFile = createImageFile(imageReference);
        final OutputStream out = new BufferedOutputStream(new FileOutputStream(imageOutputFile));
        try
        {
            facade.loadImages(Arrays.asList(imageReference), new IImageOutputStreamProvider()
                {
                    @Override
                    public OutputStream getOutputStream(PlateImageReference imageRef)
                            throws IOException
                    {
                        return out;
                    }
                }, false);
        } finally
        {
            out.close();
        }
    }

    private static void print(String msg)
    {
        System.out.println(new Date() + "\t" + msg);
    }

    private static String createImageFileName(PlateImageReference image)
    {
        WellPosition well = image.getWellPosition();
        return "img_row" + well.getWellRow() + "_col" + well.getWellColumn() + "_"
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
