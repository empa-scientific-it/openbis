/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.common.batch;

import ch.ethz.sis.openbis.generic.server.asapi.v3.context.IProgress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;

/**
 * @author pkupczyk
 */
public abstract class CollectionBatchProcessor<T>
{

    public abstract void process(T object);

    public abstract IProgress createProgress(T object, int objectIndex, int totalObjectCount);

    public CollectionBatchProcessor(IOperationContext context, CollectionBatch<? extends T> batch)
    {
        int objectIndex = batch.getFromObjectIndex() + 1;
        int totalObjectCount = batch.getTotalObjectCount();

        for (T object : batch.getObjects())
        {
            IProgress progress = createProgress(object, objectIndex, totalObjectCount);
            context.pushProgress(progress);
            process(object);
            context.popProgress();
            objectIndex++;
        }
    }

}
