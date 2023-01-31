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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.fasta.FastaUtilities;
import ch.systemsx.cisd.common.fasta.SequenceType;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.BlastUtils;

/**
 * Helper class to create temporary FASTA files.
 *
 * @author Franz-Josef Elmer
 */
class GenericFastaFileBuilder
{
    private static final class FastaEntry
    {
        private List<String> lines = new ArrayList<String>();

        private SequenceType seqType;

        FastaEntry(String id)
        {
            lines.add(">" + id);
        }

        void setSeqType(SequenceType seqType)
        {
            this.seqType = seqType;
        }

        SequenceType getSeqType()
        {
            return seqType;
        }

        void appendSeq(String seq)
        {
            lines.add(seq);
        }

        List<String> getLines()
        {
            return lines;
        }
    }

    enum EntryType
    {
        FASTA, FASTQ
    }

    private final File tempFolder;

    private final String baseName;

    private final Map<SequenceType, PrintWriter> writers = new HashMap<SequenceType, PrintWriter>();

    private FastaEntry currentFastaEntry;

    private EntryType currentEntryType;

    GenericFastaFileBuilder(File tempFolder, String baseName)
    {
        this.tempFolder = tempFolder;
        this.baseName = baseName;
    }

    String getBaseName()
    {
        return baseName;
    }

    void appendToSequence(String line)
    {
        if (currentFastaEntry == null)
        {
            throw new IllegalStateException(createErrorMessageForUndefinedEntry(line));
        }
        if (currentFastaEntry.getSeqType() == null)
        {
            currentFastaEntry.setSeqType(FastaUtilities.determineSequenceType(line));
            currentFastaEntry.appendSeq(line);
        } else if (currentEntryType == EntryType.FASTA)
        {
            currentFastaEntry.appendSeq(line);
        }
    }

    String createErrorMessageForUndefinedEntry(String line)
    {
        return "Unspecified entry";
    }

    void startEntry(EntryType entryType, String id, SequenceType sequenceTypeOrNull)
    {
        writeFastaEntry();
        currentFastaEntry = new FastaEntry(id);
        currentFastaEntry.setSeqType(sequenceTypeOrNull);
        currentEntryType = entryType;
    }

    void finish()
    {
        writeFastaEntry();
        for (PrintWriter printWriter : writers.values())
        {
            printWriter.close();
        }
    }

    void cleanUp()
    {
        SequenceType[] values = SequenceType.values();
        for (SequenceType sequenceType : values)
        {
            File file = getTemporaryFastaFileOrNull(sequenceType);
            if (file != null)
            {
                FileUtilities.delete(file);
            }
        }
    }

    File getTemporaryNuclFastaFileOrNull()
    {
        return getTemporaryFastaFileOrNull(SequenceType.NUCL);
    }

    File getTemporaryProtFastaFileOrNull()
    {
        return getTemporaryFastaFileOrNull(SequenceType.PROT);
    }

    File getTemporaryFastaFileOrNull(SequenceType seqType)
    {
        return writers.containsKey(seqType) ? getFastaFile(seqType) : null;
    }

    void writeFastaEntry()
    {
        if (currentFastaEntry == null)
        {
            return;
        }
        SequenceType seqType = currentFastaEntry.getSeqType();
        List<String> lines = currentFastaEntry.getLines();
        if (seqType == null)
        {
            throw new IllegalStateException("Unknown type of the following FASTA entry: " + lines);
        }
        PrintWriter printer = getPrinter(seqType);
        for (String line : lines)
        {
            printer.println(line);
        }
        currentFastaEntry = null;
    }

    private PrintWriter getPrinter(SequenceType seqType)
    {
        PrintWriter printWriter = writers.get(seqType);
        if (printWriter == null)
        {
            File fastaFile = getFastaFile(seqType);
            try
            {
                printWriter = new PrintWriter(new BufferedWriter(new FileWriter(fastaFile)));
                writers.put(seqType, printWriter);
            } catch (IOException ex)
            {
                throw new EnvironmentFailureException("Couldn't create temporary FASTA file '" + fastaFile
                        + "': " + ex.getMessage());
            }
        }
        return printWriter;
    }

    private File getFastaFile(SequenceType seqType)
    {
        return new File(tempFolder, BlastUtils.createDatabaseName(baseName, seqType) + ".fa");
    }

}
