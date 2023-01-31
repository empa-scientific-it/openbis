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

package ch.systemsx.cisd.openbis.dss.archiveverifier.batch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataSetArchiveVerifierTest
{
    @Test
    public void successfulVerificationCausesSuccessResult() throws Exception
    {
        DataSetArchiveVerificationResult result = verifier.run(CODE_OF_DATASET_WITH_GOOD_ARCHIVE);
        assertThat(result.getType(), is(ResultType.OK));
    }

    @Test
    public void failingVerificationCausesFailedResult() throws Exception
    {
        DataSetArchiveVerificationResult result = verifier.run(CODE_OF_DATASET_WITH_BAD_ARCHIVE);
        assertThat(result.getType(), is(ResultType.ERROR));
    }

    @Test
    public void failureToLocateArchiveFileCausesFailedResult() throws Exception
    {
        DataSetArchiveVerificationResult result = verifier.run(CODE_OF_DATASET_WITHOUT_AN_ARCHIVE_FILE);
        assertThat(result.getType(), is(ResultType.ERROR));
    }

    @BeforeMethod
    public void fixture()
    {
        Mockery context = new Mockery();
        fileRepository = context.mock(IArchiveFileRepository.class);
        fileVerifier = context.mock(IArchiveFileVerifier.class);
        verifier = new DataSetArchiveVerifier(fileRepository, fileVerifier);

        context.checking(new Expectations()
            {
                {
                    allowing(fileRepository).getArchiveFileOf(CODE_OF_DATASET_WITH_GOOD_ARCHIVE);
                    will(returnValue(GOOD_ARCHIVE_FILE));

                    allowing(fileVerifier).verify(GOOD_ARCHIVE_FILE);
                    will(returnValue(new ArrayList<VerificationError>()));

                    allowing(fileRepository).getArchiveFileOf(CODE_OF_DATASET_WITH_BAD_ARCHIVE);
                    will(returnValue(BAD_ARCHIVE_FILE));

                    allowing(fileVerifier).verify(BAD_ARCHIVE_FILE);
                    will(returnValue(Collections.singletonList(new VerificationError(VerificationErrorType.ERROR, "error"))));

                    allowing(fileRepository).getArchiveFileOf(CODE_OF_DATASET_WITHOUT_AN_ARCHIVE_FILE);
                    will(returnValue(NONEXISTING_FILE));

                }
            });
    }

    private DataSetArchiveVerifier verifier;

    private IArchiveFileRepository fileRepository;

    private IArchiveFileVerifier fileVerifier;

    private static final String CODE_OF_DATASET_WITH_GOOD_ARCHIVE = "correctly_archived_dataset";

    protected static final File GOOD_ARCHIVE_FILE = new ExistingFile("verification will succeed on this file");

    private static final String CODE_OF_DATASET_WITH_BAD_ARCHIVE = "incorrectly_archived_dataset";

    protected static final File BAD_ARCHIVE_FILE = new ExistingFile("verification will fail on this file");

    protected static final String CODE_OF_DATASET_WITHOUT_AN_ARCHIVE_FILE = "missing_archive_dataset";

    protected static final File NONEXISTING_FILE = new File("...");

    private static class ExistingFile extends File
    {

        private static final long serialVersionUID = 1L;

        private String message;

        public ExistingFile(String message)
        {
            super("");
            this.message = message;
        }

        @Override
        public boolean exists()
        {
            return true;
        }

        @Override
        public String toString()
        {
            return message;
        }

        @Override
        public int hashCode()
        {
            return message.hashCode();
        }

        @Override
        public boolean equals(Object o)
        {
            return o == this;
        }
    }
}
