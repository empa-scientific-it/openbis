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

package ch.systemsx.cisd.common.test;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

import classycle.Analyser;
import classycle.dependency.DefaultResultRenderer;
import classycle.dependency.DependencyChecker;
import classycle.util.NotStringPattern;
import classycle.util.OrStringPattern;
import classycle.util.WildCardPattern;

/**
 * Unit test checking dependency definitions by Classycle. Dependency definition file is assumed to be <tt>resource/dependency-structure.dff</tt> .
 * 
 * @author Franz-Josef Elmer
 */
public class DependencyCheckingTest extends AssertJUnit
{
    private static final String PATH_TO_DEPENDENCY_STRUCTURE_DDF =
            "resource/dependency-structure.ddf";

    private static final String ECLIPSE_CLASSES_FOLDER = "targets/classes";

    private static final String ANT_CLASSES_FOLDER = "targets/ant/classes";

    private static final String GRADLE_3_CLASSES_FOLDER = "targets/gradle/classes/main";

    private static final String GRADLE_4_CLASSES_FOLDER = "targets/gradle/classes/java/main";

    private static final String[] CLASSES_FOLDERS = { GRADLE_4_CLASSES_FOLDER, GRADLE_3_CLASSES_FOLDER, ECLIPSE_CLASSES_FOLDER, ANT_CLASSES_FOLDER};

    @Test
    public void test()
    {
        OrStringPattern orPattern = new OrStringPattern();
        orPattern.appendPattern(new WildCardPattern("*Test"));
        orPattern.appendPattern(new WildCardPattern("*Test$*"));
        orPattern.appendPattern(new WildCardPattern("*TestCase"));
        orPattern.appendPattern(new WildCardPattern("*TestCase$*"));
        List<String> excludingClassesPatterns = getExcludingClassesPatterns();
        for (String pattern : excludingClassesPatterns)
        {
            orPattern.appendPattern(new WildCardPattern(pattern));
        }
        Analyser analyser =
                new Analyser(getClassPaths(), new NotStringPattern(orPattern), null, true);
        String dependencyDefinitions =
                FileUtilities.loadExactToString(new File(PATH_TO_DEPENDENCY_STRUCTURE_DDF));
        DependencyChecker dependencyChecker =
                new DependencyChecker(analyser, dependencyDefinitions, new HashMap<Object, Object>(),
                        new DefaultResultRenderer());
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        boolean ok = dependencyChecker.check(printWriter);
        printWriter.flush();
        printWriter.close();
        assertTrue(writer.toString(), ok);
    }

    private String[] getClassPaths()
    {
        for (int i = 0; i < CLASSES_FOLDERS.length; i++) {
            if (new File(CLASSES_FOLDERS[i]).isDirectory()) {
                return new String[] { CLASSES_FOLDERS[i] };
            }
        }
        throw new RuntimeException("Class path not found");
    }

    /**
     * Returns the relative path to the class files compiled by Eclipse.
     */
    protected String getPathToClassesCompiledByEclipse()
    {
        return ECLIPSE_CLASSES_FOLDER;
    }

    /**
     * Returns a list of wild-card patterns of class files excluded from the analysis. Note, that <tt>*Test</tt>, <tt>*Test$*</tt>, <tt>*TestCase</tt>
     * , and <tt>*TestCase$*</tt> are already excluded.
     * <p>
     * This implementation returns nothing. Subclasses can override it for excluding e.g. helper classes in <tt>sourceTest/java</tt>
     */
    protected List<String> getExcludingClassesPatterns()
    {
        return Collections.emptyList();
    }
}
