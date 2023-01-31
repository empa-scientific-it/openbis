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

package ch.systemsx.cisd.openbis.dss.client.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.ssl.SslCertificateHelper;
import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.content.DssServiceRpcGenericFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractCommand
{
    static final String BASH_COMMAND = "share-manager.sh";

    private final String name;

    protected String sessionToken;

    protected IDssServiceRpcGeneric service;

    AbstractCommand(String name)
    {
        this.name = name;
    }

    String getName()
    {
        return name;
    }

    void parseArguments(String[] args)
    {
        CommonArguments arguments = getArguments();
        CmdLineParser parser = new CmdLineParser(arguments);
        try
        {
            parser.parseArgument(args);
            if (arguments.isComplete() == false)
            {
                throw new IllegalArgumentException();
            }
        } catch (Exception ex)
        {
            StringWriter writer = new StringWriter();
            PrintWriter out = new PrintWriter(writer, true);
            String cmdString = BASH_COMMAND + " " + getName();
            out.println("Usage: " + cmdString + " [options] " + getRequiredArgumentsString());
            parser.printUsage(writer, null);
            out.println("Example: " + cmdString + parser.printExample(ExampleMode.ALL) + " "
                    + getRequiredArgumentsString());
            throw new UserFailureException(writer.toString());
        }
    }

    void login()
    {
        String username = getArguments().getUsername();
        if (StringUtils.isBlank(username))
        {
            throw new UserFailureException("Unspecified user name.");
        }
        String password = getArguments().getPassword();
        if (StringUtils.isBlank(password))
        {
            throw new UserFailureException("Unspecified password.");
        }
        Properties properties = loadServiceProperties();
        String openBisServerUrl = DssPropertyParametersUtil.getOpenBisServerUrl(properties);
        IGeneralInformationService infoService = createGeneralInfoService(openBisServerUrl);
        sessionToken = infoService.tryToAuthenticateForAllServices(username, password);
        if (sessionToken == null)
        {
            throw new UserFailureException("Invalid username/password combination.");
        }
        String downloadUrl = DssPropertyParametersUtil.getDownloadUrl(properties);
        service = createDssService(downloadUrl);
    }

    IDssServiceRpcGeneric createDssService(String downloadUrl)
    {
        SslCertificateHelper.trustAnyCertificate(downloadUrl);
        return new DssServiceRpcGenericFactory().getService(downloadUrl);
    }

    IGeneralInformationService createGeneralInfoService(String openBisServerUrl)
    {
        SslCertificateHelper.trustAnyCertificate(openBisServerUrl);
        ServiceFinder serviceFinder =
                new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        return serviceFinder.createService(IGeneralInformationService.class, openBisServerUrl);
    }

    private Properties loadServiceProperties()
    {
        CommonArguments arguments = getArguments();
        File servicPropertiesFile = arguments.getServicPropertiesFile();
        FileReader reader = null;
        try
        {
            reader = new FileReader(servicPropertiesFile);
            Properties properties = new Properties();
            properties.load(reader);
            return ExtendedProperties.createWith(properties);
        } catch (FileNotFoundException ex)
        {
            throw new UserFailureException("DSS service.properties file not found: "
                    + servicPropertiesFile.getAbsolutePath()
                    + (arguments.isServicePropertiesPathSpecified() ? ""
                            : "\nUse option -sp to specify it."));
        } catch (IOException ex)
        {
            throw new UserFailureException("Error while loading '"
                    + servicPropertiesFile.getAbsolutePath() + "': " + ex, ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    protected abstract CommonArguments getArguments();

    protected abstract String getRequiredArgumentsString();

    abstract void execute();
}
