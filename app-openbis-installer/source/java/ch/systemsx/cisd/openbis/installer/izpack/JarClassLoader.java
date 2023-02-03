/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.installer.izpack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;

/**
 * Class loader based on a JAR file or directory of JAR files
 *
 * @author Franz-Josef Elmer
 */
public class JarClassLoader extends ClassLoader
{
    private final List<JarFile> jarFiles = new ArrayList<JarFile>();

    private final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

    public JarClassLoader(File jarFileOrFolder)
    {
        super(JarClassLoader.class.getClassLoader());
        addJarFile(jarFileOrFolder);
        if (jarFileOrFolder.isDirectory())
        {
            File[] files = jarFileOrFolder.listFiles();
            for (File file : files)
            {
                addJarFile(file);
            }
        }
    }

    private void addJarFile(File file)
    {
        if (file.isFile() && file.getName().endsWith(".jar"))
        {
            try
            {
                jarFiles.add(new JarFile(file));
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException
    {
        return findClass(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        Class<?> clazz = classes.get(name);
        if (clazz != null)
        {
            return clazz;
        }
        for (JarFile jarFile : jarFiles)
        {
            JarEntry jarEntry = jarFile.getJarEntry(name.replace('.', '/') + ".class");
            if (jarEntry != null)
            {
                InputStream inputStream = null;
                ByteArrayOutputStream outputStream = null;
                try
                {
                    inputStream = jarFile.getInputStream(jarEntry);
                    outputStream = new ByteArrayOutputStream();
                    int b = inputStream.read();
                    while (b >= 0)
                    {
                        outputStream.write(b);
                        b = inputStream.read();
                    }
                    byte[] bytes = outputStream.toByteArray();
                    clazz = defineClass(name, bytes, 0, bytes.length, null);
                    classes.put(name, clazz);
                    return clazz;
                } catch (Exception ex)
                {
                    // silently ignored, try next
                } finally
                {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }
        }
        try
        {
            Class<?> systemClass = findSystemClass(name);
            return systemClass;
        } catch (ClassNotFoundException e)
        {
            return null;
        }
    }

}
