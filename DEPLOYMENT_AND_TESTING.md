# 🎬 Guide de Déploiement et Tests - Système de Réservation

## 📋 Prérequis

### Software Required:
- ✅ Java JDK 17+ (installé en `C:\Program Files\Java\jdk-17`)
- ✅ MySQL Server (pour la base de données)
- ✅ Maven (inclus - Maven Wrapper dans le projet)
- ✅ IDE (IntelliJ IDEA, Eclipse, ou VS Code recommandé)

### Configuration Système:
- ✅ RAM: Minimum 4GB
- ✅ Espace disque: 1GB libre
- ✅ Connexion réseau: Pour télécharger les dépendances Maven

---

## 🚀 Étape 1: Configuration de l'Environnement

### Windows PowerShell

```powershell
# 1. Définir JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# 2. Vérifier Java
java -version
# Output: openjdk version "17.x.x" ...

# 3. Vérifier Maven Wrapper existe
cd "C:\Users\BOUZID\Desktop\Eprit-PIDEV-3A63-2026-Carthage_GG-Desktop--News-Comment\Eprit-PIDEV-3A63-2026-Carthage_GG-Desktop--News-Comment"
ls mvnw.cmd
# Output: mvnw.cmd exists

# 4. Vérifier MySQL est accessible
# Assurez-vous que MySQL est en cours d'exécution
```

### Créer la Base de Données

```sql
-- 1. Se connecter à MySQL
mysql -u root -p

-- 2. Exécuter le script de base de données
USE mysql;
SOURCE database.sql;

-- 3. Vérifier que les tables sont créées
USE carthage_gg;
SHOW TABLES;
-- Output: reservations, event, locations, users, ...

-- 4. Vérifier la table reservations
DESC reservations;
-- Output: 
-- | id | int(11) | AUTO_INCREMENT PRIMARY KEY |
-- | name | varchar(255) | NOT NULL |
-- | price | decimal(10,2) | NOT NULL |
-- | reservation_date | datetime | DEFAULT CURRENT_TIMESTAMP |
-- | event_id | int(11) | FOREIGN KEY |
-- | seats | int(11) | DEFAULT 1 |
-- | status | enum('WAITING','CONFIRMED','CANCELLED') | DEFAULT 'WAITING' |
```

---

## 🔨 Étape 2: Compilation du Projet

### Compiler avec Maven

```powershell
# 1. Naviguer au répertoire du projet
cd "C:\Users\BOUZID\Desktop\Eprit-PIDEV-3A63-2026-Carthage_GG-Desktop--News-Comment\Eprit-PIDEV-3A63-2026-Carthage_GG-Desktop--News-Comment"

# 2. Nettoyer et compiler
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd clean compile

# 3. Vérifier la compilation
# Output: [INFO] BUILD SUCCESS
```

### Option: Construire JAR exécutable

```powershell
# Créer un package JAR
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
.\mvnw.cmd package

# Output: 
# [INFO] Building jar: target\CarthageGG-1.0.jar
# [INFO] BUILD SUCCESS
```

---

## 🎮 Étape 3: Lancer l'Application

### Exécuter via Maven

```powershell
# 1. Définir JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# 2. Naviguer au dossier du projet
cd "C:\Chemin\Vers\Projet\Eprit-PIDEV-3A63-2026-Carthage_GG-Desktop--News-Comment\Eprit-PIDEV-3A63-2026-Carthage_GG-Desktop--News-Comment"

# 3. Lancer l'application
.\mvnw.cmd javafx:run

# Output:
# [INFO] Copying resources...
# [INFO] Running CarthageGG application...
# Application Window Opens ✅
```

Cela devrait ouvrir la fenêtre principale de CarthageGG.

---

## 🧪 Étape 4: Tests Détaillés

### TEST 1: Inscription/Connexion

