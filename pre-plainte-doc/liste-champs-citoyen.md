# Liste des champs demandes au citoyen

Ce document recense les informations demandees au citoyen dans le formulaire de pre-plainte.

La colonne "Obligatoire" indique :

- "Oui" : le champ doit etre rempli pour continuer.
- "Non" : le champ peut etre laisse vide.
- "Selon le cas" : le champ devient obligatoire uniquement dans certaines situations.

## Informations generales

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Confirmation de l'identite du declarant | Oui | Le citoyen confirme qu'il agit avec sa propre identite. |
| Confirmation que la situation correspond aux conditions de pre-plainte | Oui | Le citoyen confirme que sa situation est compatible avec la demarche en ligne. |
| Verification anti-robot | Selon le cas | Obligatoire lorsque le captcha est active. |

## Lien avec la personne ou l'entite concernee

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Lien avec la personne concernee | Oui | Le citoyen indique s'il declare pour lui-meme, pour un tiers ou pour son entreprise. |
| Type de representation | Selon le cas | Obligatoire si la declaration est faite pour un tiers. |
| Fonction dans l'entreprise | Selon le cas | Obligatoire si la declaration est faite pour une entreprise. |
| Justificatif de representation de l'entreprise | Non | Piece jointe possible lorsque la declaration concerne une entreprise. |

## Coordonnees et identite du declarant

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Numero de telephone | Oui | Numero de contact du declarant. |
| Nom | Oui | Nom actuel du declarant. |
| Nom de naissance | Non | A renseigner si utile. |
| Prenom | Oui | Prenom du declarant. |
| Genre | Oui | Selection dans une liste. |
| Nationalite | Oui | Selection dans une liste. |
| Lieu d'origine | Non | Affiche uniquement pour une nationalite suisse. |
| Titre de sejour | Selon le cas | Obligatoire si la nationalite indiquee n'est pas suisse. |
| Date de naissance | Oui | Le declarant doit avoir un age compatible avec le formulaire. |

## Adresse du declarant

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Pays | Oui | Suisse par defaut, modifiable via la recherche d'adresse. |
| Adresse | Oui | Rue ou adresse complete. |
| Numero postal / numero de rue | Non | Peut etre complete automatiquement par la recherche d'adresse. |
| NPA | Oui | Code postal. |
| Localite | Oui | Commune ou ville. |

## Document d'identite du declarant

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Type de document d'identite | Oui | Carte d'identite, passeport ou documents voles/perdus. |
| Numero du document d'identite | Selon le cas | Obligatoire pour une carte d'identite ou un passeport. Non demande si le citoyen indique que ses documents sont voles ou perdus. |

## Verification de l'adresse e-mail

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Adresse e-mail | Oui | Utilisee pour envoyer un code de securite. |
| Code de securite recu par e-mail | Oui | Necessaire pour continuer apres l'envoi du code. |

## Informations sur le tiers concerne

Ces champs sont demandes uniquement si le citoyen declare pour une autre personne.

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Nom du tiers | Oui | Personne concernee par la pre-plainte. |
| Prenom du tiers | Oui | Personne concernee par la pre-plainte. |
| Genre du tiers | Oui | Selection dans une liste. |
| Nationalite du tiers | Oui | Selection dans une liste. |
| Date de naissance du tiers | Oui | Date de naissance de la personne concernee. |
| Pays du tiers | Oui | Suisse par defaut, modifiable via la recherche d'adresse. |
| Adresse du tiers | Oui | Adresse de la personne concernee. |
| Numero postal / numero de rue du tiers | Non | Peut etre complete automatiquement par la recherche d'adresse. |
| NPA du tiers | Oui | Code postal. |
| Localite du tiers | Oui | Commune ou ville. |
| Type de document d'identite du tiers | Oui | Carte d'identite, passeport ou documents voles/perdus. |
| Numero du document d'identite du tiers | Selon le cas | Obligatoire pour une carte d'identite ou un passeport. Non demande si les documents sont voles ou perdus. |
| Numero de telephone du tiers | Oui | Numero de contact du tiers. |
| Adresse e-mail du tiers | Oui | Adresse de contact du tiers. |
| Confirmation de l'adresse e-mail du tiers | Oui | Doit correspondre a l'adresse e-mail saisie. |

## Informations sur l'entreprise concernee

