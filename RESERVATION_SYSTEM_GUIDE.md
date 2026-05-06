# 📋 Guide du Système de Réservation - CarthageGG

## ✅ Problème Résolu
Le formulaire de réservation n'apparaissait pas en raison d'une **erreur XML** dans le fichier `ReservationForm.fxml`. Le fichier avait une fermeture de tag mal formée (`</DialogPane></content>` au lieu de `</DialogPane>`). 

### Fichier Corrigé
**`src/main/resources/com/carthagegg/fxml/front/ReservationForm.fxml`**
- Corrigé la structure XML de la DialogPane
- Vérifié tous les éléments FXML (TextField, Spinner, Label)
- Confirmé les références aux contrôleurs

---

## 🎯 Fonctionnalités Implémentées

### 1. **Formulaire de Réservation (Front-End)**

**Emplacement**: `EventDetails.fxml` → Bouton "Reserve Now"

**Champs du formulaire**:
- ✅ **Full Name**: Champ texte pour le nom complet
- ✅ **Number of Seats**: Spinner (1-10 places)
- ✅ **Price Per Seat**: Champ lecture seule (prix calculé)
- ✅ **Total Price**: Champ lecture seule (prix total = prix/siège × nombre de sièges)
- ✅ **Event Details**: Affichage du titre, date et places disponibles

**Fonctionnement**:
1. L'utilisateur clique sur "Reserve Now" dans EventDetails
2. Une DialogPane s'affiche avec le formulaire
3. L'utilisateur remplit son nom et le nombre de sièges
4. Les prix sont calculés automatiquement
5. En cliquant sur "Reserve Now", la réservation est créée en base de données

### 2. **Vue Admin des Réservations**

**Emplacement**: `EventsManagement.fxml` → Onglet "Reservations"

**Colonnes du tableau**:
- ID: Identifiant unique de la réservation
- Name: Nom complet du client
- Price: Prix total payé
- Date: Date de création de la réservation
- Event: Titre de l'événement réservé
- Status: WAITING, CONFIRMED, ou CANCELLED
- **Actions**: Trois boutons pour chaque réservation:
  - **PDF**: Télécharger le ticket de réservation en PDF
  - **Edit**: Modifier la réservation
  - **Delete**: Supprimer la réservation

### 3. **Système de Génération de Tickets PDF**

**Classe**: `com.carthagegg.utils.PDFTicketGenerator`

