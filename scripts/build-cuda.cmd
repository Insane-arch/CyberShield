@echo off
setlocal

echo CyberShield - CUDA Build and Verification
echo Muhammad Hamza Naseer - 247121
echo Sultan Majeed - 247187
echo.

rem Resolve the project directory from this script's location.
pushd "%~dp0.." || exit /b 1

set "CYBERSHIELD_VCVARS=C:\Program Files\Microsoft Visual Studio\18\Community\VC\Auxiliary\Build\vcvarsall.bat"

if not exist "%CYBERSHIELD_VCVARS%" (
    echo ERROR: C++ environment setup script was not found.
    echo Expected: %CYBERSHIELD_VCVARS%
    goto :failed
)

call "%CYBERSHIELD_VCVARS%" x64 -vcvars_ver=14.44
if errorlevel 1 goto :failed

where cl >nul 2>&1
if errorlevel 1 (
    echo ERROR: C++ compiler is unavailable.
    goto :failed
)

where nvcc >nul 2>&1
if errorlevel 1 (
    echo ERROR: CUDA compiler is unavailable in PATH.
    goto :failed
)

if not exist out mkdir out
if not exist out goto :failed

echo.
echo Building CPU reference checks...
cl /nologo /EHsc /std:c++17 cuda\CpuReferenceChecks.cpp /Fo:out\CpuReferenceChecks.obj /Fe:out\CpuReferenceChecks.exe
if errorlevel 1 goto :failed

echo.
echo Building CUDA containment...
nvcc -std=c++17 cuda\CudaContainment.cu -o out\CudaContainment.exe
if errorlevel 1 goto :failed

echo.
echo Running CPU reference checks...
out\CpuReferenceChecks.exe
if errorlevel 1 goto :failed

echo.
echo Running CUDA step checks...
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-cuda-steps.ps1
if errorlevel 1 goto :failed

echo.
echo BUILD AND VERIFICATION PASSED
popd
endlocal
exit /b 0

:failed
echo.
echo BUILD OR VERIFICATION FAILED
popd
endlocal
exit /b 1