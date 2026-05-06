# 🔧 Système de Réservation - Documentation Technique

## 📋 Vue d'ensemble

Ce document décrit l'architecture complète du système CRUD (Create, Read, Update, Delete) de réservation pour le projet CarthageGG.

---

## 🏗️ Architecture du Système

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (JavaFX)                        │
├─────────────────────────────────────────────────────────────────┤
│  EventDetailsController     ReservationFormController           │
│  (Reserve Now click)        (Form display & validation)         │
└──────────┬──────────────────────┬──────────────────────────────┘
           │                      │
           └──────────┬───────────┘
                      │ (Dialog/FXML)
┌─────────────────────▼──────────────────────────────────────────┐
│         ReservationForm.fxml (DialogPane)                      │
│  - Full Name TextField                                         │
│  - Seats Spinner (1-10)                                        │
│  - Price Fields (auto-calculated)                              │
│  - Event Details Display                                       │
└──────────────────────┬──────────────────────────────────────────┘
                       │
        ┌──────────────▼──────────────┐
        │   ReservationFormController  │
        │  - createReservation()       │
        │  - updateTotalPrice()        │
        └──────────────┬───────────────┘
                       │
        ┌──────────────▼──────────────┐
        │   ReservationDAO            │
        │  - save()                   │
        │  - findAll()                │
        │  - update()                 │
        │  - delete()                 │
        └──────────────┬───────────────┘
                       │
        ┌──────────────▼──────────────┐
        │   DatabaseConnection        │
        │   (MySQL Database)          │
        └─────────────────────────────┘
```

---

## 📊 Flux de Données

### **Create (Créer une réservation)**

```java
// 1. EventDetailsController: Utilisateur clique "Reserve Now"
@FXML
private void handleReserve() {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/com/carthagegg/fxml/front/ReservationForm.fxml")
    );
    DialogPane dialogPane = loader.load();
    ReservationFormController controller = loader.getController();
    controller.setEvent(currentEvent);  // Passe l'événement au formulaire
    
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setDialogPane(dialogPane);
    dialog.showAndWait();
}

