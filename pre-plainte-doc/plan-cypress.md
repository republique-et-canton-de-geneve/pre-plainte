# Plan Cypress - parcours formulaire de pre-plainte

## Objectif

Mettre en place une couverture Cypress qui limite les regressions sur le parcours principal du formulaire, sans chercher a tester exhaustivement chaque combinaison de champs.

Les tests doivent valider en priorite :

- l'acces au formulaire et la progression entre les etapes ;
- les validations bloquantes qui empechent une pre-plainte invalide ;
- les affichages conditionnels qui modifient fortement le formulaire ;
- les appels backend critiques, avec des stubs stables lorsque le test ne porte pas sur l'integration reelle ;
- le recapitulatif et la soumission de base.

Le challenge email ne fait pas l'objet d'un scenario Cypress dedie. Le parcours nominal verifie uniquement son orchestration IHM avec des appels de demande et de verification stubbes. La generation du code, son envoi reel, son expiration, son verrouillage et les autres regles internes restent couverts hors Cypress.

## Parcours fonctionnel a couvrir

Le formulaire suit le parcours suivant :

1. Informations generales
2. Verification email
3. Informations personnelles
4. Informations sur l'evenement
5. Rendez-vous
6. Recapitulatif
7. Validation finale

Les donnees sont persistantes via le store Pinia et le localStorage. Les tests peuvent donc demarrer soit depuis la premiere etape, soit depuis une etape avancee avec un etat localStorage prepare. Pour eviter des tests trop lents et fragiles, il faut reserver le parcours complet a quelques scenarios nominaux et utiliser des demarrages directs pour les validations ciblees.

## Etat actuel des tests Cypress

Les tests existants couvrent deja :

- l'acces au formulaire et les informations generales ;
- la verification email dans le parcours nominal avec des appels backend stubbes ;
- quelques affichages conditionnels dans les informations personnelles ;
- plusieurs validations de champs personnels ;
- le passage de l'etape informations personnelles vers l'evenement ;
- quelques validations sur le vol de vehicule ;
- le parcours nominal complet avec un vol simple saisi via l'IHM, un rendez-vous, le recapitulatif et la validation finale ;
- les appels critiques de demande et verification email, de chargement eSirius, de soumission et de creation du rendez-vous.

Les limites actuelles :

- le challenge email ne doit pas etre couvert par un scenario dedie Cypress, mais son orchestration doit rester presente dans le parcours nominal ;
- les tests utilisent beaucoup les libelles, ce qui les rend sensibles aux changements de traduction ou de wording ;
- les donnees RIPOL sont stubbees partiellement, surtout pour le vol de vehicule.

`InformationsPersonnelles.feature` est conserve pour l'instant, mais il doit etre repris proprement. Les futurs scenarios ne doivent plus dependre d'un etat implicite ou herite d'un scenario precedent. Chaque scenario doit initialiser explicitement son contexte, par exemple avec `pp-step`, `pp-data` et les stubs RIPOL necessaires.

## Mode de realisation prevu

Les scenarios seront generes progressivement, un par un, en suivant ce plan. A chaque demande de generation, il faut :

1. choisir le scenario cible dans ce document ;
2. verifier le parcours et les selecteurs dans le code existant ;
3. ajouter ou adapter seulement les steps necessaires ;
4. garder le scenario autonome avec nettoyage du localStorage ;
5. stubber les APIs externes lorsque le scenario ne teste pas explicitement l'integration reelle.

Pour les fichiers existants, la strategie est la reprise progressive :

- ne pas supprimer brutalement `InformationsPersonnelles.feature` ;
- remplacer ou restructurer ses scenarios au fur et a mesure ;
- introduire un step explicite du type `je suis sur l'etape informations personnelles` ;
- eviter les scenarios qui commencent par un simple `cy.visit("/")` lorsqu'ils ciblent une etape avancee ;
- conserver temporairement les scenarios utiles tant qu'ils ne sont pas remplaces par une version plus fiable.

Ordre de travail a respecter autant que possible :

1. stabiliser les helpers communs et les stubs ;
2. reprendre `AccesFormulaire.feature` pour l'etape informations generales ;
3. ne pas creer de feature dediee au challenge email, mais conserver son passage dans le parcours nominal ;
4. reprendre `InformationsPersonnelles.feature` proprement avec demarrage direct apres email verifie ;
5. reprendre ou completer les scenarios evenement, rendez-vous, recapitulatif et soumission.

## Priorite 1 - Parcours nominal minimal

### Scenario P1-1 - Demarrer une pre-plainte

Objectif : verifier que l'utilisateur peut passer l'etape d'informations generales.

Points a tester :

- le bouton Continuer est desactive tant que les deux confirmations ne sont pas cochees ;
- apres coche des confirmations, le bouton devient actif ;
- l'utilisateur arrive a l'etape Verification email.

