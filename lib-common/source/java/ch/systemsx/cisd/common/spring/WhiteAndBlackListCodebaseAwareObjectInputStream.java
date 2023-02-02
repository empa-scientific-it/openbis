/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.spring;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamClass;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.remoting.rmi.CodebaseAwareObjectInputStream;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;

/**
 * @author Franz-Josef Elmer
 */
public class WhiteAndBlackListCodebaseAwareObjectInputStream extends CodebaseAwareObjectInputStream
{
    public static final String WHITE_LIST = "allowed-api-parameter-classes";

    public static final String BLACK_LIST = "disallowed-api-parameter-classes";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            WhiteAndBlackListCodebaseAwareObjectInputStream.class);

    private static final Patterns whiteListPatterns = new Patterns();

    private static final Patterns blackListPatterns = new Patterns();

    static
    {
        whiteListPatterns.addPattern("char");
        whiteListPatterns.addPattern("\\[C");
        whiteListPatterns.addPattern("byte");
        whiteListPatterns.addPattern("\\[B");
        whiteListPatterns.addPattern("short");
        whiteListPatterns.addPattern("\\[S");
        whiteListPatterns.addPattern("int");
        whiteListPatterns.addPattern("\\[I");
        whiteListPatterns.addPattern("long");
        whiteListPatterns.addPattern("\\[J");
        whiteListPatterns.addPattern("float");
        whiteListPatterns.addPattern("\\[F");
        whiteListPatterns.addPattern("double");
        whiteListPatterns.addPattern("\\[D");
        whiteListPatterns.addPattern("boolean");
        whiteListPatterns.addPattern("\\[Z");
        whiteListPatterns.addPattern("org\\.springframework\\.remoting\\.support\\.RemoteInvocation");
        whiteListPatterns.addPattern("com\\.marathon\\.util\\.spring\\.StreamSupportingRemoteInvocation");
        whiteListPatterns.addPattern("com\\.marathon\\.util\\.spring\\.RemoteInvocationDecorator");
        whiteListPatterns.addPattern("java\\..*");
        whiteListPatterns.addPattern("org\\.python\\.core\\..*");
        whiteListPatterns.addPattern("org\\.python27\\.core\\..*");
        whiteListPatterns.addPattern("ch\\.ethz\\.sis\\..*");
        whiteListPatterns.addPattern("ch\\.systemsx\\.cisd\\..*");
        whiteListPatterns.addPattern("ch\\.systemsx\\.sybit\\.imageviewer\\..*");

        blackListPatterns.addPattern("org\\.apache\\.commons\\.collections\\.functors\\.InvokerTransformer");
        blackListPatterns.addPattern("org\\.python\\.core\\.PyClass.*");
        blackListPatterns.addPattern("org\\.python27\\.core\\.PyClass.*");
        logPatterns();
    }

    private static void addToWhiteListPatterns(String regex)
    {
        whiteListPatterns.addPattern(regex);
    }

    private static void addToBlackListPatterns(String regex)
    {
        blackListPatterns.addPattern(regex);
    }

    public static void populateWhiteAndBlackListOfApiParameterClasses(Properties properties)
    {
        boolean patternsAdded = false;
        for (String pattern : PropertyUtils.getList(properties, WHITE_LIST))
        {
            patternsAdded = true;
            addToWhiteListPatterns(pattern);
        }
        for (String pattern : PropertyUtils.getList(properties, BLACK_LIST))
        {
            patternsAdded = true;
            addToBlackListPatterns(pattern);
        }
        if (patternsAdded)
        {
            logPatterns();
        }
    }

    public static void logPatterns()
    {
        operationLog.info("Allowed API parameter classes: " + whiteListPatterns);
        operationLog.info("Disallowed API parameter classes: " + blackListPatterns);
    }

    public WhiteAndBlackListCodebaseAwareObjectInputStream(InputStream in, ClassLoader classLoader, boolean acceptProxyClasses) throws IOException
    {
        super(in, classLoader, acceptProxyClasses);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass classDesc) throws IOException, ClassNotFoundException
    {
        String className = classDesc.getName();
        assertMatchingClassName(className);
        return super.resolveClass(classDesc);
    }

    private void assertMatchingClassName(String className) throws ClassNotFoundException
    {
        if (className.startsWith("[L") && className.endsWith(";"))
        {
            assertMatchingClassName(className.substring(2, className.length() - 1));
        } else
        {
            if (whiteListPatterns.matches(className) == false || blackListPatterns.matches(className))
            {
                operationLog.error("Attempt to load class " + className);
                throw new IllegalArgumentException("Class not allowed to load: " + className);
            }
        }
    }

    private static final class Patterns
    {
        private final Set<String> patternsAsStrings = new TreeSet<>();

        private final List<Pattern> patterns = new LinkedList<>();

        synchronized void addPattern(String pattern)
        {
            if (patternsAsStrings.contains(pattern) == false)
            {
                patterns.add(Pattern.compile(pattern));
                patternsAsStrings.add(pattern);
            }
        }

        synchronized boolean matches(String string)
        {
            for (Pattern pattern : patterns)
            {
                if (pattern.matcher(string).matches())
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString()
        {
            return patternsAsStrings.toString();
        }
    }
}