// 2. ReservationFormController: Crée l'objet Reservation
public Reservation createReservation() {
    Reservation reservation = new Reservation();
    reservation.setName(fullNameField.getText());
    reservation.setPrice(new BigDecimal(totalPriceField.getText()));
    reservation.setSeats(seatsSpinner.getValue());
    reservation.setEventId(event.getId());
    reservation.setStatus(Reservation.Status.WAITING);
    
    // 3. ReservationDAO: Sauvegarde en base
    reservationDAO.save(reservation);
    return reservation;
}
```

### **Read (Lire les réservations)**

```java
// EventsManagementController: Charge et affiche
private void loadReservations() {
    try {
        reservationsList.setAll(reservationDAO.findAll());
        reservationsTable.setItems(reservationsList);
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
```

### **Update (Modifier une réservation)**

```java
// EventsManagementController: Modifie une réservation
private void handleEditReservation(Reservation reservation) {
    // Ouvre un dialogue d'édition
    reservation.setName(editedName);
    reservation.setStatus(newStatus);
    // ... autres champs
    
    reservationDAO.update(reservation);
}
```

### **Delete (Supprimer une réservation)**

```java
// EventsManagementController: Supprime une réservation
private void handleDeleteReservation(Reservation reservation) {
    reservationDAO.delete(reservation.getId());
    loadReservations(); // Rafraîchir la table
}
```

---

## 🔌 Interfaces et DAOs

### **ReservationDAO - Méthodes Principales**

```java
public class ReservationDAO {
    
    // CREATE
    public void save(Reservation r) throws SQLException {
        String sql = "INSERT INTO reservations 
            (name, price, reservation_date, event_id, seats, status) 
            VALUES (?,?,?,?,?,?)";
        // Implémentation...
    }
    
    // READ ALL
    public List<Reservation> findAll() throws SQLException {
        String sql = "SELECT * FROM reservations";
        // Retourne List<Reservation>
    }
    
    // READ SINGLE (optionnel)
    public Reservation findById(int id) throws SQLException {
        // À implémenter si nécessaire
    }
    
    // UPDATE
    public void update(Reservation r) throws SQLException {
        String sql = "UPDATE reservations SET 
            name=?, price=?, status=? WHERE id=?";
        // Implémentation...
    }
    
    // DELETE
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reservations WHERE id = ?";
        // Implémentation...
    }
}
```

---

## 🎯 Contrôleurs

### **ReservationFormController**

```java
public class ReservationFormController {
    
    @FXML private TextField fullNameField;
    @FXML private Spinner<Integer> seatsSpinner;
    @FXML private TextField priceField;
    @FXML private TextField totalPriceField;
    @FXML private Label eventTitleLabel;
    
    private Event event;
    private ReservationDAO reservationDAO;
    
    @FXML
    public void initialize() {
        // Initialise le spinner 1-10 places
        seatsSpinner.setValueFactory(
            new IntegerSpinnerValueFactory(1, 10, 1)
        );
        
        // Écoute les changements de places pour recalculer le prix
        seatsSpinner.valueProperty().addListener(
            (obs, oldVal, newVal) -> updateTotalPrice()
        );
    }
    
    public void setEvent(Event event) {
        this.event = event;
        // Affiche les détails de l'événement
        eventTitleLabel.setText(event.getTitle());
        // Set default price (25.00 TND)
        priceField.setText("25.00");
        updateTotalPrice();
    }
    
    private void updateTotalPrice() {
        BigDecimal pricePerSeat = new BigDecimal(priceField.getText());
        int seats = seatsSpinner.getValue();
        BigDecimal total = pricePerSeat.multiply(new BigDecimal(seats));
        totalPriceField.setText(total.toString());
    }
    
    public Reservation createReservation() {
        // Validation
        if (fullNameField.getText().trim().isEmpty()) {
            showAlert("Error", "Please enter your name");
            return null;
        }
        
        // Création
        try {
            Reservation reservation = new Reservation();
            reservation.setName(fullNameField.getText().trim());
            reservation.setPrice(new BigDecimal(totalPriceField.getText()));
            reservation.setReservationDate(LocalDateTime.now());
            reservation.setEventId(event.getId());
            reservation.setSeats(seatsSpinner.getValue());
            reservation.setStatus(Reservation.Status.WAITING);
            
            // Sauvegarde
            reservationDAO.save(reservation);
            return reservation;
        } catch (SQLException e) {
            showAlert("Error", "Failed: " + e.getMessage());
            return null;
        }
    }
}
```

### **EventsManagementController - Méthode setupReservationsTable()**

```java
private void setupReservationsTable() {
    // Configure les colonnes
    colResId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colResName.setCellValueFactory(new PropertyValueFactory<>("name"));
    colResPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
    
    // Date formatée
    colResDate.setCellValueFactory(cellData -> {
        if (cellData.getValue().getReservationDate() == null) 
            return new SimpleStringProperty("");
        return new SimpleStringProperty(
            cellData.getValue().getReservationDate()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
        );
    });
    
    // Titre de l'événement
    colResEvent.setCellValueFactory(cellData -> {
        Event e = eventsList.stream()
            .filter(ev -> ev.getId() == cellData.getValue().getEventId())
            .findFirst()
            .orElse(null);
        return new SimpleStringProperty(e != null ? e.getTitle() : "Unknown");
    });
    
    // Statut
    colResStatus.setCellValueFactory(cellData -> 
        new SimpleStringProperty(cellData.getValue().getStatus().name())
    );
    
    // Colonne d'actions avec boutons
    colResActions.setCellFactory(param -> new TableCell<Reservation, Void>() {
        private final Button pdfBtn = new Button("PDF");
        private final Button editBtn = new Button("Edit");
        private final Button deleteBtn = new Button("Delete");
        
        {
            pdfBtn.getStyleClass().add("btn-primary");
            editBtn.getStyleClass().add("btn-gold");
            deleteBtn.setStyle("-fx-background-color: #ef4444;");
            
            pdfBtn.setOnAction(e -> handleDownloadTicket(getTableView().getItems().get(getIndex())));
            editBtn.setOnAction(e -> handleEditReservation(getTableView().getItems().get(getIndex())));
            deleteBtn.setOnAction(e -> handleDeleteReservation(getTableView().getItems().get(getIndex())));
        }
        
        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : new HBox(10, pdfBtn, editBtn, deleteBtn));
        }
    });
}
```

---

## 📁 Structure des Fichiers FXML

### **ReservationForm.fxml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<DialogPane xmlns="http://javafx.com/javafx"
            xmlns:fx="http://javafx.com/fxml"
            fx:controller="com.carthagegg.controllers.front.ReservationFormController"
            prefWidth="500" prefHeight="600">
    
    <header>
        <VBox alignment="CENTER">
            <Label text="Make a Reservation" />
        </VBox>
    </header>
    
    <content>
        <ScrollPane fitToWidth="true">
            <VBox spacing="20" style="-fx-padding: 20;">
                <!-- Full Name Field -->
                <VBox spacing="15">
                    <Label text="FULL NAME" />
                    <TextField fx:id="fullNameField" />
                </VBox>
                
                <!-- Seats Spinner -->
                <VBox spacing="15">
                    <Label text="NUMBER OF SEATS" />
                    <Spinner fx:id="seatsSpinner" min="1" max="10" />
                </VBox>
                
                <!-- Price Fields -->
                <VBox spacing="15">
                    <Label text="PRICE PER SEAT" />
                    <TextField fx:id="priceField" editable="false" />
                </VBox>
                
                <VBox spacing="15">
                    <Label text="TOTAL PRICE" />
                    <TextField fx:id="totalPriceField" editable="false" />
                </VBox>
                
                <!-- Event Details -->
                <VBox spacing="15">
                    <Label text="EVENT DETAILS" />
                    <VBox style="-fx-background-color: #1e1e2e;">
                        <Label fx:id="eventTitleLabel" />
                        <Label fx:id="eventDateLabel" />
                        <Label fx:id="availableSeatsLabel" />
                    </VBox>
                </VBox>
            </VBox>
        </ScrollPane>
    </content>
    
    <ButtonType text="Reserve Now" buttonData="OK_DONE" />
    <ButtonType text="Cancel" buttonData="CANCEL_CLOSE" />
    
</DialogPane>
```

