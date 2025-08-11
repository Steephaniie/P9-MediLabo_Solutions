# P9-MediLabo_Solutions

##  Détection du risque de diabète - application en Microservice
Projet de développement Java Spring Boot permettant aux praticiens de consulter, enrichir et évaluer les données médicales de patients afin de détecter les risques de diabètes de type 2.

##  Architecture 
### Microservices
L'application est organisée en **microservices** :
- 'patient-service' : gestion des informations personnelles (BDD relationnelle-SQL)
- 'notes-services' : gestion des observations des médecins (BDD NoSQL - MongoDB)
- 'assessment-service' : calcul du niveau de risque de diabète
- 'gateway-service': microservice de roulage basé sur Spring cloud gateway
- 'front-service' : application web front-end minimale et sobre
Chaque microservice est **indépendant, dockérisé**, et communique via le **gateway**. 

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
- MySQL (patients) - normalisation 3NF
- MongoDB (notes)

### Déploiement
- Docker + Docker-compose


##  Fonctionnalités par Sprint
###  Sprint 1 : gestion des patients 
- consultation des patients
- ajout d'un patient
- modification d'un patient
- suppression d'un patient
- recherche d'un patient
- exportation des patients
- BDD relationnelle-SQL normalisée en 3NF
- Authentification basique sécurisée via Spring Sécurity
- Première version de l'interface utilisateur (liste + fiche patient)

###  Sprint 2 : notes médicales (MongoDB)


###  Sprint 3 : évaluation du risque 


##  Installation et lancement


##  Microservices exposés


##  Sécurité - Authentification 


##  Données de tests


##  Green code - éco-conception 





##  Auteure
Stéphanie Leulliette — Développeuse Java Back-End