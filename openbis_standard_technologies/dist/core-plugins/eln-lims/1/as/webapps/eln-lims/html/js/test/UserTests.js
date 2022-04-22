var UserTests = new function() {

    this.inventorySpaceForTestUser = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(5);

            var ids = ["LAB_NOTEBOOK",
                       "TESTID",
                       "MATERIALS",
                       "_MATERIALS_BACTERIA_BACTERIA_COLLECTION",
                       "_MATERIALS_CELL_LINES_CELL_LINE_COLLECTION",
                       "_MATERIALS_FLIES_FLY_COLLECTION",
                       "_MATERIALS_PLANTS_PLANT_COLLECTION",
                       "_MATERIALS_PLASMIDS_PLASMID_COLLECTION",
                       "_MATERIALS_POLYNUCLEOTIDES_OLIGO_COLLECTION",
                       "_MATERIALS_POLYNUCLEOTIDES_RNA_COLLECTION",
                       "_MATERIALS_REAGENTS_ANTIBODY_COLLECTION",
                       "_MATERIALS_REAGENTS_CHEMICAL_COLLECTION",
                       "_MATERIALS_REAGENTS_ENZYME_COLLECTION",
                       "_MATERIALS_REAGENTS_MEDIA_COLLECTION",
                       "_MATERIALS_REAGENTS_SOLUTION_BUFFER_COLLECTION",
                       "_MATERIALS_YEASTS_YEAST_COLLECTION",
                       "METHODS",
                       "_METHODS_PROTOCOLS_GENERAL_PROTOCOLS",
                       "_METHODS_PROTOCOLS_PCR_PROTOCOLS",
                       "_METHODS_PROTOCOLS_WESTERN_BLOTTING_PROTOCOLS",
                       "PUBLICATIONS",
                       "PUBLIC_REPOSITORIES",
                       "_PUBLICATIONS_PUBLIC_REPOSITORIES_PUBLICATIONS_COLLECTION"];

            Promise.resolve().then(() => TestUtil.verifyInventory(e, ids))
                             .then(() => e.verifyExistence("USER_MANAGER", false))
                             .then(() => e.sleep(1000))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.creationSampleForm = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(6);

            Promise.resolve().then(() => UserTests.createBacteria(e, "BAC1", "Aurantimonas"))
                             .then(() => UserTests.createBacteria(e, "BAC2", "Burantimonas"))
                             .then(() => UserTests.createBacteria(e, "BAC3", "Curantimonas"))
                             .then(() => UserTests.createBacteria(e, "BAC4", "Durantimonas"))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.createBacteria = function(e, code, name) {
        return new Promise(function executor(resolve, reject) {
            var testChain = Promise.resolve();

            var richText = '<p><span style="color:#000080;"><strong>F-&nbsp;tonA21 thi-1 thr-1 leuB6 lacY1</strong></span><strong>&nbsp;</strong><span style="color:#008000;"><i><strong>glnV44 rfbC1 fhuA1 ?? mcrB e14-(mcrA-)</strong></i></span><i><strong>&nbsp;</strong></i><span style="color:#cc99ff;"><strong><u>hsdR(rK&nbsp;-mK&nbsp;+) λ-</u></strong></span></p>';

            testChain.then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                     .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                     .then(() => e.waitForId("create-btn"))
                     .then(() => e.click("create-btn"))
                     .then(() => e.waitForId("sampleFormTitle"))
                     .then(() => e.equalTo("sampleFormTitle", "New Bacteria", true, false));

            if (code === "BAC1") {
                // Show Code
                testChain.then(() => e.waitForId("options-menu-btn-sample-view-bacteria"))
                         .then(() => e.click("options-menu-btn-sample-view-bacteria"))
                         .then(() => e.waitForId("options-menu-btn-identification-info"))
                         .then(() => e.click("options-menu-btn-identification-info"));
            }

            testChain.then(() => e.waitForId("codeId"))
                     .then(() => e.waitForFill("codeId"))
                     .then(() => e.equalTo("codeId", code, true, false))
                     .then(() => e.waitForId("NAME"))
                     .then(() => e.change("NAME", name, false))
                     //Paste from Word
                     .then(() => e.waitForCkeditor("BACTERIA.GENOTYPE"))
                     .then(() => TestUtil.ckeditorSetData("BACTERIA.GENOTYPE", richText))
                     .then(() => e.waitForId("save-btn"))
                     .then(() => e.click("save-btn"))
                     //Check saving results
                     .then(() => e.waitForId("edit-btn"))
                     .then(() => e.waitForId("NAME"))
                     .then(() => e.equalTo("NAME", name, true, false))
                     .then(() => TestUtil.ckeditorTestData("BACTERIA.GENOTYPE", richText))
                     .then(() => resolve())
                     .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.editSampleForm = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/test-image.png";

        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(7);

            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac3-column-id"))
                             .then(() => e.click("bac3-column-id"))
                             // Edit Bacteria 3
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.click("edit-btn"))
                             // add photo in Bacteria genotype
                             .then(() => e.waitForCkeditor("BACTERIA.GENOTYPE"))
                             .then(() => TestUtil.ckeditorDropFile("BACTERIA.GENOTYPE", "test-image.png", baseURL + pathToResource))
                             // add mother
                             .then(() => e.waitForId("search-btn-bacteria-parents"))
                             .then(() => e.click("search-btn-bacteria-parents"))
                             .then(() => e.searchForObjectInSelect2(e, "BAC1", "add-object-bacteria"))
                             .then(() => e.waitForId("comments-bac1"))
                             .then(() => e.change("comments-bac1", "mother"))
                             // add father
                             .then(() => e.waitForId("search-btn-bacteria-parents"))
                             .then(() => e.click("search-btn-bacteria-parents"))
                             .then(() => e.searchForObjectInSelect2(e, "BAC2", "add-object-bacteria"))
                             .then(() => e.waitForId("comments-bac2"))
                             .then(() => e.change("comments-bac2", "father"))
                             // add Child
                             .then(() => e.waitForId("plus-btn-children-type-selector"))
                             .then(() => e.click("plus-btn-children-type-selector"))
                             .then(() => e.waitForId("sampleTypeSelector"))
                             .then(() => e.changeSelect2("sampleTypeSelector", 'BACTERIA'))
                             .then(() => e.searchForObjectInSelect2(e, "BAC4", "add-object-bacteria"))
                             .then(() => e.waitForId("bac4-column-id"))
                             // save
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("edit-btn"))
                             // check parents and children
                             .then(() => e.waitForId("bac1-column-id"))
                             .then(() => e.waitForId("bac2-column-id"))
                             .then(() => e.waitForId("bac4-column-id"))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.sampleHierarchyAsGraph = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(8);
            Promise.resolve().then(() => e.waitForId("options-menu-btn-sample-view-bacteria"))
                             .then(() => e.click("options-menu-btn-sample-view-bacteria"))
                             // show Hierarchy Graph
                             .then(() => e.waitForId("hierarchy-graph"))
                             .then(() => e.click("hierarchy-graph"))
                             // check parents and children
                             .then(() => e.waitForId("bac1"))
                             .then(() => e.waitForId("bac2"))
                             .then(() => e.waitForId("bac3"))
                             .then(() => e.waitForId("bac4"))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.sampleHierarchyAsTable = function() {
        var motherFirst = "<b>Code</b>: BAC1, <b>Comments</b>: mother<br><br><b>Code</b>: BAC2, <b>Comments</b>: father";
        var fatherFirst = "<b>Code</b>: BAC2, <b>Comments</b>: father<br><br><b>Code</b>: BAC1, <b>Comments</b>: mother";
        var childrenAnnotations = "<b>Code</b>: BAC4";

        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(9);

            // return to bacteria 3
            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac3-column-id"))
                             .then(() => e.click("bac3-column-id"))
                             // show Hierarchy Table
                             .then(() => e.waitForId("options-menu-btn-sample-view-bacteria"))
                             .then(() => e.click("options-menu-btn-sample-view-bacteria"))
                             .then(() => e.waitForId("hierarchy-table"))
                             .then(() => e.click("hierarchy-table"))
                             .then(() => e.sleep(2000)) // wait for table
                             // check parents and children
                             .then(() => e.waitForId("bac1"))
                             .then(() => e.waitForId("bac2"))
                             .then(() => e.waitForId("bac3"))
                             .then(() => e.equalTo("children-annotations-bac3", childrenAnnotations, true, false))
                             .then(() => e.waitForId("bac4"))
                             // check parents comments
                             .then(() => e.waitForId("parent-annotations-bac3"))
                             .then(() => e.contains("parent-annotations-bac3", [motherFirst, fatherFirst], false))
                             .then(() => e.sleep(2000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.copySampleForm = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(10);

            // return to bacteria 3
            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac3-column-id"))
                             .then(() => e.click("bac3-column-id"))
                             .then(() => e.waitForId("edit-btn"))
                             // copy
                             .then(() => e.waitForId("options-menu-btn-sample-view-bacteria"))
                             .then(() => e.click("options-menu-btn-sample-view-bacteria"))
                             .then(() => e.waitForId("copy"))
                             .then(() => e.click("copy"))
                             .then(() => e.sleep(1000))
                             // link parents
                             .then(() => e.waitForId("linkParentsOnCopy"))
                             .then(() => e.checked("linkParentsOnCopy", true))
                             .then(() => e.waitForId("copyChildrenToParent"))
                             .then(() => e.checked("copyChildrenToParent", true))
                             .then(() => e.waitForId("copyAccept"))
                             .then(() => e.click("copyAccept"))
                             .then(() => e.waitForId("jSuccess")) // wait when copy will finished
                             .then(() => e.sleep(2000))
                             // go to bac1
                             .then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac1-column-id"))
                             .then(() => e.click("bac1-column-id"))
                             // check new object in bac1 graph
                             .then(() => e.waitForId("options-menu-btn-sample-view-bacteria"))
                             .then(() => e.click("options-menu-btn-sample-view-bacteria"))
                             .then(() => e.waitForId("hierarchy-graph"))
                             .then(() => e.click("hierarchy-graph"))
                             // origin bacteria
                             .then(() => e.waitForId("bac3"))
                             .then(() => e.waitForId("bac4"))
                             // copy of origin bacteria
                             .then(() => e.waitForId("bac5"))
                             .then(() => e.waitForId("bac5_bac4"))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.deleteSampleForm = function() {

        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(11);
            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             // navigation to BAC5
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac5-column-id"))
                             .then(() => e.click("bac5-column-id"))
                             // delete
                             .then(() => e.waitForId("options-menu-btn-sample-view-bacteria"))
                             .then(() => e.click("options-menu-btn-sample-view-bacteria"))
                             // wait for "copy" to make sure that "More..." menu has shown
                             .then(() => e.waitForId("copy"))
                             // make sure Delete option is not available in the "More..." dropdown
                             .then(() => e.verifyExistence("delete", false))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.exportsImportsUpdate = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToCheckResource = "js/test/resources/exportedTableAllColumnsAllRows.tsv";
        var pathToUpdateResource = "js/test/resources/updateAllColumnsAllRows.tsv";

        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(12);
            var r = ReactTestUtils;
            Promise.resolve().then(() => TestUtil.overloadSaveAs())
                             .then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => TestUtil.testLocally("12.1 \"EXPORTS\""))
                             // Batch Update Objects
                             .then(() => UserTests.importBacteriasFromFile(e, baseURL + pathToUpdateResource, false))
                             .then(() => e.waitForId("jSuccess"))
                             .then(() => e.sleep(5000)) // wait for import
                             // check names after update
                             .then(() => e.waitForId("bac1-name-id"))
                             .then(() => e.equalTo("bac1-name-id", "AA", true, false))
                             .then(() => e.equalTo("bac2-name-id", "BB", true, false))
                             .then(() => e.equalTo("bac3-name-id", "CC", true, false))
                             .then(() => e.equalTo("bac4-name-id", "DD", true, false))
                             .then(() => e.equalTo("bac5-name-id", "EE", true, false))
                             .then(() => e.equalTo("bac5_bac4-name-id", "FF", true, false))
                             .then(() => TestUtil.returnRealSaveAs())
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.importsAutomaticCodes = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/bacteria_for_test_without_identifier.tsv";

        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(13);
            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => UserTests.importBacteriasFromFile(e, baseURL + pathToResource, true))
                             .then(() => e.waitForId("jSuccess"))
                             .then(() => e.sleep(3500)) // wait for saving
                             // check that bacterias was created
                             .then(() => e.waitForId("bac6-column-id"))
                             .then(() => e.waitForId("bac7-column-id"))
                             .then(() => e.waitForId("bac8-column-id"))
                             .then(() => e.waitForId("bac9-column-id"))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.importsGivenCodes = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/bacteria_for_test_with_identifier.tsv";

        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(14);
            var r = ReactTestUtils;
            Promise.resolve().then(() => UserTests.importBacteriasFromFile(e, baseURL + pathToResource, true))
                             .then(() => e.waitForId("jSuccess"))
                             .then(() => e.sleep(5000)) // wait for saving
                             // check that bacterias was created
                             .then(() => e.waitForId("sample-grid\\.next-page-id"))
                             .then(() => r.click("sample-grid\\.next-page-id"))
                             .then(() => e.waitForId("bac10-column-id"))
                             .then(() => e.waitForId("bac11-column-id"))
                             .then(() => e.waitForId("bac12-column-id"))
                             .then(() => e.waitForId("bac13-column-id"))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.importBacteriasFromFile = function(e, file, isNew) {
        return new Promise(function executor(resolve, reject) {
            var testChain = Promise.resolve();

            testChain.then(() => e.waitForId("sample-options-menu-btn"))
                     .then(() => e.click("sample-options-menu-btn"));

            if (isNew) {
                testChain.then(() => e.waitForId("register-object-btn"))
                         .then(() => e.click("register-object-btn"));
            } else {
                testChain.then(() => e.waitForId("update-object-btn"))
                         .then(() => e.click("update-object-btn"));
            }

            testChain.then(() => e.waitForId("choose-type-btn"))
                     .then(() => e.change("choose-type-btn", "BACTERIA", false))
                     .then(() => TestUtil.setFile("name", file, "text"))
                     .then(() => e.waitForId("accept-type-file"))
                     .then(() => e.click("accept-type-file"))
                     .then(() => resolve())
                     .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.storageTest = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(15);

            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac1-column-id"))
                             .then(() => e.click("bac1-column-id"))
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.click("edit-btn"))
                             // we wait for the save-button, cause page contains add-storage-btn
                             // even when page can't be edit. So we wait when page be reloaded.
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.waitForId("add-storage-btn"))
                             .then(() => e.click("add-storage-btn"))
                             .then(() => e.waitForId("storage-drop-down-id"))
                             .then(() => e.change("storage-drop-down-id", "DEFAULT_STORAGE", false))
                             .then(() => e.waitForId("storage-drop-down-id-1-2"))
                             .then(() => e.click("storage-drop-down-id-1-2"))
                             .then(() => e.waitForId("box-name-id"))
                             .then(() => e.write("box-name-id", "Test Box", false))
                             .then(() => e.waitForId("box-size-drop-down-id"))
                             .then(() => e.change("box-size-drop-down-id", "4X4", false))
                             .then(() => e.waitForId("storage-drop-down-id-C-2"))
                             .then(() => e.click("storage-drop-down-id-C-2"))
                             .then(() => e.click("storage-accept"))
                             .then(() => e.sleep(2000)) // wait for accept
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("jSuccess"))
                             .then(() => e.sleep(2000)) // wait for import
                             // check that new storage was created
                             .then(() => e.waitForId("testbox-c2-id"))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.movingBoxTest = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(16);

            Promise.resolve().then(() => e.waitForId("STORAGE_MANAGER"))
                             .then(() => e.click("STORAGE_MANAGER"))
                             .then(() => e.waitForId("storage-drop-down-id-a"))
                             .then(() => e.change("storage-drop-down-id-a", "DEFAULT_STORAGE", false))
                             .then(() => e.waitForId("toggle-storage-b-id"))
                             .then(() => e.click("toggle-storage-b-id"))
                             .then(() => e.waitForId("storage-drop-down-id-b"))
                             .then(() => e.change("storage-drop-down-id-b", "BENCH", false))
                             .then(() => e.waitForId("storage-drop-down-id-a-1-2-storage-box-0"))
                             .then(() => e.waitForId("storage-drop-down-id-b-1-1"))
                             .then(() => e.dragAndDrop("storage-drop-down-id-a-1-2-storage-box-0", "storage-drop-down-id-b-1-1", false))
                             .then(() => e.equalTo("change-log-container-id", "None", false, false))
                             .then(() => e.click("save-changes-btn"))
                             .then(() => e.waitForId("jSuccess"))
                             .then(() => e.sleep(5000)) // wait for saving
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.movingSampleTest = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(17);

            Promise.resolve().then(() => e.waitForId("STORAGE_MANAGER"))
                             .then(() => e.click("STORAGE_MANAGER"))
                             .then(() => e.waitForId("storage-drop-down-id-a"))
                             .then(() => e.change("storage-drop-down-id-a", "BENCH", false))
                             .then(() => e.waitForId("storage-drop-down-id-a-1-1"))
                             .then(() => e.waitForId("storage-drop-down-id-a-1-1-storage-box-0"))
                             .then(() => e.click("storage-drop-down-id-a-1-1-storage-box-0"))
                             .then(() => e.waitForId("storage-drop-down-id-a-C-2-storage-box-0"))
                             .then(() => e.dragAndDrop("storage-drop-down-id-a-C-2-storage-box-0", "storage-drop-down-id-a-A-3", false))
                             .then(() => e.equalTo("change-log-container-id", "None", false, false))
                             .then(() => e.click("save-changes-btn"))
                             .then(() => e.waitForId("jSuccess"))
                             .then(() => e.sleep(4000)) // wait for saving
                             // Open object BAC1 and verify storage.
                             .then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac1-column-id"))
                             .then(() => e.click("bac1-column-id"))
                             .then(() => e.waitForId("testbox-a3-id"))
                             .then(() => e.equalTo("testbox-a3-id", "Test Box - A3", true, false))
                             .then(() => e.sleep(3000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.createProtocol = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(18);

            Promise.resolve().then(() => e.waitForId("_METHODS_PROTOCOLS_GENERAL_PROTOCOLS"))
                             .then(() => e.click("_METHODS_PROTOCOLS_GENERAL_PROTOCOLS"))
                             .then(() => e.waitForId("create-btn"))
                             .then(() => e.click("create-btn"))
                             .then(() => e.waitForId("options-menu-btn-sample-view-general_protocol"))
                             .then(() => e.click("options-menu-btn-sample-view-general_protocol"))
                             .then(() => e.waitForId("options-menu-btn-identification-info"))
                             .then(() => e.click("options-menu-btn-identification-info"))
                             .then(() => e.waitForId("codeId"))
                             .then(() => e.waitForFill("codeId"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.createProject = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(19);

            Promise.resolve().then(() => e.waitForId("TESTID"))
                             .then(() => e.click("TESTID"))
                             .then(() => e.waitForId("create-btn"))
                             .then(() => e.click("create-btn"))
                             .then(() => e.waitForId("project-code-id"))
                             .then(() => e.write("project-code-id", "PROJECT_101", false))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.click("edit-btn"))
                             .then(() => e.waitForId("options-menu-btn-project-view"))
                             .then(() => e.click("options-menu-btn-project-view"))
                             .then(() => e.waitForId("options-menu-btn-description"))
                             .then(() => e.click("options-menu-btn-description"))
                             .then(() => e.waitForCkeditor("description-id"))
                             .then(() => TestUtil.ckeditorSetData("description-id", "Test Description 101"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.createExperiment = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(20);

            var yesterday = Util.getFormatedDate(new Date(new Date().setDate(new Date().getDate() - 1)));
            var tomorrow = Util.getFormatedDate(new Date(new Date().setDate(new Date().getDate() + 1)));

            Promise.resolve().then(() => e.waitForId("options-menu-btn"))
                             .then(() => e.click("options-menu-btn"))
                             // Create Default Experiment
                             .then(() => e.waitForId("default-experiment"))
                             .then(() => e.click("default-experiment"))
                             .then(() => e.waitForId("codeId"))
                             .then(() => e.waitForFill("codeId"))
                             // add Name
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", "Experiment 101", false))
                             // show in project overview checked
                             .then(() => e.waitForId("SHOW_IN_PROJECT_OVERVIEW"))
                             .then(() => e.checked("SHOW_IN_PROJECT_OVERVIEW", true))
                             .then(() => e.change("SHOW_IN_PROJECT_OVERVIEW", true))
                             // add first comment
                             .then(() => e.waitForId("add-comment-btn"))
                             .then(() => e.click("add-comment-btn"))
                             .then(() => e.waitForId("comment-0-box"))
                             .then(() => e.write("comment-0-box", "My first comment", false))
                             .then(() => e.waitForId("save-comment-0-btn"))
                             .then(() => e.click("save-comment-0-btn"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             // Update date and name for Experiment
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.click("edit-btn"))
                             .then(() => e.waitForId("save-btn"))
                             // edit name
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", "Experiment 101 Bis", false))
                             // set start date
                             .then(() => e.waitForId("START_DATE"))
                             .then(() => e.change("START_DATE", tomorrow, false))
                             // set end date
                             .then(() => e.waitForId("END_DATE"))
                             .then(() => e.change("END_DATE", yesterday, false))
                             // add second comment
                             .then(() => e.waitForId("add-comment-btn"))
                             .then(() => e.click("add-comment-btn"))
                             .then(() => e.waitForId("comment-0-box"))
                             .then(() => e.write("comment-0-box", "My second comment", false))
                             .then(() => e.waitForId("save-comment-0-btn"))
                             .then(() => e.click("save-comment-0-btn"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             //You should see the error
                             .then(() => e.waitForId("jNotifyDismiss"))
                             .then(() => e.click("jNotifyDismiss"))
                             // fix the error (remove end date) and save experiment
                             .then(() => e.change("END_DATE", "", false))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("jSuccess"))
                             .then(() => e.sleep(2000)) // wait for import
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.createExperimentStep = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(21);

            var tomorrow = Util.getFormatedDate(new Date(new Date().setDate(new Date().getDate() + 1)));

            Promise.resolve().then(() => e.waitForId("options-menu-btn"))
                             .then(() => e.click("options-menu-btn"))
                             // add Experimental Step
                             .then(() => e.waitForId("experimental-step"))
                             .then(() => e.click("experimental-step"))
                             .then(() => e.waitForId("options-menu-btn-sample-view-experimental_step"))
                             .then(() => e.click("options-menu-btn-sample-view-experimental_step"))
                             .then(() => e.waitForId("codeId"))
                             .then(() => e.click("codeId"))
                             // add name
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", "Step 101", false))
                             // show in project overview checked
                             .then(() => e.waitForId("SHOW_IN_PROJECT_OVERVIEW"))
                             .then(() => e.checked("SHOW_IN_PROJECT_OVERVIEW", true))
                             .then(() => e.change("SHOW_IN_PROJECT_OVERVIEW", true))
                             // set start date
                             .then(() => e.waitForId("START_DATE"))
                             .then(() => e.change("START_DATE", tomorrow, false))
                             // add protocol
                             .then(() => e.waitForId("search-btn-general-protocol"))
                             .then(() => e.click("search-btn-general-protocol"))
                             .then(() => e.searchForObjectInSelect2(e, "GEN", "add-object-general_protocol"))
                             .then(() => e.waitForId("gen10-column-id"))
                             // Operations
                             .then(() => e.waitForId("gen10-operations-column-id"))
                             .then(() => e.click("gen10-operations-column-id"))
                             .then(() => e.waitForId("gen10-operations-column-id-use-as-template"))
                             .then(() => e.click("gen10-operations-column-id-use-as-template"))
                             .then(() => e.waitForId("newSampleCodeForCopy"))
                             .then(() => e.write("newSampleCodeForCopy", "CODE1", false))
                             .then(() => e.waitForId("copyAccept"))
                             .then(() => e.click("copyAccept"))
                             // add first comment
                             .then(() => e.waitForId("add-comment-btn"))
                             .then(() => e.click("add-comment-btn"))
                             .then(() => e.waitForId("comment-0-box"))
                             .then(() => e.write("comment-0-box", "My first comment", false))
                             .then(() => e.waitForId("save-comment-0-btn"))
                             .then(() => e.click("save-comment-0-btn"))
                             .then(() => e.waitForId("code1-column-id"))
                             // save
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             // edit
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.click("edit-btn"))
                             .then(() => e.waitForId("save-btn"))
                             // edit name
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", "Step 101 Bis", false))
                             // save
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("jSuccess"))
                             .then(() => e.sleep(2000)) // wait for import
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.datasetUploader = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/test-image.png";

        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(23);

            Promise.resolve().then(() => e.waitForId("upload-btn"))
                             .then(() => e.click("upload-btn"))
                             // choose type
                             .then(() => e.waitForId("DATASET_TYPE"))
                             .then(() => e.changeSelect2("DATASET_TYPE", "ELN_PREVIEW", false))
                             // add first comment
                             .then(() => e.waitForId("add-comment-btn"))
                             .then(() => e.click("add-comment-btn"))
                             .then(() => e.waitForId("comment-0-box"))
                             .then(() => e.write("comment-0-box", "My first comment", false))
                             .then(() => e.waitForId("save-comment-0-btn"))
                             .then(() => e.click("save-comment-0-btn"))
                             // upload image
                             .then(() => e.dropFile("test-image.png", baseURL + pathToResource, "filedrop", false))
                             .then(() => e.waitForClass("progressbar.ready"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("jSuccess"))
                             .then(() => e.sleep(2000)) // wait for import
                             // open data set and edit it
                             .then(() => e.waitForId("dataSetPosInTree-0"))
                             .then(() => e.click("dataSetPosInTree-0"))
                             .then(() => e.waitForId("dataset-edit-btn"))
                             .then(() => e.click("dataset-edit-btn"))
                             .then(() => e.waitForId("save-btn"))
                             // change Name
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", "New Name", false))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("jSuccess"))
                             .then(() => e.sleep(2000)) // wait for import
                             .then(() => e.waitForId("dataset-edit-btn"))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.showInProjectOverview = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(25);

            Promise.resolve().then(() => e.waitForId("PATH_TESTID_PROJECT_101"))
                             .then(() => e.click("PATH_TESTID_PROJECT_101"))
                             // click "Show Experiments"
                             .then(() => e.waitForId("options-menu-btn-project-view"))
                             .then(() => e.waitForId("project-experiments"))
                             .then(() => e.waitForStyle("project-experiments", "display", "none", false))
                             .then(() => e.click("options-menu-btn-project-view"))
                             .then(() => e.waitForId("options-menu-btn-experiments"))
                             .then(() => e.click("options-menu-btn-experiments"))
                             .then(() => e.waitForId("project-experiments"))
                             .then(() => e.waitForStyle("project-experiments", "display", "", false))
                             // click "Show Objects"
                             .then(() => e.waitForId("options-menu-btn-project-view"))
                             .then(() => e.waitForId("project-samples"))
                             .then(() => e.waitForStyle("project-samples", "display", "none", false))
                             .then(() => e.click("options-menu-btn-project-view"))
                             .then(() => e.waitForId("options-menu-btn-objects"))
                             .then(() => e.click("options-menu-btn-objects"))
                             .then(() => e.waitForId("project-samples"))
                             .then(() => e.waitForStyle("project-samples", "display", "", false))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.search = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(26);

            Promise.resolve().then(() => e.waitForId("search"))
                             // start global search
                             .then(() => e.click("search"))
                             .then(() => e.change("search", "BAC5", false))
                             .then(() => e.keypress("search", 13, false))
                             .then(() => e.waitForId("save-btn"))
                             // check searching results
                             .then(() => e.waitForId("bac5-id"))
                             .then(() => e.waitForId("bac5_bac4-id"))
                             // save query
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("Name"))
                             .then(() => e.write("Name", "Search for BAC5", false))
                             .then(() => e.waitForId("search-query-save-btn"))
                             .then(() => e.click("search-query-save-btn"))
                             .then(() => e.waitForId("jSuccess"))
                             .then(() => e.sleep(2000)) // wait for import
                             // Click on BAC5
                             .then(() => e.waitForId("bac5-id"))
                             .then(() => e.click("bac5-id"))
                             .then(() => e.waitForId("edit-btn"))
                             // Click on Advanced Search
                             .then(() => e.waitForId("ADVANCED_SEARCH"))
                             .then(() => e.click("ADVANCED_SEARCH"))
                             .then(() => e.waitForId("saved-search-dropdown-id"))
                             .then(() => e.triggerSelectSelect2("saved-search-dropdown-id", 0, false))
                             .then(() => e.waitForId("search-btn"))
                             .then(() => e.click("search-btn"))
                             // check search results
                             .then(() => e.waitForId("bac5-id"))
                             .then(() => e.waitForId("bac5_bac4-id"))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.supplierForm = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(27);

            Promise.resolve().then(() => e.waitForId("STOCK_CATALOG"))
                             // path to Supplier Collection
                             .then(() => e.click("STOCK_CATALOG"))
                             .then(() => e.waitForId("SUPPLIERS"))
                             .then(() => e.click("SUPPLIERS"))
                             //create English supplier
                             .then(() => UserTests.createSupplier(e, "EN", "ENGLISH", "companyen@email.com"))
                             //create German supplier
                             .then(() => UserTests.createSupplier(e, "DE", "GERMAN", "companyde@email.com"))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.createSupplier = function(e, langCode, language, email) {
        return new Promise(function executor(resolve, reject) {
            Promise.resolve().then(() => e.waitForId("_STOCK_CATALOG_SUPPLIERS_SUPPLIER_COLLECTION"))
                             .then(() => e.click("_STOCK_CATALOG_SUPPLIERS_SUPPLIER_COLLECTION"))
                             .then(() => e.waitForId("create-btn"))
                             .then(() => e.click("create-btn"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.waitForFill("codeId"))
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", "Company " + langCode + " Name"))
                             .then(() => e.waitForId("SUPPLIERCOMPANY_ADDRESS_LINE_1"))
                             .then(() => e.change("SUPPLIERCOMPANY_ADDRESS_LINE_1", "Company " + langCode + " Address"))
                             .then(() => e.waitForId("SUPPLIERCOMPANY_EMAIL"))
                             .then(() => e.change("SUPPLIERCOMPANY_EMAIL", email))
                             .then(() => e.waitForId("SUPPLIERCOMPANY_LANGUAGE"))
                             .then(() => e.changeSelect2("SUPPLIERCOMPANY_LANGUAGE", language))
                             .then(() => e.waitForId("SUPPLIERCUSTOMER_NUMBER"))
                             .then(() => e.change("SUPPLIERCUSTOMER_NUMBER", langCode + "001"))
                             .then(() => e.waitForId("SUPPLIERPREFERRED_ORDER_METHOD"))
                             .then(() => e.changeSelect2("SUPPLIERPREFERRED_ORDER_METHOD", "MANUAL"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("edit-btn")) // wait for saving
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.productForm = function() {
        return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor(28);

            Promise.resolve().then(() => e.waitForId("STOCK_CATALOG"))
                             // path to Product Collection
                             .then(() => e.click("STOCK_CATALOG"))
                             .then(() => e.waitForId("PRODUCTS"))
                             .then(() => e.click("PRODUCTS"))
                             //create English product form
                             .then(() => UserTests.createProductForm(e, "EN", "EUR"))
                             //create German product form
                             .then(() => UserTests.createProductForm(e, "DE", "EUR"))
                             .then(() => e.sleep(1000))
                             .then(() => TestUtil.testPassed(e))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

    this.createProductForm = function(e, langCode, currency) {
        return new Promise(function executor(resolve, reject) {
            Promise.resolve().then(() => e.waitForId("_STOCK_CATALOG_PRODUCTS_PRODUCT_COLLECTION"))
                             .then(() => e.click("_STOCK_CATALOG_PRODUCTS_PRODUCT_COLLECTION"))
                             .then(() => e.waitForId("create-btn"))
                             .then(() => e.click("create-btn"))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.waitForFill("codeId"))
                             .then(() => e.waitForId("NAME"))
                             .then(() => e.change("NAME", "Product " + langCode + " Name"))
                             .then(() => e.waitForId("PRODUCTCATALOG_NUM"))
                             .then(() => e.change("PRODUCTCATALOG_NUM", "CC " + langCode))
                             .then(() => e.waitForId("PRODUCTPRICE_PER_UNIT"))
                             .then(() => e.change("PRODUCTPRICE_PER_UNIT", 2))
                             .then(() => e.waitForId("PRODUCTCURRENCY"))
                             .then(() => e.changeSelect2("PRODUCTCURRENCY", currency))
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             // Error: Currently only have 0 of the 1 required SUPPLIER.
                             .then(() => e.waitForId("jNotifyDismiss"))
                             .then(() => e.click("jNotifyDismiss"))
                             .then(() => e.waitForId("search-btn-suppliers"))
                             .then(() => e.click("search-btn-suppliers"))
                             .then(() => e.searchForObjectInSelect2(e, langCode, "add-object-supplier"))
                             .then(() => e.waitFor("a[id$=column-id]"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("edit-btn")) // wait for saving
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(e, error, reject));
        });
    }

     this.requestForm = function() {
         return new Promise(function executor(resolve, reject) {
             var e = new EventExecutor(29);

             Promise.resolve().then(() => e.waitForId("STOCK_CATALOG"))
                              // path to Request Collection
                              .then(() => e.click("STOCK_CATALOG"))
                              .then(() => e.waitForId("REQUESTS"))
                              .then(() => e.click("REQUESTS"))
                              // create Request with Products from Catalog
                              .then(() => e.waitForId("_STOCK_CATALOG_REQUESTS_REQUEST_COLLECTION"))
                              .then(() => e.click("_STOCK_CATALOG_REQUESTS_REQUEST_COLLECTION"))
                              .then(() => e.waitForId("create-btn"))
                              .then(() => e.click("create-btn"))
                              // set request name and status
                              .then(() => e.waitForId("NAME"))
                              .then(() => e.change("NAME", "Product EN 2 Name"))
                              .then(() => e.waitForId("ORDERINGORDER_STATUS"))
                              .then(() => e.changeSelect2("ORDERINGORDER_STATUS", "NOT_YET_ORDERED"))
                              // add Products from Catalog
                              .then(() => e.waitForId("search-btn-products"))
                              .then(() => e.click("search-btn-products"))
                              .then(() => e.searchForObjectInSelect2(e, "Product EN", "add-object-product"))
                              .then(() => e.waitFor("a[id$=column-id]"))
                              .then(() => e.waitFor("input[id^=quantity-of-items-pro]"))
                              .then(() => e.changeStartsWith("quantity-of-items-pro", "18"))
                              .then(() => e.waitForId("save-btn"))
                              .then(() => e.click("save-btn"))
                              .then(() => e.waitForId("jSuccess"))
                              .then(() => e.sleep(2000)) // wait for import
                              .then(() => e.waitForId("edit-btn")) // wait for saving
                              // create Request with new Product
                              .then(() => e.waitForId("_STOCK_CATALOG_REQUESTS_REQUEST_COLLECTION"))
                              .then(() => e.click("_STOCK_CATALOG_REQUESTS_REQUEST_COLLECTION"))
                              .then(() => e.waitForId("create-btn"))
                              .then(() => e.click("create-btn"))
                              // set request name and status
                              .then(() => e.waitForId("NAME"))
                              .then(() => e.change("NAME", "Product DE 2 Name"))
                              .then(() => e.waitForId("ORDERINGORDER_STATUS"))
                              .then(() => e.changeSelect2("ORDERINGORDER_STATUS", "NOT_YET_ORDERED"))
                              .then(() => e.waitForId("add-new-product-btn"))
                              .then(() => e.click("add-new-product-btn"))
                              // fill new product
                              .then(() => e.waitForId("new-product-name-1"))
                              .then(() => e.change("new-product-name-1", "Product DE 2 Name"))
                              .then(() => e.waitForId("new-product-currency-1"))
                              .then(() => e.changeSelect2("new-product-currency-1", "CHF"))
                              .then(() => e.waitForId("new-product-supplier-1"))
                              .then(() => e.searchSelect2("new-product-supplier-1", "DE"))
                              .then(() => e.sleep(2000))
                              .then(() => e.mouseUp("select2-results__option"))
                              .then(() => e.sleep(1000))
                              .then(() => e.waitForId("new-product-quantity-1"))
                              .then(() => e.change("new-product-quantity-1", "18"))
                              .then(() => e.waitForId("save-btn"))
                              .then(() => e.click("save-btn"))
                              .then(() => e.waitForId("jSuccess"))
                              .then(() => e.sleep(2000)) // wait for import
                              .then(() => e.waitForId("edit-btn")) // wait for saving
                              .then(() => e.sleep(1000))
                              .then(() => TestUtil.testPassed(e))
                              .then(() => resolve())
                              .catch(error => TestUtil.reportError(e, error, reject));
         });
     }

     this.orderForm = function() {
         return new Promise(function executor(resolve, reject) {
             var e = new EventExecutor(30);

             Promise.resolve().then(() => e.waitForId("STOCK_ORDERS"))
                              // path to Order Collection
                              .then(() => e.click("STOCK_ORDERS"))
                              .then(() => e.waitForId("ORDERS"))
                              .then(() => e.click("ORDERS"))
                              .then(() => e.waitForId("_STOCK_ORDERS_ORDERS_ORDER_COLLECTION"))
                              .then(() => e.click("_STOCK_ORDERS_ORDERS_ORDER_COLLECTION"))
                              // wait page reload
                              .then(() => e.waitForId("sample-options-menu-btn"))
                              // There should be no + button
                              .then(() => e.verifyExistence("create-btn", false))
                              .then(() => e.sleep(1000))
                              .then(() => TestUtil.testPassed(e))
                              .then(() => resolve())
                              .catch(error => TestUtil.reportError(e, error, reject));
         });
      }

     this.logout = function() {
         return new Promise(function executor(resolve, reject) {
            var e = new EventExecutor();

            Promise.resolve().then(() => TestUtil.setCookies("suitename", "finishTest"))
                             .then(() => e.click("logoutBtn"))
                             .then(() => resolve())
                             .catch(error => reject(error));
         });
     }
}