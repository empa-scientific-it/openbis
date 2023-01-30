package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.ExceptionUtils;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.server.business.bo.SampleCodeGeneratorByType;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleListingQuery;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.SampleRecord;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.exception.SampleUniqueCodeViolationException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

public class BatchSampleRegistrationTempCodeUpdaterTask implements IMaintenanceTask
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, BatchSampleRegistrationTempCodeUpdaterTask.class);

    private BatchSampleRegistrationTempCodeUpdaterBean bean;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
    }

    // TODO FJE: Do the update in single queue which is also fed by the
    // GenericClientService.updateTemporaryCodes()

    // this bean is programmatically created so that we can use spring managed transactions
    // in this maintenance task. Otherwise QueryTool needs to set the datasource/connection
    // explicitly and sampleDAO.createOrUpdateSamples fails because no session is found
    static class BatchSampleRegistrationTempCodeUpdaterBean
    {
        @Transactional
        public void execute()
        {
            IDAOFactory daoFactory = CommonServiceProvider.getDAOFactory();
            ISampleTypeDAO sampleTypeDAO = daoFactory.getSampleTypeDAO();
            ISampleDAO sampleDAO = daoFactory.getSampleDAO();

            ISampleListingQuery sampleListingQuery = QueryTool.getManagedQuery(ISampleListingQuery.class);
            DataIterator<SampleRecord> samplesWithTemporaryCodes = sampleListingQuery.getSamplesWithTemporaryCodes();
            // if no samples with TEMP codes are found, return
            if (samplesWithTemporaryCodes.hasNext() == false)
            {
                return;
            }

            // get system user
            ICommonServerForInternalUse server = CommonServiceProvider.getCommonServer();
            SessionContextDTO contextOrNull = server.tryToAuthenticateAsSystem();
            Person systemUser = null;
            if (contextOrNull != null)
            {
                final String sessionToken = contextOrNull.getSessionToken();
                List<Person> persons = server.listPersons(sessionToken);
                systemUser = persons.get(0);
            } else
            {
                throw new RuntimeException("Authentication failed.");
            }
            IPersonDAO personDAO = daoFactory.getPersonDAO();
            PersonPE personPE = personDAO.tryFindPersonByUserId(systemUser.getUserId());
            if (personPE == null)
            {
                throw new RuntimeException("User with id " + systemUser.getUserId() + " not found.");
            }

            // get all sample types
            List<SampleTypePE> sampleTypes = sampleTypeDAO.listSampleTypes();
            Map<Long, SampleTypePE> techIdsToSampleTypes = new HashMap<Long, SampleTypePE>();
            for (SampleTypePE sampleTypePE : sampleTypes)
            {
                techIdsToSampleTypes.put(sampleTypePE.getId(), sampleTypePE);
            }

            // helper maps
            Map<Long, SampleRecord> techIdsToSampleRecords = new HashMap<Long, SampleRecord>();
            Map<String, List<SampleRecord>> codePrefixToSampleRecords = new HashMap<String, List<SampleRecord>>();
            for (SampleRecord sampleRecord : samplesWithTemporaryCodes)
            {
                long sampleTypeId = sampleRecord.saty_id;
                String codePrefix = techIdsToSampleTypes.get(sampleTypeId).getGeneratedCodePrefix();
                List<SampleRecord> list = codePrefixToSampleRecords.get(codePrefix);
                if (list == null)
                {
                    list = new ArrayList<SampleRecord>();
                    codePrefixToSampleRecords.put(codePrefix, list);
                }
                list.add(sampleRecord);

                techIdsToSampleRecords.put(sampleRecord.id, sampleRecord);
            }
            operationLog.info(techIdsToSampleRecords.keySet().size() + " samples with temp codes were found.");

            Map<Long, String> techIdsToNewSampleCodes = new HashMap<Long, String>();
            SampleCodeGeneratorByType sampleCodeGeneratorByType = new SampleCodeGeneratorByType(daoFactory);
            for (String codePrefix : codePrefixToSampleRecords.keySet())
            {
                List<SampleRecord> sampleRecords = codePrefixToSampleRecords.get(codePrefix);
                int noOfCodesToGenerate = sampleRecords.size();
                List<String> generatedCodes =
                        sampleCodeGeneratorByType.generateCodes(codePrefix, EntityKind.SAMPLE, noOfCodesToGenerate);
                for (int i = 0; i < noOfCodesToGenerate; i++)
                {
                    techIdsToNewSampleCodes.put(sampleRecords.get(i).id, generatedCodes.get(i));
                }
            }

            // get SamplePE objects
            List<SamplePE> samplePEs = sampleDAO.listByIDs(techIdsToSampleRecords.keySet());
            for (SamplePE samplePE : samplePEs)
            {
                String newSampleCode = techIdsToNewSampleCodes.get(samplePE.getId());
                samplePE.setCode(newSampleCode);
            }

            // Update samplePEs with new codes
            sampleDAO.createOrUpdateSamples(samplePEs, personPE, true);
        }
    }

    @Override
    public void execute()
    {
        ApplicationContext applicationContext = CommonServiceProvider.getApplicationContext();

        operationLog.info("BatchSampleRegistrationTempCodeUpdaterTask started.");
        while (true)
        {
            try
            {
                getBean(applicationContext).execute();
                break;
            } catch (RuntimeException ex)
            {
                Throwable originalException = ExceptionUtils.getEndOfChain(ex);
                if (originalException instanceof SampleUniqueCodeViolationException == false)
                {
                    operationLog.error("Code updating failed: " + ex.getMessage(), ex);
                    break;
                }
            }
        }
        operationLog.info("BatchSampleRegistrationTempCodeUpdaterTask finished executing.");
    }

    private BatchSampleRegistrationTempCodeUpdaterBean getBean(ApplicationContext applicationContext)
    {
        if (bean == null)
        {
            AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
            bean = beanFactory.createBean(BatchSampleRegistrationTempCodeUpdaterBean.class);
        }
        return bean;
    }

}
