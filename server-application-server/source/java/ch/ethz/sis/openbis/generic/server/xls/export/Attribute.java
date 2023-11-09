/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.xls.export;

public enum Attribute
{

    $("$", true, true, false),

    ARCHIVING_STATUS("Archiving status", false, false, true),

    AUTO_GENERATE_CODES("Auto generate codes", true, true, true),

    AUTO_GENERATE_CODE("Auto generate code", true, true, false),

    CHILDREN("Children", true, false, true),

    CODE("Code", true, true, true),

    DESCRIPTION("Description", true, true, true),

    DISALLOW_DELETION("Disallow Deletion", false, false, true),

    EXPERIMENT("Experiment", true, false, true),

    GENERATE_CODES("Generate Codes", false, false, true),

    GENERATED_CODE_PREFIX("Generated code prefix", true, true, true),

    IDENTIFIER("Identifier", true, true, true),

    LABEL("Label", true, true, true),

    MAIN_DATA_SET_PATH("Main Data Set Path", false, false, true),

    MAIN_DATA_SET_PATTERN("Main Data Set Pattern", false, false, true),

    MODIFICATION_DATE("Modification Date", false, false, true),

    MODIFIER("Modifier", false, false, true),

    ONTOLOGY_ID("Ontology ID", true, false, true),

    ONTOLOGY_VERSION("Ontology Version", true, false, true),

    ONTOLOGY_ANNOTATION_ID("Ontology Annotation Id", true, false, true),

    PARENTS("Parents", true, false, true),

    PERM_ID("PermId", false, false, true),

    PRESENT_IN_ARCHIVE("Present in archive", true, false, true),

    PROJECT("Project", true, true, true),

    REGISTRATION_DATE("Registration Date", false, false, true),

    REGISTRATOR("Registrator", false, false, true),

    SIZE("Size", false, false, true),

    SAMPLE("Sample", false, false, true),

    SPACE("Space", true, true, true),

    STORAGE_CONFIRMATION("Storage confirmation", false, false, true),

    UNIQUE_SUBCODES("Unique Subcodes", false, false, true),

    URL_TEMPLATE("URL Template", false, false, true),

    VALIDATION_SCRIPT("Validation script", true, true, true);

    final String name;

    final boolean importable;

    final boolean requiredForImport;

    final boolean includeInDefaultList;

    Attribute(final String name, final boolean importable, final boolean requiredForImport, final boolean includeInDefaultList)
    {
        this.name = name;
        this.importable = importable;
        this.requiredForImport = requiredForImport;
        this.includeInDefaultList = includeInDefaultList;
    }

    public String getName()
    {
        return name;
    }

    public boolean isImportable()
    {
        return importable;
    }

    public boolean isRequiredForImport()
    {
        return requiredForImport;
    }

    public boolean isIncludeInDefaultList()
    {
        return includeInDefaultList;
    }
}
