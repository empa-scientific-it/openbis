/*
 * Copyright 2007 - 2018 ETH Zuerich, CISD and SIS.
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

package ch.systemsx.cisd.base.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A utility class for loading native shared libraries (following the JNI specification). A native shared library is identified by a name. The process
 * of loading the shared library is performed by calling {@link #loadNativeLibrary(String)}.
 * <p>
 * A native shared library can be provided to your program either as a file on the file system or as a resource in a jar file that is on the classpath
 * of your program. In the later case the resources will be copied to a file before being loaded. This utility class tries the following approaches
 * (in this order):
 * <ol>
 * <li>Use a specific Java property <code>native.libpath.&lt;libname&gt;</code> for each library <code>&lt;libname&gt;</code>. The name of the library
 * needs to be fully specified, e.g.
 * 
 * <pre>
 * -Dnative.libpath.&lt;libname&gt;=/home/joe/java/native/&lt;some_shared_library_file&gt;
 * </pre>
 * 
 * <i>The file has to exist or else {@link #loadNativeLibrary(String)} will throw an exception.</i></li>
 * <li>Use a naming schema that looks for a path to the library which is compatible with the platform the program is running on (as determined by
 * {@link OSUtilities #getCompatibleComputerPlatform()}). The root of the hierarchy is given by the property
 * 
 * <pre>
 * -Dnative.libpath=/home/joe/java/native/
 * </pre>
 * 
 * A real world example is the native library <code>nativedata</code> which has the hierarchy:
 * 
 * <pre>
 *    nativedata
 *       nativedata/arm-Linux
 *          nativedata/arm-Linux/libnativedata.so
 *       nativedata/i386-Linux
 *          nativedata/i386-Linux/libnativedata.so
 *       nativedata/amd64-Linux
 *          nativedata/amd64-Linux/libnativedata.so
 *       nativedata/i386-Mac OS X
 *          nativedata/i386-Mac OS X/libnativedata.jnilib
 *       nativedata/x86_64-Mac OS X
 *          nativedata/x86_64-Mac OS X/libnativedata.jnilib
 *       nativedata/x86-SunOS
 *          nativedata/x86-SunOS/libnativedata.so
 *       nativedata/amd64-SunOS
 *          nativedata/amd64-SunOS/libnativedata.so
 *       nativedata/sparc-SunOS
 *          nativedata/sparc-SunOS/libnativedata.so
 *       nativedata/sparcv9-SunOS
 *          nativedata/sparcv9-SunOS/libnativedata.so
 *       nativedata/x86-Windows
 *          nativedata/x86-Windows/nativedata.dll
 *       nativedata/amd64-Windows
 *          nativedata/amd64-Windows/nativedata.dll
 * </pre>
 * 
 * <i>The file has to exist or else {@link #loadNativeLibrary(String)} will throw an exception.</i>
 * <li>If you use the property <code>native.caching.libpath.&lt;libname&gt;</code> instead of <code>native.libpath.&lt;libname&gt;</code>, then you
 * will get the behavior of 1, but the library file on the file system will be considered a cache of a resource inside one of the jar files in the
 * class path. Inside of the jar file, the resources need ot have the structure as explained in 2.
 * <p>
 * <i>The file does not have to exist as it will be unpacked from the appropriate jar file resource.</i></li>
 * <li>If you use the property <code>native.caching.libpath</code> instead of <code>native.libpath</code>, then you will get the behavior of 2, but
 * the library files on the file system will all be considered a cache of a resource inside one of the jar files in the class path with the same
 * hierarchical structure.
 * <p>
 * <i>The file does not have to exist as it will be unpacked from the appropriate jar file resource.</i></li>
 * <li>If you do not set any of the properties desribed above when starting up your program, but you happen to have a hierarchical directory structure
 * as explained in 2 in one of the jar files of your class path, then {@link #loadNativeLibrary(String)} will unpack the appopriate shared library
 * into a temporary directory and load it from there. The temporary file will be deleted at shutdown of the program (except on Microsoft Windows where
 * mandatory locks make this impossible). This is the 'auto mode' and thus the simplest way of using this utility class for the user of your
 * program.</li>
 * <li>Finally, if no appropriate structure is found in the classpath, {@link #loadNativeLibrary(String)} will fall back to the Java default method of
 * loading JNI libraries via {@link System#loadLibrary(String)}. This may require the Java property <code>java.library.path</code> to be set and it may
 * require the library to follow a platform specific naming convention for the native shared library file.</li>
 * </ol>
 * 
 * @author Bernd Rinn
 */
