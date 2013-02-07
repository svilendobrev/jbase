@echo on
del /s /q bin\*
call ant debug

rem adb devices > %temp%\serials.txt
for /F "skip=1 tokens=1,2*" %%i in ('adb devices') do (
  rem @echo zzz %%i
  adb -s %%i install -r bin/*-debug.apk
)
rem del %temp%\serials.txt
pause
