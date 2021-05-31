# ELN Manual Tests #

## Description ##

On magic wednesday we're performing sanity test (making sure there are no general show stoppers) for the ELN. 

You can find a description of all the tests at the link - https://wiki-bsse.ethz.ch/pages/viewpage.action?spaceKey=CISDInt&title=ELN+Manual+Tests

## How to start ##

Add parameter test=true in your URL.

For example: http://localhost:8888/openbis-test/webapp/eln-lims/?test=true

or call for TestProtocol.startAdminTests();

TestProtocol contains all tests with the same names and in the same order as the wiki page.

You can start only one test if you need, but remember that test can be dependent of the results of previous tests.

To start one test look at the TestProtocol. Find the test and call for it by the name.

For example: AdminTests.login();

### Other parameters:

jenkins=true - send signals to Jenkins
testWithLogin=true - login first

## Tests results ##

If test passed you will see a successful message in browser console.

If all tests passed you will see pop up with successful message in your browser.

If test fails you will see an error in browser console.