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

package ch.systemsx.cisd.openbis.dss.etl.featurevector;

import static org.testng.AssertJUnit.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.lemnik.eodsql.QueryTool;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.etl.HCSContainerDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.AbstractDBTest;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class FeatureVectorUploaderTest extends AbstractDBTest
{
    private static final String EXP_PERM_ID = "expFvuId";

    private static final String CONTAINER_PERM_ID = "cFvuId";

    private static final String DS_PERM_ID = "dsFvuId";

    private static final String TEST_FEATURE_NAME = "test";

    private IImagingQueryDAO dao;

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        dao = QueryTool.getQuery(datasource, IImagingQueryDAO.class);
    }

    @Test
    public void testInit()
    {
        // tests that parameter bindings in all queries are correct
    }

    @Test
    public void testCreateFeatureValues()
    {
        HCSContainerDatasetInfo info = new HCSContainerDatasetInfo();
        info.setExperimentPermId(EXP_PERM_ID);
        info.setContainerSamplePermId(CONTAINER_PERM_ID);
        info.setDatasetPermId(DS_PERM_ID);
        FeatureVectorUploader uploader = new FeatureVectorUploader(dao, info);
        ArrayList<CanonicalFeatureVector> fvecs = new ArrayList<CanonicalFeatureVector>();
        new FeatureVectorProducer(fvecs).produce();

        uploader.uploadFeatureVectors(fvecs);

        new FeatureVectorVerifier(fvecs.get(0).getFeatureDef().getDataSetId()).verify();
    }

    // Class should be non-static to call the assert methods
    private class FeatureVectorVerifier
    {
        private final long datasetId;

        // Execution state
        private ImgFeatureDefDTO featureDef;

        private ImgFeatureValuesDTO featureValues;

        private int count;

        private FeatureVectorVerifier(long datasetId)
        {
            this.datasetId = datasetId;
        }

        private void verify()
        {
            List<ImgFeatureDefDTO> featureDefs = dao.listFeatureDefsByDataSetIds(datasetId);
            assertEquals(2, featureDefs.size());

            count = 0;
            featureDef = featureDefs.get(count);
            verifyFeatureDef();

            featureDef = featureDefs.get(++count);
            verifyFeatureDef();
        }

        private void verifyFeatureDef()
        {
            assertEquals(TEST_FEATURE_NAME + count, featureDef.getLabel());
            List<ImgFeatureValuesDTO> featureValuesList = dao.getFeatureValues(featureDef.getId());
            assertEquals(1, featureValuesList.size());
            featureValues = featureValuesList.get(0);
            verifyFeatureValues();
        }

        private void verifyFeatureValues()
        {
            assertEquals(0.0, featureValues.getT());
            assertEquals(0.0, featureValues.getZ());

            PlateFeatureValues spreadsheet = featureValues.getValues();
            assertEquals(3, spreadsheet.getGeometry().getNumberOfRows());
            assertEquals(5, spreadsheet.getGeometry().getNumberOfColumns());

            for (int row = 1; row <= 3; ++row)
            {
                for (int col = 1; col <= 5; ++col)
                {
                    assertEquals((float) (row + col), spreadsheet.getForWellLocation(row, col));
                }
            }
        }
    }

    private static class FeatureVectorProducer
    {
        private final ArrayList<CanonicalFeatureVector> fvecs;

        private FeatureVectorProducer(ArrayList<CanonicalFeatureVector> fvecs)
        {
            this.fvecs = fvecs;
        }

        private void produce()
        {

            fvecs.add(createFeatureVector(0, 3, 5));
            fvecs.add(createFeatureVector(1, 3, 5));
        }

        private CanonicalFeatureVector createFeatureVector(int i, int rowCount, int columnCount)
        {
            CanonicalFeatureVector fvec = new CanonicalFeatureVector();

            String featureName = TEST_FEATURE_NAME + i;
            String featureDesc = featureName + " desc";
            ImgFeatureDefDTO featureDef =
                    new ImgFeatureDefDTO(featureName, featureName, featureDesc, 0);
            fvec.setFeatureDef(featureDef);
            PlateFeatureValues values = createValues(rowCount, columnCount);
            ImgFeatureValuesDTO featureValues = new ImgFeatureValuesDTO(0.0, 0.0, values, 0);

            fvec.setValues(Collections.singletonList(featureValues));

            return fvec;
        }

        private PlateFeatureValues createValues(int rowCount, int columnCount)
        {
            PlateFeatureValues values =
                    new PlateFeatureValues(Geometry.createFromRowColDimensions(rowCount,
                            columnCount));
            for (int row = 1; row <= rowCount; ++row)
            {
                for (int col = 1; col <= columnCount; ++col)
                {
                    values.setForWellLocation(row + col, row, col);
                }
            }
            return values;
        }
    }
}