Ces champs sont demandes uniquement si le citoyen declare pour une entreprise.

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Nom de l'entreprise | Oui | Nom de l'organisation concernee. |
| Pays de l'entreprise | Oui | Suisse par defaut, modifiable via la recherche d'adresse. |
| Adresse de l'entreprise | Oui | Adresse de l'organisation. |
| Numero postal / numero de rue de l'entreprise | Non | Peut etre complete automatiquement par la recherche d'adresse. |
| NPA de l'entreprise | Oui | Code postal. |
| Localite de l'entreprise | Oui | Commune ou ville. |
| Numero de telephone de l'entreprise | Oui | Numero de contact. |
| Adresse e-mail de l'entreprise | Oui | Adresse de contact. |
| Confirmation de l'adresse e-mail de l'entreprise | Oui | Doit correspondre a l'adresse e-mail saisie. |

## Type d'evenement

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Type d'evenement | Oui | Choix entre vol, dommages materiels ou cybercriminalite. |

## Dates et heures de l'evenement

Ces champs sont demandes pour un vol, des dommages materiels et une commande frauduleuse.

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Date de debut de l'evenement | Oui | Date au format jour, mois, annee. |
| Heure de debut de l'evenement | Oui | Heure estimee ou connue. |
| Date de fin de l'evenement | Oui | Date au format jour, mois, annee. |
| Heure de fin de l'evenement | Oui | Elle doit etre coherente avec le debut de l'evenement. |

Pour un achat non recu ou une fausse annonce, le formulaire demande plutot les dates de contact.

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Date du premier contact | Oui | Date du premier echange. |
| Heure du premier contact | Oui | Heure du premier echange. |
| Date du dernier contact | Oui | Date du dernier echange. |
| Heure du dernier contact | Oui | Elle doit etre coherente avec le premier contact. |

## Adresse de l'evenement

Ces champs sont demandes pour un vol ou des dommages materiels.

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| L'adresse correspond-elle a l'adresse de la personne lesee ? | Oui | Permet de reprendre l'adresse deja saisie. |
| Type de lieu | Selon le cas | Demande si l'adresse de l'evenement est differente de l'adresse de la personne lesee. |
| L'adresse est-elle connue ? | Selon le cas | Demande si l'adresse est differente de l'adresse de la personne lesee. |
| S'agit-il d'un trajet ? | Selon le cas | Demande si l'adresse exacte n'est pas connue. |
| Pays de l'evenement | Selon le cas | Demande si une adresse d'evenement doit etre saisie. |
| Adresse de l'evenement | Selon le cas | Obligatoire si l'adresse est connue et differente de l'adresse de la personne lesee. |
| Numero postal / numero de rue de l'evenement | Non | Peut etre complete automatiquement par la recherche d'adresse. |
| NPA de l'evenement | Selon le cas | Obligatoire si l'adresse est connue et differente de l'adresse de la personne lesee. |
| Localite de l'evenement | Selon le cas | Obligatoire si l'adresse est connue et differente de l'adresse de la personne lesee. |
| Lieu d'origine | Non | Affiche pour une adresse suisse. |
| Adresse de destination | Selon le cas | Demande si l'evenement est declare comme un trajet. |
| NPA et localite de destination | Selon le cas | Demandes si une adresse de destination est saisie. |

## Pieces jointes communes

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Pieces jointes liees au vol ou aux dommages | Non | Le citoyen peut ajouter des documents utiles. |
| Autres documents pour une cybercriminalite | Non | Le citoyen peut ajouter des documents complementaires. |

## Vol

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Le vol a-t-il eu lieu dans un vehicule ? | Oui | Question oui/non. |
| Objet vole | Oui | Au moins un objet vole doit etre renseigne. |
| Y a-t-il eu des degradations ? | Oui | Question oui/non. |

### Detail d'un objet vole

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Categorie de l'objet | Oui | Exemple : vehicule, telephone, informatique, bijoux, plaque. |
| Sous-categorie | Selon le cas | Demande pour certaines categories. |
| Type d'objet | Selon le cas | Obligatoire sauf pour une plaque d'immatriculation. |
| Marque / fabricant | Selon le cas | Obligatoire pour un vehicule. Demande aussi lorsque la liste de marques est disponible pour certains objets. |
| Marque / fabricant autre | Selon le cas | Obligatoire si "Autre" est selectionne. |
| Modele | Selon le cas | Obligatoire pour un vehicule. Demande aussi lorsque la liste de modeles est disponible. |
| Modele autre | Selon le cas | Obligatoire si "Autre" est selectionne. |
| Couleur principale | Non | Selection dans une liste. |
| Couleur secondaire | Non | Selection dans une liste. |
| Gravure | Non | Demande pour les bijoux. |
| Valeur reelle | Non | Montant estime de l'objet. |
| Numero de serie | Selon le cas | Obligatoire pour telephone, informatique et photo/video, sauf si le citoyen coche qu'il ne le connait pas. |
| Numero IMEI | Selon le cas | Obligatoire pour un telephone mobile, sauf si le citoyen coche qu'il ne le connait pas. |
| Justification de l'absence d'IMEI | Non | Demande si le citoyen indique ne pas connaitre l'IMEI. |
| Numero de cadre | Non | Demande pour un velo. |
| VIN / numero de chassis | Non | Demande pour certains vehicules. |
| Identifiant Velofinder | Non | Demande pour un velo. |
| Date d'achat | Non | Date approximative ou connue. |
| Pays de la plaque | Selon le cas | Obligatoire pour une plaque volee, ou selon le type de vehicule. |
| Canton de la plaque | Selon le cas | Demande pour une plaque suisse. |
| Numero de plaque | Selon le cas | Obligatoire pour une plaque volee et pour certains vehicules, sauf si le formulaire permet d'indiquer que la plaque est inconnue. |
| Assureur du vehicule | Non | Demande pour un vehicule si le citoyen n'indique pas "aucune assurance". |
| Numero d'assurance | Non | Demande pour un vehicule assure. |
| Numero de vignette | Non | Demande pour un vehicule assure. |
| Numero master | Non | Demande pour un vehicule assure. |

