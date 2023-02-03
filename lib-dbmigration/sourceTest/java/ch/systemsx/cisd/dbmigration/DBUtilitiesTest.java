/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration;

import java.util.Arrays;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for {@link DBUtilities}.
 * 
 * @author Bernd Rinn
 */
public class DBUtilitiesTest
{

    @Test
    public void testSplitSqlStatementsSpaceLeftRight()
    {
        assertEquals(Arrays.asList("statement1;", "statement2;"), DBUtilities
                .splitSqlStatements("statement1 ; statement2;"));
    }

    @Test
    public void testSplitSqlStatementsSpaceRight()
    {
        assertEquals(Arrays.asList("statement1;", "statement2;"), DBUtilities
                .splitSqlStatements("statement1; statement2;"));
    }

    @Test
    public void testSplitSqlStatementsNoSpaces()
    {
        assertEquals(Arrays.asList("statement1;", "statement2;"), DBUtilities
                .splitSqlStatements("statement1;statement2;"));
    }

    @Test
    public void testSplitSqlStatementsNewLine()
    {
        assertEquals(Arrays.asList("statement1;", "statement2;"), DBUtilities
                .splitSqlStatements("statement1 ;\n statement2 ;\n"));
    }

    @Test
    public void testSplitSqlStatementsOneStatementOnMultipleLines()
    {
        assertEquals(
                Arrays.asList("statement part1 statement part2;", "statement2.1 statement2.2;"),
                DBUtilities
                        .splitSqlStatements("statement part1 \n statement part2 ;\n statement2.1\nstatement2.2 ; "));
    }

    @Test
    public void testSplitSqlStatementsComments()
    {
        assertEquals(
                Arrays.asList("statement1 statement2;"),
                DBUtilities
                        .splitSqlStatements("statement1 -- comment\n -- just comments; bla\n statement2; -- another comment"));
    }

}
