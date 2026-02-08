# Kafka Integration - Booking Service

## 📋 Vue d'ensemble

Cette documentation décrit l'implémentation professionnelle de Kafka pour le **Booking Service** dans l'architecture microservices du système de réservation hôtelière.

### Architecture Event-Driven

```
┌─────────────────┐         ┌─────────────┐         ┌─────────────────┐
│  Booking Service│────────▶│Apache Kafka │────────▶│  Room Service   │
│                 │         │             │         │  User Service   │
│  (Producer)     │         │  Topics:    │         │  Analytics Svc  │
│                 │         │  - created  │         │  Review Service │
│                 │         │  - cancelled│         │                 │
│                 │         │  - completed│         │  (Consumers)    │
│                 │         │  - dlq      │         │                 │
└─────────────────┘         └─────────────┘         └─────────────────┘
```

---

## 🎯 Fonctionnalités Implémentées

### ✅ Configuration Production-Ready
- **Idempotence activée** : évite les duplications
- **Compression Snappy** : optimisation de la bande passante
- **Acknowledgment "all"** : garantie de durabilité
- **Batching optimisé** : meilleures performances

### ✅ Retry Mechanism
- **3 tentatives automatiques** avec backoff exponentiel (1s, 2s, 4s)
- **@Retryable** Spring Retry
- **Isolation des échecs** : n'affecte pas la transaction DB

### ✅ Dead Letter Queue (DLQ)
- **Topic dédié** : `booking.dlq`
- **Métadonnées enrichies** : stack trace, timestamps, nombre de tentatives
- **Retention 30 jours** : analyse approfondie des erreurs
- **Alerting ready** : logs critiques pour monitoring

### ✅ Monitoring & Métriques
- **Micrometer + Prometheus**
- **4 métriques clés** :
  - `kafka.publish.success` : publications réussies
  - `kafka.publish.failure` : échecs
  - `kafka.publish.dlq` : messages en DLQ
  - `kafka.publish.duration` : temps de publication
- **Endpoint** : `http://localhost:8081/actuator/prometheus`

### ✅ Tests Professionnels
- **@EmbeddedKafka** : tests d'intégration sans dépendance externe
- **Vérification des messages** : contenu, clé, payload
- **Vérification des métriques** : compteurs incrémentés

---

## 📁 Structure du Code

```
booking_service/
└── src/
    ├── main/
    │   └── java/
    │       └── com/hotel/booking/
    │           ├── kafka/
    │           │   ├── config/
    │           │   │   └── KafkaProducerConfig.java     # Configuration Kafka
    │           │   ├── exception/
    │           │   │   └── KafkaPublishException.java    # Exception custom
    │           │   ├── model/
    │           │   │   └── DlqMessage.java               # Modèle DLQ
    │           │   ├── BookingEventProducer.java         # Producer principal
    │           │   └── KafkaTopicConfig.java             # Configuration topics
    │           └── event/
    │               ├── BookingCreatedEvent.java          # DTO événement 1
    │               ├── BookingCancelledEvent.java        # DTO événement 2
    │               └── BookingCompletedEvent.java        # DTO événement 3
    └── test/
        └── java/
            └── com/hotel/booking/kafka/
                └── BookingEventProducerIntegrationTest.java  # Tests
```

---

## 🔧 Configuration

### Topics Kafka

| Topic               | Partitions | Replicas | Retention | Consommateurs                      |
|---------------------|------------|----------|-----------|-------------------------------------|
| `booking.created`   | 3 (prod)   | 2 (prod) | 7 jours   | Room Service, Analytics Service     |
| `booking.cancelled` | 3 (prod)   | 2 (prod) | 7 jours   | Room Service, User Service, Analytics|
| `booking.completed` | 3 (prod)   | 2 (prod) | 7 jours   | Review Service, Analytics Service   |
| `booking.dlq`       | 2 (prod)   | 2 (prod) | 30 jours  | Ops/Support (monitoring manuel)     |

