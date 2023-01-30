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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration that describes what format an external data set is stored in.
 * 
 * @author Bernd Rinn
 */
// TODO 2010-05-17, Tomasz Pylak: remove me after removing BDS
public enum StorageFormat
{

    /** The proprietary data format as acquired from the measurement device. */
    PROPRIETARY("PROPRIETARY"),
    /**
     * The standardized data according to the Biological Data Standard format, using a directory container.
     */
    BDS_DIRECTORY("BDS_DIRECTORY");

    private static final Map<String, StorageFormat> codeMap = new HashMap<String, StorageFormat>();

    public static final String VOCABULARY_CODE = "$STORAGE_FORMAT";

    static
    {
        for (final StorageFormat format : values())
        {
            assert codeMap.containsKey(format.code) == false : "Duplicate code";
            codeMap.put(format.code, format);
        }
    }

    private final String code;

    private StorageFormat(final String code)
    {
        this.code = code;
    }

    /**
     * Returns the code (string representation) of this storage format.
     * <p>
     * Use this instead of {@link #toString()} when storing in the database.
     */
    public final String getCode()
    {
        return code;
    }

    /**
     * Returns the appropriate format for the given <var>code</var>, or <code>null</code>, if there is no format for this <var>code</var>.
     */
    public final static StorageFormat tryGetFromCode(final String code)
    {
        return codeMap.get(code);
    }

}
