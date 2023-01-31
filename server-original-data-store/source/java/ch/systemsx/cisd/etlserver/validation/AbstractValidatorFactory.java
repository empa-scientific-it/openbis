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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractValidatorFactory implements IValidatorFactory
{
    static final String ALLOW_EMPTY_VALUES_KEY = "allow-empty-values";

    static final String EMPTY_VALUE_SYNONYMS_KEY = "empty-value-synonyms";

    protected final boolean allowEmptyValues;

    protected final Set<String> emptyValueSynonyms;

    AbstractValidatorFactory(Properties properties)
    {
        allowEmptyValues = PropertyUtils.getBoolean(properties, ALLOW_EMPTY_VALUES_KEY, false);
        String property = properties.getProperty(EMPTY_VALUE_SYNONYMS_KEY);
        if (property == null)
        {
            emptyValueSynonyms = Collections.emptySet();
        } else
        {
            String[] synonyms =
                    PropertyParametersUtil
                            .parseItemisedProperty(property, EMPTY_VALUE_SYNONYMS_KEY);
            emptyValueSynonyms = new LinkedHashSet<String>(Arrays.asList(synonyms));
        }
    }

}
