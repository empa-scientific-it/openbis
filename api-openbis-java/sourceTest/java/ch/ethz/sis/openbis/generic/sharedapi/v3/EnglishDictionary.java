/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.sharedapi.v3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author pkupczyk
 */
public class EnglishDictionary
{

    private static EnglishDictionary instance;

    private Set<String> words;

    private EnglishDictionary(Set<String> words)
    {
        this.words = words;
    }

    public boolean contains(String word)
    {
        return words.contains(word);
    }

    public static synchronized final EnglishDictionary getInstance()
    {
        if (instance == null)
        {
            try
            {
                InputStream stream = EnglishDictionary.class.getResourceAsStream("dictionary.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                Set<String> words = new HashSet<String>();
                String line = null;

                while ((line = reader.readLine()) != null)
                {
                    String trimmedLine = line.trim();
                    if (trimmedLine.length() > 0)
                    {
                        words.add(trimmedLine.replace(" ", ""));
                    }
                }

                instance = new EnglishDictionary(words);

            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        return instance;

    }
}
