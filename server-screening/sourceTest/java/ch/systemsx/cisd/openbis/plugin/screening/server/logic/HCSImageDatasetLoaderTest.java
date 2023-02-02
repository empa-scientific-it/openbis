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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataStoreBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * @author Franz-Josef Elmer
 */
public class HCSImageDatasetLoaderTest extends AbstractServerTestCase
{
    private static final String STORE_CODE = "store-1";

    private static final String DOWNLOAD_URL = "http://download";

    private static final String HOST_URL = "http://host";

    private IScreeningBusinessObjectFactory screeningBOFactory;

    @BeforeMethod
    public void beforeMethod()
    {
        screeningBOFactory = context.mock(IScreeningBusinessObjectFactory.class);
    }

    private DataSetBuilder dataSet(long id)
    {
        DataStoreBuilder dataStoreBuilder = new DataStoreBuilder(STORE_CODE);
        dataStoreBuilder.hostUrl(HOST_URL).downloadUrl(DOWNLOAD_URL);
        Experiment experiment = new ExperimentBuilder().identifier("/S/P/E1").getExperiment();
        return new DataSetBuilder(id).store(dataStoreBuilder.getStore()).experiment(experiment);
    }

    @Test
    public void testGetSegmentationImageDatasetReferences()
    {
        final RecordingMatcher<ListOrSearchSampleCriteria> recordingCriteriaMatcher =
                new RecordingMatcher<ListOrSearchSampleCriteria>();
        SampleBuilder sampleBuilder = new SampleBuilder("/S/P1").id(42l).permID("s-1");
        VocabularyTerm value = new VocabularyTerm();
        value.setCode("96_WELLS_8X12");
        sampleBuilder.property(ScreeningConstants.PLATE_GEOMETRY).value(value);
        final Sample sample = sampleBuilder.getSample();
        final AbstractExternalData ids1 =
                dataSet(1l).code("ids1").type("HCS_IMAGE").sample(sample).getDataSet();
        final AbstractExternalData sds1 =
                dataSet(11l).code("sds1").type("HCS_IMAGE_SEGMENTATION").sample(sample)
                        .getDataSet();
        final AbstractExternalData sds2 =
                dataSet(12l).code("sds2").type("HCS_IMAGE_SEGMENTATION").sample(sample)
                        .getDataSet();
        final AbstractExternalData ids2 =
                dataSet(2l).code("ids2").type("HCS_IMAGE").sample(sample).getDataSet();
        final AbstractExternalData sds3 =
                dataSet(21l).code("sds3").type("HCS_IMAGE_SEGMENTATION").sample(sample)
                        .getDataSet();
        final AbstractExternalData sds4 =
                dataSet(100l).code("sds4").type("HCS_IMAGE_SEGMENTATION").getDataSet();
        final AbstractExternalData ds1 =
                dataSet(101l).code("ds1").type("BLABLA").sample(sample).getDataSet();
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleLister(session);
                    will(returnValue(sampleLister));
                    allowing(screeningBOFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));

                    one(sampleLister).list(with(recordingCriteriaMatcher));
                    ArrayList<Sample> samples = new ArrayList<Sample>();
                    samples.add(sample);
                    will(returnValue(samples));

                    one(datasetLister).listBySampleIds(new HashSet<Long>(Arrays.asList(42l)));
                    will(returnValue(Arrays.asList(ids1, sds1, sds2, ids2, sds3, sds4, ds1)));

                    one(datasetLister).listByParentTechIds(Arrays.asList(1l, 2l));
                    will(returnValue(Arrays.asList(sds1, sds2, sds3, ds1)));

                    one(datasetLister).listParentIds(Arrays.asList(11l, 12l, 21l));
                    HashMap<Long, Set<Long>> map = new HashMap<Long, Set<Long>>();
                    map.put(11l, Collections.singleton(1l));
                    map.put(12l, Collections.singleton(1l));
                    map.put(21l, Collections.singleton(2l));
                    will(returnValue(map));
                }
            });
        Set<PlateIdentifier> plateIdentifiers =
                new HashSet<PlateIdentifier>(Arrays.<PlateIdentifier> asList(new PlateIdentifier(
                        "P1", "S", "s-1")));
        HCSImageDatasetLoader loader =
                new HCSImageDatasetLoader(session, screeningBOFactory, null, plateIdentifiers);

        List<ImageDatasetReference> references = loader.getSegmentationImageDatasetReferences();

        assertEquals("[]", Arrays
                .asList(recordingCriteriaMatcher.recordedObject().trySampleCodes()).toString());
        assertEquals("[s-1]",
                Arrays.asList(recordingCriteriaMatcher.recordedObject().trySamplePermIds())
                        .toString());
        Collections.sort(references, new Comparator<ImageDatasetReference>()
            {
                @Override
                public int compare(ImageDatasetReference o1, ImageDatasetReference o2)
                {
                    return o1.getDatasetCode().compareTo(o2.getDatasetCode());
                }
            });
        ImageDatasetReference ref0 = references.get(0);
        assertEquals("sds1 (plate: /S/P1 [s-1])", ref0.toString());
        assertEquals("ids1 (plate: /S/P1 [s-1])", ref0.getParentImageDatasetReference().toString());
        ImageDatasetReference ref1 = references.get(1);
        assertEquals("sds2 (plate: /S/P1 [s-1])", ref1.toString());
        assertEquals("ids1 (plate: /S/P1 [s-1])", ref1.getParentImageDatasetReference().toString());
        ImageDatasetReference ref2 = references.get(2);
        assertEquals("sds3 (plate: /S/P1 [s-1])", ref2.toString());
        assertEquals("ids2 (plate: /S/P1 [s-1])", ref2.getParentImageDatasetReference().toString());
        assertEquals(3, references.size());
        context.assertIsSatisfied();

    }

}
