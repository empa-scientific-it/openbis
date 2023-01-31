/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.util.List;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.SampleImmutable;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.SearchService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SearchServiceTest extends SystemTestCase
{
    private IEncapsulatedOpenBISService openBis;

    private SearchService searchService;

    @BeforeTest
    public void setUp()
    {
        openBis = ServiceProvider.getOpenBISService();
        searchService = new SearchService(openBis);
    }

    @Test
    public void testGettingContainedSamples()
    {
        List<ISampleImmutable> samples = searchService.searchForSamples("DESCRIPTION", "*", null);
        assertTrue("Should have found at least one sample.", samples.size() > 0);

        ISampleImmutable sampleFromSearch = samples.get(0);
        // SampleSearch should return samples without contained samples
        assertFalse(sampleFromSearch.canGetContainedSamples());
        try
        {
            sampleFromSearch.getContainedSamples();
            fail("getContainedSamples should have thrown an error");
        } catch (IllegalStateException e)
        {
            // This is the correct behavior
        }

        // Getting the same sample from the openBis directly should include contained
        ISampleImmutable sampleFromServer =
                new SampleImmutable(openBis.tryGetSampleWithExperiment(SampleIdentifierFactory
                        .parse(sampleFromSearch.getSampleIdentifier())));
        assertTrue(sampleFromServer.canGetContainedSamples());
        assertTrue(sampleFromServer.getContainedSamples().size() > 0);

    }
}
