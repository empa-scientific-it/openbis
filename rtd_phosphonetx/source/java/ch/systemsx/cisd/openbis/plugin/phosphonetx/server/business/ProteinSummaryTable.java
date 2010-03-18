/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.lemnik.eodsql.DataSet;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.ProteinReferenceWithProbabilityAndPeptide;

/**
 * @author Franz-Josef Elmer
 */
class ProteinSummaryTable extends AbstractBusinessObject implements IProteinSummaryTable
{
    private final static File STORE = new File("cache/proteinSummaryTables");

    static final double[] FDR_LEVELS = new double[]
        { 0, 0.01, 0.025, 0.05, 0.1 };

    private static final class Counter
    {
        private final double fdrLevel;

        private final Set<Long> proteins;

        private final Set<String> peptides;

        Counter(double fdrLevel)
        {
            this.fdrLevel = fdrLevel;
            proteins = new HashSet<Long>();
            peptides = new HashSet<String>();
        }

        public ProteinSummary getProteinSummary()
        {
            ProteinSummary proteinSummary = new ProteinSummary();
            proteinSummary.setFDR(fdrLevel);
            proteinSummary.setProteinCount(proteins.size());
            proteinSummary.setPeptideCount(peptides.size());
            return proteinSummary;
        }

        public void handle(double fdr, ProteinReferenceWithProbabilityAndPeptide protein)
        {
            if (fdr <= fdrLevel)
            {
                proteins.add(protein.getId());
                peptides.add(protein.getPeptideSequence());
            }
        }
    }

    private List<ProteinSummary> summaries;

    ProteinSummaryTable(IDAOFactory daoFactory, IPhosphoNetXDAOFactory specificDAOFactory,
            Session session)
    {
        super(daoFactory, specificDAOFactory, session);
    }

    public List<ProteinSummary> getProteinSummaries()
    {
        return summaries;
    }

    public void load(TechId experimentID)
    {
        summaries = tryLoadFromCache(experimentID);
        if (summaries != null)
        {
            return;
        }
        IExperimentDAO experimentDAO = getDaoFactory().getExperimentDAO();
        String permID = experimentDAO.getByTechId(experimentID).getPermId();
        IProteinQueryDAO dao = getSpecificDAOFactory().getProteinQueryDAO();
        ErrorModel errorModel = new ErrorModel(getSpecificDAOFactory());
        DataSet<ProteinReferenceWithProbabilityAndPeptide> resultSet =
                dao.listProteinsWithProbabilityAndPeptidesByExperiment(permID);
        List<Counter> counters = new ArrayList<Counter>(FDR_LEVELS.length);
        for (double fdrLevel : FDR_LEVELS)
        {
            counters.add(new Counter(fdrLevel));
        }
        try
        {
            for (ProteinReferenceWithProbabilityAndPeptide protein : resultSet)
            {
                long dataSetID = protein.getDataSetID();
                double probability = protein.getProbability();
                double fdr = errorModel.calculateFalsDiscoveryRate(dataSetID, probability);
                for (Counter counter : counters)
                {
                    counter.handle(fdr, protein);
                }
            }
            summaries = new ArrayList<ProteinSummary>(counters.size());
            for (Counter counter : counters)
            {
                summaries.add(counter.getProteinSummary());
            }
            saveToCache(experimentID, summaries);
        } finally
        {
            resultSet.close();
        }
    }

    @SuppressWarnings("unchecked")
    private List<ProteinSummary> tryLoadFromCache(TechId experimentID)
    {
        final File file = new File(STORE, experimentID.toString());
        if (file.exists())
        {
            ObjectInputStream inputStream = null;
            try
            {
                inputStream = new ObjectInputStream(new FileInputStream(file));
                return (List<ProteinSummary>) inputStream.readObject();
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            } finally
            {
                IOUtils.closeQuietly(inputStream);
            }
        } else
        {
            return null;
        }
    }

    private void saveToCache(TechId experimentID, List<ProteinSummary> summary)
    {
        STORE.mkdirs();
        final File file = new File(STORE, experimentID.toString());
        ObjectOutputStream outputStream = null;
        try
        {
            outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(summary);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(outputStream);
        }
    }
    
    void clearCache(TechId experimentID)
    {
        final File file = new File(STORE, experimentID.toString());
        if (file.exists())
        {
            file.delete();
        }
    }

}
