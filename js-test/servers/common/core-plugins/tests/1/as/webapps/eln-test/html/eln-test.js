QUnit.jUnitReport = function(report) {
	$("#qunit-junit-report").text(report.xml);
	console.log(report.xml);
}

QUnit.module("ELN-Test");

QUnit.test("AdminTests.login()", function(assert) {
    stop();

    $("#eln-frame").on('test1event', function(e) {
        start();
        assert.equal(e.msg, "Test 1 passed", e.msg);
    });
});

/*
QUnit.test("AdminTests.inventorySpace()", function(assert) {
    stop();
    $(document).on('test2event', function(e) {
        start();
        assert.equal(e.msg, "Test 2 passed", e.msg);
    });
});
*/