## Dommages materiels

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Type de dommage | Oui | Exemple : vehicule, propriete, autre. |
| Constat de police deja etabli | Oui | Question oui/non. |
| Date du constat | Selon le cas | Obligatoire si un constat existe. |
| Fichier du constat de police | Selon le cas | Obligatoire si un constat existe. |
| Montant estime | Non | Montant approximatif des dommages. |
| Devise | Oui | Devise du montant. |
| Nature du dommage | Oui | Une ou plusieurs natures a selectionner. |
| Description du dommage | Oui | Description libre des faits ou degradations. |

### Vehicule endommage

Ces champs sont demandes si le dommage concerne un vehicule.

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Sous-categorie du vehicule | Oui | Type de vehicule concerne. |
| Type d'objet / vehicule | Oui | Selection dans une liste. |
| Marque / fabricant | Oui | Selection dans une liste ou saisie "Autre". |
| Marque / fabricant autre | Selon le cas | Obligatoire si "Autre" est selectionne. |
| Modele | Oui | Selection dans une liste ou saisie "Autre". |
| Modele autre | Selon le cas | Obligatoire si "Autre" est selectionne. |
| Couleur principale | Non | Selection dans une liste. |
| Couleur secondaire | Non | Selection dans une liste. |
| Valeur reelle | Non | Valeur estimee du vehicule. |
| Numero de cadre | Non | Demande pour un velo. |
| VIN / numero de chassis | Non | Demande pour certains vehicules. |
| Identifiant Velofinder | Non | Demande pour un velo. |
| Date d'achat | Non | Date approximative ou connue. |
| Pays de la plaque | Selon le cas | Demande si le vehicule possede une plaque. |
| Canton de la plaque | Selon le cas | Demande si la plaque est suisse. |
| Numero de plaque | Selon le cas | Obligatoire pour certains types de vehicules, sauf si le formulaire permet d'indiquer que la plaque est inconnue. |
| Assureur | Non | Demande si le citoyen n'indique pas "aucune assurance". |
| Numero d'assurance | Non | Demande si le vehicule est assure. |
| Numero de vignette | Non | Demande si le vehicule est assure. |
| Numero master | Non | Demande si le vehicule est assure. |

## Cybercriminalite

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Type de cybercriminalite | Oui | Choix entre commande frauduleuse, achat non recu ou fausse annonce. |
| Description | Selon le cas | Obligatoire pour les cas de cybercriminalite qui affichent un champ de description. |
| Justificatifs de paiement | Non | Pieces jointes possibles. |
| Copies d'ecran | Non | Pieces jointes possibles. |
| Autres documents | Non | Pieces jointes possibles. |

### Commande frauduleuse

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Prestataire | Oui | Nom du prestataire ou du service concerne. |
| Date de decouverte | Oui | Date a laquelle le citoyen a decouvert les faits. |
| Montant du delit | Oui | Montant concerne. |
| Assurance disponible | Oui | Question oui/non. |
| Adresse e-mail utilisee pour la commande | Selon le cas | Obligatoire sauf si le citoyen indique qu'elle est inconnue. |
| Numero de telephone utilise pour la commande | Selon le cas | Obligatoire sauf si le citoyen indique qu'il est inconnu. |
| L'adresse de livraison est-elle celle de la personne lesee ? | Oui | Question oui/non. |
| Adresse de livraison | Non | Demande si la livraison n'etait pas a l'adresse de la personne lesee. |
| Numero postal / numero de rue de livraison | Non | Demande avec l'adresse de livraison. |
| NPA de livraison | Non | Demande avec l'adresse de livraison. |
| Localite de livraison | Non | Demande avec l'adresse de livraison. |
| Pays de livraison | Non | Demande avec l'adresse de livraison. |

