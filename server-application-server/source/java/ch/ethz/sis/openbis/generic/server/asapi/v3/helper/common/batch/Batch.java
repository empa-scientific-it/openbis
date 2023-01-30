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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author pkupczyk
 */
public abstract class Batch<B>
{

    public static final int DEFAULT_BATCH_SIZE = 1000;

    private int batchIndex;

    private int fromObjectIndex;

    private int toObjectIndex;

    private B objects;

    private int totalObjectCount;

    public Batch(int batchIndex, int fromObjectIndex, int toObjectIndex, B objects, int totalObjectCount)
    {
        this.batchIndex = batchIndex;
        this.fromObjectIndex = fromObjectIndex;
        this.toObjectIndex = toObjectIndex;
        this.objects = objects;
        this.totalObjectCount = totalObjectCount;
    }

    public int getBatchIndex()
    {
        return batchIndex;
    }

    public int getFromObjectIndex()
    {
        return fromObjectIndex;
    }

    public int getToObjectIndex()
    {
        return toObjectIndex;
    }

    public B getObjects()
    {
        return objects;
    }

    public int getTotalObjectCount()
    {
        return totalObjectCount;
    }

    public static <T> Collection<CollectionBatch<T>> createBatches(Collection<T> objects)
    {
        if (objects == null || objects.isEmpty())
        {
            return Collections.emptyList();
        }

        Collection<CollectionBatch<T>> batches = new LinkedList<CollectionBatch<T>>();
        Iterator<T> iterator = objects.iterator();
        int batchIndex = 0;
        int batchSize = DEFAULT_BATCH_SIZE;

        for (int batchStart = 0; batchStart < objects.size(); batchStart += batchSize)
        {
            int batchFinish = Math.min(batchStart + batchSize, objects.size());
            Collection<T> batchObjects = new ArrayList<T>(batchSize);

            for (int objectIndex = batchStart; objectIndex < batchFinish; objectIndex++)
            {
                batchObjects.add(iterator.next());
            }

            CollectionBatch<T> batch =
                    new CollectionBatch<T>(batchIndex, batchStart, batchFinish, batchObjects, objects.size());

            batches.add(batch);
            batchIndex++;
        }

        return batches;
    }

    public static <K, V> Collection<MapBatch<K, V>> createBatches(Map<K, V> objects)
    {
        if (objects == null || objects.isEmpty())
        {
            return Collections.emptyList();
        }

        Collection<MapBatch<K, V>> batches = new LinkedList<MapBatch<K, V>>();
        Iterator<Map.Entry<K, V>> iterator = objects.entrySet().iterator();
        int batchIndex = 0;
        int batchSize = DEFAULT_BATCH_SIZE;

        for (int batchStart = 0; batchStart < objects.size(); batchStart += batchSize)
        {
            int batchFinish = Math.min(batchStart + batchSize, objects.size());
            Map<K, V> batchObjects = new HashMap<K, V>(batchSize);

            for (int objectIndex = batchStart; objectIndex < batchFinish; objectIndex++)
            {
                Map.Entry<K, V> entry = iterator.next();
                batchObjects.put(entry.getKey(), entry.getValue());
            }

            MapBatch<K, V> batch =
                    new MapBatch<K, V>(batchIndex, batchStart, batchFinish, batchObjects, objects.size());

            batches.add(batch);
            batchIndex++;
        }

        return batches;
    }

}
