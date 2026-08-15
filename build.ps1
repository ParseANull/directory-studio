# build.ps1 — Build Apache Directory Studio with a daily sequential qualifier.
# Usage: .\build.ps1 [-SkipTests] [-Qualifier a]
# Without -Qualifier, the next letter for today is auto-selected from git tags.
param(
    [switch]$SkipTests,
    [string]$Qualifier
)

$mvn = "$env:USERPROFILE\tools\apache-maven-3.9.16\bin\mvn.cmd"
$today = Get-Date -Format "yyyy.M.d"   # e.g. 2026.8.14

if (-not $Qualifier) {
    $existing = git tag -l "$today.*" | Sort-Object
    if ($existing) {
        $lastLetter = ($existing | Select-Object -Last 1) -replace ".*\.", ""
        $nextOrdinal = [int][char]$lastLetter + 1
        if ($nextOrdinal -gt [int][char]'z') {
            Write-Error "Exhausted single-letter qualifiers for $today (past 'z'). Pass -Qualifier explicitly."
            exit 1
        }
        $Qualifier = [char]$nextOrdinal
    } else {
        $Qualifier = "a"
    }
}

$version = "$today.$Qualifier"
Write-Host "Building $version" -ForegroundColor Cyan

$mvnArgs = @("install", "-DforceContextQualifier=$Qualifier")
if ($SkipTests) { $mvnArgs += "-DskipTests" }

& $mvn @mvnArgs
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# Tag the successful build
git tag "$version"
Write-Host "Tagged $version" -ForegroundColor Green
