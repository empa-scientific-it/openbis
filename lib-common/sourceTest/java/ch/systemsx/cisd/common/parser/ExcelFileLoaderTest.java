/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.poi.ss.usermodel.Sheet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class ExcelFileLoaderTest
{

    private static final TestBean ROW_1 = new TestBean("row1column1", "row1column2",
            "column3default");

    private static final TestBean ROW_1_WITH_COLLUMN_2_DEFAULT = new TestBean("row1column1",
            "column2default", "column3default");

    private static final TestBean ROW_2 = new TestBean("row2column1", "row2column2",
            "column3default");

    private static final TestBean ROW_2_WITH_COLUMN_2_DEFAULT = new TestBean("row2column1",
            "column2default", "column3default");

    @Test
    public void testEmptyFile() throws Exception
    {
        List<TestBean> beans = loadBeans("excel-empty.xls");
        assertBeans(beans, new ArrayList<TestBean>());
    }

    /**
     * Tested file format:
     * 
     * <pre>
     *      column1  column2
     *      value1   value2
     * </pre>
     */
    @Test
    public void testFormatWithColumnHeadersInTheFirstLine() throws Exception
    {
        List<TestBean> beans = loadBeans("excel-with-column-headers-in-the-first-line.xls");
        assertBeans(beans, Arrays.asList(ROW_1, ROW_2));
    }

    @Test
    public void testFormatWithColumnHeadersInTheFirstLineWithNoData() throws Exception
    {
        List<TestBean> beans =
                loadBeans("excel-with-column-headers-in-the-first-line-with-no-data.xls");
        assertBeans(beans, new ArrayList<TestBean>());
    }

    /**
     * Tested file format:
     * 
     * <pre>
     *      [DEFAULT]
     *      column2  value2
     *      [DEFAULT]
     *      column1
     *      value1
     * </pre>
     */
    @Test
    public void testFormatWithColumnHeadersInTheFirstLineWithDefaultSection() throws Exception
    {
        List<TestBean> beans =
                loadBeans("excel-with-column-headers-in-the-first-line-with-default-section.xls");
        assertBeans(beans, Arrays.asList(ROW_1_WITH_COLLUMN_2_DEFAULT, ROW_2_WITH_COLUMN_2_DEFAULT));
    }

    /**
     * Tested file format:
     * 
     * <pre>
     *      # 1. line of comment
     *      # 2. line of comment
     *      # ...
     *      column1  column2
     *      value1   value2
     * </pre>
     */
    @Test
    public void testFormatWithColumnHeadersInALineAfterComments() throws Exception
    {
        List<TestBean> beans = loadBeans("excel-with-column-headers-in-a-line-after-comments.xls");
        assertBeans(beans, Arrays.asList(ROW_1, ROW_2));
    }

    @Test
    public void testFormatWithColumnHeadersInALineAfterCommentsWithNoData() throws Exception
    {
        List<TestBean> beans =
                loadBeans("excel-with-column-headers-in-a-line-after-comments-with-no-data.xls");
        assertBeans(beans, new ArrayList<TestBean>());
    }

    /**
     * Tested file format:
     * 
     * <pre>
     *      # 1. line of comment
     *      # 2. line of comment
     *      # ...
     *      [DEFAULT]
     *      column2  value2
     *      [DEFAULT]
     *      column1
     *      value1
     * </pre>
     */
    @Test
    public void testFormatWithColumnHeadersInALineAfterCommentsWithDefaultSection()
            throws Exception
    {
        List<TestBean> beans =
                loadBeans("excel-with-column-headers-in-a-line-after-comments-with-default-section.xls");
        assertBeans(beans, Arrays.asList(ROW_1_WITH_COLLUMN_2_DEFAULT, ROW_2_WITH_COLUMN_2_DEFAULT));
    }

    /**
     * Tested file format:
     * 
     * <pre>
     *      # 1. line of comment
     *      # 2. line of comment
     *      # ...
     *      #
     *      #column1  column2
     * </pre>
     */
    @Test
    public void testFormatWithColumnHeadersInTheLastCommentLine() throws Exception
    {
        List<TestBean> beans = loadBeans("excel-with-column-headers-in-the-last-comment-line.xls");
        assertBeans(beans, Arrays.asList(ROW_1, ROW_2));
    }

    @Test
    public void testFormatWithColumnHeadersInTheLastCommentLineWithNoData() throws Exception
    {
        List<TestBean> beans =
                loadBeans("excel-with-column-headers-in-the-last-comment-line-with-no-data.xls");
        assertBeans(beans, new ArrayList<TestBean>());
    }

    /**
     * Tested file format:
     * 
     * <pre>
     *      # 1. line of comment
     *      # 2. line of comment
     *      # ...
     *      #
     *      #column1
     *      [DEFAULT]
     *      column2 value2
     *      [DEFAULT]
     *      value1
     * </pre>
     */
    @Test
    public void testFormatWithColumnHeadersInTheLastCommentLineWithDefaultSection()
            throws Exception
    {
        List<TestBean> beans =
                loadBeans("excel-with-column-headers-in-the-last-comment-line-with-default-section.xls");
        assertBeans(beans, Arrays.asList(ROW_1_WITH_COLLUMN_2_DEFAULT, ROW_2_WITH_COLUMN_2_DEFAULT));
    }

    /**
     * Tested file format:
     * 
     * <pre>
     *      # 1. line of comment
     *      # 2. line of comment
     *      #...
     *      [DEFAULT]
     *      column2  value2
     *      [DEFAULT]
     *      #...
     *      column1
     *      value1
     * </pre>
     */
    @Test
    public void testFormatWithColumnHeadersAndDefaultSectionAndCommentsMixed() throws Exception
    {
        List<TestBean> beans =
                loadBeans("excel-with-column-headers-and-default-section-and-comments-mixed.xls");
        assertBeans(beans, Arrays.asList(ROW_1_WITH_COLLUMN_2_DEFAULT, ROW_2_WITH_COLUMN_2_DEFAULT));
    }

    private List<TestBean> loadBeans(String fileName) throws Exception
    {
        Sheet sheet = ExcelTestUtil.getSheet(getClass(), fileName);
        ExcelFileLoader<TestBean> loader = new ExcelFileLoader<TestBean>(TestBean.class);
        Map<String, String> defaults = new HashMap<String, String>();
        defaults.put("column3", "column3default");
        return loader.load(sheet, sheet.getFirstRowNum(), sheet.getLastRowNum(), defaults);
    }

    private void assertBeans(List<TestBean> actualBeans, List<TestBean> expectedBeans)
    {
        Assert.assertEquals(actualBeans, expectedBeans);
    }

    public static class TestBean
    {

        private String col1;

        private String col2;

        private String col3;

        public TestBean()
        {
        }

        public TestBean(String col1, String col2, String col3)
        {
            this.col1 = col1;
            this.col2 = col2;
            this.col3 = col3;
        }

        @BeanProperty(label = "column1", optional = true)
        public void setCol1(String col1)
        {
            this.col1 = col1;
        }

        public String getCol1()
        {
            return col1;
        }

        @BeanProperty(label = "column2", optional = true)
        public void setCol2(String col2)
        {
            this.col2 = col2;
        }

        public String getCol2()
        {
            return col2;
        }

        @BeanProperty(label = "column3", optional = true)
        public void setCol3(String col3)
        {
            this.col3 = col3;
        }

        public String getCol3()
        {
            return col3;
        }

        @Override
        public boolean equals(Object obj)
        {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }

}
