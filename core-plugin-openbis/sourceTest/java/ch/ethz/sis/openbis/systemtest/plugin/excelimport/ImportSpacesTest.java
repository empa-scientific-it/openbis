/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.systemtest.plugin.excelimport;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

@ContextConfiguration(locations = "classpath:applicationContext.xml")
@Transactional(transactionManager = "transaction-manager")
@Rollback
public class ImportSpacesTest extends AbstractImportTest
{
    @Autowired
    private IApplicationServerInternalApi v3api;

    private static final String SPACES_XLS = "spaces/normal_spaces.xls";

    private static final String SPACES_NO_CODE = "spaces/no_code.xls";

    private static final String SPACES_NO_DESCRIPTION = "spaces/no_desc.xls";

    private static String FILES_DIR;

    @BeforeClass
    public void setupClass() throws IOException
    {
        String f = ImportSpacesTest.class.getName().replace(".", "/");
        FILES_DIR = f.substring(0, f.length() - ImportSpacesTest.class.getSimpleName().length()) + "/test_files/";
    }

    @Test
    @DirtiesContext
    public void testNormalSpacesAreCreated() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACES_XLS)));
        // WHEN
        Space rawData = TestUtils.getSpace(v3api, sessionToken, "TEST_SPACE");
        // THEN
        assertEquals(rawData.getCode(), "TEST_SPACE");
        assertEquals(rawData.getDescription(), "TEST");
    }

    @Test
    @DirtiesContext
    public void testNormalSpacesAreCreatedSecondSpace() throws IOException
    {
        // GIVEN
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACES_XLS)));
        // WHEN
        Space space = TestUtils.getSpace(v3api, sessionToken, "TEST_SPACE2");
        // THEN
        assertEquals(space.getCode(), "TEST_SPACE2");
        assertEquals(space.getDescription(), "TEST desc");
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void shouldThrowExceptionIfNoSpaceCode() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACES_NO_CODE)));
    }

    @Test(expectedExceptions = UserFailureException.class)
    @DirtiesContext
    public void shouldCreateSpaceWhenNoDescription() throws IOException
    {
        TestUtils.createFrom(v3api, sessionToken, Paths.get(FilenameUtils.concat(FILES_DIR, SPACES_NO_DESCRIPTION)));
    }

}
