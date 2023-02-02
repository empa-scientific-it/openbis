/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.collection;

import java.util.Comparator;

/**
 * The shortcut class to implement simple comparator. Instead of implementing a comparator with method
 * 
 * <pre>
 * {@code
 * public int compare(Data d1, Data d2)
 *    {
 *        return d1.getDifficultMethod().getComparableValue().compareTo(
 *            d2.getDifficultMethod().getComparableValue()
 *        );
 *    }
 * }
 * </pre>
 * 
 * you can now implement a {@code SimpleComparator} with a method
 * 
 * <pre>
 * {@code
 * public SomeComparable evaluate(Data data)
 *    {
 *        return data.getDifficultMethod().getComparableValue();
 *    }
 * }
 * </pre>
 * 
 * @author Jakub Straszewski
 */
@SuppressWarnings("javadoc")
public abstract class SimpleComparator<T, V extends Comparable<V>> implements Comparator<T>
{
    public abstract V evaluate(T item);

    @Override
    public int compare(T t1, T t2)
    {
        V v1 = evaluate(t1);
        V v2 = evaluate(t2);
        return v1.compareTo(v2);
    }

}
