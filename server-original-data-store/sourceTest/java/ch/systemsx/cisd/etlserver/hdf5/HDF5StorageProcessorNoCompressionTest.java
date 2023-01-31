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

package ch.systemsx.cisd.etlserver.hdf5;

import java.util.Properties;

import org.testng.annotations.Test;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class HDF5StorageProcessorNoCompressionTest extends AbstractHDF5StorageProcessorTest
{

    public HDF5StorageProcessorNoCompressionTest()
    {
        super(createProperties());
    }

    private static Properties createProperties()
    {
        return new Properties();
    }

    @Override
    @Test
    public void testStoreData()
    {
        super.testStoreData();
    }

    @Override
    @Test
    public void testUnstoreData()
    {
        super.testUnstoreData();
    }

    @Override
    @Test
    public void testGetStoreRootDirectory()
    {
        super.testGetStoreRootDirectory();
    }

    @Override
    @Test(expectedExceptions =
    { AssertionError.class })
    public void testStoreDataNullValues()
    {
        super.testStoreDataNullValues();
    }

}
