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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureVocabularyTermDTO;

/**
 * Tests of {@link FeatureVocabularyTermsMap}.
 * 
 * @author Tomasz Pylak
 */
public class FeatureVocabularyTermsMapTest extends AssertJUnit
{
    @Test
    public void testMap()
    {
        List<ImgFeatureVocabularyTermDTO> terms = new ArrayList<ImgFeatureVocabularyTermDTO>();
        int featureDefId1 = 0;
        int featureDefId2 = 1;
        int featureDefId3 = 2;
        terms.add(new ImgFeatureVocabularyTermDTO("YES", 0, featureDefId1));
        terms.add(new ImgFeatureVocabularyTermDTO("NO", 1, featureDefId1));
        terms.add(new ImgFeatureVocabularyTermDTO("A", 0, featureDefId2));
        terms.add(new ImgFeatureVocabularyTermDTO("B", 1, featureDefId2));
        terms.add(new ImgFeatureVocabularyTermDTO("C", 2, featureDefId2));
        terms.add(new ImgFeatureVocabularyTermDTO("SOMETHING", 0, featureDefId3));

        Set<ImgFeatureDefDTO> featureDefs = new HashSet<ImgFeatureDefDTO>();
        featureDefs.add(createFeatureDef(featureDefId1));
        featureDefs.add(createFeatureDef(featureDefId2));
        featureDefs.add(createFeatureDef(featureDefId3));

        FeatureVocabularyTermsMap termsMap =
                FeatureVocabularyTermsMap.createVocabularyTermsMap(terms, featureDefs);

        assertEquals("YES", termsMap.getVocabularyTerm(featureDefId1, 0).tryAsVocabularyTerm());
        assertEquals("B", termsMap.getVocabularyTerm(1, 1).tryAsVocabularyTerm());
        assertEquals("SOMETHING", termsMap.getVocabularyTerm(2, 0).tryAsVocabularyTerm());

        assertTrue(termsMap.hasVocabularyTerms(featureDefId1));

        int unexistingFeatureDefId = 123;
        assertFalse(termsMap.hasVocabularyTerms(unexistingFeatureDefId));
    }

    private static ImgFeatureDefDTO createFeatureDef(long id)
    {
        ImgFeatureDefDTO f = new ImgFeatureDefDTO("label" + id, "code" + id, "desc" + id, 0);
        f.setId(id);
        return f;
    }

    @Test(expectedExceptions = Throwable.class)
    public void testMapFails()
    {
        List<ImgFeatureVocabularyTermDTO> terms = new ArrayList<ImgFeatureVocabularyTermDTO>();
        Set<ImgFeatureDefDTO> featureDefs = Collections.emptySet();
        FeatureVocabularyTermsMap termsMap =
                FeatureVocabularyTermsMap.createVocabularyTermsMap(terms, featureDefs);
        termsMap.getVocabularyTerm(0, 0);
    }

}
