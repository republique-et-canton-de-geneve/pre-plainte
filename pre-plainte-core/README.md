# App 11729 PPEL-formulaire-api

Ce projet est basé sur une architecture inspirée de [l’article de Herberto Graca](https://herbertograca.com/2019/06/05/reflecting-architecture-and-domain-in-code/) et met en œuvre **Spring Boot** dans un **projet multi-modules Maven** structuré autour de **trois modules principaux** :

----

## 📦 Structure du projet

11729-pre-plainte/\
├── pre-plainte-core/\
│ └── src/main/java/ch/ge/police/core/\
│ ├── domain/ # Couche métier pure\
│ │ └── model/ # Entités du domaine (Aggregats, Value Objects)\
│ ├── port/\
│ │ ├── in/ # Interfaces d’entrée (cas d’usage)\
│ │ └── out/ # Interfaces de sortie (persistence, messaging...)\
│ └── application/\
│ └── service/ # Implémentation des cas d’usage\
├── infrastructure/\
│ └── src/main/java/ch/ge/police/infrastructure/\
│ ├── configuration/ # Configuration technique (Spring)\
│ └── persistence/\
│ └── jpa/ # Implémentation des ports out (adaptateurs JPA)\
└── ui/\
│ └── src/main/java/ch/ge/police/ui/\
│ │ ├── controller/ # Adaptateurs web (REST controllers)\
│ │ ├── main/ # Classe @SpringBootApplication (bootstrap)\


---

## 🧠 Détail des responsabilités

### 📦 `core/`

#### `domain/model/`
- **Responsabilité :** Modélise le **métier** (entités, agrégats, objets de valeur).
- **Aucune dépendance technique.**
- ✅ Pure logique métier, testable sans Spring.

#### `port/in/`
- **Responsabilité :** Décrit ce que l’application **expose comme actions**.
- Ces interfaces sont **ciblées par les adaptateurs UI** (`ui`, REST, etc.)

#### `application/service/`
- **Responsabilité :** Implémente les interfaces de `port/in/`
- Contient la logique des **cas d’usage** (services applicatifs)
- Appelle les ports `out` pour accéder à la persistance, etc.

#### `port/out/`
- **Responsabilité :** Décrit les **dépendances externes** dont l’application a besoin (repository, email, files...)
- Ce sont les **interfaces que l’infrastructure implémente**

---

### 🧱 `infrastructure/`

- **Responsabilité :** Fournit les **implémentations techniques** des ports `out`
- Dépend de `core`, **mais `core` ne dépend jamais de lui**
- Contient aussi les `@Configuration` Spring

---

### 🌍 `ui/`

- **Responsabilité :** Expose les interfaces utilisateur (REST, Web, CLI, etc.)
- Contient :
    - Les controllers
    - Le point d’entrée `@SpringBootApplication`
- Appelle les `port.in` définis dans `core`
- Peut aussi contenir des DTOs, mappers, validation...

---

## 🔁 Flux de dépendances

    +--------------------------+
    |    Web Controller (UI)  |  ← REST, CLI, etc.
    +--------------------------+
                |
                v
    +--------------------------+
    |  Use Case (port.in impl) |  ← Implémente un "port in"
    +--------------------------+
                |
                v
    +--------------------------+
    |        Domain            |
    |  (Entity, Value Object)  |
    +--------------------------+
                |
                v
    +--------------------------+
    |  Port Out (ex: Repo)     |
    +--------------------------+
                |
                v
    +--------------------------+
    |  Adapter (JPA, etc)      |
    +--------------------------+

---

## ▶️ Lancer l'application

1. **Compiler le projet :**

bash
mvn clean install

## ▶️ Lancer uniquement le module UI

cd ui
mvn spring-boot:run

## 🧪 Tests
Les tests unitaires du domaine (core) sont sans Spring

Les tests d’intégration avec Spring sont dans infrastructure et ui

## ✅ Avantages de cette architecture
Domain-centric : le cœur métier est découplé de tout framework

Facile à tester : on peux tester le domaine sans Spring

Flexible : changement de framework ou d’interface sans impacter le métier

Lisible : code organisé autour de concepts métier, pas de techniques

## 🧰 Technologies utilisées
Java 21+

Spring Boot 3.x

Maven multi-modules

JPA/Hibernate (dans infrastructure)

REST (dans ui)

