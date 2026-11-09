param([string]$BaseUrl = 'http://localhost:8080')

$ErrorActionPreference = 'Stop'
$endpoint = [Uri]$BaseUrl
if (-not $endpoint.IsLoopback -or $endpoint.Scheme -ne 'http') {
    throw 'Este script solo admite un backend local de desarrollo por HTTP.'
}
$BaseUrl = $BaseUrl.TrimEnd('/')
# Credencial publica de cuentas ficticias locales. Nunca usar con personas reales.
$demoPassword = 'SafeDiaryDemo2026!'
$profiles = @(
    @{ email='ana.torres.demo@example.test'; name='Demo - Ana Torres'; title='Psicologia clinica'; specialties=@('Ansiedad','Estres'); amount=80; start='09:00'; end='12:00' },
    @{ email='diego.ruiz.demo@example.test'; name='Demo - Diego Ruiz'; title='Psicologia de pareja'; specialties=@('Pareja','Comunicacion'); amount=100; start='14:00'; end='17:00' },
    @{ email='lucia.mendoza.demo@example.test'; name='Demo - Lucia Mendoza'; title='Psicologia infantil y adolescente'; specialties=@('Adolescentes','Familia'); amount=120; start='10:00'; end='13:00' },
    @{ email='carlos.vega.demo@example.test'; name='Demo - Carlos Vega'; title='Psicologia y bienestar emocional'; specialties=@('Autoestima','Duelo'); amount=90; start='16:00'; end='19:00' }
)

function Invoke-LocalApi($Method, $Path, $Headers = @{}, $Payload = $null) {
    $request = @{ Uri="$BaseUrl$Path"; Method=$Method; Headers=$Headers; TimeoutSec=30 }
    if ($null -ne $Payload) {
        $request.ContentType = 'application/json; charset=utf-8'
        $request.Body = [Text.Encoding]::UTF8.GetBytes((ConvertTo-Json -InputObject $Payload -Depth 8 -Compress))
    }
    Invoke-RestMethod @request
}

$result = foreach ($profile in $profiles) {
    try {
        $session = Invoke-LocalApi POST '/api/v1/authentication/sign-up' @{} @{
            email=$profile.email; password=$demoPassword; role='PSYCHOLOGIST'
        }
    } catch {
        if ([int]$_.Exception.Response.StatusCode -ne 409) { throw }
        $session = Invoke-LocalApi POST '/api/v1/authentication/sign-in' @{} @{
            email=$profile.email; password=$demoPassword
        }
    }
    $headers = @{ Authorization="Bearer $($session.accessToken)" }
    try { $own = Invoke-LocalApi GET '/api/v1/clinicians/me' $headers }
    catch {
        if ([int]$_.Exception.Response.StatusCode -ne 404) { throw }
        $own = Invoke-LocalApi POST '/api/v1/clinicians' $headers @{
            displayName=$profile.name; professionalTitle=$profile.title
            bio='Perfil ficticio de prueba de SafeDiary. No ofrece atencion real.'
            specialties=$profile.specialties; amount=$profile.amount; currency='PEN'; durationMinutes=50
        }
    }
    if ($own.verificationStatus -ne 'APPROVED') {
        $verification = Invoke-LocalApi POST "/api/v1/clinicians/$($own.id)/verification-requests" $headers @{
            licenseNumber="DEMO-NO-VALIDA-$($own.id)"; specialty=$profile.specialties[0]
            documentRef="private://demo/local-only/clinician-$($own.id)"
        }
        # Mecanismo existente del perfil dev; no crea ni modifica administradores reales.
        $devAdmin = @{ 'X-Account-Id'='900000001'; 'X-Role'='ADMIN' }
        $null = Invoke-LocalApi PATCH "/api/v1/verification-requests/$($verification.id)/decision" $devAdmin @{
            decision='APPROVED'; reason='Simulacion local para un perfil ficticio de prueba.'
        }
    }
    if ($own.publicationStatus -ne 'PUBLISHED') {
        $null = Invoke-LocalApi POST "/api/v1/clinicians/$($own.id)/publication" $headers
    }
    $windows = @('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY') | ForEach-Object {
        @{ dayOfWeek=$_; startTime=$profile.start; endTime=$profile.end }
    }
    $null = Invoke-LocalApi PUT '/api/v1/schedule/availability' $headers $windows
    $published = Invoke-LocalApi GET "/api/v1/clinicians/$($own.id)" $headers
    if ($published.verificationStatus -ne 'APPROVED' -or $published.publicationStatus -ne 'PUBLISHED') {
        throw "El perfil de prueba $($profile.email) no quedo disponible."
    }
    $tomorrow = [DateTime]::UtcNow.AddHours(-5).Date.AddDays(1)
    if ($tomorrow.DayOfWeek -eq 'Sunday') { $tomorrow = $tomorrow.AddDays(1) }
    $slots = Invoke-LocalApi GET "/api/v1/schedule/clinicians/$($own.id)/slots?date=$($tomorrow.ToString('yyyy-MM-dd'))" $headers
    if ($slots.Count -eq 0) { throw "El perfil $($own.id) no tiene horarios disponibles." }
    [PSCustomObject]@{ Id=$own.id; Nombre=$published.displayName; Correo=$profile.email; Tarifa="S/ $($profile.amount)"; Horarios=$slots.Count }
}
$result | Format-Table -AutoSize
