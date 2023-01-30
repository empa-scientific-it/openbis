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

package ch.systemsx.cisd.openbis.generic.client.cli;

import java.io.IOException;
import java.util.List;

import jline.ConsoleReader;

import ch.systemsx.cisd.openbis.common.api.client.ServiceFinder;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;

/**
 * A rudimentary example of using the openBIS IGeneralInformationService API to find data sets.
 * <p>
 * The main method gets a reference to the openBIS informational API and then uses this API to list samples and data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetLister
{
    public static void main(String[] args) throws Exception
    {
        String serverUrl = args[0];
        String userId = args[1];

        String password;
        if (args.length == 3)
        {
            password = args[2];
        } else
        {
            password = getConsoleReader().readLine("Password: ", Character.valueOf('*'));
        }

        // Get a reference to the service
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        IGeneralInformationService service =
                generalInformationServiceFinder.createService(IGeneralInformationService.class,
                        serverUrl);

        // Log in
        String sessionToken = service.tryToAuthenticateForAllServices(userId, password);

        // Create the lister, providing the session token it will use for future requests
        DataSetLister me = new DataSetLister(service, sessionToken);

        // Find all data sets attached to samples with the property DESCRIPTION.
        me.findDataSetsForSamplesWithProperty("DESCRIPTION", "*");
    }

    private static ConsoleReader getConsoleReader()
    {
        try
        {
            return new ConsoleReader();
        } catch (final IOException ex)
        {
            throw new EnvironmentFailureException("ConsoleReader could not be instantiated.", ex);
        }
    }

    private final IGeneralInformationService generalInformationService;

    private final String sessionToken;

    public DataSetLister(IGeneralInformationService generalInformationService, String sessionToken)
    {
        this.generalInformationService = generalInformationService;
        this.sessionToken = sessionToken;
    }

    /**
     * An example that finds all data sets. Depending on the size of the DB, this could be very slow.
     */
    @SuppressWarnings("unused")
    private void findAllDataSets()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*"));
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, sc);
        List<DataSet> result = generalInformationService.listDataSets(sessionToken, samples);

        System.out.println("Number of data sets:\n" + result.size());
        System.out.println("\n");
    }

    /**
     * Find data sets with a specific value for a property.
     */
    private void findDataSetsForSamplesWithProperty(String property, String desiredValue)
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createPropertyMatch(property, desiredValue));
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, sc);
        List<DataSet> result = generalInformationService.listDataSets(sessionToken, samples);

        StringBuilder sb = new StringBuilder();
        sb.append("Found ");
        sb.append(samples.size());
        sb.append(" sample(s) with ");
        sb.append(property);
        sb.append("=");
        sb.append(desiredValue);
        sb.append(". Data sets attached to these samples:\n");
        sb.append(result);

        System.out.println(sb.toString());
        System.out.println("\n");
    }

    /**
     * Find data sets with a specific value for a property.
     */
    @SuppressWarnings("unused")
    private void findDataSetsForSamplesWithCode(String code)
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, code));
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, sc);
        List<DataSet> result = generalInformationService.listDataSets(sessionToken, samples);

        StringBuilder sb = new StringBuilder();
        sb.append("Found ");
        sb.append(samples.size());
        sb.append(" sample(s) with code");
        sb.append("=");
        sb.append(code);
        sb.append(". Data sets attached to these samples:\n");
        sb.append(result);

        System.out.println(sb.toString());
        System.out.println("\n");
    }
}