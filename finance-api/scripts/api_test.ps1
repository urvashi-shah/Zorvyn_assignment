$ErrorActionPreference = "Stop"

$BaseUrl = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:8080" }
$AdminEmail = if ($env:ADMIN_EMAIL) { $env:ADMIN_EMAIL } else { "admin@zorvyn.com" }
$AdminPassword = if ($env:ADMIN_PASSWORD) { $env:ADMIN_PASSWORD } else { "admin123" }

$script:Pass = 0
$script:Total = 0

function Assert-Status {
    param(
        [int]$Actual,
        [int]$Expected,
        [string]$Name
    )
    $script:Total++
    if ($Actual -eq $Expected) {
        $script:Pass++
        Write-Host "PASS: $Name (expected $Expected, got $Actual)"
    } else {
        throw "FAIL: $Name (expected $Expected, got $Actual)"
    }
}

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Url,
        $Body,
        [string]$Token
    )

    $headers = @{ "Content-Type" = "application/json" }
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }

    try {
        if ($null -ne $Body) {
            $response = Invoke-WebRequest -Method $Method -Uri $Url -Headers $headers -Body ($Body | ConvertTo-Json -Depth 10) -UseBasicParsing
        } else {
            $response = Invoke-WebRequest -Method $Method -Uri $Url -Headers $headers -UseBasicParsing
        }
        return @{ Status = [int]$response.StatusCode; Body = ($response.Content | ConvertFrom-Json) }
    } catch {
        if ($_.Exception.Response) {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $content = $reader.ReadToEnd()
            $status = [int]$_.Exception.Response.StatusCode.value__
            $body = $null
            if ($content) {
                try { $body = $content | ConvertFrom-Json } catch { $body = $content }
            }
            return @{ Status = $status; Body = $body }
        }
        throw
    }
}

Write-Host "`n[AUTH] Admin login"
$res = Invoke-Api -Method "POST" -Url "$BaseUrl/api/auth/login" -Body @{ email = $AdminEmail; password = $AdminPassword }
Assert-Status -Actual $res.Status -Expected 200 -Name "Admin login should succeed"
$adminToken = $res.Body.token

Write-Host "`n[AUTH] Invalid login"
$res = Invoke-Api -Method "POST" -Url "$BaseUrl/api/auth/login" -Body @{ email = $AdminEmail; password = "wrong123" }
Assert-Status -Actual $res.Status -Expected 401 -Name "Invalid login should return 401"

Write-Host "`n[USERS] Create analyst/viewer users"
$res = Invoke-Api -Method "POST" -Url "$BaseUrl/api/users" -Token $adminToken -Body @{ email = "analyst@zorvyn.com"; password = "analyst123"; role = "ANALYST" }
if (($res.Status -ne 200) -and ($res.Status -ne 201) -and ($res.Status -ne 400)) { throw "Unexpected status for analyst create: $($res.Status)" }
$script:Pass++; $script:Total++
Write-Host "PASS: Create analyst accepted (or already exists)"

$res = Invoke-Api -Method "POST" -Url "$BaseUrl/api/users" -Token $adminToken -Body @{ email = "viewer@zorvyn.com"; password = "viewer123"; role = "VIEWER" }
if (($res.Status -ne 200) -and ($res.Status -ne 201) -and ($res.Status -ne 400)) { throw "Unexpected status for viewer create: $($res.Status)" }
$script:Pass++; $script:Total++
Write-Host "PASS: Create viewer accepted (or already exists)"

Write-Host "`n[AUTH] Analyst/viewer login"
$res = Invoke-Api -Method "POST" -Url "$BaseUrl/api/auth/login" -Body @{ email = "analyst@zorvyn.com"; password = "analyst123" }
Assert-Status -Actual $res.Status -Expected 200 -Name "Analyst login should succeed"
$analystToken = $res.Body.token

$res = Invoke-Api -Method "POST" -Url "$BaseUrl/api/auth/login" -Body @{ email = "viewer@zorvyn.com"; password = "viewer123" }
Assert-Status -Actual $res.Status -Expected 200 -Name "Viewer login should succeed"
$viewerToken = $res.Body.token

