# 📦 Infrastructure Docker - Hotel Booking System

## 🎯 Services Infrastructure

Cette infrastructure contient les services partagés nécessaires pour le système :

### Services Déployés

| Service | Port | Description | Interface Web |
|---------|------|-------------|---------------|
| **Zookeeper** | 2181 | Coordination Kafka | - |
| **Kafka** | 9092 / 29092 | Message Broker | - |
| **Kafka UI** | 8090 | Interface Kafka | http://localhost:8090 |
| **Keycloak** | 8080 | Authentification OAuth2 | http://localhost:8080 |

---

## 🚀 Démarrage

### Prérequis
- Docker Desktop installé et démarré
- Ports disponibles : 2181, 8080, 8090, 9092, 29092

### Commandes

```powershell
# Démarrer tous les services
cd infrastructure
docker-compose up -d

# Vérifier l'état
docker-compose ps

# Voir les logs
docker-compose logs -f

# Arrêter tous les services
docker-compose down

# Arrêter et supprimer les volumes (⚠️ Efface les données)
docker-compose down -v
```

---

## 🔍 Vérification des Services

### 1. Kafka UI
```
URL: http://localhost:8090
Topics attendus: booking.created, booking.cancelled, booking.completed
```

### 2. Keycloak
```
URL: http://localhost:8080
Login: admin / admin
```

### 3. Kafka (CLI)
```powershell
# Lister les topics
docker exec -it hotel-kafka kafka-topics --list --bootstrap-server localhost:9092

# Créer un topic manuellement (optionnel)
docker exec -it hotel-kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic booking.created \
  --partitions 1 \
  --replication-factor 1

# Consommer des messages
docker exec -it hotel-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic booking.created \
  --from-beginning
```

---

## 📊 Health Checks

Tous les services ont des health checks configurés :

- **Zookeeper** : Vérifie le port 2181
- **Kafka** : Vérifie l'API broker
- **Keycloak** : Vérifie /health/ready

Attendre que tous les services soient "healthy" avant de démarrer les microservices.

---

## 🐛 Dépannage

### Kafka ne démarre pas
```powershell
# Vérifier les logs
docker-compose logs kafka

# Redémarrer Kafka
docker-compose restart kafka
```

### Port déjà utilisé
```powershell
# Windows - Trouver le processus sur le port 9092
netstat -ano | findstr :9092

# Tuer le processus
taskkill /PID <PID> /F
```

### Réinitialiser complètement
```powershell
docker-compose down -v
docker system prune -a
docker-compose up -d
```

---

## 📝 Notes

- **Eureka** et **Gateway** sont démarrés manuellement avec Maven (pas dans Docker)
- Les microservices (booking, chambre, etc.) sont aussi démarrés avec Maven
- Cette infrastructure est partagée par tous les services de l'équipe
- Les topics Kafka sont créés automatiquement lors du premier message

---

## 🔐 Configuration Keycloak (Optionnel)

Pour configurer l'authentification plus tard :

1. Accéder à http://localhost:8080
2. Login : admin / admin
3. Créer realm : `hotel-booking`
4. Créer client : `hotel-booking-app`
5. Créer rôles : ROLE_USER, ROLE_MANAGER, ROLE_ADMIN
6. Créer utilisateurs de test

---

## 📈 Monitoring

### Kafka UI Features
- Visualiser les topics
- Voir les messages en temps réel
- Gérer les consumer groups
- Monitorer les performances

### Keycloak Admin Console
- Gérer les utilisateurs
- Configurer les clients OAuth2
- Voir les sessions actives
- Analyser les logs d'authentification
