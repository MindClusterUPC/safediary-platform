# Run from PowerShell. Secrets are entered locally, never as command-line arguments.
[CmdletBinding()]
param(
    [ValidateSet('Gemini', 'Groq')][string]$Provider = 'Gemini',
    [string]$Model,
    [switch]$Check,
    [switch]$ConfigureOnly
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$configPath = Join-Path $projectRoot 'config/application.properties'
& git -C $projectRoot check-ignore --quiet -- config/application.properties
if ($LASTEXITCODE -ne 0) { throw 'config/application.properties must be ignored by Git before storing a key.' }
if (@(& git -C $projectRoot ls-files -- config/application.properties).Count -ne 0) {
    throw 'The local configuration is tracked by Git. No key has been saved.'
}

if ($Check) {
    Write-Output 'Ready: local AI configuration is excluded from Git.'
    Write-Output ('Key creation: ' + $(if ($Provider -eq 'Gemini') { 'https://aistudio.google.com/apikey' } else { 'https://console.groq.com/keys' }))
    Write-Output 'Run again without -Check after creating your provider key.'
    return
}

function Invoke-AiApi([string]$Uri, [hashtable]$Headers, [string]$Body) {
    try {
        if ($Body) {
            return Invoke-RestMethod -Uri $Uri -Method Post -Headers $Headers -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($Body)) -TimeoutSec 60
        }
        return Invoke-RestMethod -Uri $Uri -Headers $Headers -TimeoutSec 30
    } catch {
        $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 'network' }
        throw "Provider request failed ($status). Check the key, available quota and model permissions. Existing configuration was not changed."
    }
}

function Select-AiModel([string[]]$Available, [string[]]$Preferred) {
    if ($Model) {
        if ($Model -notin $Available) { throw 'The requested model is not available for this key.' }
        return $Model
    }
    foreach ($candidate in $Preferred) {
        if ($candidate -in $Available) { return $candidate }
    }
    if ($Provider -eq 'Gemini') {
        $candidate = $Available | Where-Object { $_ -match '^gemini-.*flash' -and $_ -notmatch 'image|audio|tts|live|robotics|computer' } | Sort-Object -Descending | Select-Object -First 1
        if ($candidate) { return $candidate }
    }
    throw 'No preferred text model is available. Run with -Model and an enabled model from your provider console.'
}

$secureKey = Read-Host "Paste your $Provider API key (input is hidden)" -AsSecureString
$keyBuffer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureKey)
try { $key = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($keyBuffer).Trim() }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($keyBuffer) }
if ([string]::IsNullOrWhiteSpace($key) -or $key -match '\s') { throw 'Enter a key without spaces or line breaks.' }

Write-Output 'Validating the key and a short test reply with the provider...'
$properties = [ordered]@{
    'assistantai.llm.provider' = 'spring-ai'
    'assistantai.llm.spring-ai.fallback-model' = ''
}
if ($Provider -eq 'Gemini') {
    $headers = @{ 'x-goog-api-key' = $key }
    $catalog = Invoke-AiApi 'https://generativelanguage.googleapis.com/v1beta/models' $headers
    $available = @($catalog.models | Where-Object { 'generateContent' -in $_.supportedGenerationMethods } | ForEach-Object { $_.name -replace '^models/', '' })
    $selected = Select-AiModel $available @('gemini-3.5-flash-lite', 'gemini-3.5-flash', 'gemini-2.5-flash')
    $probe = @{ contents = @(@{ parts = @(@{ text = 'Responde solo con la palabra LISTO.' }) }); generationConfig = @{ maxOutputTokens = 512 } } | ConvertTo-Json -Depth 8
    $response = Invoke-AiApi "https://generativelanguage.googleapis.com/v1beta/models/${selected}:generateContent" $headers $probe
    $reply = ($response.candidates[0].content.parts | ForEach-Object { $_.text }) -join ''
    $properties['spring.ai.model.chat'] = 'google-genai'
    $properties['spring.ai.google.genai.api-key'] = $key
    $properties['spring.ai.google.genai.chat.model'] = $selected
} else {
    $headers = @{ Authorization = "Bearer $key" }
    $catalog = Invoke-AiApi 'https://api.groq.com/openai/v1/models' $headers
    $available = @($catalog.data | Where-Object { $_.active -ne $false } | ForEach-Object { $_.id })
    $selected = Select-AiModel $available @('openai/gpt-oss-20b', 'qwen/qwen3.8-27b', 'llama-3.3-70b-versatile')
    $probe = @{ model = $selected; messages = @(@{ role = 'user'; content = 'Responde solo con la palabra LISTO.' }); max_completion_tokens = 512 } | ConvertTo-Json -Depth 8
    $response = Invoke-AiApi 'https://api.groq.com/openai/v1/chat/completions' $headers $probe
    $reply = $response.choices[0].message.content
    $properties['spring.ai.model.chat'] = 'none'
    $properties['assistantai.llm.backup.api-key'] = $key
    $properties['assistantai.llm.backup.base-url'] = 'https://api.groq.com/openai/v1'
    $properties['assistantai.llm.backup.model'] = $selected
}
if ([string]::IsNullOrWhiteSpace($reply)) { throw 'The provider returned no text. Existing configuration was not changed.' }
Write-Output "Provider verified. Model: $selected."