public final class NativeLibraryUtilities
{
    private static final String JNI_LIB_PREFIX = getJNILibPrefixForSystem();

    private static final String JNI_LIB_EXTENSION = getJNILibExtensionForSystem();

    private static String getJNILibPrefixForSystem()
    {
        return OSUtilities.isWindows() ? "" : "lib";
    }

    private static String getJNILibExtensionForSystem()
    {
        if (OSUtilities.isMacOS())
        {
            return "jnilib";
        } else if (OSUtilities.isWindows())
        {
            return "dll";
        } else
        {
            return "so";
        }
    }

    /**
     * Loads the native library <var>libraryName</var>. The native library will be searched for in the way described in
     * {@link NativeLibraryUtilities}.
     * 
     * @param libraryName The name of the native library to be loaded.
     * @return <code>true</code> if the library has been loaded successfully and <code>false</code> otherwise.
     */
    public static boolean loadNativeLibrary(final String libraryName)
    {
        final boolean verbose = Boolean.getBoolean("native.libpath.verbose");
        try
        {
            // Try specific path
            String linkLibNameOrNull = System.getProperty("native.libpath." + libraryName);
            if (linkLibNameOrNull != null)
            {
                return loadLib(libraryName, toRealPath(linkLibNameOrNull, verbose), verbose);
            }

            // Try generic path
            String linkLibPathOrNull = System.getProperty("native.libpath");
            if (linkLibPathOrNull != null)
            {
                linkLibNameOrNull = getLibPath(linkLibPathOrNull, libraryName);
                return loadLib(libraryName, toRealPath(linkLibNameOrNull, verbose), verbose);
            }

            // Try specific caching path
            linkLibNameOrNull = System.getProperty("native.caching.libpath." + libraryName);
            if (linkLibNameOrNull != null)
            {
                final Path libraryPath = toRealPath(linkLibNameOrNull, verbose);
                tryCheckPerformUpdate(libraryName, libraryPath, verbose);
                return loadLib(libraryName, libraryPath, verbose);
            }

            // Try generic caching path
            linkLibPathOrNull = System.getProperty("native.caching.libpath");
            if (linkLibPathOrNull != null)
            {
                linkLibNameOrNull = getLibPath(linkLibPathOrNull, libraryName);
                final Path libraryPath = toRealPath(linkLibNameOrNull, verbose);
                tryCheckPerformUpdate(libraryName, libraryPath, verbose);
                return loadLib(libraryName, libraryPath, verbose);
            }

            // Try to resource to a temp file.
            linkLibNameOrNull = tryCopyNativeLibraryToTempFile(libraryName, verbose);
            if (linkLibNameOrNull != null)
            {
                return loadLib(libraryName, toRealPath(linkLibNameOrNull, verbose), verbose);
            }

            // Finally, try system dependent loading
            return loadSystemLibrary(libraryName);
        } catch (Exception e)
        {
            if (verbose)
            {
                System.err.printf("[native.libpath] FAILURE to load native library '%s'.\n", libraryName);
                e.printStackTrace();
            }
            return false;
        }
    }

    private static Path toRealPath(String filePath, boolean verbose) throws IOException
    {
        final Path p = Paths.get(filePath).toAbsolutePath();
        if (Files.exists(p))
        {
            return p.toRealPath();
        }
        checkCreateMissingPathElement(p, verbose);
        final Path parent = p.getParent();
        if (Files.exists(parent) && Files.isDirectory(parent))
        {
            return Paths.get(parent.toRealPath().toString(), p.getFileName().toString());
        }
        if (verbose)
        {
            System.err.printf("[native.libpath] Creating missing path elements on '%s'\n", parent);
        }
        return Paths.get(Files.createDirectories(parent).toString(), p.getFileName().toString());
    }

