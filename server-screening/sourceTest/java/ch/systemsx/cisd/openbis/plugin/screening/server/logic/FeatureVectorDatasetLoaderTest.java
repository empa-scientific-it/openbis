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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class FeatureVectorDatasetLoaderTest extends AbstractServerTestCase
{
    private IScreeningBusinessObjectFactory screeningBOFactory;

    @BeforeMethod
    public void beforeMethod()
    {
        screeningBOFactory = context.mock(IScreeningBusinessObjectFactory.class);
    }

    @Test
    public void testGetFeatureVectorDatasets()
    {
        final RecordingMatcher<ListOrSearchSampleCriteria> recordingCriteriaMatcher =
                new RecordingMatcher<ListOrSearchSampleCriteria>();
        final AbstractExternalData ids1 =
                new DataSetBuilder(1l).code("ids1").type("HCS_IMAGE").getDataSet();
        final AbstractExternalData fds1 =
                new DataSetBuilder(11l).code("fds1").type("HCS_ANALYSIS_WELL_FEATURES")
                        .getDataSet();
        final AbstractExternalData fds2 =
                new DataSetBuilder(12l).code("fds2").type("HCS_ANALYSIS_WELL_FEATURES")
                        .getDataSet();
        final AbstractExternalData ids2 =
                new DataSetBuilder(2l).code("ids2").type("HCS_IMAGE").getDataSet();
        final AbstractExternalData fds3 =
                new DataSetBuilder(21l).code("fds3").type("HCS_ANALYSIS_WELL_FEATURES")
                        .getDataSet();
        final AbstractExternalData fds4 =
                new DataSetBuilder(100l).code("fds4").type("HCS_ANALYSIS_WELL_FEATURES")
                        .getDataSet();
        final AbstractExternalData ds1 = new DataSetBuilder(101l).code("ds1").type("BLABLA").getDataSet();
        context.checking(new Expectations()
            {
                {
                    one(screeningBOFactory).createSampleLister(session);
                    will(returnValue(sampleLister));
                    allowing(screeningBOFactory).createDatasetLister(session);
                    will(returnValue(datasetLister));

                    one(sampleLister).list(with(recordingCriteriaMatcher));
                    ArrayList<Sample> samples = new ArrayList<Sample>();
                    samples.add(new SampleBuilder("/S/P1").id(42l).permID("s-1").getSample());
                    will(returnValue(samples));

                    one(datasetLister).listBySampleIds(new HashSet<Long>(Arrays.asList(42l)));
                    will(returnValue(Arrays.asList(ids1, fds1, fds2, ids2, fds3, fds4, ds1)));

                    exactly(2).of(datasetLister).listByParentTechIds(Arrays.asList(1l, 2l));
                    will(returnValue(Arrays.asList(fds1, fds2, fds3, ds1)));

                    exactly(2).of(datasetLister).listParentIds(Arrays.asList(11l, 12l, 21l));
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
        FeatureVectorDatasetLoader loader =
                new FeatureVectorDatasetLoader(session, screeningBOFactory, null, plateIdentifiers);

        List<AbstractExternalData> datasets =
                new ArrayList<AbstractExternalData>(loader.getFeatureVectorDatasets());

        assertEquals("[]", Arrays
                .asList(recordingCriteriaMatcher.recordedObject().trySampleCodes()).toString());
        assertEquals("[s-1]",
                Arrays.asList(recordingCriteriaMatcher.recordedObject().trySamplePermIds())
                        .toString());
        Collections.sort(datasets, new Comparator<AbstractExternalData>()
            {
                @Override
                public int compare(AbstractExternalData o1, AbstractExternalData o2)
                {
                    return o1.getCode().compareTo(o2.getCode());
                }
            });
        assertSame(fds1, datasets.get(0));
        assertSame(ids1, datasets.get(0).getParents().iterator().next());
        assertEquals(1, datasets.get(0).getParents().size());
        assertSame(fds2, datasets.get(1));
        assertSame(ids1, datasets.get(1).getParents().iterator().next());
        assertEquals(1, datasets.get(1).getParents().size());
        assertSame(fds3, datasets.get(2));
        assertSame(ids2, datasets.get(2).getParents().iterator().next());
        assertEquals(1, datasets.get(2).getParents().size());
        assertSame(fds4, datasets.get(3));
        assertEquals(null, datasets.get(3).getParents());
        assertEquals(4, datasets.size());
        context.assertIsSatisfied();
    }
}
