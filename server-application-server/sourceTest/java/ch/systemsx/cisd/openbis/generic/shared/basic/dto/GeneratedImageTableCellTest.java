package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class GeneratedImageTableCellTest extends AssertJUnit
{

    @Test
    public void testAgainstSimpleImageHTMLRenderer()
    {
        GeneratedImageTableCell cell = new GeneratedImageTableCell("servletName", 600, 300, 60, 30);
        cell.addParameter("code", "CODE_8472");
        String cellHTML = cell.getHTMLString("http://my.server.ch", "sessionToken");
        String rendererHTML =
                URLMethodWithParameters
                        .createEmbededImageHtml(
                                "http://my.server.ch/servletName?sessionID=sessionToken&code=CODE_8472&w=60&h=30",
                                "http://my.server.ch/servletName?sessionID=sessionToken&code=CODE_8472&w=600&h=300",
                                60, 30);
        assertEquals(cellHTML, rendererHTML);
    }
}
