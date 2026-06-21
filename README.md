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

### Lancer tous les tests de tous les microservices

Si vous souhaitez exécuter l'ensemble des suites de tests pour l'intégralité des microservices en une seule fois, vous pouvez lancer la commande suivante à la racine de ce dossier (`hireme-microservice`) :

```bash
for d in eureka-server hireme-auth-service hireme-gateway hireme-job-service hireme-matching-service hireme-resume-service; do 
  echo "Lancement des tests pour $d..."
  (cd "$d" && mvn test)
done
```
