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

import java.io.Serializable;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.string.ReflectingStringEscaper;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ReflectingStringEscaperTest extends AssertJUnit
{
    public static class TestBean implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private String foo;

        private String bar;

        private String baz;

        private TestBean wrappedBean;

        public TestBean()
        {

        }
    }

    @Test
    public void testShallowEscaper()
    {
        TestBean bean = new TestBean();
        bean.foo = "<a>foo</a>";
        bean.bar = "<b>bar</b>";
        bean.baz = "<i>baz</i>";
        bean.wrappedBean = new TestBean();
        bean.wrappedBean.foo = "<wa>foo</wa>";
        bean.wrappedBean.bar = "<wb>bar</wb>";
        bean.wrappedBean.baz = "<wi>baz</wi>";

        TestBean escaped = ReflectingStringEscaper.escapeShallow(bean);
        assertSame(bean, escaped);
        assertEquals("&lt;a&gt;foo&lt;/a&gt;", bean.foo);
        assertEquals("&lt;b&gt;bar&lt;/b&gt;", bean.bar);
        assertEquals("&lt;i&gt;baz&lt;/i&gt;", bean.baz);

        assertEquals("<wa>foo</wa>", bean.wrappedBean.foo);
        assertEquals("<wb>bar</wb>", bean.wrappedBean.bar);
        assertEquals("<wi>baz</wi>", bean.wrappedBean.baz);
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

        TestBean escaped = ReflectingStringEscaper.escapeDeep(bean);
        assertSame(bean, escaped);
        assertEquals("&lt;a&gt;foo&lt;/a&gt;", bean.foo);
        assertEquals("&lt;b&gt;bar&lt;/b&gt;", bean.bar);
        assertEquals("&lt;i&gt;baz&lt;/i&gt;", bean.baz);

        assertEquals("&lt;wa&gt;foo&lt;/wa&gt;", bean.wrappedBean.foo);
        assertEquals("&lt;wb&gt;bar&lt;/wb&gt;", bean.wrappedBean.bar);
        assertEquals("&lt;wi&gt;baz&lt;/wi&gt;", bean.wrappedBean.baz);
    }

    @Test
    public void testDeepEscaperWithCopy()
    {
        TestBean bean = new TestBean();
        bean.foo = "<a>foo</a>";
        bean.bar = "<b>bar</b>";
        bean.baz = "<i>baz</i>";
        bean.wrappedBean = new TestBean();
        bean.wrappedBean.foo = "<wa>foo</wa>";
        bean.wrappedBean.bar = "<wb>bar</wb>";
        bean.wrappedBean.baz = "<wi>baz</wi>";

        TestBean escaped = ReflectingStringEscaper.escapeDeepWithCopy(bean);
        assertNotSame(bean, escaped);
        assertEquals("<a>foo</a>", bean.foo);
        assertEquals("<b>bar</b>", bean.bar);
        assertEquals("<i>baz</i>", bean.baz);
        assertEquals("&lt;a&gt;foo&lt;/a&gt;", escaped.foo);
        assertEquals("&lt;b&gt;bar&lt;/b&gt;", escaped.bar);
        assertEquals("&lt;i&gt;baz&lt;/i&gt;", escaped.baz);

        assertNotSame(bean.wrappedBean, escaped.wrappedBean);
        assertEquals("<wa>foo</wa>", bean.wrappedBean.foo);
        assertEquals("<wb>bar</wb>", bean.wrappedBean.bar);
        assertEquals("<wi>baz</wi>", bean.wrappedBean.baz);
        assertEquals("&lt;wa&gt;foo&lt;/wa&gt;", escaped.wrappedBean.foo);
        assertEquals("&lt;wb&gt;bar&lt;/wb&gt;", escaped.wrappedBean.bar);
        assertEquals("&lt;wi&gt;baz&lt;/wi&gt;", escaped.wrappedBean.baz);
    }

    @Test
    public void testCircular()
    {
        TestBean bean = new TestBean();
        bean.foo = "<a>foo</a>";
        bean.bar = "<b>bar</b>";
        bean.baz = "<i>baz</i>";
        bean.wrappedBean = new TestBean();
        bean.wrappedBean.foo = "<wa>foo</wa>";
        bean.wrappedBean.bar = "<wb>bar</wb>";
        bean.wrappedBean.baz = "<wi>baz</wi>";
        bean.wrappedBean.wrappedBean = bean;

        TestBean escaped = ReflectingStringEscaper.escapeDeep(bean);
        assertSame(bean, escaped);
        assertEquals("&lt;a&gt;foo&lt;/a&gt;", bean.foo);
        assertEquals("&lt;b&gt;bar&lt;/b&gt;", bean.bar);
        assertEquals("&lt;i&gt;baz&lt;/i&gt;", bean.baz);

        assertEquals("&lt;wa&gt;foo&lt;/wa&gt;", bean.wrappedBean.foo);
        assertEquals("&lt;wb&gt;bar&lt;/wb&gt;", bean.wrappedBean.bar);
        assertEquals("&lt;wi&gt;baz&lt;/wi&gt;", bean.wrappedBean.baz);
    }

    @Test
    public void testShallowEscaperRestricted()
    {
        TestBean bean = new TestBean();
        bean.foo = "<a>foo</a>";
        bean.bar = "<b>bar</b>";
        bean.baz = "<i>baz</i>";
        bean.wrappedBean = new TestBean();
        bean.wrappedBean.foo = "<wa>foo</wa>";
        bean.wrappedBean.bar = "<wb>bar</wb>";
        bean.wrappedBean.baz = "<wi>baz</wi>";

        TestBean escaped = ReflectingStringEscaper.escapeShallowRestricted(bean, "foo", "baz");
        assertSame(bean, escaped);
        assertEquals("&lt;a&gt;foo&lt;/a&gt;", bean.foo);
        assertEquals("<b>bar</b>", bean.bar);
        assertEquals("&lt;i&gt;baz&lt;/i&gt;", bean.baz);

        assertEquals("<wa>foo</wa>", bean.wrappedBean.foo);
        assertEquals("<wb>bar</wb>", bean.wrappedBean.bar);
        assertEquals("<wi>baz</wi>", bean.wrappedBean.baz);
    }

    @Test
    public void testDeepEscaperRestricted()
    {
        TestBean bean = new TestBean();
        bean.foo = "<a>foo</a>";
        bean.bar = "<b>bar</b>";
        bean.baz = "<i>baz</i>";
        bean.wrappedBean = new TestBean();
        bean.wrappedBean.foo = "<wa>foo</wa>";
        bean.wrappedBean.bar = "<wb>bar</wb>";
        bean.wrappedBean.baz = "<wi>baz</wi>";

        TestBean escaped = ReflectingStringEscaper.escapeDeepRestricted(bean, "foo", "baz");
        assertSame(bean, escaped);
        assertEquals("&lt;a&gt;foo&lt;/a&gt;", bean.foo);
        assertEquals("<b>bar</b>", bean.bar);
        assertEquals("&lt;i&gt;baz&lt;/i&gt;", bean.baz);

        assertEquals("&lt;wa&gt;foo&lt;/wa&gt;", bean.wrappedBean.foo);
        assertEquals("<wb>bar</wb>", bean.wrappedBean.bar);
        assertEquals("&lt;wi&gt;baz&lt;/wi&gt;", bean.wrappedBean.baz);
    }
}
