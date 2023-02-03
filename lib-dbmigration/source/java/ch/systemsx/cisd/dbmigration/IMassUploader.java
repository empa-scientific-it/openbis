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

package ch.systemsx.cisd.dbmigration;

import java.io.File;

/**
 * Classes that implement this interface encapsulate mass uploading of tabular files to the database. The files to be processed by implementations of
 * this class are supposed to be <code>csv</code> files that can be uploaded to exactly one table. They need to follow the naming convention
 * <code><i>&lt;orderSpecifier&gt;</i>=<i>&lt;tableName&gt;</i>.&lt;file type&gt;</code>. Here the order specifier ensures that the dependency is
 * right while the table name is the name of the table to upload the data to. The file type has to be <code>csv</code> (Comma veparated values) or
 * <code>tsv</code> (TAB separated values).
 * 
 * @author Bernd Rinn
 */
public interface IMassUploader
{

    /**
     * Upload the data from file <var>massUploadFile</var> to the database.
     * 
     * @param massUploadFiles Files to upload to the database, following the naming convention <code>orderSpecifier=tableName.csv</code> or
     *            <code>orderSpecifier=tableName.tsv</code>.
     */
    public void performMassUpload(File[] massUploadFiles);

    /**
     * Upload the data from <var>data</var> to the table with <var>tableName</var> of the database. This method never updates any sequences!
     * 
     * @param tableName The name of the table to upload the <var>data</var> to.
     * @param data The data (in tsv format, \N represents NULL) that should be uploaded to the database.
     */
    public void performMassUpload(String tableName, String data);

    /**
     * Upload the data from <var>data</var> to the table with <var>tableName</var> of the database. This method never updates any sequences!
     * 
     * @param tableName The name of the table to upload the <var>data</var> to.
     * @param columnNames The names of the columns to be found in the <var>data</var>. This has to be a subset of columns of <var>tableName</var> and
     *            must contain all columns with a NOT_NULL constraint.
     * @param data The data (in tsv format, \N represents NULL) that should be uploaded to the database.
     */
    public void performMassUpload(String tableName, String[] columnNames, String data);

}
