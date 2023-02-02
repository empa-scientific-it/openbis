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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * Test the ImageValidator.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unused")
public class ImageValidatorTest extends AbstractFileSystemTestCase
{

    private Mockery context;

    private IMailClient mailClient;

    private Logger operationLog;

    private Logger notificationLog;

    private BufferedAppender logAppender;

    @Override
    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        context = new Mockery();
        mailClient = context.mock(IMailClient.class);
        operationLog = LogFactory.getLogger(LogCategory.OPERATION, ImageValidatorTest.class);
        notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, ImageValidatorTest.class);
        logAppender = LogRecordingUtils.createRecorder();
    }

    @Test
    public void testHcsImages()
    {

    }
}
