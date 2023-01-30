/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.uid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author anttil
 */
public class DictionaryUidGenerator implements UidGenerator
{
    private List<String> tokens;

    public DictionaryUidGenerator(File file) throws FileNotFoundException, IOException
    {
        tokens = new ArrayList<String>();
        BufferedReader in = new BufferedReader(new FileReader(file));
        try
        {
            String line;
            while ((line = in.readLine()) != null)
            {
                tokens.add(line);
            }
        } finally
        {
            in.close();
        }
    }

    @Override
    public String uid()
    {
        String uid = tokens.get((int) (Math.random() * tokens.size())) + "-" + UUID.randomUUID();
        uid = uid.toUpperCase();
        return uid;
    }

}
