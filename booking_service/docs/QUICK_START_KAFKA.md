# 🚀 Guide de Démarrage Rapide - Kafka Integration

## ⚡ Installation en 5 Minutes

### 📋 Prérequis

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- PowerShell (Windows)

---

## 🎯 Étape 1 : Démarrer l'Infrastructure

```bash
cd infrastructure
docker-compose up -d kafka zookeeper kafka-ui
```

**Vérification** :
```bash
docker ps | findstr kafka
```

Vous devriez voir 3 containers : `kafka`, `zookeeper`, `kafka-ui`

---

## 🎯 Étape 2 : Démarrer le Booking Service

```bash
cd booking_service
mvn spring-boot:run
```

**Attendez le message** :
```
Started BookingServiceApplication in X.XXX seconds
```

---

## 🎯 Étape 3 : Vérifier les Topics Kafka

Ouvrir dans le navigateur : **http://localhost:8090**

Vérifiez que ces 4 topics existent :
- ✅ `booking.created`
- ✅ `booking.cancelled`
- ✅ `booking.completed`
- ✅ `booking.dlq`

---

## 🎯 Étape 4 : Tester l'Intégration

Lancer le script de test :

```powershell
.\test-kafka-professional.ps1
```

**Ce script va** :
1. ✅ Créer une réservation → publie `BookingCreatedEvent`
2. ✅ Annuler la réservation → publie `BookingCancelledEvent`
3. ✅ Vérifier les métriques Prometheus
4. ✅ Afficher les statistiques

---

## 📊 Étape 5 : Consulter les Métriques

**Endpoint Prometheus** :
```
http://localhost:8081/actuator/prometheus
```

**Métriques clés** :
```
kafka_publish_success_total{service="booking"} 2
kafka_publish_failure_total{service="booking"} 0
kafka_publish_dlq_total{service="booking"} 0
```

---

## 🔍 Vérification Visuelle dans Kafka UI

1. Ouvrir **http://localhost:8090**
2. Cliquer sur **Topics**
3. Cliquer sur **booking.created**
4. Cliquer sur **Messages**
5. Vous devriez voir votre événement JSON :

```json
{
  "bookingId": 1,
  "userId": 101,
  "roomId": 202,
  "hotelId": 303,
  "checkInDate": "2024-06-15",
  "checkOutDate": "2024-06-18",
  "numberOfGuests": 2,
  "totalPrice": 450.00,
  "status": "CONFIRMED",
  "eventTimestamp": "2024-06-08T14:30:00.123456",
  "eventType": "BOOKING_CREATED"
}
```

---

## ✅ Checklist de Validation

- [ ] Kafka & Zookeeper démarrés (docker ps)
- [ ] Booking Service démarré (port 8081)
- [ ] 4 topics créés automatiquement dans Kafka
- [ ] Script de test exécuté avec succès
- [ ] Métriques Prometheus accessibles
- [ ] Messages visibles dans Kafka UI
- [ ] 0 message en DLQ (pas d'erreurs)

---

## 🎓 Points Clés de l'Implémentation

### ✨ Ce Qui a Été Implémenté

| Fonctionnalité           | Status | Description                                    |
|--------------------------|--------|------------------------------------------------|
| **Producer Configuration** | ✅     | Idempotence, compression snappy, acks=all      |
| **Retry Mechanism**        | ✅     | 3 tentatives avec backoff exponentiel (1s, 2s, 4s) |
| **Dead Letter Queue**      | ✅     | Topic DLQ avec métadonnées d'erreur enrichies  |
| **Métriques Prometheus**   | ✅     | 4 métriques (success, failure, dlq, duration)  |
| **Tests d'Intégration**    | ✅     | @EmbeddedKafka pour tests isolés               |
| **Documentation**          | ✅     | KAFKA_INTEGRATION.md complet                   |
| **Environment-aware**      | ✅     | Config dev/prod (partitions, replicas)         |

### 🔧 Configuration Professionnelle

**Topics en Dev (actuel)** :
- 1 partition, 1 replica
- Retention 1 jour

**Topics en Prod** :
- 3 partitions, 2 replicas
- Retention 7 jours (DLQ: 30 jours)
- Min in-sync replicas = 2

Pour basculer en prod, ajouter :
```properties
spring.profiles.active=prod
```

---

## 🛠️ Troubleshooting Rapide

### ❌ Booking Service ne démarre pas

**Solution** :
```bash
# Vérifier H2 et Kafka dans les logs
cd booking_service
mvn clean install
mvn spring-boot:run
```

---

### ❌ Topics n'apparaissent pas dans Kafka UI

**Solution** :
```bash
# Redémarrer Kafka
docker-compose restart kafka
# Attendre 30 secondes
# Redémarrer le Booking Service
```

---

### ❌ Messages n'arrivent pas dans Kafka

**Vérifications** :
1. Logs du service → chercher `Publishing event to topic`
2. Kafka UI → vérifier que le topic existe
3. Métriques → `kafka_publish_failure_total` > 0 ?

Si failures > 0 :
- Vérifier `bootstrap-servers=localhost:9092` dans application.properties
- Vérifier que Kafka écoute sur 9092 : `docker port <kafka_container_id>`

---

## 📚 Prochaines Étapes

### 🎯 Pour Apprendre

1. Lire [KAFKA_INTEGRATION.md](KAFKA_INTEGRATION.md) (documentation complète)
2. Analyser le code de `BookingEventProducer.java`
3. Comprendre le retry mechanism avec `@Retryable`
4. Étudier les métriques dans Prometheus

### 🎯 Pour Développer

1. **Créer un Consumer dans Analytics Service** :
   - Écouter `booking.created`, `booking.cancelled`, `booking.completed`
   - Calculer des statistiques (CA, taux d'annulation, etc.)

2. **Créer un Consumer dans Room Service** :
   - Écouter `booking.created` → marquer chambre comme réservée
   - Écouter `booking.cancelled` → libérer la chambre

3. **Ajouter Grafana Dashboard** :
   - Visualiser les métriques Kafka
   - Alertes si DLQ > 0

---

## 🎉 Félicitations !

Vous avez maintenant une intégration Kafka **production-ready** avec :

✅ Retry automatique  
✅ Dead Letter Queue  
✅ Monitoring & Métriques  
✅ Tests professionnels  
✅ Documentation complète  

**Passez à l'étape suivante** : Implémenter des consumers dans les autres microservices !

---

## 🆘 Support

**Questions ?** Consultez :
- 📖 [Documentation complète](KAFKA_INTEGRATION.md)
- 🔍 [Logs du service](booking_service/src/main/resources/application.properties)
- 🌐 [Kafka UI](http://localhost:8090)
- 📊 [Métriques](http://localhost:8081/actuator/prometheus)

**Équipe** : Booking Service Team  
**Version** : 1.0.0  
**Date** : Juin 2024
