package ch.ethz.sis;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpStatus;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.http.JettyHttpClientFactory;

public class PlasmapperConnector
{
    public static void main(String[] args) throws Exception
    {
        // URL to the server without the dash '/'
        String serverURL = "http://wishart.biology.ualberta.ca";
        // Path to input fasta file
        String fastaInputFilePath = "FRP1955.fasta";
        // Path to output svg file
        String svgOutputFilePath = "FRP1955.svg";
        // Path to output gb file
        String gbOutputFilePath = "FRP1955.gb";
        // Path to output html file
        String htmlOutputFilePath = "FRP1955.html";

        createPlasmidDataSet(
                serverURL,
                fastaInputFilePath,
                svgOutputFilePath,
                gbOutputFilePath,
                htmlOutputFilePath);
    }

    private static final String VECTOR_MAP_URL = "/PlasMapper/servlet/DrawVectorMap";

    private static final String GENBANK_URL = "/PlasMapper/servlet/GenbankOutput";

    public static void createPlasmidDataSet(
            final String serverURL,
            final String fastaInputFilePath,
            final String svgOutputFilePath,
            final String gbOutputFilePath,
            final String htmlOutputFilePath)
    {
        File svgOutputFile = new File(svgOutputFilePath);

        try
        {
            downloadFromService(serverURL, GENBANK_URL, fastaInputFilePath, gbOutputFilePath, Boolean.TRUE);
            downloadFromService(serverURL, VECTOR_MAP_URL, fastaInputFilePath, svgOutputFilePath, Boolean.FALSE);
            generateHTML(htmlOutputFilePath, svgOutputFile);
        } catch (Exception ex)
        {
            throw new RuntimeException(ex);
        } finally
        {

        }
    }

    public static void downloadFromService(
            final String serverURL,
            final String service,
            final String fastaInputFilePath,
            final String outputFilePath,
            final Boolean allowContentToFile) throws Exception
    {
        HttpClient client = JettyHttpClientFactory.getHttpClient();
        Request requestEntity = client.newRequest(serverURL + service).method("POST");

        setPlasmidFormOptions(requestEntity, fastaInputFilePath);
        ContentResponse contentResponse = requestEntity.send();

        int statusCode = contentResponse.getStatus();

        if (statusCode != HttpStatus.Code.OK.getCode())
        {
            throw new RuntimeException("Status Code was " + statusCode + " instead of " + HttpStatus.Code.OK.getCode());
        }

        String contentAsString = contentResponse.getContentAsString();

        try
        {
            downloadFileFromResponse(serverURL, contentAsString, outputFilePath);
        } catch (Exception ex)
        {
            if (allowContentToFile)
            {
                FileUtils.writeStringToFile(new File(outputFilePath), contentAsString, "UTF-8");
            } else
            {
                throw ex;
            }
        }
    }

    private static void downloadFileFromResponse(
            final String serverURL,
            final String contentAsString,
            final String outputFilePath) throws Exception
    {
        String outputFileURLPath = contentAsString;
        if (outputFileURLPath == null || !outputFileURLPath.contains("PlasMapper"))
        {
            throw new RuntimeException("PlasMapper service failed returning incorrect path to file: " + outputFileURLPath);
        } else if (outputFileURLPath.endsWith("\r\n"))
        {
            outputFileURLPath = outputFileURLPath.substring(0, outputFileURLPath.lastIndexOf("\r\n"));
        } else if (outputFileURLPath.endsWith("\n"))
        {
            outputFileURLPath = outputFileURLPath.substring(0, outputFileURLPath.lastIndexOf("\n"));
        }

        if (outputFileURLPath.startsWith("/"))
        {
            outputFileURLPath = outputFileURLPath.substring(1);
        }
        URL outputFileURL = new URL(serverURL + "/" + outputFileURLPath);

        File svgOutputFile = new File(outputFilePath);
        FileUtils.copyURLToFile(outputFileURL, svgOutputFile);
    }

    private static void generateHTML(String htmlOutputFilePath, File svgOutputFile) throws Exception
    {
        String htmlFileContents = "<html>"
                + "<head>"
                + "<meta http-equiv=\"content-type\" content=\"text/html; charset=ISO-8859-1\">"
                + "<title>PlasMapper - Graphic Map</title>"
                + "</head>"
                + "<body>"
                + "<embed src=\"" + svgOutputFile.getName()
                + "\" type=\"image/svg+xml\" pluginspage=\"http://www.adobe.com/svg/viewer/install/\" id=\"Panel\" height=\"1010\" width=\"1010\">"
                + "<br>"
                + "<a href=\"" + svgOutputFile.getName() + "\" target=\"_blank\">Download Link</a>"
                + "</body>"
                + "</html>";
        FileUtils.writeStringToFile(new File(htmlOutputFilePath), htmlFileContents, "UTF-8");
    }

    private static void setPlasmidFormOptions(final Request requestEntity, final String fastaInputFile) throws FileNotFoundException
    {
        requestEntity.param("vendor", "Amersham%20Pharmacia");
        requestEntity.param("showOption", "1");
        requestEntity.param("showOption", "2");
        requestEntity.param("showOption", "3");
        requestEntity.param("showOption", "4");
        requestEntity.param("showOption", "5");
        requestEntity.param("showOption", "6");
        requestEntity.param("showOption", "7");
        requestEntity.param("showOption", "8");
        requestEntity.param("showOption", "9");
        requestEntity.param("restriction", "1");
        requestEntity.param("orfLen", "200");
        requestEntity.param("strand", "1");
        requestEntity.param("strand", "2");
        requestEntity.param("dir1", "1");
        requestEntity.param("dir2", "1");
        requestEntity.param("dir3", "1");
        requestEntity.param("dir4", "1");
        requestEntity.param("dir5", "1");
        requestEntity.param("dir6", "1");
        requestEntity.param("category1", "origin_of_replication");
        requestEntity.param("category2", "origin_of_replication");
        requestEntity.param("category3", "origin_of_replication");
        requestEntity.param("category4", "origin_of_replication");
        requestEntity.param("category5", "origin_of_replication");
        requestEntity.param("category6", "origin_of_replication");
        requestEntity.param("scheme", "0");
        requestEntity.param("shading", "0");
        requestEntity.param("labColor", "0");
        requestEntity.param("labelBox", "1");
        requestEntity.param("labels", "0");
        requestEntity.param("innerLabels", "0");
        requestEntity.param("legend", "0");
        requestEntity.param("arrow", "0");
        requestEntity.param("tickMark", "0");
        requestEntity.param("mapTitle", "");
        requestEntity.param("comment", "Created using PlasMapper");
        requestEntity.param("imageFormat", "SVG");
        requestEntity.param("imageSize", "1000 x 1000");
        requestEntity.param("backbone", "medium");
        requestEntity.param("arc", "medium");
        requestEntity.param("biomoby", "true");

        final String CRLF = "\r\n";
        final String BOUNDARY = "MMMMM___MP_BOUNDARY___MMMMM";
        final String FILE_PART_NAME = "fastaFile";

        File fastaFile = new File(fastaInputFile);
        String fileContent = FileUtilities.loadToString(fastaFile);
        ContentProvider content = new StringContentProvider("--" + BOUNDARY + CRLF
                + "Content-Disposition: form-data; name=\"" + FILE_PART_NAME + "\"; filename=\""
                + fastaFile.getName() + "\"" + CRLF
                + "Content-Type: application/octet-stream" + CRLF + CRLF
                + fileContent + CRLF + "--" + BOUNDARY + "--" + CRLF);
        requestEntity.content(content, "multipart/form-data; boundary=" + BOUNDARY);
    }
}