---

## 🗄️ Schéma Base de Données

### Table `reservations`

```sql
CREATE TABLE reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT 'Nom du client',
    price DECIMAL(10, 2) NOT NULL COMMENT 'Prix total',
    reservation_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Date de création',
    event_id INT NOT NULL COMMENT 'Référence événement',
    seats INT DEFAULT 1 COMMENT 'Nombre de places',
    status ENUM('WAITING', 'CONFIRMED', 'CANCELLED') DEFAULT 'WAITING' COMMENT 'Statut',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    INDEX idx_event_id (event_id),
    INDEX idx_status (status),
    INDEX idx_date (reservation_date)
);
```

### Statuts Possibles

| Statut | Description |
|--------|-------------|
| `WAITING` | Réservation créée, en attente de confirmation |
| `CONFIRMED` | Réservation confirmée par l'admin |
| `CANCELLED` | Réservation annulée |

---

## ✅ Tests Unitaires (Recommandé)

```java
public class ReservationDAOTest {
    
    private ReservationDAO dao;
    private Reservation testReservation;
    
    @Before
    public void setUp() {
        dao = new ReservationDAO();
        testReservation = new Reservation();
        testReservation.setName("Test User");
        testReservation.setPrice(new BigDecimal("50.00"));
        testReservation.setEventId(1);
        testReservation.setSeats(2);
        testReservation.setStatus(Reservation.Status.WAITING);
    }
    
    @Test
    public void testCreateReservation() throws SQLException {
        dao.save(testReservation);
        assertNotEquals(0, testReservation.getId());
    }
    
    @Test
    public void testFindAll() throws SQLException {
        List<Reservation> reservations = dao.findAll();
        assertNotNull(reservations);
        assertTrue(reservations.size() > 0);
    }
    
    @Test
    public void testUpdateReservation() throws SQLException {
        // Save first
        dao.save(testReservation);
        
        // Update
        testReservation.setStatus(Reservation.Status.CONFIRMED);
        dao.update(testReservation);
        
        // Verify (note: findById needed)
        // assertEquals(Reservation.Status.CONFIRMED, 
        //     dao.findById(testReservation.getId()).getStatus());
    }
    
    @Test
    public void testDeleteReservation() throws SQLException {
        dao.save(testReservation);
        int id = testReservation.getId();
        dao.delete(id);
        // Verify deletion
    }
}
```

