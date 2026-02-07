# 🚀 PLAN D'ACTION COMPLET - Intégration Kafka & Keycloak

## 📋 PHASE 1 : CORRIGER L'INFRASTRUCTURE (30 min)

### Étape 1.1 : Corriger docker-compose.yml ⚠️ CRITIQUE
```bash
cd infrastructure
# Éditer docker-compose.yml
```

**Problème** : Les services `eureka` et `gateway` pointent vers des dossiers inexistants

**Solution** : Retirer ces sections et utiliser seulement les images Docker

**Fichier corrigé** : `infrastructure/docker-compose-fixed.yml`

### Étape 1.2 : Démarrer l'infrastructure
```powershell
cd infrastructure
docker-compose up -d

# Vérifier
docker ps
# Devrait montrer : kafka, zookeeper, keycloak (3 conteneurs)
```

### Étape 1.3 : Vérifier les services Docker
```
✅ Kafka UI       : http://localhost:8090
✅ Keycloak       : http://localhost:8080 (admin/admin)
✅ Zookeeper      : Port 2181
✅ Kafka          : Port 9092
```

---

## 📋 PHASE 2 : IMPLÉMENTER KAFKA PRODUCER (Booking Service) - 2h

### Étape 2.1 : Créer les Event DTOs (20 min)

**Fichiers à créer** :
```
booking_service/src/main/java/com/hotel/booking/
├── event/
│   ├── BookingCreatedEvent.java
│   ├── BookingCancelledEvent.java
│   └── BookingCompletedEvent.java
```

**Contenu des Events** :
- bookingId, userId, roomId, hotelId
- checkInDate, checkOutDate
- totalPrice, status
- timestamp

### Étape 2.2 : Créer la configuration Kafka (15 min)

**Fichiers à créer** :
```
booking_service/src/main/java/com/hotel/booking/
├── kafka/
│   ├── KafkaTopicConfig.java  (@Configuration)
│   └── BookingEventProducer.java
```

**Topics à créer** :
- `booking.created`
- `booking.cancelled`
- `booking.completed`

### Étape 2.3 : Créer le Producer (30 min)

**BookingEventProducer.java** :
- Injecter `KafkaTemplate<String, Object>`
- Méthode `publishBookingCreated(BookingCreatedEvent event)`
- Méthode `publishBookingCancelled(BookingCancelledEvent event)`
- Méthode `publishBookingCompleted(BookingCompletedEvent event)`
- Logging des événements envoyés

### Étape 2.4 : Intégrer dans BookingService (30 min)

**Modifier BookingService.java** :
```java
@Autowired
private BookingEventProducer eventProducer;

public BookingResponse createBooking(BookingRequest request) {
    // ... créer booking ...
    
    // Publier événement Kafka
    BookingCreatedEvent event = new BookingCreatedEvent(...);
    eventProducer.publishBookingCreated(event);
    
    return response;
}
```

**Points d'intégration** :
- `createBooking()` → publier `booking.created`
- `cancelBooking()` → publier `booking.cancelled`
- `completeBooking()` → publier `booking.completed` (si existe)

### Étape 2.5 : Tester le Producer (15 min)

```powershell
# 1. Démarrer Booking Service
cd booking_service
mvn spring-boot:run

# 2. Créer une réservation
curl -X POST http://localhost:8081/api/bookings `
  -H "Content-Type: application/json" `
  -d '{
    "userId": 1,
    "roomId": 101,
    "hotelId": 1,
    "checkInDate": "2026-03-01",
    "checkOutDate": "2026-03-05",
    "numberOfGuests": 2,
    "totalPrice": 600.00
  }'

# 3. Vérifier dans Kafka UI
# Ouvrir http://localhost:8090
# Aller dans Topics → booking.created
# Vérifier qu'il y a 1 message
```

**Validation** :
- ✅ Service démarre sans erreur
- ✅ Événement visible dans Kafka UI
- ✅ Logs montrent "Event published: booking.created"

---

## 📋 PHASE 3 : IMPLÉMENTER KAFKA CONSUMER (Chambre Service) - 1h30

### Étape 3.1 : Ajouter dépendances Kafka (10 min)

**Modifier chambre/pom.xml** :
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

**Rebuild** :
```powershell
cd chambre
mvn clean install
```

### Étape 3.2 : Configurer Kafka Consumer (10 min)

**Modifier chambre/application.properties** :
```properties
# Kafka Consumer Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=chambre-service
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
```

### Étape 3.3 : Créer les Event DTOs (15 min)

**Fichiers à créer** :
```
chambre/src/main/java/com/example/chambre/
├── event/
│   ├── BookingCreatedEvent.java  (copie de booking_service)
│   └── BookingCancelledEvent.java
```

**Note** : Utiliser exactement la même structure que dans booking_service

