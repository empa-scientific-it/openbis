set APPLICATION_NAME=openbis
set LIB=webapps\%APPLICATION_NAME%\WEB-INF\lib
java -classpath %LIB%\cisd-args4j.jar;%LIB%\commons-lang.jar;%LIB%\commons-io.jar;%LIB%\commons-codec.jar;%LIB%\jline.jar;%LIB%\log4j.jar;%LIB%\%APPLICATION_NAME%.jar ch.systemsx.cisd.authentication.file.PasswordEditorCommand %*