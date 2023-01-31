/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;

import ch.systemsx.cisd.common.string.Template;

/**
 * Helper class to create temporary FASTA files based on data set files.
 *
 * @author Franz-Josef Elmer
 */
class FastaFileBuilderForDataSetFiles extends GenericFastaFileBuilder
{
    private static final Template ID_EXTENSION_TEMPLATE = new Template("[Data set: ${data_set}, File: ${file}]");

    private final String dataSetCode;

    private String idExtension;

    FastaFileBuilderForDataSetFiles(File tempFolder, String dataSetCode)
    {
        super(tempFolder, dataSetCode);
        this.dataSetCode = dataSetCode;
    }

    void setFilePath(String filePath)
    {
        writeFastaEntry();
        Template template = ID_EXTENSION_TEMPLATE.createFreshCopy();
        template.bind("data_set", dataSetCode);
        template.bind("file", filePath);
        idExtension = template.createText();
    }

    void handle(String line)
    {
        EntryType entryType = tryToGetEntryType(line);
        if (entryType != null)
        {
            if (idExtension == null)
            {
                throw new IllegalStateException("File path not set [Data Set: " + dataSetCode + "].");
            }
            startEntry(entryType, line.substring(1) + " " + idExtension, null);
        } else
        {
            appendToSequence(line);
        }
    }

    @Override
    String createErrorMessageForUndefinedEntry(String line)
    {
        return "Invalid line " + idExtension + ". Line with identifier expected: " + line;
    }

    private EntryType tryToGetEntryType(String line)
    {
        return line.startsWith(">") ? EntryType.FASTA : (line.startsWith("@") ? EntryType.FASTQ : null);
    }

}