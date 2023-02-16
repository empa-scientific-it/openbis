/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.authorization.predicate.v3tov1;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.DeletionTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;

public class DeletionIdTranslator
{

    public static TechId translate(IDeletionId v3deletionId)
    {
        if (v3deletionId instanceof DeletionTechId)
        {
            return new TechId(((DeletionTechId) v3deletionId).getTechId());
        }
        return null;
    }

    public static List<TechId> translate(List<IDeletionId> values)
    {
        List<TechId> valuesAsTechIds = new ArrayList<TechId>();
        for (IDeletionId value : values)
        {
            valuesAsTechIds.add(translate(value));
        }
        return valuesAsTechIds;
    }
}
