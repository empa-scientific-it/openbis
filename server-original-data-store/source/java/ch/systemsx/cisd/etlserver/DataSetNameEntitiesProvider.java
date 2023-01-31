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

package ch.systemsx.cisd.etlserver;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Class which splits a data set name into entities and makes them accessible.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetNameEntitiesProvider
{
    private final char entitySeparatorCharacter;

    private final String[] entities;

    private final String errorMessagePrefix;

    /**
     * Creates an instance based on the name of the specified file using the specified character which separates entities.
     */
    public DataSetNameEntitiesProvider(File dataSetFile, char entitySeparatorCharacter,
            boolean stripExtension)
    {
        this(dataSetFile.getName(), entitySeparatorCharacter, stripExtension);
    }

    /**
     * Creates an instance for the specified name using the specified character which separates entities.
     */
    public DataSetNameEntitiesProvider(String dataSetName, char entitySeparatorCharacter,
            boolean stripExtension)
    {
        assert dataSetName != null : "Unspecified data set name.";

        this.entitySeparatorCharacter = entitySeparatorCharacter;
        final String name;
        if (stripExtension)
        {
            name = FilenameUtils.getBaseName(dataSetName);
        } else
        {
            name = dataSetName;
        }
        entities = StringUtils.split(name, entitySeparatorCharacter);
        errorMessagePrefix = "Invalid data set name '" + dataSetName + "'. ";
    }

    /**
     * Returns the entity of specified index. Negative arguments can also be used. They are interpreted as an index counting from the end of the
     * sequence of entities. For example, -1 denotes the last entity.
     */
    public String getEntity(int index)
    {
        return getEntity(index, true);
    }

    String getEntity(int index, boolean throwException)
    {
        if (index >= entities.length)
        {
            return emptyStringOrThrowUserFailureException(index + 1, throwException);
        }
        int actualIndex = index;
        if (index < 0)
        {
            actualIndex = entities.length + index;
            if (actualIndex < 0)
            {
                return emptyStringOrThrowUserFailureException(-index, throwException);
            }
        }
        return entities[actualIndex];
    }

    private String emptyStringOrThrowUserFailureException(int expectedNumberOfEntities,
            boolean throwException)
    {
        if (throwException)
        {
            throw new UserFailureException(errorMessagePrefix + "We need "
                    + expectedNumberOfEntities + " entities, separated by '"
                    + entitySeparatorCharacter + "', but got only " + entities.length + ".");
        }
        return "";
    }

}
