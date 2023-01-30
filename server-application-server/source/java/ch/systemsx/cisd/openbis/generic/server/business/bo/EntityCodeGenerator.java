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
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

/**
 * Generates unique codes for openBIS entities. It uses database sequences as a base for the code generation. Moreover it verifies that the generated
 * codes are in fact unique (i.e. entities with such codes do not exist yet). If it finds that the new generated code is already used by an existing
 * entity (e.g. the entity code was manually entered by a user) then it regenerates the code until it is unique.
 * 
 * @author pkupczyk
 */
public class EntityCodeGenerator
{
    protected final IDAOFactory daoFactory;

    private final CountQuery countQuery;

    public static interface CountQuery extends BaseQuery
    {
        public final static String PREFIX = "SELECT count(*) FROM ";

        public final static String SUFFIX = " WHERE code = ?{1}";

        @Select(sql = PREFIX + TableNames.EXPERIMENTS_ALL_TABLE + SUFFIX)
        public int getExperimentCount(String code);

        @Select(sql = PREFIX + TableNames.SAMPLES_ALL_TABLE + SUFFIX)
        public int getSampleCount(String code);

        @Select(sql = PREFIX + TableNames.DATA_ALL_TABLE + SUFFIX)
        public int getDataSetCount(String code);

        @Select(sql = PREFIX + TableNames.MATERIALS_TABLE + SUFFIX)
        public int getMaterialsCount(String code);
    }

    public EntityCodeGenerator(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
        this.countQuery = QueryTool.getManagedQuery(CountQuery.class);
    }

    public String generateCode(String codePrefix, EntityKind entityKind)
    {
        return generateCodes(codePrefix, entityKind, 1).get(0);
    }

    /**
     * Generate unique codes for openBIS entities.
     * 
     * @param codePrefix Prefix for the generated codes (e.g. when "ABC-" then codes will be "ABC-1", "ABC-2", ...)
     * @param entityKind Kind of an entity the codes should be generated for (different kinds of entities use different database sequences).
     * @param numberOfCodes Number of codes to be generated.
     */
    public List<String> generateCodes(String codePrefix, EntityKind entityKind, int numberOfCodes)
    {
        final String[] codes = new String[numberOfCodes];
        for (int i = 0; i < numberOfCodes; i++)
        {
            long sequenceValue;
            String code;

            do
            {
                sequenceValue = daoFactory.getCodeSequenceDAO().getNextCodeSequenceId(entityKind);
                code = codePrefix + sequenceValue;

            } while (isCodeUsed(code, entityKind));

            codes[i] = code;
        }

        return Arrays.asList(codes);
    }

    protected boolean isCodeUsed(String code, EntityKind entityKind)
    {
        int count;

        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            count = countQuery.getExperimentCount(code);
        } else if (EntityKind.SAMPLE.equals(entityKind))
        {
            count = countQuery.getSampleCount(code);
        } else if (EntityKind.DATA_SET.equals(entityKind))
        {
            count = countQuery.getDataSetCount(code);
        } else if (EntityKind.MATERIAL.equals(entityKind))
        {
            count = countQuery.getMaterialsCount(code);
        } else
        {
            throw new IllegalArgumentException("Unsupported entity kind: " + entityKind);
        }

        return count > 0;
    }

}
