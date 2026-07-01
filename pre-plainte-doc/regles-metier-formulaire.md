# Regles metier du formulaire

Ce document est genere depuis les tests unitaires frontend.

## Coordonnees et identite du declarant

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Date de naissance | Validation des champs | Oui | Le declarant doit avoir entre 16 et 120 ans. | 15 ans est refuse<br><br>16 ans est accepte<br><br>119 ans est accepte<br><br>120 ans est accepte<br><br>121 ans est refuse |
| Titre de sejour | Validation des champs | Selon le cas | Obligatoire si la nationalite indiquee n'est pas suisse. | nationalite suisse sans titre de sejour est acceptee<br><br>nationalite non suisse sans titre de sejour est refusee<br><br>nationalite non suisse avec titre de sejour est acceptee |
| Numero de telephone | Validation des champs | Oui | Le numero de telephone doit respecter un format international valide. | numero vide est refuse<br><br>numero avec lettres est refuse<br><br>numero suisse au format international est accepte<br><br>numero international avec espaces est accepte |

## Document d'identite du declarant

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Numero du document d'identite | Validation des champs | Selon le cas | Obligatoire pour une carte d'identite ou un passeport. Non demande si les documents sont voles ou perdus. | carte d'identite sans numero est refusee<br><br>passeport sans numero est refuse<br><br>carte d'identite avec numero est acceptee<br><br>documents voles ou perdus sans numero est accepte |

## Informations sur le tiers concerne

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Type de representation | Validation des champs | Selon le cas | Obligatoire si le citoyen declare pour un tiers. | declaration pour soi-meme sans type de representation est acceptee<br><br>declaration pour un tiers sans type de representation est refusee<br><br>declaration pour un tiers avec type de representation est acceptee |

## Informations sur l'entreprise concernee

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Fonction dans l'entreprise | Validation des champs | Selon le cas | Obligatoire si le citoyen declare pour une entreprise. | declaration pour soi-meme sans fonction dans l'entreprise est acceptee<br><br>declaration pour une entreprise sans fonction est refusee<br><br>declaration pour une entreprise avec fonction est acceptee |

## Informations sur l'evenement

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Type d'incident | Validation des champs | Oui | Le type d'incident doit etre selectionne avant de renseigner les details. | type d'incident absent est refuse<br><br>type vol est accepte avec les champs minimaux |
| Date et heure de l'evenement | Validation des champs | Oui | Les dates et heures de debut et de fin sont obligatoires et la fin doit etre posterieure au debut. | date de debut absente est refusee<br><br>heure de fin avant heure de debut est refusee<br><br>chronologie correcte est acceptee |
| Adresse de l'evenement | Validation des champs | Selon le cas | Obligatoire lorsque l'adresse de l'evenement est connue et differente de l'adresse de la personne lesee. | adresse connue trop courte est refusee<br><br>adresse de la personne lesee reutilisee est acceptee |

## Vol

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Informations de base de l'objet vole | Validation des champs | Oui | Un vol doit indiquer si l'objet etait dans un vehicule, la categorie de l'objet et l'existence de degradations. | categorie d'objet absente est refusee<br><br>information sur les degradations absente est refusee |
| Numero de serie | Validation des champs | Selon le cas | Obligatoire pour certains objets comme telephone, informatique ou photo/video sauf si le numero est inconnu. | telephone sans numero de serie est refuse<br><br>telephone avec numero de serie est accepte |
| Numero IMEI | Validation des champs | Selon le cas | Obligatoire et compose de 15 chiffres pour un telephone mobile sauf si le numero est inconnu. | telephone mobile sans IMEI est refuse<br><br>IMEI avec moins de 15 chiffres est refuse<br><br>IMEI de 15 chiffres est accepte |
| Plaque d'immatriculation | Validation des champs | Selon le cas | Le pays et le numero de plaque sont obligatoires lorsque la plaque est renseignee et non marquee comme inconnue. | plaque suisse sans canton est refusee pour un vehicule<br><br>plaque suisse complete est acceptee |
| Plaque de vehicule | Branchement | Selon le cas | Une plaque de vehicule suisse requiert un pays, un canton et un numero au format attendu. | plaque suisse complete est acceptee<br><br>plaque suisse sans canton est refusee<br><br>plaque suisse au mauvais format est refusee<br><br>plaque inconnue est acceptee pour une categorie sans plaque obligatoire |

