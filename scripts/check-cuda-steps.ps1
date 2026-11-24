$ErrorActionPreference = "Stop"

Write-Host "CyberShield - CUDA Step Checks"
Write-Host "Muhammad Hamza Naseer - 247121"
Write-Host "Sultan Majeed - 247187"
Write-Host ""

$projectRoot = Split-Path -Parent $PSScriptRoot
$executable = Join-Path $projectRoot "out\CudaContainment.exe"

if (-not (Test-Path $executable)) {
    throw "Compile cuda\CudaContainment.cu before running these checks."
}

$cases = @(
    @{ Name = "Default steps"; Arguments = @(); Valid = $true },
    @{ Name = "Zero steps"; Arguments = @("0"); Valid = $true },
    @{ Name = "One step"; Arguments = @("1"); Valid = $true },
    @{ Name = "Seven steps"; Arguments = @("7"); Valid = $true },
    @{ Name = "Beyond full spread"; Arguments = @("8"); Valid = $true },
    @{ Name = "Negative steps"; Arguments = @("-1"); Valid = $false },
    @{ Name = "Decimal steps"; Arguments = @("1.5"); Valid = $false },
    @{ Name = "Text input"; Arguments = @("abc"); Valid = $false },
    @{ Name = "Over limit"; Arguments = @("10001"); Valid = $false },
    @{ Name = "Integer overflow"; Arguments = @("99999999999999999999"); Valid = $false },
    @{ Name = "Extra argument"; Arguments = @("7", "extra"); Valid = $false }
)

$failed = 0

foreach ($case in $cases) {
    $caseArguments = $case.Arguments

    # Windows PowerShell can treat native stderr as an error record.
    # Capture it while using the executable's exit code for the check.
    $previousPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"

    try {
        $output = & $executable @caseArguments 2>&1
        $exitCode = $LASTEXITCODE
    }
    finally {
        $ErrorActionPreference = $previousPreference
    }

    $outputText = ($output | ForEach-Object { "$_" }) -join "`n"

    if ($case.Valid) {
        $passed = (
            $exitCode -eq 0 -and
            $outputText.Contains(
                "All 4 CPU/GPU state comparisons passed."
            )
        )
    }
    else {
        $passed = (
            $exitCode -ne 0 -and
            $outputText.Contains("Error:")
        )
    }

    if ($passed) {
        Write-Host ("PASS  {0}" -f $case.Name)
    }
    else {
        $failed++
        Write-Host ("FAIL  {0} (exit code: {1})" -f $case.Name, $exitCode)
        Write-Host $outputText
    }
}

Write-Host ""

if ($failed -gt 0) {
    Write-Host "$failed check(s) failed."
    exit 1
}

Write-Host "All $($cases.Count) CUDA step checks passed."
exit 0