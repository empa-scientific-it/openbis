/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.imagereaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.imagereaders.bioformats.BioFormatsReaderLibrary;
import ch.systemsx.cisd.imagereaders.ij.ImageJReaderLibrary;
import ch.systemsx.cisd.imagereaders.imageio.ImageIOReaderLibrary;

/**
 * A class that facilitates unit testing with JDK 1.5.
 * 
 * @author Kaloyan Enimanev
 */
public class ImageReadersTestHelper
{

    private static final String SERVICES_FILE_TEMPLATE =
            "./resource/manifest/%s/META-INF/services/" + IImageReaderLibrary.class.getName();

    // NOTE : we *cannot* put real classes/instances here, because we do not know if all
    // libraries jar files will be on the classpath.
    private static final Map<String/* library name */, String /* library classname */> librariesByName =
            new HashMap<String, String>();

    static
    {
        librariesByName.put(ImageReaderConstants.IMAGEIO_LIBRARY,
                "ch.systemsx.cisd.imagereaders.imageio.ImageIOReaderLibrary");
        librariesByName.put(ImageReaderConstants.IMAGEJ_LIBRARY,
                "ch.systemsx.cisd.imagereaders.ij.ImageJReaderLibrary");
        librariesByName.put(ImageReaderConstants.BIOFORMATS_LIBRARY,
                "ch.systemsx.cisd.imagereaders.bioformats.BioFormatsReaderLibrary");
    }

    /**
     * Use this method to prepare the libraries from within a unit test.
     */
    public static void setUpLibraries(String... libraryNames) throws Exception
    {
        ArrayList<IImageReaderLibrary> libs = new ArrayList<IImageReaderLibrary>();
        for (String libName : libraryNames)
        {
            IImageReaderLibrary library = getLibraryFromMap(libName);
            libs.add(library);
        }

        ImageReaderFactory.setLibraries(libs);
    }

    /**
     * Use this method to prepare the libraries from within a unit test.
     */
    public static void setUpLibrariesFromManifest(String... libraryNames) throws Exception
    {
        ArrayList<IImageReaderLibrary> libs = new ArrayList<IImageReaderLibrary>();
        for (String libName : libraryNames)
        {
            IImageReaderLibrary library = getLibraryFromManifest(libName);
            libs.add(library);
        }

        ImageReaderFactory.setLibraries(libs);
    }

    private static IImageReaderLibrary getLibraryFromMap(String library) throws Exception
    {
        String libClassName = librariesByName.get(library);
        if (libClassName == null)
        {
            throw new IllegalArgumentException("Unknown library name :" + library);
        }
        return (IImageReaderLibrary) Class.forName(libClassName).newInstance();
    }

    private static IImageReaderLibrary getLibraryFromManifest(String library) throws Exception
    {
        String libClassName = readLibraryClassName(library);
        return (IImageReaderLibrary) Class.forName(libClassName).newInstance();
    }

    private static String readLibraryClassName(String library) throws IOException
    {
        String servicesFileName = String.format(SERVICES_FILE_TEMPLATE, library);
        File servicesFile = new File(servicesFileName);
        if (servicesFile.exists() == false)
        {
            throw new IllegalArgumentException("No service loader file definition for library "
                    + library);
        }
        String fileContent = FileUtils.readFileToString(servicesFile, "UTF-8");

        if (StringUtils.isBlank(fileContent))
        {
            String error =
                    String.format("Cannot read class name for library '%s' from '%s'", library,
                            servicesFileName);
            throw new IllegalArgumentException(error);

        }

        String className = fileContent.trim();
        return className;
    }

    public static void main(String[] args)
    {
        printReaders(new ImageJReaderLibrary());
        printReaders(new ImageIOReaderLibrary());
        printReaders(new BioFormatsReaderLibrary());
    }

    private static void printReaders(IImageReaderLibrary library)
    {
        System.out.println(library.getName() + ": " + library.getReaderNames());
    }

}
