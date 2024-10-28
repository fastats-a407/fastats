@echo off

REM move to the current directory
cd /d %~dp0

REM run setup_hooks.py
python setup_hooks.py

REM print message for success
echo Git hooks have been set up successfully.
pause
