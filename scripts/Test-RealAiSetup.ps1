# Isolated launcher regression checks: fake keys and fake HTTP responses only.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$sandboxRoot = Join-Path $projectRoot ('target/ai-setup-tests/' + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path (Join-Path $sandboxRoot 'scripts') -Force | Out-Null
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'Enable-RealAi.ps1') -Destination (Join-Path $sandboxRoot 'scripts/Enable-RealAi.ps1')
[IO.File]::WriteAllText((Join-Path $sandboxRoot '.gitignore'), "/config/`n")
& git -C $sandboxRoot init --quiet
if ($LASTEXITCODE -ne 0) { throw 'Could not initialize the isolated test repository.' }
$sandboxScript = Join-Path $sandboxRoot 'scripts/Enable-RealAi.ps1'
$sandboxConfig = Join-Path $sandboxRoot 'config/application.properties'
$global:SafeDiaryAiSetupCalls = 0
$global:SafeDiaryAiSetupFail = $false
$global:SafeDiaryAiSetupKeyReads = 0

function global:Read-Host {
    param([string]$Prompt, [switch]$AsSecureString)
    $global:SafeDiaryAiSetupKeyReads++
    ConvertTo-SecureString 'fake-test-key-never-use-with-a-real-provider' -AsPlainText -Force
}

function global:Invoke-RestMethod {
    param([string]$Uri, [string]$Method, [hashtable]$Headers, [string]$ContentType, [object]$Body, [int]$TimeoutSec)
    $global:SafeDiaryAiSetupCalls++
    if ($global:SafeDiaryAiSetupFail) { throw [Net.WebException]::new('Simulated provider rejection') }
    if ($Uri -match 'generativelanguage.googleapis.com.*?/models$') {
        return [pscustomobject]@{ models = @([pscustomobject]@{ name = 'models/gemini-3.5-flash-lite'; supportedGenerationMethods = @('generateContent') }) }
    }
    if ($Uri -match 'generativelanguage.googleapis.com.*?:generateContent$') {
        return [pscustomobject]@{ candidates = @([pscustomobject]@{ content = [pscustomobject]@{ parts = @([pscustomobject]@{ text = 'LISTO' }) } }) }
    }
    if ($Uri -eq 'https://api.groq.com/openai/v1/models') {
        return [pscustomobject]@{ data = @([pscustomobject]@{ id = 'openai/gpt-oss-20b'; active = $true }) }
    }
    if ($Uri -eq 'https://api.groq.com/openai/v1/chat/completions') {
        return [pscustomobject]@{ choices = @([pscustomobject]@{ message = [pscustomobject]@{ content = 'LISTO' } }) }
    }
    throw 'Unexpected HTTP request in the setup test.'
}

function Assert-Setup([bool]$Condition, [string]$Message) {
    if (!$Condition) { throw $Message }
}

try {
    & $sandboxScript -Check | Out-Null
    Assert-Setup ($global:SafeDiaryAiSetupCalls -eq 0 -and $global:SafeDiaryAiSetupKeyReads -eq 0) 'Check mode requested a key or made a network call.'
    Assert-Setup (!(Test-Path -LiteralPath $sandboxConfig)) 'Check mode created a configuration file.'
    Write-Output 'PASS: readiness check has no side effects.'

    & $sandboxScript -ConfigureOnly -Provider Gemini | Out-Null
    $geminiConfig = [IO.File]::ReadAllText($sandboxConfig)
    Assert-Setup ($geminiConfig -match 'assistantai.llm.provider=spring-ai') 'Real provider was not enabled.'
    Assert-Setup ($geminiConfig -match 'spring.ai.model.chat=google-genai') 'Gemini routing is missing.'
    Assert-Setup ($geminiConfig -match 'spring.ai.google.genai.chat.model=gemini-3.5-flash-lite') 'Selected model is missing.'
    Assert-Setup ($global:SafeDiaryAiSetupCalls -eq 2) 'Expected model listing and a probe before saving.'
    Write-Output 'PASS: Gemini is saved only after a successful model query and reply.'

    [IO.File]::AppendAllText($sandboxConfig, "custom.setting=preserved`n")
    & $sandboxScript -ConfigureOnly -Provider Groq | Out-Null
    $groqConfig = [IO.File]::ReadAllText($sandboxConfig)
    Assert-Setup ($groqConfig -match 'assistantai.llm.backup.model=openai/gpt-oss-20b') 'Groq model is missing.'
    Assert-Setup ($groqConfig -match 'spring.ai.model.chat=none') 'Unused primary model was not disabled.'
    Assert-Setup ($groqConfig -match 'custom.setting=preserved') 'An unrelated local property was lost.'
    Assert-Setup ((@([regex]::Matches($groqConfig, '(?m)^assistantai.llm.provider=')).Count) -eq 1) 'Provider property was duplicated.'
    Write-Output 'PASS: Groq configuration preserves existing local properties.'

    $global:SafeDiaryAiSetupFail = $true
    $caught = $false
    try { & $sandboxScript -ConfigureOnly | Out-Null } catch { $caught = $true }
    Assert-Setup $caught 'Provider failure was not propagated.'
    Assert-Setup ([IO.File]::ReadAllText($sandboxConfig) -eq $groqConfig) 'Provider failure changed the existing configuration.'
    Write-Output 'PASS: failed credentials leave existing configuration untouched.'

    $global:SafeDiaryAiSetupFail = $false
    $caught = $false
    try { & $sandboxScript -ConfigureOnly -Model 'missing-model' | Out-Null } catch { $caught = $true }
    Assert-Setup $caught 'An unavailable model was accepted.'
    Assert-Setup ([IO.File]::ReadAllText($sandboxConfig) -eq $groqConfig) 'An unavailable model changed the existing configuration.'
    Write-Output 'PASS: unavailable models are rejected before writing.'

    & git -C $sandboxRoot add -f -- config/application.properties
    $readsBefore = $global:SafeDiaryAiSetupKeyReads
    $caught = $false
    try { & $sandboxScript -ConfigureOnly | Out-Null } catch { $caught = $true }
    Assert-Setup $caught 'Tracked configuration was accepted for saving a secret.'
    Assert-Setup ($global:SafeDiaryAiSetupKeyReads -eq $readsBefore) 'Tracked configuration was detected after reading the key.'
    Write-Output 'PASS: tracked configuration is rejected before requesting a key.'
} finally {
    Remove-Item Function:\Read-Host -ErrorAction SilentlyContinue
    Remove-Item Function:\Invoke-RestMethod -ErrorAction SilentlyContinue
    Remove-Variable SafeDiaryAiSetupCalls,SafeDiaryAiSetupFail,SafeDiaryAiSetupKeyReads -Scope Global -ErrorAction SilentlyContinue
}
