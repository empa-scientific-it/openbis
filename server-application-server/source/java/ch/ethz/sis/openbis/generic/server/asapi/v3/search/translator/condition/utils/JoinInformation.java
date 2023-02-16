/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils;

public class JoinInformation
{

    private String mainTable;

    private String mainTableIdField;

    private String mainTableAlias;

    private String subTable;

    private String subTableIdField;

    private String subTableAlias;

    private JoinType joinType;

    public String getMainTable() {
        return mainTable;
    }

    public void setMainTable(String mainTable) {
        this.mainTable = mainTable;
    }

    public String getMainTableIdField() {
        return mainTableIdField;
    }

    public void setMainTableIdField(String mainTableIdField) {
        this.mainTableIdField = mainTableIdField;
    }

    public String getMainTableAlias() {
        return mainTableAlias;
    }

    public void setMainTableAlias(String mainTableAlias) {
        this.mainTableAlias = mainTableAlias;
    }

    public String getSubTable() {
        return subTable;
    }

    public void setSubTable(String subTable) {
        this.subTable = subTable;
    }

    public String getSubTableIdField() {
        return subTableIdField;
    }

    public void setSubTableIdField(String subTableIdField) {
        this.subTableIdField = subTableIdField;
    }

    public String getSubTableAlias() {
        return subTableAlias;
    }

    public void setSubTableAlias(String subTableAlias) {
        this.subTableAlias = subTableAlias;
    }

    public JoinType getJoinType()
    {
        return joinType;
    }

    public void setJoinType(final JoinType joinType)
    {
        this.joinType = joinType;
    }

}
