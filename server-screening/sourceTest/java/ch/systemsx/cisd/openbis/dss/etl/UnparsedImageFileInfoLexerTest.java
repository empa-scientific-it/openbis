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

package ch.systemsx.cisd.openbis.dss.etl;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.dss.etl.UnparsedImageFileInfoLexer;
import ch.systemsx.cisd.openbis.dss.etl.dto.UnparsedImageFileInfo;

/**
 * Test cases for {@link UnparsedImageFileInfoLexer}.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = UnparsedImageFileInfoLexer.class)
public class UnparsedImageFileInfoLexerTest extends AssertJUnit
{
    @Test
    public void testExtractFileInfoCorrectFileName() throws Exception
    {
        UnparsedImageFileInfo info =
                UnparsedImageFileInfoLexer.extractImageFileInfo("bDZ01-1A_wD17_s3_z123_t321_cGFP");
        assertEquals("well location token", "D17", info.getWellLocationToken());
        assertEquals("channel token", "GFP", info.getChannelToken());
        assertEquals("tile location token", "3", info.getTileLocationToken());
        assertEquals("time point token", "321", info.getTimepointToken());
        assertEquals("depth token", "123", info.getDepthToken());
    }

    @Test
    public void testExtractFileInfoPartialFileName() throws Exception
    {
        UnparsedImageFileInfo info =
                UnparsedImageFileInfoLexer.extractImageFileInfo("bDZ01-1A_wD17_s3_z123_t321_cGFP");
        assertEquals("well location token", "D17", info.getWellLocationToken());
        assertEquals("channel token", "GFP", info.getChannelToken());
        assertEquals("tile location token", "3", info.getTileLocationToken());
        assertEquals("time point token", "321", info.getTimepointToken());
        assertEquals("depth token", "123", info.getDepthToken());
    }
}
