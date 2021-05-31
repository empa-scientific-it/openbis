For development and bug fixing the tests have to run in a Web browser. 

For this an openBIS instance has to start up. To get such a test instance running with all core-plugins with 
the test suits the build file 'build.gradle' has to be temporarily modified (do not check in this change!): 
Replace line 

options.suites('source/java/tests.xml') 

by

options.suites('source/java/tests-dev.xml') 

Then start up everything by executing inside the gradle/ directory:

./gradlew js-test:clean js-test:test

This will start up openBIS AS and two DSSs. 

When the console output is no longer busy Firefox should be started.
Enter the following URL: http://localhost:20000/openbis/
You should be able to log in as user 'admin' with any password.

Next choose a test suite in menu 'Utilities'. A tab will be opened which shows all tests.

If a test fails you can click on the test and only the failed test will be shown.

You can change the test code (e.g. in servers/common/core-plugins/tests/1/as/webapps/openbis-test/html/openbis-test.js).
To see the changes you have to reload the frame (not the application) in the Web browser.

Geckodriver
==========

If you want to develop tests on your machine make sure that you use correct Geckodriver for your operation system.

You can find it here:
https://github.com/mozilla/geckodriver/releases

If you are using MAC OS you can easily install Geckodriver using command:
brew install geckodriver

Otherwise you need to specify the path to your Geckodriver in makeGeckodriverExecutable task.

Some Tips:
==========

Developing:
-----------

Here are some tips for speed up development:

* Change the code not only in the original Javascript file but also in the file in targets/gradle/webapps/webapp. 
  Otherwise the old code is executed. This isn't necessary for testing classes.
  If you change only API code you can do the following command:
  
  cp -R <root folder>/openbis/source/java/ch/systemsx/cisd/openbis/public/resources/api <root folder>/js-test/targets/gradle/webapps/webapp/resources/
  
* In case of changes of Java classes the a stop and restart (using ./gradlew js-test:clean js-test:test) is needed.
  The test server is available much faster after outcommenting the following lines in build.gradle of 
  project openbis_standard_technologies:
  
  war.dependsOn compileGwt
  war.dependsOn signWebStartJars
  
  Note, that when compileGwt is not executed changes in JS code should be copy after server start up with 
  the above mentioned copy command.  

Debugging:
----------

Out comment the line

require.urlArgs = 'now=' + Date.now();

in servers/common/core-plugins/tests/1/as/webapps/openbis-v3-api-test/html/index.html if you want to debug
Javascript in the browser. Don't forget to bring the statement back before you do development again.