    private static void checkCreateMissingPathElement(final Path p, boolean verbose) throws IOException
    {
        final LinkedList<Path> pathList = new LinkedList<>();
        Path pp = p;
        while ((pp = pp.getParent()).getNameCount() > 0)
        {
            pathList.add(pp);
        }
        final Iterator<Path> it = pathList.descendingIterator();
        while (it.hasNext())
        {
            pp = it.next();
            if (Files.exists(pp) == false && Files.isSymbolicLink(pp))
            {
                final Path resolved = Files.readSymbolicLink(pp);
                if (verbose)
                {
                    System.err.printf("[native.libpath] Creating directory on resolved symbolic link '%s'\n", resolved);
                }
                Files.createDirectory(resolved);
                break;
            }
        }
    }

    private static boolean tryCheckPerformUpdate(String libraryName, Path linkLibPath, boolean verbose)
    {
        try
        {
            checkPerformUpdate(libraryName, linkLibPath, verbose);
            return true;
        } catch (Exception ex)
        {
            if (verbose)
            {
                System.err.printf("[native.libpath] FAILURE trying to check on whether to perform an update on library '%s' (path '%s').\n", libraryName, linkLibPath);
                ex.printStackTrace();
            }
            return false;
        }
    }

    private static void checkPerformUpdate(String libraryName, Path linkLibPath, boolean verbose) throws Exception
    {
        final boolean exists = Files.exists(linkLibPath);
        final boolean canWrite = Files.isWritable(exists ? linkLibPath : linkLibPath.getParent());
        if (canWrite == false)
        {
            if (verbose)
            {
                System.err.printf("[native.libpath] Skipping update check as '%s' is not writeable.\n", linkLibPath);
            }
            return;
        }
        final String libPathInJarfile = getLibPath("/native", libraryName);
        if (exists)
        {
            if (needsUpdate(libraryName, libPathInJarfile, linkLibPath, verbose))
            {
                performUpdate(libraryName, libPathInJarfile, linkLibPath, verbose);
            }
        } else
        {
            performUpdate(libraryName, libPathInJarfile, linkLibPath, verbose);
        }
    }

    private static boolean needsUpdate(String libName, String libPathInJarfile, Path linkLibPath, boolean verbose) throws Exception
    {
        final URL url = ResourceUtilities.class.getResource(libPathInJarfile);
        final URI uri = (url != null) ? url.toURI() : null;
        if (uri == null)
        {
            throw new IllegalArgumentException("Resource '" + libPathInJarfile + "' cannot be resolved to an URI.");
        }
        final String[] array = uri.toString().split("!");
        if (array.length == 2)
        {
            if (array[0].startsWith("jar:file:"))
            {
                final String jarFileName = array[0].substring("jar:file:".length());
                final String jarResourceName = array[1].substring(1);
                try (final ZipFile zipFile = new ZipFile(jarFileName))
                {
                    final Enumeration<? extends ZipEntry> entries = zipFile.entries();

                    while (entries.hasMoreElements())
                    {
                        ZipEntry entry = entries.nextElement();
                        if (jarResourceName.equals(entry.getName()))
                        {
                            final long size = entry.getSize();
                            final long crc32 = entry.getCrc();
                            final long[] fileStats = getSizeAnCrc32ForPath(linkLibPath);
                            final boolean needsUpdate = size != fileStats[0] || crc32 != fileStats[1];
                            if (verbose)
                            {
                                if (needsUpdate)
                                {
                                    System.err.printf(
                                            "[native.libpath] Native library '%s' needs update: jar resource '%s' (size=%d, crc32=%h) changed compared to file '%s' (size=%d, crc32=%h).\n",
                                            libName, entry.getName(), size, crc32, linkLibPath, fileStats[0], fileStats[1]);
                                } else
                                {
                                    System.err.printf("[native.libpath] Native library '%s' unchanged (file '%s', jar resource '%s').\n",
                                            libName, linkLibPath, entry.getName());

                                }
                            }
                            return needsUpdate;
                        }
                    }
                    throw new NoSuchFileException("Resource '" + jarResourceName + "' not in jar file '" + jarFileName + "'");
                }
            }
        }
        throw new IllegalArgumentException("URI " + uri.toString() + " cannot be resolved to resource in jar file.");
    }

