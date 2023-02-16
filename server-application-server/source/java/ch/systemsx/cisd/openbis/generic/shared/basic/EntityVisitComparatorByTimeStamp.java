/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.util.Comparator;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityVisit;

/**
 * Comparator between {@link EntityVisit} instances. Newer visit comes before older visit.
 *
 * @author Franz-Josef Elmer
 */
public class EntityVisitComparatorByTimeStamp implements Comparator<EntityVisit>
{
    @Override
    public int compare(EntityVisit o1, EntityVisit o2)
    {
        long t1 = o1.getTimeStamp();
        long t2 = o2.getTimeStamp();
        return t1 < t2 ? 1 : (t1 > t2 ? -1 : 0);
    }
}