Preconditions :

- captcha desactive en environnement de test, ou captcha simule avec un token.

### Scenario P1-2 - Informations personnelles nominales pour soi-meme

Objectif : valider le chemin le plus simple pour une personne qui declare pour elle-meme.

Points a tester :

- selection du lien avec la personne : Moi-meme ;
- saisie des champs obligatoires du declarant ;
- selection RIPOL de la nationalite et du genre ;
- selection du type de document d'identite ;
- numero de document obligatoire sauf cas document vole/perdu ;
- email deja verifie lors de l'etape precedente et conserve dans les donnees du parcours ;
- passage vers l'etape Informations sur l'evenement.

Donnees a privilegier :

- nationalite Suisse ;
- adresse en Suisse ;
- telephone suisse valide ;
- date de naissance avec age valide.

### Scenario P1-4 - Evenement nominal : vol d'objet simple

Objectif : couvrir un vol simple sans vehicule ni plaque, avec un objet informatique.

Points a tester :

- selection du type d'incident Vol ;
- saisie d'une periode valide ;
- selection du vol dans vehicule a Non ;
- selection d'une categorie d'objet simple ;
- selection d'un type d'objet RIPOL ;
- numero de serie obligatoire pour les categories concernees si l'option numero inconnu n'est pas cochee ;
- selection de l'absence de degradation ;
- adresse de l'evenement correspondant a l'adresse de la personne lesee ;
- passage vers le rendez-vous.

Le cas ou l'option numero inconnu permet de valider sans numero de serie reste un scenario court de validation metier, distinct du parcours nominal.

### Scenario P1-5 - Rendez-vous nominal

Objectif : verifier que l'utilisateur peut choisir un creneau compatible avec l'incident.

Points a tester :

- chargement des services et disponibilites eSirius ;
- filtrage des services compatibles avec un vol ;
- affichage d'au moins un creneau ;
- impossibilite de continuer sans creneau pour les cas qui en exigent un ;
- selection d'un creneau ;
- passage au recapitulatif.

Stubs recommandes :

- services contenant au moins un service dont le nom contient `vol` ;
- disponibilites futures dans la fenetre de 15 jours ;
- creneaux non expires, au moins une heure apres l'heure courante.

### Scenario P1-6 - Recapitulatif et soumission nominale

Objectif : valider que les donnees principales sont reprises et que la soumission aboutit.

Points a tester :

- affichage des informations personnelles principales ;
- affichage du type d'incident et des informations evenement ;
- affichage du creneau choisi ;
- appel de soumission de la pre-plainte ;
- creation du rendez-vous si un creneau a ete choisi ;
- arrivee sur la carte de validation finale.

Stubs recommandes :

- soumission pre-plainte retournant un `demandeId` ;
- creation rendez-vous retournant un `codeRdv`.

## Priorite 2 - Validations metier critiques

### Informations generales

Tests interessants :

- bouton Continuer bloque si une seule confirmation est cochee ;
- captcha requis si active ;
- retour et reprise de l'etape sans perte de donnees.

### Verification email

Pas de scenario Cypress dedie. Le parcours nominal saisit l'adresse email et le code, puis verifie les appels stubbes de demande et de verification.

Les cas d'expiration, verrouillage, challenge introuvable, generation du code et envoi email reel restent hors Cypress et sont couverts par les tests backend.

### Informations personnelles

Tests interessants :

- age inferieur a 16 ans bloque ;
- age superieur a 120 ans bloque ;
- date de naissance invalide bloque ;
- nationalite non suisse rend le titre de sejour obligatoire ;
- emails differents bloquent la continuation ;
- numero de document requis selon le type de document ;
- champs conditionnels Tiers : type de representation, identite, adresse, contact, email ;
- champs conditionnels Entreprise : poste, raison sociale, adresse, contact, email ;
- changement du type de personne masque et remet a zero les champs devenus non pertinents.

### Evenement commun

Tests interessants :

- type d'incident obligatoire ;
- date et heure de debut obligatoires ;
- date et heure de fin obligatoires ;
- format date invalide ;
- format heure invalide ;
- fin avant debut bloquante ;
- rattachement a la Suisse obligatoire : pays de l'evenement suisse ou nationalite suisse ;
- plainte deja deposee : details obligatoires ;
- adresse connue : adresse, NPA et localite obligatoires ;
- adresse connue : numero postal limite aux lettres, chiffres et espaces ;
- adresse de l'evenement correspondant a l'adresse du lese remplit les champs attendus ;
- trajet : apparition des adresses depart et destination.

### Vol

Tests interessants :

