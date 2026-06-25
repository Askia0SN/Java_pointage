# Guide de mise en place étape par étape

Projet : Système de gestion de pointage des professeurs  
Stack demandée : JavaFX 21, Hibernate ORM 6.4.x, MySQL 8, Maven, JDK 17 ou 21

Ce guide reprend le sujet du projet et le transforme en plan d'action concret. L'objectif est de construire le projet progressivement, sans commencer par l'interface graphique trop tôt. La bonne stratégie est : modèle, base, Hibernate, DAO, services, puis JavaFX.

---

## 0. Ce qu'il faut livrer à la fin

Avant de coder, garde ces livrables en tête :

- Code source complet du projet Maven.
- Rapport technique de 3 pages maximum.
- Vidéo de démonstration de 3 à 7 minutes.
- Modélisation UML, par exemple avec PlantUML.

Dans ce dossier, la modélisation PlantUML existe déjà ici :

- `docs/uml/pointage-domain.puml`

On garde donc PlantUML comme support UML.

---

## 1. Préparer l'environnement

Installe ou vérifie les outils suivants :

- JDK 17 ou JDK 21.
- Maven 3.9 ou plus.
- MySQL Server 8.
- IntelliJ IDEA.
- SceneBuilder 21.
- JavaFX SDK 21 si nécessaire.

Vérifie dans PowerShell :

```powershell
java -version
mvn -version
mysql --version
```

Si `java` ou `mvn` n'est pas reconnu, il faut corriger les variables d'environnement avant de continuer.

---

## 2. Créer le projet Maven

Depuis IntelliJ :

1. Crée un nouveau projet Maven.
2. Choisis le JDK 17 ou 21.
3. Mets comme groupId :

```text
sn.epf
```

4. Mets comme artifactId :

```text
pointage-professeurs
```

5. Vérifie que la structure ressemble à ceci :

```text
src/
  main/
    java/
    resources/
  test/
    java/
pom.xml
```

Ensuite, crée les packages :

```text
sn.epf.pointage
sn.epf.pointage.config
sn.epf.pointage.model
sn.epf.pointage.model.enums
sn.epf.pointage.dao
sn.epf.pointage.service
sn.epf.pointage.ui
sn.epf.pointage.security
```

Dans `src/main/resources`, prévois :

```text
fxml/
css/
images/
hibernate.cfg.xml
```

---

## 3. Configurer le `pom.xml`

Ajoute les dépendances principales :

- `javafx-controls`
- `javafx-fxml`
- `hibernate-core`
- `mysql-connector-j`
- `jbcrypt`
- `jasperreports`

Exemple de base :

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <javafx.version>21</javafx.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>${javafx.version}</version>
    </dependency>

    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>${javafx.version}</version>
    </dependency>

    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.4.4.Final</version>
    </dependency>

    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.3.0</version>
    </dependency>

    <dependency>
        <groupId>org.mindrot</groupId>
        <artifactId>jbcrypt</artifactId>
        <version>0.4</version>
    </dependency>

    <dependency>
        <groupId>net.sf.jasperreports</groupId>
        <artifactId>jasperreports</artifactId>
        <version>6.21.0</version>
    </dependency>
</dependencies>
```

Ajoute aussi le plugin JavaFX :

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-maven-plugin</artifactId>
            <version>0.0.8</version>
            <configuration>
                <mainClass>sn.epf.pointage.MainApp</mainClass>
            </configuration>
        </plugin>
    </plugins>
</build>
```

Teste ensuite :

```powershell
mvn clean compile
```

---

## 4. Créer la base MySQL

Connecte-toi à MySQL :

```powershell
mysql -u root -p
```

Crée la base :

```sql
CREATE DATABASE pointage_epf CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Tu peux aussi créer un utilisateur dédié :

```sql
CREATE USER 'pointage_user'@'localhost' IDENTIFIED BY 'pointage_pwd';
GRANT ALL PRIVILEGES ON pointage_epf.* TO 'pointage_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 5. Configurer Hibernate

Crée `src/main/resources/hibernate.cfg.xml`.

Exemple :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-configuration PUBLIC
        "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
        "https://hibernate.org/dtd/hibernate-configuration-3.0.dtd">

