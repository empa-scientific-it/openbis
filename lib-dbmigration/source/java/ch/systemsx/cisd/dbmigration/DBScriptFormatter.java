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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * A class that formats the SQL schema scripts. The output will have a canonical form that is easy to compare.
 * 
 * @author Bernd Rinn
 */
public class DBScriptFormatter
{
    private static interface ILineProcessor
    {
        public String process(String line);
    }

    private static interface ILineMatcher
    {
        public boolean match(String line);
    }

    private static enum StatementClass
    {
        CREATE_DOMAIN("Creating domains", "CREATE DOMAIN "), CREATE_TABLE("Creating tables",
                "CREATE TABLE "), CREATE_SEQUENCE("Creating sequences", "CREATE SEQUENCE ",
                new ILineProcessor()
                    {
                        @Override
                        public String process(String line)
                        {
                            return StringUtils.replace(line, " NO MAXVALUE NO MINVALUE NO CYCLE",
                                    "");
                        }
                    }), PRIMARY_KEY_CONSTRAINTS("Creating primary key constraints",
                new ILineMatcher()
                    {
                        @Override
                        public boolean match(String line)
                        {
                            return Pattern.matches("ALTER TABLE .+ PRIMARY KEY.+", line);
                        }
                    }, null), UNIQUE_CONSTRAINTS("Creating unique constraints", new ILineMatcher()
            {
                @Override
                public boolean match(String line)
                {
                    return Pattern.matches("ALTER TABLE .+ UNIQUE.+", line);
                }
            }, null), FOREIGN_KEY_CONSTRAINTS("Creating foreign key constraints",
                new ILineMatcher()
                    {
                        @Override
                        public boolean match(String line)
                        {
                            return Pattern.matches("ALTER TABLE .+ FOREIGN KEY.+", line);
                        }
                    }, null), CHECK_CONSTRAINTS("Creating check constraints", new ILineMatcher()
            {
                @Override
                public boolean match(String line)
                {
                    return Pattern.matches("ALTER TABLE .+ CHECK.+", line);
                }
            }, null), CREATE_INDEX("Creating indices", "CREATE INDEX "), MISC("Miscellaneous",
                new ILineMatcher()
                    {
                        @Override
                        public boolean match(String line)
                        {
                            return true;
                        }
                    }, null);

        private ILineMatcher matcher;

        private String comment;

        private ILineProcessor processorOrNull;

        StatementClass(final String comment, final String prefix)
        {
            this(comment, prefix, null);
        }

        StatementClass(final String comment, final String prefix,
                final ILineProcessor processorOrNull)
        {
            this(comment, new ILineMatcher()
                {
                    @Override
                    public boolean match(String line)
                    {
                        return line.startsWith(prefix);
                    }
                }, processorOrNull);
        }

        StatementClass(final String comment, final ILineMatcher matcher,
                final ILineProcessor processorOrNull)
        {
            this.comment = comment;
            this.matcher = matcher;
            this.processorOrNull = processorOrNull;
        }

        public String getComment()
        {
            return comment;
        }

        public boolean matches(String statement)
        {
            return matcher.match(statement);
        }

        public String process(String statement)
        {
            return (processorOrNull == null) ? statement : processorOrNull.process(statement);
        }
    }

    private static final String COMMENT = "--";

    private static void printInitialComment(PrintWriter out, String sqlScript)
    {
        for (final String scriptLine : StringUtils.split(sqlScript, '\n'))
        {
            final String trimmedScriptLine = scriptLine.trim();
            if (trimmedScriptLine.length() > 0 && trimmedScriptLine.startsWith(COMMENT) == false)
            {
                break;
            }
            if (trimmedScriptLine.substring(COMMENT.length()).trim().toUpperCase().startsWith(
                    "CREATING"))
            {
                continue;
            }
            out.println(trimmedScriptLine);
        }
    }

    private static List<String> getOrCreateList(Map<StatementClass, List<String>> map,
            StatementClass key)
    {
        List<String> list = map.get(key);
        if (list == null)
        {
            list = new ArrayList<String>();
            map.put(key, list);
        }
        return list;
    }

    private static boolean addIfAppropriate(String statement, StatementClass statementClass,
            Map<StatementClass, List<String>> map)
    {
        if (statementClass.matches(statement))
        {
            getOrCreateList(map, statementClass).add(statementClass.process(statement));
            return true;
        } else
        {
            return false;
        }
    }

    private static Map<StatementClass, List<String>> createStatementMap(List<String> sqlStatements)
    {
        final Map<StatementClass, List<String>> map = new HashMap<StatementClass, List<String>>();
        for (final String sqlStatement : sqlStatements)
        {
            final String upperCaseSqlStatement = sqlStatement.toUpperCase();

            for (StatementClass statementClass : StatementClass.values())
            {
                if (addIfAppropriate(upperCaseSqlStatement, statementClass, map))
                {
                    break;
                }
            }
        }
        for (StatementClass statementClass : StatementClass.values())
        {
            Collections.sort(getOrCreateList(map, statementClass));
        }
        return map;
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length != 1)
        {
            System.err.println("Syntax: DBScriptFormatter <sql-script>");
            System.exit(1);
        }

        final PrintWriter out =
                new PrintWriter(new FileOutputStream(new File(args[0] + ".formatted")));

        final String sqlScript = FileUtilities.loadToString(new File(args[0]));
        final List<String> sqlStatements = DBUtilities.splitSqlStatements(sqlScript);
        final Map<StatementClass, List<String>> statementMap = createStatementMap(sqlStatements);

        printInitialComment(out, sqlScript);
        for (StatementClass statementClass : StatementClass.values())
        {
            final List<String> statementList = getOrCreateList(statementMap, statementClass);
            if (statementList.size() == 0)
            {
                continue;
            }
            out.println();
            out.print("-- ");
            out.println(statementClass.getComment());
            out.println();
            for (String statement : statementList)
            {
                out.println(statement);
            }
        }
        out.close();
    }

}
