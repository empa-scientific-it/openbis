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

package ch.systemsx.cisd.openbis.generic.shared.managed_property.api;

/**
 * Contains helper method to construct {@link IElement} instances.
 * 
 * @author Kaloyan Enimanev
 */
// NOTE: All methods of this interface are part of the Managed Properties API.
public interface IElementFactory
{

    /**
     * @return creates an {@link IElement} for a given element name.
     */
    IElement createElement(String name);

    /**
     * @return creates an link element pointing towards a sample with given perm id.
     */
    IEntityLinkElement createSampleLink(String permId);

    /**
     * @return creates an link element pointing towards an experiment with given perm id.
     */
    IEntityLinkElement createExperimentLink(String permId);

    /**
     * @return creates an link element pointing towards a data set with given perm id.
     */
    IEntityLinkElement createDataSetLink(String permId);

    /**
     * @return creates an link element pointing towards a material with given code and typeCode parameters.
     */
    IEntityLinkElement createMaterialLink(String code, String typeCode);

    /**
     * @return <code>true</code> if the specified element is a link element, <code>false</code> otherwise. This method might come handy in Jython
     *         scripts, where it is undesirable to hard-code class names or invoke "instanceof".
     */
    boolean isEntityLink(IElement element);

}