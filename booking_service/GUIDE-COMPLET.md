# 🎯 Guide de Test Complet - Booking Service

## ✅ Ce qui a été implémenté

### 1. **CRUDs de base** ✓
- Créer, lire, modifier, annuler réservations
- Calcul automatique des prix
- Génération numéro de confirmation

### 2. **Kafka Producer** ✓
- Événements : `booking.created`, `booking.cancelled`
- Gestion des erreurs si Kafka est down
- Topics auto-créés

### 3. **Feign Clients mockés** ✓
- RoomServiceClient
- HotelServiceClient  
- UserServiceClient
- Fallback automatique si services indisponibles

### 4. **Infrastructure Docker** ✓
- Kafka + Zookeeper
- Kafka UI
- Keycloak

---

## 🚀 Test Complet

### **Étape 1 : Démarrer l'infrastructure**

```powershell
cd C:\Users\Oussema\hotel-booking-system\infrastructure
docker-compose up -d
```

Attendre 30 secondes, puis vérifier :
```powershell
docker-compose ps
```

Vous devriez voir :
- ✅ zookeeper (Port 2181)
- ✅ kafka (Port 9092)
- ✅ kafka-ui (Port 8090)
- ✅ keycloak (Port 8080)

### **Étape 2 : Vérifier Kafka UI**

Ouvrir http://localhost:8090

Vous devriez voir le cluster Kafka "local"

### **Étape 3 : Démarrer Booking Service**

```powershell
cd C:\Users\Oussema\hotel-booking-system\booking_service
mvn spring-boot:run
```

Attendre le message : `Started BookingServiceApplication`

### **Étape 4 : Tester les CRUDs (sans Kafka)**

Dans un nouveau PowerShell :
```powershell
cd C:\Users\Oussema\hotel-booking-system\booking_service
.\test-simple.ps1
```

Résultat attendu : ✅ Tous les tests passent

### **Étape 5 : Tester avec Kafka**

```powershell
.\test-kafka.ps1
```

Dans les logs du service, vous devriez voir :
```
Publishing BookingCreatedEvent for booking ID: 1
✓ BookingCreatedEvent published successfully: bookingId=1, offset=0
Publishing BookingCancelledEvent for booking ID: 1
✓ BookingCancelledEvent published successfully: bookingId=1, offset=1
```

### **Étape 6 : Visualiser les événements dans Kafka UI**

1. Ouvrir http://localhost:8090
2. Cliquer sur "Topics"
3. Vous devriez voir :
   - `booking.created`
   - `booking.cancelled`
4. Cliquer sur un topic → "Messages"
5. Voir les événements JSON publiés

### **Étape 7 : Consommer les messages manuellement**

```powershell
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic booking.created --from-beginning
```

Vous verrez tous les événements `booking.created` en JSON

---

## 🧪 Tests des Feign Clients

Les Feign Clients sont configurés avec des **fallbacks mockés**.

### Test 1 : Services indisponibles (utilise les mocks)

Vos collègues n'ont pas encore leurs services → Les fallbacks retournent des données mockées automatiquement.

C'est transparent ! Le Booking Service fonctionne quand même.

### Test 2 : Quand les services seront disponibles

Quand Room/Hotel/User Services seront prêts :
1. Mettre à jour les URLs dans `application.properties`
2. Les appels Feign se connecteront automatiquement
3. Les fallbacks ne seront utilisés qu'en cas d'erreur

---

## 📊 Voir les données en BDD

1. Ouvrir http://localhost:8081/h2-console
2. JDBC URL : `jdbc:h2:mem:bookingdb`
3. Username : `sa`
4. Password : (vide)
5. Query : `SELECT * FROM BOOKINGS`

---

## 🔧 Commandes utiles

### Kafka
```powershell
# Lister les topics
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Voir les messages d'un topic
docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic booking.created --from-beginning

# Supprimer un topic
docker exec kafka kafka-topics --delete --topic booking.created --bootstrap-server localhost:9092
```

### Docker
```powershell
# Logs en temps réel
docker-compose logs -f kafka

# Redémarrer un service
docker-compose restart kafka

# Tout arrêter
docker-compose down
```

---

## ✅ Checklist de validation

- [ ] Infrastructure Docker démarre sans erreur
- [ ] Kafka UI accessible sur http://localhost:8090
- [ ] Booking Service démarre sur port 8081
- [ ] Tests CRUDs passent (test-simple.ps1)
- [ ] Événements Kafka publiés (logs du service)
- [ ] Topics visibles dans Kafka UI
- [ ] Messages visibles dans les topics
- [ ] H2 Console accessible avec données
- [ ] Feign Clients avec fallbacks configurés

---

## 🎯 Prochaines étapes (Intégration équipe)

1. **Partager le docker-compose.yml** avec l'équipe
2. **Définir les contrats d'API** avec vos collègues :
   - Format des réponses de Room/Hotel/User Services
   - Endpoints exacts
3. **Coordonner les tests d'intégration** :
   - Tester Room Service → Booking Service
   - Tester les consumers Kafka dans les autres services
4. **Configurer Eureka Server** (Service Discovery)
5. **Configurer API Gateway** (Point d'entrée unique)
6. **Sécuriser avec Keycloak** (OAuth2)

---

## 📝 Notes importantes

### Kafka
- Les topics sont créés automatiquement au premier message
- Les événements sont NON-BLOQUANTS : si Kafka est down, le service continue
- Les consumers (dans les autres services) doivent être configurés pour écouter les topics

### Feign Clients
- Les fallbacks permettent de développer en isolation
- Quand les vrais services seront disponibles, pas de code à changer
- Juste mettre à jour les URLs dans application.properties

### Base de données
- H2 en mémoire : données perdues au redémarrage
- Pour production : passer à PostgreSQL/MySQL

---

## 🆘 Problèmes courants

### "Failed to publish BookingCreatedEvent"
→ Kafka n'est pas démarré. Lancer `docker-compose up -d`

### "Port 9092 already in use"
→ Un autre Kafka tourne. Arrêter avec `docker-compose down` puis relancer

### "Connection refused to localhost:8082"
→ Normal ! Room Service n'est pas encore développé. Le fallback mock s'active automatiquement

### Kafka topics non créés
→ Les topics sont créés au premier message. Envoyer une réservation et ils apparaîtront

---

**🎉 Félicitations ! Votre Booking Service est prêt pour l'intégration !**
