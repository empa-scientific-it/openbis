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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1;

/**
 * @author Kaloyan Enimanev
 */
public interface IPropertyType extends IPropertyTypeImmutable
{
    /**
     * Set the description for this property type. This is a mandatory parameter and must not be left emtpy when creating new {@link IPropertyType}-s.
     */
    void setDescription(String description);

    /**
     * Set the label for this property type. This is a mandatory parameter and must not be left emtpy when creating new {@link IPropertyType}-s.
     */
    void setLabel(String label);

    /**
     * Set the material type. Mandatory when {@link DataType} is MATERIAL.
     */
    void setMaterialType(IMaterialTypeImmutable materialType);

    /**
     * Set the associated vocabulary. Mandatory when {@link DataType} is CONTROLLEDVOCABULARY.
     */
    void setVocabulary(IVocabularyImmutable vocabulary);

    /**
     * Set an XSD used to verify the validity of properties with XML {@link DataType}.
     */
    void setXmlSchema(String schema);

    /**
     * Set an XSLT transformation to be applied to properties of XML {@link DataType} before rendering in the UI.
     */
    void setTransformation(String xsltTransformation);

    void setManagedInternally(boolean isManagedInternally);

    /**
     * Property of internal names receive a special prefix to avoid naming conflicts with user-defined property types.
     */
    void setInternalNamespace(boolean isInternalNamespace);
}
