import { ripolSelection } from "../stubs/ripol";

export const donneesEmailVerifie = {
  email: "anne.martin@example.org",
  confirmationEmail: "123456",
};

export const declarantSuisseValide = {
  ...donneesEmailVerifie,
  lienAvecPersonne: "MOI_MEME",
  telephone: "+41791234567",
  nom: "Martin",
  prenom: "Anne",
  genre: ripolSelection("2", "Femme"),
  nationalite: ripolSelection("8100", "Suisse"),
  dateNaissance: "15.04.1985",
  pays: "8100",
  adresse: "Rue du Marche 10",
  adressePostale: "10",
  npa: "1201",
  localite: "Geneve",
  typeDocumentIdentite: "carte_identite",
  numeroDocumentIdentite: "ID1234567",
};
