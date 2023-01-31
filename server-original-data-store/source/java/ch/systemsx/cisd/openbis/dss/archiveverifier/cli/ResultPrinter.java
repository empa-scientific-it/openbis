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

package ch.systemsx.cisd.openbis.dss.archiveverifier.cli;

import java.io.PrintStream;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.BatchResult;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.DataSetArchiveVerificationResult;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.ResultType;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationError;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationErrorType;

/**
 * Prints the results of a dataset archive batch verification.
 * 
 * @author anttil
 */
public class ResultPrinter
{

    private final PrintStream out;

    public ResultPrinter(PrintStream out)
    {
        this.out = out;
    }

    public void print(BatchResult result)
    {
        Map<ResultType, Integer> counts = createCountMap();

        for (String dataSet : result.getDataSets())
        {
            DataSetArchiveVerificationResult dataSetResult = result.getResult(dataSet);
            printResult(dataSet, dataSetResult);

            ResultType type = dataSetResult.getType();
            counts.put(type, counts.get(type) + 1);
        }

        printTotals(counts);
    }

    private void printResult(String dataSet, DataSetArchiveVerificationResult result)
    {
        Collection<VerificationError> errors = result.getErrors();

        if (errors.isEmpty())
        {
            out.println("OK: " + dataSet + " (" + result.getFileName() + ")");
            return;
        }

        for (VerificationError error : errors)
        {
            VerificationErrorType type = error.getType();
            if (VerificationErrorType.FATAL.equals(type))
            {
                out.println(result.getErrors().get(0).getMessage());
            } else if (VerificationErrorType.GENERAL_WARNING.equals(type))
            {
                out.println("WARNING: " + error.getMessage());
            } else
            {
                out.println(type + " in " + dataSet + " (" + result.getFileName() + "): " + error.getMessage());
            }

        }
    }

    private Map<ResultType, Integer> createCountMap()
    {
        Map<ResultType, Integer> counts = new EnumMap<ResultType, Integer>(ResultType.class);
        for (ResultType type : ResultType.values())
        {
            counts.put(type, 0);
        }
        return counts;
    }

    private void printTotals(Map<ResultType, Integer> counts)
    {
        int ok = counts.get(ResultType.OK);
        int warning = counts.get(ResultType.WARNING);
        int error = counts.get(ResultType.ERROR);
        int fatal = counts.get(ResultType.FATAL);
        int generalWarnings = counts.get(ResultType.GENERAL_WARNING);

        int total = ok + warning + error;

        if (fatal > 0)
        {
            return;
        }

        out.println();
        out.println("---");
        out.println("Total of " + total + " dataset archives tested.");

        if (warning + error == 0)
        {
            out.println("No errors found.");
        } else
        {
            if (error > 0)
            {
                out.println(error + " archive file(s) contained errors.");
            }
            if (warning > 0)
            {
                out.println(warning + " archive file(s) caused warnings");
            }
        }

        if (generalWarnings > 0)
        {
            out.println(generalWarnings + " general warning(s).");
        }
    }
}
