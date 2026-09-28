$baseUrl = "http://localhost:8080"

function Assert-Equal($actual, $expected, $message) {
    if ($actual -ne $expected) {
        Write-Error "ASSERTION FAILED: $message. Expected: $expected, Actual: $actual"
        exit 1
    } else {
        Write-Host "PASS: $message" -ForegroundColor Green
    }
}

# 1. Create Organizer
$orgBody = @{ name = "Prof. Alan Turing"; email = "turing@college.edu"; department = "Computer Science" } | ConvertTo-Json
$org = Invoke-RestMethod -Uri "$baseUrl/api/organizers" -Method Post -ContentType "application/json" -Body $orgBody
Assert-Equal $org.name "Prof. Alan Turing" "Organizer created"

# 2. Create Students
$s1Body = @{ name = "Grace Hopper"; email = "grace@college.edu"; rollNumber = "CS101" } | ConvertTo-Json
$s1 = Invoke-RestMethod -Uri "$baseUrl/api/students" -Method Post -ContentType "application/json" -Body $s1Body
Assert-Equal $s1.name "Grace Hopper" "Student 1 created"

$s2Body = @{ name = "Ada Lovelace"; email = "ada@college.edu"; rollNumber = "CS102" } | ConvertTo-Json
$s2 = Invoke-RestMethod -Uri "$baseUrl/api/students" -Method Post -ContentType "application/json" -Body $s2Body
Assert-Equal $s2.name "Ada Lovelace" "Student 2 created"

$s3Body = @{ name = "Margaret Hamilton"; email = "margaret@college.edu"; rollNumber = "CS103" } | ConvertTo-Json
$s3 = Invoke-RestMethod -Uri "$baseUrl/api/students" -Method Post -ContentType "application/json" -Body $s3Body
Assert-Equal $s3.name "Margaret Hamilton" "Student 3 created"

# 3. Feature 1: Organizer creates an event with maxSeats = 2
$futureDate = (Get-Date).AddDays(14).ToString("yyyy-MM-dd")
$eventBody = @{ title = "Tech Symposium 2026"; eventDate = $futureDate; venue = "Auditorium A"; maxSeats = 2; organizerId = $org.id } | ConvertTo-Json
$event = Invoke-RestMethod -Uri "$baseUrl/api/events" -Method Post -ContentType "application/json" -Body $eventBody
Assert-Equal $event.title "Tech Symposium 2026" "Feature 1: Event created"
Assert-Equal $event.maxSeats 2 "Event maxSeats is 2"
Assert-Equal $event.availableSeats 2 "Available seats initially 2"

# 4. Feature 2: Student 1 registers for the event
$reg1Body = @{ studentId = $s1.id; eventId = $event.id } | ConvertTo-Json
$reg1 = Invoke-RestMethod -Uri "$baseUrl/api/registrations" -Method Post -ContentType "application/json" -Body $reg1Body
Assert-Equal $reg1.status "ACTIVE" "Feature 2: Student 1 registered"

# 5. Student 2 registers for the event (capacity now 2/2)
$reg2Body = @{ studentId = $s2.id; eventId = $event.id } | ConvertTo-Json
$reg2 = Invoke-RestMethod -Uri "$baseUrl/api/registrations" -Method Post -ContentType "application/json" -Body $reg2Body
Assert-Equal $reg2.status "ACTIVE" "Feature 2: Student 2 registered"

# 6. Feature 3: Automatic Capacity Closure - Student 3 registration rejected
try {
    $reg3Body = @{ studentId = $s3.id; eventId = $event.id } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/api/registrations" -Method Post -ContentType "application/json" -Body $reg3Body
    Write-Error "Feature 3 FAILED: Should have rejected registration beyond capacity"
    exit 1
} catch {
    $res = $_.Exception.Response
    $stream = $res.GetResponseStream()
    $reader = New-Object System.IO.StreamReader($stream)
    $errorJson = $reader.ReadToEnd() | ConvertFrom-Json
    Assert-Equal ([int]$res.StatusCode) 400 "Feature 3: Status 400 on capacity limit"
    Assert-Equal $errorJson.error "Event Full" "Feature 3: Error title is Event Full"
    Write-Host "Feature 3 message: $($errorJson.message)" -ForegroundColor Cyan
}