### Achat non recu

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Montant du delit | Oui | Montant de l'achat. |
| Description des faits | Oui | Description libre. |
| Description de l'article non livre | Oui | Article ou service attendu. |
| Prenom du vendeur | Oui | Prenom connu du vendeur. |
| Nom du vendeur | Oui | Nom connu du vendeur. |
| Numero de telephone du vendeur | Selon le cas | Obligatoire sauf si le citoyen indique qu'il est inconnu. |
| Adresse e-mail du vendeur | Selon le cas | Obligatoire sauf si le citoyen indique qu'elle est inconnue. |
| Adresse du vendeur | Non | Demande si elle est connue. |
| Achat via une place de marche | Oui | Question oui/non. |
| Plateforme utilisee | Selon le cas | Obligatoire si l'achat a ete fait via une place de marche. |
| Plateforme utilisee autre | Selon le cas | Obligatoire si "Autre" est selectionne. |
| Lien ou identifiant de l'annonce | Selon le cas | Obligatoire si l'achat a ete fait via une place de marche. |
| Nom de l'entreprise vendeuse | Selon le cas | Obligatoire si l'achat n'a pas ete fait via une place de marche. |
| Site web de l'entreprise vendeuse | Selon le cas | Obligatoire si l'achat n'a pas ete fait via une place de marche. |
| Annonce | Selon le cas | Obligatoire sauf si le citoyen indique que le document n'est pas disponible. |
| Raison de l'absence d'annonce | Selon le cas | Obligatoire si l'annonce n'est pas disponible. |
| Moyen de paiement | Oui | Selection dans une liste. |
| Moyen de paiement autre | Selon le cas | Obligatoire si "Autre" est selectionne. |
| IBAN du beneficiaire | Selon le cas | Obligatoire si le paiement a ete fait par compte bancaire. |
| Compte PayPal du beneficiaire | Selon le cas | Obligatoire si le paiement a ete fait par PayPal. |
| Numero Twint du beneficiaire | Selon le cas | Obligatoire si le paiement a ete fait par Twint. |
| Adresse du wallet crypto | Selon le cas | Obligatoire si le paiement a ete fait en cryptomonnaie. |
| Hash de transaction crypto | Selon le cas | Obligatoire si le paiement a ete fait en cryptomonnaie. |
| Societe beneficiaire | Non | A renseigner si connue. |
| Nom du beneficiaire | Non | A renseigner si connu. |
| Prenom du beneficiaire | Non | A renseigner si connu. |
| Date de l'operation | Oui | Date du paiement. |
| Preuve de paiement | Selon le cas | Obligatoire sauf si le citoyen indique qu'elle n'est pas disponible. |
| Raison de l'absence de preuve de paiement | Selon le cas | Obligatoire si la preuve n'est pas disponible. |
| Copie de l'identite transmise a l'auteur | Oui | Question oui/non. |
| Document transmis a l'auteur | Selon le cas | Obligatoire si une copie d'identite a ete transmise. |
| Copie de l'identite de l'auteur transmise | Oui | Question oui/non. |
| Document d'identite de l'auteur | Selon le cas | Obligatoire si une copie d'identite de l'auteur a ete transmise. |

### Fausse annonce

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| URL complete de l'annonce | Oui | Lien vers l'annonce. |
| Titre de l'annonce | Oui | Titre affiche dans l'annonce. |
| Nom du bailleur | Oui | Nom indique par l'auteur de l'annonce. |
| Adresse e-mail du bailleur | Selon le cas | Obligatoire sauf si le citoyen indique qu'elle est inconnue. |
| Numero de telephone du bailleur | Selon le cas | Obligatoire sauf si le citoyen indique qu'il est inconnu. |
| Adresse du bien immobilier | Oui | Adresse du logement ou du bien concerne. |
| Montant demande | Oui | Montant reclame. |
| Mode de paiement demande | Oui | Moyen de paiement demande par l'auteur. |

## Rendez-vous

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Poste de police | Non | Le citoyen peut filtrer les disponibilites par poste. |
| Date souhaitee | Non | Le citoyen peut filtrer les disponibilites par date. |
| Creneau de rendez-vous | Selon le cas | Obligatoire sauf dans le cas particulier d'un vol de vehicule avec plaque, ou le formulaire peut continuer sans rendez-vous si aucun creneau compatible n'est disponible. |

## Recapitulatif et soumission

| Champ demande | Obligatoire | Precision |
| --- | --- | --- |
| Verification anti-robot avant soumission | Selon le cas | Obligatoire lorsque le captcha est active. |
| Nouveau code de securite e-mail | Selon le cas | Redemande uniquement si le code precedent est invalide, expire ou introuvable au moment de la soumission. |
