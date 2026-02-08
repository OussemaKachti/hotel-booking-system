# 🧪 Guide de Test Manuel - Kafka Integration

## 📋 Vue d'ensemble

Ce guide te permet de tester l'intégration Kafka **étape par étape** sans utiliser de scripts automatisés.

**Temps estimé** : 15-20 minutes  
**Prérequis** : Docker installé, ports 8081, 8090, 9092 disponibles

---

## 🚀 ÉTAPE 1 : Démarrer l'Infrastructure Kafka

### 1.1 Démarrer Docker Compose

```bash
cd c:\Users\Oussema\hotel-booking-system\infrastructure
docker-compose up -d kafka zookeeper kafka-ui
```

### 1.2 Vérifier que les Containers sont UP

```bash
docker ps
```

**Résultat attendu** : Tu dois voir 3 containers :
```
CONTAINER ID   IMAGE                              STATUS
xxx            confluentinc/cp-kafka:7.5.0        Up
xxx            confluentinc/cp-zookeeper:7.5.0    Up
xxx            provectuslabs/kafka-ui:latest      Up
```

### 1.3 Attendre que Kafka soit prêt

```bash
# Attendre 30 secondes pour que Kafka démarre complètement
```

⏱️ **Attends 30 secondes** avant de passer à l'étape suivante.

---

## 🎯 ÉTAPE 2 : Démarrer le Booking Service

### 2.1 Ouvrir un nouveau terminal

```bash
cd c:\Users\Oussema\hotel-booking-system\booking_service
```

### 2.2 Démarrer le service

```bash
mvn spring-boot:run
```

### 2.3 Attendre le message de démarrage

**Cherche dans les logs** :
```
Started BookingServiceApplication in X.XXX seconds
```

⚠️ **Ne ferme pas ce terminal !** Le service doit rester actif.

### 2.4 Vérifier que le service répond

Ouvre un **nouveau terminal** et teste :

```bash
curl http://localhost:8081/actuator/health
```

**Résultat attendu** :
```json
{"status":"UP"}
```

✅ Si tu vois `"status":"UP"`, le service est prêt !

---

## 🌐 ÉTAPE 3 : Vérifier Kafka UI

### 3.1 Ouvrir Kafka UI dans le navigateur

```
http://localhost:8090
```

### 3.2 Vérifier les Topics

1. Clique sur **"Topics"** dans le menu de gauche
2. Tu dois voir **4 topics** :
   - ✅ `booking.created`
   - ✅ `booking.cancelled`
   - ✅ `booking.completed`
   - ✅ `booking.dlq`

📸 **Prends une capture d'écran** si tu veux garder une trace !

### 3.3 Vérifier qu'ils sont vides

1. Clique sur **`booking.created`**
2. Clique sur **"Messages"**
3. Tu dois voir : **"No messages found"**

✅ C'est normal, nous n'avons pas encore publié d'événements.

---

## 📤 ÉTAPE 4 : Créer une Réservation (BookingCreatedEvent)

### 4.1 Préparer la requête