<hibernate-configuration>
    <session-factory>
        <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
        <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/pointage_epf?serverTimezone=Africa/Dakar&amp;useSSL=false&amp;allowPublicKeyRetrieval=true</property>
        <property name="hibernate.connection.username">pointage_user</property>
        <property name="hibernate.connection.password">pointage_pwd</property>

        <property name="hibernate.dialect">org.hibernate.dialect.MySQLDialect</property>
        <property name="hibernate.hbm2ddl.auto">update</property>
        <property name="hibernate.show_sql">true</property>
        <property name="hibernate.format_sql">true</property>

        <!-- Ajouter les mappings au fur et à mesure -->
        <!-- <mapping class="sn.epf.pointage.model.Professeur"/> -->
    </session-factory>
</hibernate-configuration>
```

Crée ensuite `HibernateConfig.java` dans `sn.epf.pointage.config`.

Objectif :

- Charger la configuration Hibernate.
- Créer une seule `SessionFactory`.
- Fournir une méthode `getSessionFactory()`.
- Fournir une méthode `shutdown()`.

Important : teste Hibernate avant de coder toute l'application.

Crée un petit `main()` temporaire :

```java
public class TestHibernate {
    public static void main(String[] args) {
        HibernateConfig.getSessionFactory().openSession().close();
        System.out.println("Connexion Hibernate OK");
        HibernateConfig.shutdown();
    }
}
```

Si ce test ne passe pas, ne commence pas JavaFX.

---

## 6. Créer les énumérations

Dans `sn.epf.pointage.model.enums`, crée :

- `TypeContrat` : `VACATAIRE`, `PERMANENT`
- `Role` : `ADMIN`, `SCOLARITE`, `PROFESSEUR`
- `FrequenceCours` : `HEBDOMADAIRE`, `BIMENSUELLE`
- `StatutSeance` : `PLANIFIEE`, `REALISEE`, `ANNULEE`, `REPORTEE`
- `TypePointage` : `DEBUT`, `FIN`
- `StatutPointage` : `A_L_HEURE`, `EN_RETARD`
- `StatutRapport` : `EN_ATTENTE`, `VALIDE`, `PAYE`
- `TypeAlerte` : `RETARD`, `ABSENCE`
- `ActionConnexion` : `CONNEXION`, `DECONNEXION`, `ECHEC_CONNEXION`
- `ResultatPointage` : `SUCCES`, `EN_RETARD`, `TROP_TOT`, `PROF_INACTIF`, `DEJA_POINTE`, `SEANCE_INTROUVABLE`

Ces enums évitent les chaînes de caractères éparpillées partout.

---

## 7. Créer les entités JPA

Dans `sn.epf.pointage.model`, crée une classe par entité.

Ordre conseillé :

1. `Professeur`
2. `Utilisateur`
3. `Cours`
4. `Salle`
5. `Assignation`
6. `PeriodiciteCours`
7. `SeancePlanifiee`
8. `Pointage`
9. `RapportMensuel`
10. `Alerte`
11. `JournalConnexion`

Règles à respecter :

- Chaque entité a `@Entity`.
- Chaque table a `@Table(name = "...")`.
- Chaque id a `@Id` et `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- Les enums utilisent `@Enumerated(EnumType.STRING)`.
- Les collections utilisent `FetchType.LAZY`.
- Les dates utilisent `LocalDate`, `LocalTime` ou `LocalDateTime`.
- Évite `java.util.Date`.

Exemple minimal :

```java
@Entity
@Table(name = "professeurs")
public class Professeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String matricule;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeContrat typeContrat;

    private BigDecimal tauxHoraireXOF;
    private LocalDate dateEmbauche;
    private boolean actif = true;
}
```

Quand une entité est créée, ajoute son mapping dans `hibernate.cfg.xml`.

Exemple :

```xml
<mapping class="sn.epf.pointage.model.Professeur"/>
<mapping class="sn.epf.pointage.model.Utilisateur"/>
```

Après chaque groupe d'entités, lance :

```powershell
mvn clean compile
```

Puis démarre le test Hibernate pour vérifier que les tables se créent.

---

## 8. Créer la couche DAO

Dans `sn.epf.pointage.dao`, crée :

- `GenericDAO<T, ID>`
- `AbstractDAO<T, ID>`
- `ProfesseurDAO`
- `SeanceDAO`
- `PointageDAO`
- `RapportDAO`