---

## 🔍 Gestion des Erreurs

Les erreurs courantes et solutions:

| Erreur | Cause | Solution |
|--------|-------|----------|
| `failed to load reservation form` | FXML mal formé | Vérifier XML valide |
| `SQLException: Unknown column` | Colonne DB manquante | Vérifier `database.sql` |
| `NullPointerException in setEvent()` | Event null | Vérifier passage d'event |
| `BigDecimal calculation error` | Format invalide | Try-catch NumberFormatException |

---

## 🚀 Performance Considerations

```java
// ✅ BON: Lazy loading avec pagination
public List<Reservation> findByEvent(int eventId, int limit, int offset) 
    throws SQLException {
    String sql = "SELECT * FROM reservations WHERE event_id = ? LIMIT ? OFFSET ?";
    // Implémentation avec pagination
}

// ❌ MAUVAIS: Charger toutes les réservations
public List<Reservation> findAll() throws SQLException {
    // Peut devenir lent avec des millions de réservations
}

// ✅ BON: Index sur les colonnes fréquemment recherchées
CREATE INDEX idx_event_id ON reservations(event_id);
CREATE INDEX idx_status ON reservations(status);
```

---

## 🔐 Sécurité

### Risk Mitigation:

```java
// ✅ SECURE: Utiliser PreparedStatement (déjà implémenté)
String sql = "INSERT INTO reservations (...) VALUES (?,?,?,?,?,?)";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, userInput);  // Paramétré, sûr contre SQL injection

// ❌ INSECURE: Concaténation de strings
String sql = "INSERT INTO reservations VALUES ('" + userInput + "')";
// Vulnérable à SQL injection
```

---

## 📦 Dépendances Requises

```xml
<!-- pom.xml -->
<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>20.0.1</version>
    </dependency>
    
    <!-- MySQL Driver -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- iText7 pour PDF (pour tickets) -->
    <dependency>
        <groupId>com.itextpdf</groupId>
        <artifactId>itext7-core</artifactId>
        <version>7.2.5</version>
        <type>pom</type>
    </dependency>
</dependencies>
```

---

## 📝 Checklist Compilation

- ✅ JAVA_HOME défini sur JDK 17+
- ✅ Maven configuré
- ✅ Database MySQL accessible
- ✅ Toutes les tables créées (`database.sql`)
- ✅ FXML bien formé (XML standard)
- ✅ Imports corrects dans les classes Java
- ✅ No circular dependencies

---

## 🔄 Cycle de Vie d'une Réservation

```
1. CREATION (Front-End)
   User clique "Reserve Now" → Formulaire s'affiche

2. SAISIE
   User remplit nom + nombre de places

3. VALIDATION
   Vérification du nom non-vide
   Vérification du nombre de places

4. SAUVEGARDE
   INSERT dans table `reservations`
   Status = WAITING

5. AFFICHAGE (Admin)
   Admin voit la réservation dans le tableau

6. MODIFICATION
   Admin peut éditer la réservation
   Admin peut changer le statut

7. GÉNÉRATION PDF
   Admin clique "PDF"
   Ticket généré et téléchargé

8. SUPPRESSION
   Admin clique "Delete"
   Réservation supprimée de la table
```

---

**🔧 Version**: Technical Spec v1.0  
**📅 Date**: Avril 2026  
**👨‍💻 Responsable**: CarthageGG Development Team

