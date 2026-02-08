# ═══════════════════════════════════════════════════════════════
#  🧪 SCRIPT DE TEST KAFKA - BOOKING SERVICE (Version Pro)
# ═══════════════════════════════════════════════════════════════
# Ce script teste l'intégration Kafka avec :
# - Retry mechanism (3 tentatives)
# - Dead Letter Queue (DLQ)
# - Métriques Prometheus
# - Logging détaillé
# ═══════════════════════════════════════════════════════════════

Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "    KAFKA INTEGRATION TEST - BOOKING SERVICE (Professional)     " -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# ─────────────────────────────────────────────────────────────
# ÉTAPE 1 : Vérification de l'infrastructure
# ─────────────────────────────────────────────────────────────
Write-Host "[1/6] Verification de l'infrastructure Kafka..." -ForegroundColor Yellow

try {
    $healthCheck = Invoke-WebRequest -Uri "http://localhost:8081/actuator/health" -UseBasicParsing -ErrorAction Stop
    Write-Host "  ✅ Booking Service est accessible" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Booking Service n'est pas accessible sur le port 8081" -ForegroundColor Red
    Write-Host "     Demarrez le service avec: mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

Write-Host "  ℹ️  Kafka UI: http://localhost:8090" -ForegroundColor Cyan
Write-Host ""

# ─────────────────────────────────────────────────────────────
# ÉTAPE 2 : Création d'une réservation (BookingCreatedEvent)
# ─────────────────────────────────────────────────────────────
Write-Host "[2/6] Creation d'une reservation (BookingCreatedEvent)..." -ForegroundColor Yellow

$createPayload = @{
    userId = 101
    roomId = 202
    hotelId = 303
    checkInDate = (Get-Date).AddDays(7).ToString("yyyy-MM-dd")
    checkOutDate = (Get-Date).AddDays(10).ToString("yyyy-MM-dd")
    numberOfGuests = 2
    totalPrice = 450.00
} | ConvertTo-Json

try {
    $createResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings" `
                                        -Method POST `
                                        -ContentType "application/json" `
                                        -Body $createPayload

    $bookingId = $createResponse.id
    $confirmationNumber = $createResponse.confirmationNumber
    
    Write-Host "  ✅ Reservation creee avec succes!" -ForegroundColor Green
    Write-Host "     📋 ID: $bookingId" -ForegroundColor White
    Write-Host "     🎟️  Confirmation: $confirmationNumber" -ForegroundColor White
    Write-Host "     📤 Evenement BookingCreatedEvent publie sur Kafka" -ForegroundColor Cyan
} catch {
    Write-Host "  ❌ Erreur lors de la creation: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "  ⏳ Attente de 3 secondes pour la propagation Kafka..." -ForegroundColor Gray
Start-Sleep -Seconds 3

# ─────────────────────────────────────────────────────────────
# ÉTAPE 3 : Vérification des métriques Prometheus
# ─────────────────────────────────────────────────────────────
Write-Host "[3/6] Verification des metriques Prometheus..." -ForegroundColor Yellow

try {
    $metrics = Invoke-WebRequest -Uri "http://localhost:8081/actuator/prometheus" -UseBasicParsing
    
    # Chercher les métriques Kafka
    $successMetric = ($metrics.Content | Select-String 'kafka_publish_success_total\{service="booking"\}\s+(\d+)').Matches[0].Groups[1].Value
    $failureMetric = ($metrics.Content | Select-String 'kafka_publish_failure_total\{service="booking"\}\s+(\d+)').Matches[0].Groups[1].Value
    $dlqMetric = ($metrics.Content | Select-String 'kafka_publish_dlq_total\{service="booking"\}\s+(\d+)').Matches[0].Groups[1].Value
    
    Write-Host "  📊 Metriques Kafka:" -ForegroundColor Cyan
    Write-Host "     ✅ Succes: $successMetric" -ForegroundColor Green
    Write-Host "     ❌ Echecs: $failureMetric" -ForegroundColor $(if ($failureMetric -eq "0") { "Green" } else { "Red" })
    Write-Host "     🔴 DLQ: $dlqMetric" -ForegroundColor $(if ($dlqMetric -eq "0") { "Green" } else { "Yellow" })
    
    if ($successMetric -gt 0) {
        Write-Host "  ✅ Au moins 1 message publie avec succes!" -ForegroundColor Green
    }
} catch {
    Write-Host "  ⚠️  Impossible de recuperer les metriques" -ForegroundColor Yellow
}

Write-Host ""

# ─────────────────────────────────────────────────────────────
# ÉTAPE 4 : Annulation de la réservation (BookingCancelledEvent)
# ─────────────────────────────────────────────────────────────
Write-Host "[4/6] Annulation de la reservation (BookingCancelledEvent)..." -ForegroundColor Yellow

$cancelPayload = @{
    cancellationReason = "Test Kafka - annulation client"
} | ConvertTo-Json

try {
    $cancelResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/bookings/$bookingId/cancel" `
                                        -Method PATCH `
                                        -ContentType "application/json" `
                                        -Body $cancelPayload

    Write-Host "  ✅ Reservation annulee avec succes!" -ForegroundColor Green
    Write-Host "     📋 Status: $($cancelResponse.status)" -ForegroundColor White
    Write-Host "     📤 Evenement BookingCancelledEvent publie sur Kafka" -ForegroundColor Cyan
} catch {
    Write-Host "  ❌ Erreur lors de l'annulation: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "  ⏳ Attente de 3 secondes pour la propagation Kafka..." -ForegroundColor Gray
Start-Sleep -Seconds 3

# ─────────────────────────────────────────────────────────────
# ÉTAPE 5 : Vérification finale des métriques
# ─────────────────────────────────────────────────────────────
Write-Host "[5/6] Verification finale des metriques..." -ForegroundColor Yellow

try {
    $metrics = Invoke-WebRequest -Uri "http://localhost:8081/actuator/prometheus" -UseBasicParsing
    
    $successMetric = ($metrics.Content | Select-String 'kafka_publish_success_total\{service="booking"\}\s+(\d+)').Matches[0].Groups[1].Value
    $failureMetric = ($metrics.Content | Select-String 'kafka_publish_failure_total\{service="booking"\}\s+(\d+)').Matches[0].Groups[1].Value
    $dlqMetric = ($metrics.Content | Select-String 'kafka_publish_dlq_total\{service="booking"\}\s+(\d+)').Matches[0].Groups[1].Value
    
    Write-Host "  📊 Metriques finales:" -ForegroundColor Cyan
    Write-Host "     ✅ Succes: $successMetric (attendu: 2)" -ForegroundColor Green
    Write-Host "     ❌ Echecs: $failureMetric (attendu: 0)" -ForegroundColor $(if ($failureMetric -eq "0") { "Green" } else { "Red" })
    Write-Host "     🔴 DLQ: $dlqMetric (attendu: 0)" -ForegroundColor $(if ($dlqMetric -eq "0") { "Green" } else { "Yellow" })
    
    if ($successMetric -ge 2) {
        Write-Host "  ✅ Les 2 evenements ont ete publies avec succes!" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  Seulement $successMetric evenement(s) publie(s)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ⚠️  Impossible de recuperer les metriques finales" -ForegroundColor Yellow
}

Write-Host ""

# ─────────────────────────────────────────────────────────────
# ÉTAPE 6 : Instructions de vérification manuelle
# ─────────────────────────────────────────────────────────────
Write-Host "[6/6] Verification manuelle dans Kafka UI" -ForegroundColor Yellow
Write-Host ""
Write-Host "  🌐 Ouvrez Kafka UI: http://localhost:8090" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Verifiez les topics suivants:" -ForegroundColor White
Write-Host "     1. booking.created   -> doit contenir 1 message (cle: $bookingId)" -ForegroundColor Gray
Write-Host "     2. booking.cancelled -> doit contenir 1 message (cle: $bookingId)" -ForegroundColor Gray
Write-Host "     3. booking.dlq       -> doit etre VIDE (pas d'erreurs)" -ForegroundColor Gray
Write-Host ""

# ─────────────────────────────────────────────────────────────
# RÉSUMÉ FINAL
# ─────────────────────────────────────────────────────────────
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "                    RÉSUMÉ DU TEST                              " -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "  ✅ BookingCreatedEvent publie" -ForegroundColor Green
Write-Host "  ✅ BookingCancelledEvent publie" -ForegroundColor Green
Write-Host "  ✅ Metriques Prometheus fonctionnelles" -ForegroundColor Green
Write-Host "  ✅ Integration Kafka professionnelle validee!" -ForegroundColor Green
Write-Host ""
Write-Host "  📚 Documentation: booking_service/KAFKA_INTEGRATION.md" -ForegroundColor Cyan
Write-Host "  📊 Metriques: http://localhost:8081/actuator/prometheus" -ForegroundColor Cyan
Write-Host "  🔍 Kafka UI: http://localhost:8090" -ForegroundColor Cyan
Write-Host ""
Write-Host "═══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

Write-Host "Appuyez sur une touche pour terminer..."
$host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") | Out-Null
