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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.etl.featurevector.CanonicalFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureVocabularyTermDTO;

/**
 * Test of {@link FeatureDefinition}
 * 
 * @author Tomasz Pylak
 */
public class FeatureDefinitionValuesTest extends AssertJUnit
{
    @Test
    public void test()
    {
        FeatureDefinition def = new FeatureDefinition(new ImgFeatureDefDTO());
        def.addValue("A1", "1.2");
        def.addValue("B2", "2.1");
        CanonicalFeatureVector vector =
                def.getCanonicalFeatureVector(Geometry.createFromRowColDimensions(2, 2));
        assertNull(vector.getVocabularyTerms());
        assertEquals(1, vector.getValues().size());
        ImgFeatureValuesDTO featureValuesDTO = vector.getValues().get(0);
        assertNull(featureValuesDTO.getT());
        assertNull(featureValuesDTO.getZ());
        PlateFeatureValues values = featureValuesDTO.getValues();
        assertEquals(1.2f, values.getForWellLocation(1, 1));
        assertEquals(2.1f, values.getForWellLocation(2, 2));
        assertTrue(Float.isNaN(values.getForWellLocation(2, 1)));
        assertTrue(Float.isNaN(values.getForWellLocation(1, 2)));
    }

    @Test
    public void testTimepointsAndDepthScans()
    {
        FeatureDefinition def = new FeatureDefinition(new ImgFeatureDefDTO());
        def.changeSeries(1.1, null);
        def.addValue("A1", "1");
        def.changeSeries(null, 2.2);
        def.addValue("A1", "10");

        CanonicalFeatureVector vector =
                def.getCanonicalFeatureVector(Geometry.createFromRowColDimensions(1, 2));
        assertNull(vector.getVocabularyTerms());
        assertEquals(2, vector.getValues().size());

        ImgFeatureValuesDTO featureValuesDTO = vector.getValues().get(0);
        assertEquals(1.1d, featureValuesDTO.getT());
        assertNull(featureValuesDTO.getZ());
        PlateFeatureValues values = featureValuesDTO.getValues();
        assertEquals(1f, values.getForWellLocation(1, 1));
        assertTrue(Float.isNaN(values.getForWellLocation(1, 2)));

        featureValuesDTO = vector.getValues().get(1);
        assertEquals(2.2d, featureValuesDTO.getZ());
        assertNull(featureValuesDTO.getT());
        values = featureValuesDTO.getValues();
        assertEquals(10f, values.getForWellLocation(1, 1));
        assertTrue(Float.isNaN(values.getForWellLocation(1, 2)));
    }

    @Test
    public void testVocabularyFeatures()
    {
        FeatureDefinition def = new FeatureDefinition(new ImgFeatureDefDTO());
        def.addValue("A1", "a");
        def.addValue("A2", "b");
        def.addValue("A3", "a");
        CanonicalFeatureVector vector =
                def.getCanonicalFeatureVector(Geometry.createFromRowColDimensions(1, 3));
        List<ImgFeatureVocabularyTermDTO> terms = vector.getVocabularyTerms();
        assertNotNull(terms);
        assertEquals(2, terms.size());
        int termIxA = terms.get(0).getCode().equals("a") ? 0 : 1;

        assertEquals("a", terms.get(termIxA).getCode());
        assertEquals("b", terms.get(1 - termIxA).getCode());

        assertEquals(1, vector.getValues().size());
        ImgFeatureValuesDTO featureValuesDTO = vector.getValues().get(0);
        assertNull(featureValuesDTO.getT());
        assertNull(featureValuesDTO.getZ());
        PlateFeatureValues values = featureValuesDTO.getValues();
        assertEquals(terms.get(termIxA).getSequenceNumber(), (int) values.getForWellLocation(1, 1));
        assertEquals(terms.get(1 - termIxA).getSequenceNumber(),
                (int) values.getForWellLocation(1, 2));
        assertEquals(terms.get(termIxA).getSequenceNumber(), (int) values.getForWellLocation(1, 3));
    }

}