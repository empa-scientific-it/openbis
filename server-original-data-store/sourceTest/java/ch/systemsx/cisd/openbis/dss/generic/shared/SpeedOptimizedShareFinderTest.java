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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
public class SpeedOptimizedShareFinderTest extends AbstractIShareFinderTestCase
{
    private static final String DATA_SET_CODE = "ds-1";

    private SpeedOptimizedShareFinder finder;

    private Mockery context;

    private SimpleDataSetInformationDTO dataSet;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetCode(DATA_SET_CODE);
        dataSet.setDataSetSize(FileUtils.ONE_MB);
        finder = new SpeedOptimizedShareFinder(new Properties());
    }

    @AfterMethod
    public void tearDown(Method method)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(method.getName() + "() : ", t);
        }
    }

    @Test
    public void testFindMatchingExtensionShare()
    {
        dataSet.setSpeedHint(-50);
        Share s1 = extensionShare("s1", megaBytes(10), 50);
        Share s2 = incomingShare("s2", 0, 50);
        Share s3 = extensionShare("s3", 0, 40);
        Share s4 = extensionShare("s4", megaBytes(11), 50);

        Share foundShare = finder.tryToFindShare(dataSet, Arrays.asList(s1, s2, s3, s4));
        assertSame(s4.getShare(), foundShare.getShare());
    }

    @Test
    public void testFindExtensionShareRespectingSpeedHint()
    {
        dataSet.setSpeedHint(-50);
        Share s1 = extensionShare("s1", megaBytes(10), 49);
        Share s2 = incomingShare("s2", 0, 50);
        Share s3 = extensionShare("s3", megaBytes(11), 40);

        Share foundShare = finder.tryToFindShare(dataSet, Arrays.asList(s1, s2, s3));
        assertSame(s3.getShare(), foundShare.getShare());
    }

    @Test
    public void testFindShareIgnoringSpeedHint()
    {
        dataSet.setSpeedHint(-50);
        dataSet.setDataSetShareId("s2");

        Share s1 = extensionShare("s1", megaBytes(10), 51);
        Share s2 = incomingShare("s2", megaBytes(20), 50);
        Share s3 = extensionShare("s3", megaBytes(11), 60);
        Share s4 = incomingShare("s4", megaBytes(15), 50);

        Share foundShare = finder.tryToFindShare(dataSet, Arrays.asList(s1, s2, s3, s4));
        assertSame(s3.getShare(), foundShare.getShare());
    }
}
