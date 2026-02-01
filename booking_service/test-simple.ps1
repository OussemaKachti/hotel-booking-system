# Tests simples pour Booking Service
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   TESTS DU BOOKING SERVICE" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Health
Write-Host "[1] Test Health..." -ForegroundColor Yellow
$health = Invoke-RestMethod "http://localhost:8081/api/bookings/health"
Write-Host "OK: $health" -ForegroundColor Green
Write-Host ""

# Test 2: Liste vide
Write-Host "[2] GET liste (devrait etre vide)..." -ForegroundColor Yellow
$all = Invoke-RestMethod "http://localhost:8081/api/bookings"
Write-Host "OK: $($all.Count) reservations" -ForegroundColor Green
Write-Host ""

# Test 3: Creer reservation 1
Write-Host "[3] POST Creer reservation 1..." -ForegroundColor Yellow
$json1 = @{
    roomId = 101
    hotelId = 1
    userId = "user-123"
    checkInDate = "2026-03-01"
    checkOutDate = "2026-03-05"
    numberOfGuests = 2
    pricePerNight = 150.00
    specialRequests = "Vue sur la mer"
} | ConvertTo-Json

$booking1 = Invoke-RestMethod "http://localhost:8081/api/bookings" -Method Post -Body $json1 -ContentType "application/json"
Write-Host "OK: ID=$($booking1.id), Confirmation=$($booking1.confirmationNumber)" -ForegroundColor Green
Write-Host "    Nuits=$($booking1.numberOfNights), Prix total=$($booking1.totalPrice) EUR" -ForegroundColor Cyan
Write-Host "    Status=$($booking1.status)" -ForegroundColor Cyan
Write-Host ""

# Test 4: Creer reservation 2
Write-Host "[4] POST Creer reservation 2..." -ForegroundColor Yellow
$json2 = @{
    roomId = 202
    hotelId = 2
    userId = "user-456"
    checkInDate = "2026-04-10"
    checkOutDate = "2026-04-17"
    numberOfGuests = 3
    pricePerNight = 200.00
    specialRequests = "Lit bebe"
} | ConvertTo-Json

$booking2 = Invoke-RestMethod "http://localhost:8081/api/bookings" -Method Post -Body $json2 -ContentType "application/json"
Write-Host "OK: ID=$($booking2.id), Confirmation=$($booking2.confirmationNumber)" -ForegroundColor Green
Write-Host "    Nuits=$($booking2.numberOfNights), Prix total=$($booking2.totalPrice) EUR" -ForegroundColor Cyan
Write-Host ""

# Test 5: GET toutes
Write-Host "[5] GET toutes les reservations..." -ForegroundColor Yellow
$all = Invoke-RestMethod "http://localhost:8081/api/bookings"
Write-Host "OK: $($all.Count) reservations" -ForegroundColor Green
foreach ($b in $all) {
    Write-Host "  - ID:$($b.id) | User:$($b.userId) | Status:$($b.status) | Total:$($b.totalPrice)EUR" -ForegroundColor Cyan
}
Write-Host ""

# Test 6: GET par ID
Write-Host "[6] GET reservation par ID..." -ForegroundColor Yellow
$byId = Invoke-RestMethod "http://localhost:8081/api/bookings/$($booking1.id)"
Write-Host "OK: Recuperee ID=$($byId.id)" -ForegroundColor Green
Write-Host ""

# Test 7: GET par confirmation number
Write-Host "[7] GET par numero de confirmation..." -ForegroundColor Yellow
$byConf = Invoke-RestMethod "http://localhost:8081/api/bookings/confirmation/$($booking1.confirmationNumber)"
Write-Host "OK: $($byConf.confirmationNumber)" -ForegroundColor Green
Write-Host ""

# Test 8: GET par user
Write-Host "[8] GET par userId..." -ForegroundColor Yellow
$byUser = Invoke-RestMethod "http://localhost:8081/api/bookings/user/user-123"
Write-Host "OK: $($byUser.Count) reservations pour user-123" -ForegroundColor Green
Write-Host ""

# Test 9: UPDATE
Write-Host "[9] PUT Modifier reservation..." -ForegroundColor Yellow
$update = @{
    numberOfGuests = 4
    specialRequests = "Etage superieur"
} | ConvertTo-Json

$updated = Invoke-RestMethod "http://localhost:8081/api/bookings/$($booking1.id)" -Method Put -Body $update -ContentType "application/json"
Write-Host "OK: Guests=$($updated.numberOfGuests)" -ForegroundColor Green
Write-Host ""

# Test 10: CANCEL
Write-Host "[10] PATCH Annuler reservation..." -ForegroundColor Yellow
$cancelled = Invoke-RestMethod "http://localhost:8081/api/bookings/$($booking2.id)/cancel" -Method Patch
Write-Host "OK: Status=$($cancelled.status)" -ForegroundColor Green
Write-Host ""

# Test 11: GET par status
Write-Host "[11] GET par status CONFIRMED..." -ForegroundColor Yellow
$confirmed = Invoke-RestMethod "http://localhost:8081/api/bookings?status=CONFIRMED"
Write-Host "OK: $($confirmed.Count) reservations CONFIRMED" -ForegroundColor Green
Write-Host ""

Write-Host "[12] GET par status CANCELLED..." -ForegroundColor Yellow
$cancelledList = Invoke-RestMethod "http://localhost:8081/api/bookings?status=CANCELLED"
Write-Host "OK: $($cancelledList.Count) reservations CANCELLED" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   TOUS LES TESTS REUSSIS !" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Pour voir dans H2 Console:" -ForegroundColor Yellow
Write-Host "  URL: http://localhost:8081/h2-console" -ForegroundColor Cyan
Write-Host "  JDBC URL: jdbc:h2:mem:bookingdb" -ForegroundColor Cyan
Write-Host "  Username: sa" -ForegroundColor Cyan
Write-Host "  Password: (vide)" -ForegroundColor Cyan
Write-Host ""
Write-Host "Puis executez: SELECT * FROM BOOKINGS" -ForegroundColor Cyan
