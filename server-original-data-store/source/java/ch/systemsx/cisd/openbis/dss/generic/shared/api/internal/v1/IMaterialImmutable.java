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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1;

/**
 * @author Kaloyan Enimanev
 */
public interface IMaterialImmutable extends IMetaprojectContent
{

    /**
     * Return the identifier for this material.
     */
    String getMaterialIdentifier();

    /**
     * Return the code for this material.
     */
    String getCode();

    /**
     * Return the type for this material. May be null.
     */
    String getMaterialType();

    /**
     * Return true if the material exists in the database.
     */
    boolean isExistingMaterial();

    /**
     * Return the value of a property specified by a code. May return null of no such property with code <code>propertyCode</code> is found.
     */
    String getPropertyValue(String propertyCode);

}