### Étape 3.4 : Créer le Listener (30 min)

**Créer chambre/kafka/BookingEventListener.java** :
```java
@Service
@Slf4j
public class BookingEventListener {
    
    @Autowired
    private ChambreRepository chambreRepository;
    
    @KafkaListener(topics = "booking.created", groupId = "chambre-service")
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("Received booking.created: {}", event);
        
        // Marquer la chambre comme réservée
        Optional<Chambre> chambre = chambreRepository.findById(event.getRoomId());
        if (chambre.isPresent()) {
            chambre.get().setStatus(ChambreStatus.RESERVED);
            chambreRepository.save(chambre.get());
            log.info("Chambre {} marked as RESERVED", event.getRoomId());
        }
    }
    
    @KafkaListener(topics = "booking.cancelled", groupId = "chambre-service")
    public void handleBookingCancelled(BookingCancelledEvent event) {
        log.info("Received booking.cancelled: {}", event);
        
        // Libérer la chambre
        Optional<Chambre> chambre = chambreRepository.findById(event.getRoomId());
        if (chambre.isPresent()) {
            chambre.get().setStatus(ChambreStatus.AVAILABLE);
            chambreRepository.save(chambre.get());
            log.info("Chambre {} marked as AVAILABLE", event.getRoomId());
        }
    }
}
```

### Étape 3.5 : Tester l'intégration complète (25 min)

**Test Scenario** :
```powershell
# 1. Démarrer tous les services
# Terminal 1: Infrastructure
cd infrastructure
docker-compose up -d

# Terminal 2: Eureka
cd discovery-service
mvn spring-boot:run

# Terminal 3: Gateway
cd gateway-service
mvn spring-boot:run

# Terminal 4: Chambre
cd chambre
mvn spring-boot:run

# Terminal 5: Booking
cd booking_service
mvn spring-boot:run

# 2. Créer une chambre
curl -X POST http://localhost:8082/api/chambres `
  -H "Content-Type: application/json" `
  -d '{
    "roomNumber": "101",
    "type": "DELUXE",
    "pricePerNight": 150.0,
    "status": "AVAILABLE"
  }'

# 3. Créer une réservation
curl -X POST http://localhost:8081/api/bookings `
  -H "Content-Type: application/json" `
  -d '{
    "userId": 1,
    "roomId": 1,
    "hotelId": 1,
    "checkInDate": "2026-03-01",
    "checkOutDate": "2026-03-05",
    "numberOfGuests": 2,
    "totalPrice": 600.00
  }'

# 4. Vérifier que la chambre a changé de statut
curl http://localhost:8082/api/chambres/1
# Devrait montrer "status": "RESERVED"

# 5. Vérifier dans Kafka UI
# http://localhost:8090 → Topics → booking.created → 1 message
```

**Validation** :
- ✅ Booking Service publie booking.created
- ✅ Chambre Service reçoit l'événement (logs)
- ✅ Statut chambre passe de AVAILABLE → RESERVED
- ✅ Événement visible dans Kafka UI

---

## 📋 PHASE 4 : IMPLÉMENTER KEYCLOAK (OPTIONNEL) - 4h

### Étape 4.1 : Configurer Keycloak (30 min)

**1. Accéder à Keycloak** :
```
URL: http://localhost:8080
Login: admin / admin
```

**2. Créer Realm** :
- Name: `hotel-booking`
- Enabled: ✅

**3. Créer Client** :
- Client ID: `hotel-booking-app`
- Client Protocol: `openid-connect`
- Access Type: `confidential`
- Valid Redirect URIs: `http://localhost:*`
- Web Origins: `http://localhost:*`

**4. Créer Rôles** :
- `ROLE_USER`
- `ROLE_MANAGER`
- `ROLE_ADMIN`

**5. Créer Utilisateurs de Test** :
```
Username: user
Password: user123
Roles: ROLE_USER

Username: manager
Password: manager123
Roles: ROLE_MANAGER

Username: admin
Password: admin123
Roles: ROLE_ADMIN
```

### Étape 4.2 : Ajouter dépendances Security (15 min)

**Modifier tous les pom.xml (booking, chambre, hotel, user, review)** :
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

### Étape 4.3 : Configurer OAuth2 (20 min par service)

**Ajouter dans application.properties** :
```properties
# Keycloak OAuth2 Configuration
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/hotel-booking
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/hotel-booking/protocol/openid-connect/certs
```

### Étape 4.4 : Créer SecurityConfig (30 min par service)

