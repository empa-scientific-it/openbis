/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.validation;

import java.util.ArrayList;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;

/**
 * Creates {@link UniqueGroupValidator}.
 * 
 * @author Izabela Adamczyk
 */
class UniqueGroupValidatorFactory implements IValidatorFactory
{
    static final String GROUPS_KEY = "groups";

    private final String regex;

    private final ArrayList<Integer> groups;

    UniqueGroupValidatorFactory(Properties properties)
    {
        regex =
                PropertyUtils.getMandatoryProperty(properties,
                        StringValidatorFactory.VALUE_PATTERN_KEY);
        String groupsParameter = PropertyUtils.getMandatoryProperty(properties, GROUPS_KEY);
        String[] uncheckedGroups =
                PropertyParametersUtil.parseItemisedProperty(groupsParameter, GROUPS_KEY);
        groups = new ArrayList<Integer>();
        for (String g : uncheckedGroups)
        {
            int value;
            try
            {
                value = Integer.parseInt(g);
            } catch (NumberFormatException ex)
            {
                throw new ConfigurationFailureException(createIllegalGroupMessage(g));
            }
            if (value < UniqueGroupValidator.MIN_GROUP)
            {
                throw new ConfigurationFailureException(createIllegalGroupMessage(g));
            }
            groups.add(value);
        }
    }

    private String createIllegalGroupMessage(String g)
    {
        return "Illegal group: " + g;
    }

    @Override
    public IValidator createValidator(String header)
    {
        return new UniqueGroupValidator(regex, groups);
    }

}
