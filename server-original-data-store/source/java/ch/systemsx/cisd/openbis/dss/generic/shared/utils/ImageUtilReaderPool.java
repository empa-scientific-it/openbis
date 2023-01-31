/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Jakub Straszewski
 */
public class ImageUtilReaderPool<T>
{
    public interface ReaderUtil<T>
    {
        T create(String imageLibraryNameOrNull,
                String imageLibraryReaderNameOrNull);

        boolean isSameLibraryAndReader(T reader, String imageLibraryNameOrNull, String imageLibraryReaderNameOrNull);

        void close(T reader);
    }

    private ReaderUtil<T> util;

    HashMap<String, List<T>> poolMap = new HashMap<>();

    public ImageUtilReaderPool(ReaderUtil<T> util)
    {
        this.util = util;
    }

    private List<T> getReadersList(String sessionId)
    {
        if (poolMap.containsKey(sessionId) == false)
        {
            poolMap.put(sessionId, new ArrayList<T>());
        }
        return poolMap.get(sessionId);
    }

    private T findMatchingReader(List<T> readersList, String imageLibraryNameOrNull,
            String imageLibraryReaderNameOrNull)
    {
        for (T reader : readersList)
        {
            if (util.isSameLibraryAndReader(reader, imageLibraryNameOrNull, imageLibraryReaderNameOrNull))
            {
                return reader;
            }
        }
        return null;
    }

    /**
     * Returns the reader to the pool, so that it can be reused in the specified session
     */
    public synchronized void put(String sessionId, T reader)
    {
        List<T> readersList = getReadersList(sessionId);
        readersList.add(reader);
    }

    /**
     * Gets readers from the pool for specified session, or creates a new one.
     */
    public synchronized T get(String sessionId, String imageLibraryNameOrNull,
            String imageLibraryReaderNameOrNull)
    {
        List<T> readersList = getReadersList(sessionId);
        T selectedReader = findMatchingReader(readersList, imageLibraryNameOrNull, imageLibraryReaderNameOrNull);
        if (selectedReader != null)
        {
            readersList.remove(selectedReader);
            return selectedReader;
        }
        else
        {
            return util.create(imageLibraryNameOrNull, imageLibraryReaderNameOrNull);
        }
    }

    /**
     * Release all readers that have been allocated during specified session
     */
    public synchronized void releaseSession(String sessionId)
    {
        if (poolMap.containsKey(sessionId))
        {
            List<T> list = poolMap.get(sessionId);
            for (T reader : list)
            {
                util.close(reader);
            }
            poolMap.remove(sessionId);
        }
    }
}
