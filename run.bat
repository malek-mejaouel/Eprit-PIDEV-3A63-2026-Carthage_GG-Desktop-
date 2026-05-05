@echo off
REM Configuration du JAVA_HOME
set JAVA_HOME=C:\Program Files\Java\jdk-17

REM Change vers le répertoire du projet
cd /d "%~dp0"

REM Lance l'application Maven
mvnw.cmd javafx:run

REM Garde la fenêtre ouverte en cas d'erreur
pause

