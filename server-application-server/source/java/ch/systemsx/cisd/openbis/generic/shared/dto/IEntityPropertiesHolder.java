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

import java.util.Set;

/**
 * Interface of classes having entity properties.
 * 
 * @author Christian Ribeaud
 */
public interface IEntityPropertiesHolder extends IIdAndCodeHolder
{

    public EntityTypePE getEntityType();

    /**
     * Gets a copy of the entity properties. Note that this method returns an immutable collection that will throw
     * {@link UnsupportedOperationException} on any method that would change it. Use {@link #setProperties(Set)} or
     * {@link #addProperty(EntityPropertyPE)} instead.
     */
    public Set<? extends EntityPropertyPE> getProperties();

    /**
     * Returns <code>true</code>, if and only if the properties have been initialized.
     */
    public boolean isPropertiesInitialized();

    /**
     * Sets the entity properties.
     */
    public void setProperties(final Set<? extends EntityPropertyPE> properties);

    /**
     * Adds the <var>property</var> to the list of properties of this property holder.
     */
    public void addProperty(EntityPropertyPE property);

    /**
     * Removes the <var>property</var> from the list of properties of this property holder. Note that such a property has to be deleted or added to a
     * different holder in the same session.
     */
    public void removeProperty(EntityPropertyPE property);
}
