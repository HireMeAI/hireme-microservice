# HireMe — Microservices Java (Spring Boot)

Ce répertoire contient l'ensemble des microservices backend Java pour la plateforme HireMe. 
L'architecture s'articule autour de plusieurs services indépendants orchestrés et exposés via une Gateway.

## Architecture

* **eureka-server** : Annuaire de services pour la découverte
* **hireme-gateway** : Point d'entrée principal (API Gateway)
* **hireme-auth-service** : Gestion de l'authentification et des utilisateurs
* **hireme-job-service** : Gestion des offres d'emploi
* **hireme-resume-service** : Gestion des profils et CVs des candidats
* **hireme-matching-service** : Service de mise en relation offres / CVs

## Tests Automatisés

Les microservices étant indépendants et gérés via Maven sans `pom.xml` parent global à la racine, les tests doivent être exécutés soit individuellement, soit via un script.

### Lancer les tests d'un service spécifique

Utilisez le wrapper Maven (`mvnw`) situé dans le dossier du microservice cible.

Exemple pour le service d'authentification :

```bash
cd hireme-auth-service
mvn test
```

### Lancer tous les tests (Java + Python)

Un script global a été mis en place pour lancer **l'intégralité des tests de tous les microservices** (les applications Spring Boot et le moteur Python `hireme-ml-engine`) en une seule fois. 

À la racine du dossier `hireme-microservice`, lancez simplement :

```bash
for d in eureka-server hireme-auth-service hireme-gateway hireme-job-service hireme-matching-service hireme-resume-service; do 
  echo "Lancement des tests pour $d..."
  (cd "$d" && ./mvnw clean test)
done
```

Ce script vérifiera successivement chaque microservice Java avec `mvn test`, puis lancera les tests Python via `pytest`. Il s'arrêtera automatiquement si l'une des suites de tests échoue.