- choix Vol dans vehicule obligatoire ;
- degradation Oui/Non obligatoire ;
- categorie d'objet obligatoire ;
- type d'objet obligatoire sauf plaque ou objet deja enregistre ;
- plaque volee : pays et numero de plaque obligatoires ;
- telephone mobile : IMEI obligatoire sauf option inconnu ;
- IMEI au format exactement 15 chiffres ;
- objet hors vehicule : numero de serie obligatoire sauf option inconnu ;
- vehicule : type, fabricant, modele obligatoires ;
- fabricant Autre : precision obligatoire ;
- modele Autre : precision obligatoire ;
- ajout, modification et suppression d'un objet vole ;
- apres ajout d'un objet, le formulaire global peut passer sans revalider l'objet en cours de saisie.

### Dommages

Tests interessants :

- type de dommage obligatoire ;
- devise obligatoire ;
- nature du dommage obligatoire ;
- description obligatoire ;
- constat police obligatoire ;
- si constat present Oui : date de constat et fichier obligatoires ;
- si constat present Non : avertissement bloquant ou message attendu ;
- dommage vehicule : ajout, modification, suppression d'un vehicule endommage ;
- changement de type de dommage remet a zero les donnees vehicule non pertinentes.

### Cybercrime

Tests interessants :

- type de cybercrime obligatoire ;
- commande frauduleuse : prestataire, date de decouverte, montant, assurance, email ou email inconnu, telephone ou telephone inconnu, livraison a l'adresse du lese ;
- achat non recu : dates de contact, montant, description, article, vendeur, plateforme ou entreprise, documents ou raison d'absence, moyen de paiement, preuve de paiement, pieces d'identite ;
- fausse annonce : URL, titre, bailleur, email ou email inconnu, telephone ou telephone inconnu, adresse du bien, montant et mode de paiement ;
- formats email invalides ;
- URL ou identifiant plateforme invalide ;
- changement de type cybercrime remet a zero les champs de l'ancien sous-parcours.

## Priorite 3 - Rendez-vous et recapitulatif

### Rendez-vous

Tests interessants :

- aucun service disponible : message ou etat attendu ;
- filtrage des services selon `vol`, `dommage`, `cybercrime` ;
- filtre par poste ;
- filtre par date souhaitee ;
- date souhaitee hors fenetre de disponibilite ;
- pagination des creneaux ;
- selection d'un creneau puis changement de filtre : selection remise a zero ;
- vol de vehicule avec plaque : rendez-vous dans les 24 heures si disponible ;
- vol de vehicule avec plaque sans creneau compatible : passage autorise sans creneau selon la logique actuelle.

### Recapitulatif

Tests interessants :

- les sections conditionnelles Tiers et Entreprise apparaissent uniquement quand concernees ;
- les sections Vol, Dommage ou Cybercrime affichent uniquement les champs du type d'incident choisi ;
- les objets multiples sont affiches dans le recapitulatif ;
- les boutons de modification renvoient vers la bonne etape ou section ;
- erreur de conflit rendez-vous : message affiche et lien de retour au rendez-vous ;

## Priorite 4 - Persistance et navigation

Tests interessants :

- les donnees sont conservees entre deux etapes ;
- le bouton Precedent conserve les valeurs saisies ;
- rechargement de la page restaure l'etape et les donnees via localStorage ;
- sauvegarde et reprise de brouillon via `demandeId` dans l'URL ;
- apres soumission reussie, les donnees persistantes sont nettoyees ;
- changement de langue ne casse pas les validations deja affichees.

## Strategie de donnees et de stubs

### Donnees de reference

Prevoir des factories Cypress pour :

- declarant suisse valide ;
- tiers valide ;
- organisation valide ;
- evenement vol simple valide ;
- evenement vol vehicule valide ;
- evenement dommage valide ;
- cybercrime commande frauduleuse valide ;
- cybercrime achat non recu valide ;
- cybercrime fausse annonce valide ;
- creneau eSirius futur valide.

### Stubs API a centraliser

Centraliser les stubs dans des helpers Cypress :

- RIPOL : nationalites, sexes, types de documents, types de lieux, types d'objets, types de vehicules, marques, modeles, couleurs, cantons ;
- eSirius : services, disponibilites, creation de rendez-vous OK, conflit de rendez-vous ;
- pre-plainte : soumission OK, erreur fonctionnelle, erreur technique ;
- reprise de brouillon.

### Selecteurs

Les tests actuels s'appuient surtout sur les libelles. C'est lisible mais fragile.

Action recommandee :

- ajouter progressivement des attributs `data-cy` sur les composants et actions critiques ;
- conserver les tests par libelles pour les assertions de contenu visible ;
- utiliser `data-cy` pour les interactions structurelles comme boutons, selections, objets enregistres et lignes de creneaux.

Selecteurs prioritaires a ajouter :

- boutons de navigation par etape ;
- champs email et OTP ;
- choix du type d'incident ;
- boutons de validation d'objet vole et d'objet degrade ;
- lignes de creneaux ;
- bouton de soumission finale ;
- messages d'erreur fonctionnels importants.

