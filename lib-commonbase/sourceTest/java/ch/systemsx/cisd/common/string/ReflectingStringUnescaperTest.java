/*
 * Copyright 2010 ETH Zuerich, CISD
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

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.string.ReflectingStringEscaper;
import ch.systemsx.cisd.common.string.ReflectingStringUnescaper;

/**
 * @author Piotr Buczek
 */
public class ReflectingStringUnescaperTest extends AssertJUnit
{
    public class TestBean
    {
        private String foo;

        private String bar;

        private String baz;

        private TestBean wrappedBean;
    }

    @Test
    public void testDeepEscaper()
    {
        TestBean bean = new TestBean();
        bean.foo = "<a>foo</a>";
        bean.bar = "<b>bar</b>";
        bean.baz = "<i>baz</i>";
        bean.wrappedBean = new TestBean();
        bean.wrappedBean.foo = "<wa>foo</wa>";
        bean.wrappedBean.bar = "<wb>bar</wb>";
        bean.wrappedBean.baz = "<wi>baz</wi>";

        // first escape
        TestBean escaped = ReflectingStringEscaper.escapeDeep(bean);
        assertSame(bean, escaped);
        assertEquals("&lt;a&gt;foo&lt;/a&gt;", bean.foo);
        assertEquals("&lt;b&gt;bar&lt;/b&gt;", bean.bar);
        assertEquals("&lt;i&gt;baz&lt;/i&gt;", bean.baz);
        assertEquals("&lt;wa&gt;foo&lt;/wa&gt;", bean.wrappedBean.foo);
        assertEquals("&lt;wb&gt;bar&lt;/wb&gt;", bean.wrappedBean.bar);
        assertEquals("&lt;wi&gt;baz&lt;/wi&gt;", bean.wrappedBean.baz);

        // unescaping goes back to original
        TestBean unescaped = ReflectingStringUnescaper.unescapeDeep(bean);
        assertSame(bean, unescaped);
        assertEquals("<a>foo</a>", bean.foo);
        assertEquals("<b>bar</b>", bean.bar);
        assertEquals("<i>baz</i>", bean.baz);
        assertEquals("<wa>foo</wa>", bean.wrappedBean.foo);
        assertEquals("<wb>bar</wb>", bean.wrappedBean.bar);
        assertEquals("<wi>baz</wi>", bean.wrappedBean.baz);
    }

}
