/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InQueryScroller<I>
{
    private static final int POSTGRES_DRIVER_MAX_ARGS = 32767; // Uses a signed 2 bytes integer

    private List<I> inArguments;

    private int fromIndex;

    private int fixParamsSize;

    public InQueryScroller(Collection<I> inArguments, int fixParamsSize)
    {
        this.inArguments = new ArrayList<>(inArguments);
        this.fromIndex = 0;
        this.fixParamsSize = fixParamsSize;
    }

    public List<I> next()
    {
        if (fromIndex < inArguments.size())
        {
            int toIndex = fromIndex + POSTGRES_DRIVER_MAX_ARGS - fixParamsSize;
            if (toIndex > inArguments.size())
            {
                toIndex = inArguments.size();
            }

            List<I> partialInArguments = inArguments.subList(fromIndex, toIndex);
            fromIndex = toIndex;
            return partialInArguments;
        } else
        {
            return null;
        }
    }
}