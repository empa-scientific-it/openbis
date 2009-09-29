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

package ch.systemsx.cisd.yeastx.quant;

import java.io.File;
import java.sql.SQLException;

import net.lemnik.eodsql.QueryTool;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.yeastx.db.AbstractDBTest;
import ch.systemsx.cisd.yeastx.db.DMDataSetDTO;

/**
 * @author Tomasz Pylak
 */
public class QuantMSDAOTest extends AbstractDBTest
{
    private IQuantMSDAO quantmsDAO;

    @BeforeMethod(alwaysRun = true)
    public void setDAO() throws SQLException
    {
        quantmsDAO = QueryTool.getQuery(datasource, IQuantMSDAO.class);
    }

    @AfterMethod(alwaysRun = true)
    public void close()
    {
        if (quantmsDAO != null)
        {
            quantmsDAO.close();
        }
    }

    @Test
    public void testUploadFiaML() throws SQLException
    {
        QuantML2Database uploader = new QuantML2Database(datasource);
        DMDataSetDTO dataSetDTO =
                new DMDataSetDTO("data set perm id", "sample perm id", "sample name",
                        "experiment perm id", "experiment name");
        uploader.uploadQuantMLFile(new File("resource/examples/allFields.quantML"), dataSetDTO);
    }

}