> **Note** : En dev, 1 partition et 1 replica suffisent.

### Application Properties

```properties
# Kafka Producer - Configuration recommandée
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.compression-type=snappy
spring.kafka.producer.enable-idempotence=true
spring.kafka.producer.max-in-flight-requests-per-connection=5
spring.kafka.producer.request-timeout-ms=30000
spring.kafka.producer.delivery-timeout-ms=120000
```

---

## 📤 Événements Publiés

### 1. BookingCreatedEvent

**Déclenché** : Après création réussie d'une réservation

```json
{
  "bookingId": 1,
  "userId": 100,
  "roomId": 200,
  "hotelId": 300,
  "checkInDate": "2024-06-15",
  "checkOutDate": "2024-06-18",
  "numberOfGuests": 2,
  "totalPrice": 450.00,
  "status": "CONFIRMED",
  "eventTimestamp": "2024-06-08T14:30:00",
  "eventType": "BOOKING_CREATED"
}
```

**Use Cases** :
- Room Service : marquer la chambre comme réservée
- Analytics Service : statistiques de réservation

---

### 2. BookingCancelledEvent

**Déclenché** : Après annulation d'une réservation

```json
{
  "bookingId": 1,
  "userId": 100,
  "roomId": 200,
  "hotelId": 300,
  "checkInDate": "2024-06-15",
  "checkOutDate": "2024-06-18",
  "numberOfGuests": 2,
  "totalPrice": 450.00,
  "status": "CANCELLED",
  "cancellationReason": "Client request",
  "eventTimestamp": "2024-06-10T10:15:00",
  "eventType": "BOOKING_CANCELLED"
}
```

**Use Cases** :
- Room Service : libérer la chambre
- User Service : mise à jour historique client
- Analytics Service : taux d'annulation

---

### 3. BookingCompletedEvent

**Déclenché** : Après checkout du client (fin du séjour)

```json
{
  "bookingId": 1,
  "userId": 100,
  "roomId": 200,
  "hotelId": 300,
  "checkInDate": "2024-06-15",
  "checkOutDate": "2024-06-18",
  "numberOfGuests": 2,
  "numberOfNights": 3,
  "totalPrice": 450.00,
  "status": "COMPLETED",
  "eventTimestamp": "2024-06-18T11:00:00",
  "eventType": "BOOKING_COMPLETED"
}
```

**Use Cases** :
- Review Service : invitation à laisser un avis
- Analytics Service : calcul CA, satisfaction

---

## 🔄 Flux de Publication avec Retry

```
┌──────────────┐
│ BookingService│
└──────┬───────┘
       │ 1. publishBookingCreated(event)
       ▼
┌──────────────────┐
│  EventProducer   │◀─────────┐
└──────┬───────────┘          │
       │ 2. kafkaTemplate.send()  │ RETRY 1: après 1s
       ▼                          │ RETRY 2: après 2s
┌──────────────┐                 │ RETRY 3: après 4s
│Apache Kafka  │                 │
└──────┬───────┘                 │
       │ SUCCESS ✅              │
       │ ou                      │
       │ FAILURE ❌──────────────┘
       │ Si 3 échecs ⇩
       ▼
┌──────────────┐
│booking.dlq   │ (Dead Letter Queue)
└──────────────┘
```

---

## 📊 Monitoring

### Métriques Prometheus

Accessible sur : `http://localhost:8081/actuator/prometheus`

```promql
# Nombre total de publications réussies
kafka_publish_success_total{service="booking"}

# Nombre total d'échecs
kafka_publish_failure_total{service="booking"}

# Nombre de messages en DLQ
kafka_publish_dlq_total{service="booking"}

# Durée de publication (percentiles)
kafka_publish_duration_seconds_bucket{service="booking"}
```

### Dashboard Grafana (Recommandé)

