# PayMyBuddy v3

Application web de transfert d'argent entre utilisateurs (type Venmo/PayPal simplifié) : inscription/connexion, ajout de "buddies" (contacts), transferts d'argent avec suivi de solde et historique des transactions.

## Stack technique

Java 21 · Spring Boot 3 · Spring Security (authentification, mots de passe hashés) · Spring Data JPA · Thymeleaf · MySQL · JUnit / Mockito

## Modèle de données

<img width="846" height="313" alt="Modèle Physique de Données" src="https://github.com/user-attachments/assets/ddf1b538-c759-4552-8c2e-d78adb6256d7" />

## Lancer le projet

Prérequis : Java 21, Maven (ou le wrapper fourni), une base MySQL locale.

```bash
git clone https://github.com/doui445/paymybuddy_v3.git
cd paymybuddy_v3
# créer la base MySQL "pay_my_buddy" et adapter src/main/resources/application.properties si besoin
./mvnw spring-boot:run
```

L'application est ensuite disponible sur `http://localhost:8080`.

## Tests

```bash
./mvnw test
```
