#!/bin/bash

JAVA_CMD=/home/webuser/.sdkman/candidates/java/21.0.4-amzn/bin/java
JAVA_OPTS="-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"
JAR="-jar /home/webuser/dev/proj/java-cli-editor/text-editor/build/libs/text-editor-uber.jar"
$JAVA_CMD $JAVA_OPTS $JAR

