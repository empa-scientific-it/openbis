/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.utils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;

/**
 * @author Franz-Josef Elmer
 */
public class FileScannerTest extends AbstractFileSystemTestCase
{
    private File root;

    private File beta;

    private File hello;

    private File hi;

    private File abc;

    private File a;

    private File b;

    private File c;

    @BeforeMethod
    public void setTestFiles() throws IOException
    {
        root = dir(workingDirectory, "root");
        {
            File alpha = dir(root, "alpha");
            {
                hello = file(alpha, "hello.txt");
                file(alpha, "abc");
            }
            beta = dir(root, "beta");
            {
                file(beta, "b.txt");
                File one = dir(beta, "1");
                {
                    hi = file(one, "hi.txt");
                    file(one, "hi");
                }
            }
            File gamma = dir(root, "gamma");
            {
                File one = dir(gamma, "1");
                {
                    abc = file(one, "abc$.png");
                }
                File dir1 = dir(gamma, "42");
                {
                    a = file(dir1, "a.tsv");
                    file(dir1, "alpha.tsv");
                    File alpha2 = dir(dir1, "alpha");
                    {
                        b = file(alpha2, "b.tsv");
                    }
                }
                File abcd = dir(gamma, "abcd");
                {
                    File dir2 = dir(abcd, "42");
                    {
                        c = file(dir2, "c.tsv");
                    }
                }
                File dir3 = dir(gamma, "43");
                {
                    file(dir3, "1.tsv");
                }
            }
        }
    }

    private File dir(File folder, String name)
    {
        File subFolder = new File(folder, name);
        subFolder.mkdirs();
        return subFolder;
    }

    private File file(File folder, String name) throws IOException
    {
        File file = new File(folder, name);
        file.createNewFile();
        return file;
    }

    @Test
    public void testFindAll()
    {
        FileScanner scanner = new FileScanner("**/*");

        List<File> files = scanner.scan(beta);
        assertEquals(4, files.size());

        files = scanner.scan(hi);
        assertEquals(hi, files.get(0));
        assertEquals(1, files.size());
    }

    @Test
    public void testFindAllTextFiles()
    {
        FileScanner scanner = new FileScanner("**/*.txt");

        List<File> files = scanner.scan(root);

        Collections.sort(files);
        assertEquals(hello, files.get(0));
        assertEquals(hi, files.get(1));
        assertEquals("b.txt", files.get(2).getName());
        assertEquals(3, files.size());
    }

    @Test
    public void testFindTopLevelTextFiles()
    {
        FileScanner scanner = new FileScanner("*.txt");

        List<File> files = scanner.scan(beta);

        Collections.sort(files);
        assertEquals("b.txt", files.get(0).getName());
        assertEquals(1, files.size());
    }

    @Test
    public void testFindSingleTopLevelFile()
    {
        FileScanner scanner = new FileScanner("**/*.txt");

        List<File> files = scanner.scan(hello);

        Collections.sort(files);
        assertEquals(hello, files.get(0));
        assertEquals(1, files.size());
    }

    @Test
    public void testFindSpecificFile()
    {
        FileScanner scanner = new FileScanner("gamma/1/abc$.png");

        List<File> files = scanner.scan(root);

        Collections.sort(files);
        assertEquals(abc, files.get(0));
        assertEquals(1, files.size());
    }

    @Test
    public void testFindByComplexPattern()
    {
        FileScanner scanner = new FileScanner("gamma/**/42/**/?.tsv");

        List<File> files = scanner.scan(root);

        Collections.sort(files);
        assertEquals(a, files.get(0));
        assertEquals(b, files.get(1));
        assertEquals(c, files.get(2));
        assertEquals(3, files.size());
    }
}
