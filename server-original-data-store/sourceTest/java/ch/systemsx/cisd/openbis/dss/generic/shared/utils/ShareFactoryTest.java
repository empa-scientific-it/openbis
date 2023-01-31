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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share.ShufflePriority;

/**
 * @author Kaloyan Enimanev
 */
public class ShareFactoryTest extends AssertJUnit
{
    private final static File DATA_DIRECTORY = new File("../datastore_server/resource/test-data/"
            + ShareFactoryTest.class.getSimpleName());

    @Test
    public void testSharePropertiesOverridesSpeedFile()
    {
        Share share = readShare("share-1");
        assertEquals(70, share.getSpeed());
        assertEquals(ShufflePriority.SPEED, share.getShufflePriority());
        assertEquals(false, share.isWithdrawShare());
        assertEquals(false, share.isIgnoredForShuffling());
    }

    @Test
    public void testSpeedFileRespected()
    {
        Share share = readShare("share-2");
        assertEquals(12, share.getSpeed());
        assertEquals(ShufflePriority.MOVE_TO_EXTENSION, share.getShufflePriority());
        assertEquals(true, share.isWithdrawShare());
        assertEquals(true, share.isIgnoredForShuffling());
        assertEquals(0, share.getExperimentIdentifiers().size());
    }

    @Test
    public void testOnlySharePropertiesAvailableAndExperiments()
    {
        Share share = readShare("share-3");
        assertEquals(70, share.getSpeed());
        assertEquals(ShufflePriority.SPEED, share.getShufflePriority());
        assertEquals(false, share.isWithdrawShare());
        List<String> experiments = new ArrayList<String>(share.getExperimentIdentifiers());
        Collections.sort(experiments);
        assertEquals("[/SPACE1/PROJECT1/EXP1, /SPACE1/PROJECT2/EXP1]", experiments.toString());
    }

    @Test
    public void testOnlySpeedFileAvailable()
    {
        Share share = readShare("share-4");
        assertEquals(12, share.getSpeed());
        assertEquals(ShufflePriority.SPEED, share.getShufflePriority());
        assertEquals(false, share.isWithdrawShare());
        assertEquals(false, share.isIgnoredForShuffling());
        assertEquals(0, share.getExperimentIdentifiers().size());
    }

    private Share readShare(String shareName)
    {
        File shareRoot = new File(DATA_DIRECTORY, shareName);
        return new ShareFactory().createShare(shareRoot, null, null);
    }
}
