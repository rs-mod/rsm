@echo off

call :gen "josefin"
call :gen "josefin-bold"
call :gen "nunito"
call :gen "opensans-regular"
call :gen "product-sans"
call :gen "roboto-medium"
call :gen "sf-pro-rounded"

pause

:gen
echo.
echo generating atlas for %~1

msdf-atlas-gen -font truetype/%~1.ttf -type msdf -size 64 -pxrange 8 -pots -imageout out/%~1.png -json out/%~1.json

exit /b