# 7. Edge case: Duplicate registration rejected
try {
    $duplicateBody = @{ studentId = $s1.id; eventId = $event.id } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/api/registrations" -Method Post -ContentType "application/json" -Body $duplicateBody
    Write-Error "Edge Case FAILED: Should have rejected duplicate registration"
    exit 1
} catch {
    $res = $_.Exception.Response
    Assert-Equal ([int]$res.StatusCode) 409 "Rule 3: Status 409 on duplicate registration"
}

# 8. Feature 4: Organizer views registered participants
$participants = Invoke-RestMethod -Uri "$baseUrl/api/events/$($event.id)/participants" -Method Get
Assert-Equal $participants.Count 2 "Feature 4: 2 participants retrieved"
Assert-Equal $participants[0].name "Grace Hopper" "Participant 1 is Grace Hopper"
Assert-Equal $participants[1].name "Ada Lovelace" "Participant 2 is Ada Lovelace"

# 9. Feature 5: Student 1 cancels registration
$cancelRes = Invoke-RestMethod -Uri "$baseUrl/api/registrations/$($reg1.id)/cancel?studentId=$($s1.id)" -Method Put
Assert-Equal $cancelRes.status "CANCELLED" "Feature 5: Registration cancelled"

# 10. Rule 2: Cancellation frees a seat - Student 3 can now register!
$reg3 = Invoke-RestMethod -Uri "$baseUrl/api/registrations" -Method Post -ContentType "application/json" -Body $reg3Body
Assert-Equal $reg3.status "ACTIVE" "Rule 2: Student 3 registered successfully after seat freed"

# 11. Verify updated participants list
$updatedParticipants = Invoke-RestMethod -Uri "$baseUrl/api/events/$($event.id)/participants" -Method Get
Assert-Equal $updatedParticipants.Count 2 "2 active participants remaining"
Assert-Equal $updatedParticipants[0].name "Ada Lovelace" "Active participant 1 is Ada Lovelace"
Assert-Equal $updatedParticipants[1].name "Margaret Hamilton" "Active participant 2 is Margaret Hamilton"

# 12. Edge Case: Non-existing event
try {
    $invalidEventBody = @{ studentId = $s1.id; eventId = 99999 } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/api/registrations" -Method Post -ContentType "application/json" -Body $invalidEventBody
    Write-Error "Failed: Non-existing event should return 404"
} catch {
    $res = $_.Exception.Response
    Assert-Equal ([int]$res.StatusCode) 404 "Non-existing event returns 404"
}

# 13. Edge Case: Non-existing student
try {
    $invalidStudentBody = @{ studentId = 99999; eventId = $event.id } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/api/registrations" -Method Post -ContentType "application/json" -Body $invalidStudentBody
    Write-Error "Failed: Non-existing student should return 404"
} catch {
    $res = $_.Exception.Response
    Assert-Equal ([int]$res.StatusCode) 404 "Non-existing student returns 404"
}

# 14. Edge Case: Cancel after or on event date
$pastDate = (Get-Date).ToString("yyyy-MM-dd") # today's event
$todayEventBody = @{ title = "Today Seminar"; eventDate = $pastDate; venue = "Lab 5"; maxSeats = 10; organizerId = $org.id } | ConvertTo-Json
$todayEvent = Invoke-RestMethod -Uri "$baseUrl/api/events" -Method Post -ContentType "application/json" -Body $todayEventBody

$todayRegBody = @{ studentId = $s1.id; eventId = $todayEvent.id } | ConvertTo-Json
$todayReg = Invoke-RestMethod -Uri "$baseUrl/api/registrations" -Method Post -ContentType "application/json" -Body $todayRegBody

try {
    Invoke-RestMethod -Uri "$baseUrl/api/registrations/$($todayReg.id)/cancel" -Method Put
    Write-Error "Failed: Cancel on or after event date should be rejected"
} catch {
    $res = $_.Exception.Response
    Assert-Equal ([int]$res.StatusCode) 400 "Rule 4: Cannot cancel on or after event date (400)"
}

Write-Host "`nALL LIVE END-TO-END TESTS PASSED ON MYSQL!" -ForegroundColor Green
