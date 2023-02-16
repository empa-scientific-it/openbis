/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package org.python.core;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class exists to expose the two-arg constructor of SyspathArchive and to hack getEntry to work when the jar file name doesn't end with .jar.
 * Fixed a bit to work with openBIS.
 * 
 * @author Kent Johnson
 */
public class SyspathArchiveHack extends SyspathArchive
{
    private static final long serialVersionUID = 694744188445154734L;

    private ZipFile zipfileToo;

    public SyspathArchiveHack(ZipFile zipFile, String archiveName) throws IOException
    {
        super(zipFile, archiveName);
        zipfileToo = zipFile;
    }

    @Override
    ZipEntry getEntry(String entryName)
    {
        return zipfileToo.getEntry("Lib/" + entryName);
    }
}
