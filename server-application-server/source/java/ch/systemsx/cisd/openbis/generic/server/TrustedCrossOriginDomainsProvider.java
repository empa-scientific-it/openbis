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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Makes the trusted cross origin domains accessible at runtime.
 * 
 * @author Kaloyan Enimanev
 */
public class TrustedCrossOriginDomainsProvider
{
    private static final String LIST_SEPARATOR = ",";

    private final List<String> trustedDomains;

    public TrustedCrossOriginDomainsProvider(String trustedPropertyValue)
    {
        if (trustedPropertyValue.startsWith("$"))
        {
            trustedDomains = Collections.emptyList();
        } else
        {
            trustedDomains = parseTrustedPropertyList(trustedPropertyValue);
        }
    }

    /**
     * Returns a list of configured trusted domains which can host external shared web resources. Typically these are lightweight webapps that
     * integrate with openBIS via JSON-RPC services.
     * <p>
     * Can return empty list.
     */
    public List<String> getTrustedDomains()
    {
        return Collections.unmodifiableList(trustedDomains);
    }

    private List<String> parseTrustedPropertyList(String trustedPropertyValue)
    {
        String[] items = trustedPropertyValue.split(LIST_SEPARATOR);
        for (int i = 0; i < items.length; i++)
        {
            items[i] = items[i].trim();
        }
        return Arrays.asList(items);
    }

}
