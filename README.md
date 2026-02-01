
# 🏨 HOTEL BOOKING SYSTEM - Architecture Microservices

## 📋 Vue d'Ensemble du Projet

Ce projet est une **plateforme complète de gestion de réservations d'hôtels** basée sur une architecture microservices moderne, scalable et professionnelle. Le système permet aux clients de rechercher des hôtels, réserver des chambres, laisser des avis, et aux gestionnaires d'administrer leurs établissements.

**Équipe** : 6 étudiants  
**Architecture** : Microservices  
**Technologies** : Spring Boot, Spring Cloud, Kafka, Keycloak, React  
**Base de données** : H2 (développement)

---

## 🏗️ Architecture Globale

```
                                    ┌─────────────────┐
                                    │   FRONTEND      │
                                    │  (React/Vue)    │
                                    │   Port 3000     │
                                    └────────┬────────┘
                                             │
                                             ▼
                          ┌──────────────────────────────────┐
                          │       API GATEWAY                │
                          │   (Routage + Load Balance)       │
                          │         Port 8888                │
                          └──────────────┬───────────────────┘
                                         │
                    ┌────────────────────┼────────────────────┐
                    │                    │                    │
         ┌──────────▼──────────┐  ┌─────▼──────┐  ┌─────────▼────────┐
         │   KEYCLOAK          │  │   EUREKA   │  │      KAFKA       │
         │ (Authentication)    │  │  SERVER    │  │  (Messaging)     │
         │    Port 8080        │  │ Port 8761  │  │   Port 9092      │
         └─────────────────────┘  └────────────┘  └──────────────────┘
                    │                    │                    │
    ────────────────┴────────────────────┴────────────────────┴────────────
                              MICROSERVICES LAYER
    ────────────────────────────────────────────────────────────────────────
         │                    │                    │
┌────────▼──────────┐  ┌──────▼───────────┐  ┌───▼──────────────┐
│ BOOKING SERVICE   │  │  USER SERVICE    │  │  HOTEL SERVICE   │
│    Port 8081      │  │   Port 8084      │  │    Port 8083     │
│      H2 DB1       │  │     H2 DB2       │  │      H2 DB3      │
└───────────────────┘  └──────────────────┘  └──────────────────┘
         │                    │                    │
┌────────▼──────────┐  ┌──────▼───────────┐  ┌───▼──────────────┐
│  ROOM SERVICE     │  │ REVIEW SERVICE   │  │ ANALYTICS SERVICE│
│    Port 8082      │  │   Port 8085      │  │    Port 8086     │
│      H2 DB4       │  │     H2 DB5       │  │      H2 DB6      │
└───────────────────┘  └──────────────────┘  └──────────────────┘
```

---

## 📦 Microservices et Responsabilités

### 1️⃣ **Booking Service** (Port 8081) 🎫
**Responsable** : Oussema  
**Rôle** : Gestion des réservations de chambres

**Fonctionnalités** :
- ✅ Créer une réservation
- ✅ Consulter les réservations (par utilisateur, par hôtel, par statut)
- ✅ Modifier une réservation (dates, nombre de guests)
- ✅ Annuler une réservation
- ✅ Calculer le prix total
- ✅ Générer un numéro de confirmation unique
- ✅ Publier des événements Kafka (booking.created, booking.cancelled)
- ✅ Appeler Room Service (vérifier disponibilité via Feign)
- ✅ Appeler Hotel Service (récupérer infos hôtel via Feign)

**Technologies** :
- Spring Boot 3.2.1
- Spring Data JPA
- OpenFeign (communication REST)
- Kafka Producer
- Keycloak OAuth2
- H2 Database

**API Endpoints** :
```
POST   /api/bookings                    - Créer réservation
GET    /api/bookings                    - Lister toutes les réservations
GET    /api/bookings/{id}               - Récupérer par ID
PUT    /api/bookings/{id}               - Modifier réservation
DELETE /api/bookings/{id}               - Annuler réservation
GET    /api/bookings/user/{userId}      - Réservations par utilisateur
GET    /api/bookings?status=CONFIRMED   - Filtrer par statut
```

**Événements Kafka Publiés** :
```
booking.created      → {bookingId, userId, roomId, checkIn, checkOut, totalPrice}
booking.cancelled    → {bookingId, userId, roomId, cancellationReason}
booking.completed    → {bookingId, userId, roomId}
```

---

