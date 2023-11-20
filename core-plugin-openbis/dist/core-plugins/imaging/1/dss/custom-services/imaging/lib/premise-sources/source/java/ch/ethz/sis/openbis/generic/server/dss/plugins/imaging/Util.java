/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.dss.plugins.imaging;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.CRC32;

class Util
{
    private Util() {}

    private static ObjectMapper getObjectMapper()
    {
        return ServiceProvider.getObjectMapperV3();
    }

    static long getCRC32Checksum(InputStream input)
    {
        if (input == null)
            throw new UserFailureException("Can not compute crc32!");
        CRC32 crc = new CRC32();
        byte[] buffer = new byte[4096];
        try
        {
            while (true)
            {
                int length = input.read(buffer);
                if (length == -1)
                    break;
                crc.update(buffer, 0, length);
            }
        } catch (Exception ex)
        {
            try
            {
                input.close();
            } catch (Exception ignored)
            {
            }
        }
        return crc.getValue();
    }

    static <T> T readConfig(String val, Class<T> clazz)
    {
        try
        {
            ObjectMapper objectMapper = getObjectMapper();
            return objectMapper.readValue(new ByteArrayInputStream(val.getBytes()),
                    clazz);
        } catch (JsonMappingException mappingException)
        {
            throw new UserFailureException(mappingException.toString(), mappingException);
        } catch (Exception e)
        {
            throw new UserFailureException("Could not read the parameters!", e);
        }
    }

    static String mapToJson(Map<String, ?> map)
    {
        try
        {
            ObjectMapper objectMapper = getObjectMapper();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
        } catch (Exception e)
        {
            throw new UserFailureException("Could not serialize the input parameters!", e);
        }
    }



    static void archiveFiles(AbstractDataSetPackager packager, File rootFile, String rootFolderName,
            Function<InputStream, Long> checksumFunction)
    {
        Deque<Map.Entry<String, File>> queue = new LinkedList<>();

        queue.add(new AbstractMap.SimpleImmutableEntry<>(rootFolderName, rootFile));
        while (!queue.isEmpty())
        {
            Map.Entry<String, File> element = queue.pollFirst();
            String prefixPath = element.getKey();
            File file = element.getValue();
            String path = Paths.get(prefixPath, file.getName()).toString();
            if (file.isDirectory())
            {
                for (File f : file.listFiles())
                {
                    queue.add(new AbstractMap.SimpleImmutableEntry<>(path, f));
                }
                packager.addDirectoryEntry(path);
            } else
            {
                try
                {
                    FileInputStream fileStream = new FileInputStream(file);
                    packager.addEntry(path, file.lastModified(), file.getTotalSpace(),
                            checksumFunction.apply(fileStream), new FileInputStream(file));
                } catch (IOException exc)
                {
                    throw new UserFailureException("Failed during export!", exc);
                }
            }
        }

    }

}
