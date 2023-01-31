/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.archiveverifier.verifier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.Collection;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.IArchiveFileVerifier;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationError;

public class ZipFileIntegrityVerifierTest
{
    @Test
    public void verificationOfZipFileWithCRCErrorFails() throws Exception
    {
        Collection<VerificationError> errors = verifier.verify(FILE_WITH_CRC_ERROR);
        assertThat(errors.isEmpty(), is(false));
    }

    @Test
    public void verificationOfInvalidZipFileFails() throws Exception
    {
        Collection<VerificationError> errors = verifier.verify(INVALID_ZIP_FILE);
        assertThat(errors.isEmpty(), is(false));
    }

    @Test
    public void verificationOfNonExistingFileFails() throws Exception
    {
        Collection<VerificationError> errors = verifier.verify(NONEXISTING_FILE);
        assertThat(errors.isEmpty(), is(false));
    }

    @Test
    public void verificationOfValidZipFileSucceeds() throws Exception
    {
        Collection<VerificationError> errors = verifier.verify(VALID_ZIP_FILE);
        assertThat(errors.isEmpty(), is(true));
    }

    @BeforeMethod
    public void fixture()
    {
        verifier = new ZipFileIntegrityVerifier();
    }

    private IArchiveFileVerifier verifier;

    private static final File PWD = new File("../datastore_server/sourceTest/java/ch/systemsx/cisd/openbis/dss/archiveverifier/verifier/");

    private static final File VALID_ZIP_FILE = new File(PWD, "VALID.zip");

    private static final File FILE_WITH_CRC_ERROR = new File(PWD, "CRC_ERROR.zip");

    private static final File INVALID_ZIP_FILE = new File(PWD, "INVALID.zip");

    private static final File NONEXISTING_FILE = new File("...");

}
