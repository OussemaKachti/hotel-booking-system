# Script de Test - Integration Kafka

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   TEST KAFKA INTEGRATION" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8081/api/bookings"

# Test 1: Creer une reservation
Write-Host "[TEST 1] Creation d'une reservation..." -ForegroundColor Yellow
$bookingData = @{
    userId = "1"
    roomId = 101
    hotelId = 1
    checkInDate = "2026-03-01"
    checkOutDate = "2026-03-05"
    numberOfGuests = 2
    pricePerNight = 150.00
    specialRequests = "Vue sur mer"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri $baseUrl -Method POST -Body $bookingData -ContentType "application/json"
    Write-Host "Reservation creee avec succes!" -ForegroundColor Green
    Write-Host "   ID: $($response.id)" -ForegroundColor Cyan
    Write-Host "   Confirmation: $($response.confirmationNumber)" -ForegroundColor Cyan
    Write-Host "   Prix Total: $($response.totalPrice) euros" -ForegroundColor Cyan
    $bookingId = $response.id
    Write-Host ""
    
    Write-Host "Attente publication Kafka (3s)..." -ForegroundColor Gray
    Start-Sleep -Seconds 3
    
    # Test 2: Annuler la reservation
    Write-Host "[TEST 2] Annulation de la reservation..." -ForegroundColor Yellow
    $cancelResponse = Invoke-RestMethod -Uri "$baseUrl/$bookingId/cancel" -Method PATCH
    Write-Host "Reservation annulee avec succes!" -ForegroundColor Green
    Write-Host "   Statut: $($cancelResponse.status)" -ForegroundColor Cyan
    Write-Host ""
    
    Write-Host "Attente publication Kafka (3s)..." -ForegroundColor Gray
    Start-Sleep -Seconds 3
    
} catch {
    Write-Host "Erreur: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   VERIFICATION KAFKA UI" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Ouvrir: http://localhost:8090" -ForegroundColor Cyan
Write-Host "Topics > booking.created (1 message)" -ForegroundColor White
Write-Host "Topics > booking.cancelled (1 message)" -ForegroundColor White
Write-Host ""
Read-Host "Appuyez sur Entree"
