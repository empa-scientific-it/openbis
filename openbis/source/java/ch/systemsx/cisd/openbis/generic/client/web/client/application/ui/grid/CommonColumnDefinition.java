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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid;


/**
 * Definition of table columns for entities of type <code>T</code> together with the instructions
 * to render each column value.
 * 
 * @author Tomasz Pylak
 * @author Franz-Josef Elmer
 */
public class CommonColumnDefinition<T> extends AbstractColumnDefinition<T>
{
    protected IColumnDefinitionKind<T> columnDefinitionKind;
    
    /** Default constructor for GWT. */
    public CommonColumnDefinition()
    {
    }

    /**
     * Creates an instance for the specified column definition kind and header text.
     */
    public CommonColumnDefinition(final IColumnDefinitionKind<T> columnDefinitionKind,
            final String headerText)
    {
        super(headerText, columnDefinitionKind.getWidth(), columnDefinitionKind.isHidden());
        this.columnDefinitionKind = columnDefinitionKind;
    }
    
    /**
     * Returns the column definition kind.
     */
    public final IColumnDefinitionKind<T> getColumnDefinitionKind()
    {
        return columnDefinitionKind;
    }

    @Override
    protected String tryGetValue(T entity)
    {
        return columnDefinitionKind.tryGetValue(entity);
    }

    public String getIdentifier()
    {
        return columnDefinitionKind.id();
    }

}
