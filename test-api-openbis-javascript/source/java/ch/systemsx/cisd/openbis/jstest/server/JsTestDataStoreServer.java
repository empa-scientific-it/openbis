/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.jstest.server;

import java.io.File;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.test.server.TestDataStoreServer;

/**
 * @author pkupczyk
 */
public abstract class JsTestDataStoreServer extends TestDataStoreServer
{
    public static final String EMAIL_FOLDER = "data/email";

    @Override
    protected String getCommand()
    {
        String classpath = System.getProperty("selenium.dss-runtime-classpath");
        if (classpath == null || classpath.length() == 0)
        {
            classpath = System.getProperty("java.class.path");
        }

        try
        {
            File extraGradle4ClassPath = new File("../../../targets/gradle/classes/java/test");
            File extraGradle3ClassPath = new File("../../../targets/gradle/classes/test");
            if (extraGradle4ClassPath.exists())
            {
                classpath += ":" + (extraGradle4ClassPath).getCanonicalPath();
            } else if (extraGradle3ClassPath.exists())
            {
                classpath += ":" + (extraGradle3ClassPath).getCanonicalPath();
            } else
            {
                throw new RuntimeException(
                        "Classpath Missing: " + extraGradle3ClassPath + " or " + extraGradle3ClassPath);
            }
            classpath +=
                    ":../../../../core-plugin-openbis/dist/core-plugins/eln-lims/1/dss/reporting-plugins/eln-lims-api/lib/htmlcleaner-2.23.jar";
            classpath +=
                    ":../../../../core-plugin-openbis/dist/core-plugins/eln-lims/1/dss/reporting-plugins/eln-lims-api/lib/plasmapper.jar";
            classpath +=
                    ":../../../../core-plugin-openbis/dist/core-plugins/eln-lims/1/dss/reporting-plugins/eln-lims-api/lib/zip4j_1.3.2.jar";
        } catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Extra classpath missing, check JsTestDataStoreServer for details.");
            System.exit(-1);
        }

        return "java -ea  --add-opens=java.base/java.util=ALL-UNNAMED -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address="
                + getDebugPort()
                + " -Dfile.encoding=UTF-8 -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StrErrLog -classpath " + classpath
                + " ch.systemsx.cisd.openbis.dss.generic.DataStoreServer";
    }

    public void cleanUp()
    {
        File emailFolder = new File(getRootPath(), EMAIL_FOLDER);
        System.out.println("DELETE EMAIL FOLDER " + emailFolder.getAbsolutePath());
        FileUtilities.deleteRecursively(emailFolder);
    }
}
