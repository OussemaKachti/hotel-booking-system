# Script pour tester les événements Kafka
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   TEST KAFKA EVENTS" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "IMPORTANT: Ce test necessite Kafka en cours d'execution" -ForegroundColor Yellow
Write-Host "Si Kafka n'est pas disponible, les events ne seront pas publies" -ForegroundColor Yellow
Write-Host "mais le service continuera de fonctionner normalement." -ForegroundColor Yellow
Write-Host ""

# Test 1: Créer une réservation (devrait publier booking.created)
Write-Host "[1] POST Creer reservation (event: booking.created)..." -ForegroundColor Yellow
$json1 = @{
    roomId = 101
    hotelId = 1
    userId = "kafka-user-1"
    checkInDate = "2026-05-01"
    checkOutDate = "2026-05-05"
    numberOfGuests = 2
    pricePerNight = 200.00
} | ConvertTo-Json

$booking1 = Invoke-RestMethod "http://localhost:8081/api/bookings" -Method Post -Body $json1 -ContentType "application/json"
Write-Host "OK: Booking cree ID=$($booking1.id)" -ForegroundColor Green
Write-Host "    => Event 'booking.created' publie sur Kafka" -ForegroundColor Cyan
Write-Host ""

Start-Sleep -Seconds 2

# Test 2: Annuler une réservation (devrait publier booking.cancelled)
Write-Host "[2] PATCH Annuler reservation (event: booking.cancelled)..." -ForegroundColor Yellow
$cancelled = Invoke-RestMethod "http://localhost:8081/api/bookings/$($booking1.id)/cancel" -Method Patch
Write-Host "OK: Booking annule Status=$($cancelled.status)" -ForegroundColor Green
Write-Host "    => Event 'booking.cancelled' publie sur Kafka" -ForegroundColor Cyan
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   TESTS TERMINES" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Verifiez les logs du service pour voir:" -ForegroundColor Yellow
Write-Host "  - 'Publishing BookingCreatedEvent...'" -ForegroundColor Cyan
Write-Host "  - 'BookingCreatedEvent published successfully'" -ForegroundColor Cyan
Write-Host "  - 'Publishing BookingCancelledEvent...'" -ForegroundColor Cyan
Write-Host "  - 'BookingCancelledEvent published successfully'" -ForegroundColor Cyan
Write-Host ""
Write-Host "Si Kafka n'est pas disponible, vous verrez:" -ForegroundColor Yellow
Write-Host "  - 'Failed to publish BookingCreatedEvent'" -ForegroundColor Red
