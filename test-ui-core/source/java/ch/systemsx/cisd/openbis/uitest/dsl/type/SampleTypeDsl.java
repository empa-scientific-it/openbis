/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.dsl.type;

import java.util.Collection;

import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Script;

/**
 * @author anttil
 */
class SampleTypeDsl extends SampleType
{

    private final String code;

    private String description;

    private boolean listable;

    private boolean showContainer;

    private boolean showParents;

    private boolean uniqueSubcodes;

    private boolean generateCodes;

    private boolean showParentMetadata;

    private String generatedCodePrefix;

    private Collection<PropertyTypeAssignment> propertyTypeAssignments;

    private Script validationScript;

    SampleTypeDsl(String code, String description, boolean listable, boolean showContainer,
            boolean showParents, boolean uniqueSubcodes, boolean generateCodes,
            boolean showParentMetadata, String generatedCodePrefix,
            Collection<PropertyTypeAssignment> propertyTypeAssignments, Script validationScript)
    {
        this.code = code;
        this.description = description;
        this.listable = listable;
        this.showContainer = showContainer;
        this.showParents = showParents;
        this.uniqueSubcodes = uniqueSubcodes;
        this.generateCodes = generateCodes;
        this.showParentMetadata = showParentMetadata;
        this.generatedCodePrefix = generatedCodePrefix;
        this.propertyTypeAssignments = propertyTypeAssignments;
        this.validationScript = validationScript;
    }

    @Override
    public String getCode()
    {
        return code;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public boolean isListable()
    {
        return listable;
    }

    @Override
    public boolean isShowContainer()
    {
        return showContainer;
    }

    @Override
    public boolean isShowParents()
    {
        return showParents;
    }

    @Override
    public boolean isUniqueSubcodes()
    {
        return uniqueSubcodes;
    }

    @Override
    public boolean isGenerateCodes()
    {
        return generateCodes;
    }

    @Override
    public boolean isShowParentMetadata()
    {
        return showParentMetadata;
    }

    @Override
    public String getGeneratedCodePrefix()
    {
        return generatedCodePrefix;
    }

    @Override
    public Collection<PropertyTypeAssignment> getPropertyTypeAssignments()
    {
        return propertyTypeAssignments;
    }

    @Override
    public Script getValidationScript()
    {
        return validationScript;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    void setListable(boolean listable)
    {
        this.listable = listable;
    }

    void setShowContainer(boolean showContainer)
    {
        this.showContainer = showContainer;
    }

    void setShowParents(boolean showParents)
    {
        this.showParents = showParents;
    }

    void setUniqueSubcodes(boolean uniqueSubcodes)
    {
        this.uniqueSubcodes = uniqueSubcodes;
    }

    void setGenerateCodes(boolean generateCodes)
    {
        this.generateCodes = generateCodes;
    }

    void setShowParentMetadata(boolean showParentMetadata)
    {
        this.showParentMetadata = showParentMetadata;
    }

    void setGeneratedCodePrefix(String generatedCodePrefix)
    {
        this.generatedCodePrefix = generatedCodePrefix;
    }

    void setPropertyTypeAssignments(Collection<PropertyTypeAssignment> propertyTypeAssignments)
    {
        this.propertyTypeAssignments = propertyTypeAssignments;
    }

    void setValidationScript(Script validationScript)
    {
        this.validationScript = validationScript;
    }
}
