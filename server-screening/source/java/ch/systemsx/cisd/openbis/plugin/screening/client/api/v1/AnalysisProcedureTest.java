/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;

/**
 * A test class for the methods that take an analysis procedure.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class AnalysisProcedureTest
{
    public static void main(String[] args) throws IOException
    {
        if (args.length != 3)
        {
            System.err.println("Usage: <user> <password> <openbis-server-url>");
            System.err.println("Example parameters: test-user my-password http://localhost:8888");
            System.exit(1);
            return;
        }

        configureLogging();

        String userId = args[0];
        String userPassword = args[1];
        String serverUrl = args[2];

        print(String.format("Connecting to the server '%s' as a user '%s.", serverUrl, userId));
        IScreeningOpenbisServiceFacade facade =
                ScreeningOpenbisServiceFacadeFactory.tryCreate(userId, userPassword, serverUrl);
        if (facade == null)
        {
            System.err.println("Authentication failed: check the user name and password.");
            System.exit(1);
            return;
        }

        // Specify the permId of the plate we want to download images for.
        AnalysisProcedureTest newMe =
                new AnalysisProcedureTest(facade, "20110203094807621-79771", "proc1");

        newMe.runTest();

        newMe.logout();
    }

    private final IScreeningOpenbisServiceFacade facade;

    private final String permIdOfPlateInterest;

    private final String analysisProcedure;

    private AnalysisProcedureTest(IScreeningOpenbisServiceFacade facade, String permIdOfInterest,
            String analysisProcedure)
    {
        this.facade = facade;
        this.permIdOfPlateInterest = permIdOfInterest;
        this.analysisProcedure = analysisProcedure;
    }

    public void runTest() throws IOException
    {

        print("Listing all analysis procedures...");
        List<String> allAnalysisProcedures = listAllAnalysisProcedures();
        print("\t" + allAnalysisProcedures);
        print("Done.");

        List<Plate> plates = findPlates();
        List<FeatureVectorDatasetReference> allFeatureVectorsForPlate =
                facade.listFeatureVectorDatasets(plates);
        List<FeatureVectorDatasetReference> featureVectorMatchingAnalysisProcedure =
                facade.listFeatureVectorDatasets(plates, analysisProcedure);
        print("Out of " + allFeatureVectorsForPlate.size() + " feature vector data sets found "
                + featureVectorMatchingAnalysisProcedure.size()
                + "  data set(s) matching the procedure \"" + analysisProcedure + "\"");
        for (FeatureVectorDatasetReference dataSet : featureVectorMatchingAnalysisProcedure)
        {
            print("\t" + dataSet.getDatasetCode());
        }

        print("Finished.");

    }

    private List<String> listAllAnalysisProcedures()
    {
        ArrayList<String> allAnalysisProcedures = new ArrayList<String>();
        for (ExperimentIdentifier experimentIdentifier : facade.listExperiments())
        {
            allAnalysisProcedures.addAll(facade.listAnalysisProcedures(experimentIdentifier));
        }
        return allAnalysisProcedures;
    }

    private List<Plate> findPlates()
    {
        print("Looking for plates...");

        List<Plate> plates = facade.listPlates();
        List<Plate> platesOfInterest = new ArrayList<Plate>();

        // We are interested in just one plate
        for (Plate plate : plates)
        {
            if (plate.getPermId().equals(permIdOfPlateInterest))
            {
                platesOfInterest.add(plate);
                break;
            }
        }

        print("...found " + platesOfInterest.size() + " plate(s).");
        return platesOfInterest;
    }

    public void logout()
    {
        facade.logout();
    }

    private static void print(String msg)
    {
        System.out.println(new Date() + "\t" + msg);
    }

    private static void configureLogging()
    {
        Properties props = new Properties();
        props.put("log4j.appender.STDOUT", "org.apache.log4j.ConsoleAppender");
        props.put("log4j.appender.STDOUT.layout", "org.apache.log4j.PatternLayout");
        props.put("log4j.appender.STDOUT.layout.ConversionPattern", "%d %-5p [%t] %c - %m%n");
        props.put("log4j.rootLogger", "INFO, STDOUT");
        PropertyConfigurator.configure(props);
    }
}
