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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetMetadataDTO;
import ch.systemsx.cisd.openbis.generic.client.cli.Login;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.DataSetInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.filter.IDataSetFilter;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade.IImageOutputStreamProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageChannel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * @author Franz-Josef Elmer
 */
public class OpenBISScreeningMLTest extends AbstractFileSystemTestCase
{
    private static String DATASETS_FOLDER = "DataSets";

    private static final PlateIdentifier PLATE_1 = PlateIdentifier
            .createFromAugmentedCode("/S/PLATE-1");

    private static final FilenameFilter FILTER_TEMP_DIR = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.startsWith(OpenBISScreeningML.TEMP_DIR_PREFIX)
                        && name.endsWith(OpenBISScreeningML.TEMP_DIR_POSTFIX);
            }
        };

    private Mockery context;

    private IScreeningOpenbisServiceFacade openbis;

    private File tempDir;

    private ExperimentIdentifier eId1;

    private ExperimentIdentifier eId2;

    private IDataSetDss ds1;

    private IDataSetDss ds2;

    private IScreeningOpenbisServiceFacadeFactory facadeFactory;

    private IOpenbisServiceFacadeFactory genericFacadeFactory;

    private IOpenbisServiceFacade genericOpenbis;

    private Plate p1;

    private Plate p2;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        openbis = context.mock(IScreeningOpenbisServiceFacade.class);
        facadeFactory = context.mock(IScreeningOpenbisServiceFacadeFactory.class);
        genericOpenbis = context.mock(IOpenbisServiceFacade.class);
        genericFacadeFactory = context.mock(IOpenbisServiceFacadeFactory.class);

        ds1 = context.mock(IDataSetDss.class, "ds1");
        ds2 = context.mock(IDataSetDss.class, "ds2");
        FileUtilities.deleteRecursively(workingDirectory);
        workingDirectory.mkdirs();
        OpenBISScreeningML.tempDir = workingDirectory;
        eId1 = new ExperimentIdentifier("E1", "P", "S", "e-1");
        eId2 = new ExperimentIdentifier("E2", "P", "S", "e-2");
        p1 = new Plate("PLATE-1", "S", "s-1", eId1);
        p2 = new Plate("PLATE-2", "S", "s-2", eId2);
        context.checking(new Expectations()
            {
                {
                    one(facadeFactory).tryToCreate("user", "password", "url");
                    will(returnValue(openbis));

                    one(openbis).getSessionToken();
                    will(returnValue("SESSION"));

                    one(genericFacadeFactory).tryToCreate("SESSION", "url", 0);
                    will(returnValue(genericOpenbis));

                    one(openbis).listExperiments();
                    will(returnValue(Arrays.asList(eId1, eId2)));

                    one(openbis).listPlates();
                    will(returnValue(Arrays.asList(p1, p2)));
                }
            });
        OpenBISScreeningML.facadeFactory = facadeFactory;
        OpenBISScreeningML.genericFacadeFactory = genericFacadeFactory;
        OpenBISScreeningML.login("user", "password", "url");
        tempDir = OpenBISScreeningML.tempDir.listFiles(FILTER_TEMP_DIR)[0];
    }

    @AfterMethod
    public void afterMethod()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testLogout()
    {
        context.checking(new Expectations()
            {
                {
                    one(openbis).logout();
                }
            });
        File a = new File(tempDir, "a");
        a.mkdirs();
        FileUtilities.writeToFile(new File(a, "1.txt"), "one");
        FileUtilities.writeToFile(new File(tempDir, "2.txt"), "two");

        OpenBISScreeningML.logout();

        assertEquals(false, tempDir.exists());
        context.assertIsSatisfied();
    }

    @Test
    public void testCheckLoggedInFailsBecauseOfMissingSessionTokenFile()
    {
        context.checking(new Expectations()
            {
                {
                    one(openbis).logout();
                }
            });
        OpenBISScreeningML.logout();

        try
        {
            OpenBISScreeningML.listExperiments();
            fail("RuntimeException expected");
        } catch (RuntimeException ex)
        {
            assertEquals("Not logged in.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testCheckLoggedInFailsBecauseOfInvalidSession()
    {
        context.checking(new Expectations()
            {
                {
                    one(openbis).logout();
                    one(facadeFactory).tryToCreate("session-token-1", "url");
                    will(returnValue(null));
                }
            });
        OpenBISScreeningML.logout();
        Login.OPENBIS_USER_FILE.getParentFile().mkdirs();
        FileUtilities.writeToFile(Login.OPENBIS_TOKEN_FILE, "session-token-1");
        FileUtilities.writeToFile(Login.OPENBIS_SERVER_URL_FILE, "url");

        try
        {
            OpenBISScreeningML.listExperiments();
            fail("RuntimeException expected");
        } catch (RuntimeException ex)
        {
            assertEquals("Login failed.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testCheckLoggedInSucceed()
    {
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(openbis).logout();

                    one(facadeFactory).tryToCreate("session-token-1", "url");
                    will(returnValue(openbis));

                    one(genericFacadeFactory).tryToCreate("session-token-1", "url", 0);
                    will(returnValue(genericOpenbis));

                    one(openbis).listExperiments();
                    one(openbis).listPlates();
                }
            });
        OpenBISScreeningML.logout();
        Login.OPENBIS_USER_FILE.getParentFile().mkdirs();
        FileUtilities.writeToFile(Login.OPENBIS_TOKEN_FILE, "session-token-1");
        FileUtilities.writeToFile(Login.OPENBIS_SERVER_URL_FILE, "url");

        OpenBISScreeningML.listExperiments();
        OpenBISScreeningML.logout();

        assertEquals(false, Login.OPENBIS_TOKEN_FILE.exists());
        context.assertIsSatisfied();
    }

    @Test
    public void testListExperiments()
    {
        Object[][] experiments = OpenBISScreeningML.listExperiments();

        sort(experiments);
        assertEquals("/S/P/E1", experiments[0][0]);
        assertEquals("e-1", experiments[0][1]);
        assertEquals("S", experiments[0][2]);
        assertEquals("P", experiments[0][3]);
        assertEquals("E1", experiments[0][4]);
        assertEquals("/S/P/E2", experiments[1][0]);
        assertEquals("e-2", experiments[1][1]);
        assertEquals("S", experiments[1][2]);
        assertEquals("P", experiments[1][3]);
        assertEquals("E2", experiments[1][4]);
        assertEquals(2, experiments.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testListPlates()
    {
        Object[][] plates = OpenBISScreeningML.listPlates();

        sort(plates);
        assertEquals("/S/PLATE-1", plates[0][0]);
        assertEquals("s-1", plates[0][1]);
        assertEquals("S", plates[0][2]);
        assertEquals("PLATE-1", plates[0][3]);
        assertEquals("/S/P/E1", plates[0][4]);
        assertEquals("e-1", plates[0][5]);
        assertEquals("S", plates[0][6]);
        assertEquals("P", plates[0][7]);
        assertEquals("E1", plates[0][8]);
        assertEquals("/S/PLATE-2", plates[1][0]);
        assertEquals("s-2", plates[1][1]);
        assertEquals("S", plates[1][2]);
        assertEquals("PLATE-2", plates[1][3]);
        assertEquals("/S/P/E2", plates[1][4]);
        assertEquals("e-2", plates[1][5]);
        assertEquals("S", plates[1][6]);
        assertEquals("P", plates[1][7]);
        assertEquals("E2", plates[1][8]);
        assertEquals(2, plates.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testListPlatesByExperiment()
    {
        try
        {
            OpenBISScreeningML.listPlates("/S/P/E3");
            fail("RuntimeException expected");
        } catch (RuntimeException ex)
        {
            assertEquals("No experiment with that code found.", ex.getMessage());
        }

        Object[][] plates = OpenBISScreeningML.listPlates("/S/P/E1");

        assertEquals("/S/PLATE-1", plates[0][0]);
        assertEquals("s-1", plates[0][1]);
        assertEquals("S", plates[0][2]);
        assertEquals("PLATE-1", plates[0][3]);
        assertEquals("/S/P/E1", plates[0][4]);
        assertEquals("e-1", plates[0][5]);
        assertEquals("S", plates[0][6]);
        assertEquals("P", plates[0][7]);
        assertEquals("E1", plates[0][8]);
        assertEquals(1, plates.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testListPlatesByExperimentAndAnalysisProcedure()
    {

        final String analysisProcedure = "PROCEDURE";

        context.checking(new Expectations()
            {
                {
                    one(openbis).listPlates(eId1, analysisProcedure);
                    will(returnValue(Arrays.asList(p1)));
                }
            });

        Object[][] plates = OpenBISScreeningML.listPlates("/S/P/E1", analysisProcedure);

        assertEquals("/S/PLATE-1", plates[0][0]);
        assertEquals("s-1", plates[0][1]);
        assertEquals("S", plates[0][2]);
        assertEquals("PLATE-1", plates[0][3]);
        assertEquals("/S/P/E1", plates[0][4]);
        assertEquals("e-1", plates[0][5]);
        assertEquals("S", plates[0][6]);
        assertEquals("P", plates[0][7]);
        assertEquals("E1", plates[0][8]);
        assertEquals(1, plates.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testListFeatures()
    {
        context.checking(new Expectations()
            {
                {
                    one(openbis).listFeatureVectorDatasets(Arrays.asList(p1), null);
                    List<FeatureVectorDatasetReference> list =
                            Arrays.asList((FeatureVectorDatasetReference) null);
                    will(returnValue(list));

                    one(openbis).listAvailableFeatureCodes(list);
                    will(returnValue(Arrays.asList("F1", "F2")));
                }
            });

        Object[][] features = OpenBISScreeningML.listFeatures("/S/P/E1", null);

        assertEquals("F1", features[0][0]);
        assertEquals("F2", features[1][0]);
        assertEquals(2, features.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetFeatureMatrixForGene()
    {
        final MaterialIdentifier gene = new MaterialIdentifier(MaterialTypeIdentifier.GENE, "GEN1");
        context.checking(new Expectations()
            {
                {
                    one(openbis).loadFeaturesForPlateWells(gene, null, null);
                    FeatureVectorDatasetReference fds1 =
                            new FeatureVectorDatasetReference("ds1", "MY-TYPE", "", p1,
                                    ExperimentIdentifier.createFromAugmentedCode("/S/P/E"),
                                    Geometry.createFromCartesianDimensions(3, 2), new Date(4711),
                                    null, null);
                    FeatureVectorDatasetWellReference well1 =
                            new FeatureVectorDatasetWellReference(fds1, new WellPosition(3, 4));
                    FeatureVectorWithDescription f1 =
                            new FeatureVectorWithDescription(well1, Arrays.asList("F1", "F3"),
                                    new double[]
                                    { 1.5, 42 });
                    FeatureVectorDatasetReference fds2 =
                            new FeatureVectorDatasetReference("ds2", "MY-TYPE", "", p2,
                                    ExperimentIdentifier.createFromAugmentedCode("/S/P/E"),
                                    Geometry.createFromCartesianDimensions(3, 2), new Date(4711),
                                    null, null);
                    FeatureVectorDatasetWellReference well2 =
                            new FeatureVectorDatasetWellReference(fds2, new WellPosition(2, 7));
                    FeatureVectorWithDescription f2 =
                            new FeatureVectorWithDescription(well2, Arrays.asList("F1", "F2"),
                                    new double[]
                                    { -3, 7.125 });
                    will(returnValue(Arrays.asList(f1, f2)));
                }
            });

        Object[][][][] matrix = OpenBISScreeningML.getFeatureMatrix("GEN1", null, null);

        assertEquals(Double.NaN, matrix[0][0][0][0]);
        assertEquals(1.5, matrix[0][0][1][0]);
        assertEquals(Double.NaN, matrix[0][1][0][0]);
        assertEquals(Double.NaN, matrix[0][1][1][0]);
        assertEquals(Double.NaN, matrix[0][2][0][0]);
        assertEquals(42.0, matrix[0][2][1][0]);
        assertEquals(-3.0, matrix[0][0][0][1]);
        assertEquals(Double.NaN, matrix[0][0][1][1]);
        assertEquals(7.125, matrix[0][1][0][1]);
        assertEquals(Double.NaN, matrix[0][1][1][1]);
        assertEquals(Double.NaN, matrix[0][2][0][1]);
        assertEquals(Double.NaN, matrix[0][2][1][1]);
        assertEquals(3, matrix[0].length);
        assertEquals(
                "[PLATE-2:B7, /S/PLATE-2, s-2, S, PLATE-2, 2, 7, /S/P/E, null, S, P, E, ds2, MY-TYPE]",
                Arrays.asList(matrix[1][0][1]).toString());
        assertEquals(
                "[PLATE-1:C4, /S/PLATE-1, s-1, S, PLATE-1, 3, 4, /S/P/E, null, S, P, E, ds1, MY-TYPE]",
                Arrays.asList(matrix[1][1][0]).toString());
        assertEquals(2, matrix[1].length);
        assertEquals("F1", matrix[2][0][0][0]);
        assertEquals("F2", matrix[2][1][0][0]);
        assertEquals("F3", matrix[2][2][0][0]);
        assertEquals(3, matrix[2].length);
        assertEquals(3, matrix.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetEmptyFeatureMatrixForPlate()
    {
        context.checking(new Expectations()
            {
                {
                    one(openbis).loadFeaturesForPlates(
                            Arrays.asList(PlateIdentifier.createFromAugmentedCode(p1
                                    .getAugmentedCode())), null, null);
                }
            });

        Object[][][][] matrix =
                OpenBISScreeningML.getFeatureMatrixForPlate(p1.getAugmentedCode(), null, null);

        assertEquals(0, matrix[0].length);
        assertEquals(0, matrix[1].length);
        assertEquals(0, matrix[2].length);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetFeatureMatrixForPlate()
    {
        context.checking(new Expectations()
            {
                {
                    one(openbis).loadFeaturesForPlates(Arrays.asList(PLATE_1),
                            Arrays.asList("F1", "F2", "F3"), null);
                    FeatureVectorDatasetReference ref =
                            new FeatureVectorDatasetReference("ds1", "MY-TYPE", "", PLATE_1,
                                    ExperimentIdentifier.createFromAugmentedCode("/S/P/E"),
                                    Geometry.createFromCartesianDimensions(3, 2), new Date(4711),
                                    null, null);
                    FeatureVector v1 = new FeatureVector(new WellPosition(2, 1), new double[]
                    { 1.5, 42 });
                    FeatureVector v2 = new FeatureVector(new WellPosition(1, 3), new double[]
                    { 1.25, 42.5 }, new boolean[]
                    { true, false }, new String[]
                    { "a", "b" });
                    FeatureVectorDataset d1 =
                            new FeatureVectorDataset(ref, Arrays.asList("F1", "F3"), Arrays.asList(
                                    "f1", "f3"), Arrays.asList(v1, v2));
                    will(returnValue(Arrays.asList(d1)));
                }
            });

        Object[][][][] matrix =
                OpenBISScreeningML.getFeatureMatrixForPlate("/S/PLATE-1", null, new String[]
                { "F1", "F2", "F3" });

        assertPlateFeatures("[a, 1.5]", matrix[0][0]);
        assertPlateFeatures("[42.5, 42.0]", matrix[0][1]);
        assertEquals(2, matrix[0].length);
        assertEquals(
                "[PLATE-1:A3, /S/PLATE-1, null, S, PLATE-1, 1, 3, /S/P/E, null, S, P, E, ds1, MY-TYPE]",
                Arrays.asList(matrix[1][0][0]).toString());
        assertEquals(
                "[PLATE-1:B1, /S/PLATE-1, null, S, PLATE-1, 2, 1, /S/P/E, null, S, P, E, ds1, MY-TYPE]",
                Arrays.asList(matrix[1][1][0]).toString());
        assertEquals(2, matrix[1].length);
        assertEquals("F1", matrix[2][0][0][0]);
        assertEquals("F3", matrix[2][1][0][0]);
        assertEquals(2, matrix[2].length);
        context.assertIsSatisfied();
    }

    private void assertPlateFeatures(String expectedFeatures, Object[][] plateFeatures)
    {
        List<Object> list = new ArrayList<Object>();
        for (Object[] plateFeature : plateFeatures)
        {
            list.add(plateFeature[0]);
            assertEquals(1, plateFeature.length);
        }
        assertEquals(expectedFeatures, list.toString());
    }

    @Test
    public void testGetWellProperties()
    {
        context.checking(new Expectations()
            {
                {
                    Plate plate1 = new Plate("PLATE-1", "S", "s-1", eId1);
                    one(openbis).listPlateWells(plate1);
                    WellIdentifier w1 = new WellIdentifier(plate1, new WellPosition(1, 1), "w1");
                    WellIdentifier w2 = new WellIdentifier(plate1, new WellPosition(1, 2), "w2");
                    WellIdentifier w3 = new WellIdentifier(plate1, new WellPosition(2, 1), "w3");
                    will(returnValue(Arrays.asList(w1, w2, w3)));

                    one(openbis).getWellProperties(w2);
                    Map<String, String> properties = new LinkedHashMap<String, String>();
                    properties.put("answer", "42");
                    properties.put("name", "Albert");
                    will(returnValue(properties));
                }
            });

        Object[][] wellProperties = OpenBISScreeningML.getWellProperties("/S/PLATE-1", 1, 2);

        assertEquals("answer", wellProperties[0][0]);
        assertEquals("42", wellProperties[0][1]);
        assertEquals("name", wellProperties[1][0]);
        assertEquals("Albert", wellProperties[1][1]);
        assertEquals(2, wellProperties.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateWellProperties()
    {
        context.checking(new Expectations()
            {
                {
                    Plate plate1 = new Plate("PLATE-1", "S", "s-1", eId1);
                    one(openbis).listPlateWells(plate1);
                    WellIdentifier w1 = new WellIdentifier(plate1, new WellPosition(1, 1), "w1");
                    WellIdentifier w2 = new WellIdentifier(plate1, new WellPosition(1, 2), "w2");
                    WellIdentifier w3 = new WellIdentifier(plate1, new WellPosition(2, 1), "w3");
                    will(returnValue(Arrays.asList(w1, w2, w3)));

                    Map<String, String> properties = new LinkedHashMap<String, String>();
                    properties.put("A", "42");
                    properties.put("B", "43");
                    one(openbis).updateWellProperties(w2, properties);
                }
            });

        OpenBISScreeningML.updateWellProperties("/S/PLATE-1", 1, 2, new Object[][]
        {
                { "A", "42" },
                { "B", "43" } });

        context.assertIsSatisfied();
    }

    @Test
    public void testListDataSetsFiles()
    {
        final RecordingMatcher<IDataSetFilter> filterMatcher =
                new RecordingMatcher<IDataSetFilter>();
        context.checking(new Expectations()
            {
                {
                    one(openbis).getDataSets(with(p1), with(filterMatcher));
                    will(returnValue(Arrays.asList(ds1, ds2)));

                    one(ds1).listFiles("/", true);
                    will(returnValue(new FileInfoDssDTO[]
                    { new FileInfoDssDTO("a", "a", true, -1),
                            new FileInfoDssDTO("a/b", "a/b", false, 42) }));
                    one(ds1).getCode();
                    will(returnValue("ds1"));

                    one(ds2).listFiles("/", true);
                    will(returnValue(new FileInfoDssDTO[]
                    { new FileInfoDssDTO("c", "c", false, 137) }));
                    one(ds2).getCode();
                    will(returnValue("ds2"));
                }
            });

        Object[][][] files = OpenBISScreeningML.listDataSetsFiles(p1.getAugmentedCode(), ".*");

        assertEquals("Type:.*", filterMatcher.recordedObject().toString());
        assertEquals("ds1", files[0][0][0]);
        assertEquals("[a, a/b]", Arrays.asList(files[0][1]).toString());
        assertEquals("ds2", files[1][0][0]);
        assertEquals("[c]", Arrays.asList(files[1][1]).toString());
        assertEquals(2, files.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadDataSetFile()
    {
        context.checking(new Expectations()
            {
                {
                    one(openbis).getDataSet("ds1");
                    will(returnValue(ds1));

                    one(ds1).getLinkOrCopyOfContent("root", tempDir, "a/b/c");
                    will(returnValue(new File("data")));
                }
            });

        Object file = OpenBISScreeningML.loadDataSetFile("ds1", "a/b/c", "root");

        assertEquals("data", file);
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadDataSets()
    {
        final File dataSetFolder =
                new File(OpenBISScreeningML.tempDir, DATASETS_FOLDER);
        final File ds1Folder = new File(dataSetFolder, "ds-1");
        final File ds2Folder = new File(dataSetFolder, "ds-2");
        ds2Folder.mkdirs();
        final String datasetTypePattern = "blablaCode";
        final String mountPoint = "/mount/openbis/store";
        final RecordingMatcher<IDataSetFilter> filterMatcher =
                new RecordingMatcher<IDataSetFilter>();

        List<String> codes = new ArrayList<String>();
        final DataSet dataSet1 = createDataSet("ds-1", ds1, codes, codes);
        final DataSet dataSet2 = createDataSet("ds-2", ds2, codes, codes);

        context.checking(new Expectations()
            {
                {
                    one(openbis).getFullDataSets(with(new Plate("PLATE-1", "S", "s-1", eId1)),
                            with(filterMatcher));
                    will(returnValue(Arrays.asList(dataSet1, dataSet2)));

                    one(ds1).getLinkOrCopyOfContents(with(mountPoint), with(any(File.class)));
                    will(returnValue(ds1Folder));

                    one(ds2).getLinkOrCopyOfContents(with(mountPoint), with(any(File.class)));
                    will(returnValue(ds2Folder));

                }
            });

        Object[][] result =
                OpenBISScreeningML.loadDataSets("/S/PLATE-1", datasetTypePattern, mountPoint);

        assertEquals("Type:blablaCode AND Properties:[]", filterMatcher.recordedObject().toString());
        assertEquals("ds-1", result[0][0]);
        assertEquals(ds1Folder.getPath(), result[0][1]);
        assertEqualProperties(dataSet1.getProperties(), (Object[][]) result[0][2]);
        assertEquals("ds-2", result[1][0]);
        assertEquals(ds2Folder.getPath(), result[1][1]);
        assertEqualProperties(dataSet2.getProperties(), (Object[][]) result[1][2]);
        assertEquals(2, result.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadDataSetsFilteredOnProperties()
    {
        final File dataSetFolder =
                new File(OpenBISScreeningML.tempDir, DATASETS_FOLDER);
        final File ds1Folder = new File(dataSetFolder, "ds-1");
        final File ds2Folder = new File(dataSetFolder, "ds-2");
        ds2Folder.mkdirs();
        final String datasetTypePattern = "blablaCode";
        final String mountPoint = "/mount/openbis/store";
        final RecordingMatcher<IDataSetFilter> filterMatcher =
                new RecordingMatcher<IDataSetFilter>();

        List<String> codes = new ArrayList<String>();
        final DataSet dataSet1 = createDataSet("ds-1", ds1, codes, codes);
        final DataSet dataSet2 = createDataSet("ds-2", ds2, codes, codes);

        context.checking(new Expectations()
            {
                {
                    one(openbis).getFullDataSets(with(new Plate("PLATE-1", "S", "s-1", eId1)),
                            with(filterMatcher));
                    will(returnValue(Arrays.asList(dataSet1, dataSet2)));

                    one(ds1).getLinkOrCopyOfContents(with(mountPoint), with(any(File.class)));
                    will(returnValue(ds1Folder));

                    one(ds2).getLinkOrCopyOfContents(with(mountPoint), with(any(File.class)));
                    will(returnValue(ds2Folder));
                }
            });

        Object[][] properties = new Object[][]
        { new Object[]
                { "a", "alpha" }, new Object[]
                { "b", "beta" } };
        Object[][] result =
                OpenBISScreeningML.loadDataSets("/S/PLATE-1", datasetTypePattern, properties,
                        mountPoint);

        assertEquals("Type:blablaCode AND Properties:[a=alpha, b=beta]", filterMatcher
                .recordedObject().toString());
        assertEquals("ds-1", result[0][0]);
        assertEquals(ds1Folder.getPath(), result[0][1]);
        assertEqualProperties(dataSet1.getProperties(), (Object[][]) result[0][2]);
        assertEquals("ds-2", result[1][0]);
        assertEquals(ds2Folder.getPath(), result[1][1]);
        assertEqualProperties(dataSet2.getProperties(), (Object[][]) result[1][2]);
        assertEquals(2, result.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadDataSetsForExperiment()
    {
        final File dataSetFolder =
                new File(OpenBISScreeningML.tempDir, DATASETS_FOLDER);
        final File ds1Folder = new File(dataSetFolder, "ds-1");
        final File ds2Folder = new File(dataSetFolder, "ds-2");
        ds2Folder.mkdirs();
        final String datasetTypePattern = "blablaCode";
        final String mountPoint = "/mount/openbis/store";
        final RecordingMatcher<IDataSetFilter> filterMatcher =
                new RecordingMatcher<IDataSetFilter>();

        List<String> codes = new ArrayList<String>();
        final DataSet dataSet1 = createDataSet("ds-1", ds1, codes, codes);
        final DataSet dataSet2 = createDataSet("ds-2", ds2, codes, codes);

        context.checking(new Expectations()
            {
                {
                    one(openbis).getFullDataSets(with(eId1), with(filterMatcher));
                    will(returnValue(Arrays.asList(dataSet1, dataSet2)));

                    one(ds1).getLinkOrCopyOfContents(with(mountPoint), with(any(File.class)));
                    will(returnValue(ds1Folder));

                    one(ds2).getLinkOrCopyOfContents(with(mountPoint), with(any(File.class)));
                    will(returnValue(ds2Folder));

                }
            });

        Object[][] result =
                OpenBISScreeningML.loadDataSetsForExperiment("/S/P/E1", datasetTypePattern,
                        new Object[0][], mountPoint);

        assertEquals("Type:blablaCode AND Properties:[]", filterMatcher.recordedObject().toString());
        assertEquals("ds-1", result[0][0]);
        assertEquals(ds1Folder.getPath(), result[0][1]);
        assertEqualProperties(dataSet1.getProperties(), (Object[][]) result[0][2]);
        assertEquals("ds-2", result[1][0]);
        assertEquals(ds2Folder.getPath(), result[1][1]);
        assertEqualProperties(dataSet2.getProperties(), (Object[][]) result[1][2]);
        assertEquals(2, result.length);
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateDataSet()
    {
        final File dataSetFolder =
                new File(OpenBISScreeningML.tempDir, DATASETS_FOLDER);
        final File ds1Folder = new File(dataSetFolder, "ds-1");
        ds1Folder.mkdirs();
        final RecordingMatcher<NewDataSetMetadataDTO> metaDataMatcher =
                new RecordingMatcher<NewDataSetMetadataDTO>();
        context.checking(new Expectations()
            {
                {
                    Map<String, String> properties = new LinkedHashMap<String, String>();
                    properties.put("A", "42");
                    properties.put("B", "43");
                    try
                    {
                        one(openbis).putDataSet(with(new Plate("PLATE-1", "S", "s-1", eId1)),
                                with(ds1Folder), with(metaDataMatcher));
                        will(returnValue(ds1));
                    } catch (Exception ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }

                    one(ds1).getCode();
                    will(returnValue("DS-1"));
                }
            });

        Object code =
                OpenBISScreeningML.uploadDataSet("/S/PLATE-1", ds1Folder.getPath(), "my-type",
                        new Object[][]
                        {
                                { "A", "42" },
                                { "B", "43" } });

        assertEquals("DS-1", code);
        assertEquals("my-type", metaDataMatcher.recordedObject().tryDataSetType());
        assertEquals("{A=42, B=43}", metaDataMatcher.recordedObject().getProperties().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testUploadDataSetForPlateAndParents()
    {
        final File dataSetFolder =
                new File(OpenBISScreeningML.tempDir, DATASETS_FOLDER);
        final File ds1Folder = new File(dataSetFolder, "ds-1");
        ds1Folder.mkdirs();
        final RecordingMatcher<NewDataSetMetadataDTO> metaDataMatcher =
                new RecordingMatcher<NewDataSetMetadataDTO>();
        context.checking(new Expectations()
            {
                {
                    Map<String, String> properties = new LinkedHashMap<String, String>();
                    properties.put("A", "42");
                    properties.put("B", "43");
                    try
                    {
                        one(openbis).putDataSet(with(new Plate("PLATE-1", "S", "s-1", eId1)),
                                with(ds1Folder), with(metaDataMatcher));
                        will(returnValue(ds1));
                    } catch (Exception ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }

                    one(ds1).getCode();
                    will(returnValue("DS-1"));
                }
            });

        Object code = OpenBISScreeningML.uploadDataSetForPlateAndParents("/S/PLATE-1", new Object[]
        { "DATA-SET-CODE1", "DATA-SET-CODE2" }, ds1Folder.getPath(), "my-type", new Object[][]
        {
                { "A", "42" },
                { "B", "43" } });

        assertEquals("DS-1", code);
        assertEquals("my-type", metaDataMatcher.recordedObject().tryDataSetType());
        assertEquals("{A=42, B=43}", metaDataMatcher.recordedObject().getProperties().toString());
        assertEquals("[DATA-SET-CODE1, DATA-SET-CODE2]", metaDataMatcher.recordedObject()
                .getParentDataSetCodes().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testUploadDataSetForExperimentAndParents()
    {
        final File dataSetFolder =
                new File(OpenBISScreeningML.tempDir, DATASETS_FOLDER);
        final File ds1Folder = new File(dataSetFolder, "ds-1");
        ds1Folder.mkdirs();
        final RecordingMatcher<NewDataSetMetadataDTO> metaDataMatcher =
                new RecordingMatcher<NewDataSetMetadataDTO>();
        context.checking(new Expectations()
            {
                {
                    Map<String, String> properties = new LinkedHashMap<String, String>();
                    properties.put("A", "42");
                    properties.put("B", "43");
                    try
                    {
                        one(openbis).putDataSet(
                                with(new ExperimentIdentifier("S", "P", "E1", "e-1")),
                                with(ds1Folder), with(metaDataMatcher));
                        will(returnValue(ds1));
                    } catch (Exception ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }

                    one(ds1).getCode();
                    will(returnValue("DS-1"));
                }
            });

        Object code =
                OpenBISScreeningML.uploadDataSetForExperimentAndParents("/S/P/E1", new Object[]
                { "DATA-SET-CODE1", "DATA-SET-CODE2" }, ds1Folder.getPath(), "my-type",
                        new Object[][]
                        {
                                { "A", "42" },
                                { "B", "43" } });

        assertEquals("DS-1", code);
        assertEquals("my-type", metaDataMatcher.recordedObject().tryDataSetType());
        assertEquals("{A=42, B=43}", metaDataMatcher.recordedObject().getProperties().toString());
        assertEquals("[DATA-SET-CODE1, DATA-SET-CODE2]", metaDataMatcher.recordedObject()
                .getParentDataSetCodes().toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testLoadImages()
    {
        final RecordingMatcher<List<PlateImageReference>> imgRefsMatcher1 =
                new RecordingMatcher<List<PlateImageReference>>();
        final RecordingMatcher<List<PlateImageReference>> imgRefsMatcher2 =
                new RecordingMatcher<List<PlateImageReference>>();
        final Sequence sequence = context.sequence("load");
        context.checking(new Expectations()
            {
                {
                    Plate plate = new Plate("PLATE-1", "S", "s-1", eId1);
                    exactly(2).of(openbis).listRawImageDatasets(Arrays.asList(plate));
                    ImageDatasetReference ds1Ref =
                            new ImageDatasetReference("ds1", "MY-TYPE", "", plate, eId1, null,
                                    null, null, null);
                    List<ImageDatasetReference> imageRefs = Arrays.asList(ds1Ref);
                    will(returnValue(imageRefs));

                    exactly(2).of(openbis).listImageMetadata(imageRefs);
                    List<ImageChannel> channels =
                            Arrays.asList(new ImageChannel("G", "green"), new ImageChannel("R",
                                    "red"));
                    ImageDatasetMetadata metaData1 =
                            new ImageDatasetMetadata(ds1Ref, channels, 1, 2, 100, 60, 10, 6);
                    will(returnValue(Arrays.asList(metaData1)));

                    try
                    {
                        one(openbis).loadImages(with(imgRefsMatcher1),
                                with(new BaseMatcher<IImageOutputStreamProvider>()
                                    {
                                        @Override
                                        public boolean matches(Object item)
                                        {
                                            if (item instanceof IImageOutputStreamProvider)
                                            {
                                                IImageOutputStreamProvider provider =
                                                        (IImageOutputStreamProvider) item;
                                                List<PlateImageReference> recordedObject =
                                                        imgRefsMatcher1.recordedObject();
                                                for (PlateImageReference ref : recordedObject)
                                                {
                                                    try
                                                    {
                                                        OutputStream outputStream =
                                                                provider.getOutputStream(ref);
                                                        new PrintWriter(outputStream, true)
                                                                .println(ref.toString() + " (1)");
                                                    } catch (IOException ex)
                                                    {
                                                        throw CheckedExceptionTunnel
                                                                .wrapIfNecessary(ex);
                                                    }
                                                }
                                                return true;
                                            }
                                            return false;
                                        }

                                        @Override
                                        public void describeTo(Description description)
                                        {
                                        }
                                    }), with(false));
                        inSequence(sequence);
                        one(openbis).loadImages(with(imgRefsMatcher2),
                                with(new BaseMatcher<IImageOutputStreamProvider>()
                                    {
                                        @Override
                                        public boolean matches(Object item)
                                        {
                                            if (item instanceof IImageOutputStreamProvider)
                                            {
                                                IImageOutputStreamProvider provider =
                                                        (IImageOutputStreamProvider) item;
                                                List<PlateImageReference> recordedObject =
                                                        imgRefsMatcher2.recordedObject();
                                                for (PlateImageReference ref : recordedObject)
                                                {
                                                    try
                                                    {
                                                        OutputStream outputStream =
                                                                provider.getOutputStream(ref);
                                                        new PrintWriter(outputStream, true)
                                                                .println(ref.toString() + " (2)");
                                                    } catch (IOException ex)
                                                    {
                                                        throw CheckedExceptionTunnel
                                                                .wrapIfNecessary(ex);
                                                    }
                                                }
                                                return true;
                                            }
                                            return false;
                                        }

                                        @Override
                                        public void describeTo(Description description)
                                        {
                                        }
                                    }), with(false));
                        inSequence(sequence);
                    } catch (IOException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }
            });

        Object[][][] result1 = OpenBISScreeningML.loadImages("/S/PLATE-1", 1, 2, 1, new String[]
        { "G" });
        List<PlateImageReference> imgRefs1 = imgRefsMatcher1.recordedObject();
        for (PlateImageReference plateImageReference : imgRefs1)
        {
            assertEquals("ds1", plateImageReference.getDatasetCode());
            assertEquals(1, plateImageReference.getWellPosition().getWellRow());
            assertEquals(2, plateImageReference.getWellPosition().getWellColumn());
        }
        assertEquals("G", imgRefs1.get(0).getChannel());
        assertEquals(1, imgRefs1.get(0).getTile());
        assertEquals(1, imgRefs1.size());
        assertEquals(2, result1.length);
        assertEquals("/images/img_PLATE-1_ds1_row1_col2_G_tile1.tiff", getImagePath(result1, 0));
        assertEquals("Image for [dataset ds1, well [1, 2], channel G, tile 1] (1)", FileUtilities
                .loadToString(new File(result1[0][0][0].toString())).trim());
        assertEquals(1, result1[0].length);
        assertEquals(
                "[G, 1, PLATE-1:A2, /S/PLATE-1, s-1, S, PLATE-1, 1, 2, /S/P/E1, e-1, S, P, E1, ds1]",
                Arrays.asList(result1[1][0]).toString());
        assertEquals(1, result1[1].length);

        Object[][][] result2 = OpenBISScreeningML.loadImages("/S/PLATE-1", 1, 2);
        List<PlateImageReference> imgRefs2 = imgRefsMatcher2.recordedObject();
        for (PlateImageReference plateImageReference : imgRefs2)
        {
            assertEquals("ds1", plateImageReference.getDatasetCode());
            assertEquals(1, plateImageReference.getWellPosition().getWellRow());
            assertEquals(2, plateImageReference.getWellPosition().getWellColumn());
        }
        assertEquals("G", imgRefs2.get(0).getChannel());
        assertEquals(0, imgRefs2.get(0).getTile());
        assertEquals("R", imgRefs2.get(1).getChannel());
        assertEquals(0, imgRefs2.get(1).getTile());
        assertEquals("R", imgRefs2.get(2).getChannel());
        assertEquals(1, imgRefs2.get(2).getTile());
        assertEquals(3, imgRefs2.size());

        assertEquals(2, result2.length);
        assertEquals("/images/img_PLATE-1_ds1_row1_col2_G_tile0.tiff", getImagePath(result2, 0));
        assertEquals("Image for [dataset ds1, well [1, 2], channel G, tile 0] (2)", FileUtilities
                .loadToString(new File(result2[0][0][0].toString())).trim());
        assertEquals("/images/img_PLATE-1_ds1_row1_col2_G_tile1.tiff", getImagePath(result2, 1));
        assertEquals("Image for [dataset ds1, well [1, 2], channel G, tile 1] (1)", FileUtilities
                .loadToString(new File(result2[0][1][0].toString())).trim());

        assertEquals("/images/img_PLATE-1_ds1_row1_col2_R_tile0.tiff", getImagePath(result2, 2));
        assertEquals("Image for [dataset ds1, well [1, 2], channel R, tile 0] (2)", FileUtilities
                .loadToString(new File(result2[0][2][0].toString())).trim());
        assertEquals("/images/img_PLATE-1_ds1_row1_col2_R_tile1.tiff", getImagePath(result2, 3));
        assertEquals("Image for [dataset ds1, well [1, 2], channel R, tile 1] (2)", FileUtilities
                .loadToString(new File(result2[0][3][0].toString())).trim());

        assertEquals(4, result2[0].length);
        assertEquals(
                "[G, 0, PLATE-1:A2, /S/PLATE-1, s-1, S, PLATE-1, 1, 2, /S/P/E1, e-1, S, P, E1, ds1]",
                Arrays.asList(result2[1][0]).toString());
        assertEquals(
                "[G, 1, PLATE-1:A2, /S/PLATE-1, s-1, S, PLATE-1, 1, 2, /S/P/E1, e-1, S, P, E1, ds1]",
                Arrays.asList(result2[1][1]).toString());
        assertEquals(
                "[R, 0, PLATE-1:A2, /S/PLATE-1, s-1, S, PLATE-1, 1, 2, /S/P/E1, e-1, S, P, E1, ds1]",
                Arrays.asList(result2[1][2]).toString());
        assertEquals(
                "[R, 1, PLATE-1:A2, /S/PLATE-1, s-1, S, PLATE-1, 1, 2, /S/P/E1, e-1, S, P, E1, ds1]",
                Arrays.asList(result2[1][3]).toString());
        assertEquals(4, result2[1].length);
        context.assertIsSatisfied();
    }

    @Test
    public void testListAnalysisProcedures()
    {

        final String[] analysisProcedures = new String[]
        { "PROC-1", "PROC-2", "PROC-3" };

        context.checking(new Expectations()
            {
                {
                    one(openbis).listAnalysisProcedures(eId1);
                    will(returnValue(Arrays.asList(analysisProcedures)));
                }
            });

        Object[][] result = OpenBISScreeningML.listAnalysisProcedures("/S/P/E1");
        String[] returnedAnalysisProcedures = new String[result.length];
        for (int i = 0; i < result.length; i++)
        {
            returnedAnalysisProcedures[i] = (String) result[i][0];
        }

        assertEquals(Arrays.toString(analysisProcedures),
                Arrays.toString(returnedAnalysisProcedures));
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDataSetMetaData()
    {
        context.checking(new Expectations()
            {
                {
                    one(openbis).getDataSetMetaData(Arrays.asList("ds1", "ds2"));
                    List<String> noCodes = Arrays.asList();
                    will(returnValue(Arrays.asList(
                            createDataSet("ds1", ds1, Arrays.asList("ds3", "ds4"), noCodes),
                            createDataSet("ds2", ds2, Arrays.asList("ds1"),
                                    Arrays.asList("ds3", "ds5")))));

                    one(ds1).tryGetInternalPathInDataStore();
                    one(ds2).tryGetInternalPathInDataStore();
                }
            });

        Object[][][] dataSets = OpenBISScreeningML.getDataSetMetaData(new String[]
        { "ds1", "ds2" });

        assertEquals("ds1", dataSets[0][0][0]);
        assertEquals("ds-type", dataSets[0][0][1]);
        assertEquals("ds1-key1", ((Object[]) dataSets[0][1][0])[0]);
        assertEquals("ds1-value1", ((Object[]) dataSets[0][1][0])[1]);
        assertEquals("ds1-key2", ((Object[]) dataSets[0][1][1])[0]);
        assertEquals("ds1-value2", ((Object[]) dataSets[0][1][1])[1]);
        assertEquals("[ds3, ds4]", Arrays.asList(dataSets[0][2]).toString());
        assertEquals("[]", Arrays.asList(dataSets[0][3]).toString());

        assertEquals("ds2", dataSets[1][0][0]);
        assertEquals("ds-type", dataSets[1][0][1]);
        assertEquals("ds2-key1", ((Object[]) dataSets[1][1][0])[0]);
        assertEquals("ds2-value1", ((Object[]) dataSets[1][1][0])[1]);
        assertEquals("ds2-key2", ((Object[]) dataSets[1][1][1])[0]);
        assertEquals("ds2-value2", ((Object[]) dataSets[1][1][1])[1]);
        assertEquals("[ds1]", Arrays.asList(dataSets[1][2]).toString());
        assertEquals("[ds3, ds5]", Arrays.asList(dataSets[1][3]).toString());

        assertEquals(2, dataSets.length);
        context.assertIsSatisfied();
    }

    private void assertEqualProperties(Map<String, String> properties, Object[][] matlabProps)
    {
        TreeMap<String, String> expected = new TreeMap<String, String>(properties);
        TreeMap<String, String> actual = new TreeMap<String, String>();
        for (Object[] prop : matlabProps)
        {
            actual.put((String) prop[0], (String) prop[1]);
        }
        assertEquals(expected.toString(), actual.toString());
    }

    private String getImagePath(Object[][][] result, int i)
    {
        return result[0][i][0].toString().substring(tempDir.getPath().length());
    }

    private void sort(Object[][] entities)
    {
        Arrays.sort(entities, new Comparator<Object[]>()
            {
                @Override
                public int compare(Object[] o1, Object[] o2)
                {
                    return o1[0].toString().compareTo(o2[0].toString());
                }
            });
    }

    private DataSet createDataSet(String code, IDataSetDss dataSetDss, List<String> parentCodes,
            List<String> childrenCodes)
    {
        EntityRegistrationDetailsInitializer entityRegInitializer =
                new EntityRegistrationDetailsInitializer();
        EntityRegistrationDetails regDetails = new EntityRegistrationDetails(entityRegInitializer);

        DataSetInitializer dsInitializer = new DataSetInitializer();
        dsInitializer.setRegistrationDetails(regDetails);
        dsInitializer.setCode(code);
        dsInitializer.setExperimentIdentifier("EXPERIMENT");
        dsInitializer.setDataSetTypeCode("ds-type");
        dsInitializer.setParentCodes(parentCodes);
        dsInitializer.setChildrenCodes(childrenCodes);
        dsInitializer
                .setRetrievedConnections(EnumSet.of(Connections.CHILDREN, Connections.PARENTS));

        Map<String, String> properties = createProperties(code);
        for (String propKey : properties.keySet())
        {
            dsInitializer.putProperty(propKey, properties.get(propKey));
        }

        ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet metadata =
                new ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet(dsInitializer);
        return new DataSet(null, null, metadata, dataSetDss);
    }

    private Map<String, String> createProperties(String dataSetCode)
    {
        Map<String, String> properties = new LinkedHashMap<String, String>();
        properties.put(dataSetCode + "-key1", dataSetCode + "-value1");
        properties.put(dataSetCode + "-key2", dataSetCode + "-value2");
        return properties;
    }
}
