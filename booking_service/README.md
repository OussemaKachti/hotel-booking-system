# Booking Service
cd booking_service
mvn spring-boot:run

cd booking_service
.\test-api.ps1


Microservice de gestion des réservations pour le système Hotel Booking System.

## 📋 Description

Ce service gère toutes les opérations liées aux réservations :
- Création de réservations
- Consultation des réservations
- Modification des réservations
- Annulation des réservations
- Calcul automatique du prix total

## 🚀 Démarrage

### Prérequis
- Java 17+
- Maven 3.9+

### Lancer le service

```bash
cd booking_service
mvn clean install
mvn spring-boot:run
```

Le service démarre sur **http://localhost:8081**

### Accès à la console H2
- URL: http://localhost:8081/h2-console
- JDBC URL: jdbc:h2:mem:bookingdb
- Username: sa
- Password: (vide)

## 📚 API Endpoints

### 1. Créer une réservation
```http
POST /api/bookings
Content-Type: application/json

{
  "roomId": 1,
  "hotelId": 1,
  "userId": "user-123",
  "checkInDate": "2026-03-01",
  "checkOutDate": "2026-03-05",
  "numberOfGuests": 2,
  "pricePerNight": 150.00,
  "specialRequests": "Vue sur la mer"
}
```

**Réponse (201 Created):**
```json
{
  "id": 1,
  "confirmationNumber": "BK-A1B2C3D4",
  "roomId": 1,
  "hotelId": 1,
  "userId": "user-123",
  "checkInDate": "2026-03-01",
  "checkOutDate": "2026-03-05",
  "numberOfGuests": 2,
  "numberOfNights": 4,
  "pricePerNight": 150.00,
  "totalPrice": 600.00,
  "status": "CONFIRMED",
  "specialRequests": "Vue sur la mer",
  "createdAt": "2026-02-01T10:30:00",
  "updatedAt": "2026-02-01T10:30:00"
}
```

### 2. Récupérer toutes les réservations
```http
GET /api/bookings
```

### 3. Récupérer une réservation par ID
```http
GET /api/bookings/1
```

### 4. Récupérer par numéro de confirmation
```http
GET /api/bookings/confirmation/BK-A1B2C3D4
```

### 5. Récupérer les réservations d'un utilisateur
```http
GET /api/bookings/user/user-123
```

### 6. Récupérer par statut
```http
GET /api/bookings?status=CONFIRMED
```

Statuts disponibles: `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`, `NO_SHOW`

### 7. Récupérer les réservations d'un hôtel
```http
GET /api/bookings/hotel/1
```

### 8. Modifier une réservation
```http
PUT /api/bookings/1
Content-Type: application/json

{
  "checkInDate": "2026-03-02",
  "checkOutDate": "2026-03-06",
  "numberOfGuests": 3,
  "specialRequests": "Étage supérieur"
}
```

### 9. Annuler une réservation
```http
PATCH /api/bookings/1/cancel
```

### 10. Supprimer une réservation
```http
DELETE /api/bookings/1
```

### 11. Health Check
```http
GET /api/bookings/health
```

## 🧪 Tests avec cURL

### Créer une réservation
```bash
curl -X POST http://localhost:8081/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 1,
    "hotelId": 1,
    "userId": "user-123",
    "checkInDate": "2026-03-01",
    "checkOutDate": "2026-03-05",
    "numberOfGuests": 2,
    "pricePerNight": 150.00
  }'
```

### Lister toutes les réservations
```bash
curl http://localhost:8081/api/bookings
```

### Récupérer une réservation
```bash
curl http://localhost:8081/api/bookings/1
```

### Annuler une réservation
```bash
curl -X PATCH http://localhost:8081/api/bookings/1/cancel
```

## 📊 Modèle de Données

### Booking Entity
- `id` (Long) - Identifiant unique
- `confirmationNumber` (String) - Numéro de confirmation (ex: BK-A1B2C3D4)
- `roomId` (Long) - ID de la chambre
- `hotelId` (Long) - ID de l'hôtel
- `userId` (String) - ID de l'utilisateur
- `checkInDate` (LocalDate) - Date d'arrivée
- `checkOutDate` (LocalDate) - Date de départ
- `numberOfGuests` (Integer) - Nombre de personnes
- `numberOfNights` (Integer) - Nombre de nuits (calculé)
- `pricePerNight` (BigDecimal) - Prix par nuit
- `totalPrice` (BigDecimal) - Prix total (calculé)
- `status` (BookingStatus) - Statut de la réservation
- `specialRequests` (String) - Demandes spéciales
- `createdAt` (LocalDateTime) - Date de création
- `updatedAt` (LocalDateTime) - Date de mise à jour

## ✅ Validation

### Règles de validation
- Check-in date doit être dans le futur
- Check-out date doit être après check-in
- Maximum 30 nuits par réservation
- Nombre de guests: 1 à 10
- Prix par nuit > 0

## 🔧 Configuration

### application.properties
```properties
# Port
server.port=8081

# Base de données H2
spring.datasource.url=jdbc:h2:mem:bookingdb

# JPA
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Console H2
spring.h2.console.enabled=true
```

## 📈 Prochaines Étapes

- [ ] Intégration avec Room Service (Feign)
- [ ] Intégration avec Hotel Service (Feign)
- [ ] Intégration avec User Service (Feign)
- [ ] Publication d'événements Kafka
- [ ] Authentification Keycloak
- [ ] Tests unitaires
- [ ] Tests d'intégration

## 🐛 Erreurs Courantes

### 400 Bad Request
- Dates invalides (passées ou check-out avant check-in)
- Champs obligatoires manquants
- Nombre de guests invalide

### 404 Not Found
- ID de réservation inexistant
- Numéro de confirmation invalide

### 422 Unprocessable Entity
- Tentative de modifier une réservation annulée ou terminée
- Réservation déjà annulée

## 📞 Contact

**Développeur**: Oussema  
**Service**: Booking Service  
**Port**: 8081
