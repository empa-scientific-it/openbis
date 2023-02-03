/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration.postgresql;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.dbmigration.SimpleDatabaseMetaData;
import ch.systemsx.cisd.dbmigration.SimpleTableMetaData;

/**
 * @author Franz-Josef Elmer
 */
public class DumpPreparatorTest
{
    private static final File TEST_FOLDER = new File("targets/unit-test-wd/dump-preparator");

    private static final String EXAMPLE =
            "--\n"
                    + "-- PostgreSQL database dump\n"
                    + "--\n"
                    + "\n"
                    + "-- Started on 2007-11-28 13:36:10\n"
                    + "\n"
                    + "SET client_encoding = \'UTF8\';\n"
                    + "SET standard_conforming_strings = off;\n"
                    + "COMMENT ON SCHEMA public IS 'Standard public schema';\n"
                    + "CREATE PROCEDURAL LANGUAGE plpgsql;\n"
                    + "--\n"
                    + "-- TOC entry 1908 (class 0 OID 326942)\n"
                    + "-- Dependencies: 1374\n"
                    + "-- Data for Name: data; Type: TABLE DATA; Schema: public; Owner: felmer\n"
                    + "--\n"
                    + "\n"
                    + "COPY public.data (id, registration_timestamp, obty_id) FROM stdin;\n"
                    + "\\.\n"
                    + "\n"
                    + "\n"
                    + "--\n"
                    + "-- TOC entry 1890 (class 0 OID 326868)\n"
                    + "-- Dependencies: 1356\n"
                    + "-- Data for Name: data_types; Type: TABLE DATA; Schema: public; Owner: felmer\n"
                    + "--\n"
                    + "\n"
                    + "COPY public.data_types (id, \"location\", description) FROM stdin;\n"
                    + "1\tVARCHAR\tVariable length character\n"
                    + "2\tINTEGER\tInteger\n"
                    + "3\tREAL\tReal number, i.e. an inexact, variable-precision numeric type\n"
                    + "\\.\n"
                    + "\n"
                    + "\n"
                    + "--\n"
                    + "-- TOC entry 1891 (class 0 OID 326873)\n"
                    + "-- Dependencies: 1357\n"
                    + "-- Data for Name: data_values; Type: TABLE DATA; Schema: public; Owner: felmer\n"
                    + "--\n"
                    + "\n"
                    + "COPY public.data_values (id, data_id, saco_id, value) FROM stdin;\n"
                    + "\\.\n"
                    + "\n"
                    + "\n"
                    + "--\n"
                    + "-- TOC entry 1887 (class 0 OID 326837)\n"
                    + "-- Dependencies: 1353\n"
                    + "-- Data for Name: database_version_logs; Type: TABLE DATA; Schema: public; Owner: felmer\n"
                    + "--\n"
                    + "\n"
                    + "COPY public.database_version_logs (db_version, module_name, run_status) FROM stdin;\n"
                    + "011\tsource/sql/postgresql/010/schema-010.sql\tSUCCESS 2007-11-22 08:46:04.25\n"
                    + "010\tsource/sql/postgresql/010/data-010.sql\tSUCCESS 2007-11-22 08:46:04.53\n"
                    + "\\.\n"
                    + "\n"
                    + "\n"
                    + "\n"
                    + "--\n"
                    + "-- TOC entry 1823 (class 2606 OID 327015)\n"
                    + "-- Dependencies: 1374 1374\n"
                    + "-- Name: data_pk; Type: CONSTRAINT; Schema: public; Owner: felmer; Tablespace: \n"
                    + "--\n" + "\n" + "ALTER TABLE ONLY data\n"
                    + "    ADD CONSTRAINT data_pk PRIMARY KEY (id);\n" + "\n"
                    + "-- Completed on 2007-11-28 13:36:13\n" + "\n" + "--\n"
                    + "-- PostgreSQL database dump complete\n" + "--\n";

    @Test
    public void test() throws IOException
    {
        FileUtilities.deleteRecursively(TEST_FOLDER);
        StringReader reader = new StringReader(EXAMPLE);
        File folder = new File(TEST_FOLDER, "011");
        folder.mkdirs();
        assertEquals(true, folder.exists());
        SimpleDatabaseMetaData metaData = DumpPreparator.createUploadFiles(reader, folder, false);

        assertEquals(true, folder.isDirectory());
        assertEquals("SET standard_conforming_strings = off;\n\n", FileUtilities
                .loadToString(new File(folder, "schema-011.sql")));
        assertEquals("1\tVARCHAR\tVariable length character\n" + "2\tINTEGER\tInteger\n"
                + "3\tREAL\tReal number, i.e. an inexact, variable-precision numeric type\n",
                FileUtilities.loadToString(new File(folder, "002=data_types.tsv")));
        assertEquals(
                "011\tsource/sql/postgresql/010/schema-010.sql\tSUCCESS 2007-11-22 08:46:04.25\n"
                        + "010\tsource/sql/postgresql/010/data-010.sql\tSUCCESS 2007-11-22 08:46:04.53\n",
                FileUtilities.loadToString(new File(folder, "004=database_version_logs.tsv")));
        assertEquals(
                "ALTER TABLE ONLY data\n" + "    ADD CONSTRAINT data_pk PRIMARY KEY (id);\n\n",
                FileUtilities.loadToString(new File(folder, "finish-011.sql")));
        assertEquals(4, folder.listFiles().length);
        assertEquals("011", metaData.getDatabaseVersion());
        assertEquals("DATA_VALUES", metaData.tryToGetTableMetaData("Data_Values").getTableName());
        List<SimpleTableMetaData> tables = metaData.getTables();
        assertEquals(4, tables.size());
        checkTableMetaData("data", "[id, registration_timestamp, obty_id]", tables, 0);
        checkTableMetaData("data_types", "[id, location, description]", tables, 1);
        checkTableMetaData("data_values", "[id, data_id, saco_id, value]", tables, 2);
        checkTableMetaData("database_version_logs", "[db_version, module_name, run_status]", tables, 3);
    }

    private void checkTableMetaData(String expectedTableName, String expectedColumns,
            List<SimpleTableMetaData> list, int index)
    {
        SimpleTableMetaData metaData = list.get(index);
        assertEquals(expectedTableName.toUpperCase(), metaData.getTableName());
        String expectedFileName = "00" + (index + 1) + "=" + expectedTableName + ".tsv";
        assertEquals(expectedFileName, metaData.getTableFileName());
        assertEquals(expectedColumns.toUpperCase(), metaData.getColumnNames().toString());
    }
}
