/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertContainsNot;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAOTest;
import ch.systemsx.cisd.openbis.generic.shared.util.EodSqlUtils;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 *
 */
public class EodSqlUtilsTest extends AbstractDAOTest
{
    @Test
    public void test() throws Exception, IllegalAccessException
    {
        BufferedAppender recorder = LogRecordingUtils.createRecorder();
        
        try
        {
            EodSqlUtils.setManagedConnection(daoFactory.getSessionFactory().getCurrentSession().getTransaction());
        } finally
        {
            EodSqlUtils.clearManagedConnection();
        }
        
        assertContainsNot("EodSql", recorder.getLogContent());
    }

}
