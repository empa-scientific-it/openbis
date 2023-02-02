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

/**
 * Integration tests of MatLab API on Biozentrum server.
 * 
 * @author Tomasz Pylak
 */
@SuppressWarnings("unused")
public class BiozentrumMatLabApiTest
{
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.err
                    .println("Specify the password and optionally the plate identifier, mount point and dataset type pattern!");
        }
        String passwd = "xxx";
        String chosenPlate = "/GROUP_DEHIO/KB03-2O-130";
        String mountPoint = null;
        String datasetTypePattern = "HCS_ANALYSIS_CELL_CLASSIFICATIONS_MAT";
        if (args.length > 1)
        {
            chosenPlate = args[1];
        }
        if (args.length > 2)
        {
            mountPoint = args[2];
        }

        // OpenBISScreeningML.login("tpylak", passwd, "https://infectx.biozentrum.unibas.ch");
        // Object[][] metadata = OpenBISScreeningML.getImagesMetadata("/RESEARCH_IT/TEST2");
        // System.out.println(Arrays.toString(metadata[0]));
        // Object[][][] loadImages =
        // OpenBISScreeningML.loadImages("/INFECTX/DZ56-1K", 1, 1, 1, new String[]
        // { "CY5", "DAPI" });
        // System.out.println(loadImages[0][0][0]);

        // OpenBISScreeningML.login("cisd", passwd, "http://bc2-openbis01.bc2.unibas.ch:8443");
        OpenBISScreeningML.login("admin", passwd, "https://sprint-openbis.ethz.ch:8446");

        // OpenBISScreeningML.login("admin", passwd, "http://127.0.0.1:8888");
        // Object[][] metadata = OpenBISScreeningML.getImagesMetadata("/TEST/PLATE1");
        // System.out.println(Arrays.toString(metadata[0]));
        // Object[][][] loadImages =
        Object[][][] images = OpenBISScreeningML.loadImages("/TEST/PLATE1", 1, 1, 1, new String[]
        { "DAPI" });

        // String experiment = "/TEST/TEST-USER/MY-ASSAY";

        // testListChannels(experiment);
        // testLoadFeatures(chosenPlate, experiment);
        // testImagesMetadata(chosenPlate);
        // testLoadImage(chosenPlate);
        // testWellProperties(chosenPlate);
        // testLoadDataset(chosenPlate, mountPoint, datasetTypePattern);

        // testUploadDataset(chosenPlate);
        // testUploadDataset("/RESEARCH_IT/TEST2");
        // testUploadDataset("/DEMO/VL0206A-FV1801");
        // testUploadDataset("/TEST/PLATE1");

        // integration server test
        // OpenBISScreeningML.login("admin", passwd, "https://127.0.0.1:8443");
        // String datasetType = "HCS_MODULESETTINGS_OBJECTCLASSIFICATION_MAT";
        // String datasetType = "HCS_ANALYSIS_WELL_CLASSIFICATION_SUMMARIES";
        // String datasetType = "HCS_ANALYSIS_CELL_CLASSIFICATIONS_MAT";
        // String datasetType = "UNKNOWN";
        // testUploadDataset("/GROUP_RESEARCHIT/DZ01-1A", datasetType);

        // PlateMetadata[] plateMetadataList = OpenBISScreeningML.getPlateMetadataList(new String[]
        // { "/TEST/PLATE1", "/TEST/PLATE2", "/TEST/PLATE1.96WELLS", "/TEST/PLATE1.SUFFIX", });
        sleepLong();
        OpenBISScreeningML.logout();
    }

    private static void sleepLong()
    {
        try
        {
            Thread.sleep(1000000);
        } catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }

    private static void testListChannels(String experiment)
    {
        Object[][] channels = OpenBISScreeningML.listChannels(experiment);
        print2DArray(channels);
    }

    private static void testUploadDataset(String chosenPlate, String datasetType)
    {
        String file = "/Users/tpylak/main/tmp/bioz-test/PlateSummary.csv";
        String datasetCode;
        Object[][] dataSetProperties = new Object[][]
        { new Object[] {} };

        datasetCode =
                (String) OpenBISScreeningML.uploadDataSet(chosenPlate, file, datasetType,
                        dataSetProperties);
        System.out.println("Uploaded any dataset " + datasetCode);
    }

    private static void testLoadFeatures(String chosenPlate, String experiment)
    {
        Object[][] features = OpenBISScreeningML.listFeatures(experiment, null);
        print2DArray(features);

        Object[][][] featureMatrix =
                OpenBISScreeningML.getFeatureMatrixForPlate(chosenPlate, null, null);
        System.out.println("per plate features -------------------------------");
        print3DArray(featureMatrix);

        String[] featureNames = new String[]
        { "OOF" };
        featureMatrix =
                OpenBISScreeningML.getFeatureMatrixForPlate(chosenPlate, null, featureNames);
        System.out.println("one per plate feature -------------------------------");
        print3DArray(featureMatrix);

        featureMatrix = OpenBISScreeningML.getFeatureMatrix(experiment, "149420", featureNames);
        System.out.println("one per gene feature -------------------------------");
        print3DArray(featureMatrix);
    }

    private static void print3DArray(Object[][][] array)
    {
        System.out.println("{");
        for (int i = 0; i < array.length; i++)
        {
            print2DArray(array[i]);
        }
        System.out.println("}");
    }

    private static void print2DArray(Object[][] array)
    {
        System.out.println("[");
        for (int i = 0; i < array.length; i++)
        {
            printAsRow(array[i]);
        }
        System.out.println("]");
    }

    private static void printAsRow(Object[] objects)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < objects.length; i++)
        {
            sb.append(objects[i]);
            sb.append(",");
        }
        sb.append("]");
        System.out.println(sb.toString());
    }

    private static void testImagesMetadata(String chosenPlate)
    {
        Object[][] meta = OpenBISScreeningML.getImagesMetadata(chosenPlate);
        System.out.println(String.format("Number of tiles: %s. Plate geometry %sx%s", meta[0][2],
                meta[0][5], meta[0][6]));
    }

    private static void testLoadDataset(String chosenPlate, String mountPoint,
            String datasetTypePattern)
    {
        // Loads dataset with classification results.
        // OpenBIS store diretcory is mounted locally in "/mount/openbis/store", so no data are
        // copied and just a path to the appropriate location
        // is returned.
        Object[][] datasets =
                OpenBISScreeningML.loadDataSets(chosenPlate, datasetTypePattern, mountPoint);
        System.out.println("Path to the first downloaded dataset: " + datasets[0][1]);
    }

    private static void testLoadImage(String chosenPlate)
    {
        // fetch 3ed tile of well (1,1). The second call to fetch this image will return it from the
        // local cache.
        // The last optional parameter can specify a list of channels to load (otherwise all
        // channels are loaded).
        long start = System.currentTimeMillis();
        Object[][][] images = OpenBISScreeningML.loadImages(chosenPlate, 1, 1, 3, new String[]
        { "CY3" });
        System.out.println("Image path: " + images[0][0][0]);
        System.out.println("Took: " + (System.currentTimeMillis() - start));
    }

    private static void testWellProperties(String chosenPlate)
    {
        // properties of well (2,4)
        Object[][] props = OpenBISScreeningML.getWellProperties(chosenPlate, 2, 4);
        for (int i = 0; i < props.length; i++)
        {
            System.out.println(String.format("Property %s = %s ", props[i][0], props[i][1]));
        }

        // save description of the well (2,4)
        props = new Object[][]
        {
                { "DESCRIPTION", "hello example" } };
        OpenBISScreeningML.updateWellProperties(chosenPlate, 2, 4, props);
    }
}
