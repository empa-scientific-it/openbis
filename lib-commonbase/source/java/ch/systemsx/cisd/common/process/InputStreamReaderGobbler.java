/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * This class uses an internal worker thread to constantly consume input from specified {@link InputStream}.
 * <p>
 * When it is finished, you can get the lines with {@link #getLines()}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class InputStreamReaderGobbler
{

    private final Semaphore waitForReadingFinishedSemaphore = new Semaphore(1);

    private final List<String> lines = new ArrayList<String>();

    public InputStreamReaderGobbler(final InputStream stream) throws InterruptedException
    {
        waitForReadingFinishedSemaphore.acquire();
        final Thread t = new Thread()
            {

                //
                // Thread
                //

                @Override
                public final void run()
                {
                    try
                    {
                        final BufferedReader reader =
                                new BufferedReader(new InputStreamReader(stream));
                        String line;
                        while ((line = reader.readLine()) != null)
                        {
                            lines.add(line);
                        }
                    } catch (IOException ex)
                    {
                        throw new EnvironmentFailureException("Couldn't gobble stream content", ex);
                    } finally
                    {
                        waitForReadingFinishedSemaphore.release();
                    }
                }
            };
        t.start();
    }

    public final List<String> getLines() throws InterruptedException
    {
        waitForReadingFinishedSemaphore.acquire();
        try
        {
            return lines;
        } finally
        {
            waitForReadingFinishedSemaphore.release();
        }
    }

}