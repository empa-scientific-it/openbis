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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Scanner of files based on wild-card patterns. In addition of the usual wild cards '*' (zero or more of any character) and '?' (exactly one
 * arbitrary character) the wild card '**&#47;' is used for zero or more sub folders of any name.
 *
 * @author Franz-Josef Elmer
 */
public class FileScanner
{
    private final Pattern pathRegex;

    public FileScanner(String pattern)
    {
        String patternAsRegEx = pattern.replace("$", "\\$");
        patternAsRegEx = patternAsRegEx.replace(".", "\\.");
        patternAsRegEx = patternAsRegEx.replace("**/", "(.+/){0,}");
        patternAsRegEx = patternAsRegEx.replace("*", "[^/]*");
        patternAsRegEx = patternAsRegEx.replace("?", ".");
        pathRegex = Pattern.compile("/" + patternAsRegEx);
    }

    /**
     * Returns a list of all files found matching the pattern. If <code>fileOrFolder</code> is a file the file is return if it matches the pattern. if
     * <code>fileOrFolder</code> is a folder all files inside the folders are checked.
     */
    public List<File> scan(File fileOrFolder)
    {
        List<File> files = new ArrayList<File>();
        int pathPrefixLength = fileOrFolder.toString().length();
        if (fileOrFolder.isFile())
        {
            pathPrefixLength = fileOrFolder.getParent().toString().length();
        }
        gatherFiles(files, fileOrFolder, pathPrefixLength);
        return files;
    }

    private void gatherFiles(List<File> gatheredFiles, File fileOrFolder, int pathPrefixLength)
    {
        String fileName = fileOrFolder.toString().substring(pathPrefixLength);
        if (pathRegex.matcher(fileName).matches())
        {
            gatheredFiles.add(fileOrFolder);
        }
        if (fileOrFolder.isDirectory())
        {
            File[] files = fileOrFolder.listFiles();
            for (File file : files)
            {
                gatherFiles(gatheredFiles, file, pathPrefixLength);
            }
        }
    }
}
