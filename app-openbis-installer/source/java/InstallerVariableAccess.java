
/*
 * Copyright 2019 ETH Zuerich, SIS
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
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 
 * Helper application to get values of installer variables. Usage:
 * <pre>
 * java InstallerVariableAccess &lt;installer jar&gt; &lt;variable 1&gt; &lt;variable 2&gt; ...
 * </pre>
 * The console output has the values line by line.
 * 
 * @author Franz-Josef Elmer
 *
 */
public class InstallerVariableAccess
{
    public static void main(String[] args) throws Exception
    {
        ZipFile zipFile = new ZipFile(new File(args[0]));
        ZipEntry entry = zipFile.getEntry("resources/vars");
        
        InputStream fstream = null;
        ObjectInputStream oistream = null;
        try
        {
            fstream = zipFile.getInputStream(entry);
            oistream = new ObjectInputStream(fstream);
            Properties properties = (Properties) oistream.readObject();
            for (int i = 1; i < args.length; i++)
            {
                System.out.println(properties.get(args[i]));
            }
        } finally
        {
            if (fstream != null)
            {
                fstream.close();
            }
            if (oistream != null)
            {
                oistream.close();
            }
            zipFile.close();
        }
    }
}
