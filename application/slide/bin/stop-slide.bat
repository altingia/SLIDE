@echo off

REM ****** AUTOGENERATED CODE *******
SET glassfish_dir=C:\Program Files\glassfish-4.1.1\bin
REM *********************************

taskkill /f /im mongod.exe

CALL "%glassfish_dir%\asadmin.bat" stop-domain domain1
