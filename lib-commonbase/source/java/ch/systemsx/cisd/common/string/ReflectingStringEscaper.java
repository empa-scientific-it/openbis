/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.string;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogUtils;
import ch.systemsx.cisd.common.reflection.BeanUtils;

/**
 * Performs HTML escaping the string fields of an object. If desired, users can restrict the fields that are escaped by the invoker. Support for
 * controlling the behavior of the escaper by using annotations may come in the future.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ReflectingStringEscaper
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ReflectingStringEscaper.class);

    private static int MIN_TIME_LOGGED_MS = 100;

    /**
     * Escape all the string fields on the bean.
     */
    public static <T> T escapeShallow(T bean)
    {
        ReflectingStringEscaperUnrestricted<T> escaper =
                new ReflectingStringEscaperUnrestricted<T>(false, bean);
        return escaper.escape();
    }

    /**
     * Like {@link #escapeDeep(Object)} but the original object is not changed. The result contains a deep copy of original bean with all the string
     * fields on the bean and all fields of objects referred to by the bean escaped.
     * <p>
     * NOTE: If serialization of the provided <code>bean</code> fails the result will not be escaped.
     */
    public static <T extends Serializable> T escapeDeepWithCopy(T bean)
    {
        try
        {
            long startTime = System.currentTimeMillis();
            T beanClone = BeanUtils.clone(bean);
            long cloneTime = System.currentTimeMillis();
            T result = doEscapeDeep(beanClone);
            long escapeTime = System.currentTimeMillis();
            long timeSpent = escapeTime - startTime;
            if (timeSpent >= MIN_TIME_LOGGED_MS)
            {
                operationLog.info((timeSpent) + "ms for escaping (cloning: "
                        + (cloneTime - startTime) + "ms) "
                        + (bean == null ? "" : bean.getClass().getSimpleName()));
            }
            return result;
        } catch (Exception ex)
        {
            // fail in development mode, in production mode return unescaped object
            LogUtils.logErrorWithFailingAssertion(operationLog, ex.toString());
            ex.printStackTrace();
            return bean;
        }
    }

    /**
     * Escape all the string fields on the bean and all fields of objects referred to by the bean.
     */
    public static <T> T escapeDeep(T bean)
    {
        try
        {
            long time = System.currentTimeMillis();
            T result = doEscapeDeep(bean);
            long timeSpent = System.currentTimeMillis() - time;
            if (timeSpent >= MIN_TIME_LOGGED_MS)
            {
                operationLog.info((timeSpent) + "ms for escaping "
                        + (bean == null ? "" : bean.getClass().getSimpleName()));
            }
            return result;
        } catch (Exception ex)
        {
            ex.printStackTrace();
            LogUtils.logErrorWithFailingAssertion(operationLog, ex.toString());
            return null;
        }
    }

    /**
     * Escape all the string fields on the bean and all fields of objects referred to by the bean.
     */
    private static <T> T doEscapeDeep(T bean)
    {
        ReflectingStringEscaperUnrestricted<T> escaper =
                new ReflectingStringEscaperUnrestricted<T>(true, bean);
        return escaper.escape();
    }

    /**
     * Escape the specified string fields on the bean.
     */
    public static <T> T escapeShallowRestricted(T bean, String... escapedProperties)
    {
        ReflectingStringEscaperRestricted<T> escaper =
                new ReflectingStringEscaperRestricted<T>(false, bean, escapedProperties);
        return escaper.escape();
    }

    /**
     * Escape all the string fields on the bean and all fields of objects referred to by the bean, restricting the field names to those specified.
     */
    public static <T> T escapeDeepRestricted(T bean, String... escapedProperties)
    {
        ReflectingStringEscaperRestricted<T> escaper =
                new ReflectingStringEscaperRestricted<T>(true, bean, escapedProperties);
        return escaper.escape();
    }
}

/**
 * @author Chandrasekhar Ramakrishnan
 */
class ReflectingStringEscaperImpl<T>
{

    protected final boolean isDeep;

    protected final T bean;

    protected ReflectingStringEscaperImpl(boolean isDeep, T bean)
    {
        this.isDeep = isDeep;
        this.bean = bean;
    }

    protected T traverse(ReflectionStringTraverser.ReflectionFieldVisitor visitor)
    {
        if (isDeep)
        {
            ReflectionStringTraverser.traverseDeep(bean, visitor);
        } else
        {
            ReflectionStringTraverser.traverseShallow(bean, visitor);
        }
        return bean;
    }

}

/**
 * Utility Class that preforms the restricted escaping.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class ReflectingStringEscaperRestricted<T> extends ReflectingStringEscaperImpl<T>
{
    private final HashSet<String> escapedProperties;

    private class Visitor implements ReflectionStringTraverser.ReflectionFieldVisitor
    {

        @Override
        public String tryVisit(String value, Object object, Field fieldOrNull)
        {
            // Only change the value if the name of the field is in the list provided
            if (null == fieldOrNull)
            {
                return null;
            }
            if (escapedProperties.contains(fieldOrNull.getName()))
            {
                return StringEscapeUtils.escapeHtml4(value);
            }
            return null;
        }
    }

    ReflectingStringEscaperRestricted(boolean isDeep, T bean, String... escapedProperties)
    {
        super(isDeep, bean);
        this.escapedProperties = new HashSet<String>();
        Collections.addAll(this.escapedProperties, escapedProperties);
    }

    T escape()
    {
        return traverse(new Visitor());
    }
}

/**
 * Utility Class that preforms the unrestricted escaping.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class ReflectingStringEscaperUnrestricted<T> extends ReflectingStringEscaperImpl<T>
{

    private static class Visitor implements ReflectionStringTraverser.ReflectionFieldVisitor
    {

        private final HashSet<String> unescapedProperties;

        private Visitor()
        {
            unescapedProperties = new HashSet<String>();
        }

        @Override
        public String tryVisit(String value, Object object, Field fieldOrNull)
        {
            if (null == fieldOrNull)
            {
                // happens e.g. when array of strings is escaped
                return StringEscapeUtils.escapeHtml4(value);
            }

            // Don't escape the ones that are specified as not to be escaped
            if (unescapedProperties.contains(fieldOrNull.getName()))
            {
                return null;
            }

            return StringEscapeUtils.escapeHtml4(value);
        }
    }

    ReflectingStringEscaperUnrestricted(boolean isDeep, T bean)
    {
        super(isDeep, bean);
    }

    T escape()
    {
        return traverse(new Visitor());
    }
}