Le DAO générique doit fournir :

- `save(T entity)`
- `findById(ID id)`
- `findAll()`
- `update(T entity)`
- `delete(ID id)`
- `exists(ID id)`

Règle importante :

- Les transactions sont ouvertes dans les DAO.
- En cas de succès : `commit`.
- En cas d'erreur : `rollback`.
- Les contrôleurs JavaFX n'appellent jamais Hibernate directement.

Exemples de méthodes spécialisées :

```text
ProfesseurDAO
- findByMatricule(String matricule)
- findActifs()
- searchByNom(String nom)

SeanceDAO
- findSeancesDuJour()
- findSansPointage()
- findByProfesseurAndMois(Long professeurId, int mois, int annee)

PointageDAO
- findBySeanceAndType(Long seanceId, TypePointage type)
- countByProfesseurAndMois(Long professeurId, int mois, int annee)

RapportDAO
- findByProfesseurAndPeriode(Long professeurId, int mois, int annee)
- findNonPayes()
```

À ce stade, crée un `main()` de test console :

1. Créer un professeur.
2. Le sauvegarder.
3. Le relire par id.
4. Le rechercher par matricule.
5. Le désactiver.

Si les DAO marchent en console, la suite sera beaucoup plus simple.

---

## 9. Créer les services métier

Dans `sn.epf.pointage.service`, crée les services dans cet ordre :

1. `EnrolementService`
2. `PointageService`
3. `RapportService`
4. `AuthService`
5. `DashboardService`

### 9.1 `EnrolementService`

Responsabilités :

- Valider les champs obligatoires.
- Générer le matricule.
- Hacher le mot de passe initial avec BCrypt.
- Créer le professeur.
- Créer le compte utilisateur lié.
- Désactiver un professeur.
- Assigner un cours.
- Générer les séances planifiées depuis une périodicité.

Format du matricule :

```text
EPF-ANNEE-INITIALES-SEQUENTIEL
```

Exemple :

```text
EPF-2026-AD-001
```

### 9.2 `PointageService`

C'est le coeur du projet.

Règles à appliquer :

- RG-01 : le pointage début est autorisé entre 15 minutes avant et 5 minutes après le début de séance.
- RG-02 : une séance devient `REALISEE` si le pointage `DEBUT` existe.
- RG-03 : après 5 minutes de retard, le pointage est `EN_RETARD` et une alerte est créée.
- RG-05 : un professeur inactif ne peut pas pointer.

Pseudo-algorithme :

```text
pointer(seanceId, professeurId, typePointage)
1. Charger la séance.
2. Charger le professeur.
3. Refuser si le professeur est inactif.
4. Vérifier si ce type de pointage existe déjà.
5. Comparer l'heure actuelle avec l'heure de début de la séance.
6. Refuser si l'utilisateur pointe trop tôt.
7. Marquer EN_RETARD si l'écart dépasse +5 minutes.
8. Enregistrer le pointage.
9. Si type = DEBUT, passer la séance en REALISEE.
10. Retourner un ResultatPointage.
```

### 9.3 `RapportService`

Responsabilités :

- Générer un rapport mensuel.
- Bloquer la génération s'il reste des séances `PLANIFIEE` dans le mois.
- Compter seulement les séances `REALISEE`.
- Calculer les heures réalisées.
- Calculer le montant en XOF.
- Valider un rapport.
- Exporter en PDF si tu fais le bonus.

---

## 10. Créer l'authentification et les rôles

Dans `sn.epf.pointage.security`, crée :

- `SessionContext`
- `PasswordService`
- éventuellement `AccessControlService`

`PasswordService` utilise BCrypt :

```java
public String hash(String plainPassword) {
    return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
}

public boolean matches(String plainPassword, String hash) {
    return BCrypt.checkpw(plainPassword, hash);
}
```

`SessionContext` conserve :

- l'utilisateur connecté,
- son rôle,
- le professeur lié si le rôle est `PROFESSEUR`.

Règle importante :

Même si un bouton est caché dans JavaFX, le service doit aussi vérifier le rôle. Il ne faut jamais faire confiance uniquement à l'interface.

---

## 11. Créer JavaFX progressivement