```
OBJECTIF: S'assurer que l'authentification fonctionne

ÉTAPES:
1. L'application affiche l'écran de connexion
2. Connexion Admin:
   - Email: admin@carthagegg.tn
   - Mot de passe: admin123
3. ✅ VÉRIFIER:
   - Connexion réussie
   - Redirect vers le Dashboard

RÉSULTAT ATTENDU:
- Page d'accueil affichée
- Sidebar de navigation visible
```

### TEST 2: Navigation vers Events

```
OBJECTIF: Vérifier que la navigation vers Events fonctionne

ÉTAPES:
1. Depuis la page d'accueil
2. Cliquer sur "Events" dans la sidebar
3. ✅ VÉRIFIER:
   - Liste des événements chargée
   - Événements affichés avec titres et images
   - Boutons "View Details" présents

RÉSULTAT ATTENDU:
- Page Events affichée correctement
- Tous les événements visibles
```

### TEST 3: Consulter les Détails d'un Événement ✅ PRINCIPAL

```
OBJECTIF: Vérifier l'affichage correcte de EventDetails

ÉTAPES:
1. Depuis la liste Events
2. Cliquer sur un événement (ou "View Details")
3. ✅ VÉRIFIER:
   - Titre de l'événement affiché
   - Description visible
   - Localisation affichée
   - CARTE INTERACTIVE affichée (OpenStreetMap)
   - Bouton "Reserve Now" visible

RÉSULTAT ATTENDU:
- Page de détails affichée
- Carte interactive montre location
- Bouton "Reserve Now" prêt à être cliqué
```

### TEST 4: Créer une Réservation ⭐ CRITIQUE

```
OBJECTIF: Tester le système de réservation complet

ÉTAPES:
1. Dans EventDetails, cliquer sur "Reserve Now"
2. ✅ VÉRIFIER:
   - Formulaire de réservation s'affiche dans un Dialog
   - Header: "Make a Reservation"
   - Champs présents:
     * Full Name (TextField)
     * Number of Seats (Spinner 1-10)
     * Price Per Seat (read-only)
     * Total Price (read-only)
     * Event Details (section info)
   
3. Saisir les données:
   - Full Name: "Ahmed Ben Ali"
   - Seats: 3 (via spinner)
   
4. ✅ VÉRIFIER:
   - Total Price calculé: 3 × 25.00 = 75.00
   - Mise à jour au fur et à mesure des changements
   
5. Cliquer "Reserve Now"
   
6. ✅ VÉRIFIER:
   - Alerte de succès apparaît
   - Message: "Your reservation has been successfully created!"
   - Dialog se ferme

RÉSULTAT ATTENDU:
- Réservation créée en base de données
- Statut: WAITING
- Client peut voir confirmation
```

### TEST 5: Admin Voir les Réservations ⭐ CRITIQUE

```
OBJECTIF: Vérifier le tableau de réservations pour admin

ÉTAPES:
1. Connectez-vous en tant qu'Admin (si pas encore fait)
2. Allez à la section "Events Management"
3. Cliquez sur l'onglet "Reservations"

4. ✅ VÉRIFIER - Tableau contient:
   - Colonne ID: 1 (ou dernier ID)
   - Colonne Name: "Ahmed Ben Ali"
   - Colonne Price: "75.00"
   - Colonne Date: "DD MMM YYYY HH:MM" (ex: "27 Apr 2026 14:30")
   - Colonne Event: Titre de l'événement réservé
   - Colonne Status: "WAITING" (souligné ou en couleur)
   
5. ✅ VÉRIFIER - Boutons d'actions:
   - Bouton "PDF" (bleu)
   - Bouton "Edit" (or)
   - Bouton "Delete" (rouge)

RÉSULTAT ATTENDU:
- Tableau s'affiche sans erreur
- Données formatées correctement
- Tous les boutons fonctionnels
```

### TEST 6: Télécharger Ticket PDF ⭐ CRITIQUE

