# Couverture Cypress des parcours frontend

Ce document est généré depuis les scénarios Cypress écrits en Gherkin. Il synthétise les parcours frontend réellement exécutés de bout en bout.

Scénarios Cypress documentés : 24.

## Accès au formulaire

| Parcours testé | Couverture fonctionnelle testée | Exemples testés | Source |
| --- | --- | --- | --- |
| Démarrer une pré-plainte | Conditions d'accès au formulaire et passage vers la vérification de l'adresse e-mail. | La confirmation d'identité est obligatoire pour démarrer une pré-plainte.<br><br>La confirmation de situation est obligatoire pour démarrer une pré-plainte.<br><br>Le type d'incident est obligatoire pour démarrer une pré-plainte.<br><br>Des informations générales valides permettent d'accéder à la vérification de l'adresse e-mail. | `AccesFormulaire.feature` |

## Informations sur l'événement - cybercrime

| Parcours testé | Couverture fonctionnelle testée | Exemples testés | Source |
| --- | --- | --- | --- |
| Achat non reçu - le paiement par IBAN demande le compte bénéficiaire | Champs et validations spécifiques à un achat non reçu. | Les champs spécifiques à un achat non reçu sont affichés pour ce type de cybercriminalité.<br><br>L'IBAN ou le compte du bénéficiaire est obligatoire pour un paiement par IBAN. | `EvenementCybercrime.feature` |

## Informations sur l'événement - dommage

| Parcours testé | Couverture fonctionnelle testée | Exemples testés | Source |
| --- | --- | --- | --- |
| Dommage avec constat présent - le fichier du constat est obligatoire | Champs et validations spécifiques à un dommage avec constat présent. | Les informations du constat de police sont affichées lorsqu'un dommage avec constat présent est déclaré.<br><br>Le fichier du constat de police est obligatoire lorsqu'un constat est présent. | `EvenementDommage.feature` |

## Informations sur l'événement - vol

| Parcours testé | Couverture fonctionnelle testée | Exemples testés | Source |
| --- | --- | --- | --- |
| BVA - date de début de l'événement absente<br><br>BVA - fin de l'événement avant le début<br><br>BVA - dégradations non renseignées<br><br>BVA - catégorie d'objet volé absente<br><br>Plaque volée - les champs de plaque sont affichés | Validations bloquantes et affichages conditionnels liés à un vol. | La date de début de l'événement est obligatoire pour déclarer un vol.<br><br>La fin de l'événement doit être postérieure au début.<br><br>L'information sur les dégradations est obligatoire pour un objet volé.<br><br>La catégorie de l'objet volé est obligatoire.<br><br>Les champs de plaque sont affichés lorsqu'une plaque volée est déclarée. | `EvenementVol.feature` |

## Informations personnelles

| Parcours testé | Couverture fonctionnelle testée | Exemples testés | Source |
| --- | --- | --- | --- |
| Affichage conditionnel des champs Tiers<br><br>Affichage conditionnel des champs Entreprise<br><br>Informations personnelles nominales pour soi-même<br><br>BVA - date de naissance inférieure à 16 ans<br><br>BVA - nationalité non suisse sans titre de séjour<br><br>BVA - numéro de document manquant<br><br>BVA - numéro de téléphone invalide | Affichages conditionnels et validations bloquantes de l'étape informations personnelles. | Selon le type de représentation (tiers, entreprise), on affiche le formulaire correspondant.<br><br>Des informations personnelles valides permettent de passer à l'étape suivante.<br><br>La date de naissance doit correspondre à un âge compris entre 16 et 120 ans.<br><br>Un titre de séjour est obligatoire pour une nationalité non suisse.<br><br>Le numéro du document d'identité est obligatoire.<br><br>Le format du numéro de téléphone est vérifié. | `InformationsPersonnelles.feature` |

## Parcours nominal complet

| Parcours testé | Couverture fonctionnelle testée | Exemples testés | Source |
| --- | --- | --- | --- |
| Soumettre une pré-plainte nominale pour un vol simple<br><br>Soumission pré-plainte en erreur serveur<br><br>Créneau eSirius devenu indisponible au récapitulatif | Soumission de la pré-plainte et gestion des erreurs d'intégration au récapitulatif. | Une pré-plainte nominale avec rendez-vous peut être soumise jusqu'à la validation finale.<br><br>Une erreur serveur lors de la soumission est affichée au citoyen sans quitter le récapitulatif.<br><br>Un créneau devenu indisponible au moment de la soumission est signalé au citoyen.<br><br>Le citoyen peut revenir à l'étape rendez-vous lorsqu'un créneau est devenu indisponible. | `RecapitulatifSoumission.feature` |

## Rendez-vous

| Parcours testé | Couverture fonctionnelle testée | Exemples testés | Source |
| --- | --- | --- | --- |
| BVA - aucun créneau sélectionné<br><br>Filtrer les services par type d'incident cybercrime | Sélection obligatoire d'un créneau et filtrage des services de rendez-vous. | Un créneau horaire doit être sélectionné pour continuer après l'étape rendez-vous.<br><br>Les services de rendez-vous proposés dépendent du type d'incident déclaré. | `RendezVous.feature` |

## Reprise de brouillon

| Parcours testé | Couverture fonctionnelle testée | Exemples testés | Source |
| --- | --- | --- | --- |
| Reprendre une pré-plainte avec un demandeId dans l'URL | Restauration du parcours citoyen depuis un identifiant de brouillon. | Une pré-plainte sauvegardée est restaurée lorsqu'un demandeId valide est présent dans l'URL. | `RepriseBrouillon.feature` |

## Vol d'objet - véhicule

| Parcours testé | Couverture fonctionnelle testée | Exemples testés | Source |
| --- | --- | --- | --- |
| BVA - aucun type de véhicule renseigné<br><br>BVA - fabricant "Autre" vide puis renseigné avec un caractère<br><br>BVA - modèle "Autre" vide puis renseigné avec un caractère | Validations propres à l'ajout d'un véhicule volé. | Le type de véhicule est obligatoire pour enregistrer un véhicule volé.<br><br>Le fabricant doit être précisé lorsque la valeur "Autre" est sélectionnée.<br><br>Le modèle doit être précisé lorsque la valeur "Autre" est sélectionnée. | `VolVehicule.feature` |

