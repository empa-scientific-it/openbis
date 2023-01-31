/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import org.apache.log4j.Logger;

/**
 * Special class loader for classes based on jython 2.7. The jython JAR file has to be provided in the
 * constructor. 
 * <ul>
 * <li>This class loader always try first to find and load the class from the specified JAR file.
 * Exceptions are classes from packages starting with <tt>sun.</tt> or <tt>java</tt>.
 * <li>If it isn't found (or it is from these packages) and if its fully-qualified class name 
 * does not match <tt>*.v27.*</tt> the class is loaded by the application class loader. 
 * <li>A class not found in the provided JAR file and fully-qualified class name does match <tt>*.v27.*</tt>
 * are loaded with this class loader.
 * </ul>
 *
 * @author Franz-Josef Elmer
 */
public class Jython27ClassLoader extends ClassLoader
{
    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, Jython27ClassLoader.class);
    /*
     * Potentially all JDK Packages should be loaded using the Standard Class Loader
     * https://docs.oracle.com/en/java/javase/11/docs/api/allpackages-index.html
     */
    private static final List<String> EXCLUDED_PACKAGES_STARTS = Arrays.asList("java", "jdk.", "sun.", "com.sun.");
    private final URLClassLoader jythonJarClassLoader;
    private final Map<String, Class<?>> cachedClasses = new HashMap<>();

    public Jython27ClassLoader(File jythonJar)
    {
        if (jythonJar.exists() == false)
        {
            throw new EnvironmentFailureException("JAR file does not exist: " + jythonJar.getAbsolutePath());
        }
        if (jythonJar.isDirectory())
        {
            throw new EnvironmentFailureException("JAR file is a directory: " + jythonJar.getAbsolutePath());
        }
        try
        {
            jythonJarClassLoader = new URLClassLoader(new URL[] { jythonJar.toURI().toURL() }, null);
        } catch (MalformedURLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        Class<?> clazz = cachedClasses.get(name);
        if (clazz == null)
        {
            if (excludedPackageStart(name) == false)
            {
                clazz = tryLoadClass(jythonJarClassLoader, name);
            }
            if (clazz == null)
            {
                if (name.contains(".v27."))
                {
                    clazz = tryLoadClass(this, name);
                    if (clazz == null)
                    {
                        return super.loadClass(name, resolve);
                    }
                } else
                {
                    return super.loadClass(name, resolve);
                }
            }
        }
        if (resolve)
        {
            resolveClass(clazz);
        }
        cachedClasses.put(name, clazz);
        definePackage(name);
        return clazz;
    }
    
    private boolean excludedPackageStart(String className)
    {
        for (String packageStart : EXCLUDED_PACKAGES_STARTS)
        {
            if (className.startsWith(packageStart))
            {
                return true;
            }
        }
        return false;
    }
    
    private void definePackage(String className)
    {
        String packageName = getPackageName(className);
        if (getPackage(packageName) == null)
        {
            definePackage(packageName, null, null, null, null, null, null, null);
        }
    }
    
    private String getPackageName(String className)
    {
        int offset = className.lastIndexOf('.');
        return (offset == -1) ? null : className.substring(0, offset);
    }
    
    @Override
    public URL getResource(String name)
    {
        URL resource = jythonJarClassLoader.getResource(name);
        if (resource != null)
        {
            return resource;
        }
        return super.getResource(name);
    }

    @Override
    protected URL findResource(String name)
    {
        URL resource = jythonJarClassLoader.findResource(name);
        if (resource != null)
        {
            return resource;
        }
        return super.findResource(name);
    }
    
    @Override
    protected Enumeration<URL> findResources(String name) throws IOException
    {
        Enumeration<URL> resources = jythonJarClassLoader.findResources(name);
        if (resources != null)
        {
            return resources;
        }
        return super.findResources(name);
    }
    
    private Class<?> tryLoadClass(ClassLoader classLoader, String name)
    {
//        machineLog.info("tryLoadClass - classLoader: " + classLoader + " name: " + name);
        synchronized (getClassLoadingLock(name))
        {
            InputStream stream = classLoader.getResourceAsStream(name.replace('.', '/') + ".class");
            if (stream != null)
            {
                try
                {
                    byte[] bytes = IOUtils.toByteArray(stream);
                    return defineClass(name, bytes, 0, bytes.length);
                } catch (SecurityException ex)
                {
                    return null;
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                } finally
                {
                    IOUtils.closeQuietly(stream);
                }
            }
            return null;
        }
    }
}