```
OBJECTIF: Vérifier la génération et téléchargement de PDF

ÉTAPES:
1. Dans le tableau Reservations, cliquer "PDF"
2. ✅ VÉRIFIER:
   - Dialogue FileChooser s'affiche
   - Emplacement par défaut: "Ticket_[id].pdf"
   
3. Choisir dossier (ex: Bureau)
4. Cliquer "Save"

5. ✅ VÉRIFIER:
   - Fichier créé: "Ticket_1_[timestamp].pdf"
   - Taille > 50 KB
   - PDF ouvert dans lecteur
   
6. Vérifier le contenu PDF:
   ✅ En-tête avec titre "RESERVATION TICKET"
   ✅ Numéro ticket: [ID]
   ✅ Nom client: Ahmed Ben Ali
   ✅ Nombre de places: 3
   ✅ Prix: 75.00 TND
   ✅ Nom événement: [Event Title]
   ✅ Date événement: [Event Date]
   ✅ Localisation: [Location Name]
   ✅ Adresse: [Location Address]
   ✅ Status: WAITING
   ✅ Couleurs thème (Cyan #00f0ff, Or #FFC107)
   ✅ Formatage professionnel

RÉSULTAT ATTENDU:
- PDF généré correctement
- Contient tous les champs attendus
- Formatage attrayant
- Peut être imprimé ou partagé
```

### TEST 7: Éditer une Réservation

```
OBJECTIF: Tester la modification de réservation

ÉTAPES:
1. Dans le tableau Reservations, cliquer "Edit" sur une ligne
2. ✅ VÉRIFIER:
   - Dialogue d'édition s'affiche
   - Champs pré-remplis avec données actuelles
   
3. Modifier:
   - Name: "Ahmed Ben Ali" → "New Name"
   - Status: "WAITING" → "CONFIRMED"
   
4. Cliquer "Save"
5. ✅ VÉRIFIER:
   - Tableau se rafraîchit
   - Changements visible:
     * Nom modifié
     * Statut changé à CONFIRMED

RÉSULTAT ATTENDU:
- Réservation modifiée en base de données
- Tableau affiche les nouveaux détails
- Pas d'erreur
```

### TEST 8: Supprimer une Réservation

```
OBJECTIF: Tester la suppression de réservation

ÉTAPES:
1. Dans le tableau Reservations, cliquer "Delete"
2. ✅ VÉRIFIER:
   - Alerte de confirmation: "Are you sure?"
   
3. Cliquer "OK" pour confirmer
4. ✅ VÉRIFIER:
   - Réservation disparaît du tableau
   - Tableau se rafraîchit automatiquement
   
5. Vérifier la DB:
   - SELECT * FROM reservations WHERE id=[id];
   - Résultat: Vide (réservation supprimée)

RÉSULTAT ATTENDU:
- Réservation supprimée de la BD
- Tableau mis à jour
- Pas de trace de la réservation
```

---

## 📊 Checklist de Validation Complète

### Phase 1: Installation
- [ ] Java JDK 17 installé et JAVA_HOME défini
- [ ] MySQL serveur en cours d'exécution
- [ ] Base de données `carthage_gg` créée
- [ ] Toutes les tables créées avec succès

### Phase 2: Compilation
- [ ] `mvn clean compile` réussit
- [ ] Aucune erreur de compilation
- [ ] Pas de warnings critiques

### Phase 3: Application
- [ ] Application se lance sans erreur
- [ ] Écran de connexion visible
- [ ] Connexion admin fonctionne

### Phase 4: Front-End
- [ ] Navigation fonctionne
- [ ] Page Events affichée
- [ ] Détails événement chargent
- [ ] Carte Leaflet s'affiche
- [ ] Bouton "Reserve Now" visible

### Phase 5: Réservation
- [ ] Clic sur "Reserve Now" ouvre dialogue
- [ ] Formulaire chargé correctement
- [ ] Spinner fonctionne (1-10)
- [ ] Calcul prix automatique correct
- [ ] Création de réservation réussit
- [ ] Alerte de succès affichée

