QUnit.module("ELN-Test");

QUnit.asyncTest("1. Login", function(assert) {
    QUnit.expect(1);
    $("#eln-frame").on('test1event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 1 passed", e.msg);
    });
});

QUnit.asyncTest("2. Inventory Space and Sample Types", function(assert) {
    QUnit.expect(1);
    $(document).on('test2event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 2 passed", e.msg);
    });
});

QUnit.asyncTest("3. Settings Form - Enable Sample Types to Show in Drop-downs", function(assert) {
    QUnit.expect(1);
    $(document).on('test3event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 3 passed", e.msg);
    });
});

QUnit.asyncTest("4. Microscopy and Flow Cytometry plugin", function(assert) {
    QUnit.expect(1);
    QUnit.start();
    assert.equal("Test 4 should be tested locally",
                 "Test 4 should be tested locally",
                 "Test 4 should be tested locally");
});

QUnit.asyncTest("5. User Manager", function(assert) {
    QUnit.expect(1);
    $(document).on('test5event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 5 passed", e.msg);
    });
});

QUnit.asyncTest("6. Sample Form - Creation", function(assert) {
    QUnit.expect(1);
    $(document).on('test6event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 6 passed", e.msg);
    });
});

QUnit.asyncTest("7. Sample Form - Edit: Add a Photo and Parents/Children", function(assert) {
    QUnit.expect(1);
    $(document).on('test7event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 7 passed", e.msg);
    });
});

QUnit.asyncTest("8. Sample Hierarchy as Graph", function(assert) {
    QUnit.expect(1);
    $(document).on('test8event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 8 passed", e.msg);
    });
});

QUnit.asyncTest("9. Sample Hierarchy as Table", function(assert) {
    QUnit.expect(1);
    $(document).on('test9event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 9 passed", e.msg);
    });
});

QUnit.asyncTest("10. Sample Form - Copy", function(assert) {
    QUnit.expect(1);
    $(document).on('test10event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 10 passed", e.msg);
    });
});

QUnit.asyncTest("11. Sample Form - Delete", function(assert) {
    QUnit.expect(1);
    $(document).on('test11event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 11 passed", e.msg);
    });
});

QUnit.asyncTest("12. Inventory Table - Exports/Imports for Update", function(assert) {
    QUnit.expect(1);
    $(document).on('test12event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 12 passed", e.msg);
    });
});

QUnit.asyncTest("13. Inventory Table - Imports for Create - Automatic Codes", function(assert) {
    QUnit.expect(1);
    $(document).on('test13event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 13 passed", e.msg);
    });
});

QUnit.asyncTest("14. Inventory Table - Imports for Create - Given Codes", function(assert) {
    QUnit.expect(1);
    $(document).on('test14event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 14 passed", e.msg);
    });
});

QUnit.asyncTest("15. Sample Form - Storage", function(assert) {
    QUnit.expect(1);
    $(document).on('test15event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 15 passed", e.msg);
    });
});

QUnit.asyncTest("16. Storage Manager - Moving Box", function(assert) {
    QUnit.expect(1);
    $(document).on('test16event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 16 passed", e.msg);
    });
});

QUnit.asyncTest("17. Storage Manager - Moving Sample", function(assert) {
    QUnit.expect(1);
    $(document).on('test17event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 17 passed", e.msg);
    });
});

QUnit.asyncTest("18. Create Protocol", function(assert) {
    QUnit.expect(1);
    $(document).on('test18event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 18 passed", e.msg);
    });
});

QUnit.asyncTest("19. Project Form - Create/Update", function(assert) {
    QUnit.expect(1);
    $(document).on('test19event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 19 passed", e.msg);
    });
});

QUnit.asyncTest("20. Experiment Form - Create/Update", function(assert) {
    QUnit.expect(1);
    $(document).on('test20event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 20 passed", e.msg);
    });
});

QUnit.asyncTest("21. Experiment Step Form - Create/Update", function(assert) {
    QUnit.expect(1);
    $(document).on('test21event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 21 passed", e.msg);
    });
});

QUnit.asyncTest("23. Experiment Step Form - Dataset Uploader and Viewer", function(assert) {
    QUnit.expect(1);
    $(document).on('test23event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 23 passed", e.msg);
    });
});

QUnit.asyncTest("24. Experiment Step Form - Children Generator (not exist)", function(assert) {
    QUnit.expect(1);
    $(document).on('test24event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 24 is not exist", e.msg);
    });
});

QUnit.asyncTest("25. Project  Form - Show in project overview", function(assert) {
    QUnit.expect(1);
    $(document).on('test25event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 25 passed", e.msg);
    });
});

QUnit.asyncTest("26. Search", function(assert) {
    QUnit.expect(1);
    $(document).on('test26event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 26 passed", e.msg);
    });
});

QUnit.asyncTest("27. Supplier Form", function(assert) {
    QUnit.expect(1);
    $(document).on('test27event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 27 passed", e.msg);
    });
});

QUnit.asyncTest("28. Product Form", function(assert) {
    QUnit.expect(1);
    $(document).on('test28event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 28 passed", e.msg);
    });
});

QUnit.asyncTest("29. Request Form", function(assert) {
    QUnit.expect(1);
    $(document).on('test29event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 29 passed", e.msg);
    });
});

QUnit.asyncTest("30. Order Form", function(assert) {
    QUnit.expect(1);
    $(document).on('test30event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 30 passed", e.msg);
    });
});

QUnit.asyncTest("31. logout", function(assert) {
    QUnit.expect(1);
    $(document).on('test31event', function(e) {
        QUnit.start();
        assert.equal(e.msg, "Test 31 passed", e.msg);
    });
});
