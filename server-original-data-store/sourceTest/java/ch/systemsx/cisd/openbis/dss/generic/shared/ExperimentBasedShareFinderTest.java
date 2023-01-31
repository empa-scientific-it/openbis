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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentBasedShareFinderTest extends AbstractIShareFinderTestCase
{
    private IShareFinder finder = new ExperimentBasedShareFinder(new Properties());

    private List<Share> shares;

    @BeforeMethod
    public void setUp()
    {
        Share share1 = extensionShare("1", 100 * FileUtils.ONE_KB);
        share1.setExperimentIdentifiers(new HashSet<String>(Arrays.asList("/S1/P1/EXP1", "/S1/P2/EXP1")));
        Share share2 = extensionShare("2", 200 * FileUtils.ONE_KB);
        share2.setExperimentIdentifiers(new HashSet<String>(Arrays.asList("/S1/P1/EXP2")));
        Share share3 = extensionShare("3", 300 * FileUtils.ONE_KB);
        share3.setExperimentIdentifiers(new HashSet<String>());
        shares = Arrays.asList(share1, share2, share3);
    }

    @Test
    public void testNoMatchingExperiments()
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setSpaceCode("S1");
        dataSet.setProjectCode("P1");
        dataSet.setExperimentCode("EXP3");
        dataSet.setDataSetSize(42 * FileUtils.ONE_KB);

        Share share = finder.tryToFindShare(dataSet, shares);

        assertEquals(null, share);
    }

    @Test
    public void testMatchingExperimentsButNotEnoughMemory()
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setSpaceCode("S1");
        dataSet.setProjectCode("P1");
        dataSet.setExperimentCode("EXP1");
        dataSet.setDataSetSize(142 * FileUtils.ONE_KB);

        Share share = finder.tryToFindShare(dataSet, shares);

        assertEquals(null, share);
    }

    @Test
    public void testMatchingExperimentsAndEnoughMemory()
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setSpaceCode("S1");
        dataSet.setProjectCode("P2");
        dataSet.setExperimentCode("EXP1");
        dataSet.setDataSetSize(42 * FileUtils.ONE_KB);

        Share share = finder.tryToFindShare(dataSet, shares);

        assertEquals("1", share.getShareId());
    }

}
