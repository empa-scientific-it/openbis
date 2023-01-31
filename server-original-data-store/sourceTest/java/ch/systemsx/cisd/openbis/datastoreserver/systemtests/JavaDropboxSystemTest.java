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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;

/**
 * Test the java-based dropbox implementation
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JavaDropboxSystemTest extends SystemTestCase
{

    // for configuration go to sourceTest/core-plugins/generic-test/1/dss/drop-boxes/java-dropbox

    File emailDirectory = new File(new File(new File(workingDirectory, "SystemTests"), "dss-root"),
            "email");

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-java-dropbox");
    }

    private HashSet<String> preexistingDataSetCodes;

    private void preparePreexistingDataSetCodes()
    {
        preexistingDataSetCodes = new HashSet<String>();
        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        List<AbstractExternalData> dataSets = listAllYoungDataSets(openBISService);
        for (AbstractExternalData dto : dataSets)
        {
            preexistingDataSetCodes.add(dto.getCode());
        }
    }

    private LinkedList<String> newDataSetCodes()
    {
        LinkedList<String> newCodes = new LinkedList<String>();

        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        List<AbstractExternalData> dataSets = listAllYoungDataSets(openBISService);
        for (AbstractExternalData dto : dataSets)
        {
            String code = dto.getCode();

            if (false == preexistingDataSetCodes.contains(code))
            {
                newCodes.add(code);
            }
        }
        return newCodes;
    }

    private List<AbstractExternalData> listAllYoungDataSets(IEncapsulatedOpenBISService openBISService)
    {
        List<AbstractExternalData> dataSets =
                openBISService.listNewerDataSets(new TrackingDataSetCriteria(24));
        return dataSets;
    }

    @Test
    public void testJavaDropbox() throws Exception
    {
        preparePreexistingDataSetCodes();

        File exampleDataSet = new File(workingDirectory, "my-data");
        createExampleDataSet(exampleDataSet);
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported();

        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        List<String> codes = newDataSetCodes();

        List<AbstractExternalData> x = openBISService.listDataSetsByCode(codes);

        assertEquals("Exactly one dataset should have been imported.", 1, x.size());
        assertEquals("/CISD/PLATE_WELLSEARCH:DP1-A", x.get(0).getSampleIdentifier());

        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "PLATE_WELLSEARCH"));
        List<Sample> samples = openBISService.searchForSamples(searchCriteria);
        assertEquals("[/CISD/DEFAULT/PLATE_WELLSEARCH]", extractIdentifiers(samples).toString());
        List<Sample> components = openBISService.listSamples(ListSampleCriteria.createForContainer(new TechId(samples.get(0))));
        assertEquals("[/CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A01, /CISD/DEFAULT/PLATE_WELLSEARCH:WELL-A02, /CISD/PLATE_WELLSEARCH:DP1-A]",
                extractIdentifiers(components).toString());
    }

    private List<String> extractIdentifiers(List<Sample> samples)
    {
        List<String> identifiers = new ArrayList<>();
        for (Sample sample : samples)
        {
            identifiers.add(sample.getIdentifier());
        }
        Collections.sort(identifiers);
        return identifiers;
    }

    private void createExampleDataSet(File exampleDataSet)
    {
        exampleDataSet.mkdirs();
    }

}
