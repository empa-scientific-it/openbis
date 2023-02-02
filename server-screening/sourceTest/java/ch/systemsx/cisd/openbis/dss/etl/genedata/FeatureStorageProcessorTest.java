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

package ch.systemsx.cisd.openbis.dss.etl.genedata;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.IStorageProcessorTransaction;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.StorageProcessorTransactionParameters;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateDimensionParser;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAnalysisDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgExperimentDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;

/**
 * @author Franz-Josef Elmer
 */
public class FeatureStorageProcessorTest extends AbstractFileSystemTestCase
{
    private static final String DATA_SET_PERM_ID = "dataset-1";

    private static final String CONTAINER_PERM_ID = "perm12";

    private static final String EXPERIMENT_PERM_ID = "perm11";

    private static final String EXAMPLE1 = "barcode = Plate_042" + "\n\n<Layer=alpha>\n"
            + "\t1\t2\n" + "A\t4.5\t4.6\n" + "B\t3.5\t5.6\n" + "C\t3.3\t5.7\n"
            + "\n\n<Layer=beta>\n" + "\t1\t2\n" + "A\t14.5\t14.6\n" + "B\t13.5\t15.6\n"
            + "C\t13.3\t15.7\n";

    private Mockery context;

    private IImagingQueryDAO dao;

    private DataSource dataSource;

    private IEncapsulatedOpenBISService openBisService;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();

        context = new Mockery();
        dao = context.mock(IImagingQueryDAO.class);
        dataSource = context.mock(DataSource.class);
        openBisService = context.mock(IEncapsulatedOpenBISService.class);

        context.checking(new Expectations()
            {
                {
                    one(dao).tryGetExperimentByPermId(EXPERIMENT_PERM_ID);
                    ImgExperimentDTO exp = new ImgExperimentDTO();
                    exp.setId(1);
                    will(returnValue(exp));

                    one(dao).tryGetContainerIdPermId(CONTAINER_PERM_ID);
                    will(returnValue((long) 1));

                    ImgAnalysisDatasetDTO dataSetDTO =
                            new ImgAnalysisDatasetDTO(DATA_SET_PERM_ID, 1L);
                    dataSetDTO.setId(1);
                    one(dao).tryGetAnalysisDatasetByPermId(DATA_SET_PERM_ID);
                    will(returnValue(dataSetDTO));

                    long datasetId = 1;
                    one(dao).addAnalysisDataset(with(any(ImgAnalysisDatasetDTO.class)));
                    will(returnValue(datasetId));

                    ImgFeatureDefDTO featureDTO =
                            new ImgFeatureDefDTO("alpha", "ALPHA", "alpha", datasetId);
                    one(dao).addFeatureDef(with(equal(featureDTO)));
                    will(returnValue((long) 1));

                    one(dao).addFeatureValues(with(any(ImgFeatureValuesDTO.class)));
                    will(returnValue((long) 1));

                    featureDTO = new ImgFeatureDefDTO("beta", "BETA", "beta", datasetId);
                    one(dao).addFeatureDef(with(equal(featureDTO)));
                    will(returnValue((long) 2));

                    one(dao).addFeatureValues(with(any(ImgFeatureValuesDTO.class)));
                    will(returnValue((long) 2));

                    one(dao).commit();
                    one(dao).close();
                }
            });
    }

    @Test
    public void test()
    {
        File incomingDir = new File(workingDirectory, "incoming");
        incomingDir.mkdirs();
        File dataSetFile = new File(incomingDir, "Plate042.stat");
        FileUtilities.writeToFile(dataSetFile, EXAMPLE1);
        File rootDir = new File(workingDirectory, "rootDir");
        rootDir.mkdirs();
        Properties storageProcessorProps = createStorageProcessorProperties();
        IStorageProcessorTransactional storageProcessor =
                new FeatureStorageProcessor(storageProcessorProps)
                    {
                        // For Testing

                        @Override
                        protected IImagingQueryDAO createDAO()
                        {
                            return dao;
                        }

                        @Override
                        protected DataSource createDataSource(Properties properties)
                        {
                            // Overide because we have problems with Spring otherwise.
                            return dataSource;
                        }

                        @Override
                        protected IEncapsulatedOpenBISService createOpenBisService()
                        {
                            // Overide because we have problems with Spring otherwise.
                            return openBisService;
                        }

                    };

        DataSetInformation dataSetInfo = createDataSetInformation();
        final StorageProcessorTransactionParameters parameters =
                new StorageProcessorTransactionParameters(dataSetInfo, dataSetFile, rootDir);

        IStorageProcessorTransaction transaction = storageProcessor.createTransaction(parameters);
        transaction.storeData(null, null, dataSetFile);

        assertEquals(0, incomingDir.listFiles().length);
        assertEquals(1, rootDir.listFiles().length);
        File original = new File(rootDir, "original");
        assertEquals(true, original.isDirectory());
        assertEquals(2, original.listFiles().length);

        transaction.commit();

        assertEquals(2, original.listFiles().length);
        File[] transformedFiles = original.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.endsWith("stat.txt");
                }
            });
        assertEquals(1, transformedFiles.length);
        File transformedDataSetFile = transformedFiles[0];
        assertEquals("Plate042.stat.txt", transformedDataSetFile.getName());
        List<String> lines = FileUtilities.loadToStringList(transformedDataSetFile);
        assertEquals("barcode;row;col;alpha;beta", lines.get(0));
        assertEquals("Plate_042;A;1;4.5;14.5", lines.get(1));
        assertEquals("Plate_042;A;2;4.6;14.6", lines.get(2));
        assertEquals("Plate_042;B;1;3.5;13.5", lines.get(3));
        assertEquals("Plate_042;B;2;5.6;15.6", lines.get(4));
        assertEquals("Plate_042;C;1;3.3;13.3", lines.get(5));
        assertEquals("Plate_042;C;2;5.7;15.7", lines.get(6));
        assertEquals(7, lines.size());
    }

    private DataSetInformation createDataSetInformation()
    {
        // Set the Experiment
        DataSetInformation dataSetInfo = new DataSetInformation();
        Experiment exp = new Experiment();
        exp.setIdentifier("/Test/Test1/Exp1");
        exp.setPermId(EXPERIMENT_PERM_ID);
        dataSetInfo.setExperiment(exp);

        // Set the Sample
        Sample sample = new Sample();
        sample.setCode("Samp1");
        sample.setExperiment(exp);
        sample.setPermId(CONTAINER_PERM_ID);
        dataSetInfo.setSample(sample);

        // Set the DataSet
        dataSetInfo.setDataSetCode(DATA_SET_PERM_ID);

        // Set the properties
        IEntityProperty properties[] = new IEntityProperty[1];
        GenericEntityProperty entityProperty = new GenericEntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(PlateDimensionParser.PLATE_GEOMETRY_PROPERTY_NAME);
        entityProperty.setPropertyType(propertyType);
        entityProperty.setValue("A_2X2");
        properties[0] = entityProperty;
        sample.setProperties(Arrays.asList(properties));
        dataSetInfo.setSampleProperties(properties);
        return dataSetInfo;
    }

    private Properties createStorageProcessorProperties()
    {
        Properties storageProcessorProps = new Properties();
        storageProcessorProps.setProperty("processor",
                "ch.systemsx.cisd.etlserver.DefaultStorageProcessor");
        // storageProcessorProps.setProperty("data-source", "imaging-db");
        storageProcessorProps.setProperty("data-source", "imaging-db");
        return storageProcessorProps;
    }
}
