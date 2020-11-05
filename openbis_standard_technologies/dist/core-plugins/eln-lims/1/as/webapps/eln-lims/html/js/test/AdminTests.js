var AdminTests = new function() {

    this.login = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            testChain = Promise.resolve();

            testChain.then(() => TestUtil.login("admin", "admin"))
                     .then(() => TestUtil.testPassed(1))
                     .then(() => resolve())
                     .catch(error => TestUtil.reportError(1, error, reject));
        });
    }

	this.inventorySpace = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            var ids = ["tree",
                       "LAB_NOTEBOOK",
                       "INVENTORY",
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
                       "_PUBLICATIONS_PUBLIC_REPOSITORIES_PUBLICATIONS_COLLECTION",
                       "STOCK",
                       "USER_PROFILE",
                       "SAMPLE_BROWSER",
                       "VOCABULARY_BROWSER",
                       "ADVANCED_SEARCH",
                       "STORAGE_MANAGER",
                       "USER_MANAGER",
                       "TRASHCAN",
                       "SETTINGS"];

            Promise.resolve().then(() => TestUtil.verifyInventory(2, ids))
                             .then(() => TestUtil.testPassed(2))
                             .then(() => resolve())
                             .catch(error => TestUtil.reportError(2, error, reject));
        });
    }

    this.enableBacteriaToShowInDropDowns = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            testChain = Promise.resolve();
            testChain.then(() => e.waitForId("SETTINGS"))
                     .then(() => e.click("SETTINGS"))
                     .then(() => e.waitForId("settingsDropdown"))
                     .then(() => e.change("settingsDropdown", "/ELN_SETTINGS/GENERAL_ELN_SETTINGS"))
                     .then(() => e.waitForId("edit-btn"))
                     .then(() => e.click("edit-btn"))
                     // we wait for the save-button, cause page contains settings-section-sample type-BACTERIA
                     // even when page can't be edit. So we wait when page be reloaded.
                     .then(() => e.waitForId("save-btn"))
                     .then(() => e.click("settings-section-sampletype-BACTERIA"))
                     .then(() => e.waitForId("BACTERIA_show_in_drop_downs"))
                     .then(() => e.checked("BACTERIA_show_in_drop_downs", true))
                     .then(() => e.click("save-btn"))
                     // wait until the save
                     .then(() => e.waitForId("edit-btn"))
                     .then(() => TestUtil.testPassed(3))
                     .then(() => resolve())
                     .catch(error => TestUtil.reportError(3, error, reject));
        });
    }

    this.userManager = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            testChain = Promise.resolve();
            testChain.then(() => e.waitForId("USER_MANAGER"))
                     .then(() => e.click("USER_MANAGER"))
                     .then(() => e.waitForId("createUser"))
                     .then(() => e.click("createUser"))
                     .then(() => e.waitForId("userId"))
                     .then(() => e.change("userId", "testId"))
                     .then(() => e.click("createUserBtn"))
                     .then(() => AdminTests.createPassword())
                     .then(() => AdminTests.userExist())
                     .then(() => TestUtil.setCookies("suitename", "testId"))
                     .then(() => e.click("logoutBtn"))
                     .then(() => resolve())
                     .catch((error) => reject(error));
        });
    }

    // Sometimes it ask for password. Try to fill it.
    this.createPassword = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            testChain = Promise.resolve();

            testChain.then(() => e.waitForId("passwordId", true, 2000))
                     .then(() => e.change("passwordId", "pass", true))
                     .then(() => e.change("passwordRepeatId", "pass", true))
                     .then(() => e.click("createUserBtn", true))
                     .then(() => resolve())
                     .catch((error) => reject(error));
        });
    }

    // If the user already exists, we will see an error.
    // This is not a problem for the script, and we can continue.
    this.userExist = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            testChain = Promise.resolve();

            testChain.then(() => e.waitForId("jError", true, 2000))
                     .then(() => e.waitForId("jNotifyDismiss", true, 2000))
                     .then(() => e.click("jNotifyDismiss", true))
                     .then(() => e.click("cancelBtn", true))
                     .then(() => resolve())
                     .catch((error) => reject(error));
        });
    }

    this.orderForm = function() {
        var baseURL = location.protocol + '//' + location.host + location.pathname;
        var pathToResource = "js/test/resources/order_ORD1_p0.txt";

        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            testChain = Promise.resolve();

            testChain.then(() => TestUtil.overloadSaveAs())
                    // path to Order Collection
                    .then(() => e.waitForId("STOCK_CATALOG"))
                     .then(() => e.click("STOCK_CATALOG"))
                     .then(() => e.waitForId("STOCK_ORDERS"))
                     .then(() => e.click("STOCK_ORDERS"))
                     .then(() => e.waitForId("ORDERS"))
                     .then(() => e.click("ORDERS"))
                     .then(() => e.waitForId("_STOCK_ORDERS_ORDERS_ORDER_COLLECTION"))
                     .then(() => e.click("_STOCK_ORDERS_ORDERS_ORDER_COLLECTION"))
                     // create new Order
                     .then(() => e.waitForId("create-btn"))
                     .then(() => e.click("create-btn"))
                     .then(() => e.waitForId("save-btn"))
                     // add request
                     .then(() => e.waitForId("plus-btn-requests"))
                     .then(() => e.click("plus-btn-requests"))
                     .then(() => e.waitForId("req1-column-id"))
                     .then(() => e.click("req1-column-id"))
                     .then(() => e.waitForId("req1-operations-column-id"))
                     // choose oder status
                     .then(() => e.waitForId("ORDERINGORDER_STATUS"))
                     .then(() => e.changeSelect2("ORDERINGORDER_STATUS", "ORDERED"))
                     // save
                     .then(() => e.click("save-btn"))
                     .then(() => e.waitForId("edit-btn"))
                     // check data
                     .then(() => e.waitForId("req1-column-id"))
                     .then(() => e.waitForId("catalogNum-0"))
                     // print
                     .then(() => e.waitForId("print-order-id"))
                     .then(() => e.click("print-order-id"))
                     .then(() => e.sleep(3000)) // wait for download
                     .then(() => TestUtil.checkFileEquality("order_ORD1_p0.txt", baseURL + pathToResource, TestUtil.dateReplacer))
                     .then(() => TestUtil.returnRealSaveAs())
                     .then(() => TestUtil.testPassed(31))
                     .then(() => resolve())
                     .catch(error => TestUtil.reportError(31, error, reject));
        });
    }

    this.deletedRequests = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            testChain = Promise.resolve();

            testChain.then(() => e.waitForId("catalogNum-0"))
                     // check data before delete
                     .then(() => e.equalTo("catalogNum-0", "CC EN", true, false))
                     .then(() => e.waitForId("supplier-0"))
                     .then(() => e.equalTo("supplier-0", "Company EN Name", true, false))
                     .then(() => e.waitForId("currency-0"))
                     .then(() => e.equalTo("currency-0", "EUR", true, false))
                     // delete request
                     .then(() => e.waitForId("req1-column-id"))
                     .then(() => e.click("req1-column-id"))
                     .then(() => e.waitForId("pro1-column-id"))
                     .then(() => e.waitForId("options-menu-btn-sample-view-request"))
                     .then(() => e.click("options-menu-btn-sample-view-request"))
                     .then(() => e.waitForId("delete"))
                     .then(() => e.click("delete"))
                     .then(() => e.waitForId("reason-to-delete-id"))
                     .then(() => e.write("reason-to-delete-id", "test"))
                     .then(() => e.waitForId("accept-btn"))
                     .then(() => e.click("accept-btn"))
                     .then(() => e.waitForId("create-btn"))
                     // go to the Order 1
                     .then(() => e.waitForId("_STOCK_ORDERS_ORDERS_ORDER_COLLECTION"))
                     .then(() => e.click("_STOCK_ORDERS_ORDERS_ORDER_COLLECTION"))
                     .then(() => e.waitForId("ord1-column-id"))
                     .then(() => e.click("ord1-column-id"))
                     .then(() => e.waitForId("edit-btn"))
                     // check data after delete (should be the same)
                     .then(() => e.waitForId("catalogNum-0"))
                     .then(() => e.equalTo("catalogNum-0", "CC EN", true, false))
                     .then(() => e.waitForId("supplier-0"))
                     .then(() => e.equalTo("supplier-0", "Company EN Name", true, false))
                     .then(() => e.waitForId("currency-0"))
                     .then(() => e.equalTo("currency-0", "EUR", true, false))
                     .then(() => TestUtil.testPassed(32))
                     .then(() => resolve())
                     .catch(error => TestUtil.reportError(32, error, reject));
        });
    }

    this.trashManager = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            testChain = Promise.resolve();

            testChain.then(() => e.waitForId("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                     .then(() => e.click("_MATERIALS_BACTERIA_BACTERIA_COLLECTION"))
                     .then(() => e.waitForId("bac1-column-id"))
                     .then(() => e.click("bac1-column-id"))
                     .then(() => e.waitForId("edit-btn")) // wait for page reload
                     //delete BAC1
                     .then(() => e.waitForId("options-menu-btn-sample-view-bacteria"))
                     .then(() => e.click("options-menu-btn-sample-view-bacteria"))
                     .then(() => e.waitForId("delete"))
                     .then(() => e.click("delete"))
                     // fill Confirm form
                     .then(() => e.waitForId("reason-to-delete-id"))
                     .then(() => e.write("reason-to-delete-id", "test"))
                     .then(() => e.waitForId("accept-btn"))
                     .then(() => e.click("accept-btn"))
                     .then(() => e.waitForId("create-btn")) // wait for page reload
                     // go to TRASHCAN
                     .then(() => e.waitForId("TRASHCAN"))
                     .then(() => e.click("TRASHCAN"))
                     // The Objects BAC1 and the deleted request should be there.
                     .then(() => e.waitForId("deleted--materials-bacteria-bac1-id"))
                     .then(() => e.waitForId("deleted--stock_catalog-requests-req1-id"))
                     // clear Trash
                     .then(() => e.waitForId("empty-trash-btn"))
                     .then(() => e.click("empty-trash-btn"))
                     .then(() => e.waitForId("warningAccept"))
                     .then(() => e.click("warningAccept"))
                     .then(() => e.sleep(2000)) // wait for delete
                     // check that trash is empty
                     .then(() => e.verifyExistence("deleted--materials-bacteria-bac1-id", false))
                     .then(() => e.verifyExistence("deleted--stock_catalog-requests-req1-id", false))
                     .then(() => TestUtil.testPassed(33))
                     .then(() => resolve())
                     .catch(error => TestUtil.reportError(33, error, reject));
        });
    }

    this.vocabularyViewer = function() {
        return new Promise(function executor(resolve, reject) {
            var e = EventUtil;

            testChain = Promise.resolve();

            testChain.then(() => e.waitForId("VOCABULARY_BROWSER"))
                     .then(() => e.click("VOCABULARY_BROWSER"))
                     .then(() => e.waitForId("vocabulary-browser-title-id")) // wait for page reload
                     // check count
                     .then(() => e.waitForId("total-count-id"))
                     .then(() => e.equalTo("total-count-id", "36", true, false))
                     // search for PLASMID
                     .then(() => e.waitForId("search-input-id"))
                     .then(() => e.write("search-input-id", "PLASMID"))
                     .then(() => e.click("search-button-id"))
                     .then(() => e.sleep(2000)) // wait for page reload
                     // Click on the PLASMID_RELATIONSHIP row, it should show a list with five relationships.
                     .then(() => e.waitForId("annotationplasmid_relationship_id"))
                     .then(() => e.click("annotationplasmid_relationship_id"))
                     .then(() => e.sleep(2000)) // wait for page reload
                     .then(() => e.waitForId("total-count-id"))
                     .then(() => e.equalTo("total-count-id", "5", true, false))
                     .then(() => TestUtil.testPassed(34))
                     .then(() => resolve())
                     .catch(error => TestUtil.reportError(34, error, reject));
        });
    }
}