## Plan d'action

### Etape 1 - Stabiliser l'outillage de test - couverte

- Centraliser les helpers de saisie et selection Vuetify.
- Centraliser les stubs RIPOL, email challenge avec demande et verification, eSirius et soumission.
- Ajouter les `data-cy` indispensables sur les elements instables.
- Ajouter une commande Cypress pour demarrer directement a une etape avec `pp-step` et `pp-data`.
- Ajouter un nettoyage systematique du localStorage avant chaque scenario.

### Etape 2 - Reprendre les premieres etapes du parcours - couverte

- Reprendre `AccesFormulaire.feature` pour rendre explicite l'etape informations generales.
- Ne pas creer de scenario dedie au challenge email.
- Reprendre `InformationsPersonnelles.feature` sans le supprimer brutalement.
- Demarrer les scenarios d'informations personnelles avec un etat `pp-step = 3` explicite.

L'adresse email et le code de verification ne font plus partie des informations personnelles. Ils sont saisis a l'etape Verification email puis conserves dans `pp-data` pour les scenarios qui demarrent directement a l'etape 3.

### Etape 3 - Couvrir le parcours nominal complet - couverte

- Ecrire un scenario complet : informations generales -> email -> informations personnelles -> vol simple -> rendez-vous -> recapitulatif -> validation.
- Garder ce scenario unique et representatif.
- Stubber les appels externes pour qu'il soit rapide et deterministe.

Etat actuel :

- le scenario nominal traverse reellement les informations generales, la verification email, les informations personnelles, la saisie d'un vol simple, le rendez-vous, le recapitulatif et la validation ;
- la demande et les deux verifications du challenge email sont stubbees : une verification a l'etape email, puis une nouvelle verification avant la soumission ;
- le vol simple est saisi via l'IHM avec un objet informatique et un numero de serie ;
- les appels eSirius, la soumission de la pre-plainte et la creation du rendez-vous sont verifies.

### Etape 4 - Couvrir les validations bloquantes

- Ajouter des tests courts par etape, demarrant directement avec un localStorage prepare.
- Prioriser les validations qui empechent la continuation.
- Eviter les tests redondants avec les tests unitaires Zod lorsqu'une validation est deja couverte plus bas niveau.

### Etape 5 - Couvrir les variantes metier les plus exposees

- Vol de vehicule avec fabricant/modele Autre.
- Plaque volee.
- Dommage avec constat present.
- Cybercrime achat non recu, car c'est le sous-parcours le plus riche.
- Rendez-vous filtre par type d'incident.

### Etape 6 - Couvrir les erreurs d'integration

- Soumission pre-plainte en erreur.
- Creneau eSirius devenu indisponible au recapitulatif.
- Reprise de brouillon par `demandeId`.

## Proposition de decoupage des fichiers `.feature`

- `AccesFormulaire.feature` : acces et informations generales.
- `InformationsPersonnelles.feature` : fichier existant a reprendre progressivement, avec validations et variantes declarant, tiers, entreprise.
- `EvenementVol.feature` : vol simple, vehicule, plaque, IMEI, objets multiples.
- `EvenementDommage.feature` : dommage simple, constat, vehicule endommage.
- `EvenementCybercrime.feature` : commande frauduleuse, achat non recu, fausse annonce.
- `RendezVous.feature` : disponibilites, filtres, selection, cas sans creneau.
- `RecapitulatifSoumission.feature` : recapitulatif, soumission, erreurs finales.

## Ordre recommande de realisation

1. Helpers communs : nettoyage localStorage, demarrage par etape, stubs RIPOL de base.
2. `AccesFormulaire.feature` : informations generales.
3. `InformationsPersonnelles.feature` : reprise propre du scenario nominal pour soi-meme.
4. `InformationsPersonnelles.feature` : validations bloquantes principales.
5. `InformationsPersonnelles.feature` : variantes Tiers et Entreprise.
6. Parcours nominal complet avec vol simple.
7. Rendez-vous nominal et absence de selection.
8. Vol vehicule et plaque.
9. Dommage avec constat present.
10. Cybercrime achat non recu.
11. Recapitulatif avec conflit rendez-vous.
12. Persistance localStorage et reprise brouillon.

## Criteres de qualite

- Chaque test doit avoir une intention metier claire.
- Les tests longs doivent etre rares.
- Les tests de validation doivent demarrer au plus pres de l'etape concernee.
- Les donnees externes doivent etre stubbees sauf si le test vise explicitement l'integration.
- Les assertions doivent verifier soit un changement d'etape, soit un message, soit un appel API, soit une donnee visible au recapitulatif.
- Les scenarios ne doivent pas dependre de l'ordre d'execution.
- Les donnees localStorage doivent etre nettoyees avant chaque scenario.