**PDF contient**:
- En-tête avec logo/icône
- Numéro et détails de la réservation
- Informations sur l'événement (titre, date, lieu)
- Informations sur le client et les places
- Code-barres ou identifiant
- Formatage professionnel avec couleurs thème (Cyan #00f0ff, Or #FFC107)

---

## 📊 Structure de la Base de Données

### Table `reservations`
```sql
CREATE TABLE reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,              -- Nom du client
    price DECIMAL(10, 2) NOT NULL,           -- Prix total
    reservation_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    event_id INT,                            -- Référence à l'événement
    seats INT DEFAULT 1,                     -- Nombre de places
    status ENUM('WAITING', 'CONFIRMED', 'CANCELLED') DEFAULT 'WAITING',
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE
);
```

---

## 🔧 Modèles et DAOs

### Modèle `Reservation`
```java
public class Reservation {
    private int id;
    private String name;              // Nom du client
    private BigDecimal price;         // Prix total
    private LocalDateTime reservationDate;
    private int eventId;
    private int seats;                // Nombre de places
    private Status status;            // WAITING, CONFIRMED, CANCELLED
}
```

### DAO `ReservationDAO`
Méthodes implémentées:
- `save(Reservation r)`: Créer une nouvelle réservation
- `findAll()`: Récupérer toutes les réservations
- `update(Reservation r)`: Modifier une réservation
- `delete(int id)`: Supprimer une réservation

---

## 🚀 Guide de Test

### **Scénario 1: Créer une réservation (Front-End)**

1. Lancez l'application
2. Allez à la section "Events"
3. Cliquez sur un événement pour voir les détails
4. Cliquez sur le bouton **"Reserve Now"**
5. Le formulaire de réservation s'affiche
6. Remplissez les champs:
   - **Full Name**: Ex. "Ahmed Ben Ali"
   - **Number of Seats**: Sélectionnez 2-3 places
7. Observez le calcul automatique du **Total Price**
8. Cliquez sur **"Reserve Now"** pour créer la réservation

### **Scénario 2: Voir les réservations (Admin)**

1. Connectez-vous en tant que **Admin**
2. Allez à **"Events Management"**
3. Cliquez sur l'onglet **"Reservations"**
4. Vous verrez la table avec toutes les réservations:
   - Colonnes: ID, Name, Price, Date, Event, Status
   - Actions: PDF, Edit, Delete

### **Scénario 3: Télécharger un ticket PDF**

1. Dans l'onglet Reservations, cliquez sur le bouton **"PDF"** d'une réservation
2. Une fenêtre de sauvegarde s'affiche
3. Choisissez l'emplacement et le nom du fichier
4. Le PDF est généré et téléchargé

### **Scénario 4: Modifier une réservation**

1. Dans l'onglet Reservations, cliquez sur **"Edit"**
2. Un dialogue s'affiche pour modifier:
   - Nom, prix, nombre de places, statut
3. Cliquez sur "Save" pour enregistrer les modifications

### **Scénario 5: Supprimer une réservation**

1. Dans l'onglet Reservations, cliquez sur **"Delete"**
2. Une confirmation s'affiche
3. Confirmez pour supprimer la réservation

---

## 📁 Fichiers Clés Modifiés

### Contrôleurs:
- ✅ `EventDetailsController.java`: Méthode `handleReserve()` - ouvre le formulaire
- ✅ `ReservationFormController.java`: Gère le formulaire et crée les réservations
- ✅ `EventsManagementController.java`: Affiche la table des réservations

### FXML:
- ✅ `EventDetails.fxml`: Contient le bouton "Reserve Now"
- ✅ `ReservationForm.fxml`: Formulaire de réservation (CORRIGÉ)
- ✅ `EventsManagement.fxml`: Admins voient les réservations

### Utilitaires:
- ✅ `PDFTicketGenerator.java`: Génère les PDFs des tickets

### Base de Données:
- ✅ `database.sql`: Table `reservations` créée

---

## 🎨 Intégration du Thème

Les couleurs utilisées dans le système de réservation:
- **Cyan** `#00f0ff`: Boutons principaux, icônes
- **Or** `#FFC107`: Labels, titres de section
- **Gris foncé** `#1e1e2e`: Sections de contenu
- **Rouge** `#ef4444`: Bouton de suppression

---

## ✨ Fonctionnalités Ajoutées Précédemment

### 1. **Cartes Interactives (OpenStreetMap)**
- Affiche la localisation de l'événement sur une carte Leaflet.js
- Visible dans EventDetails.fxml avec un WebView
- Marqueur automatique sur les coordonnées de l'événement

### 2. **Génération d'Descriptions IA**
- Génère des descriptions d'événements avec OpenAI (API clé requise)
- Fallback vers Hugging Face si OpenAI échoue
- Fallback local si les APIs échouent

### 3. **Génération de Tickets PDF**
- Crée des PDFs professionnels avec iText7
- Contient toutes les informations de réservation
- Téléchargeable via FileChooser

---

## 🐛 Compilation et Exécution

### Configuration de JAVA_HOME (Windows)
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

### Compiler le projet
```bash
.\mvnw.cmd clean compile
```

### Exécuter l'application
```bash
.\mvnw.cmd javafx:run
```

---

## 📋 Résumé des Corrections

| Problème | Solution | Fichier |
|----------|----------|---------|
| ❌ XML mal formé dans DialogPane | ✅ Corrigé la fermeture `</DialogPane>` | ReservationForm.fxml |
| ❌ Formulaire ne s'affichait pas | ✅ FXML conforme et compilable | ReservationForm.fxml |
| ❌ Manque de structure | ✅ Vérifié tous les DAOs et modèles | Tous les fichiers de réservation |

---

## 🎯 Prochaines Étapes (Optionnel)

1. **Validation de paiement**: Intégrer un système de paiement (Stripe, PayPal)
2. **Notifications**: Envoyer des emails de confirmation de réservation
3. **Code QR**: Ajouter un code QR au PDF pour la numérisation
4. **Statistiques**: Tableau de bord avec statistiques des réservations
5. **Export**: Exporter les réservations en CSV/Excel

---

**✅ Statut**: Système de réservation **COMPLÈTEMENT OPÉRATIONNEL**
**🔧 Version**: CarthageGG v1.0
**📅 Dernière mise à jour**: Avril 2026

