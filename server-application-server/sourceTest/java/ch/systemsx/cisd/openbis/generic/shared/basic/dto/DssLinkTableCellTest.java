package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.ArrayList;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DssLinkTableCellTest extends AssertJUnit
{

    @Test
    public void testAgainstSimpleImageHTMLRenderer()
    {
        ArrayList<LinkModel.LinkParameter> parameters = new ArrayList<LinkModel.LinkParameter>();
        parameters.add(new LinkModel.LinkParameter("paramName", "paramValue"));

        LinkModel linkModel = new LinkModel();
        linkModel.setSchemeAndDomain("http://testdomain.com");
        linkModel.setPath("testPath");
        linkModel.setParameters(parameters);

        DssLinkTableCell cell = new DssLinkTableCell("linkText", linkModel);
        String cellHtml = cell.getHtmlString("sessionToken");

        URLMethodWithParameters urlMethod =
                new URLMethodWithParameters("http://testdomain.com/testPath");
        urlMethod.addParameter("paramName", "paramValue");
        urlMethod.addParameter(LinkModel.SESSION_ID_PARAMETER_NAME, "sessionToken");

        String basicHtml =
                URLMethodWithParameters.createEmbededLinkHtml("linkText", urlMethod.toString(),
                        "center");
        assertEquals(basicHtml, cellHtml);
    }
}
