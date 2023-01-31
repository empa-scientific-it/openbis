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

import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class MutableV1Test extends MutableTest
{
    // for jython script go to
    // sourceTest/core-plugins/generic-test/1/dss/drop-boxes/mutable-v1-test/mutable-v1-test.py

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-mutable-v1-test");
    }

    @Override
    @Test
    public void testMutable() throws Exception
    {
        super.testMutable();
    }

}
