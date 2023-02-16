/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;

import javax.annotation.Resource;

import org.apache.log4j.Level;
import org.springframework.beans.factory.InitializingBean;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

//@Test(groups = "db")
public class DAOFactoryTest extends AbstractDAOTest
{
    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    private BufferedAppender logRecorder;

    @BeforeMethod
    public void setUpLogRecorder()
    {
        logRecorder = LogRecordingUtils.createRecorder(null, Level.INFO, ".*\\." + DAOFactory.class.getSimpleName());
    }

    @Test(priority = -1, groups = "project-samples")
    public void testDisableProjectSamples() throws Exception
    {
        configurer.getResolvedProps().setProperty(Constants.PROJECT_SAMPLES_ENABLED_KEY, "false");
        ((InitializingBean) daoFactory).afterPropertiesSet();
        assertEquals("It is not possible to disable project samples feature. "
                + "The system still considers project-samples-enabled=true.",
                logRecorder.getLogContent());
    }

}
