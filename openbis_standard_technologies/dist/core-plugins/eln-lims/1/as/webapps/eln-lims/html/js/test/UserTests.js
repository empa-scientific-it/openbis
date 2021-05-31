var UserTests = new function() {

    this.inventorySpaceForTestUser = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

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
                       "_METHODS_PROTOCOLS_WESTERN_BLOTTING_PROTOCOLS"];

            Promise.resolve().then(() => TestUtil.verifyInventory(5, ids))
                             .then(() => e.verifyExistence("USER_MANAGER", false))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(5, error, reject));
        });
    }

    this.creationSampleForm = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => UserTests.createBacteria("BAC1", "Aurantimonas"))
                             .then(() => UserTests.createBacteria("BAC2", "Burantimonas"))
                             .then(() => UserTests.createBacteria("BAC3", "Curantimonas"))
                             .then(() => UserTests.createBacteria("BAC4", "Durantimonas"))
                             .then(() => TestUtil.testPassed(6))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(6, error, reject));
        });
    }

    this.createBacteria = function(code, name) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
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
                     .catch(error => reject(error));
        });
    }

    this.editSampleForm = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/test-image.png";

        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

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
                             .then(() => e.waitForId("plus-btn-bacteria-parents"))
                             .then(() => e.click("plus-btn-bacteria-parents"))
                             .then(() => e.waitForId("bac1-column-id"))
                             .then(() => e.click("bac1-column-id"))
                             .then(() => e.waitForId("comments-bac1"))
                             .then(() => e.change("comments-bac1", "mother"))
                             // add father
                             .then(() => e.click("plus-btn-bacteria-parents"))
                             .then(() => e.waitForId("bac2-column-id"))
                             .then(() => e.click("bac2-column-id"))
                             .then(() => e.waitForId("comments-bac2"))
                             .then(() => e.change("comments-bac2", "father"))
                             // add Child
                             .then(() => e.waitForId("plus-btn-children-type-selector"))
                             .then(() => e.click("plus-btn-children-type-selector"))
                             .then(() => e.waitForId("sampleTypeSelector"))
                             .then(() => e.changeSelect2("sampleTypeSelector", "Bacteria"))
                             .then(() => e.waitForId("bac4-column-id"))
                             .then(() => e.click("bac4-column-id"))
                             .then(() => e.waitForId("bac4-operations-column-id"))
                             // save
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("edit-btn"))
                             // check parents and children
                             .then(() => e.waitForId("bac1-column-id"))
                             .then(() => e.waitForId("bac2-column-id"))
                             .then(() => e.waitForId("bac4-column-id"))
                             .then(() => TestUtil.testPassed(7))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(7, error, reject));
        });
    }

    this.sampleHierarchyAsGraph = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
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
                             .then(() => TestUtil.testPassed(8))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(8, error, reject));
        });
    }

    this.sampleHierarchyAsTable = function() {
        var parentAnnotations = "<b>Code</b>: BAC1, <b>Comments</b>: mother<br><br><b>Code</b>: BAC2, <b>Comments</b>: father";
        var childrenAnnotations = "<b>Code</b>: BAC4";

        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            // return to bacteria 3
            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac3-column-id"))
                             .then(() => e.click("bac3-column-id"))
                             // show Hierarchy Graph
                             .then(() => e.waitForId("options-menu-btn-sample-view-bacteria"))
                             .then(() => e.click("options-menu-btn-sample-view-bacteria"))
                             .then(() => e.waitForId("hierarchy-table"))
                             .then(() => e.click("hierarchy-table"))
                             .then(() => e.sleep(2000)) // wait for table
                             // show Identifier
                             .then(() => e.waitForId("columns-dropdown-id"))
                             .then(() => e.click("columns-dropdown-id"))
                             .then(() => e.waitForId("identifier-cln"))
                             .then(() => e.click("identifier-cln"))
                             .then(() => e.click("columns-dropdown-id"))
                             // check parents and children
                             .then(() => e.waitForId("bac1"))
                             .then(() => e.waitForId("bac2"))
                             .then(() => e.waitForId("bac3"))
                             .then(() => e.equalTo("children-annotations-bac3", childrenAnnotations, true, false))
                             .then(() => e.waitForId("bac4"))
                             // show the Parent/Annotations column
                             .then(() => e.waitForId("columns-dropdown-id"))
                             .then(() => e.click("columns-dropdown-id"))
                             .then(() => e.waitForId("parentannotations-cln"))
                             .then(() => e.click("parentannotations-cln"))
                             .then(() => e.click("columns-dropdown-id"))
                             // check parents comments
                             .then(() => e.waitForId("parent-annotations-bac3"))
                             .then(() => e.equalTo("parent-annotations-bac3", parentAnnotations, true, false))
                             .then(() => TestUtil.testPassed(9))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(9, error, reject));
        });
    }

    this.copySampleForm = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

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
                             // link parents
                             .then(() => e.waitForId("linkParentsOnCopy"))
                             .then(() => e.checked("linkParentsOnCopy", true))
                             .then(() => e.waitForId("copyChildrenToParent"))
                             .then(() => e.checked("copyChildrenToParent", true))
                             .then(() => e.waitForId("copyAccept"))
                             .then(() => e.click("copyAccept"))
                             .then(() => e.sleep(3500)) // wait when copy will finished
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
                             .then(() => TestUtil.testPassed(10))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(10, error, reject));
        });
    }

    this.deleteSampleForm = function() {

        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             // navigation to BAC5
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac5-column-id"))
                             .then(() => e.click("bac5-column-id"))
                             // delete
                             .then(() => e.waitForId("options-menu-btn-sample-view-bacteria"))
                             .then(() => e.click("options-menu-btn-sample-view-bacteria"))
                             .then(() => e.waitForId("delete"))
                             .then(() => e.click("delete"))
                             // fill Confirm form
                             .then(() => e.waitForId("reason-to-delete-id"))
                             .then(() => e.write("reason-to-delete-id", "test"))
                             .then(() => e.waitForId("accept-btn"))
                             .then(() => e.click("accept-btn"))
                             //You should see the error
                             .then(() => e.waitForId("jNotifyDismiss"))
                             .then(() => e.click("jNotifyDismiss"))
                             .then(() => TestUtil.testPassed(11))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(11, error, reject));
        });
    }

    this.exportsImportsUpdate = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToCheckResource = "js/test/resources/exportedTableAllColumnsAllRows.tsv";
        var pathToUpdateResource = "js/test/resources/updateAllColumnsAllRows.tsv";

        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
            Promise.resolve().then(() => TestUtil.overloadSaveAs())
                             .then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             // export all columns with all rows
                             .then(() => e.waitForId("export-btn-id"))
                             .then(() => e.click("export-btn-id"))
                             .then(() => e.waitForId("export-all-columns-and-rows"))
                             .then(() => e.click("export-all-columns-and-rows"))
                             .then(() => e.sleep(3000)) // wait for download
                             .then(() => TestUtil.checkFileEquality("exportedTableAllColumnsAllRows.tsv", baseURL + pathToCheckResource, TestUtil.idReplacer))
                             .then(() => e.equalTo("bac1-column-id", "Aurantimonas", true, false))
                             .then(() => e.equalTo("bac2-column-id", "Burantimonas", true, false))
                             .then(() => e.equalTo("bac3-column-id", "Curantimonas", true, false))
                             .then(() => e.equalTo("bac4-column-id", "Durantimonas", true, false))
                             .then(() => e.equalTo("bac5-column-id", "Curantimonas", true, false))
                             .then(() => e.equalTo("bac5_bac4-column-id", "Durantimonas", true, false))
                             // Batch Update Objects
                             .then(() => UserTests.importBacteriasFromFile(baseURL + pathToUpdateResource, false))
                             .then(() => e.sleep(3500)) // wait for import
                             // check names after update
                             .then(() => e.equalTo("bac1-column-id", "AA", true, false))
                             .then(() => e.equalTo("bac2-column-id", "BB", true, false))
                             .then(() => e.equalTo("bac3-column-id", "CC", true, false))
                             .then(() => e.equalTo("bac4-column-id", "DD", true, false))
                             .then(() => e.equalTo("bac5-column-id", "EE", true, false))
                             .then(() => e.equalTo("bac5_bac4-column-id", "FF", true, false))
                             .then(() => TestUtil.returnRealSaveAs())
                             .then(() => TestUtil.testPassed(12))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(12, error, reject));
        });
    }

    this.importsAutomaticCodes = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/bacteria_for_test_without_identifier.tsv";

        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => UserTests.importBacteriasFromFile(baseURL + pathToResource, true))
                             .then(() => e.sleep(3500)) // wait for saving
                             // check that bacterias was created
                             .then(() => e.waitForId("bac6-column-id"))
                             .then(() => e.waitForId("bac7-column-id"))
                             .then(() => e.waitForId("bac8-column-id"))
                             .then(() => e.waitForId("bac9-column-id"))
                             .then(() => TestUtil.testPassed(13))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(13, error, reject));
        });
    }

    this.importsGivenCodes = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/bacteria_for_test_with_identifier.tsv";

        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;
            Promise.resolve().then(() => UserTests.importBacteriasFromFile(baseURL + pathToResource, true))
                             .then(() => e.sleep(3500)) // wait for saving
                             // check that bacterias was created
                             .then(() => e.waitForId("next-page-id"))
                             .then(() => e.click("next-page-id"))
                             .then(() => e.waitForId("bac10-column-id"))
                             .then(() => e.waitForId("bac11-column-id"))
                             .then(() => e.waitForId("bac12-column-id"))
                             .then(() => e.waitForId("bac13-column-id"))
                             .then(() => TestUtil.testPassed(14))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(14, error, reject));
        });
    }

    this.importBacteriasFromFile = function(file, isNew) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

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
                     .catch(error => reject(error));
        });
    }

    this.storageTest = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac1-column-id"))
                             .then(() => e.click("bac1-column-id"))
                             .then(() => e.waitForId("edit-btn"))
                             .then(() => e.click("edit-btn"))
                             // we wait for the save-button, cause page contains add-storage-btn
                             // even when page can't be edit. So we wait when page be reloaded.
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.sleep(2000)) // wait for saving
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
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             // check that new storage was created
                             .then(() => e.waitForId("testbox-c2-id"))
                             .then(() => TestUtil.testPassed(15))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(15, error, reject));
        });
    }

    this.movingBoxTest = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("STORAGE_MANAGER"))
                             .then(() => e.click("STORAGE_MANAGER"))
                             .then(() => e.waitForId("storage-drop-down-id-a"))
                             .then(() => e.change("storage-drop-down-id-a", "DEFAULT_STORAGE", false))
                             .then(() => e.waitForId("toggle-storage-b-id"))
                             .then(() => e.click("toggle-storage-b-id"))
                             .then(() => e.waitForId("storage-drop-down-id-b"))
                             .then(() => e.change("storage-drop-down-id-b", "BENCH", false))
                             .then(() => e.waitForId("storage-drop-down-id-a-1-2-storage-box"))
                             .then(() => e.waitForId("storage-drop-down-id-b-1-1"))
                             .then(() => e.dragAndDrop("storage-drop-down-id-a-1-2-storage-box", "storage-drop-down-id-b-1-1", false))
                             .then(() => e.equalTo("change-log-container-id", "None", false, false))
                             .then(() => e.click("save-changes-btn"))
                             .then(() => e.sleep(3000)) // wait for saving
                             .then(() => TestUtil.testPassed(16))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(16, error, reject));
        });
    }

    this.movingSampleTest = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("STORAGE_MANAGER"))
                             .then(() => e.click("STORAGE_MANAGER"))
                             .then(() => e.waitForId("storage-drop-down-id-a"))
                             .then(() => e.change("storage-drop-down-id-a", "BENCH", false))
                             .then(() => e.waitForId("storage-drop-down-id-a-1-1"))
                             .then(() => e.waitForId("storage-drop-down-id-a-1-1-storage-box"))
                             .then(() => e.click("storage-drop-down-id-a-1-1-storage-box"))
                             .then(() => e.waitForId("storage-drop-down-id-a-C-2-storage-box"))
                             .then(() => e.dragAndDrop("storage-drop-down-id-a-C-2-storage-box", "storage-drop-down-id-a-A-3", false))
                             .then(() => e.equalTo("change-log-container-id", "None", false, false))
                             .then(() => e.click("save-changes-btn"))
                             .then(() => e.sleep(3000)) // wait for saving
                             // Open object BAC1 and verify storage.
                             .then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                             .then(() => e.waitForId("bac1-column-id"))
                             .then(() => e.click("bac1-column-id"))
                             .then(() => e.waitForId("testbox-a3-id"))
                             .then(() => e.equalTo("testbox-a3-id", "Test Box - A3", true, false))
                             .then(() => TestUtil.testPassed(17))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(17, error, reject));
        });
    }

    this.createProtocol = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

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
                             .then(() => TestUtil.testPassed(18))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(18, error, reject));
        });
    }

    this.createProject = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

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
                             .then(() => TestUtil.testPassed(19))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(19, error, reject));
        });
    }

    this.createExperiment = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

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
                             .then(() => TestUtil.testPassed(20))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(20, error, reject));
        });
    }

    this.createExperimentStep = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

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
                             .then(() => e.waitForId("plus-btn-general-protocol"))
                             .then(() => e.click("plus-btn-general-protocol"))
                             .then(() => e.waitForId("gen1-column-id"))
                             .then(() => e.click("gen1-column-id"))
                             // Operations
                             .then(() => e.waitForId("gen1-operations-column-id"))
                             .then(() => e.click("gen1-operations-column-id"))
                             .then(() => e.waitForId("gen1-operations-column-id-use-as-template"))
                             .then(() => e.click("gen1-operations-column-id-use-as-template"))
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
                             .then(() => TestUtil.testPassed(21))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(21, error, reject));
        });
    }

    this.datasetUploader = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/test-image.png";

        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

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
                             .then(() => e.waitForId("dataset-edit-btn"))
                             .then(() => TestUtil.testPassed(23))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(23, error, reject));
        });
    }

    this.showInProjectOverview = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

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
                             .then(() => TestUtil.testPassed(25))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(25, error, reject));
        });
    }

    this.search = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("search"))
                             // start global search
                             .then(() => e.click("search"))
                             .then(() => e.change("search", "BAC5", false))
                             .then(() => e.keypress("search", 13, false))
                             .then(() => e.waitForId("save-btn"))
                             // check searching results
                             .then(() => e.waitForId("columns-dropdown-id"))
                             .then(() => e.click("columns-dropdown-id"))
                             .then(() => e.waitForId("code-cln"))
                             .then(() => e.click("code-cln"))
                             .then(() => e.click("columns-dropdown-id"))
                             .then(() => e.waitForId("bac5-id"))
                             .then(() => e.waitForId("bac5_bac4-id"))
                             // save query
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("Name"))
                             .then(() => e.write("Name", "Search for BAC5", false))
                             .then(() => e.waitForId("search-query-save-btn"))
                             .then(() => e.click("search-query-save-btn"))
                             .then(() => e.sleep(3000)) // wait for saving
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
                             .then(() => TestUtil.testPassed(26))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(26, error, reject));
        });
    }

    this.supplierForm = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("STOCK_CATALOG"))
                             // path to Supplier Collection
                             .then(() => e.click("STOCK_CATALOG"))
                             .then(() => e.waitForId("SUPPLIERS"))
                             .then(() => e.click("SUPPLIERS"))
                             //create English supplier
                             .then(() => UserTests.createSupplier("EN", "ENGLISH", "companyen@email.com"))
                             //create German supplier
                             .then(() => UserTests.createSupplier("DE", "GERMAN", "companyde@email.com"))
                             .then(() => TestUtil.testPassed(27))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(27, error, reject));
        });
    }

    this.createSupplier = function(langCode, language, email) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

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
                             .catch(error => reject(error));
        });
    }

    this.productForm = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => e.waitForId("STOCK_CATALOG"))
                             // path to Product Collection
                             .then(() => e.click("STOCK_CATALOG"))
                             .then(() => e.waitForId("PRODUCTS"))
                             .then(() => e.click("PRODUCTS"))
                             //create English product form
                             .then(() => UserTests.createProductForm("EN", "EUR", "sup1-column-id"))
                             //create German product form
                             .then(() => UserTests.createProductForm("DE", "EUR", "sup2-column-id"))
                             .then(() => TestUtil.testPassed(28))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(28, error, reject));
        });
    }

    this.createProductForm = function(langCode, currency, supId) {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

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
                             .then(() => e.waitForId("plus-btn-suppliers"))
                             .then(() => e.click("plus-btn-suppliers"))
                             .then(() => e.waitForId(supId))
                             .then(() => e.click(supId))
                             .then(() => e.sleep(2000)) // wait for form's update
                             .then(() => e.waitForId("save-btn"))
                             .then(() => e.click("save-btn"))
                             .then(() => e.waitForId("edit-btn")) // wait for saving
                             .then(() => resolve())
                             .catch(error => reject(error));
        });
    }

     this.requestForm = function() {
         return new Promise(function executor(resolve, reject) {
             var e = EventUtil;

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
                              .then(() => e.waitForId("ORDERINGORDER_STATUS"))
                              .then(() => e.changeSelect2("ORDERINGORDER_STATUS", "NOT_YET_ORDERED"))
                              // add Products from Catalog
                              .then(() => e.waitForId("plus-btn-products"))
                              .then(() => e.click("plus-btn-products"))
                              .then(() => e.waitForId("pro1-column-id"))
                              .then(() => e.click("pro1-column-id"))
                              .then(() => e.waitForId("quantity-of-items-pro1"))
                              .then(() => e.change("quantity-of-items-pro1", "18"))
                              .then(() => e.waitForId("save-btn"))
                              .then(() => e.click("save-btn"))
                              .then(() => e.waitForId("edit-btn")) // wait for saving
                              // create Request with new Product
                              .then(() => e.waitForId("_STOCK_CATALOG_REQUESTS_REQUEST_COLLECTION"))
                              .then(() => e.click("_STOCK_CATALOG_REQUESTS_REQUEST_COLLECTION"))
                              .then(() => e.waitForId("create-btn"))
                              .then(() => e.click("create-btn"))
                              .then(() => e.waitForId("ORDERINGORDER_STATUS"))
                              .then(() => e.changeSelect2("ORDERINGORDER_STATUS", "NOT_YET_ORDERED"))
                              .then(() => e.waitForId("add-new-product-btn"))
                              .then(() => e.click("add-new-product-btn"))
                              // fill new product
                              .then(() => e.waitForId("new-product-name-1"))
                              .then(() => e.change("new-product-name-1", "Product EN 2 Name"))
                              .then(() => e.waitForId("new-product-currency-1"))
                              .then(() => e.changeSelect2("new-product-currency-1", "CHF"))
                              .then(() => e.waitForId("new-product-supplier-1"))
                              .then(() => e.changeSelect2("new-product-supplier-1", "/STOCK_CATALOG/SUPPLIERS/SUP1"))
                              .then(() => e.waitForId("new-product-quantity-1"))
                              .then(() => e.change("new-product-quantity-1", "18"))
                              .then(() => e.waitForId("save-btn"))
                              .then(() => e.click("save-btn"))
                              .then(() => e.waitForId("edit-btn")) // wait for saving
                              .then(() => TestUtil.testPassed(29))
                              .then(() => resolve())
                              .catch(error => TestUtil.reportError(29, error, reject));
         });
     }

     this.orderForm = function() {
         return new Promise(function executor(resolve, reject) {
             var e = EventUtil;

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
                              .then(() => TestUtil.testPassed(30))
                              .then(() => resolve())
                              .catch(error => TestUtil.reportError(30, error, reject));
         });
      }

     this.logout = function() {
         return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            Promise.resolve().then(() => TestUtil.setCookies("suitename", "finishTest"))
                             .then(() => e.click("logoutBtn"))
                             .then(() => resolve())
                             .catch(error => reject(error));
         });
     }
}