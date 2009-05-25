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

package ch.systemsx.cisd.yeastx.eicml;

import java.sql.Connection;
import java.sql.SQLException;

import ch.systemsx.cisd.yeastx.db.DBFactory;

/**
 * A performance test of reading all chromatograms from the database.
 *
 * @author Bernd Rinn
 */
public class ReadChromatogramsPerformanceTest
{

    public static void main(String[] args) throws SQLException
    {
        final Connection conn = new DBFactory(DBFactory.createDefaultDBContext()).getConnection();
        long start = System.currentTimeMillis();
        try
        {
            final IEICMSRunDAO dao = EICML2Database.getDAO(conn);
            for (EICMSRunDTO run : dao.getMsRuns())
            {
                // We need to iterate over the chromatograms to make sure they are really read.
                for (@SuppressWarnings("unused")
                ChromatogramDTO chromatogram : dao.getChromatogramsForRun(run))
                {
                    // Nothing to do.
                }
            }
        } finally
        {
            conn.close();
        }
        System.out.println((System.currentTimeMillis() - start) / 1000.0f);
    }

}