Ouvre un **nouveau terminal** (garde le service actif dans l'autre).

### 4.2 Créer une réservation avec cURL

```bash
curl -X POST http://localhost:8081/api/bookings ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":101,\"roomId\":202,\"hotelId\":303,\"checkInDate\":\"2026-02-15\",\"checkOutDate\":\"2026-02-18\",\"numberOfGuests\":2,\"totalPrice\":450.00}"
```

**📝 Note** : Le `^` est pour Windows PowerShell. Si tu es sur CMD, c'est correct. Sur Linux/Mac, utilise `\` au lieu de `^`.

### 4.3 Analyser la réponse

**Résultat attendu** :
```json
{
  "id": 1,
  "userId": 101,
  "roomId": 202,
  "hotelId": 303,
  "confirmationNumber": "BK-XXXXXXXX",
  "checkInDate": "2026-02-15",
  "checkOutDate": "2026-02-18",
  "numberOfGuests": 2,
  "totalPrice": 450.00,
  "status": "CONFIRMED",
  "createdAt": "..."
}
```

✅ **Note l'ID** : Dans mon exemple, c'est `1`. Tu en auras besoin pour l'annulation.

### 4.4 Vérifier les logs du service

**Retourne dans le terminal du service** et cherche ce message :

```
📤 Publishing BookingCreated event to topic: booking.created - Key: 1
✅ BookingCreated event published successfully to booking.created - Offset: 0, Partition: 0
```

✅ Si tu vois ces 2 lignes, l'événement a été publié avec succès !

---

## 🔍 ÉTAPE 5 : Vérifier le Message dans Kafka UI

### 5.1 Rafraîchir Kafka UI

1. Retourne sur **http://localhost:8090**
2. Clique sur **Topics** > **booking.created**
3. Clique sur **Messages**

### 5.2 Observer le message

Tu dois voir **1 message** avec :

**Key** : `1`  
**Value** : JSON complet de l'événement

```json
{
  "bookingId": 1,
  "userId": 101,
  "roomId": 202,
  "hotelId": 303,
  "checkInDate": "2026-02-15",
  "checkOutDate": "2026-02-18",
  "numberOfGuests": 2,
  "totalPrice": 450.00,
  "status": "CONFIRMED",
  "eventTimestamp": "2026-02-07T...",
  "eventType": "BOOKING_CREATED"
}
```

### 5.3 Vérifier les métadonnées

- **Partition** : 0
- **Offset** : 0
- **Timestamp** : Date/heure actuelle

✅ **BRAVO !** Ton premier événement Kafka est publié ! 🎉

---

## ❌ ÉTAPE 6 : Annuler la Réservation (BookingCancelledEvent)

### 6.1 Annuler avec cURL

**⚠️ Remplace `1` par l'ID de ta réservation si différent**

```bash
curl -X PATCH http://localhost:8081/api/bookings/1/cancel ^
  -H "Content-Type: application/json" ^
  -d "{\"cancellationReason\":\"Test manuel Kafka\"}"
```

### 6.2 Analyser la réponse

**Résultat attendu** :
```json
{
  "id": 1,
  "status": "CANCELLED",
  "cancellationReason": "Test manuel Kafka",
  ...
}
```

✅ Le statut doit être **"CANCELLED"**.

### 6.3 Vérifier les logs

**Dans les logs du service**, cherche :

```
📤 Publishing BookingCancelled event to topic: booking.cancelled - Key: 1
✅ BookingCancelled event published successfully to booking.cancelled - Offset: 0, Partition: 0
```

✅ Événement d'annulation publié !

---

## 🔍 ÉTAPE 7 : Vérifier l'Événement d'Annulation

### 7.1 Dans Kafka UI

1. Clique sur **Topics** > **booking.cancelled**
2. Clique sur **Messages**
3. Tu dois voir **1 message**

```json
{
  "bookingId": 1,
  "userId": 101,
  "roomId": 202,
  "hotelId": 303,
  "checkInDate": "2026-02-15",
  "checkOutDate": "2026-02-18",
  "cancellationReason": "Test manuel Kafka",
  "status": "CANCELLED",
  "eventTimestamp": "...",
  "eventType": "BOOKING_CANCELLED"
}
```

✅ **PARFAIT !** Les 2 événements sont publiés ! 🎉

---

## 📊 ÉTAPE 8 : Vérifier les Métriques Prometheus

### 8.1 Accéder à l'endpoint Prometheus

```bash
curl http://localhost:8081/actuator/prometheus
```

### 8.2 Filtrer les métriques Kafka

**Windows PowerShell** :
```powershell
Invoke-WebRequest http://localhost:8081/actuator/prometheus | Select-String "kafka_publish"
```

**CMD** :
```bash
curl http://localhost:8081/actuator/prometheus | findstr kafka_publish
```

### 8.3 Analyser les métriques

**Tu devrais voir** :

```
kafka_publish_success_total{service="booking"} 2.0
kafka_publish_failure_total{service="booking"} 0.0
kafka_publish_dlq_total{service="booking"} 0.0
kafka_publish_duration_seconds_count{service="booking"} 2.0
kafka_publish_duration_seconds_sum{service="booking"} 0.05...
```

**Analyse** :
- ✅ `success_total = 2.0` → 2 événements publiés (created + cancelled)
- ✅ `failure_total = 0.0` → Aucun échec
- ✅ `dlq_total = 0.0` → Aucun message en DLQ (c'est bien !)
- ✅ `duration_seconds_sum` → Temps total de publication

✅ **Toutes les métriques sont correctes !** 📈

---

## 🔴 ÉTAPE 9 : Vérifier la Dead Letter Queue (DLQ)

### 9.1 Vérifier que la DLQ est vide

1. Dans Kafka UI : **Topics** > **booking.dlq**
2. Clique sur **Messages**
3. Tu dois voir : **"No messages found"**

✅ **C'est normal !** La DLQ est vide car nous n'avons eu aucune erreur.

### 9.2 Comprendre la DLQ

La DLQ recevrait des messages si :
- Kafka est inaccessible après 3 retries
- Problème de sérialisation JSON
- Timeout réseau

Comme tout fonctionne bien, elle reste vide. 👍

---

## 🎯 ÉTAPE 10 : Résumé et Validation Finale

### ✅ Checklist de Validation

Coche mentalement chaque élément :

- [ ] ✅ Kafka, Zookeeper, Kafka-UI démarrés
- [ ] ✅ Booking Service démarré (port 8081)
- [ ] ✅ 4 topics créés automatiquement
- [ ] ✅ Réservation créée avec succès
- [ ] ✅ Événement `booking.created` visible dans Kafka UI
- [ ] ✅ Réservation annulée avec succès
- [ ] ✅ Événement `booking.cancelled` visible dans Kafka UI
- [ ] ✅ Métriques Prometheus accessibles
- [ ] ✅ 2 succès, 0 échecs, 0 DLQ
- [ ] ✅ Logs du service montrent les publications

---

## 🧪 TESTS SUPPLÉMENTAIRES (Optionnel)

### Test 1 : Créer Plusieurs Réservations

```bash
# Réservation 2
curl -X POST http://localhost:8081/api/bookings ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":102,\"roomId\":203,\"hotelId\":303,\"checkInDate\":\"2026-03-01\",\"checkOutDate\":\"2026-03-05\",\"numberOfGuests\":3,\"totalPrice\":800.00}"

# Réservation 3
curl -X POST http://localhost:8081/api/bookings ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":103,\"roomId\":204,\"hotelId\":303,\"checkInDate\":\"2026-03-10\",\"checkOutDate\":\"2026-03-12\",\"numberOfGuests\":1,\"totalPrice\":300.00}"
```

**Vérifie** : `kafka_publish_success_total` devrait être à `4.0`

---

### Test 2 : Vérifier le Retry Automatique

**Arrête Kafka temporairement** :

```bash
docker-compose stop kafka
```

**Essaye de créer une réservation** :

```bash
curl -X POST http://localhost:8081/api/bookings ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":999,\"roomId\":999,\"hotelId\":999,\"checkInDate\":\"2026-04-01\",\"checkOutDate\":\"2026-04-05\",\"numberOfGuests\":2,\"totalPrice\":500.00}"
```

**Dans les logs, tu verras** :
```
❌ Failed to publish BookingCreated event to booking.created: ...
📤 Publishing BookingCreated event to topic: booking.created - Key: X  (retry 1)
❌ Failed to publish BookingCreated event to booking.created: ...
📤 Publishing BookingCreated event to topic: booking.created - Key: X  (retry 2)
❌ Failed to publish BookingCreated event to booking.created: ...
📤 Publishing BookingCreated event to topic: booking.created - Key: X  (retry 3)
🔴 All retries exhausted for BookingCreatedEvent. Sending to DLQ. BookingId: X
⚠️ Message sent to DLQ: booking.created - Key: X - Attempts: 3
```

**Redémarre Kafka** :

```bash
docker-compose start kafka
```

**Vérifie la DLQ dans Kafka UI** → Tu verras 1 message avec l'erreur !

---

## 🛑 ÉTAPE 11 : Arrêter Proprement

### 11.1 Arrêter le Booking Service

Dans le terminal du service, appuie sur **CTRL+C**

### 11.2 Arrêter Docker Compose

```bash
cd c:\Users\Oussema\hotel-booking-system\infrastructure
docker-compose down
```

**Ou pour garder les données** :

```bash
docker-compose stop
```

---

## 📊 TABLEAU DE BORD FINAL

| Élément                | Status | Valeur          |
|------------------------|--------|-----------------|
| Kafka                  | ✅     | Running         |
| Booking Service        | ✅     | Running         |
| Topics créés           | ✅     | 4/4             |
| Événements publiés     | ✅     | 2+ (created + cancelled) |
| Succès Prometheus      | ✅     | 2.0+            |
| Échecs Prometheus      | ✅     | 0.0             |
| Messages DLQ           | ✅     | 0               |
| Retry fonctionne       | ✅     | Oui (test optionnel) |

---

## 🎓 Ce Que Tu As Appris

1. ✅ **Démarrer** une infrastructure Kafka avec Docker
2. ✅ **Créer** des réservations via API REST
3. ✅ **Publier** des événements Kafka automatiquement
4. ✅ **Visualiser** les messages dans Kafka UI
5. ✅ **Monitorer** avec Métriques Prometheus
6. ✅ **Vérifier** la Dead Letter Queue
7. ✅ **Tester** le retry mechanism (optionnel)

---

## 🆘 Troubleshooting

### ❌ Problème : "Connection refused" sur port 8081

**Cause** : Le service n'est pas démarré  
**Solution** : Vérifie que `mvn spring-boot:run` est actif

---

### ❌ Problème : "No messages found" dans Kafka UI

**Cause** : L'événement n'a pas été publié  
**Solution** : Vérifie les logs du service pour voir `✅ Event published successfully`

---

### ❌ Problème : Topics n'apparaissent pas dans Kafka UI

**Cause** : Kafka pas encore prêt ou service pas démarré  
**Solution** : Attends 30 secondes et redémarre le Booking Service

---

### ❌ Problème : curl ne fonctionne pas

**Solution Windows PowerShell** : Utilise `Invoke-RestMethod` à la place :

```powershell
$body = @{
    userId = 101
    roomId = 202
    hotelId = 303
    checkInDate = "2026-02-15"
    checkOutDate = "2026-02-18"
    numberOfGuests = 2
    totalPrice = 450.00
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8081/api/bookings" -Method POST -Body $body -ContentType "application/json"
```

---

## 🎉 FÉLICITATIONS !

Tu as testé **manuellement** toute l'intégration Kafka ! 🚀

**Prochaines étapes** :
1. Implémenter des **consumers** dans d'autres services
2. Ajouter **Grafana** pour visualiser les métriques
3. Tester en **production** avec plus de partitions/replicas

---

**Besoin d'aide ?** Consulte :
- [KAFKA_INTEGRATION.md](booking_service/KAFKA_INTEGRATION.md) - Doc complète
- [ARCHITECTURE_KAFKA.md](ARCHITECTURE_KAFKA.md) - Diagrammes

**Version** : 1.0.0  
**Date** : Février 2026
