# Configuration du bouton RUN dans JetBrains IntelliJ IDEA

## Option 1 : Via l'interface IDE (recommandé)

Suivez ces étapes dans JetBrains IntelliJ IDEA :

1. **Allez à Run → Edit Configurations** (ou Shift + Alt + F10 → Edit Configurations)

2. **Cliquez sur le "+" pour ajouter une nouvelle configuration**

3. **Sélectionnez "Maven"** dans la liste des types de configuration

4. **Remplissez les champs :**
   - **Name:** `CarthageGG Run`
   - **Working directory:** `$PROJECT_DIR$` (le répertoire du projet)
   - **Command line:** `javafx:run`
   - **Run:** Sélectionnez cette configuration dans le dropdown

5. **Cliquez sur OK** pour enregistrer

6. **Cliquez sur le bouton Run ▶** (ou Shift + F10) pour lancer l'application

---

## Option 2 : Double-cliquez sur run.bat

Un fichier `run.bat` a été créé à la racine du projet. 
Vous pouvez double-cliquer dessus pour lancer l'application directement.

---

## Option 3 : Via un terminal intégré

1. Ouvrez le terminal dans JetBrains (View → Tool Windows → Terminal)
2. Tapez :
   ```bash
   mvnw.cmd javafx:run
   ```
3. Appuyez sur Entrée

---

## Dépannage

Si vous rencontrez des erreurs :

- **JAVA_HOME non défini :** Vérifiez que `C:\Program Files\Java\jdk-17` existe
- **Port en cours d'utilisation :** Fermez d'autres instances de l'application
- **Dépendances manquantes :** Exécutez d'abord `mvnw.cmd clean install`

---

**L'application démarre avec :**
- Email : anasbouzid250@gmail.com (ou créez un compte)
- Mot de passe : (selon votre base de données)

