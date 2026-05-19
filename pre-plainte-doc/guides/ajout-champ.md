# 🧩 Guide technique — Ajout d’un champ

## 🎯 Objectif
Documenter les étapes nécessaires pour ajouter un nouveau champ dans le flux de pré-plainte.

---

## 🖥️ Frontend (adapter entrant)

- Ajouter dans `pre-plainte.interface.ts`
- Ajouter dans `initial-form-data.ts`
- Ajouter dans le fichier de schema (validation)
- Ajouter dans le template du formulaire concerné

---

## 🧾 Gestion de l’affichage

- Ajouter dans `incident-fields.ts` (si le champ concerne un incident)
- Ajouter dans le composant récapitulatif (affichage du résumé)

---

## 🔄 Mapping Front ↔ Backend

- Ajouter dans `preplainte-payload.interface.ts`
- Ajouter dans le mapper Front → Backend
- Ajouter dans `backend-to-form.mapper.ts` (reprise du brouillon Backend → Front)

---

## 🧠 Backend

- Ajouter dans le DTO backend concerné
- Ajouter dans `PdfGenerationAdapter`

---

## 🌍 Intégration externe (ePolice / ECH0051)

- Ajouter dans `Ech0051DocumentPayload`
- Ajouter dans `Ech0051DocumentXml`
- Ajouter dans le mapper `SuisseEpolice`
- Ajouter dans `Ech051Builder`

---

## ⚠️ Points de vigilance

- Vérifier la cohérence du champ entre Front / Back
- Vérifier la reprise dans le brouillon
- Vérifier l’impact sur les exports (PDF / XML)
- Vérifier les validations côté frontend et backend