## Dommages materiels

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Nature du dommage | Validation des champs | Oui | Un dommage materiel doit indiquer le type de dommage, au moins une nature, une description et le constat de police. | nature du dommage absente est refusee |
| Constat de police | Validation des champs | Oui | Le constat de police doit etre present et le fichier du constat doit etre fourni. | absence de constat est refusee<br><br>constat sans fichier est refuse |

## Cybercriminalite

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Type de cybercriminalite | Validation des champs | Oui | Le type de cybercriminalite est obligatoire lorsqu'un incident de cybercriminalite est declare. | type de cybercriminalite absent est refuse |

## Cybercriminalite - commande frauduleuse

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Coordonnees de commande | Validation des champs | Oui | Une commande frauduleuse doit renseigner le prestataire, la date de decouverte, le montant, l'assurance, les coordonnees de commande et l'adresse de livraison. | prestataire absent est refuse<br><br>email de commande invalide est refuse |

## Cybercriminalite - achat non recu

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Paiement | Validation des champs | Oui | Un achat non recu doit indiquer le moyen de paiement et les informations propres au moyen choisi. | moyen de paiement absent est refuse<br><br>paiement par IBAN sans beneficiaire est refuse |

## Cybercriminalite - fausse annonce

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Annonce | Validation des champs | Oui | Une fausse annonce doit contenir une URL valide, un titre, le bailleur, ses coordonnees, l'adresse du bien, le montant et le mode de paiement demande. | URL d'annonce absente est refusee<br><br>email du bailleur invalide est refuse |

## Informations generales

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Bouton continuer | Blocage | Oui | La continuation est autorisee uniquement si les confirmations et le type d'incident requis sont renseignes. | incident vol avec confirmations est autorise<br><br>confirmation d'identite absente bloque la continuation<br><br>dommage sans type de dommage bloque la continuation<br><br>cybercrime autre bloque la continuation<br><br>captcha active sans jeton bloque la continuation<br><br>captcha active avec jeton autorise la continuation |
| Changement du type d'incident | Blocage | Selon le cas | Les champs specifiques dommage ou cybercriminalite sont reinitialises lorsque le type d'incident ne les concerne plus. | passage vers vol reinitialise le type de dommage<br><br>passage vers vol reinitialise le type de cybercriminalite<br><br>incident dommage conserve le type de dommage |

## Verification email

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Adresse email | Validation des champs | Oui | L'adresse email est obligatoire, nettoyee des espaces et doit respecter le format email. | email vide est refuse<br><br>email au format invalide est refuse<br><br>email valide avec espaces est accepte |
| Bouton continuer | Blocage | Oui | La continuation est autorisee apres envoi du code et saisie d'un code de securite valide, sauf bypass de developpement. | code valide apres envoi autorise la continuation<br><br>code non envoye bloque la continuation<br><br>code incomplet bloque la continuation<br><br>erreur d'envoi bloque la continuation<br><br>bypass developpement avec email valide autorise la continuation |
| Code de securite | Blocage | Selon le cas | Le code de securite est reinitialise si l'adresse email change apres l'envoi du code. | email modifie apres envoi reinitialise le challenge<br><br>email identique apres envoi conserve le challenge |

## Rendez-vous

| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- | --- |
| Date souhaitee | Validation des champs | Non | La date souhaitee doit etre valide et comprise dans la periode proposee par le service. | date au format invalide est refusee<br><br>date avant la premiere disponibilite est refusee<br><br>date apres la derniere disponibilite est refusee<br><br>date dans la periode disponible est acceptee |
| Service propose | Blocage | Oui | Les services proposes dependent du type d'incident et doivent avoir au moins un creneau disponible. | incident vol conserve uniquement le service vol<br><br>incident dommage conserve uniquement le service dommage |
| Creneaux compatibles | Blocage | Oui | Les creneaux passes, hors fenetre de rendez-vous ou incompatibles avec l'incident sont exclus. | vol sans plaque conserve les deux creneaux vol de la fenetre<br><br>vol de vehicule avec plaque conserve uniquement les creneaux sous 24 heures |
| Alerte rendez-vous | Blocage | Selon le cas | Un vol de vehicule avec plaque affiche une alerte specifique selon la disponibilite des creneaux. | vol simple affiche l'information generale<br><br>vol de vehicule avec plaque affiche l'alerte d'urgence |
| Vol de vehicule avec plaque | Branchement | Selon le cas | Un vol est considere comme vol de vehicule avec plaque si au moins un objet vole effectif est un vehicule avec plaque connue. | vehicule avec plaque connue est detecte<br><br>vehicule avec plaque inconnue n'est pas detecte |
