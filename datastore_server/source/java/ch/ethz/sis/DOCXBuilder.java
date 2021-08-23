package ch.ethz.sis;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;

import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTAltChunk;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.systemsx.cisd.common.http.JettyHttpClientFactory;
import com.github.freva.asciitable.AsciiTable;

public class DOCXBuilder
{
    public static void main(String[] args) throws Exception
    {
        DOCXBuilder docx = new DOCXBuilder();
        docx.addTitle("TitleA");
        docx.addHeader("MetaA");
        docx.addProperty("PropertyA", "ValueA");
        docx.addProperty("PropertyB", "ValueB");
        docx.addProperty("PropertyC",
                "<p>I am normal</p><p style=\"color:red;\">I am red</p><p style=\"color:blue;\">I am blue</p><p style=\"font-size:36px;\">I am big</p>");

        FileOutputStream out = new FileOutputStream(new File("wordFromHTML.docx"));
        out.write(docx.getDocBytes());
        out.close();
    }

    private static final String START_RICH_TEXT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<html><head></head><body>";

    private static final String END_RICH_TEXT = "</body></html>";

    private StringBuffer doc;

    private String closedDoc;

    private boolean closed;

    public DOCXBuilder()
    {
        System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
        closed = false;
        doc = new StringBuffer();
        startDoc();
    }

    public void setDocument(String doc)
    {
        this.doc = new StringBuffer(doc);
        closed = true;
    }

    private void startDoc()
    {
        if (!closed)
        {
            doc.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
            doc.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
            doc.append("<head></head>");
            doc.append("<body>");
        }
    }

    private void endDoc()
    {
        if (!closed)
        {
            doc.append("</body>");
            doc.append("</html>");
            closed = true;
            closedDoc = fixImages(doc);
        }
    }

    public void addProperty(String key, String value)
    {
        if (!closed)
        {
            doc.append("<p>").append("<b>").append(key).append(": ").append("</b>").append("</p>");
            addParagraph(value);
        }
    }

    public void addParagraph(String value)
    {
        if (!closed)
        {
            value = cleanXMLEnvelope(value);
            doc.append("<p>").append(value).append("</p>");
        }
    }

    public void addTitle(String title)
    {
        if (!closed)
        {
            doc.append("<h1>").append(title).append("</h1>");
        }
    }

    public void addHeader(String header)
    {
        if (!closed)
        {
            doc.append("<h2>").append(header).append("</h2>");
        }
    }

    public byte[] getHTMLBytes() throws Exception
    {
        if (!closed)
        {
            endDoc();
        }
        return closedDoc.getBytes();
    }

    public byte[] getDocBytes() throws Exception
    {
        // .. Finish Document
        if (!closed)
        {
            endDoc();
        }

        // .. HTML Code
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(new PartName("/hw.html"));
        afiPart.setBinaryData(closedDoc.getBytes());
        afiPart.setContentType(new ContentType("text/html"));
        Relationship altChunkRel = wordMLPackage.getMainDocumentPart().addTargetPart(afiPart);

        // .. the bit in document body
        CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk();
        ac.setId(altChunkRel.getId());
        wordMLPackage.getMainDocumentPart().addObject(ac);

        // .. content type
        wordMLPackage.getContentTypeManager().addDefaultContentType("html", "text/html");
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        wordMLPackage.save(outStream);

        return outStream.toByteArray();
    }

    private String cleanXMLEnvelope(String value)
    {
        if (value.startsWith(START_RICH_TEXT) && value.endsWith(END_RICH_TEXT))
        {
            value = value.substring(START_RICH_TEXT.length(), value.length() - END_RICH_TEXT.length());
        }
        return value;
    }

    private String fixImages(StringBuffer buffer)
    {
        Document jsoupDoc = Jsoup.parse(buffer.toString());
        jsoupDoc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        Elements elements = jsoupDoc.select("img");

        // Fixes images sizes
        for (Element element : elements)
        {
            String style = element.attr("style");
            if (style != null)
            {
                String[] rules = style.split(";");
                if (rules != null)
                {
                    for (int rIdx = 0; rIdx < rules.length; rIdx++)
                    {
                        String rule = rules[rIdx];
                        String[] ruleElements = rule.split(":");
                        if (ruleElements != null && ruleElements.length == 2)
                        {
                            String ruleKey = ruleElements[0].trim();
                            String ruleValue = ruleElements[1].trim();
                            if ((ruleKey.toLowerCase().equals("width") || ruleKey.toLowerCase().equals("height"))
                                    && ruleValue.endsWith("px"))
                            {
                                element.attr(ruleKey, ruleValue.substring(0, ruleValue.length() - 2));
                            }
                        }
                    }
                }
            }

            // Converts to Base64
            String src = element.attr("src");
            try
            {
                element.attr("src", getDataUriFromUri(src));
            } catch (Exception ex)
            {

            }
        }

        return jsoupDoc.html();
    }

    // private String encodeImgAsBase64(String value) {
    // Document doc = Jsoup.parse(value);
    // doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
    // Elements elements = doc.select("img");
    //
    // for (Element element : elements) {
    // String src = element.attr("src");
    // try {
    // element.attr("src", getDataUriFromUri(src));
    // } catch(Exception ex) {
    //
    // }
    // }
    //
    // return doc.html();
    // }

    private static String getDataUriFromUri(String url) throws Exception
    {
        HttpClient client = JettyHttpClientFactory.getHttpClient();
        Request requestEntity = client.newRequest(url).method("GET");
        ContentResponse contentResponse = requestEntity.send();
        return "data:" + contentResponse.getMediaType() + ";base64," + Base64.getEncoder().encodeToString(contentResponse.getContent());
    }

    // public static String convertJsonToText(final String jsonString) {
    // return AsciiTable.getTable(null, jsonStringToArray(jsonString));
    // }
    //
    // private static String[] jsonStringToArray(final String jsonString) {
    // final ArrayList<String> stringList = new ArrayList<String>();
    //
    // final JSONArray jsonArray = new JSONArray(jsonString);
    //
    // for (int i = 0; i < jsonArray.length(); i++) {
    // stringList.add(jsonArray.getString(i));
    // }
    //
    // return stringList.toArray();
    // }
}
