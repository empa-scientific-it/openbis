/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.dsl;

import static org.hamcrest.CoreMatchers.not;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.hamcrest.Matcher;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.uitest.dsl.matcher.CellContentMatcher;
import ch.systemsx.cisd.openbis.uitest.dsl.matcher.CollectionContainsExactlyMatcher;
import ch.systemsx.cisd.openbis.uitest.dsl.matcher.CollectionContainsMatcher;
import ch.systemsx.cisd.openbis.uitest.dsl.matcher.CurrentPageMatcher;
import ch.systemsx.cisd.openbis.uitest.dsl.matcher.RegisterSampleFormContainsInputsForPropertiesMatcher;
import ch.systemsx.cisd.openbis.uitest.dsl.matcher.RowExistsMatcher;
import ch.systemsx.cisd.openbis.uitest.dsl.type.Builder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.DataSetBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.DataSetPropertyTypeAssignmentBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.DataSetTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.ExperimentBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.ExperimentTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.MaterialBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.MaterialTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.MetaProjectBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.ProjectBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.PropertyTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.SampleBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.SamplePropertyTypeAssignmentBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.SampleTypeBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.SampleTypeUpdateBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.SampleUpdateBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.ScriptBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.SpaceBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.UpdateBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.UserBuilder;
import ch.systemsx.cisd.openbis.uitest.dsl.type.VocabularyBuilder;
import ch.systemsx.cisd.openbis.uitest.gui.DeleteExperimentTypeGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeleteExperimentsOfProjectGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeleteProjectGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeletePropertyTypeGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeleteSampleGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeleteSampleTypeGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeleteSpaceGui;
import ch.systemsx.cisd.openbis.uitest.gui.DeleteVocabularyGui;
import ch.systemsx.cisd.openbis.uitest.gui.EmptyTrashGui;
import ch.systemsx.cisd.openbis.uitest.gui.GeneralBatchImportGui;
import ch.systemsx.cisd.openbis.uitest.gui.LoginGui;
import ch.systemsx.cisd.openbis.uitest.gui.LogoutGui;
import ch.systemsx.cisd.openbis.uitest.gui.RegisterSampleBatchGui;
import ch.systemsx.cisd.openbis.uitest.layout.Location;
import ch.systemsx.cisd.openbis.uitest.layout.RegisterSampleLocation;
import ch.systemsx.cisd.openbis.uitest.layout.SampleBrowserLocation;
import ch.systemsx.cisd.openbis.uitest.layout.UserSettingsDialogLocation;
import ch.systemsx.cisd.openbis.uitest.menu.TabBar;
import ch.systemsx.cisd.openbis.uitest.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.page.Browsable;
import ch.systemsx.cisd.openbis.uitest.page.BrowserRow;
import ch.systemsx.cisd.openbis.uitest.page.RegisterSample;
import ch.systemsx.cisd.openbis.uitest.page.SampleDetails;
import ch.systemsx.cisd.openbis.uitest.rmi.AddEntitiesToMetaProjectRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.AggregationReportRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.GetDataSetMetaDataRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListDataSetsOfExperimentsOnBehalfOfUserRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListDataSetsOfExperimentsRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListDataSetsOfSampleRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListDataSetsOfSamplesOnBehalfOfUserRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListDataSetsOfSamplesRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListDataSetsOfSamplesWithConnectionsRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListExperimentsHavingDataSetsOfProjectsRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListExperimentsHavingSamplesOfProjectsRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListExperimentsOfProjectsRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListExperimentsRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListMaterialsRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListMetaProjectsRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListSamplesOfExperimentOnBehalfOfUserRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ListSamplesOfExperimentRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.ReportFromDataSetsRmi;
import ch.systemsx.cisd.openbis.uitest.screenshot.FileScreenShotter;
import ch.systemsx.cisd.openbis.uitest.screenshot.ScreenShotter;
import ch.systemsx.cisd.openbis.uitest.type.BrowsableWrapper;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;
import ch.systemsx.cisd.openbis.uitest.type.DataSetType;
import ch.systemsx.cisd.openbis.uitest.type.Entity;
import ch.systemsx.cisd.openbis.uitest.type.Experiment;
import ch.systemsx.cisd.openbis.uitest.type.ExperimentType;
import ch.systemsx.cisd.openbis.uitest.type.GeneralBatchImportFile;
import ch.systemsx.cisd.openbis.uitest.type.ImportFile;
import ch.systemsx.cisd.openbis.uitest.type.Material;
import ch.systemsx.cisd.openbis.uitest.type.MetaProject;
import ch.systemsx.cisd.openbis.uitest.type.Project;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeAssignment;
import ch.systemsx.cisd.openbis.uitest.type.PropertyTypeDataType;
import ch.systemsx.cisd.openbis.uitest.type.Sample;
import ch.systemsx.cisd.openbis.uitest.type.SampleType;
import ch.systemsx.cisd.openbis.uitest.type.Script;
import ch.systemsx.cisd.openbis.uitest.type.ScriptType;
import ch.systemsx.cisd.openbis.uitest.type.Space;
import ch.systemsx.cisd.openbis.uitest.type.User;
import ch.systemsx.cisd.openbis.uitest.type.Vocabulary;
import ch.systemsx.cisd.openbis.uitest.uid.DictionaryUidGenerator;
import ch.systemsx.cisd.openbis.uitest.uid.UidGenerator;
import ch.systemsx.cisd.openbis.uitest.webdriver.Pages;
import ch.systemsx.cisd.openbis.uitest.widget.Widget;

