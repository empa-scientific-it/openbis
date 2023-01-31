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

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Result of verification of a data set archive
 * 
 * @author anttil
 */
public class DataSetArchiveVerificationResult
{

    private File file;

    private List<VerificationError> errors;

    public DataSetArchiveVerificationResult(File file, List<VerificationError> errors)
    {
        this.file = file;
        this.errors = errors;
    }

    public DataSetArchiveVerificationResult(VerificationErrorType type, String message)
    {
        this.file = null;
        this.errors = Collections.singletonList(new VerificationError(type, message));
    }

    public DataSetArchiveVerificationResult(Exception e)
    {
        this.file = null;
        this.errors = Collections.singletonList(new VerificationError(VerificationErrorType.ERROR, e.getClass().getName() + ":" + e.getMessage()));
    }

    public ResultType getType()
    {
        if (errors.isEmpty())
        {
            return ResultType.OK;

        }
        // find the maximum error
        VerificationErrorType errorType = VerificationErrorType.WARNING;
        for (VerificationError error : errors)
        {
            errorType = errorType.compareTo(error.getType()) > 0 ? errorType : error.getType();
        }

        switch (errorType)
        {
            case WARNING:
                return ResultType.WARNING;
            case GENERAL_WARNING:
                return ResultType.GENERAL_WARNING;
            case ERROR:
                return ResultType.ERROR;
            case FATAL:
                return ResultType.FATAL;
            default:
                throw new IllegalStateException("unknown error type " + errorType);
        }
    }

    public List<VerificationError> getErrors()
    {
        return errors;
    }

    public String getFileName()
    {
        if (file != null)
        {
            return file.getAbsolutePath();
        } else
        {
            return "file not found";
        }
    }

}
