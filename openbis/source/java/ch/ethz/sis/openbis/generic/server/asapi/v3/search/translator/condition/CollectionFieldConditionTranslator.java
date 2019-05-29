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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CollectionFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;

import java.util.List;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.IN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.SELECT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.UNNEST;

public class CollectionFieldConditionTranslator implements IConditionTranslator<CollectionFieldSearchCriteria<?>>
{

    @Override
    public void translate(final CollectionFieldSearchCriteria<?> criterion,
            final EntityKind entityKind, final List<Object> args,
            final StringBuilder sqlBuilder)
    {
        final Object fieldName = criterion.getFieldName();

        sqlBuilder.append(fieldName).append(SP).append(IN).append(SP).append(LP).
                append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP).
                append(RP);
    }

}
