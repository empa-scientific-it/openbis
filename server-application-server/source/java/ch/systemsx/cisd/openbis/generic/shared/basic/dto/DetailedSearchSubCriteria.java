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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

/**
 * Describes detailed search sub criteria for specified associated entity kind.
 * 
 * @author Piotr Buczek
 */
public class DetailedSearchSubCriteria implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private DetailedSearchCriteria criteria;

    private AssociatedEntityKind targetEntityKind;

    public DetailedSearchSubCriteria(AssociatedEntityKind targetEntityKind,
            DetailedSearchCriteria criteria)
    {
        this.targetEntityKind = targetEntityKind;
        this.criteria = criteria;
    }

    public DetailedSearchCriteria getCriteria()
    {
        return criteria;
    }

    public AssociatedEntityKind getTargetEntityKind()
    {
        return targetEntityKind;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(targetEntityKind + ": ");
        sb.append(criteria.toString(false));
        return sb.toString();
    }

    // GWT only
    @SuppressWarnings("unused")
    private DetailedSearchSubCriteria()
    {
    }
}
