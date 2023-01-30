/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;

/**
 * Business object handling table of {@link Deletion} objects.
 * 
 * @author Franz-Josef Elmer
 */
public interface IDeletionTable
{
    /**
     * Load all {@link Deletion} objects.
     * 
     * @param withEntities If <code>true</code> deletion objects will be enriched with those deleted entities which are the root of the deletion tree.
     */
    public void load(boolean withEntities);

    public void load(List<Long> deletionIds, boolean withEntities);

    /**
     * Load all {@link Deletion} objects with original entities.
     */
    public void loadOriginal();

    public void loadOriginal(List<Long> deletionIds);

    public List<Deletion> getDeletions();
}
