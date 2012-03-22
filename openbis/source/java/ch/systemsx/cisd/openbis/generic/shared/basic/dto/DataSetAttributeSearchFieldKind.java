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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

/**
 * Kinds of fields connected with Data Set attributes that can be used in detailed text queries.
 * 
 * @author Piotr Buczek
 */
public enum DataSetAttributeSearchFieldKind implements Serializable, IAttributeSearchFieldKind
{
    CODE("Code"),

    DATA_SET_TYPE("Data Set Type"),

    FILE_TYPE("File Type"),

    REGISTRATION_DATE("Registration date equals"),

    MODIFICATION_DATE("Modification date equals"),

    REGISTRATION_DATE_BEFORE("Registration date before"),

    MODIFICATION_DATE_BEFORE("Modification date before"),

    REGISTRATION_DATE_AFTER("Registration date after"),

    MODIFICATION_DATE_AFTER("Modification date after");

    private final String description;

    private DataSetAttributeSearchFieldKind(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public String getCode()
    {
        return name();
    }

}
