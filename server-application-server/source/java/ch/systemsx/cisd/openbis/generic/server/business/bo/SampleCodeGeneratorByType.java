/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.Arrays;
import java.util.List;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.Select;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

public class SampleCodeGeneratorByType extends EntityCodeGenerator
{

    private final MaxQuery maxQuery;

    public static interface MaxQuery extends BaseQuery
    {
        @Select(sql = "SELECT max(substr(code, length(?{1})+1)::int) "
                + "FROM samples_all WHERE UPPER(code) similar to UPPER(?{1}) || '[1234567890]+'")
        public int getMaxCode(String prefix);
    }

    public SampleCodeGeneratorByType(IDAOFactory daoFactory)
    {
        super(daoFactory);
        this.maxQuery = QueryTool.getManagedQuery(MaxQuery.class);
    }

    @Override
    public List<String> generateCodes(String codePrefix, EntityKind entityKind, int numberOfCodes)
    {
        if (codePrefix == null)
        {
            throw new IllegalArgumentException("codePrefix can't be null.");
        }

        int maxCode = maxQuery.getMaxCode(codePrefix);
        final String[] codes = new String[numberOfCodes];
        for (int i = 0; i < numberOfCodes; i++)
        {
            String code;

            do
            {
                maxCode++;
                code = codePrefix + maxCode;
            } while (isCodeUsed(code, entityKind));

            codes[i] = code;
        }

        return Arrays.asList(codes);
    }

}
