/*
 * Copyright 2008 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant.INTERNAL_NAMESPACE_PREFIX;
import static ch.systemsx.cisd.openbis.generic.shared.basic.TableCellUtil.INTERN_PREFIX;
import static ch.systemsx.cisd.openbis.generic.shared.basic.TableCellUtil.USER_PREFIX;

/**
 * Methods for converter codes from business layer to database and from database to business layer.
 * 
 * @author Bernd Rinn
 */
public final class CodeConverter
{
    private CodeConverter()
    {
        // Cannot be instantiated.
    }

    /**
     * Converts a code from database form to business layer form.
     * <p>
     * If <var>internalNamespace</var> is <code>true</code>, the prefix '$' will be used, because internal properties will be represented as 'NAME' in
     * the database and as $NAME in the business layer.
     * 
     * @return The code appropriate for the business layer.
     */
    public static String tryToBusinessLayer(final String codeFromDatabaseOrNull,
            final boolean internalNamespace)
    {
        if (internalNamespace && codeFromDatabaseOrNull != null)
        {
            return INTERNAL_NAMESPACE_PREFIX + codeFromDatabaseOrNull;
        } else
        {
            return codeFromDatabaseOrNull;
        }
    }

    /**
     * Converts a property type code from business layer form to database form.
     * <p>
     * The code will be translated to upper case. Internal properties will be represented as 'NAME' in the database and as $NAME in the business
     * layer.
     * 
     * @return The code appropriate for the database.
     */
    public static String tryToDatabase(final String codeFromBusinessLayerOrNull)
    {
        if (codeFromBusinessLayerOrNull == null)
        {
            return null;
        }
        final String upperCaseCode = codeFromBusinessLayerOrNull.toUpperCase();
        if (upperCaseCode.startsWith(INTERNAL_NAMESPACE_PREFIX))
        {
            return upperCaseCode.substring(INTERNAL_NAMESPACE_PREFIX.length());
        } else
        {
            return upperCaseCode;
        }
    }

    /**
     * Returns <code>true</code>, if the <var>codeFromBusinessLayerOrNull</var> represents a internal property code.
     */
    public static boolean isInternalNamespace(final String codeFromBusinessLayerOrNull)
    {
        if (codeFromBusinessLayerOrNull == null)
        {
            return false;
        } else
        {
            return codeFromBusinessLayerOrNull.toUpperCase().startsWith(INTERNAL_NAMESPACE_PREFIX) == true;
        }
    }

    /**
     * Convert property column name as used in GUI to property type code as used in business layer.
     */
    public static String getPropertyTypeCode(String propertyColumnName)
    {
        if (propertyColumnName.startsWith(USER_PREFIX))
        {
            return tryToBusinessLayer(propertyColumnName.substring(USER_PREFIX.length()), false);
        }
        if (propertyColumnName.startsWith(INTERN_PREFIX))
        {
            return tryToBusinessLayer(propertyColumnName.substring(INTERN_PREFIX.length()), true);
        }
        return propertyColumnName;
    }

}
