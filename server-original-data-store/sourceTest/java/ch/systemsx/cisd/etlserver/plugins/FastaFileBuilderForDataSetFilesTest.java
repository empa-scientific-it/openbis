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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
public class FastaFileBuilderForDataSetFilesTest extends AbstractFileSystemTestCase
{
    private static final String DATA_SET_CODE = "11358-13";

    private File tempFolder;

    private FastaFileBuilderForDataSetFiles builder;

    @BeforeMethod
    public void setUpTempFolder()
    {
        tempFolder = new File(workingDirectory, "temp");
        tempFolder.mkdirs();
        builder = new FastaFileBuilderForDataSetFiles(tempFolder, DATA_SET_CODE);
    }

    @Test
    public void testThreeNuclEntriesFromTwoFastaFiles()
    {
        builder.setFilePath("my-data/1.fa");
        builder.handle(">lcl|1 example 1");
        builder.handle("GTTTACCCAAACTTCTATATGACTT");
        builder.handle("AAATTAAAATAATGCTGAGATGATA");
        builder.handle(">lcl|2 example 2");
        builder.handle("GACTTCTATATGATTTACCCAACTT");
        builder.handle("ATAATGCTGAATTAAAATAAGATGA");
        builder.setFilePath("my-data/2.fa");
        builder.handle(">lcl|3 example 3");
        builder.handle("GACTTCTTTATATGATTTACCCAACTTAGCGT");
        builder.finish();

        assertEquals(null, builder.getTemporaryProtFastaFileOrNull());
        File temporaryNuclFastaFile = builder.getTemporaryNuclFastaFileOrNull();
        assertEquals(DATA_SET_CODE + "-nucl.fa", FileUtilities.getRelativeFilePath(tempFolder, temporaryNuclFastaFile));
        assertEquals(">lcl|1 example 1 [Data set: 11358-13, File: my-data/1.fa]\n"
                + "GTTTACCCAAACTTCTATATGACTT\n"
                + "AAATTAAAATAATGCTGAGATGATA\n"
                + ">lcl|2 example 2 [Data set: 11358-13, File: my-data/1.fa]\n"
                + "GACTTCTATATGATTTACCCAACTT\n"
                + "ATAATGCTGAATTAAAATAAGATGA\n"
                + ">lcl|3 example 3 [Data set: 11358-13, File: my-data/2.fa]\n"
                + "GACTTCTTTATATGATTTACCCAACTTAGCGT",
                FileUtilities.loadToString(temporaryNuclFastaFile).trim());
    }

    @Test
    public void testFastqFiles()
    {
        builder.setFilePath("my-data/1.fastq");
        builder.handle("@lcl|1 example 1");
        builder.handle("GTTTACCCAAACTTCTATATGACTT");
        builder.handle("+");
        builder.handle("d^dddadd^BBBBBBefcfffffcc");
        builder.handle("@lcl|2 example 2");
        builder.handle("ATAATGCTGAATTAAAATAAGATGA");
        builder.handle("BBBefcfffffd^dddadd^BBBcc");
        builder.handle("@lcl|3 example 3");
        builder.handle("GACTTCTTTATATGATTTACCCAACTTAGCGT");
        builder.handle("@lcl|4 example 4");
        builder.handle("GACTTCTTTATATGCTTAGCGTATTTACCCAA");
        builder.handle("+");
        builder.finish();

        assertEquals(null, builder.getTemporaryProtFastaFileOrNull());
        File temporaryNuclFastaFile = builder.getTemporaryNuclFastaFileOrNull();
        assertEquals(DATA_SET_CODE + "-nucl.fa", FileUtilities.getRelativeFilePath(tempFolder, temporaryNuclFastaFile));
        assertEquals(">lcl|1 example 1 [Data set: 11358-13, File: my-data/1.fastq]\n"
                + "GTTTACCCAAACTTCTATATGACTT\n"
                + ">lcl|2 example 2 [Data set: 11358-13, File: my-data/1.fastq]\n"
                + "ATAATGCTGAATTAAAATAAGATGA\n"
                + ">lcl|3 example 3 [Data set: 11358-13, File: my-data/1.fastq]\n"
                + "GACTTCTTTATATGATTTACCCAACTTAGCGT\n"
                + ">lcl|4 example 4 [Data set: 11358-13, File: my-data/1.fastq]\n"
                + "GACTTCTTTATATGCTTAGCGTATTTACCCAA",
                FileUtilities.loadToString(temporaryNuclFastaFile).trim());
    }

