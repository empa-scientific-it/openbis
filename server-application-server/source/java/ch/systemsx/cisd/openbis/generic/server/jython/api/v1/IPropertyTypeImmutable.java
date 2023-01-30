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
 * Read-only interface to an existing property type.
 * 
 * @author Kaloyan Enimanev
 */
public interface IPropertyTypeImmutable extends IAbstractType
{
    /**
     * Return the property type label.
     */
    String getLabel();

    /**
     * Return the data type of the property.
     */
    DataType getDataType();

    /**
     * Returns the material type for properties with data type {@link DataType#MATERIAL}
     */
    IMaterialTypeImmutable getMaterialType();

    /**
     * Returns the vocabulary for properties with data type {@link DataType#CONTROLLEDVOCABULARY}
     */
    IVocabularyImmutable getVocabulary();

    /**
     * Return an XSD used to verify the validity of properties with XML {@link DataType}. Return <code>null</code> if the DataType is not XML or there
     * is no schema set.
     */
    String getXmlSchema();

    /**
     * Return an XSLT transformation applied to properties with XML {@link DataType}. Return <code>null</code> if the DataType is not XML or there is
     * no transformation set.
     */
    String getTransformation();

    boolean isManagedInternally();

    boolean isInternalNamespace();

}