Ne commence pas par toutes les vues. Fais une vue, teste, puis continue.

Ordre conseillé :

1. `login.fxml`
2. `dashboard.fxml`
3. `professeurs.fxml`
4. `prof_form.fxml`
5. `planning.fxml`
6. `pointage.fxml`
7. `rapports.fxml`

Crée aussi :

- `MainApp.java`
- `MainController.java`
- un contrôleur par FXML.

Principe :

- `MainApp` lance l'application.
- `MainController` gère la navigation.
- Les autres contrôleurs appellent les services.
- Aucun contrôleur ne fait de requête Hibernate directement.

---

## 12. Créer les vues demandées

### `login.fxml`

À faire :

- Champ login.
- Champ mot de passe.
- Bouton connexion.
- Message d'erreur.
- Redirection selon le rôle.

### `dashboard.fxml`

À faire :

- Nombre de séances du jour.
- Nombre de présents.
- Nombre d'absents.
- Taux de présence.
- Graphique sur les 6 derniers mois.
- Répartition vacataires/permanents.
- Alertes du jour.

### `professeurs.fxml`

À faire :

- TableView des professeurs.
- Recherche par nom.
- Filtre par type de contrat.
- Boutons Ajouter, Modifier, Désactiver.
- Affichage de la photo si possible.

### `planning.fxml`

À faire :

- Vue hebdomadaire.
- Liste des séances.
- Création d'une assignation.
- Création d'une périodicité.
- Vérification que les séances sont générées automatiquement.

### `pointage.fxml`

À faire :

- Liste des séances du jour du professeur connecté.
- Bouton Pointer.
- Bouton actif uniquement dans la fenêtre autorisée.
- Couleurs :
  - vert : à l'heure,
  - orange : retard,
  - rouge : absent ou impossible.

### `rapports.fxml`

À faire :

- Sélecteur mois.
- Sélecteur année.
- Sélecteur professeur.
- Tableau des séances réalisées.
- Total heures.
- Total montant.
- Bouton Générer rapport.
- Bouton Export PDF si bonus.

---

## 13. Ajouter le CSS EPF Africa

Crée `src/main/resources/css/styles.css`.

Couleurs demandées :

```css
.sidebar {
    -fx-background-color: #1A2744;
}

.nav-button:hover {
    -fx-background-color: #2E75B6;
}

.table-view .column-header {
    -fx-background-color: #1F4E79;
}

.table-view .column-header .label {
    -fx-text-fill: white;
}

.table-row-cell:selected {
    -fx-background-color: #D6E8F7;
}

.status-ok {
    -fx-text-fill: #1E8449;
}

.status-retard {
    -fx-text-fill: #D35400;
}

.status-absent {
    -fx-text-fill: #C0392B;
}

.dashboard-card {
    -fx-background-color: white;
    -fx-background-radius: 8;
    -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 8, 0, 0, 2);
}
```

---

## 14. Tester dans le bon ordre

Ne teste pas tout à la fin. Fais des petits tests.

### Test 1 : Hibernate

- La SessionFactory démarre.
- Les tables sont créées.

### Test 2 : DAO

- Créer un professeur.
- Créer un cours.
- Créer une salle.
- Créer une assignation.
- Créer des séances.

### Test 3 : services

- Enrôler un professeur.
- Créer son utilisateur.
- Pointer une séance.
- Vérifier que la séance passe en `REALISEE`.
- Générer un rapport.

### Test 4 : JavaFX

- Connexion admin.
- Dashboard.
- Enrôlement professeur.
- Planning.
- Connexion professeur.
- Pointage.
- Rapport.

---

## 15. Scénario exact pour la vidéo

Le sujet impose cet ordre. Prépare tes données pour que tout marche avant de lancer l'enregistrement.

1. Connexion `ADMIN`.
2. Affichage du dashboard avec des statistiques réelles.
3. Enrôlement d'un nouveau professeur vacataire avec photo.
4. Création d'un cours.
5. Assignation du cours au professeur avec périodicité hebdomadaire.
6. Déconnexion.
7. Reconnexion en tant que `SCOLARITE`.
8. Consultation du planning.
9. Vérification de la séance générée automatiquement.
10. Connexion avec le compte du professeur enrôlé.
11. Simulation du pointage `DEBUT` dans la fenêtre autorisée.
12. Reconnexion `ADMIN`.
13. Vérification du passage de la séance à `REALISEE`.
14. Génération et affichage du rapport mensuel.
15. Si implémenté : export PDF et ouverture du PDF.

