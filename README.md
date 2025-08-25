# P9-MediLabo_Solutions

##  Détection du risque de diabète - application en Microservice
Projet de développement Java Spring Boot permettant aux praticiens de consulter, enrichir et évaluer les données médicales de patients afin de détecter les risques de diabètes de type 2.

##  Architecture 
### Microservices
L'application est organisée en **microservices** :
- 'gateway' : microservice de routage basé sur Spring cloud gateway
- 'front'   : application web front-end minimale et sobre
- 'patient' : gestion des informations personnelles (BDD relationnelle-SQL)
- 'note'    : gestion des observations des médecins (BDD NoSQL - MongoDB)
- 'rapport' : calcul du niveau de risque de diabète
Chaque microservice est **indépendant, dockérisé**, et communique entre eux. 

###  Structure du projet 


###  Technologies
- Java 21
- Spring Boot 
- Sping Data JPA
- Spring Cloud 
- Spring Data MongoDB
- Netflix Eureka (service Discovery)
- OpenFeign (communication interservice)
- Postman 
- Git 
- Thymeleaf 
- Bootstrap 
- HTLM5/CSS3
- Maven 
- junit 5
- Mockito 


### Bases de Données 
- H2 (patients)
- MongoDB (notes)

### Déploiement
- Docker + Docker-compose


##  Fonctionnalités attendues
###  Sprint 1 : microservices gestion des patients 
- consultation des patients
- ajout d'un patient
- modification d'un patient
- suppression d'un patient
- recherche d'un patient
- exportation des patients
- BDD relationnelle-SQL normalisée en 3NF
- Authentification basique sécurisée via Spring Sécurity
- Première version de l'interface utilisateur (liste + fiche patient)

###  Sprint 2 : microservice notes médicales (MongoDB)
- ajout de notes libres à chaque patient
- consultation de l'historique des notes
- conservation du format d'écriture
- exportation des notes
- BDD NoSQL MongoDB

###  Sprint 3 : microservice évaluation du risque 
- analyse des notes à la recherche de termes déclencheurs
- calcul du niveau de risque selon l'âge, le sexe, et le nombre de déclencheurs
- affichage du niveau de risque dans la fiche patient 

###  Front-end 
- Affichage liste des patients
- affichage détaillée : données personnelles, notes, risque
- interface simple et intuitive

###  Gateway
- routage des requêtes vers les microservices backend
- centralisation de la sécurité (authentification)

###  Post Sprint  : dockerisation et Green code
- Dockerisation chaque microservice (dockerfile individuel)
- créer un docker-compose.yml global (avec services backend, frontend, gateway, BDD SQL/MAngoDB)
- Recherche personnelle et résumé ci-dessous


##  Installation et lancement avec l'application Docker

### 1. Prérequis 
- Docker : https://www.docker.com/
- Docker-compose : https://docs.docker.com/compose/install/

### 2. cloner le repository 
git clone https://github.com/Steephaniie/P9-MediLabo_Solutions
cd nom-du-repo

### 3. lancer tous les services
docker-compose up --build


##  Microservices exposés
- Gateway : http://localhost:8080/
- Patient : http://localhost:8081/
- Front : http://localhost:8082/
- Notes : http://localhost:8083/
- Rapport : http://localhost:8084/
- Eureka : http://localhost:8761/

##  Sécurité - Authentification 


##  Données de tests


##  Green code - éco-conception 
##  Enjeux
 Le Green code vise à réduire l'empreinte écologique du code en optimisant : 
- la consommation mémoire 
- les cycles CPU
- Le volume de données échangées

##  Bonnes pratiques appliquées
- microservices découplés : chargement limité à ce qui est nécessaire 
- utilisation de DTO : uniquement les données utiles transitent entre les microservices 
- 
- dockerisation propre : image légères     

##  Pistes d'amélioration 
-



##  Auteure 
Developpé dans le cadre du projet P9 de la formation developpeur Java - openclasserooms
auteur : Stéphanie Leulliette — Développeuse Java Back-End