Write-Host "`n[RECORDS] Create/list permissions"
$res = Invoke-Api -Method "POST" -Url "$BaseUrl/api/records" -Token $adminToken -Body @{
    amount = 5000
    type = "INCOME"
    category = "Salary"
    date = "2026-04-01"
    notes = "April Salary"
}
Assert-Status -Actual $res.Status -Expected 200 -Name "Admin can create record"
$recordId = $res.Body.id

$res = Invoke-Api -Method "GET" -Url "$BaseUrl/api/records" -Token $analystToken
Assert-Status -Actual $res.Status -Expected 200 -Name "Analyst can list records"

$res = Invoke-Api -Method "GET" -Url "$BaseUrl/api/records" -Token $viewerToken
Assert-Status -Actual $res.Status -Expected 403 -Name "Viewer cannot list records"

Write-Host "`n[RBAC] Analyst cannot create"
$res = Invoke-Api -Method "POST" -Url "$BaseUrl/api/records" -Token $analystToken -Body @{
    amount = 99
    type = "EXPENSE"
    category = "Food"
    date = "2026-04-03"
    notes = "Lunch"
}
Assert-Status -Actual $res.Status -Expected 403 -Name "Analyst create blocked"

Write-Host "`n[VALIDATION] Negative amount rejected"
$res = Invoke-Api -Method "POST" -Url "$BaseUrl/api/records" -Token $adminToken -Body @{
    amount = -5
    type = "EXPENSE"
    category = "Food"
    date = "2026-04-03"
    notes = "Invalid"
}
Assert-Status -Actual $res.Status -Expected 400 -Name "Negative amount should return 400"

Write-Host "`n[FILTERS] Category and type"
$res = Invoke-Api -Method "GET" -Url "$BaseUrl/api/records?category=Salary" -Token $analystToken
Assert-Status -Actual $res.Status -Expected 200 -Name "Category filter works"
if ($res.Body.Count -lt 1) { throw "FAIL: Expected at least one Salary record" }
$script:Pass++; $script:Total++
Write-Host "PASS: Category filter returned records"

$res = Invoke-Api -Method "GET" -Url "$BaseUrl/api/records?type=INCOME" -Token $analystToken
Assert-Status -Actual $res.Status -Expected 200 -Name "Type filter works"

Write-Host "`n[DASHBOARD] Visibility"
$res = Invoke-Api -Method "GET" -Url "$BaseUrl/api/dashboard/summary" -Token $viewerToken
Assert-Status -Actual $res.Status -Expected 200 -Name "Viewer can read summary"

$res = Invoke-Api -Method "GET" -Url "$BaseUrl/api/dashboard/categories" -Token $analystToken
Assert-Status -Actual $res.Status -Expected 200 -Name "Analyst can read categories"

$res = Invoke-Api -Method "GET" -Url "$BaseUrl/api/dashboard/trends" -Token $adminToken
Assert-Status -Actual $res.Status -Expected 200 -Name "Admin can read trends"

$res = Invoke-Api -Method "GET" -Url "$BaseUrl/api/dashboard/recent" -Token $adminToken
Assert-Status -Actual $res.Status -Expected 200 -Name "Admin can read recent"

Write-Host "`n[RECORDS] Delete"
$res = Invoke-Api -Method "DELETE" -Url "$BaseUrl/api/records/$recordId" -Token $adminToken
Assert-Status -Actual $res.Status -Expected 200 -Name "Admin can delete"

Write-Host "`n[SECURITY] Missing token"
$res = Invoke-Api -Method "GET" -Url "$BaseUrl/api/dashboard/summary"
Assert-Status -Actual $res.Status -Expected 401 -Name "No token should return 401"

Write-Host "`n----------------------------------------"
Write-Host "API TEST RESULT: $script:Pass / $script:Total checks passed"
Write-Host "All core assignment scenarios are working."
Write-Host "----------------------------------------"
