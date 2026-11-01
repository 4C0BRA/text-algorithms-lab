param([string]$Task = "all")
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$Build = "build"
$Jar = "$Build/text-algorithms-lab.jar"

function Invoke-Checked {
    param([string]$Exe, [string[]]$Arguments)
    & $Exe @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Exe exited with code $LASTEXITCODE" }
}

function Compile {
    if (Test-Path $Build) { Remove-Item -Recurse -Force $Build }
    New-Item -ItemType Directory -Force "$Build/main", "$Build/test" | Out-Null
    $main = Get-ChildItem -Recurse -Filter *.java src/main/java | ForEach-Object { $_.FullName }
    $test = Get-ChildItem -Recurse -Filter *.java src/test/java | ForEach-Object { $_.FullName }
    Invoke-Checked javac (@("--release", "17", "-encoding", "UTF-8", "-d", "$Build/main") + $main)
    Invoke-Checked javac (@("--release", "17", "-encoding", "UTF-8", "-cp", "$Build/main", "-d", "$Build/test") + $test)
    Invoke-Checked jar @("--create", "--file", $Jar, "--main-class", "io.github.ignat22.textlab.App", "-C", "$Build/main", ".")
    Write-Host "Built $Jar"
}

function Run-Tests {
    Invoke-Checked java @("-cp", "$Build/main;$Build/test", "io.github.ignat22.textlab.testkit.TestLauncher")
}

switch ($Task) {
    "build" { Compile }
    "test"  { Compile; Run-Tests }
    "demo"  { Compile; Invoke-Checked java @("-jar", $Jar, "demo") }
    "bench" { Compile; Invoke-Checked java @("-jar", $Jar, "bench") }
    "all"   { Compile; Run-Tests; Invoke-Checked java @("-jar", $Jar, "demo"); Invoke-Checked java @("-jar", $Jar, "bench") }
    default { Write-Host "usage: .\lab.ps1 [build|test|demo|bench|all]"; exit 2 }
}
