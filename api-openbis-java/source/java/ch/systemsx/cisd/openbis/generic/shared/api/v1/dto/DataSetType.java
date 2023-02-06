/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Immutable value object representing a data set type.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
@JsonObject("DataSetType")
public final class DataSetType extends EntityType
{
    private static final long serialVersionUID = 1L;

    /**
     * Class used to initialize a new {@link DataSetType} instance. Necessary since all the fields of a {@link DataSetType} are final.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static final class DataSetTypeInitializer extends EntityTypeInitializer
    {
        private boolean deletionDisallowed;

        private String mainDataSetPattern;

        private String mainDataSetPath;

        public DataSetTypeInitializer()
        {
            super();
        }

        public boolean isDeletionDisallowed()
        {
            return deletionDisallowed;
        }

        public void setDeletionDisallowed(boolean deletionDisallowed)
        {
            this.deletionDisallowed = deletionDisallowed;
        }

        public String getMainDataSetPattern()
        {
            return mainDataSetPattern;
        }

        public void setMainDataSetPattern(String mainDataSetPattern)
        {
            this.mainDataSetPattern = mainDataSetPattern;
        }

        public String getMainDataSetPath()
        {
            return mainDataSetPath;
        }

        public void setMainDataSetPath(String mainDataSetPath)
        {
            this.mainDataSetPath = mainDataSetPath;
        }

    }

    private boolean deletionDisallowed;

    private String mainDataSetPattern;

    private String mainDataSetPath;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public DataSetType(DataSetTypeInitializer initializer)
    {
        super(initializer);
        deletionDisallowed = initializer.isDeletionDisallowed();
        mainDataSetPattern = initializer.getMainDataSetPattern();
        mainDataSetPath = initializer.getMainDataSetPath();
    }

    /**
     * Returns <code>true</code> if deletion for data sets of this type are disallowed.
     */
    public boolean isDeletionDisallowed()
    {
        return deletionDisallowed;
    }

    /**
     * Returns main data set pattern if defined.
     */
    public String getMainDataSetPattern()
    {
        return mainDataSetPattern;
    }

    /**
     * Returns main data set path if defined.
     */
    public String getMainDataSetPath()
    {
        return mainDataSetPath;
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getCode());
        builder.append(getDescription());
        builder.append("deletionDisallowed", deletionDisallowed);
        builder.append("mainDataSetPattern", mainDataSetPattern);
        builder.append("mainDataSetPath", mainDataSetPath);
        builder.append(getPropertyTypeGroups());
        return builder.toString();
    }

    //
    // JSON-RPC
    //

    private DataSetType()
    {
    }

    private void setDeletionDisallowed(boolean deletionDisallowed)
    {
        this.deletionDisallowed = deletionDisallowed;
    }

    private void setMainDataSetPattern(String mainDataSetPattern)
    {
        this.mainDataSetPattern = mainDataSetPattern;
    }

    private void setMainDataSetPath(String mainDataSetPath)
    {
        this.mainDataSetPath = mainDataSetPath;
    }
}
