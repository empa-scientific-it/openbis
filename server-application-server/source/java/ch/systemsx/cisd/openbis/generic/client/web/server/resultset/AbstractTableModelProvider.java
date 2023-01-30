/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;

/**
 * Superclass of {@link ITableModelProvider} implementations which creates the actual table model lazily.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractTableModelProvider<T extends Serializable> implements
        ITableModelProvider<T>
{
    private TypedTableModel<T> tableModel = null;

    /**
     * Creates the table model with the specified maximum number of rows. If {@link Integer#MAX_VALUE} is specified the complete table will be
     * created.
     */
    @Override
    public TypedTableModel<T> getTableModel(int maxSize)
    {
        if (tableModel == null)
        {
            tableModel = createTableModel();
        }
        List<TableModelColumnHeader> headers = tableModel.getHeader();
        List<TableModelRowWithObject<T>> rows = tableModel.getRows();
        List<TableModelRowWithObject<T>> limitedRows = new ArrayList<TableModelRowWithObject<T>>();
        for (TableModelRowWithObject<T> row : rows)
        {
            if (limitedRows.size() == maxSize)
            {
                break;
            }
            limitedRows.add(row);
        }
        return new TypedTableModel<T>(headers, limitedRows);
    }

    /**
     * Creates the complete table model.
     */
    protected abstract TypedTableModel<T> createTableModel();

    protected String metaProjectsToString(Collection<Metaproject> metaProjects)
    {
        String text = "";
        if (metaProjects != null)
        {
            List<String> names = new ArrayList<String>();
            for (Metaproject metaProject : metaProjects)
            {
                names.add(metaProject.getName());
            }
            Collections.sort(names, new Comparator<String>()
                {
                    @Override
                    public int compare(String arg0, String arg1)
                    {
                        return arg0.toLowerCase().compareTo(arg1.toLowerCase());
                    }
                });

            for (String name : names)
            {
                text += ", " + name;
            }

        }
        if (text.length() > 1)
        {
            text = text.substring(2);
        }
        return text;
    }

}