Astuce : prépare une séance proche de l'heure réelle avant de tourner la vidéo, sinon le bouton de pointage risque d'être bloqué par la règle des 15 minutes.

---

## 16. Planning de travail conseillé

### Étape 1 - Base solide

Objectif :

- Modélisation validée.
- Projet Maven créé.
- Base MySQL créée.
- Hibernate connecté.

Résultat attendu :

- `mvn clean compile` fonctionne.
- Le test Hibernate affiche `Connexion Hibernate OK`.

### Étape 2 - Persistance

Objectif :

- Toutes les entités JPA.
- Tous les enums.
- DAO générique.
- DAO spécialisés.
- Tests console.

Résultat attendu :

- Tu peux créer, lire, modifier et supprimer des données sans JavaFX.

### Étape 3 - Métier

Objectif :

- `EnrolementService`
- `PointageService`
- `RapportService`
- `AuthService`

Résultat attendu :

- Les règles RG-01 à RG-06 sont testables en console.

### Étape 4 - Interface

Objectif :

- Login.
- Dashboard.
- Gestion des professeurs.
- Pointage.
- Planning.
- Rapports.
- CSS.

Résultat attendu :

- Le scénario vidéo peut être joué du début à la fin.

### Étape 5 - Finition

Objectif :

- Rapport technique 3 pages.
- Vidéo.
- Nettoyage du code.
- Vérification des livrables.

Résultat attendu :

- Dossier prêt à déposer sur Moodle.

---

## 17. Priorités si le temps manque

Si tu manques de temps, ne pars pas dans tous les bonus. Priorise :

1. Connexion avec rôles.
2. Enrôlement professeur.
3. Création cours + assignation.
4. Génération des séances.
5. Pointage avec règles métier.
6. Rapport mensuel simple.
7. Dashboard avec statistiques simples.
8. CSS propre.

Les bonus viennent seulement après :

- Export PDF professionnel.
- Emails.
- Import Excel.
- Statistiques avancées.
- Jours fériés sénégalais.

---

## 18. Erreurs fréquentes à éviter

- Commencer par JavaFX avant d'avoir testé Hibernate.
- Mettre des requêtes Hibernate dans les contrôleurs.
- Utiliser `FetchType.EAGER` sur les collections.
- Stocker les mots de passe en clair.
- Oublier `@Enumerated(EnumType.STRING)`.
- Oublier les contraintes `unique` sur matricule, email, login.
- Créer les rapports alors que des séances du mois sont encore `PLANIFIEE`.
- Tester le pointage avec une séance trop éloignée de l'heure actuelle.
- Faire la vidéo au dernier moment.

---

## 19. Mini check-list finale

Avant le rendu, vérifie :

- [ ] Le projet compile avec `mvn clean compile`.
- [ ] L'application démarre.
- [ ] La connexion ADMIN fonctionne.
- [ ] La connexion SCOLARITE fonctionne.
- [ ] La connexion PROFESSEUR fonctionne.
- [ ] Un professeur peut être enrôlé.
- [ ] Un cours peut être créé.
- [ ] Une assignation génère des séances.
- [ ] Le professeur peut pointer.
- [ ] Une séance pointée passe en `REALISEE`.
- [ ] Le rapport mensuel se génère.
- [ ] Les rôles bloquent les actions interdites.
- [ ] Le CSS est appliqué.
- [ ] Le rapport technique fait 3 pages maximum.
- [ ] La vidéo respecte le scénario imposé.

---

## 20. Ordre de fichiers recommandé

Quand tu commenceras à coder, suis cet ordre :

```text
1. pom.xml
2. hibernate.cfg.xml
3. HibernateConfig.java
4. enums
5. entités JPA
6. GenericDAO.java
7. AbstractDAO.java
8. DAO spécialisés
9. services
10. SessionContext et sécurité
11. MainApp.java
12. FXML + contrôleurs
13. CSS
14. tests manuels
15. rapport + vidéo
```

Si tu suis cet ordre, tu construis le projet comme une maison : fondations d'abord, interface à la fin.
