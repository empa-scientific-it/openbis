/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.util.Collection;

/**
 * Interface defining the role of a key extractor when an entity can have multiple keys.
 * 
 * @author Franz-Josef Elmer
 */
public interface IMultiKeyExtractor<K, E>
{
    /**
     * Returns the keys of type <code>K</code> from an entity <code>E</code>.
     */
    public Collection<K> getKey(E e);
}