    @Test
    public void testNuclFastaFileAndProtFastaFile()
    {
        builder.setFilePath("my-data/1.fa");
        builder.handle(">lcl|1 example 1");
        builder.handle("GTTTACCCAAACTTCTATATGACTT");
        builder.handle("AAATTAAAATAATGCTGAGATGATA");
        builder.handle(">lcl|2 example 2");
        builder.handle("GACTTCTATATGATTTACCCAACTT");
        builder.handle("ATAATGCTGAATTAAAATAAGATGA");
        builder.setFilePath("my-data/2.fa");
        builder.handle(">lcl|3 example 3");
        builder.handle("VGLTNYAAAYCTGLLLAR");
        builder.finish();

        File temporaryNuclFastaFile = builder.getTemporaryNuclFastaFileOrNull();
        assertEquals(DATA_SET_CODE + "-nucl.fa", FileUtilities.getRelativeFilePath(tempFolder, temporaryNuclFastaFile));
        assertEquals(">lcl|1 example 1 [Data set: 11358-13, File: my-data/1.fa]\n"
                + "GTTTACCCAAACTTCTATATGACTT\n"
                + "AAATTAAAATAATGCTGAGATGATA\n"
                + ">lcl|2 example 2 [Data set: 11358-13, File: my-data/1.fa]\n"
                + "GACTTCTATATGATTTACCCAACTT\n"
                + "ATAATGCTGAATTAAAATAAGATGA",
                FileUtilities.loadToString(temporaryNuclFastaFile).trim());
        File temporaryProtFastaFile = builder.getTemporaryProtFastaFileOrNull();
        assertEquals(DATA_SET_CODE + "-prot.fa", FileUtilities.getRelativeFilePath(tempFolder, temporaryProtFastaFile));
        assertEquals(">lcl|3 example 3 [Data set: 11358-13, File: my-data/2.fa]\n"
                + "VGLTNYAAAYCTGLLLAR",
                FileUtilities.loadToString(temporaryProtFastaFile).trim());
    }

    @Test
    public void testCleanUp()
    {
        builder.setFilePath("my-data/1.fa");
        builder.handle(">lcl|1 example 1");
        builder.handle("GTTTACCCAAACTTCTATATGACTT");
        builder.finish();
        File temporaryNuclFastaFile = builder.getTemporaryNuclFastaFileOrNull();
        assertEquals(true, temporaryNuclFastaFile.exists());

        builder.cleanUp();

        assertEquals(false, temporaryNuclFastaFile.exists());
    }

    @Test
    public void testUnspecifiedFilePath()
    {
        try
        {
            builder.handle(">lcl|1");
        } catch (IllegalStateException ex)
        {
            assertEquals("File path not set [Data Set: 11358-13].", ex.getMessage());
        }
    }

    @Test
    public void testMissingIdLine()
    {
        builder.setFilePath("my-data/1.fa");
        try
        {
            builder.handle("GATTACA");
        } catch (IllegalStateException ex)
        {
            assertEquals("Invalid line [Data set: 11358-13, File: my-data/1.fa]. "
                    + "Line with identifier expected: GATTACA", ex.getMessage());
        }
    }

    @Test
    public void testMissingSequenceLine()
    {
        builder.setFilePath("my-data/1.fa");
        builder.handle(">lcl|1");
        try
        {
            builder.finish();
        } catch (IllegalStateException ex)
        {
            assertEquals("Unknown type of the following FASTA entry: "
                    + "[>lcl|1 [Data set: 11358-13, File: my-data/1.fa]]", ex.getMessage());
        }
    }

}