$backendProcesses = @()
if (!$ConfigureOnly) {
    $listeners = @(Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue)
    foreach ($backendPid in @($listeners.OwningProcess | Sort-Object -Unique)) {
        $backendProcess = Get-CimInstance Win32_Process -Filter "ProcessId=$backendPid"
        if (!$backendProcess -or $backendProcess.Name -notmatch '^java(w)?\.exe$' -or $backendProcess.CommandLine -notmatch 'com\.mindcluster\.safediary\..*Application|safediary-platform') {
            throw 'Port 8080 belongs to another process. Use -ConfigureOnly; no process was stopped.'
        }
        $backendProcesses += $backendProcess
    }
    $javaCandidates = @(@('C:\Program Files\Java\jdk-21', $env:JAVA_HOME, 'C:\Program Files\Android\Android Studio\jbr') | Where-Object {
        if (!$_ -or !(Test-Path -LiteralPath (Join-Path $_ 'bin/java.exe'))) { return $false }
        $releasePath = Join-Path $_ 'release'
        if (!(Test-Path -LiteralPath $releasePath)) { return $false }
        $versionLine = Get-Content -LiteralPath $releasePath | Where-Object { $_ -match '^JAVA_VERSION="(?<major>\d+)' } | Select-Object -First 1
        return $versionLine -and $versionLine -match '^JAVA_VERSION="(?<major>\d+)' -and [int]$Matches.major -ge 21
    })
    if (!$javaCandidates) { throw 'A JDK 21 or newer is required. Set JAVA_HOME, then run again.' }
    $env:JAVA_HOME = $javaCandidates[0]
}

$configDirectory = Split-Path -Parent $configPath
New-Item -ItemType Directory -Path $configDirectory -Force | Out-Null
$lines = [Collections.Generic.List[string]]::new()
if (Test-Path -LiteralPath $configPath) {
    $lines.AddRange([string[]][IO.File]::ReadAllLines($configPath))
    Copy-Item -LiteralPath $configPath -Destination ($configPath + '.backup-' + [DateTime]::Now.ToString('yyyyMMdd-HHmmss'))
}
foreach ($entry in $properties.GetEnumerator()) {
    $pattern = '^\s*' + [regex]::Escape($entry.Key) + '\s*[:=]'
    $replacement = $entry.Key + '=' + ([string]$entry.Value).Replace('\', '\\')
    $found = $false
    for ($index = $lines.Count - 1; $index -ge 0; $index--) {
        if ($lines[$index] -match $pattern) {
            if ($found) { $lines.RemoveAt($index) } else { $lines[$index] = $replacement; $found = $true }
        }
    }
    if (!$found) { $lines.Add($replacement) }
}
[IO.File]::WriteAllLines($configPath, $lines, [Text.UTF8Encoding]::new($false))
$key = $null
$secureKey.Dispose()
Write-Output 'Real AI configuration saved locally and excluded from Git.'
if ($ConfigureOnly) { Write-Output 'Restart the backend with ASSISTANTAI_LLM_PROVIDER=spring-ai to apply it.'; return }

foreach ($backendProcess in $backendProcesses) {
    Stop-Process -Id $backendProcess.ProcessId
    Wait-Process -Id $backendProcess.ProcessId -Timeout 20 -ErrorAction SilentlyContinue
}
$env:ASSISTANTAI_LLM_PROVIDER = 'spring-ai'
$env:SPRING_AI_CHAT_MODEL = $properties['spring.ai.model.chat']
New-Item -ItemType Directory -Path (Join-Path $projectRoot 'target') -Force | Out-Null
$run = Start-Process -FilePath $env:ComSpec -ArgumentList @('/d', '/c', 'mvnw.cmd spring-boot:run') -WorkingDirectory $projectRoot -WindowStyle Hidden -RedirectStandardOutput (Join-Path $projectRoot 'target/real-ai-backend.log') -RedirectStandardError (Join-Path $projectRoot 'target/real-ai-backend-error.log') -PassThru
Write-Output "Starting SafeDiary backend (launcher PID $($run.Id))..."
$deadline = [DateTime]::UtcNow.AddMinutes(3)
while ([DateTime]::UtcNow -lt $deadline) {
    Start-Sleep -Seconds 2
    try {
        $null = Invoke-RestMethod 'http://localhost:8080/v3/api-docs' -TimeoutSec 3
        Write-Output 'Backend ready with real AI. Open a new Diarito conversation.'
        Write-Output 'If the backend used a temporary signing key, sign in again after this restart.'
        return
    } catch {
        if ($run.HasExited) { break }
    }
}
throw 'Backend startup did not finish. Inspect target/real-ai-backend.log; the API key was not printed.'
