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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.view;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author pkupczyk
 */
public class SetView<T> extends AbstractCollectionView<T> implements Set<T>
{
    private static final long serialVersionUID = 1L;

    public SetView(Collection<T> originalCollection, Integer from, Integer count)
    {
        super(originalCollection, from, count);
    }

    @Override
    protected Collection<T> createLimited(Collection<T> originalCollection, Integer fromOrNull, Integer countOrNull)
    {
        Set<T> limited = new LinkedHashSet<T>();
        copyItems(originalCollection, limited, fromOrNull, countOrNull);
        return Collections.unmodifiableSet(limited);
    }

}
