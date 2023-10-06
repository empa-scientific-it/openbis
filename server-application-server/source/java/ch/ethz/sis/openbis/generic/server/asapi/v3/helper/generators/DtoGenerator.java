/*
 * Copyright ETH 2014 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.generators;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.Year;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;
import ch.systemsx.cisd.base.annotation.JsonObject;

@SuppressWarnings("hiding")
public class DtoGenerator
{
    private static final String PACKAGE_PREFIX = "ch.ethz.sis.openbis.generic.asapi.v3.dto";

    private int indent = 0;

    List<DTOField> fields;

    private String subPackage;

    private String className;

    private Set<String> additionalImports;

    private List<String> additionalMethods;

    private PrintStream outputStream = System.out;

    private Class<?> fetchOptionsClass;

    private String toStringContent;

    private String extendsClass;

    private Set<String> implementedInterfaces;

    private boolean deprecated;

    private DTOField fetchOptionsField;

    public DtoGenerator(String subPackage, String className, Class<?> fetchOptionsClass)
    {
        this.subPackage = subPackage;
        this.className = className;
        this.additionalImports = new TreeSet<String>();
        this.additionalMethods = new LinkedList<String>();
        this.fields = new LinkedList<DtoGenerator.DTOField>();
        this.implementedInterfaces = new TreeSet<String>();

        addClassForImport(JsonProperty.class);
        addClassForImport(JsonIgnore.class);
        addClassForImport(JsonObject.class);
        addClassForImport(Serializable.class);
        addClassForImport(NotFetchedException.class);

        this.fetchOptionsClass = fetchOptionsClass;
        fetchOptionsField = addSimpleField(fetchOptionsClass, "fetchOptions");
    }

    @Override
    public String toString()
    {
        return className;
    }

    public DTOField getFetchOptionsField()
    {
        return fetchOptionsField;
    }

    private DTOField create(String fieldName, Class<?> fieldClass, String description,
            Class<?> fetchOptions, String fetchOptionsFieldName)
    {
        return new DTOField(fieldName, fieldClass, description, fetchOptions, fetchOptionsFieldName,
                false);
    }

    private DTOField createPlural(String fieldName, String className, String fullClassName,
            String description, Class<?> fetchOptions)
    {
        return new DTOField(fieldName, className, fullClassName, description, fetchOptions, true);
    }

    class DTOField
    {
        String fieldName;

        /**
         * Only if different than fieldName
         */
        String persistentFieldName;

        String description;

        String definitionClassName;

        String importClassName;

        Class<?> fetchOptions;

        String fetchOptionsFieldName;

        boolean plural;

        Class<?> interfaceClass;

        boolean deprecated;

        String customSetter;
        String customGetter;

        boolean isInherited = false;

        private DTOField(String fieldName, Class<?> fieldClass, String description,
                Class<?> fetchOptions, String fetchOptionsFieldName,
                boolean plural)
        {
            this.fieldName = fieldName;
            this.definitionClassName = fieldClass.getSimpleName();
            if (false == (fieldClass.isArray() || fieldClass.isPrimitive()))
            {
                this.importClassName = fieldClass.getCanonicalName();
            }
            this.description = description;
            this.fetchOptions = fetchOptions;
            this.fetchOptionsFieldName = fetchOptionsFieldName;
            this.plural = plural;
        }

        private DTOField(String fieldName, String className, String fullClassName,
                String description, Class<?> fetchOptions, boolean plural)
        {
            this.fieldName = fieldName;
            this.definitionClassName = className;
            this.importClassName = fullClassName;
            this.description = description;
            this.fetchOptions = fetchOptions;
            this.plural = plural;
        }

        String getPersistentName()
        {
            return persistentFieldName == null ? fieldName : persistentFieldName;
        }

        String getCapitalizedName()
        {
            return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        }

        String getFetchOptionsFieldName()
        {
            if (fetchOptionsFieldName == null)
            {
                return getCapitalizedName();
            } else
            {
                return fetchOptionsFieldName.substring(0, 1)
                        .toUpperCase() + fetchOptionsFieldName.substring(1);
            }
        }

        void withInterface(Class<?> i)
        {
            addImplementedInterface(i);
            interfaceClass = i;
        }

        void withInterfaceReflexive(Class<?> i)
        {
            addImplementedInterfaceGeneric(i);
            interfaceClass = i;
        }

        void withCustomGetter(String getter) {
            customGetter = getter;
        }

        void withCustomSetter(String setter) {
            customSetter = setter;
        }

        DTOField deprecated()
        {
            deprecated = true;
            return this;
        }
    }

    private void setOutputStream(PrintStream printStream)
    {
        this.outputStream = printStream;
    }

    /**
     * Add simple field i.e. one that doesn't have fetched content
     */
    public DTOField addSimpleField(Class<?> c, String name)
    {
        DTOField field = create(name, c, null, null, null);
        fields.add(field);
        return field;
    }

    /**
     * Add simple boolean field
     */
    public void addBooleanField(String name)
    {
        addSimpleField(Boolean.class, name);
    }

    /**
     * Add simple date field
     */
    public void addDateField(String name)
    {
        addSimpleField(Date.class, name);
    }

    /**
     * Add simple string field
     */
    public void addStringField(String name)
    {
        addSimpleField(String.class, name);
    }

    /**
     * This method is intended for cases when field name is illegal identifier in java (e.g.
     * keyword)
     */
    public void addSimpleField(Class<?> c, String name, String persistentFieldName)
    {
        DTOField dtoField = create(name, c, null, null, null);
        dtoField.persistentFieldName = persistentFieldName;
        fields.add(dtoField);
    }

    public void addSimpleField(String definitionClassName, String name, String fullClassName, String persistentFieldName)
    {
        DTOField dtoField = createPlural(name, definitionClassName, fullClassName, null, null);
        dtoField.persistentFieldName = persistentFieldName;
        fields.add(dtoField);
    }

    public DTOField addFetchedField(Class<?> c, String name, String description,
            Class<?> fetchOptionsClass)
    {
        DTOField field = create(name, c, description, fetchOptionsClass, null);
        fields.add(field);
        return field;
    }

    public DTOField addFetchedField(Class<?> c, String name, String description,
            Class<?> fetchOptionsClass, String fetchOptionsFieldName)
    {
        DTOField field = create(name, c, description, fetchOptionsClass, fetchOptionsFieldName);
        fields.add(field);
        return field;
    }

    public DTOField addPluralFetchedField(String definitionClassName, String importClassName,
            String name, String description,
            Class<?> fetchOptionsClass)
    {
        DTOField field = createPlural(name, definitionClassName, importClassName, description,
                fetchOptionsClass);
        fields.add(field);
        return field;
    }

    public void addAdditionalMethod(String method)
    {
        additionalMethods.add(method);
    }

    public void addClassForImport(Class<?> c)
    {
        additionalImports.add(c.getName());
    }

    public void addImplementedInterface(Class<?> i)
    {
        implementedInterfaces.add(i.getSimpleName());
        additionalImports.add(i.getName());
    }

    public void addImplementedInterfaceGeneric(Class<?> i)
    {
        implementedInterfaces.add(i.getSimpleName() + "<" + className + ">");
        additionalImports.add(i.getName());
    }

    public void addExtendsClass(String className) {
        this.extendsClass = className;
    }

    public DtoGenerator deprecated()
    {
        deprecated = true;
        return this;
    }

    public void setToStringMethod(String toStringContent)
    {
        this.toStringContent = toStringContent;
    }

    private void print(String code, Object... formatArguments)
    {
        if (indent > 0 && code.length() > 0)
        {
            outputStream.print(String.format("%" + indent + "s", ""));
        }
        outputStream.println(String.format(code, formatArguments));
    }

    private void startBlock()
    {
        print("{");
        indent += 4;
    }

    private void endBlock()
    {
        indent -= 4;
        print("}");
    }

    public void generateDTO() throws FileNotFoundException
    {
        generateDTO(
                "../api-openbis-java/source/java/ch/ethz/sis/openbis/generic/asapi/v3/dto/" + subPackage.replaceAll(
                        "\\.", "/") + "/" + className
                        + ".java");
    }

    public void generateDTOJS() throws FileNotFoundException
    {
        generateDTOJS(
                "../test-api-openbis-javascript/servers/common/core-plugins/tests/1/as/webapps/openbis-v3-api-test/html/dto/" + className + ".js");
    }

    private void generateDTO(String file) throws FileNotFoundException
    {
        PrintStream fos = new PrintStream(new FileOutputStream(file, false));
        try
        {
            generateDTO(fos);
        } finally
        {
            fos.close();
        }
    }

    private void generateDTOJS(String file) throws FileNotFoundException
    {
        PrintStream fos = new PrintStream(new FileOutputStream(file, false));
        try
        {
            generateDTOJS(fos);
        } finally
        {
            fos.close();
        }
    }

    public void generateDTO(PrintStream os)
    {
        setOutputStream(os);

        printHeaders();
        printPackage(subPackage);
        printImports();

        printClassHeader(className, subPackage, extendsClass, implementedInterfaces);
        startBlock();
        printFields();

        printAccessors();
        printAdditionalMethods();

        printToString();

        endBlock();
    }

    public void generateDTOJS(PrintStream os)
    {
        setOutputStream(os);
        printClassHeaderJS(className);
        startBlock();
        printTypeJS(className);
        printAccessorsJS();
        endBlock();
    }

    public void generateFetchOptions() throws FileNotFoundException
    {
        generateFetchOptions(
                "../api-openbis-java/source/java/ch/ethz/sis/openbis/generic/asapi/v3/dto/" + subPackage.replaceAll(
                        "\\.", "/")
                        + "/fetchoptions/" + className + "FetchOptions.java");
    }

    public void generateFetchOptionsJS() throws FileNotFoundException
    {
        generateFetchOptionsJS(
                "../test-api-openbis-javascript/servers/common/core-plugins/tests/1/as/webapps/openbis-v3-api-test/html/dto/" + className
                        + "FetchOptions.js");
    }

    private void generateFetchOptions(String file) throws FileNotFoundException
    {
        PrintStream fos = new PrintStream(new FileOutputStream(file, false));
        try
        {
            generateFetchOptions(fos);
        } finally
        {
            fos.close();
        }
    }

    private void generateFetchOptionsJS(String file) throws FileNotFoundException
    {
        PrintStream fos = new PrintStream(new FileOutputStream(file, false));
        try
        {
            generateFetchOptionsJS(fos);
        } finally
        {
            fos.close();
        }
    }

    public void generateFetchOptions(PrintStream os)
    {
        setOutputStream(os);
        printHeaders();
        printPackage(subPackage + ".fetchoptions");
        printImportsForFetchOptions();

        printClassHeader(fetchOptionsClass.getSimpleName(), subPackage + ".fetchoptions",
                "FetchOptions<" + className + ">", null);
        startBlock();

        printFetchOptionsFields();
        printFetchOptionsAccessors();
        printFetchOptionsStringBuilder();

        endBlock();
    }

    public void generateFetchOptionsJS(PrintStream os)
    {
        setOutputStream(os);
        printClassHeaderJS(className + "FetchOptions");
        startBlock();
        printTypeJS(className + "FetchOptions");
        printFetchOptionsAccessorsJS();
        endBlock();
    }

    private void printHeaders()
    {
        print("/*");
        print(" * Copyright ETH 2014 - " + Year.now().getValue() +" Zürich, Scientific IT Services");
        print(" *");
        print(" * Licensed under the Apache License, Version 2.0 (the \"License\");");
        print(" * you may not use this file except in compliance with the License.");
        print(" * You may obtain a copy of the License at");
        print(" *");
        print(" *      http://www.apache.org/licenses/LICENSE-2.0");
        print(" *");
        print(" * Unless required by applicable law or agreed to in writing, software");
        print(" * distributed under the License is distributed on an \"AS IS\" BASIS,");
        print(" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.");
        print(" * See the License for the specific language governing permissions and");
        print(" * limitations under the License.");
        print(" */");

    }

    private void printAccessors()
    {
        for (DTOField field : fields)
        {
            printAccessors(field);
        }
    }

    private void printAccessorsJS()
    {
        for (DTOField field : fields)
        {
            printAccessorsJS(field);
        }
    }

    private void printAccessors(DTOField field)
    {

        if(field.customGetter != null)
        {
            print(field.customGetter);
        } else
        {
            if (field.fetchOptions != null)
            {
                printGetterWithFetchOptions(field);
            } else
            {
                printBasicGetter(field);
            }
        }

        if(field.customSetter != null)
        {
            print(field.customSetter);
        } else
        {
            printBasicSetter(field);
        }
    }

    private void printAccessorsJS(DTOField field)
    {
        if (field.fetchOptions != null)
        {
            printGetterWithFetchOptionsJS(field);
        } else
        {
            printBasicGetterJS(field);
        }

        printBasicSetterJS(field);
    }

    private void printFetchOptionsAccessors()
    {
        for (DTOField field : fields)
        {
            if (field.fetchOptions != null && field.fetchOptionsFieldName == null)
            {
                printFetchOptionsAccessors(field);
            }
        }

        printMethodJavaDoc();
        print("@Override");
        print("public " + className + "SortOptions sortBy()");
        startBlock();
        print("if (sort == null)");
        startBlock();
        print("sort = new " + className + "SortOptions();");
        endBlock();
        print("return sort;");
        endBlock();

        print("");
        printMethodJavaDoc();
        print("@Override");
        print("public " + className + "SortOptions getSortBy()");
        startBlock();
        print("return sort;");
        endBlock();
    }

    private void printFetchOptionsAccessorsJS()
    {
        for (DTOField field : fields)
        {
            if (field.fetchOptions != null && field.fetchOptionsFieldName == null)
            {
                printFetchOptionsAccessorsJS(field);
            }
        }
    }

    private void printBasicSetter(DTOField field)
    {
        printMethodJavaDoc();
        printSetterAnnotation(field);
        print("public void set%s(%s %s)",
                field.getCapitalizedName(),
                field.definitionClassName,
                field.getPersistentName());
        startBlock();
        print("this.%s = %s;", field.getPersistentName(), field.getPersistentName());
        endBlock();
        print("");
    }

    private void printBasicSetterJS(DTOField field)
    {
        print("this.set%s = function(%s)",
                field.getCapitalizedName(),
                field.getPersistentName());
        startBlock();
        print("this.%s = %s;", field.getPersistentName(), field.getPersistentName());
        endBlock();
        print("");
    }

    private void printSetterAnnotation(DTOField field)
    {
        Method interfaceMethod = null;

        if (field.interfaceClass != null)
        {
            interfaceMethod = BeanUtils.findMethodWithMinimalParameters(field.interfaceClass,
                    "set" + field.getCapitalizedName());
        }

        if (interfaceMethod != null)
        {
            print("@Override");
        }

        printDeprecated(field.deprecated);
    }

    private void printGetterAnnotation(DTOField field)
    {
        print("@JsonIgnore");

        Method interfaceMethod = null;

        if (field.interfaceClass != null)
        {
            interfaceMethod = BeanUtils.findMethodWithMinimalParameters(field.interfaceClass,
                    "get" + field.getCapitalizedName());
        }

        if (interfaceMethod != null)
        {
            print("@Override");
        }

        printDeprecated(field.deprecated);
    }

    private void printGetterWithFetchOptions(DTOField field)
    {
        printMethodJavaDoc();
        printGetterAnnotation(field);
        if (field.definitionClassName.equals("Boolean"))
        {
            print("public %s is%s()", field.definitionClassName, field.getCapitalizedName());
        } else
        {
            print("public %s get%s()", field.definitionClassName, field.getCapitalizedName());
        }
        startBlock();
        print("if (getFetchOptions() != null && getFetchOptions().has%s())",
                field.getFetchOptionsFieldName());
        startBlock();
        print("return %s;", field.getPersistentName());
        endBlock();
        print("else");
        startBlock();
        if (field.plural)
        {
            print("throw new NotFetchedException(\"%s have not been fetched.\");",
                    field.description);
        } else
        {
            print("throw new NotFetchedException(\"%s has not been fetched.\");",
                    field.description);
        }
        endBlock();
        endBlock();
        print("");
    }

    private void printGetterWithFetchOptionsJS(DTOField field)
    {
        print("this.get%s = function()", field.getCapitalizedName());
        startBlock();
        print("if (getFetchOptions() != null && this.getFetchOptions().has%s())",
                field.getFetchOptionsFieldName());
        startBlock();
        print("return %s;", field.getPersistentName());
        endBlock();
        print("else");
        startBlock();
        if (field.plural)
        {
            print("throw '%s have not been fetched.'", field.description);
        } else
        {
            print("throw '%s has not been fetched.'", field.description);
        }
        endBlock();
        endBlock();
        print("");
    }

    private void printBasicGetter(DTOField field)
    {
        printMethodJavaDoc();
        printGetterAnnotation(field);
        if (field.definitionClassName.equalsIgnoreCase("Boolean"))
        {
            print("public %s is%s()", field.definitionClassName, field.getCapitalizedName());
        } else
        {
            print("public %s get%s()", field.definitionClassName, field.getCapitalizedName());
        }
        startBlock();
        print("return %s;", field.getPersistentName());
        endBlock();
        print("");
    }

    private void printBasicGetterJS(DTOField field)
    {
        if (field.definitionClassName.equalsIgnoreCase("Boolean"))
        {
            print("this.is%s = function()", field.getCapitalizedName());
        } else
        {
            print("this.get%s = function()", field.getCapitalizedName());
        }
        startBlock();
        print("return %s;", field.getPersistentName());
        endBlock();
        print("");
    }

    private void printFetchOptionsAccessors(DTOField field)
    {
        printMethodJavaDoc();
        printDeprecated(field.deprecated);
        print("public %s with%s()", field.fetchOptions.getSimpleName(), field.getCapitalizedName());
        startBlock();
        print("if (%s == null)", field.getPersistentName());
        startBlock();
        print("%s = new %s();", field.getPersistentName(), field.fetchOptions.getSimpleName());
        endBlock();
        print("return %s;", field.getPersistentName());
        endBlock();
        print("");

        printMethodJavaDoc();
        printDeprecated(field.deprecated);
        print("public %s with%sUsing(%s fetchOptions)", field.fetchOptions.getSimpleName(),
                field.getCapitalizedName(),
                field.fetchOptions.getSimpleName());
        startBlock();
        print("return %s = fetchOptions;", field.getPersistentName());
        endBlock();
        print("");

        printMethodJavaDoc();
        printDeprecated(field.deprecated);
        print("public boolean has%s()", field.getCapitalizedName());
        startBlock();
        print("return %s != null;", field.getPersistentName());
        endBlock();
        print("");

    }

    private void printFetchOptionsAccessorsJS(DTOField field)
    {
        print("this.fetch%s = function()", field.getCapitalizedName());
        startBlock();
        print("if (!this.%s)", field.getPersistentName());
        startBlock();
        print("this.%s = new %s();", field.getPersistentName(), field.fetchOptions.getSimpleName());
        endBlock();
        print("return this.%s;", field.getPersistentName());
        endBlock();
        print("");

        print("this.has%s = function()", field.getCapitalizedName());
        startBlock();
        print("return this.%s;", field.getPersistentName());
        endBlock();
        print("");
    }

    private void printClassHeader(String className, String jsonPackage, String extendsClass,
            Collection<String> implementedInterfaces)
    {
        print("/*");
        print(" * Class automatically generated with %s", this.getClass().getSimpleName());
        print(" */");
        print("@JsonObject(\"as.dto.%s.%s\")", jsonPackage, className);

        printDeprecated(deprecated);

        String extendsStr = "";
        if (extendsClass != null)
        {
            extendsStr = " extends " + extendsClass;
        }

        StringBuilder interfaces = new StringBuilder();
        if (implementedInterfaces != null)
        {
            for (String i : implementedInterfaces)
            {
                interfaces.append(", ");
                interfaces.append(i);
            }
        }
        print("public class %s%s implements Serializable%s", className, extendsStr,
                interfaces.toString());
    }

    private void printMethodJavaDoc()
    {
        print("// Method automatically generated with %s", this.getClass().getSimpleName());
    }

    private void printClassHeaderJS(String className)
    {
        print("var %s = function()", className);
    }

    private void printTypeJS(String className)
    {
        print("this['@type'] = '%s';", className);
    }

    private void printFields()
    {
        print("private static final long serialVersionUID = 1L;");
        print("");
        for (DTOField field : fields)
        {
            if(!field.isInherited)
            {
                printField(field);
            }
        }
    }

    private void printField(DTOField field)
    {
        printJsonPropertyAnnotation(field);

        printDeprecated(field.deprecated);

        print("private %s %s;", field.definitionClassName, field.getPersistentName());
        print("");
    }

    private void printFetchOptionsFields()
    {
        print("private static final long serialVersionUID = 1L;");
        print("");
        for (DTOField field : fields)
        {
            if (field.fetchOptions != null && field.fetchOptionsFieldName == null)
            {
                printFetchOptionField(field);
            }
        }

        print("@JsonProperty");
        print("private " + className + "SortOptions sort;");
        print("");
    }

    private void printFetchOptionField(DTOField field)
    {
        printJsonPropertyAnnotation(field);

        printDeprecated(field.deprecated);

        print("private %s %s;", field.fetchOptions.getSimpleName(), field.getPersistentName());
        print("");
    }

    private void printFetchOptionsStringBuilder()
    {
        print("@Override");
        print("protected FetchOptionsToStringBuilder getFetchOptionsStringBuilder()");
        startBlock();
        print("FetchOptionsToStringBuilder f = new FetchOptionsToStringBuilder(\"" + this.className + "\", this);");
        for (DTOField field : fields)
        {
            if (field.fetchOptions != null && field.fetchOptionsFieldName == null)
            {
                print("f.addFetchOption(\"" + field.getCapitalizedName() + "\", " + field.fieldName + ");");
            }
        }
        print("return f;");
        endBlock();
        print("");
    }

    protected void printJsonPropertyAnnotation(DTOField field)
    {
        if (field.fieldName.equalsIgnoreCase(field.getPersistentName()))
        {
            print("@JsonProperty");
        } else
        {
            print("@JsonProperty(value=\"%s\")", field.fieldName);
        }
    }

    private void printImports()
    {
        Set<String> imports = new TreeSet<String>();

        for (DTOField field : fields)
        {
            if (field.importClassName != null)
            {
                imports.add(field.importClassName);
            }
        }

        imports.addAll(additionalImports);

        for (String s : imports)
        {
            if (false == s.startsWith("java.lang"))
            {
                print("import %s;", s);
            }
        }
        print("");
    }

    private void printImportsForFetchOptions()
    {
        Set<String> imports = new TreeSet<String>();

        imports.add(JsonObject.class.getName());
        imports.add(JsonProperty.class.getName());
        imports.add(Serializable.class.getName());
        imports.add(FetchOptions.class.getName());
        imports.add("ch.ethz.sis.openbis.generic.asapi.v3.dto." + subPackage + "." + className);
        imports.add(
                "ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptionsToStringBuilder");

        for (DTOField field : fields)
        {
            if (field.fetchOptions != null && field.fetchOptionsFieldName == null)
            {
                imports.add(field.fetchOptions.getName());
            }
        }

        for (String s : imports)
        {
            print("import %s;", s);
        }
        print("");
    }

    private void printDeprecated(boolean deprecated)
    {
        if (deprecated)
        {
            print("@Deprecated");
        }
    }

    private void printPackage(String p)
    {
        print("package %s.%s;", PACKAGE_PREFIX, p);
        print("");
    }

    private void printToString()
    {
        if (toStringContent != null)
        {
            printMethodJavaDoc();
            print("@Override");
            print("public String toString()");
            startBlock();
            print("return %s;", toStringContent);
            endBlock();
            print("");
        }
    }

    private void printAdditionalMethods()
    {
        for (String additionalMethod : additionalMethods)
        {
            print(additionalMethod);
            print("");
        }
    }

}
