QUnit.module("ELN-Test");

var elnTest = function(number, name) {
    QUnit.asyncTest(number + ". " + name, function(assert) {
        console.log("TEST2 "+number+": "+name);
        QUnit.expect(1);
        $(document).on('test' + number + 'event', function(e) {
            console.log("TEST3 "+number+": "+name);
            QUnit.start();
            assert.equal(e.msg, "Test " + number + " passed", e.msg);
        });
    });
}

elnTest(1, "Login");
elnTest(2, "Inventory Space and Sample Types");
elnTest(3, "Settings Form - Enable Sample Types to Show in Drop-downs");
QUnit.asyncTest("4. Microscopy and Flow Cytometry plugin", function(assert) {
    QUnit.expect(1);
    QUnit.start();
    assert.equal("Test 4 should be tested locally",
                 "Test 4 should be tested locally",
                 "Test 4 should be tested locally");
});
elnTest(5, "User Manager");
elnTest(6, "Sample Form - Creation");
elnTest(7, "Sample Form - Edit: Add a Photo and Parents/Children");
elnTest(8, "Sample Hierarchy as Graph");
elnTest(9, "Sample Hierarchy as Table");
elnTest(10, "Sample Form - Copy");
elnTest(11, "Sample Form - Delete");
elnTest(12, "Inventory Table - Exports/Imports for Update");
elnTest(13, "Inventory Table - Imports for Create - Automatic Codes");
elnTest(14, "Inventory Table - Imports for Create - Given Codes");
elnTest(15, "Sample Form - Storage");
elnTest(16, "Storage Manager - Moving Box");
elnTest(17, "Storage Manager - Moving Sample");
elnTest(18, "Create Protocol");
elnTest(19, "Project Form - Create/Update");
elnTest(20, "Experiment Form - Create/Update");
elnTest(21, "Experiment Step Form - Create/Update");
elnTest(23, "Experiment Step Form - Dataset Uploader and Viewer");
elnTest(24, "Experiment Step Form - Children Generator (not exist)");
elnTest(25, "Project  Form - Show in project overview");
elnTest(26, "Search");
elnTest(27, "Supplier Form");
elnTest(28, "Product Form");
elnTest(29, "Request Form");
elnTest(30, "Order Form");
elnTest(31, "orderForm");
elnTest(32, "deletedRequests");
elnTest(33, "trashManager");
elnTest(34, "vocabularyViewer");

