# 🏗️ Infrastructure Docker pour Hotel Booking System

Ce dossier contient la configuration Docker Compose pour lancer l'infrastructure commune nécessaire à tous les microservices.

## 📦 Services inclus

### 1. **Kafka** (Port 9092)
Message broker pour la communication asynchrone entre microservices
- **Topics** : `booking.created`, `booking.cancelled`, `booking.completed`
- **Interface** : Port interne 29092, externe 9092

### 2. **Zookeeper** (Port 2181)
Coordination distribuée requise par Kafka

### 3. **Kafka UI** (Port 8090)
Interface web pour visualiser et gérer les topics Kafka
- **URL** : http://localhost:8090
- Visualiser les messages
- Gérer les topics

### 4. **Keycloak** (Port 8080)
Serveur d'authentification OAuth2/JWT
- **Console admin** : http://localhost:8080
- **Username** : admin
- **Password** : admin

---

## 🚀 Démarrage

### Démarrer tous les services
```powershell
cd infrastructure
docker-compose up -d
```

### Démarrer seulement Kafka (sans Keycloak)
```powershell
docker-compose up -d zookeeper kafka kafka-ui
```

### Démarrer seulement Keycloak (sans Kafka)
```powershell
docker-compose up -d keycloak
```

### Voir les logs
```powershell
docker-compose logs -f
docker-compose logs -f kafka    # Logs Kafka uniquement
```

### Arrêter les services
```powershell
docker-compose down
```

### Arrêter et supprimer les volumes
```powershell
docker-compose down -v
```

---

## 📊 Vérification

### Kafka
```powershell
# Lister les topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Créer un topic manuellement (optionnel, Spring Boot les crée automatiquement)
docker exec kafka kafka-topics --create --topic booking.created --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Consommer les messages d'un topic
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic booking.created --from-beginning
```

### Kafka UI
Ouvrir http://localhost:8090 dans le navigateur

### Keycloak
Ouvrir http://localhost:8080 dans le navigateur

---

## 🔧 Configuration des Microservices

### Dans application.properties
```properties
# Kafka
spring.kafka.bootstrap-servers=localhost:9092

# Keycloak
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/hotel-booking
```

---

## 🐛 Troubleshooting

### Kafka ne démarre pas
```powershell
# Supprimer les volumes et redémarrer
docker-compose down -v
docker-compose up -d
```

### Port 9092 déjà utilisé
```powershell
# Vérifier quel processus utilise le port
netstat -ano | findstr :9092

# Modifier le port dans docker-compose.yml
ports:
  - "9093:9092"  # Utiliser 9093 au lieu de 9092
```

### Keycloak ne démarre pas
```powershell
# Vérifier les logs
docker-compose logs keycloak

# Port 8080 déjà utilisé ? Modifier dans docker-compose.yml
ports:
  - "8081:8080"
```

---

## 📝 Topics Kafka utilisés

| Topic | Producer | Consumers | Description |
|-------|----------|-----------|-------------|
| `booking.created` | Booking Service | Room, User, Analytics | Nouvelle réservation |
| `booking.cancelled` | Booking Service | Room, User, Analytics | Réservation annulée |
| `booking.completed` | Booking Service | Review, Analytics | Séjour terminé |

---

## 🔐 Configuration Keycloak

### Étapes après démarrage :

1. **Accéder à la console** : http://localhost:8080
2. **Login** : admin / admin
3. **Créer un Realm** : `hotel-booking`
4. **Créer un Client** :
   - Client ID: `hotel-booking-app`
   - Client Protocol: `openid-connect`
   - Access Type: `public` ou `confidential`
5. **Créer des Rôles** :
   - `ROLE_USER`
   - `ROLE_MANAGER`
   - `ROLE_ADMIN`
6. **Créer des Utilisateurs de test** :
   - user / user123 (ROLE_USER)
   - manager / manager123 (ROLE_MANAGER)
   - admin / admin123 (ROLE_ADMIN)

---

## 📦 Réseau Docker

Tous les services sont dans le réseau `hotel-network` pour pouvoir communiquer entre eux.

---

## ⚡ Commandes utiles

```powershell
# Statut des containers
docker-compose ps

# Redémarrer un service
docker-compose restart kafka

# Voir l'utilisation des ressources
docker stats

# Nettoyer tout
docker-compose down -v
docker system prune -a
```

---

## 🎯 Prochaines étapes

1. Démarrer l'infrastructure : `docker-compose up -d`
2. Attendre 30-60 secondes que tout démarre
3. Vérifier Kafka UI : http://localhost:8090
4. Démarrer vos microservices
5. Tester la publication d'événements Kafka
