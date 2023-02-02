/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.filesystem;

import org.testng.annotations.Listeners;

import ch.systemsx.cisd.common.test.TestReportCleaner;

/**
 * Test cases for the {@link RecursiveHardLinkMaker}.
 * 
 * @author Tomasz Pylak
 */
@Listeners(TestReportCleaner.class)
public class RecursiveHardLinkMakerTest extends AbstractHardlinkMakerTest
{
    @Override
    protected IImmutableCopier createHardLinkCopier()
    {
        IImmutableCopier copier = RecursiveHardLinkMaker.tryCreate(HardLinkMaker.tryCreate());
        assert copier != null;
        return copier;
    }

}
