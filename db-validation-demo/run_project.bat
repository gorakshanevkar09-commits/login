@echo off
rem Run DB validation project tasks with environment loading
rem Usage: run_project.bat [ENV] [action]
rem ENV: DEV (default), UAT, OAT
rem action: tests (default), watcher, all

setlocal enabledelayedexpansion
set "ENV=%~1"
if "%ENV%"=="" set "ENV=DEV"
set "ACTION=%~2"
if "%ACTION%"=="" set "ACTION=tests"

echo Running db-validation-demo with ENV=%ENV% ACTION=%ACTION%
set "ENVFILE=env\%ENV%.env"
if not exist "%ENVFILE%" (
  echo Environment file %ENVFILE% not found.
  exit /b 1
)

rem Load env file (ignore comments and empty lines)
for /f "usebackq tokens=1* delims==" %%A in ('findstr /r /v "^#" "%ENVFILE%"') do (
  if not "%%B"=="" (
    set "%%A=%%B"
  )
)

echo Loaded environment from %ENVFILE%

if /i "%ACTION%"=="tests" (
  mvn -f db-validation-demo test
  exit /b %errorlevel%
) else if /i "%ACTION%"=="watcher" (
  pushd db-validation-demo
  mvn -q -DskipTests test-compile exec:java -Dexec.mainClass=com.example.dbvalidation.service.AwsWatcherDemo
  set "RC=%errorlevel%"
  popd
  exit /b %RC%
) else if /i "%ACTION%"=="all" (
  mvn -f db-validation-demo test
  pushd db-validation-demo
  mvn -q -DskipTests test-compile exec:java -Dexec.mainClass=com.example.dbvalidation.service.AwsWatcherDemo
  set "RC=%errorlevel%"
  popd
  exit /b %RC%
) else (
  echo Unknown action %ACTION%
  exit /b 2
)

endlocal
