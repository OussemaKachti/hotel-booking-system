# Script de test pour le Booking Service
# Assurez-vous que le service tourne sur http://localhost:8081

Write-Host "=== Test du Booking Service ===" -ForegroundColor Green
Write-Host ""

# Test 1: Health Check
Write-Host "Test 1: Health Check" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings/health" -Method Get
    Write-Host "✓ Health check OK: $response" -ForegroundColor Green
} catch {
    Write-Host "✗ Health check failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 2: GET toutes les réservations (devrait être vide)
Write-Host "Test 2: GET toutes les reservations (vide au debut)" -ForegroundColor Yellow
try {
    $bookings = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings" -Method Get
    Write-Host "✓ Nombre de reservations: $($bookings.Count)" -ForegroundColor Green
} catch {
    Write-Host "✗ Erreur: $_" -ForegroundColor Red
}
Write-Host ""

# Test 3: Créer une réservation
Write-Host "Test 3: POST Creer une reservation" -ForegroundColor Yellow
$bookingRequest = @{
    roomId = 1
    hotelId = 1
    userId = "user-123"
    checkInDate = "2026-03-01"
    checkOutDate = "2026-03-05"
    numberOfGuests = 2
    pricePerNight = 150.00
    specialRequests = "Vue sur la mer"
} | ConvertTo-Json

try {
    $newBooking = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings" `
        -Method Post `
        -Body $bookingRequest `
        -ContentType "application/json"
    
    Write-Host "✓ Reservation creee avec succes!" -ForegroundColor Green
    Write-Host "  ID: $($newBooking.id)" -ForegroundColor Cyan
    Write-Host "  Numero de confirmation: $($newBooking.confirmationNumber)" -ForegroundColor Cyan
    Write-Host "  Nombre de nuits: $($newBooking.numberOfNights)" -ForegroundColor Cyan
    Write-Host "  Prix total: $($newBooking.totalPrice) EUR" -ForegroundColor Cyan
    Write-Host "  Status: $($newBooking.status)" -ForegroundColor Cyan
    
    $bookingId = $newBooking.id
    $confirmationNumber = $newBooking.confirmationNumber
} catch {
    Write-Host "✗ Erreur: $_" -ForegroundColor Red
    Write-Host $_.Exception.Response.StatusCode
}
Write-Host ""

# Test 4: GET la réservation par ID
if ($bookingId) {
    Write-Host "Test 4: GET reservation par ID" -ForegroundColor Yellow
    try {
        $booking = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings/$bookingId" -Method Get
        Write-Host "✓ Reservation recuperee: ID=$($booking.id), Confirmation=$($booking.confirmationNumber)" -ForegroundColor Green
    } catch {
        Write-Host "✗ Erreur: $_" -ForegroundColor Red
    }
    Write-Host ""
}

# Test 5: GET par numéro de confirmation
if ($confirmationNumber) {
    Write-Host "Test 5: GET par numero de confirmation" -ForegroundColor Yellow
    try {
        $booking = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings/confirmation/$confirmationNumber" -Method Get
        Write-Host "✓ Reservation trouvee: $($booking.confirmationNumber)" -ForegroundColor Green
    } catch {
        Write-Host "✗ Erreur: $_" -ForegroundColor Red
    }
    Write-Host ""
}

# Test 6: GET réservations par utilisateur
Write-Host "Test 6: GET reservations par utilisateur" -ForegroundColor Yellow
try {
    $userBookings = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings/user/user-123" -Method Get
    Write-Host "✓ Nombre de reservations pour user-123: $($userBookings.Count)" -ForegroundColor Green
} catch {
    Write-Host "✗ Erreur: $_" -ForegroundColor Red
}
Write-Host ""

# Test 7: Créer une deuxième réservation
Write-Host "Test 7: POST Creer une 2eme reservation" -ForegroundColor Yellow
$bookingRequest2 = @{
    roomId = 2
    hotelId = 1
    userId = "user-456"
    checkInDate = "2026-04-10"
    checkOutDate = "2026-04-15"
    numberOfGuests = 3
    pricePerNight = 200.00
    specialRequests = "Lit bebe requis"
} | ConvertTo-Json

try {
    $newBooking2 = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings" `
        -Method Post `
        -Body $bookingRequest2 `
        -ContentType "application/json"
    
    Write-Host "✓ 2eme reservation creee: $($newBooking2.confirmationNumber)" -ForegroundColor Green
    $bookingId2 = $newBooking2.id
} catch {
    Write-Host "✗ Erreur: $_" -ForegroundColor Red
}
Write-Host ""

# Test 8: GET toutes les réservations
Write-Host "Test 8: GET toutes les reservations" -ForegroundColor Yellow
try {
    $allBookings = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings" -Method Get
    Write-Host "✓ Nombre total de reservations: $($allBookings.Count)" -ForegroundColor Green
    foreach ($b in $allBookings) {
        Write-Host "  - ID: $($b.id), User: $($b.userId), Status: $($b.status), Prix: $($b.totalPrice) EUR" -ForegroundColor Cyan
    }
} catch {
    Write-Host "✗ Erreur: $_" -ForegroundColor Red
}
Write-Host ""

# Test 9: Modifier une réservation
if ($bookingId) {
    Write-Host "Test 9: PUT Modifier une reservation" -ForegroundColor Yellow
    $updateRequest = @{
        numberOfGuests = 3
        specialRequests = "Etage superieur SVP"
    } | ConvertTo-Json

    try {
        $updatedBooking = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings/$bookingId" `
            -Method Put `
            -Body $updateRequest `
            -ContentType "application/json"
        
        Write-Host "✓ Reservation modifiee: Guests=$($updatedBooking.numberOfGuests)" -ForegroundColor Green
    } catch {
        Write-Host "✗ Erreur: $_" -ForegroundColor Red
    }
    Write-Host ""
}

# Test 10: Annuler une réservation
if ($bookingId2) {
    Write-Host "Test 10: PATCH Annuler une reservation" -ForegroundColor Yellow
    try {
        $cancelled = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings/$bookingId2/cancel" -Method Patch
        Write-Host "✓ Reservation annulee: Status=$($cancelled.status)" -ForegroundColor Green
    } catch {
        Write-Host "✗ Erreur: $_" -ForegroundColor Red
    }
    Write-Host ""
}

# Test 11: GET par statut
Write-Host "Test 11: GET reservations CONFIRMED" -ForegroundColor Yellow
try {
    $confirmed = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings?status=CONFIRMED" -Method Get
    Write-Host "✓ Reservations CONFIRMED: $($confirmed.Count)" -ForegroundColor Green
} catch {
    Write-Host "✗ Erreur: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "Test 12: GET reservations CANCELLED" -ForegroundColor Yellow
try {
    $cancelled = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings?status=CANCELLED" -Method Get
    Write-Host "✓ Reservations CANCELLED: $($cancelled.Count)" -ForegroundColor Green
} catch {
    Write-Host "✗ Erreur: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== Tests termines ===" -ForegroundColor Green
Write-Host ""
Write-Host "Pour tester manuellement, utilisez:" -ForegroundColor Yellow
Write-Host "- H2 Console: http://localhost:8081/h2-console" -ForegroundColor Cyan
Write-Host "  JDBC URL: jdbc:h2:mem:bookingdb" -ForegroundColor Cyan
Write-Host "  Username: sa" -ForegroundColor Cyan
Write-Host "  Password: (vide)" -ForegroundColor Cyan
Write-Host ""
Write-Host "- Actuator Health: http://localhost:8081/actuator/health" -ForegroundColor Cyan
Write-Host "- Actuator Metrics: http://localhost:8081/actuator/metrics" -ForegroundColor Cyan
