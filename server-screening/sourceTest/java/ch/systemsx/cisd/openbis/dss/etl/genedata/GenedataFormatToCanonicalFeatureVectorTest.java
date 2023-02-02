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
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CanonicalFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;

/**
 * Check that Genedata feature vectors can be converted to the canonical form.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class GenedataFormatToCanonicalFeatureVectorTest extends AbstractFileSystemTestCase
{
    private static final String EXAMPLE1 =
            "barcode = Plate_042" + "\n\n<Layer=alpha>\n" + "\t1\t2\n" + "A\t4.5\t4.6\n"
                    + "B\t3.5\t5.6\n" + "C\t3.3\t5.7\n" + "\n\n<Layer=beta>\n" + "\t1\t2\n"
                    + "A\t14.5\t14.6\n" + "B\t13.5\t15.6\n" + "C\t13.3\t15.7\n";

    @Test
    public void test()
    {
        File incomingDir = new File(workingDirectory, "incoming");
        incomingDir.mkdirs();
        File dataSetFile = new File(incomingDir, "Plate042.stat");
        FileUtilities.writeToFile(dataSetFile, EXAMPLE1);
        List<String> lines = FileUtilities.loadToStringList(dataSetFile);
        GenedataFormatToCanonicalFeatureVector converter =
                new GenedataFormatToCanonicalFeatureVector(lines, "<Layer=");
        ArrayList<CanonicalFeatureVector> features = converter.convert();

        assertEquals(2, features.size());

        float[] feature1Values =
        { 4.5f, 4.6f, 3.5f, 5.6f, 3.3f, 5.7f };
        verifyFeature(features.get(0), "alpha", feature1Values);
        float[] feature2Values =
        { 14.5f, 14.6f, 13.5f, 15.6f, 13.3f, 15.7f };
        verifyFeature(features.get(1), "beta", feature2Values);
    }

    private void verifyFeature(CanonicalFeatureVector feature, String featureName, float[] values)
    {
        ImgFeatureDefDTO featureDef = feature.getFeatureDef();
        assertEquals(featureName, featureDef.getLabel());
        List<ImgFeatureValuesDTO> featureValues = feature.getValues();
        assertEquals(1, featureValues.size());
        ImgFeatureValuesDTO featureValue = featureValues.get(0);
        assertEquals(0., featureValue.getT());
        assertEquals(0., featureValue.getZ());
        PlateFeatureValues array = featureValue.getValues();
        assertEquals(values[0], array.getForWellLocation(1, 1));
        assertEquals(values[1], array.getForWellLocation(1, 2));
        assertEquals(values[2], array.getForWellLocation(2, 1));
        assertEquals(values[3], array.getForWellLocation(2, 2));
        assertEquals(values[4], array.getForWellLocation(3, 1));
    }
}