### 2️⃣ **User Service** (Port 8084) 👥
**Responsable** : Étudiant 2  
**Rôle** : Gestion des utilisateurs et profils

**Fonctionnalités** :
- ✅ Créer un compte utilisateur
- ✅ Authentification (avec Keycloak)
- ✅ Gérer le profil utilisateur
- ✅ Historique des réservations
- ✅ Préférences utilisateur
- ✅ Écouter les événements Kafka (booking.created pour mise à jour profil)

**API Endpoints** :
```
POST   /api/users                - Créer utilisateur
GET    /api/users/{id}           - Récupérer utilisateur
PUT    /api/users/{id}           - Modifier profil
GET    /api/users/{id}/bookings  - Historique réservations
```

**Événements Kafka Consommés** :
```
booking.created → Mettre à jour le compteur de réservations utilisateur
```

---

### 3️⃣ **Hotel Service** (Port 8083) 🏨
**Responsable** : Étudiant 3  
**Rôle** : Gestion des hôtels

**Fonctionnalités** :
- ✅ CRUD des hôtels
- ✅ Recherche d'hôtels (par ville, étoiles, prix)
- ✅ Gestion des équipements (piscine, wifi, parking)
- ✅ Photos de l'hôtel
- ✅ Coordonnées GPS

**API Endpoints** :
```
POST   /api/hotels              - Créer hôtel
GET    /api/hotels              - Lister hôtels
GET    /api/hotels/{id}         - Récupérer hôtel par ID
PUT    /api/hotels/{id}         - Modifier hôtel
DELETE /api/hotels/{id}         - Supprimer hôtel
GET    /api/hotels?city=Paris   - Recherche par ville
```

**Modèle de Données** :
```json
{
  "id": 1,
  "name": "Grand Hotel Paris",
  "description": "Hôtel 5 étoiles au centre de Paris",
  "address": "123 Rue de la Paix",
  "city": "Paris",
  "country": "France",
  "rating": 5,
  "amenities": ["wifi", "piscine", "restaurant", "parking"],
  "latitude": 48.8566,
  "longitude": 2.3522
}
```

---

### 4️⃣ **Room Service** (Port 8082) 🛏️
**Responsable** : Étudiant 4  
**Rôle** : Gestion des chambres

**Fonctionnalités** :
- ✅ CRUD des chambres
- ✅ Vérifier la disponibilité
- ✅ Gérer les types de chambres (Simple, Double, Suite)
- ✅ Prix par nuit
- ✅ Capacité (nombre de personnes)
- ✅ Écouter Kafka pour mettre à jour la disponibilité

**API Endpoints** :
```
POST   /api/rooms                              - Créer chambre
GET    /api/rooms                              - Lister chambres
GET    /api/rooms/{id}                         - Récupérer chambre
PUT    /api/rooms/{id}                         - Modifier chambre
GET    /api/rooms/{id}/availability            - Vérifier disponibilité
GET    /api/rooms?hotelId=1&available=true     - Recherche
```

**Modèle de Données** :
```json
{
  "id": 1,
  "roomNumber": "101",
  "roomType": "DELUXE",
  "hotelId": 1,
  "pricePerNight": 150.00,
  "capacity": 2,
  "available": true,
  "amenities": ["wifi", "tv", "minibar"],
  "surface": 25.5
}
```

**Événements Kafka Consommés** :
```
booking.created → Marquer la chambre comme réservée
booking.cancelled → Libérer la chambre
```

---

### 5️⃣ **Review Service** (Port 8085) ⭐
**Responsable** : Étudiant 5  
**Rôle** : Gestion des avis et évaluations

**Fonctionnalités** :
- ✅ Créer un avis (après séjour terminé)
- ✅ Noter l'hôtel (1-5 étoiles)
- ✅ Commentaires
- ✅ Photos
- ✅ Réponse de l'hôtel
- ✅ Calculer la note moyenne

**API Endpoints** :
```
POST   /api/reviews                     - Créer avis
GET    /api/reviews/hotel/{hotelId}     - Avis par hôtel
GET    /api/reviews/user/{userId}       - Avis par utilisateur
PUT    /api/reviews/{id}                - Modifier avis
DELETE /api/reviews/{id}                - Supprimer avis
GET    /api/reviews/{id}/reply          - Réponse de l'hôtel
```

