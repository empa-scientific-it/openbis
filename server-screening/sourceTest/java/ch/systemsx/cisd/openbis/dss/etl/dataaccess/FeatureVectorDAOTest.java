/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.dataaccess;

import static org.testng.AssertJUnit.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.lemnik.eodsql.QueryTool;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.AbstractDBTest;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgAnalysisDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureVocabularyTermDTO;

/**
 * Tests for {@link IImagingQueryDAO} methods that deal with feature vectors.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@Test(groups =
{ "db", "screening" })
public class FeatureVectorDAOTest extends AbstractDBTest
{
    private IImagingQueryDAO dao;

    private ImgAnalysisDatasetDTO dataset;

    private static final String EXP_PERM_ID = "expFvId";

    private static final String CONTAINER_PERM_ID = "cFvId";

    private static final String DS_PERM_ID = "dsFvId";

    private static final String TEST_FEATURE_LABEL = "test 42";

    @BeforeClass(alwaysRun = true)
    public void init() throws SQLException
    {
        dao = QueryTool.getQuery(datasource, IImagingQueryDAO.class);
    }

    private ImgAnalysisDatasetDTO createAnalysisDataSet()
    {
        IImagingQueryDAO imagingDao = dao;

        // Create an Experiment
        final long experimentId = imagingDao.addExperiment(EXP_PERM_ID);

        // Create a container
        final Integer spotWidth = 1;
        final Integer spotHeight = 2;
        final ImgContainerDTO container =
                new ImgContainerDTO(CONTAINER_PERM_ID, spotHeight, spotWidth, experimentId);
        final Long containerId = imagingDao.addContainer(container);

        final ImgAnalysisDatasetDTO ds = new ImgAnalysisDatasetDTO(DS_PERM_ID, containerId);
        final long datasetId = imagingDao.addAnalysisDataset(ds);

        ds.setId(datasetId);
        return ds;
    }

    @Test
    public void testInit()
    {
        // tests that parameter bindings in all queries are correct
    }

    // adding rows to tables

    @Test
    public void testCreateFeatureValues()
    {
        // Initialize the data set
        dataset = createAnalysisDataSet();

        createFeatureDef(dataset);
        List<ImgFeatureDefDTO> featureDefs = dao.listFeatureDefsByDataSetIds(dataset.getId());
        assertEquals(1, featureDefs.size());

        ImgFeatureDefDTO featureDef = featureDefs.get(0);
        assertEquals(TEST_FEATURE_LABEL, featureDef.getLabel());
        assertEquals(CodeNormalizer.normalize(TEST_FEATURE_LABEL), featureDef.getCode());

        testCreateAndListFeatureVocabularyValues(featureDef);

        createFeatureFloatValues(featureDef);
        List<ImgFeatureValuesDTO> featureValuesList = dao.getFeatureValues(featureDef.getId());
        assertEquals(1, featureValuesList.size());

        ImgFeatureValuesDTO featureValues = featureValuesList.get(0);
        assertEquals(0.0, featureValues.getT());
        assertEquals(0.0, featureValues.getZ());

        PlateFeatureValues spreadsheet = featureValues.getValues();
        assertEquals(2, spreadsheet.getGeometry().getNumberOfRows());
        assertEquals(3, spreadsheet.getGeometry().getNumberOfColumns());

        for (int row = 1; row <= 2; ++row)
        {
            for (int col = 1; col <= 3; ++col)
            {
                assertEquals((float) (row + col), spreadsheet.getForWellLocation(row, col));
            }
        }
    }

    private long createFeatureFloatValues(ImgFeatureDefDTO featureDef)
    {
        final PlateFeatureValues values =
                new PlateFeatureValues(Geometry.createFromRowColDimensions(2, 3));
        for (int row = 1; row <= 2; ++row)
        {
            for (int col = 1; col <= 3; ++col)
            {
                values.setForWellLocation(row + col, row, col);
            }
        }
        final ImgFeatureValuesDTO featureValues =
                new ImgFeatureValuesDTO(0.0, 0.0, values, featureDef.getId());
        return dao.addFeatureValues(featureValues);
    }

    private void testCreateAndListFeatureVocabularyValues(ImgFeatureDefDTO featureDef)
    {
        String yesCode = "YES";
        String noCode = "NO";

        List<ImgFeatureVocabularyTermDTO> availableTerms =
                new ArrayList<ImgFeatureVocabularyTermDTO>();
        availableTerms.add(new ImgFeatureVocabularyTermDTO(yesCode, 0, featureDef.getId()));
        availableTerms.add(new ImgFeatureVocabularyTermDTO(noCode, 1, featureDef.getId()));
        dao.addFeatureVocabularyTerms(availableTerms);

        List<ImgFeatureVocabularyTermDTO> listedTerms =
                dao.listFeatureVocabularyTermsByDataSetId(featureDef.getDataSetId());
        assertEquals(availableTerms.size(), listedTerms.size());

        int yesIx = (listedTerms.get(0).getCode().equals(yesCode)) ? 0 : 1;

        ImgFeatureVocabularyTermDTO yesTerm = listedTerms.get(yesIx);
        assertEquals(yesCode, yesTerm.getCode());
        assertEquals(0, yesTerm.getSequenceNumber());
        assertEquals(featureDef.getId(), yesTerm.getFeatureDefId());

        ImgFeatureVocabularyTermDTO noTerm = listedTerms.get(1 - yesIx);
        assertEquals(noCode, noTerm.getCode());
        assertEquals(1, noTerm.getSequenceNumber());
        assertEquals(featureDef.getId(), noTerm.getFeatureDefId());
    }

    private long createFeatureDef(ImgAnalysisDatasetDTO dataSet)
    {
        // Attach a feature def to it
        ImgFeatureDefDTO featureDef =
                new ImgFeatureDefDTO(TEST_FEATURE_LABEL,
                        CodeNormalizer.normalize(TEST_FEATURE_LABEL), "Test", dataSet.getId());
        return dao.addFeatureDef(featureDef);
    }
}
