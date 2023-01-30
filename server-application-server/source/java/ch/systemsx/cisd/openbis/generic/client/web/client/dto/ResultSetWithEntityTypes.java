/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.annotation.DoNotEscape;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;

/**
 * Holds not only the result set, but also a set of entity types which are contained in the result set.
 * <p>
 * Entity types can be helpful in deciding which property types can be found in the result set.
 * </p>
 * 
 * @author Tomasz Pylak
 */
@DoNotEscape
public class ResultSetWithEntityTypes<T extends IEntityInformationHolder> implements
        IsSerializable, IResultSetHolder<T>
{
    private ResultSet<T> resultSet;

    private Set<BasicEntityType> availableEntityTypes;

    // GWT only
    @SuppressWarnings("unused")
    private ResultSetWithEntityTypes()
    {
    }

    public ResultSetWithEntityTypes(ResultSet<T> resultSet,
            Set<BasicEntityType> availableEntityTypes)
    {
        this.resultSet = resultSet;
        this.availableEntityTypes = availableEntityTypes;
    }

    @Override
    public ResultSet<T> getResultSet()
    {
        return resultSet;
    }

    public Set<BasicEntityType> getAvailableEntityTypes()
    {
        return availableEntityTypes;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\nAvailable entitiy types:\n\t");
        sb.append(availableEntityTypes.toString());
        sb.append("\nResult set:\n\t");
        sb.append(resultSet.toString());
        return sb.toString();
    }

}
