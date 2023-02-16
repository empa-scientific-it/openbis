/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.importer.handler;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JSONHandler
{
    public static Map<String, String> parseMetaData(String metaData)
    {
        Map<String, String> map = new HashMap<>();
        try
        {
            JsonParser parser = new JsonFactory().createParser(metaData);
            parser.nextToken();

            while (parser.nextToken() != JsonToken.END_OBJECT)
            {
                String name = parser.getText();
                parser.nextToken();
                String value = parser.getText();
                map.put(name, value);
            }

        } catch (IOException e)
        {
            throw new UserFailureException("Wrong json: " + e.getMessage());
        }
        return map;
    }

    public static Map<String, Map<String, Integer>> parseVersionDataFile(String versions)
    {
        Map<String, Map<String, Integer>> mainMap = new HashMap<>();
        try
        {
            JsonParser parser = new JsonFactory().createParser(versions);
            parser.nextToken();

            while (parser.nextToken() != JsonToken.END_OBJECT)
            {
                String xlsName = parser.getText();
                if (parser.nextToken() != JsonToken.START_OBJECT)
                {
                    throw new UserFailureException("Wrong structure of version file. Expect '{', got '" + parser.getText() + "'.");
                }
                Map<String, Integer> xlsMap = new HashMap<>();
                while (parser.nextToken() != JsonToken.END_OBJECT)
                {
                    String name = parser.getText();
                    parser.nextToken();
                    int value = parser.getIntValue();
                    xlsMap.put(name, value);
                }
                mainMap.put(xlsName, xlsMap);
            }

        } catch (IOException e)
        {
            throw new UserFailureException("Wrong json: " + e.getMessage());
        }
        return mainMap;
    }

    public static void writeVersionDataFile(Map<String, Map<String, Integer>> versions, String path)
    {
        JsonFactory factory = new JsonFactory();
        try
        {
            JsonGenerator generator = factory.createGenerator(new File(path), JsonEncoding.UTF8);
            generator.useDefaultPrettyPrinter();
            generator.writeStartObject();
            for (String xlsName : versions.keySet())
            {
                Map<String, Integer> typeVersions = versions.get(xlsName);
                generator.writeFieldName(xlsName);
                generator.writeStartObject();
                for (String key : typeVersions.keySet())
                {
                    generator.writeFieldName(key);
                    generator.writeNumber(typeVersions.get(key));
                }
                generator.writeEndObject();
            }
            generator.writeEndObject();
            generator.close();
        } catch (IOException e)
        {
            throw new UserFailureException("Can't generate json: " + e.getMessage());
        }
    }
}
