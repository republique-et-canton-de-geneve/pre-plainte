# Regles metier du formulaire

Ce document est genere depuis les tests unitaires frontend.

## Coordonnees et identite du declarant

| Champ demande | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- |
| Date de naissance | Oui | Le declarant doit avoir entre 16 et 120 ans. | 15 ans est refuse<br>16 ans est accepte<br>119 ans est accepte<br>120 ans est accepte<br>121 ans est refuse |
| Titre de sejour | Selon le cas | Obligatoire si la nationalite indiquee n'est pas suisse. | nationalite suisse sans titre de sejour est acceptee<br>nationalite non suisse sans titre de sejour est refusee<br>nationalite non suisse avec titre de sejour est acceptee |
| Numero de telephone | Oui | Le numero de telephone doit respecter un format international valide. | numero vide est refuse<br>numero avec lettres est refuse<br>numero suisse au format international est accepte<br>numero international avec espaces est accepte |

## Document d'identite du declarant

| Champ demande | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- |
| Numero du document d'identite | Selon le cas | Obligatoire pour une carte d'identite ou un passeport. Non demande si les documents sont voles ou perdus. | carte d'identite sans numero est refusee<br>passeport sans numero est refuse<br>carte d'identite avec numero est acceptee<br>documents voles ou perdus sans numero est accepte |

## Informations sur le tiers concerne

| Champ demande | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- |
| Type de representation | Selon le cas | Obligatoire si le citoyen declare pour un tiers. | declaration pour soi-meme sans type de representation est acceptee<br>declaration pour un tiers sans type de representation est refusee<br>declaration pour un tiers avec type de representation est acceptee |

## Informations sur l'entreprise concernee

| Champ demande | Obligatoire | Precision | Exemples testes |
| --- | --- | --- | --- |
| Fonction dans l'entreprise | Selon le cas | Obligatoire si le citoyen declare pour une entreprise. | declaration pour soi-meme sans fonction dans l'entreprise est acceptee<br>declaration pour une entreprise sans fonction est refusee<br>declaration pour une entreprise avec fonction est acceptee |
