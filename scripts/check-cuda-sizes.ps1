$ErrorActionPreference = "Stop"

Write-Host "CyberShield - CUDA Network Size Checks"
Write-Host "Muhammad Hamza Naseer - 247121"
Write-Host "Sultan Majeed - 247187"
Write-Host ""

$projectRoot = Split-Path -Parent $PSScriptRoot
$executable = Join-Path $projectRoot "out\CudaContainment.exe"

if (-not (Test-Path $executable)) {
    throw "Build CudaContainment.exe before running these checks."
}

# Totals are listed in the program's scenario order:
# no defence, isolated source, isolated middle, protected middle.
$cases = @(
    @{
        Name = "Minimum size"
        Arguments = @("2", "3")
        Totals = @(3, 1, 1, 1)
    },
    @{
        Name = "100 computers"
        Arguments = @("99", "100")
        Totals = @(100, 1, 49, 49)
    },
    @{
        Name = "Below block boundary"
        Arguments = @("126", "127")
        Totals = @(127, 1, 63, 63)
    },
    @{
        Name = "Exact block boundary"
        Arguments = @("127", "128")
        Totals = @(128, 1, 63, 63)
    },
    @{
        Name = "Above block boundary"
        Arguments = @("128", "129")
        Totals = @(129, 1, 64, 64)
    },
    @{
        Name = "Maximum size with zero steps"
        Arguments = @("0", "10000")
        Totals = @(1, 1, 1, 1)
    },
    @{
        Name = "Size below minimum"
        Arguments = @("7", "2")
        Totals = $null
    },
    @{
        Name = "Size above maximum"
        Arguments = @("7", "10001")
        Totals = $null
    },
    @{
        Name = "Non-numeric size"
        Arguments = @("7", "abc")
        Totals = $null
    },
    @{
        Name = "Too many arguments"
        Arguments = @("7", "8", "extra")
        Totals = $null
    }
)

$failed = 0

foreach ($case in $cases) {
    $caseArguments = $case.Arguments
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

    if ($null -eq $case.Totals) {
        $passed = (
            $exitCode -ne 0 -and
            $outputText.Contains("Error:")
        )
    }
    else {
        $passed = (
            $exitCode -eq 0 -and
            $outputText.Contains(
                "All 4 CPU/GPU state comparisons passed."
            )
        )

        $rowPattern = '(?m)^(?:No defence|Isolate Computer \d+|Protect Computer \d+)\s+(\d+)\s+(\d+)\s+YES\s+PASS\s*$'
        $rows = [regex]::Matches($outputText, $rowPattern)

        if ($rows.Count -ne 4) {
            $passed = $false
        }
        else {
            for ($index = 0; $index -lt 4; $index++) {
                $cpuTotal = [int]$rows[$index].Groups[1].Value
                $gpuTotal = [int]$rows[$index].Groups[2].Value
                $expectedTotal = $case.Totals[$index]

                if ($cpuTotal -ne $expectedTotal -or
                    $gpuTotal -ne $expectedTotal) {
                    $passed = $false
                }
            }
        }
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
    Write-Host "$failed network size check(s) failed."
    exit 1
}

Write-Host "All $($cases.Count) CUDA network size checks passed."
exit 0