public abstract class SeleniumTest
{
    public static int IMPLICIT_WAIT = 30;

    public static String ADMIN_USER = "selenium";

    public static String ADMIN_PASSWORD = "selenium4CISD";

    public static WebDriver driver;

    private static UidGenerator uid;

    private static Application openbis;

    private static Ui ui;

    private static Ui defaultUi;

    private static Pages pages;

    private static String asUrl;

    private static String dssUrl;

    private static String dssUrl2;

    private static String startPage;

    private static Console console = new Console();

    @BeforeSuite
    public void initialization() throws Exception
    {
        deleteOldScreenShots();
        initializeLogging();
        uid = new DictionaryUidGenerator(new File("resource/corncob_lowercase.txt"));

        asUrl = getSystemPropertyOrNull("ui-test.as-url");
        dssUrl = getSystemPropertyOrNull("ui-test.dss-url");
        dssUrl2 = getSystemPropertyOrNull("ui-test.dss-url2");
        startPage = getSystemPropertyOrNull("ui-test.start-page");

        /* Run against sprint server */
        /*
         * asUrl = "https://sprint-openbis.ethz.ch/openbis"; dssUrl = "https://sprint-openbis.ethz.ch"; startPage = asUrl;
         */

        /* Run against local DSS and local AS in development mode */
        /* Firefox profile should be one with GWT dev mode plugin available */
        /*
         * asUrl = "http://127.0.0.1:8888"; dssUrl = "http://127.0.0.1:8889"; startPage = asUrl +
         * "/ch.systemsx.cisd.openbis.OpenBIS/index.html?gwt.codesvr=127.0.0.1:9997"; System.setProperty("webdriver.firefox.profile", "default");
         */

        if (asUrl == null)
        {
            asUrl = startApplicationServer();

            if (startPage == null)
            {
                startPage = asUrl;
            }
        }

        if (dssUrl == null)
        {
            dssUrl = startDataStoreServer();
        }

        if (dssUrl2 == null)
        {
            dssUrl2 = startDataStoreServer2();
        }

        System.out.println("asUrl: " + asUrl);
        System.out.println("dssUrl: " + dssUrl);
        System.out.println("dssUrl2: " + dssUrl2);
        System.out.println("startPage: " + startPage);

        pages = new Pages();
        openbis = new Application(asUrl, dssUrl, dssUrl2, pages, console);

    }

