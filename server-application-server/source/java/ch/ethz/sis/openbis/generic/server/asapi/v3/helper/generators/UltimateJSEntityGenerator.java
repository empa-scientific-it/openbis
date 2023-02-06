package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.generators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.generators.uglify.UglifyJS;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.generators.uglify.UglifyOptions;

public class UltimateJSEntityGenerator
{
    private static final String API_PROJECT_ROOT_FOLDER = "/Users/fedoreno/projects/work/openbis";

    private static final String API_PROJECT_SOURCE_FOLDER =
            API_PROJECT_ROOT_FOLDER + "/api-openbis-java/source/java/";

    @SuppressWarnings("unused")
    private static final String JSTEST_PROJECT_SOURCE_FOLDER = API_PROJECT_ROOT_FOLDER
            + "/test-api-openbis-javascript/servers/common/core-plugins/tests/1/as/webapps/openbis-v3-api-test/html/dto/";

    private static final String[] CLASSES_TO_CONVERT = new String[] {
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.attachment.AttachmentFetchOptions",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment",
            "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentUpdate",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentType",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentTypeFetchOptions",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.person.PersonFetchOptions",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.property.PropertyFetchOptions",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleType",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleTypeFetchOptions",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag",
            // "ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions"
    };

    public static void main(String[] args) throws IOException
    {
        for (String classToConvert : CLASSES_TO_CONVERT)
        {
            String javaClass = readFileAsString(
                    API_PROJECT_SOURCE_FOLDER + classToConvert.replace('.', '/') + ".java");
            String jsClass = translateFromJavaToJS(javaClass);
            // whriteStringAsFile(JSTEST_PROJECT_SOURCE_FOLDER + getSimpleName(classToConvert) + ".js", jsClass);
            System.out.println(jsClass);
            System.out.println(prettyPrint(jsClass));
        }
    }

    private static final String translateFromJavaToJS(String toTranslate)
    {
        return toTranslate
                // Remove Java specific features
                .replaceAll("package.+;", "")
                .replaceAll("import.+;", "")
                .replaceAll("@JsonObject.+\\)", "")
                .replaceAll("@JsonProperty", "")
                .replaceAll("@JsonIgnore", "")
                .replaceAll("@Override", "")
                .replaceAll("private static final long serialVersionUID.+;", "")
                // Remove generic parameters
                .replaceAll("<[\\w, ]+>", "")
                // And nested generic parameters
                .replaceAll("<[\\w, ]+>", "")
                // Remove array declarations
                .replaceAll("\\[\\]", "")
                // Translate private parameters to var
                .replaceAll("private\\s+\\w+\\s+", "var _")
                .replaceAll("this.", "_")
                // Translate Class to function
                .replaceAll("public class ([\\w]+)\\s*(?:implements Serializable)?\\s*[\\n\\s]*\\{",
                        "function $1()\n{\n\tthis['@type'] = '$1';")
                // Translate methods
                .replaceAll("public [\\w\\[\\]]+ ", "function ")
                // Remove function parameter types
                .replaceAll("function (\\w+)\\([\\w\\[\\]]+ ", "function $1(")
                // Special case for fetch* methods with parameters
                .replaceAll("function fetch(\\w+)\\(((\\w+))\\)", "function setFetch$1($2)")
                // Translate Exceptions
                .replaceAll("throw new .+\\(\"(.+)\"\\);", "throw '$1';")
                // Translate Equality operators
                .replaceAll("([\\w]+)\\s*==\\s*null", "!$1")
                .replaceAll("([\\w]+)\\s*!=\\s*null", "$1")
                .replaceAll("==", "===")
                .replaceAll("!=", "!==")
                // Remove Comments
                .replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "");
    }

    private static final void whriteStringAsFile(String filePath, String string) throws IOException
    {
        Writer out = null;
        try
        {
            out = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
            out.write(string);
        } finally
        {
            if (out != null)
            {
                out.close();
            }
        }
    }

    private static final String readFileAsString(String filePath) throws IOException
    {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1)
        {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    private static String prettyPrint(String jsClass) throws IOException
    {
        UglifyOptions options = new UglifyOptions();
        options.beautify = true;
        options.noMangle = true;
        options.noSqueeze = true;
        options.noSeqs = true;

        ArrayList<String> optionsArgList = options.toArgList();

        File temp = File.createTempFile("temp", "");
        whriteStringAsFile(temp.getAbsolutePath(), jsClass);

        optionsArgList.add(temp.getAbsolutePath());
        String[] args = new String[optionsArgList.size()];

        args = optionsArgList.toArray(args);
        UglifyJS uglifyjs = new UglifyJS();
        return uglifyjs.uglify(args);
    }
}
