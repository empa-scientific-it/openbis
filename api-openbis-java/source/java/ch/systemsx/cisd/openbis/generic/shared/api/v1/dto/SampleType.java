/*
 * Copyright 2013 ETH Zuerich, CISD
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
 * Immutable value object representing a sample type.
 *
 * @author Franz-Josef Elmer
 */
@SuppressWarnings("unused")
@JsonObject("SampleType")
public class SampleType extends EntityType
{
    private static final long serialVersionUID = 1L;

    /**
     * Class used to initialize a new {@link SampleType} instance. Necessary since all the fields of a {@link SampleType} are final.
     * 
     * @author Franz-Josef Elmer
     */
    public static final class SampleTypeInitializer extends EntityTypeInitializer
    {
        private boolean listable;

        private boolean showContainer;

        private boolean showParents;

        private boolean uniqueSubcodes;

        private boolean automaticCodeGeneration;

        private boolean showParentMetaData;

        private String codePrefix;

        public SampleTypeInitializer()
        {
            super();
        }

        public boolean isListable()
        {
            return listable;
        }

        public void setListable(boolean listable)
        {
            this.listable = listable;
        }

        public boolean isShowContainer()
        {
            return showContainer;
        }

        public void setShowContainer(boolean showContainer)
        {
            this.showContainer = showContainer;
        }

        public boolean isShowParents()
        {
            return showParents;
        }

        public void setShowParents(boolean showParents)
        {
            this.showParents = showParents;
        }

        public boolean isUniqueSubcodes()
        {
            return uniqueSubcodes;
        }

        public void setUniqueSubcodes(boolean uniqueSubcodes)
        {
            this.uniqueSubcodes = uniqueSubcodes;
        }

        public boolean isAutomaticCodeGeneration()
        {
            return automaticCodeGeneration;
        }

        public void setAutomaticCodeGeneration(boolean automaticCodeGeneration)
        {
            this.automaticCodeGeneration = automaticCodeGeneration;
        }

        public boolean isShowParentMetaData()
        {
            return showParentMetaData;
        }

        public void setShowParentMetaData(boolean showParentMetaData)
        {
            this.showParentMetaData = showParentMetaData;
        }

        public String getCodePrefix()
        {
            return codePrefix;
        }

        public void setCodePrefix(String codePrefix)
        {
            this.codePrefix = codePrefix;
        }
    }

    private boolean listable;

    private boolean showContainer;

    private boolean showParents;

    private boolean uniqueSubcodes;

    private boolean automaticCodeGeneration;

    private boolean showParentMetaData;

    private String codePrefix;

    /**
     * Creates a new instance with the provided initializer
     * 
     * @throws IllegalArgumentException if some of the required information is not provided.
     */
    public SampleType(SampleTypeInitializer initializer)
    {
        super(initializer);
        listable = initializer.isListable();
        showContainer = initializer.isShowContainer();
        showParents = initializer.isShowParents();
        uniqueSubcodes = initializer.isUniqueSubcodes();
        automaticCodeGeneration = initializer.isAutomaticCodeGeneration();
        showParentMetaData = initializer.isShowParentMetaData();
        codePrefix = initializer.getCodePrefix();
    }

    /**
     * Returns <code>true</code> if this is a sample type of listable samples.
     */
    public boolean isListable()
    {
        return listable;
    }

    /**
     * Returns <code>true</code> if for samples of this type the container field is shown in edit/registration form.
     */
    public boolean isShowContainer()
    {
        return showContainer;
    }

    /**
     * Returns <code>true</code> if for sample of this type the parents field is shown in edit/registration form.
     */
    public boolean isShowParents()
    {
        return showParents;
    }

    /**
     * Returns <code>true</code> if the sub code of samples of this type has to be unique.
     */
    public boolean isUniqueSubcodes()
    {
        return uniqueSubcodes;
    }

    /**
     * Returns <code>true</code> if the sample code is created automatically.
     */
    public boolean isAutomaticCodeGeneration()
    {
        return automaticCodeGeneration;
    }

    /**
     * Returns <code>true</code> if meta data of parents of samples of this type should be shown.
     */
    public boolean isShowParentMetaData()
    {
        return showParentMetaData;
    }

    /**
     * Returns code prefix used to create sample code automatically.
     */
    public String getCodePrefix()
    {
        return codePrefix;
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append(getCode());
        builder.append(getDescription());
        builder.append(getValidationPluginInfo());
        builder.append("listable", isListable());
        builder.append("showContainer", isShowContainer());
        builder.append("showParents", isShowParents());
        builder.append("showParentMetaData", isShowParentMetaData());
        builder.append("uniqueSubcodes", isUniqueSubcodes());
        builder.append("automaticCodeGeneration", isAutomaticCodeGeneration());
        builder.append("codePrefix", getCodePrefix());
        builder.append(getPropertyTypeGroups());
        return builder.toString();
    }

    //
    // JSON-RPC
    //

    private SampleType()
    {
    }

    private void setListable(boolean listable)
    {
        this.listable = listable;
    }

    private void setShowContainer(boolean showContainer)
    {
        this.showContainer = showContainer;
    }

    private void setShowParents(boolean showParents)
    {
        this.showParents = showParents;
    }

    private void setUniqueSubcodes(boolean uniqueSubcodes)
    {
        this.uniqueSubcodes = uniqueSubcodes;
    }

    private void setAutomaticCodeGeneration(boolean automaticCodeGeneration)
    {
        this.automaticCodeGeneration = automaticCodeGeneration;
    }

    private void setShowParentMetaData(boolean showParentMetaData)
    {
        this.showParentMetaData = showParentMetaData;
    }

    private void setCodePrefix(String codePrefix)
    {
        this.codePrefix = codePrefix;
    }

}
