QUnit.jUnitReport = function(report) {
	$("#qunit-junit-report").text(report.xml);
	console.log(report.xml);
}

QUnit.test("AdminTests.login()", function(assert) {

    assert.ok(1 == "1", "Passed!");
});