/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator;

import static ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant.INTERNAL_NAMESPACE_PREFIX;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public class PrefixBasedNameTranslator implements INameTranslator
{
    private final String prefix;

    public PrefixBasedNameTranslator(String prefix)
    {
        this.prefix = prefix + "_";
    }

    private String translateInternal(String name)
    {
        return prefix + name;
    }

    private String translateBackInternal(String name)
    {
        return name.startsWith(prefix) ? name.substring(prefix.length()) : name;
    }
    
    /**
     * INTERNAL_NAMESPACE_PREFIX is checked because of the following cases following cases: 1. While parsing master data, for property types with dataType = CONTROLLEDVOCABULARY or
     * MATERIAL in which case the vocabulary or material attribute might start with $ (INTERNAL_NAMESPACE_PREFIX) 2. While parsing master data, for
     * property assignments where property type code might start with $ (INTERNAL_NAMESPACE_PREFIX) the prop. assignment element in the incoming xml
     * will start with $ if propertyTypeCode is managedInternally. 3. While parsing meta data, the property code will will start with $ if
     * propertyTypeCode is internalNamespac
     */
    @Override
    public String translate(String name)
    {
        assert StringUtils.isBlank(name) == false : "Prefix translation can only be done for non-null values";
        if (name.startsWith(INTERNAL_NAMESPACE_PREFIX))
        {
            return INTERNAL_NAMESPACE_PREFIX + translateInternal(name.substring(INTERNAL_NAMESPACE_PREFIX.length()));
        }
        else
        {
            return translateInternal(name);
        }
    }

    @Override
    public String translateBack(String name)
    {
        if (name.startsWith(INTERNAL_NAMESPACE_PREFIX))
        {
            return INTERNAL_NAMESPACE_PREFIX + translateBackInternal(name.substring(INTERNAL_NAMESPACE_PREFIX.length()));
        }
        return translateBackInternal(name);
    }
}
