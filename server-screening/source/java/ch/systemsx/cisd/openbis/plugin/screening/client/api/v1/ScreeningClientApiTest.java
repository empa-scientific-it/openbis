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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;

import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade.IImageOutputStreamProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellMetadata;
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
        List<ExperimentIdentifier> experiments = facade.listExperiments();
        print("Experiments: " + experiments);

        MaterialIdentifier gene = new MaterialIdentifier(MaterialTypeIdentifier.GENE, "1111");
        ExperimentIdentifier experimentIdentifer = experiments.get(0);
        List<PlateWellReferenceWithDatasets> plateWells = null;
        List<FeatureVectorWithDescription> featuresForPlateWells = null;
        List<FeatureVectorWithDescription> featuresForPlateWellsCheck = null;
        try
        {
            plateWells = facade.listPlateWells(experimentIdentifer, gene, true);
            print(String.format("Wells with gene '%s' in experiment '%s': %s", gene,
                    experimentIdentifer, plateWells));
            featuresForPlateWells =
                    facade.loadFeaturesForPlateWells(experimentIdentifer, gene, null, null);
            print("Features for wells: " + featuresForPlateWells);
            featuresForPlateWellsCheck =
                    facade.loadFeaturesForDatasetWellReferences(
                            facade.convertToFeatureVectorDatasetWellIdentifier(plateWells), null);
        } catch (Exception e)
        {
            print(e.toString());
        }
        if (featuresForPlateWells != null
                && featuresForPlateWells.equals(featuresForPlateWellsCheck) == false)
        {
            throw new IllegalStateException(String.format(
                    "Inconsistent results to fetch feature vectors, expected:\n%s\nbut got:\n%s",
                    featuresForPlateWells, featuresForPlateWellsCheck));
        }

        List<Plate> plates = facade.listPlates();
        print("Plates: " + plates);

        @SuppressWarnings("deprecation")
        // TODO 2011-02-16, CR, There is no non-deprecated method to get all data sets.
        // In the Test, Plate1 has a raw image data set, plates 2 and 3 have overlay image data
        // sets. There is no other way to get the overlay image data sets.
        List<ImageDatasetReference> imageDatasets = facade.listImageDatasets(plates);
        Collections.sort(imageDatasets, new Comparator<ImageDatasetReference>()
            {
                @Override
                public int compare(ImageDatasetReference r1, ImageDatasetReference r2)
                {
                    return r2.getPlate().getPlateCode().compareTo(r1.getPlate().getPlateCode());
                }
            });
        print("Image datasets: " + imageDatasets.subList(0, Math.min(5, imageDatasets.size())));

        List<FeatureVectorDatasetReference> featureVectorDatasets =
                facade.listFeatureVectorDatasets(plates, null);
        Collections.sort(featureVectorDatasets, new Comparator<FeatureVectorDatasetReference>()
            {
                @Override
                public int compare(FeatureVectorDatasetReference r1,
                        FeatureVectorDatasetReference r2)
                {
                    return r2.getPlate().getPlateCode().compareTo(r1.getPlate().getPlateCode());
                }
            });
        print("Feature vector datasets: "
                + featureVectorDatasets.subList(0, Math.min(5, featureVectorDatasets.size())));

        List<String> featureCodes = facade.listAvailableFeatureCodes(featureVectorDatasets);
        Collections.sort(featureCodes);
        print("Feature codes: " + featureCodes);
        List<FeatureVectorDataset> features =
                facade.loadFeatures(featureVectorDatasets, featureCodes);
        Collections.sort(features, new Comparator<FeatureVectorDataset>()
            {
                @Override
                public int compare(FeatureVectorDataset f1, FeatureVectorDataset f2)
                {
                    return f2.getDataset().getPlate().getPlateCode()
                            .compareTo(f1.getDataset().getPlate().getPlateCode());
                }
            });
        print("Loaded feature datasets: " + features.size());
        if (features.size() > 0)
        {
            print("Features of the first dataset: " + features.get(0));
        }
        Map<String, List<ImageDatasetReference>> imageDataSetReferencesPerDss =
                new HashMap<String, List<ImageDatasetReference>>();
        for (ImageDatasetReference imageDataset : imageDatasets)
        {
            String url = imageDataset.getDatastoreServerUrl();
            List<ImageDatasetReference> list = imageDataSetReferencesPerDss.get(url);
            if (list == null)
            {
                list = new ArrayList<ImageDatasetReference>();
                imageDataSetReferencesPerDss.put(url, list);
            }
            list.add(imageDataset);
        }
        Collection<List<ImageDatasetReference>> bundle = imageDataSetReferencesPerDss.values();
        for (List<ImageDatasetReference> imageDataSets : bundle)
        {
            List<ImageDatasetMetadata> imageMetadata = facade.listImageMetadata(imageDataSets);
            print("Image metadata: " + imageMetadata);
        }

        loadImages(facade, getFirstTwo(facade, imageDatasets));
        // loadImagesFromFeatureVectors(facade, getFirstTwo(facade, featureVectorDatasets));

        List<PlateMetadata> plateMetadata =
                facade.getPlateMetadataList(Arrays.asList(plates.get(0), plates.get(1), plates.get(2)));
        for (PlateMetadata metadata : plateMetadata)
        {
            WellMetadata well = metadata.getWell(1, 1);
            if (well != null)
            {
                System.out.println(metadata.getAugmentedCode() + ": well:" + well.getCode()
                        + " well properties:" + well.getProperties());
                Map<String, Material> materialProperties = well.getMaterialProperties();
                System.out.println(renderMaterialProperties(materialProperties));
            }
        }
        facade.logout();
        System.out.println("Test finished");
    }

    private static String renderMaterialProperties(Map<String, Material> materialProperties)
    {
        StringBuilder builder = new StringBuilder();
        renderMaterials(materialProperties, builder, "  ");
        return builder.toString();
    }

    private static void renderMaterials(Map<String, Material> materialProperties, StringBuilder builder,
            String indent)
    {
        Set<Entry<String, Material>> entrySet = materialProperties.entrySet();
        for (Entry<String, Material> entry : entrySet)
        {
            builder.append(indent).append("material property of type ").append(entry.getKey()).append(":\n");
            renderMaterial(entry.getValue(), builder, indent + "  ");
        }
    }

    private static void renderMaterial(Material material, StringBuilder builder, String indent)
    {
        builder.append(indent).append(material.getAugmentedCode()).append(" properties: ");
        builder.append(material.getProperties()).append("\n");
        Map<String, Material> materialProperties = material.getMaterialProperties();
        renderMaterials(materialProperties, builder, indent);
    }

    private static <T extends DatasetIdentifier> List<T> getFirstTwo(
            IScreeningOpenbisServiceFacade facade, List<T> identfiers)
    {
        List<T> result = new ArrayList<T>();
        for (int i = 0; i < Math.min(2, identfiers.size()); i++)
        {
            T ident = identfiers.get(i);
            result.add(ident);
            IDatasetIdentifier fetchedIdent = getDatasetIdentifier(facade, ident.getDatasetCode());
            if (fetchedIdent.getPermId().equals(ident.getPermId()) == false)
            {
                throw new IllegalStateException(
                        "Fetched dataset identifier is not the same as the expected one. It is "
                                + fetchedIdent + " instead of " + ident);
            }
        }
        return result;
    }

    private static IDatasetIdentifier getDatasetIdentifier(IScreeningOpenbisServiceFacade facade,
            String datasetCode)
    {
        IDatasetIdentifier datasetIdentifier =
                facade.getDatasetIdentifiers(Arrays.asList(datasetCode)).get(0);
        return datasetIdentifier;
    }

    private static void loadImages(IScreeningOpenbisServiceFacade facade,
            List<ImageDatasetReference> datasetIdentifiers) throws FileNotFoundException,
            IOException
    {
        List<PlateImageReference> imageRefs = createAllImagesReferences(facade, datasetIdentifiers);
        List<File> imageFiles = createImageFiles(imageRefs);

        loadImages(facade, imageRefs, imageFiles);
    }

    private static List<PlateImageReference> createAllImagesReferences(
            IScreeningOpenbisServiceFacade facade, List<ImageDatasetReference> datasetIdentifiers)
    {
        Map<IImageDatasetIdentifier, ImageDatasetMetadata> metadataMap =
                fetchMetadataMap(facade, datasetIdentifiers);
        List<PlateImageReference> imageRefs = new ArrayList<PlateImageReference>();
        for (ImageDatasetReference datasetIdentifier : datasetIdentifiers)
        {
            ImageDatasetMetadata metadata = metadataMap.get(datasetIdentifier);
            List<PlateImageReference> datasetImageRefs =
                    createOneWellImageReferences(metadata, datasetIdentifier);
            imageRefs.addAll(datasetImageRefs);
        }
        return imageRefs;
    }

    private static List<File> createImageFiles(List<PlateImageReference> imageRefs)
    {
        List<File> imageFiles = new ArrayList<File>();
        for (PlateImageReference imageRef : imageRefs)
        {
            File dir = new File(imageRef.getDatasetCode());
            dir.mkdir();
            imageFiles.add(new File(dir, createImageFileName(imageRef)));
        }
        return imageFiles;
    }

    private static List<PlateImageReference> createOneWellImageReferences(
            ImageDatasetMetadata metadata, ImageDatasetReference datasetIdentifier)
    {
        List<PlateImageReference> imageRefs = new ArrayList<PlateImageReference>();
        int wellRow = 1;
        int wellCol = 3;
        for (String channel : metadata.getChannelCodes())
        {
            for (int tile = 0; tile < metadata.getNumberOfTiles(); tile++)
            {
                PlateImageReference imageRef =
                        new PlateImageReference(wellRow, wellCol, tile, channel, datasetIdentifier);
                imageRefs.add(imageRef);
            }
        }
        return imageRefs;
    }

    private static Map<IImageDatasetIdentifier, ImageDatasetMetadata> fetchMetadataMap(
            IScreeningOpenbisServiceFacade facade, List<ImageDatasetReference> datasetIdentifiers)
    {
        Map<IImageDatasetIdentifier, ImageDatasetMetadata> map =
                new HashMap<IImageDatasetIdentifier, ImageDatasetMetadata>();
        List<ImageDatasetMetadata> metadatum = facade.listImageMetadata(datasetIdentifiers);
        for (ImageDatasetMetadata metadata : metadatum)
        {
            map.put(metadata.getImageDataset(), metadata);
        }
        return map;
    }

    @SuppressWarnings("unused")
    private static void loadImagesFromFeatureVectors(IScreeningOpenbisServiceFacade facade,
            List<FeatureVectorDatasetReference> datasetIdentifiers) throws FileNotFoundException,
            IOException
    {
        List<PlateImageReference> imageRefs = new ArrayList<PlateImageReference>();
        List<File> imageFiles = new ArrayList<File>();
        for (IDatasetIdentifier datasetIdentifier : datasetIdentifiers)
        {
            File dir = new File(datasetIdentifier.getDatasetCode());
            dir.mkdir();

            PlateImageReference imageRef =
                    new PlateImageReference(1, 1, 0, "DAPI", datasetIdentifier);
            imageRefs.add(imageRef);
            imageFiles.add(new File(dir, createImageFileName(imageRef)));
        }
        loadImages(facade, imageRefs, imageFiles);
    }

    /**
     * Saves images for a given list of image references (given by data set code, well position, channel and tile) in the specified files.<br>
     * The number of image references has to be the same as the number of files.
     * 
     * @throws IOException when reading images from the server or writing them to the files fails
     */
    private static void loadImages(IScreeningOpenbisServiceFacade facade,
            List<PlateImageReference> imageReferences, List<File> imageOutputFiles)
            throws IOException
    {
        print("Load " + imageReferences.size() + " images");
        final Map<PlateImageReference, OutputStream> imageRefToFileMap =
                createImageToFileMap(imageReferences, imageOutputFiles);
        try
        {
            facade.loadImages(imageReferences, new IImageOutputStreamProvider()
                {
                    @Override
                    public OutputStream getOutputStream(PlateImageReference imageReference)
                            throws IOException
                    {
                        return imageRefToFileMap.get(imageReference);
                    }
                });
        } finally
        {
            closeOutputStreams(imageRefToFileMap.values());
        }
    }

    private static void print(String msg)
    {
        System.out.println(new Date() + "\t" + msg);
    }

    private static void closeOutputStreams(Collection<OutputStream> streams) throws IOException
    {
        for (OutputStream stream : streams)
        {
            stream.close();
        }
    }

    private static Map<PlateImageReference, OutputStream> createImageToFileMap(
            List<PlateImageReference> imageReferences, List<File> imageOutputFiles)
            throws FileNotFoundException
    {
        assert imageReferences.size() == imageOutputFiles.size() : "there should be one file specified for each image reference";
        Map<PlateImageReference, OutputStream> map =
                new HashMap<PlateImageReference, OutputStream>();
        for (int i = 0; i < imageReferences.size(); i++)
        {
            OutputStream out =
                    new BufferedOutputStream(new FileOutputStream(imageOutputFiles.get(i)));
            map.put(imageReferences.get(i), out);
        }
        return map;
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