**Créer SecurityConfig.java dans chaque service** :
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        
        return http.build();
    }
}
```

### Étape 4.5 : Protéger les Controllers (15 min par service)

**Ajouter annotations dans Controllers** :
```java
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<BookingResponse> createBooking(...) {
        // ...
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBooking(...) {
        // ...
    }
}
```

### Étape 4.6 : Tester l'authentification (30 min)

**1. Obtenir un token JWT** :
```powershell
$response = Invoke-RestMethod -Uri "http://localhost:8080/realms/hotel-booking/protocol/openid-connect/token" `
  -Method POST `
  -Body @{
    grant_type="password"
    client_id="hotel-booking-app"
    client_secret="<client-secret>"
    username="user"
    password="user123"
  }

$token = $response.access_token
```

**2. Tester un endpoint protégé** :
```powershell
# Sans token → 401
curl http://localhost:8081/api/bookings

# Avec token → 200
curl http://localhost:8081/api/bookings `
  -H "Authorization: Bearer $token"
```

---

## 📊 RÉSUMÉ DES PHASES

| Phase | Priorité | Durée | Complexité | Bloquant? |
|-------|----------|-------|------------|-----------|
| **Phase 1: Infrastructure** | 🔴 CRITIQUE | 30 min | Faible | ✅ OUI |
| **Phase 2: Kafka Producer** | 🟠 HAUTE | 2h | Moyenne | ✅ OUI |
| **Phase 3: Kafka Consumer** | 🟠 HAUTE | 1h30 | Moyenne | ✅ OUI |
| **Phase 4: Keycloak** | 🟢 MOYENNE | 4h | Élevée | ❌ NON |

---

## 🎯 ORDRE D'EXÉCUTION RECOMMANDÉ

### Aujourd'hui (Priorité MAX)
1. ✅ **Phase 1** : Corriger docker-compose et démarrer infrastructure
2. ✅ **Phase 2** : Implémenter Kafka Producer dans Booking
3. ✅ Tester l'envoi d'événements

### Demain
4. ✅ **Phase 3** : Implémenter Kafka Consumer dans Chambre
5. ✅ Tester l'intégration complète Booking → Chambre

### Plus tard (si nécessaire)
6. ⏸️ **Phase 4** : Keycloak (optionnel pour MVP)

---

## 🚨 POINTS D'ATTENTION

### ⚠️ Problèmes potentiels

1. **Sérialisation JSON** :
   - Les Events doivent être identiques dans Producer et Consumer
   - Utiliser `@JsonProperty` si nécessaire

2. **Transactions Kafka** :
   - Wrap dans try-catch pour ne pas bloquer les transactions SQL
   - Kafka en échec ne doit pas rollback la réservation

3. **Topics Kafka** :
   - Vérifier que les topics sont créés automatiquement
   - Ou les créer manuellement via Kafka UI

4. **Consumer Group** :
   - Chaque service doit avoir un group-id unique
   - `chambre-service`, `analytics-service`, etc.

### ✅ Best Practices

1. **Logging** :
   - Logger AVANT et APRÈS chaque envoi Kafka
   - Logger les événements reçus dans les Consumers

2. **Error Handling** :
   - `@KafkaListener` doit gérer les exceptions
   - Configurer DLQ (Dead Letter Queue) si nécessaire

3. **Testing** :
   - Tester chaque phase séparément
   - Vérifier Kafka UI après chaque test

4. **Documentation** :
   - Documenter les formats des Events
   - Maintenir à jour la liste des Topics

---

## 📝 CHECKLIST FINALE

### Infrastructure
- [ ] docker-compose.yml corrigé
- [ ] Kafka, Zookeeper, Keycloak démarrés
- [ ] Kafka UI accessible (port 8090)
- [ ] Tous les topics créés

### Booking Service (Producer)
- [ ] Event DTOs créés
- [ ] KafkaTopicConfig créé
- [ ] BookingEventProducer créé
- [ ] Intégré dans BookingService
- [ ] Tests passent
- [ ] Événements visibles dans Kafka UI

### Chambre Service (Consumer)
- [ ] Dépendances Kafka ajoutées
- [ ] Configuration Kafka ajoutée
- [ ] Event DTOs créés
- [ ] BookingEventListener créé
- [ ] Tests d'intégration passent
- [ ] Statut chambre change après événement

### Keycloak (Optionnel)
- [ ] Realm créé
- [ ] Client configuré
- [ ] Rôles créés
- [ ] Utilisateurs créés
- [ ] Dépendances Security ajoutées
- [ ] SecurityConfig créé
- [ ] Endpoints protégés
- [ ] Tests avec JWT passent

---

## 🎯 PROCHAINE ACTION

**Veux-tu que je commence à implémenter :**

1. 🔥 **La correction du docker-compose** (5 min) ?
2. 🔥 **Le code Kafka Producer complet** (30 min) ?
3. 📖 **Un guide détaillé pour Kafka Consumer** ?
4. ⏸️ **Reporter Keycloak pour plus tard** ?

**Dis-moi par quoi commencer !**