**Modèle de Données** :
```json
{
  "id": 1,
  "userId": "user-123",
  "userName": "John Doe",
  "hotelId": 1,
  "bookingId": 1,
  "rating": 5,
  "comment": "Excellent séjour!",
  "cleanliness": 5,
  "service": 5,
  "location": 4,
  "photos": ["url1", "url2"],
  "hotelReply": "Merci pour votre avis!",
  "createdAt": "2026-02-01T10:00:00"
}
```

**Événements Kafka Consommés** :
```
booking.completed → Envoyer invitation à laisser un avis
```

---

### 6️⃣ **Analytics Service** (Port 8086) 📊
**Responsable** : Étudiant 6  
**Rôle** : Rapports et statistiques

**Fonctionnalités** :
- ✅ Statistiques de réservations (par jour, mois, année)
- ✅ Taux d'occupation des chambres
- ✅ Revenus par hôtel
- ✅ Top hôtels les plus réservés
- ✅ Analyse des avis
- ✅ Tableaux de bord

**API Endpoints** :
```
GET /api/analytics/bookings/stats        - Statistiques réservations
GET /api/analytics/revenue               - Revenus totaux
GET /api/analytics/occupancy/{hotelId}   - Taux d'occupation
GET /api/analytics/top-hotels            - Top hôtels
GET /api/analytics/reviews/summary       - Résumé des avis
```

**Événements Kafka Consommés** :
```
booking.created → Enregistrer pour statistiques
booking.cancelled → Mettre à jour métriques
review.created → Analyser sentiment
```

---

## 🔧 Infrastructure & Services Transverses

### 7️⃣ **Eureka Server** (Port 8761) 🔍
**Rôle** : Service Discovery - Registre de tous les microservices

**Responsable** : Partagé (1 personne configure)

**Fonction** :
- Enregistrement automatique de tous les microservices
- Permet aux services de se découvrir mutuellement
- Load balancing automatique

**Configuration** :
Chaque microservice se connecte automatiquement :
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

**Dashboard** : http://localhost:8761

---

### 8️⃣ **API Gateway** (Port 8888) 🚪
**Rôle** : Point d'entrée unique pour tous les appels API

**Responsable** : Partagé (1 personne configure)

**Fonction** :
- Routage intelligent vers les microservices
- Load balancing
- Rate limiting
- Authentification centralisée
- CORS

**Routes** :
```yaml
/api/bookings/**    → booking-service:8081
/api/users/**       → user-service:8084
/api/hotels/**      → hotel-service:8083
/api/rooms/**       → room-service:8082
/api/reviews/**     → review-service:8085
/api/analytics/**   → analytics-service:8086
```

**Accès Frontend** :
```
http://localhost:8888/api/bookings
http://localhost:8888/api/hotels
```

---

### 9️⃣ **Keycloak** (Port 8080) 🔐
**Rôle** : Serveur d'authentification OAuth2/JWT

**Responsable** : Partagé (tous utilisent)

**Configuration** :
- **Realm** : `hotel-booking`
- **Client ID** : `hotel-booking-app`
- **Rôles** :
  - `ROLE_USER` : Client standard
  - `ROLE_MANAGER` : Gestionnaire d'hôtel
  - `ROLE_ADMIN` : Administrateur système

**Utilisateurs de Test** :
```
user     / user123     (ROLE_USER)
manager  / manager123  (ROLE_MANAGER)
admin    / admin123    (ROLE_ADMIN)
```

---

### 🔟 **Apache Kafka** (Port 9092) 📨
**Rôle** : Message Broker pour communication asynchrone

**Responsable** : Partagé (tous utilisent)

**Topics Kafka** :
```
booking.created      - Nouvelle réservation créée
booking.cancelled    - Réservation annulée
booking.completed    - Séjour terminé
review.created       - Nouvel avis créé
payment.processed    - Paiement effectué
```

**Communication Asynchrone** :
```
Booking Service (Producer)
    ↓ booking.created
Room Service (Consumer) → Met à jour disponibilité
User Service (Consumer) → Met à jour historique
Analytics (Consumer) → Enregistre statistiques
```

---

### 1️⃣1️⃣ **Frontend** (Port 3000) 🎨
**Rôle** : Interface utilisateur web

**Responsable** : Partagé ou 1 personne

**Technologies** :
- React / Vue.js / Angular
- Axios pour appels API
- React Router
- Material-UI / Bootstrap

**Pages** :
- Accueil / Recherche d'hôtels
- Détails hôtel
- Réservation
- Mon compte / Mes réservations
- Avis
- Dashboard Admin

