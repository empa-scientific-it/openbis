/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetMetadataDTO;
import ch.systemsx.cisd.openbis.generic.client.cli.Login;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.filter.AndDataSetFilter;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.filter.PropertiesBasedDataSetFilter;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.filter.TypeBasedDataSetFilter;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade.IImageOutputStreamProvider;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureCodesProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * Simple Matlab interface for openBIS for Screening. It is meant to be used in one Matlab session at a time, i.e. it is <i>not</i> multi-threading
 * safe.
 * <p>
 * While written in Java, the API is idiomatic for Matlab, i.e. values are returned as multi-dimensional arrays. For the <code>get...</code> and
 * <code>load...</code> methods the first index will contain the actual data, while the second index will contain per-row annotations. For
 * <code>getFeatureMatrix</code>, the third index contains per-column annotations. This allows simple access with Matlab's slicing operator, see doc
 * of e.g. {@link #getFeatureMatrix(String, String, String[])}.
 * <p>
 * A typical Matlab session looks like:
 * 
 * <pre>
 * % Add the API jar file to the classpath
 * javaaddpath('/home/brinn/matlab/openbis_screening_api-batteries_included.jar')
 * % Login to server
 * OpenBISScreeningML.login('user', 'secret', 'https://www.infectome.org')
 * 
 * % ...perform calls on the server...
 * 
 * % Logout to close the session on the server
 * OpenBISScreeningML.logout()
 * </pre>
 * 
 * <i>Note: using this login your password will end up in the Matlab command history. An alternative that avoids this is to call the
 * {@link ch.systemsx.cisd.openbis.generic.client.cli.Login} class. Logging in on the console will grant this class access to the openBIS server.</i>
 * <p>
 * To learn the API one needs to understand three basic notions: code, augmented code and perm id. Space, project, experiment, plate and well have
 * their own <b>code</b>, which is unique only in the context of the parent.<br>
 * That's why one needs <b>augmented code</b> to point e.g. to one experiment, because two different projects can have experiments with the same code.
 * <br>
 * Such an augmented code for experiment has a form of "/space-code/project-code/experiment-code".<br>
 * For plate it has a form of "/space-code/plate-code" (note that plate code is unique on the space level). <br>
 * The drawback of an augmented code is that it's not persistent. If someone e.g. moves the experiment from one space to the other augmented code of
 * the experiment becomes invalid. That is why experiments, plates and datasets have <b>perm id</b> (permament identifier) which never change and
 * allow to refer to them with one "magic" identifier, e.g. 20110516124520378-737166.
 * </p>
 * 
 * @author Bernd Rinn
 */
public class OpenBISScreeningML
{
    private static interface ITileNumberIterable extends Iterable<Integer>
    {
        public void setMaximumNumberOfTiles(int numberOfTiles);

        public int getMaximumNumberOfTiles();
    }

    private static final class ImageReferenceAndFile
    {
        private final PlateImageReference imageReference;

        private final File imageFile;

        private BufferedOutputStream outputStream;

        ImageReferenceAndFile(PlateImageReference imageReference, File imageFile)
        {
            this.imageReference = imageReference;
            this.imageFile = imageFile;
        }

        public PlateImageReference getImageReference()
        {
            return imageReference;
        }

        public File getImageFile()
        {
            return imageFile;
        }

        public OutputStream open() throws IOException
        {
            if (outputStream == null)
            {
                outputStream = new BufferedOutputStream(new FileOutputStream(imageFile));
            }
            return outputStream;
        }

        public void close() throws IOException
        {
            if (outputStream != null)
            {
                outputStream.close();
            }
            outputStream = null;
        }
    }

    private static final class IndexSet<T extends Comparable<T>>
    {
        private final Map<T, Integer> indexMap = new HashMap<T, Integer>();

        private final Set<T> set = new TreeSet<T>();

        public void add(T item)
        {
            set.add(item);
        }

        public int getIndex(T item)
        {
            if (set.size() != indexMap.size())
            {
                List<T> items = new ArrayList<T>(set);
                for (int i = 0; i < items.size(); i++)
                {
                    indexMap.put(items.get(i), i);
                }
            }
            Integer index = indexMap.get(item);
            if (index == null)
            {
                throw new IllegalArgumentException("Unknown item: " + item);
            }
            return index;
        }

        public int size()
        {
            return set.size();
        }
    }

    private static File temporarySessionDir;

    private static Map<PlateImageReference, File> loadedImages;

    static IScreeningOpenbisServiceFacadeFactory facadeFactory =
            ScreeningOpenbisServiceFacadeFactory.INSTANCE;

    private static IScreeningOpenbisServiceFacade openbis = null;

    static IOpenbisServiceFacadeFactory genericFacadeFactory = null;

    private static IOpenbisServiceFacade genericOpenbis = null;

    private static List<ExperimentIdentifier> experiments = null;

    private static List<Plate> plates = null;

    private static Map<String, List<Plate>> experimentToPlateMap =
            new HashMap<String, List<Plate>>();

    private static Map<String, ExperimentIdentifier> experimentCodeToExperimentMap =
            new HashMap<String, ExperimentIdentifier>();

    private static Map<String, Plate> plateCodeToPlateMap = new HashMap<String, Plate>();

    private OpenBISScreeningML()
    {
        // Not to be constructed.
    }

    //
    // Versioning
    //

    /**
     * The version of the API.
     */
    public static final String VERSION = "1";

    /**
     * The required version ("major.minor") of the screening API on the openBIS application server.
     */
    public static final String REQUIRES_OPENBIS_AS_API = "1.7";

    /**
     * The required version ("major.minor") of the screening API on the openBIS datastore server.
     */
    public static final String REQUIRES_OPENBIS_DSS_API = "1.1";

    /**
     * Root temporary directory for data sets and images. By default <code>java.io.tmpdir</code> is used.
     */
    static File tempDir = new File(System.getProperty("java.io.tmpdir"));

    static final String TEMP_DIR_PREFIX = "openbis_";

    static final String TEMP_DIR_POSTFIX = "_temp_dir";

    //
    // Authentication methods
    //

    /**
     * Login to the openBIS server given as <var>url</var>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * OpenBISScreeningML.login('user', 'secret', 'https://www.infectome.org')
     * </pre>
     * 
     * @param user The user id on the server
     * @param password The password on the server
     * @param url The URL, e.g. <code>https://www.infectome.org</code>
     */
    public static void login(String user, String password, String url)
    {
        System.setProperty("force-accept-ssl-certificate", "true");
        IScreeningOpenbisServiceFacade facade = facadeFactory.tryToCreate(user, password, url);
        if (facade == null)
        {
            throw new RuntimeException("Login failed.");
        }

        if (null == genericFacadeFactory)
        {
            genericFacadeFactory = OpenbisServiceFacadeFactory.INSTANCE;
        }
        IOpenbisServiceFacade genericFacade =
                genericFacadeFactory.tryToCreate(facade.getSessionToken(), url, 0);
        init(facade, genericFacade);
    }

    private static void init(IScreeningOpenbisServiceFacade openBisFacade,
            IOpenbisServiceFacade genericFacade)
    {
        openbis = openBisFacade;
        genericOpenbis = genericFacade;
        temporarySessionDir =
                new File(tempDir, TEMP_DIR_PREFIX + System.getProperty("user.name") + "_"
                        + System.currentTimeMillis() / 1000 + TEMP_DIR_POSTFIX);
        if (temporarySessionDir.mkdirs() == false)
        {
            throw new RuntimeException("Couldn't create a temporary directory.");
        }
        temporarySessionDir.deleteOnExit();
        loadedImages = new HashMap<PlateImageReference, File>();
        experiments = openbis.listExperiments();
        experimentCodeToExperimentMap.clear();
        for (ExperimentIdentifier e : experiments)
        {
            experimentCodeToExperimentMap.put(e.getAugmentedCode(), e);
        }
        plates = openbis.listPlates();
        plateCodeToPlateMap.clear();
        experimentToPlateMap.clear();
        for (Plate p : plates)
        {
            final String plateCode = p.getAugmentedCode();
            plateCodeToPlateMap.put(plateCode, p);
            final String experimentCode = p.getExperimentIdentifier().getAugmentedCode();
            List<Plate> experimentPlates = experimentToPlateMap.get(experimentCode);
            if (experimentPlates == null)
            {
                experimentPlates = new ArrayList<Plate>();
                experimentToPlateMap.put(experimentCode, experimentPlates);
            }
            experimentPlates.add(p);
        }
    }

    /**
     * Logs out and closes the session on the server.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * OpenBISScreeningML.logout()
     * </pre>
     */
    public static void logout()
    {
        if (openbis == null)
        {
            return;
        }
        openbis.logout();
        if (Login.OPENBIS_TOKEN_FILE.exists())
        {
            Login.OPENBIS_TOKEN_FILE.delete();
        }
        delete(temporarySessionDir);
        openbis = null;
    }

    private static void delete(File file)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            for (File child : files)
            {
                delete(child);
            }
        }
        file.delete();
    }

    //
    // Information methods
    //

    /**
     * Lists all experiment.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the experiments
     * exps = OpenBISScreeningML.listExperiments();
     * % How many experiments do we have?
     * length(exps)
     * % Get all information about experiment 3
     * exp3 = exps(3,:)
     * % Get the perm ids for all experiments
     * permids = exps(:,2)
     * </pre>
     * 
     * @return Each row contains information about one experiment:
     *         <p>
     *         <code>{ experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code }</code>
     */
    public static Object[][] listExperiments()
    {
        checkLoggedIn();
        final Object[][] result = new Object[experiments.size()][5];
        for (int i = 0; i < experiments.size(); ++i)
        {
            final Object[] annotations =
                    new Object[]
                    { experiments.get(i).getAugmentedCode(), experiments.get(i).getPermId(),
                            experiments.get(i).getSpaceCode(),
                            experiments.get(i).getProjectCode(),
                            experiments.get(i).getExperimentCode() };
            System.arraycopy(annotations, 0, result[i], 0, annotations.length);
        }
        return result;
    }

    /**
     * Lists all plates.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the plates
     * plates = OpenBISScreeningML.listPlates();
     * % How many plates do we have?
     * length(plates)
     * % Get all information about plate 2
     * plate2 = plates(2,:)
     * % Get the simple plate codes for all plates
     * codes = plates(:,4)
     * </pre>
     * 
     * @return Each row contains information about one plate:
     *         <p>
     *         <code>{ plate augmented code, plate perm id, plate space code, plate code, 
     *         experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code }</code>
     */
    public static Object[][] listPlates()
    {
        checkLoggedIn();
        return listPlates(plates);
    }

    /**
     * Lists the plates of <var>experiment</var>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the plates of experiment MYEXP in project PROJ of space SPACE
     * plates = OpenBISScreeningML.listPlates('/SPACE/PROJ/MYEXP');
     * % How many plates do we have?
     * length(plates)
     * % Get all information about plate 2
     * plate2 = plates(2,:)
     * % Get the augmented plate codes for all plates
     * acodes = plates(:,1)
     * </pre>
     * 
     * @param experiment The augmented code of the experiment to list the plates for
     * @return Each row contains information about one plate:
     *         <p>
     *         <code>{ plate augmented code, plate perm id, plate space code, plate code, 
     *         experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code }</code>
     */
    public static Object[][] listPlates(String experiment)
    {
        checkLoggedIn();
        final List<Plate> experimentPlates = experimentToPlateMap.get(experiment);
        if (experimentPlates == null)
        {
            throw new RuntimeException("No experiment with that code found.");
        }
        return listPlates(experimentPlates);
    }

    /**
     * Lists the plates of <var>experiment</var> and analysis procedure. Each returned plate has at least one data set with the specified analysis
     * procedure.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get all plates having data sets with analysis procedure 'PROC' in the experiment MYEXP in project PROJ of space SPACE
     * plates = OpenBISScreeningML.listPlates('/SPACE/PROJ/MYEXP', 'PROC');
     * % Get all information about plate 2
     * plate2 = plates(2,:)
     * % Get the augmented plate codes for all plates
     * acodes = plates(:,1)
     * </pre>
     * 
     * @param experiment The augmented code of the experiment to list the plates for
     * @param analysisProcedure The analysis procedure
     * @return Each row contains information about one plate:
     *         <p>
     *         <code>{ plate augmented code, plate perm id, plate space code, plate code, 
     *         experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code }</code>
     */
    public static Object[][] listPlates(String experiment, String analysisProcedure)
    {
        checkLoggedIn();
        ExperimentIdentifier experimentIdentifier = getExperimentIdentifierOrFail(experiment);
        List<Plate> resultPlates = openbis.listPlates(experimentIdentifier, analysisProcedure);
        return listPlates(resultPlates);
    }

    private static Object[][] listPlates(final List<Plate> list)
    {
        final Object[][] result = new Object[list.size()][9];
        for (int i = 0; i < list.size(); ++i)
        {
            final Object[] annotations =
                    new Object[]
                    { list.get(i).getAugmentedCode(), list.get(i).getPermId(),
                            list.get(i).tryGetSpaceCode(), list.get(i).getPlateCode(),
                            list.get(i).getExperimentIdentifier().getAugmentedCode(),
                            list.get(i).getExperimentIdentifier().getPermId(),
                            list.get(i).getExperimentIdentifier().getSpaceCode(),
                            list.get(i).getExperimentIdentifier().getProjectCode(),
                            list.get(i).getExperimentIdentifier().getExperimentCode(), };
            System.arraycopy(annotations, 0, result[i], 0, annotations.length);
        }
        return result;
    }

    /**
     * Returns an alphabetically sorted list of analysis procedure codes of all data sets of a specified experiment.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the analysis procedures for experiment MYEXP in project PROJ of space SPACE
     * analysisProcedures = OpenBISScreeningML.listAnalysisProcedures('/SPACE/PROJ/MYEXP');
     * % How many analysis procedures do we have?
     * length(analysisProcedures)
     * % Get all the analysis procedure codes 
     * acodes = analysisProcedures(:,1)
     * </pre>
     * 
     * @param experiment The augmented code of the experiment to list analysis procedures for
     * @return Each row contains information about one analysis procedure:
     *         <p>
     *         <code>{ analysis procedure code }</code>
     */
    public static Object[][] listAnalysisProcedures(String experiment)
    {
        checkLoggedIn();
        ExperimentIdentifier experimentIdentifier = getExperimentIdentifierOrFail(experiment);
        List<String> proceduresList = openbis.listAnalysisProcedures(experimentIdentifier);
        return listAnalysisProcedures(proceduresList);
    }

    private static Object[][] listAnalysisProcedures(final List<String> list)
    {
        final Object[][] result = new Object[list.size()][1];
        for (int i = 0; i < list.size(); ++i)
        {
            result[i][0] = list.get(i);
        }
        return result;
    }

    /**
     * List all material of a given type.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % List all SIRNA material
     * sirna = OpenBISScreeningML.listMaterials('SIRNA')
     * % All material codes
     * sirna(:,1,1)
     * % All material ids
     * sirna(:,2,1)
     * % Properties of first material
     * metadata(1,3,:)
     * </pre>
     * 
     * @param materialType Material type as string.
     * @return For each data set: <code>{{material code}, {material id}, { {key1, value1}, {key2, value2} ... } }</code>
     */

    public static Object[][][] listMaterials(String materialType)
    {
        checkLoggedIn();
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, materialType));

        List<Material> materialList = openbis.searchForMaterials(searchCriteria);

        if (materialList.size() < 1)
        {
            return new Object[0][0][0];
        }

        Object[][][] result = new Object[materialList.size()][][];

        for (int materialIdx = 0; materialIdx < materialList.size(); materialIdx++)
        {
            Material material = materialList.get(materialIdx);
            result[materialIdx] = new Object[3][];
            result[materialIdx][0] = new Object[]
            { material.getMaterialCode() };
            result[materialIdx][1] = new Object[]
            { material.getId() };
            result[materialIdx][2] = listProperties(material.getProperties());
        }
        return result;
    }

    /**
     * Returns the properties of specified plate.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get properties for plate P005 in space SPACE
     * properties = OpenBISScreeningML.getPlateProperties('/SPACE/P005')
     * % Get property type code of first property
     * properties(1,1)
     * % Get property value of first property
     * properties(1,2)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code
     * @return A two dimensional array where the first column contains the property codes and the second column the corresponding property values.
     */
    public static Object[][] getPlateProperties(String augmentedPlateCode)
    {
        checkLoggedIn();
        String[] plateArray = { augmentedPlateCode };
        List<PlateMetadata> metadataList = openbis.getPlateMetadataList(toPlates(plateArray));
        if (metadataList.size() < 1)
        {
            throw new RuntimeException("No metadata for that code found.");
        }
        return listProperties(metadataList.get(0).getProperties());
    }

    /**
     * Updates properties of specified plate.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Updates properties DESCRIPTION and NUMBER for plate P005 in space SPACE
     * properties = {'DESCRIPTION' 'hello example'; 'NUMBER' 3.14}
     * OpenBISScreeningML.updatePlateProperties('/SPACE/P005', properties)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code
     * @param properties A two dimensional array where the first column contains the property codes and the second column the corresponding property
     *            values.
     */
    public static void updatePlateProperties(String augmentedPlateCode, Object[][] properties)
    {
        checkLoggedIn();
        PlateIdentifier plateIdentifier = PlateIdentifier.createFromAugmentedCode(augmentedPlateCode);
        openbis.updatePlateProperties(plateIdentifier, createMap(properties));
    }

    /**
     * Returns the properties of specified well for specified plate.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get properties for well A03 of plate P005 in space SPACE
     * properties = OpenBISScreeningML.getWellProperties('/SPACE/P005', 1, 3)
     * % Get property type code of first property
     * properties(1,1)
     * % Get property value of first property
     * properties(1,2)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code
     * @param row The row in the plate to get the well properties for
     * @param column The column in the plate to get the well properties for
     * @return A two dimensional array where the first column contains the property codes and the second column the corresponding property values.
     */
    public static Object[][] getWellProperties(String augmentedPlateCode, int row, int column)
    {
        checkLoggedIn();
        WellPosition wellPosition = new WellPosition(row, column);
        WellIdentifier wellIdentifier = getWell(augmentedPlateCode, wellPosition);
        Map<String, String> wellProperties = openbis.getWellProperties(wellIdentifier);
        return listProperties(wellProperties);
    }

    /**
     * Updates properties of specified well for specified plate.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Updates properties DESCRIPTION and NUMBER for well A03 of plate P005 in space SPACE
     * properties = {'DESCRIPTION' 'hello example'; 'NUMBER' 3.14}
     * OpenBISScreeningML.updateWellProperties('/SPACE/P005', 1, 3, properties)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code
     * @param row The row in the plate to get the well properties for
     * @param column The column in the plate to get the well properties for
     * @param properties A two dimensional array where the first column contains the property codes and the second column the corresponding property
     *            values.
     */
    public static void updateWellProperties(String augmentedPlateCode, int row, int column,
            Object[][] properties)
    {
        checkLoggedIn();
        WellPosition wellPosition = new WellPosition(row, column);
        WellIdentifier wellIdentifier = getWell(augmentedPlateCode, wellPosition);
        openbis.updateWellProperties(wellIdentifier, createMap(properties));
    }

    private static WellIdentifier getWell(String augmentedPlateCode, WellPosition wellPosition)
    {
        Plate plate = getPlate(augmentedPlateCode);
        List<WellIdentifier> wells = openbis.listPlateWells(plate);
        for (WellIdentifier wellIdentifier : wells)
        {
            if (wellIdentifier.getWellPosition().equals(wellPosition))
            {
                return wellIdentifier;
            }
        }
        throw new RuntimeException("Plate '" + augmentedPlateCode + "' has no well at "
                + wellPosition + ".");
    }

    /**
     * Fetches metadata of the image datasets for the specified <var>plate</var>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the metadata of image datasets of plate P005 from space SPACE
     * imagesMetadata = OpenBISScreeningML.getImagesMetadata('/SPACE/P005');
     * % How many image datasets do we have? Usually there will be just one.
     * length(imagesMetadata)
     * % What is the number of tiles in the first image dataset?
     * imagesMetadata(1, 3)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code.
     * @return <code>{ images width, images height, number of tiles in the well, 
     *                 number of tiles rows, number of tiles columns, number of plate rows, number of plate columns }</code> .
     */
    public static Object[][] getImagesMetadata(String augmentedPlateCode)
    {
        checkLoggedIn();
        final Plate plateId = getPlate(augmentedPlateCode);
        final List<ImageDatasetReference> imageDatasets = listRawImageDatasets(plateId);
        final List<ImageDatasetMetadata> metaList = openbis.listImageMetadata(imageDatasets);

        Object[][] result = new Object[metaList.size()][];
        for (int i = 0; i < metaList.size(); ++i)
        {
            ImageDatasetMetadata meta = metaList.get(i);
            ImageDatasetReference imageDatasetReference = imageDatasets.get(i);
            Geometry plateGeometry = imageDatasetReference.getPlateGeometry();
            result[i] =
                    new Object[]
                    { meta.getWidth(), meta.getHeight(), meta.getNumberOfTiles(),
                            meta.getTilesRows(), meta.getTilesCols(),
                            plateGeometry.getNumberOfRows(), plateGeometry.getNumberOfColumns() };
        }
        return result;
    }

    /**
     * Lists all channels measured in <var>experiment</var>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the channels of experiment MYEXP in project PROJ of space SPACE
     * channels = OpenBISScreeningML.listChannels('/SPACE/PROJ/MYEXP');
     * % How many channels do we have?
     * length(channels)
     * % What is the name of channel 1?
     * channels(1)
     * </pre>
     * 
     * @param experiment The augmented code of the experiment to list the channels for
     * @return Each row contains information about one channel. Currently the only information available is the channel name.
     */
    public static Object[][] listChannels(String experiment)
    {
        checkLoggedIn();
        final List<Plate> experimentPlates = experimentToPlateMap.get(experiment);
        if (experimentPlates == null)
        {
            throw new RuntimeException("No experiment with that code found.");
        }
        if (experimentPlates.isEmpty())
        {
            return new Object[0][];
        }
        final List<ImageDatasetReference> imageDatasets =
                openbis.listRawImageDatasets(experimentPlates);
        if (imageDatasets.isEmpty())
        {
            return new Object[0][];
        }
        return extractChannels(imageDatasets.get(0));
    }

    private static List<String> getChannelCodes(final List<ImageDatasetMetadata> meta)
    {
        return meta.get(0).getChannelCodes();
    }

    /**
     * Lists all features computed for <var>experiment</var> using specified analysis procedure.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the features of experiment MYEXP in project PROJ of space SPACE
     * features = OpenBISScreeningML.listFeatures('/SPACE/PROJ/MYEXP', []);
     * % Get the features of experiment MYEXP in project PROJ of space SPACE which are computed
     * % with analysis procedure AP-4711
     * features = OpenBISScreeningML.listFeatures('/SPACE/PROJ/MYEXP', 'AP-4711');
     * % How many features do we have?
     * length(features)
     * % What is the name of features 1?
     * features(1)
     * </pre>
     * 
     * @param experiment The augmented code of the experiment to list the features for
     * @param analysisProcedureOrNull The analysis procedure used to filter the result. That is, the result is restricted to feature vector data sets
     *            with a value of property <code>ANALYSIS_PROCEDURE</code> as specified. If <code>null</code> (or <code>[]</code> in MatLab) no
     *            restriction applies.
     * @return Each row contains information about one feature. Currently the only information available is the feature name.
     */
    public static Object[][] listFeatures(String experiment, String analysisProcedureOrNull)
    {
        checkLoggedIn();
        final List<Plate> experimentPlates = experimentToPlateMap.get(experiment);
        if (experimentPlates == null)
        {
            throw new RuntimeException("No experiment with that code found.");
        }
        if (experimentPlates.isEmpty())
        {
            return new Object[0][];
        }
        final List<FeatureVectorDatasetReference> featureDatasets =
                openbis.listFeatureVectorDatasets(experimentPlates, analysisProcedureOrNull);
        if (featureDatasets.isEmpty())
        {
            return new Object[0][];
        }
        List<String> featureCodes = openbis.listAvailableFeatureCodes(featureDatasets);
        Collections.sort(featureCodes);
        Object[][] result = new Object[featureCodes.size()][1];
        for (int i = 0; i < result.length; ++i)
        {
            result[i][0] = featureCodes.get(i);
        }
        return result;
    }

    /**
     * Loads data sets for specified plate code. For each data set the path to the root of the data set is returned. If it is possible the path points
     * directly into the data set store. No data is copied. Otherwise the data is retrieved from the data store server.<br>
     * If the same dataset is loaded for the second time in one session it will be immediately returned from the local cache.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Load all data sets of plate P005 in space SPACE
     * dsinfo = OpenBISScreeningML.loadDataSets('/SPACE/P005', 'HCS_ANALYSIS_CELL_FEATURES_CC_MAT', '/mount/openbis-store')
     * % Get the data set codes
     * dsinfo(:,1)
     * % Get root path of first data set (assuming there is at least one)
     * dsginfo(1,2)
     * % Get the properties for the first data set
     * props = dsginfo(1,3)
     * % Get property key of first property
     * props(1,1)
     * % Get property value of first property
     * props(1,2)
     * % Get all parents of first data set (assuming there is at least one)
     * dsInfo(1,4)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code.
     * @param dataSetTypeCodePattern only datasets of the type which matches the specified pattern will be returned. To fetch all datasets specify
     *            ".*".
     * @param overrideStoreRootPathOrNull A path, in the context of the local file system mounts, to the DSS' store root. If null, paths are returned
     *            in the context of the DSS' file system mounts.
     * @return Each row contains information about one data set:
     *         <p>
     *         <code>{ data set code, data set root path, { {key1, value1}, {key2, value2} ...}, parents }</code>
     */
    public static Object[][] loadDataSets(String augmentedPlateCode, String dataSetTypeCodePattern,
            String overrideStoreRootPathOrNull)
    {
        return loadDataSets(augmentedPlateCode, dataSetTypeCodePattern, new Object[0][],
                overrideStoreRootPathOrNull);
    }

    /**
     * Loads data sets for specified plate code. For each data set the path to the root of the data set is returned. If it is possible the path points
     * directly into the data set store. No data is copied. Otherwise the data is retrieved from the data store server.<br>
     * If the same dataset is loaded for the second time in one session it will be immediately returned from the local cache.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Load all data sets of plate P005 in space SPACE
     * properties = {'ANALYSIS_PROCEDURE' 'AX87'}
     * dsinfo = OpenBISScreeningML.loadDataSets('/SPACE/P005', 'HCS_ANALYSIS_CELL_FEATURES_CC_MAT', properties, '/mount/openbis-store')
     * % Get the data set codes
     * dsinfo(:,1)
     * % Get root path of first data set (assuming there is at least one)
     * dsinfo(1,2)
     * % Get the properties for the first data set
     * props = dsinfo(1,3)
     * % Get property key of first property
     * props(1,1)
     * % Get property value of first property
     * props(1,2)
     * % Get all parents of first data set (assuming there is at least one)
     * dsInfo(1,4)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code.
     * @param dataSetTypeCodePattern only data sets of the type which matches the specified pattern will be returned. To fetch all data sets specify
     *            ".*".
     * @param properties Only data set with specified property values will be returned. This is a two dimensional array where the first column
     *            contains the property codes and the second column the corresponding property values.
     * @return Each row contains information about one data set:
     * @param overrideStoreRootPathOrNull A path, in the context of the local file system mounts, to the DSS' store root. If null, paths are returned
     *            in the context of the DSS' file system mounts.
     *            <p>
     *            <code>{ data set code, data set root path, { {key1, value1}, {key2, value2} ...}, parents }</code>
     */
    public static Object[][] loadDataSets(String augmentedPlateCode,
            final String dataSetTypeCodePattern, final Object[][] properties,
            String overrideStoreRootPathOrNull)
    {
        checkLoggedIn();
        Plate plateIdentifier = getPlate(augmentedPlateCode);
        List<DataSet> dataSets =
                openbis.getFullDataSets(plateIdentifier, new AndDataSetFilter(
                        new TypeBasedDataSetFilter(dataSetTypeCodePattern),
                        new PropertiesBasedDataSetFilter(createMap(properties))));
        try
        {
            return translateDataSets(overrideStoreRootPathOrNull, dataSets);
        } catch (Exception ex)
        {
            throw createException("Loading data sets for plate '" + augmentedPlateCode
                    + "' failed.", ex);
        }
    }

    /**
     * Loads data sets for specified experiment code. For each data set the path to the root of the data set is returned. If it is possible the path
     * points directly into the data set store. No data is copied. Otherwise the data is retrieved from the data store server.<br>
     * If the same dataset is loaded for the second time in one session it will be immediately returned from the local cache.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Load all data sets of experiment E005 in space SPACE and project PROJECT
     * properties = {'ANALYSIS_PROCEDURE' 'AX87'}
     * dsinfo = OpenBISScreeningML.loadDataSetsForExperiment('/SPACE/PROJECT/E005', 'HCS_ANALYSIS_CELL_FEATURES_CC_MAT', properties, '/mount/openbis-store')
     * % Get the data set codes
     * dsinfo(:,1)
     * % Get root path of first data set (assuming there is at least one)
     * dsinfo(1,2)
     * % Get the properties for the first data set
     * props = dsinfo(1,3)
     * % Get property key of first property
     * props(1,1)
     * % Get property value of first property
     * props(1,2)
     * % Get all parents of first data set (assuming there is at least one)
     * dsInfo(1,4)
     * </pre>
     * 
     * @param augmentedExperimentCode The augmented experiment code.
     * @param dataSetTypeCodePattern only data sets of the type which matches the specified pattern will be returned. To fetch all data sets specify
     *            ".*".
     * @param properties Only data set with specified property values will be returned. This is a two dimensional array where the first column
     *            contains the property codes and the second column the corresponding property values.
     * @return Each row contains information about one data set:
     * @param overrideStoreRootPathOrNull A path, in the context of the local file system mounts, to the DSS' store root. If null, paths are returned
     *            in the context of the DSS' file system mounts.
     *            <p>
     *            <code>{ data set code, data set root path, { {key1, value1}, {key2, value2} ...}, parents }</code>
     */
    public static Object[][] loadDataSetsForExperiment(String augmentedExperimentCode,
            final String dataSetTypeCodePattern, final Object[][] properties,
            String overrideStoreRootPathOrNull)
    {
        checkLoggedIn();
        ExperimentIdentifier experimentIdentifier =
                getExperimentIdentifierOrFail(augmentedExperimentCode);
        List<DataSet> dataSets =
                openbis.getFullDataSets(experimentIdentifier, new AndDataSetFilter(
                        new TypeBasedDataSetFilter(dataSetTypeCodePattern),
                        new PropertiesBasedDataSetFilter(createMap(properties))));
        try
        {
            return translateDataSets(overrideStoreRootPathOrNull, dataSets);
        } catch (Exception ex)
        {
            throw createException("Loading data sets for experiment '" + augmentedExperimentCode
                    + "' failed.", ex);
        }
    }

    /**
     * Lists all files of all data sets for specified experiment and data set type code matching specified regular expression pattern.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % List all data sets of experiment E005 in space SPACE and project PROJECT. The query is restricted to data sets
     * % of a type starting with HCS_IMAGE
     * files = OpenBISScreeningML.listDataSetFilesForExperiment('/SPACE/PROJECT/E005', 'HCS_IMAGE.*')
     * % Codes of all found data sets
     * files(:,1)
     * % Code of third data set (assuming at least three data sets found)
     * files(3,1)
     * % Files of third data set (assuming at least three data sets found)
     * files(3,2,:)
     * </pre>
     * 
     * @param augmentedExperimentCode The augmented experiment code.
     * @param dataSetTypeCodePattern only data sets of the type which matches the specified pattern will be returned. To fetch all data sets specify
     *            ".*".
     * @return <code>{data set code, file/folder paths}</code>
     */
    public static Object[][][] listDataSetFilesForExperiment(String augmentedExperimentCode,
            String dataSetTypeCodePattern)
    {
        checkLoggedIn();
        ExperimentIdentifier experimentIdentifier =
                getExperimentIdentifierOrFail(augmentedExperimentCode);

        List<IDataSetDss> dataSets =
                openbis.getDataSets(experimentIdentifier, new TypeBasedDataSetFilter(
                        dataSetTypeCodePattern));
        Object[][][] result = new Object[dataSets.size()][][];
        for (int i = 0; i < dataSets.size(); i++)
        {
            IDataSetDss dataSet = dataSets.get(i);
            FileInfoDssDTO[] fileInfos = dataSet.listFiles("/", true);
            String code = dataSet.getCode();
            result[i] = new Object[4][];
            result[i][0] = new Object[]
            { code };
            result[i][1] = new Object[fileInfos.length];
            for (int j = 0; j < fileInfos.length; j++)
            {
                FileInfoDssDTO fileInfo = fileInfos[j];
                result[i][1][j] = fileInfo.getPathInDataSet();
            }
        }
        return result;
    }

    private static Object[][] translateDataSets(String overrideStoreRootPathOrNull,
            List<DataSet> dataSets)
    {
        Object[][] result = new Object[dataSets.size()][];
        for (int i = 0; i < dataSets.size(); i++)
        {
            DataSet dataSet = dataSets.get(i);
            String code = dataSet.getCode();
            File file = new File(temporarySessionDir, code);
            if (file.exists() == false)
            {
                file = getLinkOrCopyOfContents(overrideStoreRootPathOrNull, dataSet, file);
            }
            List<String> parents = dataSet.getParentCodes();
            Object[] parentCodes = new Object[parents.size()];
            for (int j = 0; j < parentCodes.length; j++)
            {
                parentCodes[j] = parents.get(j);
            }
            Object[][] dataSetProperties = listProperties(dataSet.getProperties());
            result[i] = new Object[]
            { code, file.getPath(), dataSetProperties, parentCodes };
        }
        return result;
    }

    protected static File getLinkOrCopyOfContents(String overrideStoreRootPathOrNull, DataSet dataSet, File file)
    {
        try
        {
            return dataSet.getLinkOrCopyOfContents(overrideStoreRootPathOrNull, temporarySessionDir);
        } catch (RuntimeException ex)
        {
            // In case of error we don't want any files remaining
            if (file.exists())
            {
                try
                {
                    FileUtils.deleteDirectory(file);
                } catch (IOException ioe)
                {
                    CheckedExceptionTunnel.wrapIfNecessary(ioe);
                }
            }
            throw ex;
        }
    }

    /**
     * Loads file/folder of specified data set and specified file/folder path inside the data set. If it is possible the path points directly into the
     * data set store. No data is copied. Otherwise the data is retrieved from the data store server.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % List all data sets of plate P005 in space SPACE. The query is restricted to data sets
     * % of a type starting with HCS_IMAGE
     * files = OpenBISScreeningML.listDataSetsFiles('/SPACE/P005', 'HCS_IMAGE.*')
     * % Load from the first data set (assuming at least one data set found) the third file/folder 
     * % (assuming at least three files/folders)
     * file = OpenBISScreeningML.loadDataSetFile(files(1,1), files(1,2,3), [])
     * </pre>
     * 
     * @param dataSetCode The code of the data set.
     * @param pathInDataSet Path inside the data set pointing to the file/folder which should be down loaded. Use '/' if all files are requested.
     * @param overrideStoreRootPathOrNull A path, in the context of the local file system mounts, to the DSS' store root. If null, paths are returned
     *            in the context of the DSS' file system mounts.
     * @return path to the down loaded file/folder.
     */
    public static Object loadDataSetFile(String dataSetCode, String pathInDataSet,
            String overrideStoreRootPathOrNull)
    {
        checkLoggedIn();
        File file = new File(temporarySessionDir + "/" + dataSetCode, pathInDataSet);
        if (file.exists() && file.isFile() && file.length() > 0)
        {
            return file.toString();
        }
        IDataSetDss dataSet = openbis.getDataSet(dataSetCode);
        return dataSet.getLinkOrCopyOfContent(overrideStoreRootPathOrNull, temporarySessionDir,
                pathInDataSet).toString();
    }

    /**
     * Lists all files of all data sets for specifies plate and data set type code matching specified regular expression pattern.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % List all data sets of plate P005 in space SPACE. The query is restricted to data sets
     * % of a type starting with HCS_IMAGE
     * files = OpenBISScreeningML.listDataSetsFiles('/SPACE/P005', 'HCS_IMAGE.*')
     * % Codes of all found data sets
     * files(:,1)
     * % Code of third data set (assuming at least three data sets found)
     * files(3,1)
     * % Files of third data set (assuming at least three data sets found)
     * files(3,2,:)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code.
     * @param dataSetTypeCodePattern only data sets of the type which matches the specified pattern will be returned. To fetch all data sets specify
     *            ".*".
     * @return <code>{data set code, file/folder paths}</code>
     */
    public static Object[][][] listDataSetsFiles(String augmentedPlateCode,
            String dataSetTypeCodePattern)
    {
        checkLoggedIn();
        Plate plateIdentifier = getPlate(augmentedPlateCode);

        List<IDataSetDss> dataSets =
                openbis.getDataSets(plateIdentifier, new TypeBasedDataSetFilter(
                        dataSetTypeCodePattern));
        Object[][][] result = new Object[dataSets.size()][][];
        for (int i = 0; i < dataSets.size(); i++)
        {
            IDataSetDss dataSet = dataSets.get(i);
            FileInfoDssDTO[] fileInfos = dataSet.listFiles("/", true);
            String code = dataSet.getCode();
            result[i] = new Object[4][];
            result[i][0] = new Object[]
            { code };
            result[i][1] = new Object[fileInfos.length];
            for (int j = 0; j < fileInfos.length; j++)
            {
                FileInfoDssDTO fileInfo = fileInfos[j];
                result[i][1][j] = fileInfo.getPathInDataSet();
            }
        }
        return result;
    }

    /**
     * Lists meta data of specified data sets. This includes data set type, properties and codes of connected parent and children data sets. The
     * result is returned in the same order as the data set code array argument.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % List meta data for data sets 20101006020318852-10 and 20110919083636428-236
     * metadata = OpenBISScreeningML.getDataSetMetaData({ '20101006020318852-10' '20110919083636428-236'})
     * % Codes of all data sets
     * metadata(:,1,1)
     * % Types of all data sets
     * metadata(:,1,2)
     * % Properties of first data set
     * metadata(1, 2)
     * % Parents of second data set
     * metadata(2, 3, :)
     * % Children of first data set
     * metadata(1, 4, :)
     * </pre>
     * 
     * @param dataSetCodes Codes of data sets from whom meta data are queried.
     * @return For each data set: <code>{{data set code, data set type}, { {key1, value1}, {key2, value2} ...}, parents, children }</code>
     */
    public static Object[][][] getDataSetMetaData(String[] dataSetCodes)
    {
        checkLoggedIn();
        List<DataSet> dataSets = openbis.getDataSetMetaData(Arrays.asList(dataSetCodes));
        TableMap<String, DataSet> dataSetMap =
                new TableMap<String, DataSet>(dataSets, new IKeyExtractor<String, DataSet>()
                    {
                        @Override
                        public String getKey(DataSet e)
                        {
                            return e.getCode();
                        }
                    });
        Object[][][] result = new Object[dataSetCodes.length][][];
        for (int i = 0; i < dataSetCodes.length; i++)
        {
            DataSet dataSet = dataSetMap.tryGet(dataSetCodes[i]);
            result[i] = new Object[5][];
            result[i][0] = new Object[]
            { dataSet.getCode(), dataSet.getDataSetTypeCode() };
            result[i][1] = listProperties(dataSet.getProperties());
            result[i][2] = dataSet.getParentCodes().toArray();
            result[i][3] = dataSet.getChildrenCodes().toArray();
            if (dataSet.isContainerDataSet())
            {
                result[i][4] = new Object[] { "" };
            } else
            {
                result[i][4] = new Object[] { dataSet.tryGetInternalPathInDataStore() };
            }
        }

        return result;
    }

    /**
     * Uploads specified data set for specified plate. The data set code will be returned.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Upload data set /path/to/my-data-set with properties DESCRIPTION and NUMBER for 
     * % plate P005 in space SPACE
     * properties = {'DESCRIPTION' 'hello example'; 'NUMBER' 3.14}
     * datasetcode = OpenBISScreeningML.uploadDataSet('/SPACE/P005', '/path/to/my-data-set', 'HCS_IMAGE', properties)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code.
     * @param dataSetFilePath Path to the data set file/folder to be uploaded.
     * @param dataSetType Data set type.
     * @param dataSetProperties A two dimensional array where the first column contains the property codes and the second column the corresponding
     *            property values.
     */
    public static Object uploadDataSet(String augmentedPlateCode, String dataSetFilePath,
            String dataSetType, Object[][] dataSetProperties)
    {
        checkLoggedIn();
        Plate plateIdentifier = getPlate(augmentedPlateCode);
        File dataSetFile = new File(dataSetFilePath);
        if (dataSetFile.exists() == false)
        {
            throw new RuntimeException("Unknown data set file path '" + dataSetFilePath + "'.");
        }
        try
        {
            Map<String, Serializable> map = createMapSerializable(dataSetProperties);
            IDataSetDss dataSet =
                    openbis.putDataSet(plateIdentifier, dataSetFile, new NewDataSetMetadataDTO(
                            dataSetType, map));
            return dataSet.getCode();
        } catch (Exception ex)
        {
            throw createException("Couldn't upload data set for plate '" + augmentedPlateCode
                    + "'.", ex);
        }
    }

    /**
     * Uploads specified data set for specified plate. The data set code will be returned.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Upload data set /path/to/my-data-set with properties DESCRIPTION and NUMBER for 
     * % plate P005 in space SPACE
     * % with data set  201007091122-928 as the parent
     * properties = {'DESCRIPTION' 'hello example'; 'NUMBER' 3.14}
     * parents = {'201007091122-928' }
     * datasetcode = OpenBISScreeningML.uploadDataSetForPlateAndParents('/SPACE/P005', parents, '/path/to/my-data-set', 'HCS_IMAGE', properties)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code.
     * @param parentDataSetCodeObjects The codes of the parents of this data set
     * @param dataSetFilePath Path to the data set file/folder to be uploaded.
     * @param dataSetType Data set type.
     * @param dataSetProperties A two dimensional array where the first column contains the property codes and the second column the corresponding
     *            property values.
     */
    public static Object uploadDataSetForPlateAndParents(String augmentedPlateCode,
            Object[] parentDataSetCodeObjects, String dataSetFilePath, String dataSetType,
            Object[][] dataSetProperties)
    {
        checkLoggedIn();
        Plate plateIdentifier = getPlate(augmentedPlateCode);
        List<String> dataSetCodes = createStringList(parentDataSetCodeObjects);
        File dataSetFile = new File(dataSetFilePath);
        if (dataSetFile.exists() == false)
        {
            throw new RuntimeException("Unknown data set file path '" + dataSetFilePath + "'.");
        }
        try
        {
            Map<String, Serializable> map = createMapSerializable(dataSetProperties);
            IDataSetDss dataSet =
                    openbis.putDataSet(plateIdentifier, dataSetFile, new NewDataSetMetadataDTO(
                            dataSetType, map, dataSetCodes));
            return dataSet.getCode();
        } catch (Exception ex)
        {
            throw createException("Couldn't upload data set for plate '" + augmentedPlateCode
                    + "'.", ex);
        }
    }

    /**
     * Uploads a data set to the specified experiment, setting the data set parents. The data set code will be returned.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Upload data set /path/to/my-data-set (with property DESCRIPTION)
     * % to experiment E103 in project PROJECT and space SPACE,
     * % with data set  201007091122-928 as the parent
     * properties = {'DESCRIPTION' 'hello example' }
     * parents = {'201007091122-928' }
     * datasetcode = OpenBISScreeningML.uploadDataSetForExperimentAndParents('/SPACE/PROJECT/E103', parents, '/path/to/my-data-set', 'HCS_IMAGE', properties)
     * </pre>
     * 
     * @param augmentedExperimentCode The augmented experiment code.
     * @param parentDataSetCodeObjects The codes of the parents of this data set
     * @param dataSetFilePath Path to the data set file/folder to be uploaded.
     * @param dataSetType Data set type.
     * @param dataSetProperties A two dimensional array where the first column contains the property codes and the second column the corresponding
     *            property values.
     */
    public static Object uploadDataSetForExperimentAndParents(String augmentedExperimentCode,
            Object[] parentDataSetCodeObjects, String dataSetFilePath, String dataSetType,
            Object[][] dataSetProperties)
    {
        checkLoggedIn();
        ExperimentIdentifier experimentIdentifier =
                getExperimentIdentifierOrFail(augmentedExperimentCode);
        List<String> dataSetCodes = createStringList(parentDataSetCodeObjects);
        File dataSetFile = new File(dataSetFilePath);
        if (dataSetFile.exists() == false)
        {
            throw new RuntimeException("Unknown data set file path '" + dataSetFilePath + "'.");
        }
        try
        {
            Map<String, Serializable> map = createMapSerializable(dataSetProperties);
            IDataSetDss dataSet =
                    openbis.putDataSet(experimentIdentifier, dataSetFile,
                            new NewDataSetMetadataDTO(dataSetType, map, dataSetCodes));
            return dataSet.getCode();
        } catch (Exception ex)
        {
            throw createException(
                    "Couldn't upload data set for experiment '" + augmentedExperimentCode
                            + "' and parents '" + Arrays.toString(parentDataSetCodeObjects) + "'.",
                    ex);
        }
    }

    private static Map<String, String> createMap(Object[][] properties)
    {
        Map<String, String> map = new HashMap<String, String>();
        for (Object[] objects : properties)
        {
            if (objects.length == 2)
            {
                Object value = objects[1];
                map.put(objects[0].toString(), value == null ? null : value.toString());
            }
        }
        return map;
    }

    private static Map<String, Serializable> createMapSerializable(Object[][] properties)
    {
        Map<String, Serializable> map = new HashMap<String, Serializable>();
        for (Object[] objects : properties)
        {
            if (objects.length == 2)
            {
                Object value = objects[1];
                map.put(objects[0].toString(), value == null ? null : value.toString());
            }
        }
        return map;
    }

    private static List<String> createStringList(Object[] identifiers)
    {
        List<String> list = new ArrayList<String>();
        for (Object identifier : identifiers)
        {
            list.add(identifier.toString());
        }
        return list;
    }

    //
    // Images
    //

    /**
     * Loads the TIFF images for the given well location, all tiles and all channels and stores them in temporary files. The temporary files will be
     * removed automatically when the Java Virtual Machine exits.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Load the images for all channels of well B10 of plate P005 in space SPACE
     * imginfo = OpenBISScreeningML.loadImages('/SPACE/P005', 2, 10)
     * % Get the plate-well descriptions of all locations
     * imginfo(2,:,3)
     * % Show the third image (assuming there are at least three images)
     * imtool(imginfo(1,3))
     * </pre>
     * 
     * @param plate The augmented plate code
     * @param row The row in the plate to get the images for
     * @param col The column in the plate to get the images for
     * @return <code>{ names of TIFF files, image annotation }</code>
     *         <p>
     *         Each of <code>names of TIFF files</code> and <code>image annotation</code> is a vector of length of the number of images.
     *         <p>
     *         <code>image annotation</code> contains <code>{ channel name, tile number, plate well description, 
     *         plate augmented code, plate perm id, plate space code, plate code, row, column, 
     *         experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] loadImages(String plate, int row, int col)
    {
        return loadImages(plate, row, col, (String[]) null);
    }

    /**
     * Loads the TIFF images for the given well location, tile number, and all channels and stores them in temporary files. The temporary files will
     * be removed automatically when the Java Virtual Machine exits.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Load the images for all channels of well B10 and tile 3 of plate P005 in space SPACE
     * imginfo = OpenBISScreeningML.loadImages('/SPACE/P005', 2, 10, 3)
     * % Get the plate-well descriptions of all locations
     * imginfo(2,:,3)
     * % Show the third image (assuming there are at least three images)
     * imtool(imginfo(1,3))
     * </pre>
     * 
     * @param plate The augmented plate code
     * @param row The row in the plate to get the images for
     * @param col The column in the plate to get the images for
     * @param tile The tile number. Starts with 0.
     * @return <code>{ names of TIFF files, image annotation }</code>
     *         <p>
     *         Each of <code>names of TIFF files</code> and <code>image annotation</code> is a vector of length of the number of images.
     *         <p>
     *         <code>image annotation</code> contains <code>{ channel name, tile number, plate well description, 
     *         plate augmented code, plate perm id, plate space code, plate code, row, column, 
     *         experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] loadImages(String plate, int row, int col, int tile)
    {
        return loadImages(plate, row, col, tile, (String[]) null);
    }

    /**
     * Loads the TIFF images for the given well location, list of channels, and all tiles and stores them in temporary files. The temporary files will
     * be removed automatically when the Java Virtual Machine exits.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Load the images for channel DAPI of well H10 of plate P005 in space SPACE
     * imginfo=OpenBISScreeningML.loadImages('/SPACE/P005', 8, 10, 'DAPI')
     * % Get the channel names and tile numbers of all locations
     * imginfo(2,:,1:2)
     * % Show the second image (assuming there are at least two images)
     * imtool(imginfo(1,2))
     * </pre>
     * 
     * @param plate The augmented plate code
     * @param row The row in the plate to get the images for
     * @param col The column in the plate to get the images for
     * @param channels The names of the channels to get the images for
     * @return <code>{ names of TIFF files, image annotation }</code>
     *         <p>
     *         Each of <code>names of TIFF files</code> and <code>image annotation</code> is a vector of length of the number of images.
     *         <p>
     *         <code>image annotation</code> contains <code>{ channel name, tile number, plate well description, 
     *         plate augmented code, plate perm id, plate space code, plate code, row, column, 
     *         experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] loadImages(String plate, int row, int col, String[] channels)
    {
        return loadRawImages(plate, row, col, channels, createAllTilesIterator());
    }

    /**
     * Has the same effect as {@link #loadImages(String, int, int, String[])}, but instead of loading raw images loads their segmentation results if
     * available.
     * 
     * @param objectNamesOrNull The names of the segmentation objects to get the images for. If <code>null</code> (or <code>[]</code> in MatLab) no
     *            restriction applies.
     * @param analysisProcedureOrNull The analysis procedure used to filter the result. That is, the result is restricted to feature vector data sets
     *            with a value of property <code>ANALYSIS_PROCEDURE</code> as specified. If <code>null</code> (or <code>[]</code> in MatLab) no
     *            restriction applies.
     */
    public static Object[][][] loadSegmentationImages(String plate, int row, int col,
            String[] objectNamesOrNull, String analysisProcedureOrNull)
    {
        return loadSegmentationImages(plate, row, col, objectNamesOrNull, createAllTilesIterator(),
                analysisProcedureOrNull);
    }

    private static ITileNumberIterable createAllTilesIterator()
    {
        return new ITileNumberIterable()
            {
                private int maximumNumberOfTiles;

                @Override
                public void setMaximumNumberOfTiles(int numberOfTiles)
                {
                    this.maximumNumberOfTiles = numberOfTiles;
                }

                @Override
                public int getMaximumNumberOfTiles()
                {
                    return maximumNumberOfTiles;
                }

                @Override
                public Iterator<Integer> iterator()
                {
                    return new Iterator<Integer>()
                        {
                            private int index;

                            @Override
                            public boolean hasNext()
                            {
                                return index < maximumNumberOfTiles;
                            }

                            @Override
                            public Integer next()
                            {
                                return index++;
                            }

                            @Override
                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    /**
     * Loads the TIFF images for the given well location, tile number, and list of channels and stores them in temporary files. The temporary files
     * will be removed automatically when the Java Virtual Machine exits.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Load the images for channel DAPI of well H10 and tile 3 of plate P005 in space SPACE
     * imginfo=OpenBISScreeningML.loadImages('/SPACE/P005', 8, 10, 3, 'DAPI')
     * % Get the channel names of all locations
     * imginfo(2,:,1)
     * % Show the second image (assuming there are at least two images)
     * imtool(imginfo(1,2))
     * </pre>
     * 
     * @param plate The augmented plate code
     * @param row The row in the plate to get the images for
     * @param col The column in the plate to get the images for
     * @param tile The tile number. Starts with 0.
     * @param channels The names of the channels to get the images for
     * @return <code>{ names of TIFF files, image annotation }</code>
     *         <p>
     *         Each of <code>names of TIFF files</code> and <code>image annotation</code> is a vector of length of the number of images.
     *         <p>
     *         <code>image annotation</code> contains <code>{ channel name, tile number, plate well description, 
     *         plate augmented code, plate perm id, plate space code, plate code, row, column, 
     *         experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] loadImages(String plate, int row, int col, final int tile,
            String[] channels)
    {
        return loadRawImages(plate, row, col, channels, createSingleTileIterator(tile));
    }

    /**
     * Has the same effect as {@link #loadImages(String, int, int, int, String[])}, but instead of loading raw images loads their segmentation results
     * if available.
     * 
     * @param objectNamesOrNull The names of the segmentation objects to get the images for. If <code>null</code> (or <code>[]</code> in MatLab) no
     *            restriction applies.
     * @param analysisProcedureOrNull The analysis procedure used to filter the result. That is, the result is restricted to feature vector data sets
     *            with a value of property <code>ANALYSIS_PROCEDURE</code> as specified. If <code>null</code> (or <code>[]</code> in MatLab) no
     *            restriction applies.
     */
    public static Object[][][] loadSegmentationImages(String plate, int row, int col,
            final int tile, String[] objectNamesOrNull, String analysisProcedureOrNull)
    {
        return loadSegmentationImages(plate, row, col, objectNamesOrNull,
                createSingleTileIterator(tile), analysisProcedureOrNull);
    }

    private static ITileNumberIterable createSingleTileIterator(final int tile)
    {
        return new ITileNumberIterable()
            {
                @Override
                public void setMaximumNumberOfTiles(int numberOfTiles)
                {
                    if (tile >= numberOfTiles)
                    {
                        throw new IllegalArgumentException("Tile number " + tile
                                + " is not less than number of tiles " + numberOfTiles + ".");
                    }
                }

                @Override
                public int getMaximumNumberOfTiles()
                {
                    return 1;
                }

                @Override
                public Iterator<Integer> iterator()
                {
                    return new Iterator<Integer>()
                        {
                            private boolean delivered;

                            @Override
                            public boolean hasNext()
                            {
                                return delivered == false;
                            }

                            @Override
                            public Integer next()
                            {
                                delivered = true;
                                return tile;
                            }

                            @Override
                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    /**
     * Lists all segmentation objects for the <var>plate</var>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the segmentation objects of plate P005 in space SPACE.
     * segmentationObjects = OpenBISScreeningML.listSegmentationObjects('/SPACE/P005', []);
     * % Get the segmentation objects of plate P005 in space SPACE for data sets calculated 
     * % with analysis procedure AP-42.
     * segmentationObjects = OpenBISScreeningML.listSegmentationObjects('/SPACE/P005', 'AP-42');
     * % How many segmentation objects do we have?
     * length(segmentationObjects)
     * % What is the name of segmentation objects 1?
     * segmentationObjects(1)
     * </pre>
     * 
     * @param plate augmented code of the plate
     * @param analysisProcedureOrNull The analysis procedure used to filter the result. That is, the result is restricted to feature vector data sets
     *            with a value of property <code>ANALYSIS_PROCEDURE</code> as specified. If <code>null</code> (or <code>[]</code> in MatLab) no
     *            restriction applies.
     * @return Each row contains information about one segmentation object. Currently the only information available is the segmentation object name.
     */
    public static Object[][] listSegmentationObjects(String plate, String analysisProcedureOrNull)
    {
        checkLoggedIn();
        Plate plateId = getPlate(plate);
        final List<ImageDatasetReference> imageDatasets =
                listSegmentationImageDatasets(plateId, analysisProcedureOrNull);
        if (imageDatasets.isEmpty())
        {
            return new Object[0][];
        }
        return extractChannels(imageDatasets.get(0));
    }

    private static Object[][] extractChannels(ImageDatasetReference imageDatasetReference)
    {
        final List<ImageDatasetMetadata> meta =
                openbis.listImageMetadata(Arrays.asList(imageDatasetReference));
        if (meta.isEmpty())
        {
            return new Object[0][];
        }
        final List<String> channels = getChannelCodes(meta);
        Object[][] result = new Object[channels.size()][1];
        for (int i = 0; i < result.length; ++i)
        {
            result[i][0] = channels.get(i);
        }
        return result;
    }

    private static Object[][][] loadRawImages(String plate, int row, int col, String[] channels,
            ITileNumberIterable tileNumberIterable)
    {
        checkLoggedIn();
        final Plate plateId = getPlate(plate);
        final List<ImageDatasetReference> imageDatasets = listRawImageDatasets(plateId);
        return loadImages(plateId, imageDatasets, row, col, channels, tileNumberIterable);
    }

    private static Object[][][] loadSegmentationImages(String plate, int row, int col,
            String[] channelsOrNull, ITileNumberIterable tileNumberIterable,
            String analysisProcedureOrNull)
    {
        checkLoggedIn();
        final Plate plateId = getPlate(plate);
        final List<ImageDatasetReference> imageDatasets =
                listSegmentationImageDatasets(plateId, analysisProcedureOrNull);
        return loadImages(plateId, imageDatasets, row, col, channelsOrNull, tileNumberIterable);
    }

    private static Object[][][] loadImages(Plate plate, List<ImageDatasetReference> imageDatasets,
            int row, int col, String[] channelsOrNull, ITileNumberIterable tileNumberIterable)
    {
        final List<ImageDatasetMetadata> meta = openbis.listImageMetadata(imageDatasets);
        if (meta.isEmpty())
        {
            return new Object[][][]
            { new Object[0][], new Object[0][] };
        }
        final List<String> imageChannels;
        if (channelsOrNull == null || channelsOrNull.length == 0)
        {
            imageChannels = getChannelCodes(meta);
        } else
        {
            imageChannels = Arrays.asList(channelsOrNull);
        }
        final List<ImageReferenceAndFile> imageReferencesAndFiles =
                new ArrayList<ImageReferenceAndFile>(imageDatasets.size());
        final Object[][][] result = new Object[2][][];
        tileNumberIterable.setMaximumNumberOfTiles(meta.get(0).getNumberOfTiles());
        int numberOfTiles = tileNumberIterable.getMaximumNumberOfTiles();
        result[0] = new Object[imageDatasets.size() * imageChannels.size() * numberOfTiles][1];
        result[1] = new Object[imageDatasets.size() * imageChannels.size() * numberOfTiles][15];
        int resultIdx = 0;
        for (ImageDatasetReference ds : imageDatasets)
        {
            for (String channel : imageChannels)
            {
                for (Integer tile : tileNumberIterable)
                {
                    final PlateImageReference ref =
                            new PlateImageReference(row, col, tile, channel, ds);
                    final File imageFile = createImageFileName(plate, ref);
                    imageReferencesAndFiles.add(new ImageReferenceAndFile(ref, imageFile));
                    result[0][resultIdx][0] = imageFile.getPath();
                    PlateIdentifier plateIdentifier = ds.getPlate();
                    ExperimentIdentifier expIdentifier = ds.getExperimentIdentifier();
                    final Object[] annotations =
                            new Object[]
                            { channel, tile,
                                    createPlateWellDescription(plateIdentifier, row, col),
                                    plateIdentifier.getAugmentedCode(),
                                    plateIdentifier.getPermId(),
                                    plateIdentifier.tryGetSpaceCode(),
                                    plateIdentifier.getPlateCode(), row, col,
                                    expIdentifier.getAugmentedCode(),
                                    expIdentifier.getPermId(), expIdentifier.getSpaceCode(),
                                    expIdentifier.getProjectCode(),
                                    expIdentifier.getExperimentCode(), ds.getPermId(), };
                    System.arraycopy(annotations, 0, result[1][resultIdx], 0, annotations.length);
                    resultIdx++;
                }
            }
        }
        try
        {
            loadImages(imageReferencesAndFiles);
        } catch (IOException ex)
        {
            throw createException("Image loading error.", ex);
        }
        return result;
    }

    private static List<ImageDatasetReference> listRawImageDatasets(final Plate plateId)
    {
        return openbis.listRawImageDatasets(Arrays.asList(plateId));
    }

    private static List<ImageDatasetReference> listSegmentationImageDatasets(final Plate plateId,
            String analysisProcedureOrNull)
    {
        return openbis.listSegmentationImageDatasets(Arrays.asList(plateId),
                analysisProcedureOrNull);
    }

    /**
     * Saves images for a given list of image references (given by data set code, well position, channel and tile) and files.
     * 
     * @throws IOException when reading images from the server or writing them to the files fails
     */
    private static void loadImages(List<ImageReferenceAndFile> imageReferencesAndFiles)
            throws IOException
    {
        List<PlateImageReference> imageReferences = new ArrayList<PlateImageReference>();
        final Map<PlateImageReference, ImageReferenceAndFile> imageRefToFileMap =
                new HashMap<PlateImageReference, ImageReferenceAndFile>();
        for (ImageReferenceAndFile imageReferenceAndFile : imageReferencesAndFiles)
        {
            PlateImageReference imageReference = imageReferenceAndFile.getImageReference();
            File file = loadedImages.get(imageReference);
            if (file == null)
            {
                imageReferences.add(imageReference);
                imageRefToFileMap.put(imageReference, imageReferenceAndFile);
            }
        }
        try
        {
            openbis.loadImages(imageReferences, new IImageOutputStreamProvider()
                {
                    @Override
                    public OutputStream getOutputStream(PlateImageReference imageReference)
                            throws IOException
                    {
                        return imageRefToFileMap.get(imageReference).open();
                    }
                }, false);
        } finally
        {
            Collection<ImageReferenceAndFile> values = imageRefToFileMap.values();
            for (ImageReferenceAndFile imageReferenceAndFile : values)
            {
                imageReferenceAndFile.close();
                PlateImageReference imageReference = imageReferenceAndFile.getImageReference();
                loadedImages.put(imageReference, imageReferenceAndFile.getImageFile());
            }
        }
    }

    private static File createImageFileName(Plate plate, PlateImageReference image)
    {
        final WellPosition well = image.getWellPosition();
        File imageDir = new File(temporarySessionDir, "images");
        imageDir.mkdirs();
        final File f =
                new File(imageDir, "img_" + plate.getPlateCode() + "_" + image.getDatasetCode()
                        + "_row" + well.getWellRow() + "_col" + well.getWellColumn()
                        + (image.getChannel() == null ? "" : "_" + image.getChannel()) + "_tile"
                        + image.getTile() + ".tiff");
        f.deleteOnExit();
        return f;
    }

    //
    // Feature matrix
    //

    /**
     * Returns the feature matrix of the specified features for all locations in <var>experiment</var> (a location is one well position in one feature
     * vector data set) in <var>experiment</var> connected to <var>gene</var> in <code>[0]</code>, location annotations in <code>[1]</code> and
     * feature annotation in <code>[2]</code>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get feature matrix for experiment /SPACE/PROJ/MYEXP for locations connected to GENENAME
     * fmatrix = OpenBISScreeningML.getFeatureMatrix('/SPACE/PROJ/MYEXP', 'GENENAME', [], []);
     * % Get feature matrix for features F1, F2 and F3 for 
     * % experiment /SPACE/PROJ/MYEXP for locations connected to GENENAME
     * fmatrix = OpenBISScreeningML.getFeatureMatrix('/SPACE/PROJ/MYEXP', 'GENENAME', [], {'F1' 'F2' 'F3'));
     * % Get feature matrix for features F1 and F2 for experiment /SPACE/PROJ/MYEXP for locations 
     * % connected to GENENAME calculated with analysis procedure AP-42.
     * fmatrix = OpenBISScreeningML.getFeatureMatrix('/SPACE/PROJ/MYEXP', 'GENENAME', 'AP-42', {'F1' 'F2'));
     * % Get the feature vector for the second location (assuming that there are at least two locations) 
     * % of third data set (assuming that there are at least three data sets)
     * fmatrix(1,:,2,3)
     * % Get the values of the fourth feature for all locations (assuming that there are at least 4 features) 
     * % of third data set (assuming that there are at least three data sets)
     * fmatrix(1,4,:,3)
     * % Get code of the fourth feature (assuming that there are at least 4 features)
     * fmatrix(3,4)
     * % Get the plate-well descriptions for the second location (assuming that there are at least two locations) 
     * % of third data set (assuming that there are at least three data sets)
     * fmatrix(2,2,3,:)
     * </pre>
     * 
     * @param experiment The augmented experiment code
     * @param gene The gene code (stored as material code in openBIS, usually it is gene id)
     * @param analysisProcedureOrNull The code of the analysis procedure used to calculate requested features. That is, the result is restricted to
     *            feature vector data sets with a value of property <code>ANALYSIS_PROCEDURE</code> as specified. If <code>null</code> (or
     *            <code>[]</code> in MatLab) no restriction applies.
     * @param featuresOrNull The codes of the features to contain the feature matrix. Unknown feature codes will be ignored. If <code>null</code> (or
     *            <code>[]</code> in MatLab) all features are returned.
     * @return a four dimensional matrix. The first dimension denotes the type in the following order:
     *         <code>{feature matrix, annotations per location, feature codes}</code>. The other dimensions depend on the value of the first
     *         dimension:
     *         <ol>
     *         <li>feature matrix: 2. dimension is feature vector, 3. dimension is location number, 4. dimension is data set number. If for a
     *         particular location and a particular data set the corresponding feature value does not exists <code>NaN</code> will be returned. <li>
     *         annotations: 2. dimension is location number, 3. dimension is data set number, 4. dimension is location annotations in the following
     *         order: <code>{plate well description, plate augmented code, plate perm id, plate space code, plate code, row, column, experiment
     *         augmented code, experiment perm id, experiment space code, experiment project code, experiment code, data set code, data set type}
     *         </code> <li> feature codes: 2. dimension is feature codes in alphabetical order. 3. and 4. dimension are meaningless (i.e. they have
     *         length one)
     *         </ol>
     */
    public static Object[][][][] getFeatureMatrix(String experiment, String gene,
            String analysisProcedureOrNull, String[] featuresOrNull)
    {
        checkLoggedIn();
        final ExperimentIdentifier experimentId = experimentCodeToExperimentMap.get(experiment);
        if (experimentId == null)
        {
            throw new RuntimeException("No experiment with that code found.");
        }
        final List<Plate> experimentPlates = experimentToPlateMap.get(experiment);
        if (experimentPlates == null || experimentPlates.isEmpty())
        {
            return new Object[][][][]
            { new Object[0][][], new Object[0][][], new Object[0][][] };
        }
        final List<FeatureVectorWithDescription> featureVectors =
                openbis.loadFeaturesForPlateWells(experimentId, new MaterialIdentifier(
                        MaterialTypeIdentifier.GENE, gene), analysisProcedureOrNull,
                        (featuresOrNull == null) ? null : Arrays.asList(featuresOrNull));
        return getFeatureMatrix(featureVectors);
    }

    /**
     * Returns the feature matrix of the specified features for all locations (a location is one well position in one feature vector data set) in
     * <var>experiment</var> connected to <var>gene</var> in <code>[0]</code>, location annotations in <code>[1]</code> and feature annotation in
     * <code>[2]</code>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get feature matrix for GENENAME
     * fmatrix = OpenBISScreeningML.getFeatureMatrix('GENENAME', [], []);
     * % Get feature matrix for features FEATURE1, FEATURE2 and FEATURE3 for GENENAME
     * fmatrix = OpenBISScreeningML.getFeatureMatrix('GENENAME', [], {'FEATURE1' 'FEATURE2' 'FEATURE3'});
     * % Get feature matrix for features FEATURE1 and FEATURE2 for GENENAME 
     * % computed with analysis procedure AP-42
     * fmatrix = OpenBISScreeningML.getFeatureMatrix('GENENAME', 'AP-42', {'FEATURE1' 'FEATURE2'});
     * % Get the feature vector for the second location (assuming that there are at least two locations) 
     * % of third data set (assuming that there are at least three data sets)
     * fmatrix(1,:,2,3)
     * % Get the values of the fourth feature for all locations (assuming that there are at least 4 features) 
     * % of third data set (assuming that there are at least three data sets)
     * fmatrix(1,4,:,3)
     * % Get code of the fourth feature (assuming that there are at least 4 features)
     * fmatrix(3,4)
     * % Get the plate-well descriptions for the second location (assuming that there are at least two locations) 
     * % of third data set (assuming that there are at least three data sets)
     * fmatrix(2,2,3,:)
     * </pre>
     * 
     * @param gene The gene code (stored as material code in openBIS, usually it is gene id)
     * @param analysisProcedureOrNull The code of the analysis procedure used to calculate requested features. That is, the result is restricted to
     *            feature vector data sets with a value of property <code>ANALYSIS_PROCEDURE</code> as specified. If <code>null</code> (or
     *            <code>[]</code> in MatLab) no restriction applies.
     * @param featuresOrNull The codes of the features to contain the feature matrix. Unknown feature codes will be ignored. If <code>null</code> (or
     *            <code>[]</code> in MatLab) all features are returned.
     * @return a four dimensional matrix. The first dimension denotes the type in the following order:
     *         <code>{feature matrix, annotations per location, feature codes}</code>. The other dimensions depend on the value of the first
     *         dimension:
     *         <ol>
     *         <li>feature matrix: 2. dimension is feature vector, 3. dimension is location number, 4. dimension is data set number. If for a
     *         particular location and a particular data set the corresponding feature value does not exists <code>NaN</code> will be returned. <li>
     *         annotations: 2. dimension is location number, 3. dimension is data set number, 4. dimension is location annotations in the following
     *         order: <code>{plate well description, plate augmented code, plate perm id, plate space code, plate code, row, column, experiment
     *         augmented code, experiment perm id, experiment space code, experiment project code, experiment code, data set code, data set type}
     *         </code> <li> feature codes: 2. dimension is feature codes in alphabetical order. 3. and 4. dimension are meaningless (i.e. they have
     *         length one)
     *         </ol>
     */
    public static Object[][][][] getFeatureMatrix(String gene, String analysisProcedureOrNull,
            String[] featuresOrNull)
    {
        checkLoggedIn();
        final List<FeatureVectorWithDescription> featureVectors =
                openbis.loadFeaturesForPlateWells(new MaterialIdentifier(
                        MaterialTypeIdentifier.GENE, gene), analysisProcedureOrNull,
                        (featuresOrNull == null) ? null : Arrays.asList(featuresOrNull));
        return getFeatureMatrix(featureVectors);
    }

    private static Object[][][][] getFeatureMatrix(
            final List<FeatureVectorWithDescription> featureVectors)
    {
        final Object[][][][] result = new Object[3][][][];
        if (featureVectors.isEmpty())
        {
            return result;
        }
        List<String> featureCodes = getFeatureCodes(featureVectors);
        Map<String, Integer> featureCodeToIndexMap = new HashMap<String, Integer>();
        result[2] = new Object[featureCodes.size()][1][1];
        for (int i = 0; i < featureCodes.size(); ++i)
        {
            String code = featureCodes.get(i);
            result[2][i][0][0] = code;
            featureCodeToIndexMap.put(code, i);
        }
        IndexSet<String> dataSetCodes = new IndexSet<String>();
        IndexSet<WellPosition> wellPositions = new IndexSet<WellPosition>();
        for (FeatureVectorWithDescription featureVector : featureVectors)
        {
            WellPosition wellPosition = featureVector.getWellPosition();
            wellPositions.add(wellPosition);
            dataSetCodes.add(featureVector.getDatasetWellReference().getDatasetCode());
        }
        result[0] = new Object[featureCodes.size()][wellPositions.size()][dataSetCodes.size()];
        result[1] = new Object[wellPositions.size()][dataSetCodes.size()][14];
        for (FeatureVectorWithDescription vector : featureVectors)
        {
            WellPosition wellPosition = vector.getWellPosition();
            int wellIndex = wellPositions.getIndex(wellPosition);
            int[] featureIndexes = createFeatureIndexes(vector, featureCodeToIndexMap);
            FeatureVectorDatasetWellReference dataSetRef = vector.getDatasetWellReference();
            int dataSetIndex = dataSetCodes.getIndex(dataSetRef.getDatasetCode());
            copyFeatureValuesInto(result, wellIndex, dataSetIndex, featureIndexes, vector);
            copyAnnotationsInto(result, wellIndex, dataSetIndex, dataSetRef, vector);
        }
        return replaceFeatureNullValuesByNaN(result);
    }

    /**
     * Returns the feature matrix of the specified features for all locations (a location is one well position in one feature vector data set) of all
     * feature vector data sets created by specified analysis procedure of the given <var>plate</var> in <code>[0]</code>, location annotations in
     * <code>[1]</code> and feature annotation in <code>[2]</code>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get feature matrix for PLATECODE
     * fmatrix = OpenBISScreeningML.getFeatureMatrixForPlate('PLATECODE', [], []);
     * % Get feature matrix for features FEATURE1, FEATURE2 and FEATURE3 for PLATECODE.
     * fmatrix = OpenBISScreeningML.getFeatureMatrixForPlate('PLATECODE', [], {'FEATURE1' 'FEATURE2' 'FEATURE3'});
     * % Get feature matrix for features FEATURE1 and FEATURE2 for PLATECODE calculated by analysis procedure AP-42.
     * fmatrix = OpenBISScreeningML.getFeatureMatrixForPlate('PLATECODE', 'AP-42', {'FEATURE1' 'FEATURE2'});
     * % Get the feature vector for the second location (assuming that there are at least two locations) 
     * % of third data set (assuming that there are at least three data sets)
     * fmatrix(1,:,2,3)
     * % Get the values of the fourth feature for all locations (assuming that there are at least 4 features) 
     * % of third data set (assuming that there are at least three data sets)
     * fmatrix(1,4,:,3)
     * % Get code of the fourth feature (assuming that there are at least 4 features)
     * fmatrix(3,4)
     * % Get the plate-well descriptions for the second location (assuming that there are at least two locations) 
     * % of third data set (assuming that there are at least three data sets)
     * fmatrix(2,2,3,:)
     * </pre>
     * 
     * @param plate augmented code of the plate for which features should be loaded
     * @param analysisProcedureOrNull The code of the analysis procedure used to calculate requested features. That is, the result is restricted to
     *            feature vector data sets with a value of property <code>ANALYSIS_PROCEDURE</code> as specified. If <code>null</code> (or
     *            <code>[]</code> in MatLab) no restriction applies.
     * @param featuresOrNull The codes of the features to contain the feature matrix. Unknown feature codes will be ignored. If <code>null</code> (or
     *            <code>[]</code> in MatLab) all features are returned.
     * @return a four dimensional matrix. The first dimension denotes the type in the following order:
     *         <code>{feature matrix, annotations per location, feature codes}</code>. The other dimensions depend on the value of the first
     *         dimension:
     *         <ol>
     *         <li>feature matrix: 2. dimension is feature vector, 3. dimension is location number, 4. dimension is data set number. If for a
     *         particular location and a particular data set the corresponding feature value does not exists <code>NaN</code> will be returned. <li>
     *         annotations: 2. dimension is location number, 3. dimension is data set number, 4. dimension is location annotations in the following
     *         order: <code>{plate well description, plate augmented code, plate perm id, plate space code, plate code, row, column, experiment
     *         augmented code, experiment perm id, experiment space code, experiment project code, experiment code, data set code, data set type}
     *         </code> <li> feature codes: 2. dimension is feature codes in alphabetical order. 3. and 4. dimension are meaningless (i.e. they have
     *         length one)
     *         </ol>
     */
    public static Object[][][][] getFeatureMatrixForPlate(String plate,
            String analysisProcedureOrNull, String[] featuresOrNull)
    {
        checkLoggedIn();
        final List<FeatureVectorDataset> dataSets =
                openbis.loadFeaturesForPlates(
                        Arrays.asList(PlateIdentifier.createFromAugmentedCode(plate)),
                        (featuresOrNull == null) ? null : Arrays.asList(featuresOrNull),
                        analysisProcedureOrNull);
        final Object[][][][] result = new Object[3][][][];
        if (dataSets.isEmpty())
        {
            return result;
        }
        List<String> featureCodes = getFeatureCodes(dataSets);
        Map<String, Integer> featureCodeToIndexMap = new HashMap<String, Integer>();
        result[2] = new Object[featureCodes.size()][1][1];
        for (int i = 0; i < featureCodes.size(); ++i)
        {
            String code = featureCodes.get(i);
            result[2][i][0][0] = code;
            featureCodeToIndexMap.put(code, i);
        }
        IndexSet<WellPosition> wellPositions = new IndexSet<WellPosition>();
        for (FeatureVectorDataset dataSet : dataSets)
        {
            List<FeatureVector> featureVectors = dataSet.getFeatureVectors();
            for (FeatureVector featureVector : featureVectors)
            {
                WellPosition wellPosition = featureVector.getWellPosition();
                wellPositions.add(wellPosition);
            }
        }
        int numberOfDataSets = dataSets.size();
        result[0] = new Object[featureCodes.size()][wellPositions.size()][numberOfDataSets];
        result[1] = new Object[wellPositions.size()][numberOfDataSets][14];
        for (int dataSetIndex = 0; dataSetIndex < numberOfDataSets; dataSetIndex++)
        {
            FeatureVectorDataset dataSet = dataSets.get(dataSetIndex);
            FeatureVectorDatasetReference dataSetRef = dataSet.getDataset();
            int[] featureIndexes = createFeatureIndexes(dataSet, featureCodeToIndexMap);
            for (FeatureVector vector : dataSet.getFeatureVectors())
            {
                WellPosition wellPosition = vector.getWellPosition();
                int wellIndex = wellPositions.getIndex(wellPosition);
                copyFeatureValuesInto(result, wellIndex, dataSetIndex, featureIndexes, vector);
                copyAnnotationsInto(result, wellIndex, dataSetIndex, dataSetRef, vector);
            }
        }
        return replaceFeatureNullValuesByNaN(result);
    }

    private static Object[][][][] replaceFeatureNullValuesByNaN(final Object[][][][] result)
    {
        Double nan = Double.NaN;
        for (int i = 0; i < result[0].length; i++)
        {
            Object[][] r0i = result[0][i];
            for (int j = 0; j < r0i.length; j++)
            {
                Object[] r0ij = r0i[j];
                for (int k = 0; k < r0ij.length; k++)
                {
                    if (r0ij[k] == null)
                    {
                        r0ij[k] = nan;
                    }
                }
            }
        }
        return result;
    }

    private static int[] createFeatureIndexes(IFeatureCodesProvider dataSet,
            Map<String, Integer> featureCodeToIndexMap)
    {
        List<String> codes = dataSet.getFeatureCodes();
        int[] featureIndexes = new int[codes.size()];
        for (int i = 0; i < featureIndexes.length; i++)
        {
            featureIndexes[i] = featureCodeToIndexMap.get(codes.get(i));
        }
        return featureIndexes;
    }

    private static void copyFeatureValuesInto(final Object[][][][] result, int wellIndex,
            int dataSetIndex, int[] featureIndexes, FeatureVector vector)
    {
        List<Object> valueObjects = vector.getValueObjects();
        for (int i = 0, n = valueObjects.size(); i < n; i++)
        {
            int featureIndex = featureIndexes[i];
            try
            {
                Object[][] f = result[0][featureIndex];
                Object[] d = f[wellIndex];
                d[dataSetIndex] = valueObjects.get(i);

            } catch (Exception ex)
            {
                throw createException(featureIndex + "." + wellIndex + "." + dataSetIndex, ex);
            }
        }
    }

    private static void copyAnnotationsInto(final Object[][][][] result, int wellIndex,
            int dataSetIndex, FeatureVectorDatasetReference dataSetRef, FeatureVector vector)
    {
        Object[] annotations =
                new Object[]
                { createPlateWellDescription(dataSetRef.getPlate(), vector),
                        dataSetRef.getPlate().getAugmentedCode(),
                        dataSetRef.getPlate().getPermId(),
                        dataSetRef.getPlate().tryGetSpaceCode(),
                        dataSetRef.getPlate().getPlateCode(),
                        vector.getWellPosition().getWellRow(),
                        vector.getWellPosition().getWellColumn(),
                        dataSetRef.getExperimentIdentifier().getAugmentedCode(),
                        dataSetRef.getExperimentIdentifier().getPermId(),
                        dataSetRef.getExperimentIdentifier().getSpaceCode(),
                        dataSetRef.getExperimentIdentifier().getProjectCode(),
                        dataSetRef.getExperimentIdentifier().getExperimentCode(),
                        dataSetRef.getDatasetCode(), dataSetRef.getDataSetType() };
        System.arraycopy(annotations, 0, result[1][wellIndex][dataSetIndex], 0, annotations.length);
    }

    private static List<String> getFeatureCodes(List<? extends IFeatureCodesProvider> dataSets)
    {
        Set<String> codes = new HashSet<String>();
        for (IFeatureCodesProvider featureVectorDataset : dataSets)
        {
            codes.addAll(featureVectorDataset.getFeatureCodes());
        }
        List<String> result = new ArrayList<String>(codes);
        Collections.sort(result);
        return result;
    }

    /**
     * Returns the gene mapping for the given <var>plateCodes</var> in <code>[0]</code> and location annotations in <code>[1]</code>.
     * <p>
     * One row in the matrix corresponds to one well.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get feature matrix for features FEATURE1, FEATURE2 and FEATURE for PLATECODE
     * genes = getGeneMappingForPlate('PLATECODE');
     * % Get the plate well location description of the 10th wells
     * loc2 = genes(2,10,1)
     * % Get the gene ids that are in the 10th well
     * geneIds = genes(1,10,:)
     * </pre>
     * 
     * @param platesCodes The augmented codes of the plates to get the mapping for
     * @return <code>{ gene ids, annotations per well }</code> where <code>gene ids</code> can be 0, 1 or more gene ids.
     *         <code>annotations per location</code> contain:
     *         <p>
     *         <code>{ plate well description, plate augmented code, plate perm id,
     *         plate space code, plate code, row, column }</code>
     */
    public static Object[][][] getGeneMappingForPlates(String[] platesCodes)
    {
        checkLoggedIn();
        final List<PlateWellMaterialMapping> mappingList =
                openbis.listPlateMaterialMapping(toPlates(platesCodes), MaterialTypeIdentifier.GENE);
        int size = 0;
        for (PlateWellMaterialMapping mapping : mappingList)
        {
            size +=
                    mapping.getPlateGeometry().getNumberOfRows()
                            * mapping.getPlateGeometry().getNumberOfColumns();
        }
        final Object[][][] result = new Object[2][size][];
        int resultIdx = 0;
        for (PlateWellMaterialMapping mapping : mappingList)
        {
            for (int row = 1; row <= mapping.getPlateGeometry().getNumberOfRows(); ++row)
            {
                for (int col = 1; col <= mapping.getPlateGeometry().getNumberOfColumns(); ++col)
                {
                    final List<MaterialIdentifier> genes = mapping.getMaterialsForWell(row, col);
                    result[0][resultIdx] = new Object[genes.size()];
                    for (int i = 0; i < genes.size(); ++i)
                    {
                        result[0][resultIdx][i] = genes.get(i).getMaterialCode();
                    }
                    final PlateIdentifier plate = mapping.getPlateIdentifier();
                    result[1][resultIdx] =
                            new Object[]
                            { createPlateWellDescription(plate, row, col),
                                    plate.getAugmentedCode(), plate.getPermId(),
                                    plate.tryGetSpaceCode(), plate.getPlateCode(), row, col, };
                    ++resultIdx;
                }
            }
        }
        return result;
    }

    /**
     * Experimental method that returns an array of {@link PlateMetadata} Java objects for a given list of plate codes.
     * <p>
     * 
     * <pre>
     * chosenPlates= { '/MY-SPACE/MY-PLATE1', '/MY-SPACE/MY-PLATE2' };
     * metadata = OpenBISScreeningML.getPlateMetadataList(chosenPlates);
     * % List all methods available on the result:
     * methods(metadata(1),'-full');
     * % Lists all wells of the first plate
     * javaMethod('getWells',metadata(1))
     * % Shows plate geometry of the first plate
     * javaMethod('getPlateGeometry',metadata(1))
     * </pre>
     */
    public static PlateMetadata[] getPlateMetadataList(String[] platesCodes)
    {
        checkLoggedIn();

        List<PlateMetadata> metadataList = openbis.getPlateMetadataList(toPlates(platesCodes));

        return metadataList.toArray(new PlateMetadata[0]);
    }

    public static String getContainerDataSetCode(String dataSetCode)
    {
        checkLoggedIn();

        DataSet dataSet = genericOpenbis.getDataSet(dataSetCode);

        if (dataSet != null)
        {
            DataSet container = dataSet.getContainerOrNull();

            if (container != null)
            {
                return container.getCode();
            }
        }
        return null;
    }

    //
    // Helper methods
    //

    private static RuntimeException createException(String message, Throwable cause)
    {
        Throwable originalCause = cause;
        while (originalCause.getCause() != null)
        {
            originalCause = originalCause.getCause();
        }
        return new RuntimeException(message + " Reason: " + originalCause, cause);
    }

    private static List<PlateIdentifier> toPlates(String[] augmentedPlateCodes)
    {
        final List<PlateIdentifier> result =
                new ArrayList<PlateIdentifier>(augmentedPlateCodes.length);
        for (String plateCode : augmentedPlateCodes)
        {
            result.add(PlateIdentifier.createFromAugmentedCode(plateCode));
        }
        return result;
    }

    private static Object[][] listProperties(Map<String, String> properties)
    {
        if (properties == null || properties.isEmpty())
        {
            return new Object[0][];
        }

        List<Map.Entry<String, String>> list =
                new ArrayList<Map.Entry<String, String>>(properties.entrySet());
        Object[][] result = new Object[list.size()][2];
        for (int i = 0; i < list.size(); i++)
        {
            result[i] = new Object[]
            { list.get(i).getKey(), list.get(i).getValue() };
        }
        return result;
    }

    private static Plate getPlate(String augmentedPlateCode)
    {
        Plate plateIdentifier = plateCodeToPlateMap.get(augmentedPlateCode);
        if (plateIdentifier == null)
        {
            throw new RuntimeException("No plate with that code '" + augmentedPlateCode
                    + "' found.");
        }
        return plateIdentifier;
    }

    private static ExperimentIdentifier getExperimentIdentifierOrFail(String experiment)
    {
        ExperimentIdentifier experimentIdentifier = experimentCodeToExperimentMap.get(experiment);
        if (experimentIdentifier == null)
        {
            String errorMessage = String.format("No experiment with code '%s' found.", experiment);
            throw new RuntimeException(errorMessage);
        }
        return experimentIdentifier;
    }

    private static String createPlateWellDescription(PlateIdentifier p, FeatureVector f)
    {
        return createPlateWellDescription(p, f.getWellPosition().getWellRow(), f.getWellPosition()
                .getWellColumn());
    }

    private static String createPlateWellDescription(PlateIdentifier p, int row, int col)
    {
        return p.getPlateCode() + ":" + translateRowNumberIntoLetterCode(row) + col;
    }

    /**
     * Translates a row number into letter code. Thus, 1 -> A, 2 -> B, 26 -> Z, 27 -> AA, 28 -> AB, etc.
     */
    private static String translateRowNumberIntoLetterCode(int rowNumber)
    {
        int rowIndex = rowNumber - 1;
        String code = "";
        while (rowIndex >= 0)
        {
            code = (char) (rowIndex % 26 + 'A') + code;
            rowIndex = rowIndex / 26 - 1;
        }
        return code;
    }

    private static void checkLoggedIn()
    {
        if (openbis == null)
        {
            if (Login.OPENBIS_TOKEN_FILE.exists())
            {
                BufferedReader br = null;
                try
                {
                    br = new BufferedReader(new FileReader(Login.OPENBIS_TOKEN_FILE));
                    final String token = br.readLine();
                    br.close();
                    br = new BufferedReader(new FileReader(Login.OPENBIS_SERVER_URL_FILE));
                    final String serverUrl = br.readLine();
                    br.close();
                    br = null;
                    IScreeningOpenbisServiceFacade facade =
                            facadeFactory.tryToCreate(token, serverUrl);
                    if (facade == null)
                    {
                        throw new RuntimeException("Login failed.");
                    }
                    IOpenbisServiceFacade genericFacade =
                            genericFacadeFactory.tryToCreate(token, serverUrl, 0);
                    init(facade, genericFacade);
                } catch (IOException ex)
                {
                    if (openbis == null)
                    {
                        throw createException("Login failed.", ex);
                    }
                } finally
                {
                    if (br != null)
                    {
                        try
                        {
                            br.close();
                        } catch (IOException ex)
                        {
                            // Silence this.
                        }
                    }
                }
            }
            if (openbis == null)
            {
                throw new RuntimeException("Not logged in.");
            }
        }
    }

}
