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

import java.io.File;

import org.testng.annotations.Listeners;

import ch.systemsx.cisd.common.test.TestReportCleaner;

/**
 * Test cases for the {@link FastRecursiveHardLinkMaker}.
 * <p>
 * More or less a duplicate of {@link RecursiveHardLinkMakerTest}.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@Listeners(TestReportCleaner.class)
public class FastRecursiveHardLinkMakerTest extends AbstractHardlinkMakerTest
{

    @Override
    protected TestBigStructureCreator createBigStructureCreator(File root)
    {
        int[] numberOfFolders =
                { 100, 10 };
        int[] numberOfFiles =
                { 1, 10, 10 };
        return new TestBigStructureCreator(root, numberOfFolders, numberOfFiles);
    }

    @Override
    protected IImmutableCopier createHardLinkCopier()
    {
        IImmutableCopier copier = FastRecursiveHardLinkMaker.tryCreate(null);
        assert copier != null;
        return copier;
    }
}