### Phase 6: Admin Panel
- [ ] Onglet Reservations affichée
- [ ] Tableau contient réservations
- [ ] Toutes les colonnes visibles
- [ ] Données formatées correctement

### Phase 7: PDF & Actions
- [ ] Bouton PDF généère fichier
- [ ] PDF contient toutes les infos
- [ ] PDF formaté correctement
- [ ] Edit fonctionne
- [ ] Delete fonctionne

### Phase 8: Base de Données
- [ ] Réservations sauvegardées en DB
- [ ] Modifications reflétées en DB
- [ ] Suppressions reflétées en DB
- [ ] Intégrité référentielle maintenue

---

## 🔍 Dépannage

### Problème: Application ne démarre pas

```
SYMPTÔME:
Error: JAVA_HOME not found

SOLUTION:
1. Vérifier JAVA_HOME:
   echo $env:JAVA_HOME
   
2. Définir si vide:
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
   
3. Relancer:
   .\mvnw.cmd javafx:run
```

### Problème: Formulaire de réservation ne s'affiche pas

```
SYMPTÔME:
ERROR: failed to load reservation form

SOLUTION:
1. Vérifier FXML: ✅ Déjà corrigé
2. Vérifier le fichier existe:
   Test-Path "src\main\resources\com\carthagegg\fxml\front\ReservationForm.fxml"
   
3. Recompiler:
   .\mvnw.cmd clean compile
   
4. Relancer l'app
```

### Problème: Tableau des réservations vide

```
SYMPTÔME:
Admin panel affiche tableau vide

CAUSES POSSIBLES:
1. Aucune réservation créée
   → Créer une réservation d'abord
   
2. Erreur DB
   → Vérifier connexion MySQL
   → Vérifier table reservations existe
   → Vérifier données dans réservations
   
3. Requête échouée
   → Vérifier les logs
   → Vérifier les permissions BD
```

### Problème: PDF ne se génère pas

```
SYMPTÔME:
FileChooser n'apparaît pas ou PDF vide

SOLUTION:
1. Vérifier iText7 dependency dans pom.xml
   ✅ Déjà présent
   
2. Vérifier la réservation est valide
   → Tous les champs remplis
   → Event et location existent
   
3. Vérifier permissions dossier temp
   → C:\Users\[User]\AppData\Local\Temp
   
4. Vérifier logs pour erreurs
```

---

## 📈 Logs et Debugging

### Activer les logs

```java
// Dans EventDetailsController:
private static final Logger logger = LoggerFactory.getLogger(EventDetailsController.class);

// Avant de charger le formulaire:
logger.info("Loading reservation form for event: " + currentEvent.getId());

// En cas d'erreur:
logger.error("Failed to load form", e);
```

### Vérifier les Requêtes DB

```bash
# 1. Se connecter à MySQL
mysql -u root -p

# 2. Activer le logging
SET GLOBAL general_log = 'ON';
SET GLOBAL log_output = 'TABLE';

# 3. Exécuter les tests

# 4. Voir les requêtes:
SELECT * FROM mysql.general_log;

# 5. Désactiver:
SET GLOBAL general_log = 'OFF';
```

---

## ✅ Validation Finale

Une fois tous les tests passés:

```
✅ Système de réservation OPÉRATIONNEL
✅ CRUD complètement fonctionnel
✅ Interface utilisateur intuitive
✅ Admin panel fonctionnel
✅ PDF generation fonctionnel
✅ Base de données synchronisée
✅ Prêt pour production
```

---

## 📞 Support et Rapports de Bugs

En cas de problème:

1. **Noter l'erreur exacte** (copier le message complett)
2. **Vérifier les logs** (console et logs fichier)
3. **Reproduire le problème** (étapes exactes)
4. **Vérifier la configuration** (JAVA_HOME, MySQL, etc.)

---

**✅ Version**: Deployment Guide v1.0  
**📅 Date**: Avril 2026  
**📞 Support**: CarthageGG Development Team