---

## 🗂️ Structure des Repositories

```
HOTEL-BOOKING-SYSTEM/
│
├── 📂 booking_service/         (Port 8081) - Vous
├── 📂 user_service/            (Port 8084) - Étudiant 2
├── 📂 hotel_service/           (Port 8083) - Étudiant 3
├── 📂 room_service/            (Port 8082) - Étudiant 4
├── 📂 review_service/          (Port 8085) - Étudiant 5
├── 📂 analytics_service/       (Port 8086) - Étudiant 6
│
├── 📂 eureka_server/           (Port 8761) - Partagé
├── 📂 api_gateway/             (Port 8888) - Partagé
├── 📂 frontend/                (Port 3000) - Partagé
│
└── 📂 infrastructure/
    ├── docker-compose.yml      ← Keycloak, Kafka, Zookeeper
    ├── keycloak-config/
    ├── postman/
    │   ├── Hotel-Booking-System.postman_collection.json
    │   └── Local-Environment.postman_environment.json
    └── README.md
```

---

## 🔄 Communication Inter-Services

### Communication Synchrone (REST via OpenFeign)

**Exemple : Booking Service appelle Room Service**

```java
// Dans booking-service
@FeignClient(name = "room-service", url = "${services.room-service.url}")
public interface RoomServiceClient {
    @GetMapping("/api/rooms/{id}")
    RoomResponse getRoomById(@PathVariable Long id);
    
    @GetMapping("/api/rooms/{id}/availability")
    Boolean checkAvailability(@PathVariable Long id,
                             @RequestParam String checkIn,
                             @RequestParam String checkOut);
}
```

**Flux de réservation** :
```
1. Frontend → API Gateway → Booking Service
2. Booking Service → (Feign) → Room Service (vérifier disponibilité)
3. Booking Service → (Feign) → Hotel Service (récupérer infos)
4. Booking Service → Kafka (booking.created) → Room/User/Analytics Services
5. Booking Service → Frontend (réservation confirmée)
```

### Communication Asynchrone (Kafka)

**Exemple : Room Service écoute les événements de réservation**

```java
// Dans room-service
@KafkaListener(topics = "booking.created")
public void handleBookingCreated(BookingCreatedEvent event) {
    // Marquer la chambre comme réservée
    roomService.markAsBooked(event.getRoomId(), 
                            event.getCheckIn(), 
                            event.getCheckOut());
}
```

---

## 🚀 Démarrage du Système Complet

### Prérequis
- ✅ Java 17+
- ✅ Maven 3.9+
- ✅ Docker Desktop
- ✅ Node.js 18+ (pour frontend)
- ✅ Postman

### Étape 1 : Infrastructure (1 fois)

```powershell
# Dans infrastructure/
cd infrastructure
docker-compose up -d

# Attendre 60 secondes pour Keycloak
docker logs keycloak -f
# Attendre "Listening on: http://0.0.0.0:8080"
```

### Étape 2 : Configuration Keycloak (1 fois)

1. Ouvrir http://localhost:8080
2. Login : `admin` / `admin`
3. Créer realm : `hotel-booking`
4. Créer client : `hotel-booking-app`
5. Créer rôles : `ROLE_USER`, `ROLE_MANAGER`, `ROLE_ADMIN`
6. Créer utilisateurs de test

### Étape 3 : Démarrer Eureka Server

```powershell
cd eureka_server
mvn spring-boot:run
# Attendre "Started Eureka Server"
# Dashboard : http://localhost:8761
```

### Étape 4 : Démarrer API Gateway

```powershell
cd api_gateway
mvn spring-boot:run
# Attendre "Started API Gateway"
```

### Étape 5 : Démarrer les Microservices (en parallèle)

