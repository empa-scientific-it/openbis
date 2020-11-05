QUnit.module("ELN-Test");

QUnit.test("1. Login", function(assert) {
    stop();

    $("#eln-frame").on('test1event', function(e) {
        start();
        assert.equal(e.msg, "Test 1 passed", e.msg);
    });
});

QUnit.test("2. Inventory Space and Sample Types", function(assert) {
    stop();

    $(document).on('test2event', function(e) {
        start();
        assert.equal(e.msg, "Test 2 passed", e.msg);
    });
});

QUnit.test("3. Settings Form - Enable Sample Types to Show in Drop-downs", function(assert) {
    stop();

    $(document).on('test3event', function(e) {
        start();
        assert.equal(e.msg, "Test 3 passed", e.msg);
    });
});

QUnit.test("4. Microscopy and Flow Cytometry plugin", function(assert) {
    assert.equal("Test 4 should be tested locally",
                 "Test 4 should be tested locally",
                 "Test 4 should be tested locally");
});

QUnit.test("5. User Manager", function(assert) {
    stop();

    $(document).on('test5event', function(e) {
        start();
        assert.equal(e.msg, "Test 5 passed", e.msg);
    });
});

QUnit.test("6. Sample Form - Creation", function(assert) {
    stop();

    $(document).on('test6event', function(e) {
        start();
        assert.equal(e.msg, "Test 6 passed", e.msg);
    });
});

QUnit.test("7. Sample Form - Edit: Add a Photo and Parents/Children", function(assert) {
    stop();

    $(document).on('test7event', function(e) {
        start();
        assert.equal(e.msg, "Test 7 passed", e.msg);
    });
});

QUnit.test("8. Sample Hierarchy as Graph", function(assert) {
    stop();

    $(document).on('test8event', function(e) {
        start();
        assert.equal(e.msg, "Test 8 passed", e.msg);
    });
});

QUnit.test("9. Sample Hierarchy as Table", function(assert) {
    stop();

    $(document).on('test9event', function(e) {
        start();
        assert.equal(e.msg, "Test 9 passed", e.msg);
    });
});

QUnit.test("10. Sample Form - Copy", function(assert) {
    stop();

    $(document).on('test10event', function(e) {
        start();
        assert.equal(e.msg, "Test 10 passed", e.msg);
    });
});

QUnit.test("11. Sample Form - Delete", function(assert) {
    stop();

    $(document).on('test11event', function(e) {
        start();
        assert.equal(e.msg, "Test 11 passed", e.msg);
    });
});

QUnit.test("12. Inventory Table - Exports/Imports for Update", function(assert) {
    stop();

    $(document).on('test12event', function(e) {
        start();
        assert.equal(e.msg, "Test 12 passed", e.msg);
    });
});

QUnit.test("13. Inventory Table - Imports for Create - Automatic Codes", function(assert) {
    stop();

    $(document).on('test13event', function(e) {
        start();
        assert.equal(e.msg, "Test 13 passed", e.msg);
    });
});

QUnit.test("14. Inventory Table - Imports for Create - Given Codes", function(assert) {
    stop();

    $(document).on('test14event', function(e) {
        start();
        assert.equal(e.msg, "Test 14 passed", e.msg);
    });
});

QUnit.test("15. Sample Form - Storage", function(assert) {
    stop();

    $(document).on('test15event', function(e) {
        start();
        assert.equal(e.msg, "Test 15 passed", e.msg);
    });
});

QUnit.test("16. Storage Manager - Moving Box", function(assert) {
    stop();

    $(document).on('test16event', function(e) {
        start();
        assert.equal(e.msg, "Test 16 passed", e.msg);
    });
});

QUnit.test("17. Storage Manager - Moving Sample", function(assert) {
    stop();

    $(document).on('test17event', function(e) {
        start();
        assert.equal(e.msg, "Test 17 passed", e.msg);
    });
});

QUnit.test("18. Create Protocol", function(assert) {
    stop();

    $(document).on('test18event', function(e) {
        start();
        assert.equal(e.msg, "Test 18 passed", e.msg);
    });
});

QUnit.test("19. Project Form - Create/Update", function(assert) {
    stop();

    $(document).on('test19event', function(e) {
        start();
        assert.equal(e.msg, "Test 19 passed", e.msg);
    });
});

QUnit.test("20. Experiment Form - Create/Update", function(assert) {
    stop();

    $(document).on('test20event', function(e) {
        start();
        assert.equal(e.msg, "Test 20 passed", e.msg);
    });
});

QUnit.test("21. Experiment Step Form - Create/Update", function(assert) {
    stop();

    $(document).on('test21event', function(e) {
        start();
        assert.equal(e.msg, "Test 21 passed", e.msg);
    });
});

QUnit.test("22. is now disabled", function(assert) {
    stop();

    $(document).on('test22event', function(e) {
        start();
        assert.equal(e.msg, "Test 22 is not exist", e.msg);
    });
});

QUnit.test("23. Experiment Step Form - Dataset Uploader and Viewer", function(assert) {
    stop();

    $(document).on('test23event', function(e) {
        start();
        assert.equal(e.msg, "Test 23 passed", e.msg);
    });
});

QUnit.test("24. Experiment Step Form - Children Generator (not exist)", function(assert) {
    stop();

    $(document).on('test24event', function(e) {
        start();
        assert.equal(e.msg, "Test 24 is not exist", e.msg);
    });
});

QUnit.test("25. Project  Form - Show in project overview", function(assert) {
    stop();

    $(document).on('test25event', function(e) {
        start();
        assert.equal(e.msg, "Test 25 passed", e.msg);
    });
});

QUnit.test("26. Search", function(assert) {
    stop();

    $(document).on('test26event', function(e) {
        start();
        assert.equal(e.msg, "Test 26 passed", e.msg);
    });
});

QUnit.test("27. Supplier Form", function(assert) {
    stop();

    $(document).on('test27event', function(e) {
        start();
        assert.equal(e.msg, "Test 27 passed", e.msg);
    });
});

QUnit.test("28. Product Form", function(assert) {
    stop();

    $(document).on('test28event', function(e) {
        start();
        assert.equal(e.msg, "Test 28 passed", e.msg);
    });
});

QUnit.test("29. Request Form", function(assert) {
    stop();

    $(document).on('test29event', function(e) {
        start();
        assert.equal(e.msg, "Test 29 passed", e.msg);
    });
});

QUnit.test("30. Order Form", function(assert) {
    stop();

    $(document).on('test30event', function(e) {
        start();
        assert.equal(e.msg, "Test 30 passed", e.msg);
    });
});

QUnit.test("31. logout", function(assert) {
    stop();

    $(document).on('test31event', function(e) {
        start();
        assert.equal(e.msg, "Test 31 passed", e.msg);
    });
});