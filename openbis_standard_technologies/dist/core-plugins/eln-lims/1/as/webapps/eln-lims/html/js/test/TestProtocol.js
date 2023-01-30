var TestProtocol = new function () {

    this.getTestCount = function() {
        return 34; // If one test is broken, then all tests must be failed.
    }

    this.startAdminTests = function(withLogin) {
        testChain = Promise.resolve();

        if (withLogin) {
            //1. Login
            testChain.then(() => AdminTests.login())
        }
                 //2. Inventory Space and Sample Types
        testChain.then(() => AdminTests.inventorySpace())
                 //3. Settings Form - Enable Sample Types to Show in Drop-downs
                 .then(() => AdminTests.enableBacteriaToShowInDropDowns())
                 //4. Microscopy and Flow Cytometry plugin
                 .then(() => TestUtil.testLocally(4))
                 //5. User Manager
                 .then(() => AdminTests.userManager())
                 .catch(error => { console.log(error) });
    }

    this.startUserTests = function() {
        testChain = Promise.resolve();
                 //5. User Manager (end of test)
        testChain.then(() => UserTests.userManager(5))
                 //6. Sample Form - Creation
                 .then(() => UserTests.creationSampleForm())
                 //7. Sample Form - Edit: Add a Photo and Parents/Children
                 .then(() => UserTests.editSampleForm())
                 //8. Sample Hierarchy as Graph
                 .then(() => UserTests.sampleHierarchyAsGraph())
                 //9. Sample Hierarchy as Table
                 .then(() => UserTests.sampleHierarchyAsTable())
                 //10. Sample Form - Copy
                 .then(() => UserTests.copySampleForm())
                 //11. Sample Form - Delete
                 .then(() => UserTests.deleteSampleForm())
                 //12. Inventory Table - Exports/Imports for Update
                 .then(() => UserTests.exportsImportsUpdate())
                 //13. Inventory Table - Imports for Create - Automatic Codes
                 .then(() => UserTests.importsAutomaticCodes())
                 //14. Inventory Table - Imports for Create - Given Codes
                 .then(() => UserTests.importsGivenCodes())
                 //15. Sample Form - Storage
                 .then(() => UserTests.storageTest())
                 //16. Storage Manager - Moving Box
                 .then(() => UserTests.movingBoxTest())
                 //17. Storage Manager - Moving Sample
                 .then(() => UserTests.movingSampleTest())
                 //18. Create Protocol
                 .then(() => UserTests.createProtocol())
                 //19. Project Form - Create/Update
                 .then(() => UserTests.createProject())
                 //20. Experiment Form - Create/Update
                 .then(() => UserTests.createExperiment())
                 //21. Experiment Step Form - Create/Update
                 .then(() => UserTests.createExperimentStep())
                 //22. is now disabled
                 .then(() => TestUtil.testNotExist(22))
                 //23. Experiment Step Form - Dataset Uploader and Viewer
                 .then(() => UserTests.datasetUploader())
                 //24. Experiment Step Form - Children Generator (not exist)
                 .then(() => TestUtil.testNotExist(24))
                 //25. Project  Form - Show in project overview
                 .then(() => UserTests.showInProjectOverview())
                 //26. Search
                 .then(() => UserTests.search())
                 //27. Supplier Form
                 .then(() => UserTests.supplierForm())
                 //28. Product Form
                 .then(() => UserTests.productForm())
                 //29. Request Form
                 .then(() => UserTests.requestForm())
                 //30. Order Form
                 .then(() => UserTests.orderForm())
                 //31. logout
                 .then(() => UserTests.logout())
                 .catch(error => { console.log(error) });
    }

    this.finishTests = function() {
        testChain = Promise.resolve();
                 //31 . Order Form
        testChain.then(() => TestUtil.deleteCookies("suitename"))
                 .then(() => TestUtil.login("admin", "a"))
                 .then(() => AdminTests.orderForm())
                 //32. Order Form - Avoiding modifying orders by deleted requests
                 .then(() => AdminTests.deletedRequests())
                 //33. Trash Manager
                 .then(() => AdminTests.trashManager())
                 //34. Vocabulary Viewer
                 .then(() => AdminTests.vocabularyViewer())
                 //Tests passed
                 .then(() => TestUtil.allTestsPassed())
                 .catch(error => { console.log(error) });
    }
}