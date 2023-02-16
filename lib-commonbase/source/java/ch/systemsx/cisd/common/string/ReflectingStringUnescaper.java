/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import java.lang.reflect.Field;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogUtils;

/**
 * Performs HTML unescaping the string fields of an object. Its implementation is based on {@link ReflectingStringEscaper} but there is no support to
 * restrict unescaped fields.
 * 
 * @author Piotr Buczek
 */
public class ReflectingStringUnescaper
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ReflectingStringUnescaper.class);

    private static int MIN_TIME_LOGGED_MS = 100;

    /**
     * Unescape all the string fields on the bean and all fields of objects referred to by the bean.
     */
    public static <T> T unescapeDeep(T bean)
    {
        try
        {
            long time = System.currentTimeMillis();
            T result = doUnescapeDeep(bean);
            long timeSpent = System.currentTimeMillis() - time;
            if (timeSpent >= MIN_TIME_LOGGED_MS)
            {
                operationLog.info((timeSpent) + "ms for unescaping "
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
     * Unescape all the string fields on the bean and all fields of objects referred to by the bean.
     */
    private static <T> T doUnescapeDeep(T bean)
    {
        ReflectingStringUnescaperUnrestricted<T> escaper =
                new ReflectingStringUnescaperUnrestricted<T>(true, bean);
        return escaper.escape();
    }

}

/**
 * @author Piotr Buczek
 */
class ReflectingStringUnescaperImpl<T>
{

    protected final boolean isDeep;

    protected final T bean;

    protected ReflectingStringUnescaperImpl(boolean isDeep, T bean)
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
 * Utility Class that preforms the unrestricted escaping.
 * 
 * @author Piotr Buczek
 */
class ReflectingStringUnescaperUnrestricted<T> extends ReflectingStringUnescaperImpl<T>
{

    private static class Visitor implements ReflectionStringTraverser.ReflectionFieldVisitor
    {
        @Override
        public String tryVisit(String value, Object object, Field fieldOrNull)
        {
            if (null == fieldOrNull)
            {
                // happens e.g. when array of strings is unescaped
                return StringEscapeUtils.unescapeHtml4(value);
            }

            return StringEscapeUtils.unescapeHtml4(value);
        }
    }

    ReflectingStringUnescaperUnrestricted(boolean isDeep, T bean)
    {
        super(isDeep, bean);
    }

    T escape()
    {
        return traverse(new Visitor());
    }
}
