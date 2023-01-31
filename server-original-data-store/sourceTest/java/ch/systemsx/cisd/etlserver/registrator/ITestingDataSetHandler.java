package ch.systemsx.cisd.etlserver.registrator;

import java.io.File;

import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;

public interface ITestingDataSetHandler
{
    TestingDataSetHandlerExpectations getExpectations();

    void handle(File file);

    TopLevelDataSetRegistratorGlobalState getGlobalState();
}
