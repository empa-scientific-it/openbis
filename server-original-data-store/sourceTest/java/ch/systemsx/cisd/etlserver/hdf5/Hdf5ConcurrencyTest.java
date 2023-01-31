/*
 * Copyright 2013 ETH Zuerich, CISD
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

import java.io.File;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.common.hdf5.HDF5Container;
import ch.systemsx.cisd.openbis.common.hdf5.IHDF5ContainerReader;

/**
 * A test to concurrently read entries from a HDF5 container. Created to reproduce problem SOB-121. To run the test, copy the file
 * /links/groups/cisd/sob-121/thumbnails.h5 to your local /tmp directory. The file is from dataset 20110826184451275-75145 from plate EZ03-1K, well
 * I15 from Biozentrum prod. The test is marked as "broken" so it will not be run as part of automatic test suites.
 * 
 * @author anttil
 */
public class Hdf5ConcurrencyTest
{

    public boolean stillRunning = true;

    @Test(groups = "broken")
    public void go() throws Exception
    {
        HDF5Container container = new HDF5Container(new File("/tmp/thumbnails.h5"));
        new Thread(new EntryReader(container, "/bEZ03-1K_wA01_s1_z1_t1_cCy3_u001.png")).start();
        Thread thread = new Thread(new EntryReader(container, "/bEZ03-1K_wA01_s1_z1_t1_cCy3_u001.png"));
        thread.start();
        thread.join();
    }

    private class EntryReader implements Runnable
    {
        private final String path;

        private HDF5Container container;

        public EntryReader(HDF5Container container, String path)
        {
            this.container = container;
            this.path = path;
        }

        @Override
        public void run()
        {
            write("Starting");
            while (stillRunning)
            {
                IHDF5ContainerReader reader = container.createSimpleReader();
                try
                {
                    reader.tryGetEntry(path);
                    write("Successful read");
                } catch (Exception e)
                {
                    write(e.getClass().getName() + ": " + e.getMessage());
                    if (e.getMessage().contains("H5Gloc.c line 195"))
                    {
                        e.printStackTrace();
                        stillRunning = false;
                    }
                } finally
                {
                    reader.close();
                }
            }
        }

        private void write(String text)
        {
            System.out.println(Thread.currentThread().getName() + "  " + text);
        }
    }
}