    private String getSystemPropertyOrNull(String propertyName)
    {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue == null || propertyValue.trim().length() == 0)
        {
            return null;
        } else
        {
            return propertyValue;
        }
    }

    protected String startApplicationServer() throws Exception
    {
        return StartApplicationServer.go();
    }

    protected String startDataStoreServer() throws Exception
    {
        return StartDataStoreServer.go();
    }

    protected String startDataStoreServer2() throws Exception
    {
        // FIXME dss2 is started together with dss1 in StartDataStoreServer.go()
        return "http://localhost:10002";
    }

    private void startWebDriver()
    {
        if (driver == null)
        {
            FirefoxOptions opts = new FirefoxOptions();
            // If you use headless option selenium will work fine but Firefox won't be visible on the screen.
            opts.addArguments("--headless");

            driver = new FirefoxDriver(opts);
            setImplicitWaitToDefault();
            driver.manage().deleteAllCookies();

            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenResolution =
                    new Dimension((int) toolkit.getScreenSize().getWidth(), (int) toolkit
                            .getScreenSize().getHeight());
            driver.manage().window().setSize(screenResolution);
        }
        driver.get(startPage);
    }

    private void deleteOldScreenShots()
    {
        delete(new File("targets/dist"));

    }

    private void initializeLogging()
    {
        Logger rootLogger = Logger.getRootLogger();
        if (rootLogger.getAllAppenders().hasMoreElements())
        {
            throw new IllegalStateException("log4j has appenders!");
        }
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(new ConsoleAppender(new PatternLayout(
                "%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%t]: %m%n")));

        WriterAppender appender =
                new WriterAppender(
                        new PatternLayout("%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%t]: %m%n"), console);
        rootLogger.addAppender(appender);
    }

    @AfterSuite
    public void closeBrowser() throws Exception
    {
        if (driver != null)
        {
            driver.quit();
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void initPageProxy(Method method) throws Exception
    {
        System.out.println("--- " + method.getDeclaringClass().getSimpleName() + "."
                + method.getName() + "() STARTS ---");

        ScreenShotter shotter;
        if (driver != null)
        {
            shotter =
                    new FileScreenShotter((TakesScreenshot) driver, "targets/dist/"
                            + this.getClass().getSimpleName() + "/" + method.getName());
        } else
        {
            shotter = new ScreenShotter()
                {
                    @Override
                    public void screenshot()
                    {
                    }
                };
        }

        pages.setScreenShotter(shotter);
    }

    @AfterMethod(alwaysRun = true)
    public void takeScreenShot(Method method) throws IOException
    {
        pages.screenshot();
        System.out.println("--- " + method.getDeclaringClass().getSimpleName() + "."
                + method.getName() + "() ENDS ---");
    }

    @AfterTest
    public void takeScreenShot()
    {
        pages.screenshot();
    }

    private void delete(File f)
    {
        if (f.isDirectory())
        {
            for (File c : f.listFiles())
                delete(c);
        }
        f.delete();
    }

    public <T> T using(Void anything, T t)
    {
        ui = defaultUi;
        return t;
    }

    public <T> T as(User user, T t)
    {
        openbis.changeLogin(assume(aUser().withName(SeleniumTest.ADMIN_USER)));

        if (ui.equals(Ui.WEB))
        {
            logout();
            login(SeleniumTest.ADMIN_USER, "pwd");
            TabBar tabs = pages.load(TabBar.class);
            for (String tab : tabs.getTabs())
            {
                pages.load(TabBar.class).closeTab(tab);
            }

        }
        return t;
    }

    public User user(User user)
    {
        openbis.changeLogin(user);

        if (ui.equals(Ui.WEB))
        {
            logout();
            login(user.getName(), "pwd");
            TabBar tabs = pages.load(TabBar.class);
            for (String tab : tabs.getTabs())
            {
                pages.load(TabBar.class).closeTab(tab);
            }

        }

        return user;
    }

    public User user(UserBuilder builder)
    {
        return user(using(publicApi(), create(builder)));
    }

    public static void setImplicitWait(long amount, TimeUnit unit)
    {
        driver.manage().timeouts().implicitlyWait(amount, unit);
    }

    public static void setImplicitWaitToDefault()
    {
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT, TimeUnit.SECONDS);
    }

    public void logout()
    {
        openbis.execute(new LogoutGui());
    }

    protected void login(String user, String password)
    {
        openbis.execute(new LoginGui(user, password));
    }

    public String loggedInAs()
    {
        TopBar t = pages.tryLoad(TopBar.class);
        if (t != null)
        {
            return t.getUserName();
        } else
        {
            return null;
        }
    }

    public boolean tabsContain(Location<?> location)
    {
        TabBar bar = assumePage(TabBar.class);
        return bar.getTabs().contains(location.getTabName());
    }

    public <T> T switchTabTo(Location<T> location)
    {
        TabBar bar = assumePage(TabBar.class);
        bar.selectTab(location.getTabName());
        return assumePage(location.getPage());
    }

    public void closeTab(Location<?> location)
    {
        TabBar bar = assumePage(TabBar.class);
        bar.closeTab(location.getTabName());
    }

    protected SampleBrowserLocation sampleBrowser()
    {
        return new SampleBrowserLocation();
    }

    protected UserSettingsDialogLocation userSettings()
    {
        return new UserSettingsDialogLocation();
    }

    protected Collection<SampleType> sampleTypesInSampleBrowser()
    {
        Set<SampleType> types = new HashSet<SampleType>();

        for (String code : pages.goTo(sampleBrowser()).getSampleTypes())
        {
            types.add(assume(aSampleType().withCode(code)));
        }
        return types;
    }

    protected List<MetaProject> listOfAllMetaProjects()
    {
        return openbis.execute(new ListMetaProjectsRmi());
    }

    protected Pages browser()
    {
        return pages;
    }

    protected BrowserRow browserEntryOf(DataSetType type)
    {
        return getRow(new BrowsableWrapper(type));
    }

    protected BrowserRow browserEntryOf(ExperimentType type)
    {
        return getRow(new BrowsableWrapper(type));
    }

    protected BrowserRow browserEntryOf(Project project)
    {
        return getRow(new BrowsableWrapper(project));
    }

    protected BrowserRow browserEntryOf(PropertyType type)
    {
        return getRow(new BrowsableWrapper(type));
    }

    protected BrowserRow browserEntryOf(PropertyTypeAssignment assignment)
    {
        return getRow(new BrowsableWrapper(assignment));
    }

    protected BrowserRow browserEntryOf(SampleType type)
    {
        return getRow(new BrowsableWrapper(type));
    }

    protected BrowserRow browserEntryOf(Sample sample)
    {
        return getRow(new BrowsableWrapper(sample));
    }

    protected BrowserRow browserEntryOf(Script script)
    {
        return getRow(new BrowsableWrapper(script));
    }

    protected BrowserRow browserEntryOf(Space space)
    {
        return getRow(new BrowsableWrapper(space));
    }

    protected BrowserRow browserEntryOf(Vocabulary vocabulary)
    {
        return getRow(new BrowsableWrapper(vocabulary));
    }

    private BrowserRow getRow(Browsable browsable)
    {
        return pages.goTo(browsable.getBrowserLocation()).getRow(browsable);
    }

    protected SampleDetails detailsOf(Sample sample)
    {
        // return openbis.browseToDetailsOf(sample);
        return null;
    }

    protected void emptyTrash()
    {
        openbis.execute(new EmptyTrashGui());
    }

    public RegisterSample sampleRegistrationPageFor(SampleType type)
    {
        pages.goTo(new RegisterSampleLocation()).selectSampleType(type);
        return assumePage(RegisterSample.class);
    }

    public <T> T assumePage(Class<T> pageClass)
    {
        return pages.load(pageClass);
    }

    public Matcher<Pages> displays(Class<?> pageClass)
    {
        return new CurrentPageMatcher(pageClass);
    }

    public Matcher<BrowserRow> hasSpace(Space space)
    {
        return containsValue("Space", space.getCode());
    }

    public Matcher<BrowserRow> hasContainer(Sample sample)
    {
        return containsValue("Container",
                "/" + sample.getSpace().getCode() + "/" + sample.getCode());
    }

    public Matcher<BrowserRow> hasNoContainer()
    {
        return containsValue("Container", "");
    }

    protected Matcher<RegisterSample> hasInputsForProperties(PropertyType... fields)
    {
        return new RegisterSampleFormContainsInputsForPropertiesMatcher(fields);
    }

    protected Matcher<BrowserRow> containsValue(String column, String value)
    {
        return new CellContentMatcher(column, value, false);
    }

    protected Matcher<BrowserRow> exists()
    {
        return new RowExistsMatcher();
    }

    protected Matcher<BrowserRow> doesNotExist()
    {
        return not(new RowExistsMatcher());
    }

    protected Matcher<BrowserRow> containsLink(String column, String value)
    {
        return new CellContentMatcher(column, value, true);
    }

    protected <T> Matcher<Collection<T>> contain(T t)
    {
        return new CollectionContainsMatcher<T>(t);
    }

    protected <T> Matcher<Collection<T>> contains(T t)
    {
        return new CollectionContainsMatcher<T>(t);
    }

    protected <T> Matcher<Collection<T>> doNotContain(T t)
    {
        return not(new CollectionContainsMatcher<T>(t));
    }

    protected <T> Matcher<Collection<T>> doesNotContain(T t)
    {
        return not(new CollectionContainsMatcher<T>(t));
    }

    protected <T> Matcher<Collection<T>> containsExactly(T... t)
    {
        return new CollectionContainsExactlyMatcher<T>(t);
    }

    protected <T> Matcher<Collection<T>> containExactly(T... t)
    {
        return new CollectionContainsExactlyMatcher<T>(t);
    }

    public <T> T create(Builder<T> builder)
    {
        T t = builder.build(openbis, ui);
        return t;
    }

    protected GeneralBatchImportFileBuilder aGeneralBatchImportFile()
    {
        return new GeneralBatchImportFileBuilder();
    }

    public <T extends ImportFile> T create(T importFile)
    {
        return importFile;
    }

    public <T extends ImportFile> T in(T importFile)
    {
        return importFile;
    }

    public Sample create(ImportFile file, Builder<Sample> builder, IdentifiedBy identifiedBy)
    {
        Sample sample = assume(builder);

        IdentifiedBy idType;
        if (identifiedBy == null)
        {
            idType =
                    sample.getContainer() == null ? IdentifiedBy.SPACE_AND_CODE
                            : sample.getSpace() == null ? IdentifiedBy.SUBCODE
                                    : IdentifiedBy.SPACE_AND_CODE;
        } else
        {
            idType = identifiedBy;
        }
        file.add(sample, idType);
        return sample;
    }

    public Sample create(ImportFile file, Builder<Sample> builder)
    {
        return create(file, builder, null);
    }

    protected <T> T assume(Builder<T> builder)
    {
        return using(dummyApplication(), builder.build(openbis, ui));
    }

    protected Void delete(Space space)
    {
        openbis.execute(new DeleteSpaceGui(space));
        return null;
    }

    protected void deleteExperimentsFrom(Project project)
    {
        openbis.execute(new DeleteExperimentsOfProjectGui(project));
    }

    protected void delete(Project project)
    {
        openbis.execute(new DeleteProjectGui(project));
    }

    protected void delete(Sample sample)
    {
        openbis.execute(new DeleteSampleGui(sample));
    }

    protected void delete(SampleType sampleType)
    {
        openbis.execute(new DeleteSampleTypeGui(sampleType));
    }

    protected void delete(ExperimentType experimentType)
    {
        openbis.execute(new DeleteExperimentTypeGui(experimentType));
    }

    protected void delete(PropertyType propertyType)
    {
        openbis.execute(new DeletePropertyTypeGui(propertyType));
    }

    protected void delete(Vocabulary vocabulary)
    {
        openbis.execute(new DeleteVocabularyGui(vocabulary));
    }

    protected UserBuilder aUser()
    {
        return new UserBuilder(uid);
    }

    public SpaceBuilder aSpace()
    {
        return new SpaceBuilder(uid);
    }

    protected ProjectBuilder aProject()
    {
        return new ProjectBuilder(uid);
    }

    protected MetaProjectBuilder aMetaProject()
    {
        return new MetaProjectBuilder(uid);
    }

    protected SampleTypeBuilder aSampleType()
    {
        return new SampleTypeBuilder(uid);
    }

    protected ExperimentTypeBuilder anExperimentType()
    {
        return new ExperimentTypeBuilder(uid);
    }

    protected SampleBuilder aSample()
    {
        return new SampleBuilder(uid);
    }

    protected ExperimentBuilder anExperiment()
    {
        return new ExperimentBuilder(uid);
    }

    protected VocabularyBuilder aVocabulary()
    {
        return new VocabularyBuilder(uid);
    }

    protected PropertyTypeBuilder aBooleanPropertyType()
    {
        return new PropertyTypeBuilder(uid, PropertyTypeDataType.BOOLEAN);
    }

    protected PropertyTypeBuilder anIntegerPropertyType()
    {
        return new PropertyTypeBuilder(uid, PropertyTypeDataType.INTEGER);
    }

    protected PropertyTypeBuilder aRealPropertyType()
    {
        return new PropertyTypeBuilder(uid, PropertyTypeDataType.REAL);
    }

    protected PropertyTypeBuilder aVarcharPropertyType()
    {
        return new PropertyTypeBuilder(uid, PropertyTypeDataType.VARCHAR);
    }

    protected PropertyTypeBuilder aVocabularyPropertyType(Vocabulary vocabulary)
    {
        return new PropertyTypeBuilder(uid, vocabulary);
    }

    protected SamplePropertyTypeAssignmentBuilder aSamplePropertyTypeAssignment()
    {
        return new SamplePropertyTypeAssignmentBuilder(uid);
    }

    protected DataSetPropertyTypeAssignmentBuilder aDataSetPropertyTypeAssignment()
    {
        return new DataSetPropertyTypeAssignmentBuilder(uid);
    }

    protected DataSetTypeBuilder aDataSetType()
    {
        return new DataSetTypeBuilder(uid);
    }

    protected DataSetBuilder aDataSet()
    {
        return new DataSetBuilder(uid);
    }

    protected MaterialTypeBuilder aMaterialType()
    {
        return new MaterialTypeBuilder(uid);
    }

    protected MaterialBuilder aMaterial()
    {
        return new MaterialBuilder(uid);
    }

    protected ScriptBuilder anEntityValidationScript()
    {
        return new ScriptBuilder(uid, ScriptType.ENTITY_VALIDATOR);
    }

    protected ScriptBuilder aDynamicPropertyEvaluatorScript()
    {
        return new ScriptBuilder(uid, ScriptType.DYNAMIC_PROPERTY_EVALUATOR);
    }

    protected ScriptBuilder aManagedPropertyHandlerScript()
    {
        return new ScriptBuilder(uid, ScriptType.MANAGED_PROPERTY_HANDLER);
    }

    protected SampleTypeUpdateBuilder anUpdateOf(SampleType type)
    {
        return new SampleTypeUpdateBuilder(type);
    }

    protected SampleUpdateBuilder anUpdateOf(Sample sample)
    {
        return new SampleUpdateBuilder(sample);
    }

    protected void perform(UpdateBuilder<?> builder)
    {
        builder.update(openbis, ui);
    }

    protected Void tagWith(MetaProject metaProject, Entity... entities) throws Exception
    {
        openbis.execute(new AddEntitiesToMetaProjectRmi(metaProject, Arrays.asList(entities)));
        return null;
    }

    public Void gui()
    {
        ui = Ui.WEB;
        return null;
    }

    public Void publicApi()
    {
        ui = Ui.PUBLIC_API;
        return null;
    }

    public Void dummyApplication()
    {
        ui = Ui.DUMMY;
        return null;
    }

    protected void useGui()
    {
        startWebDriver();
        ui = Ui.WEB;
        defaultUi = ui;
    }

    protected void usePublicApi()
    {
        ui = Ui.PUBLIC_API;
        defaultUi = ui;
    }

    public static void mouseOver(WebElement element)
    {
        pages.screenshot();
        Actions builder = new Actions(SeleniumTest.driver);
        builder.moveToElement(element).build().perform();
    }

    public static <U extends Widget> U initializeWidget(Class<U> widgetClass, WebElement context)
    {
        return pages.initializeWidget(widgetClass, context, false);
    }

    public List<DataSet> listDataSetsOfSamples(Sample first, Sample... rest)
    {
        return openbis.execute(new ListDataSetsOfSamplesRmi(first, rest));
    }

    public List<DataSet> listDataSetsOfSamplesWithConnections(Sample first, Sample... rest)
    {
        return openbis.execute(new ListDataSetsOfSamplesWithConnectionsRmi(first, rest));
    }

    public List<DataSet> listDataSetsOfExperiments(Experiment first, Experiment... rest)
    {
        return openbis.execute(new ListDataSetsOfExperimentsRmi(first, rest));
    }

    public List<DataSet> listDataSetsOfSample(Sample sample)
    {
        return openbis.execute(new ListDataSetsOfSampleRmi(sample));
    }

    public List<DataSet> listDataSetsOfSamplesOnBehalfOfUser(User user, Sample first,
            Sample... rest)
    {
        return openbis.execute(new ListDataSetsOfSamplesOnBehalfOfUserRmi(user, first, rest));
    }

    public List<DataSet> listDataSetsOfExperimentsOnBehalfOfUser(User user, Experiment first,
            Experiment... rest)
    {
        return openbis.execute(new ListDataSetsOfExperimentsOnBehalfOfUserRmi(user, first, rest));
    }

    public List<DataSet> getDataSetMetaData(String firstCode, String... rest)
    {
        return openbis.execute(new GetDataSetMetaDataRmi(firstCode, rest));
    }

    public List<Experiment> listExperiments(String experimentId, String... rest)
    {
        return openbis.execute(new ListExperimentsRmi(experimentId, rest));
    }

    public List<Experiment> listExperimentsOfProjects(Project first, Project... rest)
    {
        return openbis.execute(new ListExperimentsOfProjectsRmi(first, rest));
    }

    public List<Experiment> listExperimentsHavingSamplesOfProjects(Project first, Project... rest)
    {
        return openbis.execute(new ListExperimentsHavingSamplesOfProjectsRmi(first, rest));
    }

    public List<Experiment> listExperimentsHavingDataSetsOfProjects(Project first, Project... rest)
    {
        return openbis.execute(new ListExperimentsHavingDataSetsOfProjectsRmi(first, rest));
    }

    public List<Sample> listSamplesOfExperiment(Experiment experiment)
    {
        return openbis.execute(new ListSamplesOfExperimentRmi(experiment));
    }

    public List<Sample> listSamplesOfExperimentOnBehalfOf(Experiment experiment, User user)
    {
        return openbis.execute(new ListSamplesOfExperimentOnBehalfOfUserRmi(experiment, user));
    }

    public List<Material> listMaterials(MaterialIdentifier first, MaterialIdentifier... rest)
    {
        return openbis.execute(new ListMaterialsRmi(first, rest));
    }

    public <T extends Command<U>, U> U execute(T command)
    {
        return openbis.execute(command);
    }

    public DataSetSearchCommandBuilder dataSets()
    {
        return new DataSetSearchCommandBuilder();
    }

    public MaterialSearchCommandBuilder materials()
    {
        return new MaterialSearchCommandBuilder();
    }

    public SampleSearchCommandBuilder samples()
    {
        return new SampleSearchCommandBuilder();
    }

    public <T extends Entity> List<T> searchFor(SearchCommandBuilder<T> builder)
    {
        return openbis.execute(builder.build());
    }

    public Collection<MetaProject> metaProjectsOf(Entity entity)
    {
        return entity.getMetaProjects();
    }

    public QueryTableModel reportInExternal(String dataSetCode, String... rest)
    {
        return openbis.execute(new ReportFromDataSetsRmi("EXTERNAL", dataSetCode, rest));
    }

    public QueryTableModel aggregationReportInInternal(Map<String, Object> params)
    {
        return openbis.execute(new AggregationReportRmi("Internal", params));
    }

    public QueryTableModel reportInInternal(String dataSetCode, String... rest)
    {
        return openbis.execute(new ReportFromDataSetsRmi("INTERNAL", dataSetCode, rest));
    }

    public void batchRegister(List<Sample> samples)
    {
        openbis.execute(new RegisterSampleBatchGui(samples));
    }

    public Void generalBatchImport(GeneralBatchImportFile file, final String expectedErrorMsg)
    {
        openbis.execute(new GeneralBatchImportGui(file, expectedErrorMsg));
        return null;
    }

    protected static String randomValue()
    {
        return UUID.randomUUID().toString();
    }

    public static void waitForVisibilityOf(String id) {
        WebElement webElement = SeleniumTest.driver.findElement(By.id(id));
        new FluentWait<WebElement>(webElement)
                .withTimeout(Duration.of(30, ChronoUnit.SECONDS))
                .pollingEvery(Duration.of(100, ChronoUnit.MILLIS))
                .until(
                        new Function<WebElement, Boolean>()
                        {

                            @Override
                            public Boolean apply(WebElement webElement)
                            {
                                return webElement.isDisplayed();
                            }
                        });
    }
}