**Terminal 1 - Booking Service**
```powershell
cd booking_service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Terminal 2 - User Service**
```powershell
cd user_service
mvn spring-boot:run
```

**Terminal 3 - Hotel Service**
```powershell
cd hotel_service
mvn spring-boot:run
```

**Terminal 4 - Room Service**
```powershell
cd room_service
mvn spring-boot:run
```

**Terminal 5 - Review Service**
```powershell
cd review_service
mvn spring-boot:run
```

**Terminal 6 - Analytics Service**
```powershell
cd analytics_service
mvn spring-boot:run
```

### Étape 6 : Démarrer Frontend

```powershell
cd frontend
npm install
npm start
# Ouvrir http://localhost:3000
```

### ✅ Vérification

```
✅ Eureka        : http://localhost:8761 (voir tous les services enregistrés)
✅ API Gateway   : http://localhost:8888/actuator/health
✅ Keycloak      : http://localhost:8080
✅ Frontend      : http://localhost:3000
✅ Booking       : http://localhost:8081/actuator/health
✅ User          : http://localhost:8084/actuator/health
✅ Hotel         : http://localhost:8083/actuator/health
✅ Room          : http://localhost:8082/actuator/health
✅ Review        : http://localhost:8085/actuator/health
✅ Analytics     : http://localhost:8086/actuator/health
```

---

## 🧪 Tests avec Postman

### 1. Importer la Collection

```
infrastructure/postman/Hotel-Booking-System.postman_collection.json
infrastructure/postman/Local-Environment.postman_environment.json
```

### 2. Obtenir un Token JWT

```
POST http://localhost:8080/realms/hotel-booking/protocol/openid-connect/token

Body (x-www-form-urlencoded):
- client_id: hotel-booking-app
- username: user
- password: user123
- grant_type: password
```

### 3. Scénario de Test Complet

#### Test 1 : Créer un Hôtel
```
POST http://localhost:8888/api/hotels
Authorization: Bearer {token}

{
  "name": "Grand Hotel Paris",
  "city": "Paris",
  "rating": 5,
  "pricePerNight": 150.00
}
```

#### Test 2 : Créer une Chambre
```
POST http://localhost:8888/api/rooms

{
  "roomNumber": "101",
  "roomType": "DELUXE",
  "hotelId": 1,
  "pricePerNight": 150.00,
  "capacity": 2
}
```

#### Test 3 : Créer une Réservation
```
POST http://localhost:8888/api/bookings

{
  "roomId": 1,
  "userId": "user-123",
  "checkInDate": "2026-03-01",
  "checkOutDate": "2026-03-05",
  "numberOfGuests": 2
}
```

#### Test 4 : Laisser un Avis
```
POST http://localhost:8888/api/reviews

{
  "hotelId": 1,
  "bookingId": 1,
  "rating": 5,
  "comment": "Excellent séjour!"
}
```

#### Test 5 : Voir les Statistiques
```
GET http://localhost:8888/api/analytics/bookings/stats
```

---

## 📊 Workflow de Développement

### Phase 1 : Développement Indépendant (Semaines 1-2)

Chaque étudiant développe **son microservice en isolation** :

**Vous (Booking Service)** :
```
✅ Créer entités JPA
✅ Créer repositories
✅ Créer services (logique métier)
✅ Créer controllers REST
✅ Tester avec Postman (sans autres services)
✅ Mocker les appels Feign pour l'instant
```

### Phase 2 : Intégration (Semaine 3)

**Intégrer les services ensemble** :
```
✅ Démarrer tous les services en même temps
✅ Tester les appels Feign entre services
✅ Vérifier les événements Kafka
✅ Résoudre les problèmes de communication
```

### Phase 3 : Frontend (Semaine 4)

```
✅ Développer l'interface React
✅ Connecter aux API via Gateway
✅ Gestion de l'authentification
✅ Tests end-to-end
```

### Phase 4 : Finalisation (Semaine 5)

```
✅ Documentation complète
✅ Tests d'intégration
✅ Déploiement Docker
✅ Présentation
```

---

## 🔐 Sécurité

### Endpoints Protégés par Rôle

| Service | Endpoint | Rôle Requis |
|---------|----------|-------------|
| **Booking** | POST /api/bookings | USER, MANAGER, ADMIN |
| **Booking** | GET /api/bookings (toutes) | ADMIN |
| **Booking** | GET /api/bookings/{id} | USER (si propriétaire), ADMIN |
| **Hotel** | POST /api/hotels | MANAGER, ADMIN |
| **Hotel** | PUT /api/hotels/{id} | MANAGER, ADMIN |
| **Room** | POST /api/rooms | MANAGER, ADMIN |
| **Review** | POST /api/reviews | USER |
| **Analytics** | GET /api/analytics/** | ADMIN |

---

## 📈 Métriques et Monitoring

Chaque service expose des métriques via **Spring Boot Actuator** :

```
GET http://localhost:8081/actuator/health
GET http://localhost:8081/actuator/metrics
GET http://localhost:8081/actuator/info
```

**Dashboard Eureka** : http://localhost:8761 pour voir l'état de tous les services

---

## 🐛 Dépannage

### Problème : Service ne se connecte pas à Eureka
```
Solution : Vérifier application.properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

