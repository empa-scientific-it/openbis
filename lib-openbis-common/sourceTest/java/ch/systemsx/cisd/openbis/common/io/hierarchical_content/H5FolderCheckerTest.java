/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.common.io.hierarchical_content;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class H5FolderCheckerTest
{
    @DataProvider
    Object[][] handleForNoFlags()
    {
        return t(p("abc/def.h5", false), p("abc/def.h5ar", false), p("abc/def.txt", false));
    }
    
    @Test(dataProvider = "handleForNoFlags")
    public void testhandleHdf5AsFolderForNoFlags(String path, boolean handleAsFolder)
    {
        H5FolderChecker folderChecker = new H5FolderChecker(Arrays.asList());
        
        boolean result = folderChecker.handleHdf5AsFolder(path);
        
        assertEquals(result, handleAsFolder);
    }
    
    @Test(dataProvider = "handleForNoFlags")
    public void testhandleHdf5AsFolderByDefaultForNoFlags(String path, boolean handleAsFolder)
    {
        H5FolderChecker folderChecker = new H5FolderChecker(Arrays.asList());
        
        boolean result = folderChecker.handleHdf5AsFolderByDefault(path);
        
        assertEquals(result, handleAsFolder);
    }
    
    @DataProvider
    Object[][] handleForOneFlagH5FalseH5arFalse()
    {
        return t(p("abc/def.h5", false), p("b/def.h5", false), p("abc/def.h5ar", false), p("abc/def.txt", false));
    }
    
    @Test(dataProvider = "handleForOneFlagH5FalseH5arFalse")
    public void testhandleHdf5AsFolderForOneFlagH5FalseH5arFalse(String path, boolean handleAsFolder)
    {
        H5FolderChecker folderChecker = new H5FolderChecker(Arrays.asList(new H5FolderFlags("b", false, false)));
        
        boolean result = folderChecker.handleHdf5AsFolder(path);
        
        assertEquals(result, handleAsFolder);
    }
    
    @Test(dataProvider = "handleForOneFlagH5FalseH5arFalse")
    public void testhandleHdf5AsFolderByDefaultForOneFlagH5FalseH5arFalse(String path, boolean handleAsFolder)
    {
        H5FolderChecker folderChecker = new H5FolderChecker(Arrays.asList(new H5FolderFlags("b", false, false)));
        
        boolean result = folderChecker.handleHdf5AsFolderByDefault(path);
        
        assertEquals(result, handleAsFolder);
    }
    
    @DataProvider
    Object[][] handleForOneFlagH5TrueH5arFalse()
    {
        return t(p("abc/def.h5", true), p("b/def.h5", true), p("abc/def.h5ar", false), p("abc/def.txt", false));
    }
    
    @Test(dataProvider = "handleForOneFlagH5TrueH5arFalse")
    public void testhandleHdf5AsFolderForOneFlagH5TrueH5arFalse(String path, boolean handleAsFolder)
    {
        H5FolderChecker folderChecker = new H5FolderChecker(Arrays.asList(new H5FolderFlags("b", true, false)));
        
        boolean result = folderChecker.handleHdf5AsFolder(path);
        
        assertEquals(result, handleAsFolder);
    }
    
    @Test(dataProvider = "handleForOneFlagH5TrueH5arFalse")
    public void testhandleHdf5AsFolderByDefaultForOneFlagH5TrueH5arFalse(String path, boolean handleAsFolder)
    {
        H5FolderChecker folderChecker = new H5FolderChecker(Arrays.asList(new H5FolderFlags("b", true, false)));
        
        boolean result = folderChecker.handleHdf5AsFolderByDefault(path);
        
        assertEquals(result, handleAsFolder);
    }
    
    @DataProvider
    Object[][] handleForOneFlagH5FalseH5arTrue()
    {
        return t(p("abc/def.h5", false), p("b/def.h5", false), p("abc/def.h5ar", true), p("abc/def.txt", false));
    }
    
    @Test(dataProvider = "handleForOneFlagH5FalseH5arTrue")
    public void testhandleHdf5AsFolderForOneFlagH5FalseH5arTrue(String path, boolean handleAsFolder)
    {
        H5FolderChecker folderChecker = new H5FolderChecker(Arrays.asList(new H5FolderFlags("b", false, true)));
        
        boolean result = folderChecker.handleHdf5AsFolder(path);
        
        assertEquals(result, handleAsFolder);
    }
    
    @Test(dataProvider = "handleForOneFlagH5FalseH5arTrue")
    public void testhandleHdf5AsFolderByDefaultForOneFlagH5FalseH5arTrue(String path, boolean handleAsFolder)
    {
        H5FolderChecker folderChecker = new H5FolderChecker(Arrays.asList(new H5FolderFlags("b", false, true)));
        
        boolean result = folderChecker.handleHdf5AsFolderByDefault(path);
        
        assertEquals(result, handleAsFolder);
    }
    
    @DataProvider
    Object[][] handleForOneFlagH5TrueH5arTrue()
    {
        return t(p("abc/def.h5", true), p("b/def.h5", true), p("abc/def.h5ar", true), p("abc/def.txt", false));
    }
    
    @Test(dataProvider = "handleForOneFlagH5TrueH5arTrue")
    public void testhandleHdf5AsFolderForOneFlagH5TrueH5arTrue(String path, boolean handleAsFolder)
    {
        H5FolderChecker folderChecker = new H5FolderChecker(Arrays.asList(new H5FolderFlags("b", true, true)));
        
        boolean result = folderChecker.handleHdf5AsFolder(path);
        
        assertEquals(result, handleAsFolder);
    }
    
    @Test(dataProvider = "handleForOneFlagH5TrueH5arTrue")
    public void testhandleHdf5AsFolderByDefaultForOneFlagH5TrueH5arTrue(String path, boolean handleAsFolder)
    {
        H5FolderChecker folderChecker = new H5FolderChecker(Arrays.asList(new H5FolderFlags("b", true, true)));
        
        boolean result = folderChecker.handleHdf5AsFolderByDefault(path);
        
        assertEquals(result, handleAsFolder);
    }
    
    @DataProvider
    Object[][] handleForTwoFlagsH5TrueH5arFalseH5FalseH5arTrue()
    {
        return t(p("a/def.h5", true), p("abc/def.h5", false), p("a/def.h5ar", false), p("abc/def.h5ar", false),
                 p("a/def.txt", false), p("abc/def.txt", false),
                 p("b/def.h5", false), p("bcd/ef.h5", false), p("b/def.h5ar", true), p("bcd/ef.h5ar", false),
                 p("b/def.txt", false));
    }
    
    @Test(dataProvider = "handleForTwoFlagsH5TrueH5arFalseH5FalseH5arTrue")
    public void testhandleHdf5AsFolderTwoFlagsH5TrueH5arFalseH5FalseH5arTrue(String path, boolean handleAsFolder)
    {
        H5FolderChecker folderChecker = new H5FolderChecker(
                Arrays.asList(new H5FolderFlags("a", true, false), new H5FolderFlags("b", false, true)));
        
        boolean result = folderChecker.handleHdf5AsFolder(path);
        
        assertEquals(result, handleAsFolder);
    }
    
    @DataProvider
    Object[][] handleByDefaultForTwoFlagsH5TrueH5arFalseH5FalseH5arTrue()
    {
        return t(p("a/def.h5", false), p("abc/def.h5", false), p("a/def.h5ar", false), p("abc/def.h5ar", false),
                 p("a/def.txt", false), p("abc/def.txt", false),
                 p("b/def.h5", false), p("bcd/ef.h5", false), p("b/def.h5ar", false), p("bcd/ef.h5ar", false),
                 p("b/def.txt", false));
    }
    
    @Test(dataProvider = "handleByDefaultForTwoFlagsH5TrueH5arFalseH5FalseH5arTrue")
    public void testhandleHdf5AsFolderByDefaultTwoFlagsH5TrueH5arFalseH5FalseH5arTrue(String path, boolean handleAsFolder)
    {
        H5FolderChecker folderChecker = new H5FolderChecker(
                Arrays.asList(new H5FolderFlags("a", true, false), new H5FolderFlags("b", false, true)));
        
        boolean result = folderChecker.handleHdf5AsFolderByDefault(path);
        
        assertEquals(result, handleAsFolder);
    }
    
    private InputOutputPair p(String path, boolean isFolder)
    {
        return new InputOutputPair(path, isFolder);
    }
    
    private Object[][] t(InputOutputPair...pairs)
    {
        Object[][] result = new Object[pairs.length][];
        for (int i = 0; i < pairs.length; i++)
        {
            result[i] = new Object[] {pairs[i].path, pairs[i].isFolder};
        }
        return result;
    }

    private static final class InputOutputPair
    {
        private String path;
        private boolean isFolder;

        InputOutputPair(String path, boolean isFolder)
        {
            this.path = path;
            this.isFolder = isFolder;
        }
    }
}
