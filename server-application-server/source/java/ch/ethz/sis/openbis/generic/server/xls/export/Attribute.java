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

    $("$", true, true),

    ARCHIVING_STATUS("Archiving status", false, false),

    AUTO_GENERATE_CODES("Auto generate codes", true, true),

    CHILDREN("Children", true, false),

    CODE("Code", true, true),

    DESCRIPTION("Description", true, true),

    DISALLOW_DELETION("Disallow Deletion", false, false),

    EXPERIMENT("Experiment", true, false),

    GENERATE_CODES("Generate Codes", false, false),

    GENERATED_CODE_PREFIX("Generated code prefix", true, true),

    IDENTIFIER("Identifier", true, true),

    LABEL("Label", true, true),

    MAIN_DATA_SET_PATH("Main Data Set Path", false, false),

    MAIN_DATA_SET_PATTERN("Main Data Set Pattern", false, false),

    MODIFICATION_DATE("Modification Date", false, false),

    MODIFIER("Modifier", false, false),

    ONTOLOGY_ID("Ontology ID", true, false),

    ONTOLOGY_VERSION("Ontology Version", true, false),

    ONTOLOGY_ANNOTATION_ID("Ontology Annotation Id", true, false),

    PARENTS("Parents", true, false),

    PERM_ID("PermId", false, false),

    PRESENT_IN_ARCHIVE("Present in archive", true, false),

    PROJECT("Project", true, true),

    REGISTRATION_DATE("Registration Date", false, false),

    REGISTRATOR("Registrator", false, false),

    SAMPLE("Sample", false, false),

    SPACE("Space", true, true),

    STORAGE_CONFIRMATION("Storage confirmation", false, false),

    UNIQUE_SUBCODES("Unique Subcodes", false, false),

    URL_TEMPLATE("URL Template", false, false),

    VALIDATION_SCRIPT("Validation Script", true, true),

    VALIDATION_PLUGIN("Validation Plugin", false, false);

    final String name;

    final boolean importable;

    final boolean requiredForImport;

    Attribute(final String name, final boolean importable, final boolean requiredForImport)
    {
        this.name = name;
        this.importable = importable;
        this.requiredForImport = requiredForImport;
    }

    public String getName()
    {
        return name;
    }

}