**Panels à créer** :
1. **Taux de succès** : `kafka_publish_success` / (`kafka_publish_success` + `kafka_publish_failure`)
2. **Messages DLQ** : `kafka_publish_dlq` (alerter si > 0)
3. **Latence P95** : `kafka_publish_duration_seconds{quantile="0.95"}`
4. **Throughput** : `rate(kafka_publish_success[1m])`

---

## 🧪 Tests

### Lancer les Tests

```bash
cd booking_service
mvn test -Dtest=BookingEventProducerIntegrationTest
```

### Tests Couverts

✅ Publication réussie de `BookingCreatedEvent`  
✅ Publication réussie de `BookingCancelledEvent`  
✅ Publication réussie de `BookingCompletedEvent`  
✅ Vérification des clés de message  
✅ Vérification du contenu JSON  
✅ Vérification des métriques  

---

## 🚀 Déploiement

### 1. Démarrer l'Infrastructure

```bash
cd infrastructure
docker-compose up -d kafka zookeeper kafka-ui
```

### 2. Vérifier Kafka UI

Ouvrir : `http://localhost:8090`

Vérifier que les 4 topics existent :
- `booking.created`
- `booking.cancelled`
- `booking.completed`
- `booking.dlq`

### 3. Démarrer le Service

```bash
cd booking_service
mvn spring-boot:run
```

### 4. Vérifier les Métriques

```bash
curl http://localhost:8081/actuator/prometheus | grep kafka_publish
```

---

## 🛠️ Troubleshooting

### ❌ Problème : Messages n'arrivent pas aux consommateurs

**Solutions** :
1. Vérifier que Kafka tourne : `docker ps | grep kafka`
2. Vérifier les logs du producer : `LOG > Publishing event to topic`
3. Vérifier dans Kafka UI : `http://localhost:8090 > Topics > booking.created > Messages`

---

### ❌ Problème : Messages arrivent en DLQ

**Diagnostic** :
1. Consulter la DLQ dans Kafka UI
2. Analyser le champ `errorMessage` et `errorStackTrace`
3. Vérifier la connectivité réseau Kafka

**Actions** :
- Si erreur réseau : vérifier `bootstrap-servers`
- Si erreur sérialisation : vérifier les DTOs
- Si erreur timeout : augmenter `request-timeout-ms`

---

### ⚠️ Problème : Latence élevée (> 100ms)

**Optimisations** :
1. Activer compression : déjà activé (snappy)
2. Augmenter `batch.size` : tester 32768
3. Augmenter `linger.ms` : tester 20ms
4. Ajouter partitions : passer de 3 à 5

---

## 📈 Évolutions Futures

### Phase 2 (Court terme)
- [ ] Implémenter un Consumer dans Analytics Service
- [ ] Implémenter un Consumer dans Room Service
- [ ] Ajouter Grafana dashboard pour métriques

### Phase 3 (Moyen terme)
- [ ] Schema Registry (Avro) pour versioning des événements
- [ ] Circuit Breaker pattern (Resilience4j)
- [ ] Kafka Streams pour agrégations temps réel

### Phase 4 (Long terme)
- [ ] Multi-region Kafka pour haute disponibilité
- [ ] Event Sourcing complet (stockage événements)
- [ ] CQRS pattern (séparation lecture/écriture)

---

## 🔐 Sécurité

### TODO Production

**Actuellement** : Kafka en PLAINTEXT (développement uniquement)

**Pour production** :
1. Activer SSL/TLS
2. Activer SASL authentification
3. Implémenter ACLs Kafka
4. Chiffrement des données sensibles (GDPR)

---

## 👥 Support

**Équipe** : Booking Team  
**Canal Slack** : #booking-service  
**Runbook** : [Confluence - Kafka Runbook](link-to-be-added)

---

## 📚 Références

- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/reference/html/)
- [Apache Kafka Best Practices](https://kafka.apache.org/documentation/#bestpractices)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Spring Retry Documentation](https://github.com/spring-projects/spring-retry)

---

**Version** : 1.0.0  
**Date** : Juin 2024  
**Auteur** : Booking Service Team
