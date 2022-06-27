# openBIS - next generation UI

## Development of NG UI

1. Generate openBIS JS bundle by running in command line
   1. cd /<OPENBIS_PROJECT_ROOT>/openbis_standard_technologies
   2. ./gradlew :bundleOpenbisStaticResources
2. Start openBIS in your chosen IDE (NG UI assumes it will run at: http://localhost:8888/openbis-test/):
   1. run openBISDevelopementEnvironmentASPrepare gradle task
   2. run openBISDevelopementEnvironmentASStart gradle task
3. In command line do:
   1. cd /<OPENBIS_PROJECT_ROOT>/openbis_ng_ui
   2. npm install
   3. npm run dev
4. Open in your chosen browser a url: http://localhost:8124/ng-ui-path

## Setting up IntelliJ Idea

1. Under "IntelliJ IDEA" -> "Preferences" -> "Languages and Frameworks" -> Javascript, set the language version to ECMAScript 6.

## Setting up Visual Studio Code (alternative to IntelliJ Idea)

Install "ESLint" and "Prettier - Code formatter" extensions.