### Problème : Feign ne trouve pas le service
```
Solution : Vérifier que le service cible est enregistré dans Eureka
Vérifier l'URL dans application.properties
services.room-service.url=http://localhost:8082
```

### Problème : Kafka ne fonctionne pas
```
Solution : 
docker logs kafka
docker restart kafka
```

### Problème : 401 Unauthorized
```
Solution : Vérifier que le token JWT n'est pas expiré
Regénérer un nouveau token depuis Keycloak
```

---

## 📚 Technologies Utilisées

### Backend
- **Spring Boot** 3.2.1
- **Spring Cloud** 2023.0.0
- **Spring Data JPA**
- **Spring Security OAuth2**
- **OpenFeign** (REST Client)
- **Apache Kafka** (Messaging)
- **Keycloak** (Authentication)
- **H2 Database** (Dev)
- **Lombok** (Boilerplate reduction)
- **ModelMapper** (DTO mapping)

### Infrastructure
- **Eureka Server** (Service Discovery)
- **Spring Cloud Gateway** (API Gateway)
- **Docker** & **Docker Compose**

### Frontend
- **React** 18+
- **React Router**
- **Axios**
- **Material-UI**

### Testing
- **JUnit 5**
- **Mockito**
- **Spring Boot Test**
- **Postman**

---

## 👥 Équipe et Contributions

| Membre | Microservice | Port | Complexité |
|--------|-------------|------|------------|
| **Oussema** | Booking Service | 8081 | ⭐⭐⭐⭐ |
| **Étudiant 2** | User Service | 8084 | ⭐⭐⭐ |
| **Étudiant 3** | Hotel Service | 8083 | ⭐⭐ |
| **Étudiant 4** | Room Service | 8082 | ⭐⭐⭐ |
| **Étudiant 5** | Review Service | 8085 | ⭐⭐ |
| **Étudiant 6** | Analytics Service | 8086 | ⭐⭐⭐ |
| **Partagé** | Eureka Server | 8761 | ⭐ |
| **Partagé** | API Gateway | 8888 | ⭐⭐ |
| **Partagé** | Frontend | 3000 | ⭐⭐⭐ |

---

## 🎯 Objectifs Pédagogiques

Ce projet permet d'apprendre :

✅ **Architecture Microservices** : Services découplés et indépendants  
✅ **Communication Inter-Services** : REST (Feign) + Événements (Kafka)  
✅ **Service Discovery** : Eureka pour enregistrement automatique  
✅ **API Gateway** : Point d'entrée unique avec routage intelligent  
✅ **Sécurité** : OAuth2/JWT avec Keycloak  
✅ **Messaging Asynchrone** : Kafka pour événements  
✅ **Conteneurisation** : Docker & Docker Compose  
✅ **Travail en Équipe** : Git, branches, pull requests  
✅ **Bonnes Pratiques** : REST API, SOLID, Clean Code  

---

## 📖 Documentation Additionnelle

- 📂 [Booking Service README](booking_service/README.md)
- 📂 [API Gateway Configuration](api_gateway/README.md)
- 📂 [Infrastructure Setup](infrastructure/README.md)
- 📂 [Frontend Documentation](frontend/README.md)

---

## 🚀 Prochaines Étapes

### Pour VOUS (Booking Service) :

1. ✅ **Corriger les erreurs de compilation** de votre booking-service
2. ✅ **Tester en mode dev** (sans Keycloak/Kafka pour commencer)
3. ✅ **Implémenter tous les endpoints CRUD**
4. ✅ **Ajouter les Feign Clients** (Room, Hotel, User)
5. ✅ **Intégrer Kafka Producer**
6. ✅ **Tests avec Postman**

### Pour l'Équipe :

1. ✅ Chaque membre crée son microservice
2. ✅ Définir les contrats d'API ensemble
3. ✅ Configurer l'infrastructure (Docker Compose)
4. ✅ Tests individuels puis intégration
5. ✅ Développement du frontend
6. ✅ Tests end-to-end
7. ✅ Documentation et présentation

---

**🎉 Bon courage à toute l'équipe ! N'hésitez pas à documenter votre progression et à communiquer régulièrement !**

---

**Version** : 1.0.0  
**Date** : 1 Février 2026  
**Auteur** : Équipe Hotel Booking System
