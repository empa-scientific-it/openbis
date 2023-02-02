/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.ethz.sis.filetransfer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pkupczyk
 */
public class FailureGenerator
{

    private Collection<String> operations;

    private int failures;

    private int successes;

    private boolean retriable;

    // keep a separate counters for each download thread
    private ThreadLocal<Map<String, Integer>> counter = new ThreadLocal<Map<String, Integer>>();

    public FailureGenerator(Collection<String> operations, int failures, int successes, boolean retriable)
    {
        this.operations = operations;
        this.failures = failures;
        this.successes = successes;
        this.retriable = retriable;
    }

    public synchronized void maybeFail(String operation) throws DownloadException
    {
        if (operations.contains(operation))
        {
            Map<String, Integer> values = counter.get();

            if (values == null)
            {
                values = new HashMap<String, Integer>();
                counter.set(values);
            }

            Integer value = values.get(operation);

            if (value == null)
            {
                value = -1;
            }

            values.put(operation, ++value);

            // failures first then successes then again and again...

            if (value % (failures + successes) < failures)
            {
                throw new DownloadException("Intentional failure for testing purposes", retriable);
            }
        }
    }

}
