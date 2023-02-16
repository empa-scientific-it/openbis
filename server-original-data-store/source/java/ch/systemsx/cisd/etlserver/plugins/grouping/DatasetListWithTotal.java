/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.etlserver.plugins.grouping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;

/**
 * Storage for grouped archival candidates
 * 
 * @author Sascha Fedorenko
 */
public class DatasetListWithTotal implements Iterable<AbstractExternalData>, Comparable<DatasetListWithTotal>
{
    private long dataSize = 0;

    private List<AbstractExternalData> list = new ArrayList<AbstractExternalData>();

    public boolean add(AbstractExternalData e)
    {
        addSize(e.getSize());
        list.add(e);
        return true;
    }

    public long getCumulatedSize()
    {
        return dataSize;
    }

    private void addSize(long addon)
    {
        this.dataSize += addon;
    }

    public List<AbstractExternalData> getList()
    {
        return Collections.unmodifiableList(list);
    }

    @Override
    public int compareTo(DatasetListWithTotal arg0)
    {
        return Long.signum(dataSize - arg0.dataSize);
    }

    @Override
    public Iterator<AbstractExternalData> iterator()
    {
        return java.util.Collections.unmodifiableList(list).iterator();
    }

    /**
     * Sorts the underlying list according to the given comparator
     */
    public void sort(Comparator<AbstractExternalData> comparator)
    {
        Collections.sort(list, comparator);
    }

    @Override
    public String toString()
    {
        return CollectionUtils.collect(list, new Transformer<AbstractExternalData, String>()
            {
                @Override
                public String transform(AbstractExternalData input)
                {
                    return input.getCode();
                }
            }).toString();
    }
}