    private static void performUpdate(String libName, String libPathInJarfile, Path linkLibPath, boolean verbose) throws IOException
    {
        if (verbose)
        {
            System.err.printf("[native.libpath] Updating library '%s': refresh file '%s' from jar resource '%s'.\n", libName, linkLibPath,
                    libPathInJarfile);
        }
        try (final RandomAccessFile randomAccessFile = new RandomAccessFile(linkLibPath.toFile(), "rw"))
        {
            // Get an exclusive lock so we are not interfering with other processes trying to do the same.
            randomAccessFile.getChannel().lock(0, 1, false);
            ResourceUtilities.tryCopyResourceToFile(libPathInJarfile, linkLibPath, randomAccessFile, verbose, "[native.libpath] ");
        }
    }

    private static long[] getSizeAnCrc32ForPath(Path linkLibPath) throws IOException
    {
        return new long[] { Files.size(linkLibPath), crc32(linkLibPath) };
    }

    private static long crc32(Path path) throws IOException
    {
        try (final InputStream in = new FileInputStream(path.toFile()))
        {
            final CRC32 crcMaker = new CRC32();
            byte[] buffer = new byte[16 * 1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1)
            {
                crcMaker.update(buffer, 0, bytesRead);
            }
            return crcMaker.getValue();
        }
    }

    private static boolean loadLib(String libName, Path libPath, boolean verbose)
    {
        if (verbose)
        {
            System.err.printf("[native.libpath] Loading native library '%s' from file '%s'.\n", libName, libPath);
        }
        final File linkLib = libPath.toFile();
        if (linkLib.exists() && linkLib.canRead() && linkLib.isFile())
        {
            final String linkLibNameAbsolute = linkLib.getAbsolutePath();
            try
            {
                try (final RandomAccessFile randomAccessFile = new RandomAccessFile(linkLib, "r"))
                {
                    // Get a shared lock so other processes do not interfere with us when they try to write to this file.
                    randomAccessFile.getChannel().lock(0, 1, true);
                    System.load(linkLibNameAbsolute);
                    return true;
                }
            } catch (final Throwable err)
            {
                if (verbose)
                {
                    System.err.printf("[native.libpath] FAILURE loading native library '%s'.\n", linkLibNameAbsolute);
                    err.printStackTrace();
                }
                return false;
            }
        } else
        {
            if (verbose)
            {
                System.err.printf("[native.libpath] FAILURE as native library '%s' does not exist or is not readable.\n", linkLib
                        .getAbsolutePath());
            }
            return false;
        }
    }

    private static boolean loadSystemLibrary(String libName)
    {
        try
        {
            System.loadLibrary(libName);
            return true;
        } catch (Throwable th)
        {
            // Silence this - we return failure back as boolean value.
            return false;
        }
    }

    /**
     * Tries to copy a native library which is available as a resource to a temporary file. It will use the following naming schema to locate the
     * resource containing the native library:
     * <p>
     * {@code /native/<libraryName>/<platform_id>/<libraryName>.so}.
     * 
     * @param libraryName The name of the library.
     * @param verbose If <code>true</code>, print error to <code>stderr</code> if copying fails.
     * @return The name of the temporary file, or <code>null</code>, if the resource could not be copied.
     */
    public static String tryCopyNativeLibraryToTempFile(final String libraryName, final boolean verbose)
    {
        // Request clean-up of old native library temp files as under Windows the files are locked and
        // cannot be deleted on regular shutdown.
        return ResourceUtilities.tryCopyResourceToTempFile(getLibPath("/native", libraryName),
                libraryName, ".so", true, verbose, "[native.libpath] ");
    }

    private static String getLibPath(final String prefix, final String libraryName)
    {
        return String.format("%s/%s/%s/%s%s.%s", prefix, libraryName, OSUtilities
                .getCompatibleComputerPlatform(), JNI_LIB_PREFIX, libraryName, JNI_LIB_EXTENSION);
    }

}
