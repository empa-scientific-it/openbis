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
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;

/**
 * Collection of {@link IValidatorFactory} instances. Which one is used will be selected by a regular expression the column header matches.
 * 
 * @author Franz-Josef Elmer
 */
public class HeaderBasedValueValidatorFactory implements IValidatorFactory
{
    private static final class HeaderPatternAndFactory
    {
        private final Pattern pattern;

        private final IValidatorFactory factory;

        HeaderPatternAndFactory(Pattern pattern, IValidatorFactory factory)
        {
            super();
            this.pattern = pattern;
            this.factory = factory;
        }
    }

    static final String HEADER_PATTERN_KEY = "header-pattern";

    static final String HEADER_TYPES_KEY = "header-types";

    private final List<HeaderPatternAndFactory> factories =
            new ArrayList<HeaderPatternAndFactory>();

    private final String headerMessage;

    public HeaderBasedValueValidatorFactory(Properties properties)
    {
        SectionProperties[] columnsProperties =
                PropertyParametersUtil
                        .extractSectionProperties(properties, HEADER_TYPES_KEY, false);
        StringBuilder builder = new StringBuilder();
        for (SectionProperties sectionProperties : columnsProperties)
        {
            String key = sectionProperties.getKey();
            Properties props = sectionProperties.getProperties();
            String pattern = PropertyUtils.getMandatoryProperty(props, HEADER_PATTERN_KEY);
            builder.append('\n').append("Regular expression for headers of type '").append(key);
            builder.append("' have to match: ").append(pattern);
            try
            {
                factories.add(new HeaderPatternAndFactory(Pattern.compile(pattern),
                        ColumnDefinition.createValidatorFactory(props)));
            } catch (Exception ex)
            {
                throw new ConfigurationFailureException("Invalid header pattern for header type '"
                        + key + "': " + pattern);
            }
        }
        headerMessage = builder.toString();
    }

    @Override
    public IValidator createValidator(String columnHeader)
    {
        for (HeaderPatternAndFactory factory : factories)
        {
            if (factory.pattern.matcher(columnHeader).matches())
            {
                return factory.factory.createValidator(columnHeader);
            }
        }
        throw new UserFailureException("No value validator found for header '" + columnHeader
                + "': " + headerMessage);
    }

}
