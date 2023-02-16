/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.dss.api.v3;

import java.util.Random;

import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.create.DataSetFileCreation;

public class DataSetFileCreationBuilder
{
    private static Random rnd = new Random();

    private String path;

    private boolean isDirectory;

    private Long fileLength;

    private Integer checksumCRC32;

    private String checksum;

    private String checksumType;

    public DataSetFileCreationBuilder(boolean isDirectory)
    {
        if (isDirectory)
        {
            this.isDirectory = true;
        } else
        {
            this.isDirectory = false;
            this.fileLength = Long.valueOf(rnd.nextInt(1024 * 1024 * 1024));
            this.checksumCRC32 = rnd.nextInt();
        }
        this.path = randomPath();
    }

    public DataSetFileCreationBuilder withPath(String path)
    {
        this.path = path;
        return this;
    }

    public DataSetFileCreationBuilder withFileLength(Long fileLength)
    {
        this.fileLength = fileLength;
        return this;
    }

    public DataSetFileCreationBuilder withChecksum(Integer checksum)
    {
        this.checksumCRC32 = checksum;
        return this;
    }
    
    public DataSetFileCreationBuilder withChecksum(String checksum)
    {
        this.checksum = checksum;
        return this;
    }

    public DataSetFileCreationBuilder withChecksumType(String checksumType)
    {
        this.checksumType = checksumType;
        return this;
    }
    
    public DataSetFileCreation build()
    {
        DataSetFileCreation dsfc = new DataSetFileCreation();
        dsfc.setDirectory(isDirectory);
        dsfc.setPath(path);
        if (fileLength != null)
        {
            dsfc.setFileLength(fileLength);
        }
        dsfc.setChecksumCRC32(checksumCRC32);
        dsfc.setChecksum(checksum);
        dsfc.setChecksumType(checksumType);
        return dsfc;
    }

    private String randomPath()
    {
        String result = "";
        for (int i = 0; i < rnd.nextInt(15) + 1; i++)
        {
            result = "/" + randomString(rnd.nextInt(20) + 1);
        }
        return result.substring(1);
    }

    private static char[] CHARSET_AZ_09 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    public static String randomString(int length)
    {
        char[] result = new char[length];
        for (int i = 0; i < result.length; i++)
        {
            int randomCharIndex = rnd.nextInt(CHARSET_AZ_09.length);
            result[i] = CHARSET_AZ_09[randomCharIndex];
        }
        return new String(result);
    }
}
