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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ReportRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

/**
 * @author Franz-Josef Elmer
 */
public class TableModelUtils
{

    public static List<TableModelRowWithObject<ReportRowModel>> asTableModelRowsWithReportRowModels(
            List<TableModelRow> rows)
    {
        List<TableModelRowWithObject<ReportRowModel>> result =
                new ArrayList<TableModelRowWithObject<ReportRowModel>>();
        int rowCounter = 0;
        for (TableModelRow row : rows)
        {
            result.add(new TableModelRowWithObject<ReportRowModel>(
                    new ReportRowModel(rowCounter++), row.getValues()));
        }
        return result;
    }

}
