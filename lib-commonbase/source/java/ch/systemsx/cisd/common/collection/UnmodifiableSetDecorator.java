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

package ch.systemsx.cisd.common.collection;

import java.util.Collections;
import java.util.Set;

import ch.systemsx.cisd.common.collection.UnmodifiableCollectionDecorator;

/**
 * A decorator for a {@link Set} that does the same as {@link Collections#unmodifiableSet(Set)} but with a 'back-door'.
 *
 * @author Bernd Rinn
 */
public class UnmodifiableSetDecorator<E> extends UnmodifiableCollectionDecorator<E> implements Set<E>
{
    public UnmodifiableSetDecorator(Set<E> set)
    {
        super(set);
    }

    /**
     * Returns the decorated (wrapped) set.
     */
    @Override
    public Set<E> getDecorated()
    {
        return (Set<E>) super.getDecorated();
    }

}
