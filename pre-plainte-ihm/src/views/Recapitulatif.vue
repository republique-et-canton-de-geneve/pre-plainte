<template>
  <main>
    <h1 class="mb-4 text-h1 text-md-h1 d-none d-md-block">{{ t("titreApplication.prePlainte") }}</h1>

    <section aria-labelledby="recap-title" class="mb-6">
      <h2 id="recap-title" class="pre-plainte-main-card-title text-h2">{{ t("steps.recapitulatif") }}</h2>
    </section>

    <v-card class="pa-2 pa-md-6 mb-4">
      <h2 id="recap-personal-info" class="pre-plainte-main-card-title mb-4 text-h2 text-md-h1 text-wrap">
        {{ t("steps.informationsPersonnelles") }}
      </h2>
      <div class="mb-4">
        <!-- Informations personnelles -->
        <v-row class="ma-0">
          <v-col cols="12" class="pa-0">
            <dl class="w-100">
              <v-row class="ma-0">
                <v-col cols="12" md="6">
                  <dt id="lbl-lienAvecPersonne">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.lienAvecPersonne") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-lienAvecPersonne">
                    {{ formatLienAvecPersonne(data.lienAvecPersonne) }}
                  </dd>
                </v-col>

                <v-col cols="12" md="6">
                  <dt id="lbl-nom">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.nom") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-nom">
                    {{ data.nom }}
                  </dd>
                </v-col>

                <v-col cols="12" md="6">
                  <dt id="lbl-prenom">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.prenom") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-prenom">
                    {{ data.prenom }}
                  </dd>
                </v-col>

                <v-col cols="12" md="6">
                  <dt id="lbl-genre">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.genre") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-genre">
                    {{ formatGenre(data.genre) }}
                  </dd>
                </v-col>

                <v-col cols="12" md="6">
                  <dt id="lbl-nationalite">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.nationalite") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-nationalite">
                    {{ formatRipol(data.nationalite) }}
                  </dd>
                </v-col>

                <v-col cols="12" md="6">
                  <dt id="lbl-dateNaissance">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.dateNaissance") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-dateNaissance">
                    {{ data.dateNaissance }}
                  </dd>
                </v-col>

                <v-col cols="12" md="6">
                  <dt id="lbl-pays">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.pays") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-pays">
                    {{ formatPays(data.pays) }}
                  </dd>
                </v-col>

                <v-col cols="12">
                  <dt id="lbl-adresse">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.adresse") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-adresse">
                    {{ data.adresse }}
                  </dd>
                </v-col>

                <v-col cols="12" md="6" v-if="data.adressePostale">
                  <dt id="lbl-adressePostale">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.adressePostale") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-adressePostale">
                    {{ data.adressePostale }}
                  </dd>
                </v-col>

                <v-col cols="12" md="3">
                  <dt id="lbl-npa">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.npa") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-npa">
                    {{ data.npa }}
                  </dd>
                </v-col>

                <v-col cols="12" md="3">
                  <dt id="lbl-localite">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.localite") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-localite">
                    {{ data.localite }}
                  </dd>
                </v-col>

                <v-col cols="12" md="6">
                  <dt id="lbl-telephone">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.numeroTelephone") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-telephone">
                    {{ data.telephone }}
                  </dd>
                </v-col>

                <v-col cols="12" md="6">
                  <dt id="lbl-email">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.email") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-email">
                    {{ data.email }}
                  </dd>
                </v-col>

                <v-col v-if="data.typeDocumentIdentite" cols="12" md="6">
                  <dt id="lbl-typeDocumentIdentite">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.typeDocumentIdentite") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-typeDocumentIdentite">
                    {{ formatTypeDocumentIdentite(data.typeDocumentIdentite) }}
                  </dd>
                </v-col>

                <v-col
                  v-if="data.typeDocumentIdentite !== 'aucun_document' && data.numeroDocumentIdentite"
                  cols="12"
                  md="6"
                >
                  <dt id="lbl-numeroDocumentIdentite">
                    <v-label class="ge-field-label">{{ t("informationsPersonnelles.numeroDocumentIdentite") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-numeroDocumentIdentite">
                    {{ data.numeroDocumentIdentite }}
                  </dd>
                </v-col>
              </v-row>
            </dl>
          </v-col>
        </v-row>

        <v-divider class="my-4" />

        <!-- Identité tiers concerné -->
        <section v-if="isTiers">
          <h3 class="pre-plainte-main-card-title text-h3 mb-3">
            {{ t("informationsPersonnelles.identiteTiersConcerne") }}
          </h3>

          <v-row class="ma-0">
            <v-col cols="12" class="pa-0">
              <dl class="w-100">
                <v-row class="ma-0">
                  <v-col cols="12" md="6" v-if="data.typeRepresentation">
                    <dt id="lbl-tiers-typeRepresentation">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.typeRepresentation") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-typeRepresentation">
                      {{ formatTypeRepresentation(data.typeRepresentation) }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.tiersNom">
                    <dt id="lbl-tiers-nom">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.nom") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-nom">
                      {{ data.tiersNom }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.tiersPrenom">
                    <dt id="lbl-tiers-prenom">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.prenom") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-prenom">
                      {{ data.tiersPrenom }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.tiersGenre">
                    <dt id="lbl-tiers-genre">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.genre") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-genre">
                      {{ formatGenre(data.tiersGenre) }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.tiersNationalite">
                    <dt id="lbl-tiers-nationalite">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.nationalite") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-nationalite">
                      {{ formatRipol(data.tiersNationalite) }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.tiersDateNaissance">
                    <dt id="lbl-tiers-dateNaissance">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.dateNaissance") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-dateNaissance">
                      {{ data.tiersDateNaissance }}
                    </dd>
                  </v-col>

                  <v-col cols="12" v-if="data.tiersAdresse">
                    <dt id="lbl-tiers-adresse">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.adresse") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-adresse">
                      {{ data.tiersAdresse }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.tiersAdressePostale">
                    <dt id="lbl-tiers-adressePostale">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.adressePostale") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-adressePostale">
                      {{ data.tiersAdressePostale }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="3" v-if="data.tiersNpa">
                    <dt id="lbl-tiers-npa">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.npa") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-npa">
                      {{ data.tiersNpa }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="3" v-if="data.tiersLocalite">
                    <dt id="lbl-tiers-localite">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.localite") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-localite">
                      {{ data.tiersLocalite }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.tiersTelephone">
                    <dt id="lbl-tiers-telephone">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.numeroTelephone") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-telephone">
                      {{ data.tiersTelephone }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.tiersEmail">
                    <dt id="lbl-tiers-email">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.email") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-email">
                      {{ data.tiersEmail }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.tiersPays">
                    <dt id="lbl-tiers-pays">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.pays") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-pays">
                      {{ formatPays(data.tiersPays) }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.tiersTypeDocumentIdentite">
                    <dt id="lbl-tiers-typeDocumentIdentite">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.typeDocumentIdentite") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-typeDocumentIdentite">
                      {{ formatTypeDocumentIdentite(data.tiersTypeDocumentIdentite) }}
                    </dd>
                  </v-col>

                  <v-col
                    cols="12"
                    md="6"
                    v-if="data.tiersTypeDocumentIdentite !== 'aucun_document' && data.tiersNumeroDocumentIdentite"
                  >
                    <dt id="lbl-tiers-numeroDocumentIdentite">
                      <v-label class="ge-field-label">{{
                        t("informationsPersonnelles.numeroDocumentIdentite")
                      }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-tiers-numeroDocumentIdentite">
                      {{ data.tiersNumeroDocumentIdentite }}
                    </dd>
                  </v-col>
                </v-row>
              </dl>
            </v-col>
          </v-row>
        </section>

        <!-- Informations organisation -->
        <section v-if="isEntreprise">
          <h3 class="pre-plainte-main-card-title text-h3 mb-3">
            {{ t("informationsPersonnelles.identiteOrganisation") }}
          </h3>

          <v-row class="ma-0">
            <v-col cols="12" class="pa-0">
              <dl class="w-100">
                <v-row class="ma-0">
                  <v-col cols="12" md="6" v-if="data.postePersonneMorale">
                    <dt id="lbl-organisation-poste">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.postePersonneMorale") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-organisation-poste">
                      {{ formatPostePersonneMorale(data.postePersonneMorale) }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.organisationNom">
                    <dt id="lbl-organisation-nom">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.nomOrganisation") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-organisation-nom">
                      {{ data.organisationNom }}
                    </dd>
                  </v-col>

                  <v-col cols="12" v-if="data.organisationAdresse">
                    <dt id="lbl-organisation-adresse">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.adresse") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-organisation-adresse">
                      {{ data.organisationAdresse }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.organisationAdressePostale">
                    <dt id="lbl-organisation-adressePostale">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.adressePostale") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-organisation-adressePostale">
                      {{ data.organisationAdressePostale }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="3" v-if="data.organisationNpa">
                    <dt id="lbl-organisation-npa">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.npa") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-organisation-npa">
                      {{ data.organisationNpa }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="3" v-if="data.organisationLocalite">
                    <dt id="lbl-organisation-localite">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.localite") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-organisation-localite">
                      {{ data.organisationLocalite }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.organisationTelephone">
                    <dt id="lbl-organisation-telephone">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.numeroTelephone") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-organisation-telephone">
                      {{ data.organisationTelephone }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.organisationEmail">
                    <dt id="lbl-organisation-email">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.email") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-organisation-email">
                      {{ data.organisationEmail }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6" v-if="data.organisationPays">
                    <dt id="lbl-organisation-pays">
                      <v-label class="ge-field-label">{{ t("informationsPersonnelles.pays") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-organisation-pays">
                      {{ formatPays(data.organisationPays) }}
                    </dd>
                  </v-col>
                </v-row>
              </dl>
            </v-col>
          </v-row>
        </section>
      </div>
      <v-card-actions class="pa-0 pt-4">
        <v-btn variant="outlined" color="primary" prepend-icon="mdi-pencil" @click="goTo(3)">{{
          t("common.modifier")
        }}</v-btn>
      </v-card-actions>
    </v-card>

    <v-card class="pa-2 pa-md-6 mb-4">
      <h2 id="recap-event" class="pre-plainte-main-card-title mb-4 text-h2 text-wrap">
        {{ t("informationsEvenement.titre") }}
      </h2>
      <div class="mb-4">
        <v-row class="ma-0">
          <v-col cols="12" class="pa-0">
            <dl class="w-100">
              <v-row class="ma-0">
                <v-col cols="12" md="6">
                  <dt id="lbl-typeIncident">
                    <v-label class="ge-field-label">{{ t("informationsEvenement.typeIncident") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-typeIncident">
                    {{ formatTypeIncident(data.typeIncident) }}
                  </dd>
                </v-col>

                <template v-if="!data.typeCybercrime || (data.typeCybercrime !== 'achat-non-recu' && data.typeCybercrime !== 'fausse-annonce')">
                  <v-col cols="12" md="6">
                    <dt id="lbl-dateDebutEvenement">
                      <v-label class="ge-field-label">{{ t("informationsEvenement.dateDebutEvenement") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-dateDebutEvenement">
                      {{ formatDateTimeFrench(toIsoDate(data.dateDebutEvenement)) }}
                    </dd>
                  </v-col>

                  <v-col cols="12" md="6">
                    <dt id="lbl-dateFinEvenement">
                      <v-label class="ge-field-label">{{ t("informationsEvenement.dateFinEvenement") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-dateFinEvenement">
                      {{ formatDateTimeFrench(toIsoDate(data.dateFinEvenement)) }}
                    </dd>
                  </v-col>
                </template>

                <template v-if="data.typeIncident !== 'cybercrime'">
                  <v-col v-if="isFieldVisible('adresseLesee') && data.adresseLesee" cols="12" md="6">
                    <dt id="lbl-adresseLesee">
                      <v-label class="ge-field-label">{{ t("adresseEvent.adresseCorrespond") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-adresseLesee">
                      {{ data.adresseLesee ? t("adresseEvent.adresseTiers") : t("adresseEvent.adresseAutre") }}
                    </dd>
                  </v-col>

                  <template v-if="!data.adresseLesee">
                    <v-col v-if="isFieldVisible('typeLieu') && data.typeLieu" cols="12" md="6">
                      <dt id="lbl-typeLieu">
                        <v-label class="ge-field-label">{{ t("adresseEvent.typeLieu") }}</v-label>
                      </dt>
                      <dd class="ge-field-value text-body-1" aria-labelledby="lbl-typeLieu">
                        {{ formatRipol(data.typeLieu) }}
                      </dd>
                    </v-col>

                    <v-col v-if="isFieldVisible('adresseConnue') && data.adresseConnue" cols="12" md="6">
                      <dt id="lbl-adresseConnue">
                        <v-label class="ge-field-label">{{ t("adresseEvent.adresseConnue") }}</v-label>
                      </dt>
                      <dd class="ge-field-value text-body-1" aria-labelledby="lbl-adresseConnue">
                        {{ data.adresseConnue }}
                      </dd>
                    </v-col>

                    <template v-if="hasAdresseEvenementPrincipale">
                      <v-col cols="12">
                        <dt id="lbl-adresseEvenementSection">
                          <v-label class="ge-field-label">
                            {{
                              isTrajetRenseigne ? t("adresseEvent.adresseDepart") : t("informationsPersonnelles.adresse")
                            }}
                          </v-label>
                        </dt>
                      </v-col>

                      <v-col cols="12" md="6" v-if="isFieldVisible('adresseEvenement') && data.adresseEvenement">
                        <dt id="lbl-adresseEvenement">
                          <v-label class="ge-field-label">{{ t("informationsPersonnelles.adresse") }}</v-label>
                        </dt>
                        <dd class="ge-field-value text-body-1" aria-labelledby="lbl-adresseEvenement">
                          {{ data.adresseEvenement }}
                        </dd>
                      </v-col>

                      <v-col
                        cols="12"
                        md="6"
                        v-if="isFieldVisible('adressePostaleEvenement') && data.adressePostaleEvenement"
                      >
                        <dt id="lbl-adressePostaleEvenement">
                          <v-label class="ge-field-label">{{ t("informationsPersonnelles.adressePostale") }}</v-label>
                        </dt>
                        <dd class="ge-field-value text-body-1" aria-labelledby="lbl-adressePostaleEvenement">
                          {{ data.adressePostaleEvenement }}
                        </dd>
                      </v-col>

                      <v-col cols="12" md="3" v-if="isFieldVisible('npaEvenement') && data.npaEvenement">
                        <dt id="lbl-npaEvenement">
                          <v-label class="ge-field-label">{{ t("informationsPersonnelles.npa") }}</v-label>
                        </dt>
                        <dd class="ge-field-value text-body-1" aria-labelledby="lbl-npaEvenement">
                          {{ data.npaEvenement }}
                        </dd>
                      </v-col>

                      <v-col cols="12" md="3" v-if="isFieldVisible('localiteEvenement') && data.localiteEvenement">
                        <dt id="lbl-localiteEvenement">
                          <v-label class="ge-field-label">{{ t("informationsPersonnelles.localite") }}</v-label>
                        </dt>
                        <dd class="ge-field-value text-body-1" aria-labelledby="lbl-localiteEvenement">
                          {{ data.localiteEvenement }}
                        </dd>
                      </v-col>

                      <v-col cols="12" md="6" v-if="data.paysEvenement">
                        <dt id="lbl-paysEvenement">
                          <v-label class="ge-field-label">{{ t("informationsPersonnelles.pays") }}</v-label>
                        </dt>
                        <dd class="ge-field-value text-body-1" aria-labelledby="lbl-paysEvenement">
                          {{ formatPays(data.paysEvenement) }}
                        </dd>
                      </v-col>

                      <v-col cols="12" md="6" v-if="isFieldVisible('lieuOrigine') && data.lieuOrigine">
                        <dt id="lbl-lieuOrigine">
                          <v-label class="ge-field-label">{{ t("adresseEvent.lieuOrigine") }}</v-label>
                        </dt>
                        <dd class="ge-field-value text-body-1" aria-labelledby="lbl-lieuOrigine">
                          {{ formatRipol(data.lieuOrigine) }}
                        </dd>
                      </v-col>
                    </template>

                    <template v-if="hasAdresseEvenementSecondaire">
                      <v-col cols="12" class="mt-2">
                        <dt id="lbl-adresseEvenementSecondaireSection">
                          <v-label class="ge-field-label">{{ t("adresseEvent.adresseDestination") }}</v-label>
                        </dt>
                      </v-col>

                      <v-col cols="12" md="6" v-if="data.adresseEvenementSecondaire">
                        <dt id="lbl-adresseEvenementSecondaire">
                          <v-label class="ge-field-label">{{ t("informationsPersonnelles.adresse") }}</v-label>
                        </dt>
                        <dd class="ge-field-value text-body-1" aria-labelledby="lbl-adresseEvenementSecondaire">
                          {{ data.adresseEvenementSecondaire }}
                        </dd>
                      </v-col>

                      <v-col cols="12" md="6" v-if="data.adressePostaleEvenementSecondaire">
                        <dt id="lbl-adressePostaleEvenementSecondaire">
                          <v-label class="ge-field-label">{{ t("informationsPersonnelles.adressePostale") }}</v-label>
                        </dt>
                        <dd class="ge-field-value text-body-1" aria-labelledby="lbl-adressePostaleEvenementSecondaire">
                          {{ data.adressePostaleEvenementSecondaire }}
                        </dd>
                      </v-col>

                      <v-col cols="12" md="3" v-if="data.npaEvenementSecondaire">
                        <dt id="lbl-npaEvenementSecondaire">
                          <v-label class="ge-field-label">{{ t("informationsPersonnelles.npa") }}</v-label>
                        </dt>
                        <dd class="ge-field-value text-body-1" aria-labelledby="lbl-npaEvenementSecondaire">
                          {{ data.npaEvenementSecondaire }}
                        </dd>
                      </v-col>

                      <v-col cols="12" md="3" v-if="data.localiteEvenementSecondaire">
                        <dt id="lbl-localiteEvenementSecondaire">
                          <v-label class="ge-field-label">{{ t("informationsPersonnelles.localite") }}</v-label>
                        </dt>
                        <dd class="ge-field-value text-body-1" aria-labelledby="lbl-localiteEvenementSecondaire">
                          {{ data.localiteEvenementSecondaire }}
                        </dd>
                      </v-col>

                      <v-col cols="12" md="6" v-if="data.paysEvenementSecondaire">
                        <dt id="lbl-paysEvenementSecondaire">
                          <v-label class="ge-field-label">{{ t("informationsPersonnelles.pays") }}</v-label>
                        </dt>
                        <dd class="ge-field-value text-body-1" aria-labelledby="lbl-paysEvenementSecondaire">
                          {{ formatPays(data.paysEvenementSecondaire) }}
                        </dd>
                      </v-col>
                    </template>
                  </template>
                </template>

                <v-col v-if="isFieldVisible('volDansVehicule') && data.volDansVehicule !== undefined" cols="12" md="6">
                  <dt id="lbl-volDansVehicule">
                    <v-label class="ge-field-label">{{ t("incidentTypes.volDansVehicule") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-volDansVehicule">
                    {{ data.volDansVehicule ? t("common.oui") : t("common.non") }}
                  </dd>
                </v-col>

                <template v-if="recapVolObjetsMulti(data)">
                  <v-col cols="12">
                    <v-sheet
                      v-for="(obj, vIdx) in data.objetsVolesValides"
                      :key="`recap-vol-obj-${vIdx}`"
                      class="pa-4 mb-4"
                      rounded
                      border
                    >
                      <div class="text-subtitle-1 font-weight-medium mb-3">
                        {{ t("incidentTypes.objetVoleNumero", { n: vIdx + 1 }) }}
                      </div>
                      <v-row dense>
                        <v-col cols="12" md="6">
                          <dt :id="`lbl-vo-cat-${vIdx}`">
                            <v-label class="ge-field-label">{{ t("categoriesObjets.titre") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-cat-${vIdx}`">
                            {{ formatCategorie(obj.categorieObjet) }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.sousCategorie" cols="12" md="6">
                          <dt :id="`lbl-vo-sub-${vIdx}`">
                            <v-label class="ge-field-label">{{ t("sousCategories.titre") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-sub-${vIdx}`">
                            {{ formatSousCategorie(obj.sousCategorie, obj.categorieObjet) }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.typeObjet" cols="12" md="6">
                          <dt :id="`lbl-vo-type-${vIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.typeObjet") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-type-${vIdx}`">
                            {{ formatRipol(obj.typeObjet) }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.fabricant && obj.fabricant.code !== 'AUTRE'" cols="12" md="6">
                          <dt :id="`lbl-vo-fab-${vIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.fabricant") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-fab-${vIdx}`">
                            {{ formatRipol(obj.fabricant) }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.fabricant?.code === 'AUTRE' && obj.fabricantAutre" cols="12" md="6">
                          <dt :id="`lbl-vo-faba-${vIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.fabricant") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-faba-${vIdx}`">
                            {{ obj.fabricantAutre }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.modele && obj.modele.code !== 'AUTRE'" cols="12" md="6">
                          <dt :id="`lbl-vo-mod-${vIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.modele") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-mod-${vIdx}`">
                            {{ formatRipol(obj.modele) }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.modele?.code === 'AUTRE' && obj.modeleAutre" cols="12" md="6">
                          <dt :id="`lbl-vo-moda-${vIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.modele") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-moda-${vIdx}`">
                            {{ obj.modeleAutre }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.couleur" cols="12" md="6">
                          <dt :id="`lbl-vo-coul-${vIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.couleur") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-coul-${vIdx}`">
                            {{ formatRipol(obj.couleur) }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.couleurSecondaire" cols="12" md="6">
                          <dt :id="`lbl-vo-coul2-${vIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.couleurSecondaire") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-coul2-${vIdx}`">
                            {{ formatRipol(obj.couleurSecondaire) }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.valeurReelle" cols="12" md="6">
                          <dt :id="`lbl-vo-val-${vIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.valeurReelle") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-val-${vIdx}`">
                            {{ obj.valeurReelle }} CHF
                          </dd>
                        </v-col>
                        <v-col v-if="obj.categorieObjet === 'bijoux' && obj.gravure" cols="12" md="6">
                          <dt :id="`lbl-vo-grav-${vIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.gravure") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-grav-${vIdx}`">
                            {{ obj.gravure }}
                          </dd>
                        </v-col>
                        <template v-if="obj.categorieObjet === 'vehicule'">
                          <v-col v-if="obj.sousCategorie === 'velos' && (obj.numeroCadre || obj.numeroCadreInconnu)" cols="12" md="6">
                            <dt :id="`lbl-vo-cadre-${vIdx}`">
                              <v-label class="ge-field-label">{{ t("incidentTypes.numeroCadre") }}</v-label>
                            </dt>
                            <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-cadre-${vIdx}`">
                              {{
                                obj.numeroCadreInconnu
                                  ? t("incidentTypes.numeroCadreInconnu")
                                  : obj.numeroCadre || "—"
                              }}
                            </dd>
                          </v-col>
                          <template v-if="obj.sousCategorie && VEHICULE_CATEGORIES_AVEC_VIN.includes(obj.sousCategorie)">
                            <v-col cols="12" md="6">
                              <dt :id="`lbl-vo-vin-${vIdx}`">
                                <v-label class="ge-field-label">{{ t("incidentTypes.vin") }}</v-label>
                              </dt>
                              <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-vin-${vIdx}`">
                                {{ obj.vinInconnu ? t("incidentTypes.vinInconnu") : obj.vin || "—" }}
                              </dd>
                            </v-col>
                          </template>
                          <template v-if="obj.sousCategorie && VEHICULE_CATEGORIES_AVEC_PLAQUE.includes(obj.sousCategorie)">
                            <v-col v-if="obj.plaqueNumero && !obj.plaqueInconnu" cols="12" md="6">
                              <dt :id="`lbl-vo-plaq-${vIdx}`">
                                <v-label class="ge-field-label">{{ t("incidentTypes.plaqueNumero") }}</v-label>
                              </dt>
                              <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-plaq-${vIdx}`">
                                {{ obj.plaqueNumero }}
                              </dd>
                            </v-col>
                            <v-col v-if="obj.plaqueInconnu" cols="12" md="6">
                              <dt :id="`lbl-vo-plaqinc-${vIdx}`">
                                <v-label class="ge-field-label">{{ t("incidentTypes.plaqueInconnu") }}</v-label>
                              </dt>
                              <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-plaqinc-${vIdx}`">
                                {{ t("common.oui") }}
                              </dd>
                            </v-col>
                            <v-col v-if="obj.plaquePays && !obj.plaqueInconnu" cols="12" md="6">
                              <dt :id="`lbl-vo-plaqpays-${vIdx}`">
                                <v-label class="ge-field-label">{{ t("incidentTypes.plaquePays") }}</v-label>
                              </dt>
                              <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-plaqpays-${vIdx}`">
                                {{ formatRipol(obj.plaquePays) }}
                              </dd>
                            </v-col>
                            <v-col
                              v-if="obj.plaqueCanton && !obj.plaqueInconnu && obj.plaquePays?.code === RIPOL.PAYS_SUISSE"
                              cols="12"
                              md="6"
                            >
                              <dt :id="`lbl-vo-plaqcant-${vIdx}`">
                                <v-label class="ge-field-label">{{ t("incidentTypes.plaqueCanton") }}</v-label>
                              </dt>
                              <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-plaqcant-${vIdx}`">
                                {{ formatRipol(obj.plaqueCanton) }}
                              </dd>
                            </v-col>
                          </template>
                          <v-col v-if="obj.dateAchat" cols="12" md="6">
                            <dt :id="`lbl-vo-da-${vIdx}`">
                              <v-label class="ge-field-label">{{ t("incidentTypes.dateAchat") }}</v-label>
                            </dt>
                            <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-da-${vIdx}`">
                              {{ obj.dateAchat }}
                            </dd>
                          </v-col>
                        </template>
                        <template v-else-if="obj.categorieObjet === 'plaque'">
                          <v-col cols="12" md="6">
                            <dt :id="`lbl-vo-plaq-pays-${vIdx}`">
                              <v-label class="ge-field-label">{{ t("incidentTypes.plaquePays") }}</v-label>
                            </dt>
                            <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-plaq-pays-${vIdx}`">
                              {{ obj.plaquePays ? formatRipol(obj.plaquePays) : "—" }}
                            </dd>
                          </v-col>
                          <v-col cols="12" md="6">
                            <dt :id="`lbl-vo-plaq-${vIdx}`">
                              <v-label class="ge-field-label">{{ t("incidentTypes.plaqueNumero") }}</v-label>
                            </dt>
                            <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-plaq-${vIdx}`">
                              {{ obj.plaqueNumero || "—" }}
                            </dd>
                          </v-col>
                        </template>
                        <v-col v-else cols="12" md="6">
                          <dt :id="`lbl-vo-ser-${vIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.numeroSerie") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-ser-${vIdx}`">
                            {{
                              obj.numeroSerieInconnu
                                ? t("incidentTypes.numeroSerieInconnu")
                                : obj.numeroSerie || "—"
                            }}
                          </dd>
                        </v-col>
                        <template v-if="hasImei(obj.typeObjet)">
                          <v-col cols="12" md="6">
                            <dt :id="`lbl-vo-imei-${vIdx}`">
                              <v-label class="ge-field-label">{{ t("incidentTypes.numeroImei") }}</v-label>
                            </dt>
                            <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-imei-${vIdx}`">
                              {{
                                obj.numeroIMEIInconnu
                                  ? t("incidentTypes.numeroIMEIInconnu")
                                  : obj.numeroIMEI || "—"
                              }}
                            </dd>
                          </v-col>
                          <v-col
                            v-if="obj.numeroIMEIInconnu && obj.justificationAbsenceIMEI"
                            cols="12"
                            md="6"
                          >
                            <dt :id="`lbl-vo-jimei-${vIdx}`">
                              <v-label class="ge-field-label">{{ t("incidentTypes.justificationAbsenceIMEI") }}</v-label>
                            </dt>
                            <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-jimei-${vIdx}`">
                              {{ obj.justificationAbsenceIMEI }}
                            </dd>
                          </v-col>
                        </template>
                        <v-col v-if="obj.descriptionObjet" cols="12">
                          <dt :id="`lbl-vo-desc-${vIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.descriptionComplementaireObjet") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-vo-desc-${vIdx}`">
                            {{ obj.descriptionObjet }}
                          </dd>
                        </v-col>
                      </v-row>
                    </v-sheet>
                  </v-col>
                </template>

                <template v-if="recapDommageVehiculesMulti(data)">
                  <v-col cols="12">
                    <v-sheet
                      v-for="(obj, dIdx) in data.objetsDegradesValides"
                      :key="`recap-dom-veh-${dIdx}`"
                      class="pa-4 mb-4"
                      rounded
                      border
                    >
                      <div class="text-subtitle-1 font-weight-medium mb-3">
                        {{ t("dommages.vehiculeEndommageNumero", { n: dIdx + 1 }) }}
                      </div>
                      <v-row dense>
                        <v-col v-if="obj.sousCategorie" cols="12" md="6">
                          <dt :id="`lbl-dv-sub-${dIdx}`">
                            <v-label class="ge-field-label">{{ t("sousCategories.titre") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-sub-${dIdx}`">
                            {{ formatSousCategorie(obj.sousCategorie, "vehicule") }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.typeObjet" cols="12" md="6">
                          <dt :id="`lbl-dv-type-${dIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.typeObjet") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-type-${dIdx}`">
                            {{ formatRipol(obj.typeObjet) }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.fabricant && obj.fabricant.code !== 'AUTRE'" cols="12" md="6">
                          <dt :id="`lbl-dv-fab-${dIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.fabricant") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-fab-${dIdx}`">
                            {{ formatRipol(obj.fabricant) }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.fabricant?.code === 'AUTRE' && obj.fabricantAutre" cols="12" md="6">
                          <dt :id="`lbl-dv-faba-${dIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.fabricant") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-faba-${dIdx}`">
                            {{ obj.fabricantAutre }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.modele && obj.modele.code !== 'AUTRE'" cols="12" md="6">
                          <dt :id="`lbl-dv-mod-${dIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.modele") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-mod-${dIdx}`">
                            {{ formatRipol(obj.modele) }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.modele?.code === 'AUTRE' && obj.modeleAutre" cols="12" md="6">
                          <dt :id="`lbl-dv-moda-${dIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.modele") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-moda-${dIdx}`">
                            {{ obj.modeleAutre }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.couleur" cols="12" md="6">
                          <dt :id="`lbl-dv-coul-${dIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.couleur") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-coul-${dIdx}`">
                            {{ formatRipol(obj.couleur) }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.couleurSecondaire" cols="12" md="6">
                          <dt :id="`lbl-dv-coul2-${dIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.couleurSecondaire") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-coul2-${dIdx}`">
                            {{ formatRipol(obj.couleurSecondaire) }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.valeurReelle" cols="12" md="6">
                          <dt :id="`lbl-dv-val-${dIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.valeurReelle") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-val-${dIdx}`">
                            {{ obj.valeurReelle }} CHF
                          </dd>
                        </v-col>
                        <v-col
                          v-if="obj.sousCategorie === 'velos' && obj.numeroCadre && !obj.numeroCadreInconnu"
                          cols="12"
                          md="6"
                        >
                          <dt :id="`lbl-dv-cadre-${dIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.numeroCadre") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-cadre-${dIdx}`">
                            {{ obj.numeroCadre }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.sousCategorie === 'velos' && obj.numeroCadreInconnu" cols="12" md="6">
                          <dt :id="`lbl-dv-cadreu-${dIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.numeroCadreInconnu") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-cadreu-${dIdx}`">
                            {{ t("common.oui") }}
                          </dd>
                        </v-col>
                        <template
                          v-if="obj.sousCategorie && VEHICULE_CATEGORIES_AVEC_VIN.includes(obj.sousCategorie)"
                        >
                          <v-col v-if="obj.vin && !obj.vinInconnu" cols="12" md="6">
                            <dt :id="`lbl-dv-vin-${dIdx}`">
                              <v-label class="ge-field-label">{{ t("incidentTypes.vin") }}</v-label>
                            </dt>
                            <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-vin-${dIdx}`">
                              {{ obj.vin }}
                            </dd>
                          </v-col>
                          <v-col v-if="obj.vinInconnu" cols="12" md="6">
                            <dt :id="`lbl-dv-vinu-${dIdx}`">
                              <v-label class="ge-field-label">{{ t("incidentTypes.vinInconnu") }}</v-label>
                            </dt>
                            <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-vinu-${dIdx}`">
                              {{ t("common.oui") }}
                            </dd>
                          </v-col>
                        </template>
                        <template
                          v-if="obj.sousCategorie && VEHICULE_CATEGORIES_AVEC_PLAQUE.includes(obj.sousCategorie)"
                        >
                          <v-col v-if="obj.plaqueNumero && !obj.plaqueInconnu" cols="12" md="6">
                            <dt :id="`lbl-dv-plaq-${dIdx}`">
                              <v-label class="ge-field-label">{{ t("incidentTypes.plaqueNumero") }}</v-label>
                            </dt>
                            <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-plaq-${dIdx}`">
                              {{ obj.plaqueNumero }}
                            </dd>
                          </v-col>
                          <v-col v-if="obj.plaqueInconnu" cols="12" md="6">
                            <dt :id="`lbl-dv-plaqi-${dIdx}`">
                              <v-label class="ge-field-label">{{ t("incidentTypes.plaqueInconnu") }}</v-label>
                            </dt>
                            <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-plaqi-${dIdx}`">
                              {{ t("common.oui") }}
                            </dd>
                          </v-col>
                          <v-col v-if="obj.plaquePays && !obj.plaqueInconnu" cols="12" md="6">
                            <dt :id="`lbl-dv-ppays-${dIdx}`">
                              <v-label class="ge-field-label">{{ t("incidentTypes.plaquePays") }}</v-label>
                            </dt>
                            <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-ppays-${dIdx}`">
                              {{ formatRipol(obj.plaquePays) }}
                            </dd>
                          </v-col>
                          <v-col
                            v-if="
                              obj.plaqueCanton &&
                              !obj.plaqueInconnu &&
                              obj.plaquePays?.code === RIPOL.PAYS_SUISSE
                            "
                            cols="12"
                            md="6"
                          >
                            <dt :id="`lbl-dv-pcant-${dIdx}`">
                              <v-label class="ge-field-label">{{ t("incidentTypes.plaqueCanton") }}</v-label>
                            </dt>
                            <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-pcant-${dIdx}`">
                              {{ formatRipol(obj.plaqueCanton) }}
                            </dd>
                          </v-col>
                        </template>
                        <v-col v-if="obj.dateAchat" cols="12" md="6">
                          <dt :id="`lbl-dv-da-${dIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.dateAchat") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-da-${dIdx}`">
                            {{ obj.dateAchat }}
                          </dd>
                        </v-col>
                        <v-col v-if="obj.descriptionObjet" cols="12">
                          <dt :id="`lbl-dv-desc-${dIdx}`">
                            <v-label class="ge-field-label">{{ t("incidentTypes.descriptionObjet") }}</v-label>
                          </dt>
                          <dd class="ge-field-value text-body-1" :aria-labelledby="`lbl-dv-desc-${dIdx}`">
                            {{ obj.descriptionObjet }}
                          </dd>
                        </v-col>
                      </v-row>
                    </v-sheet>
                  </v-col>
                </template>

                <v-col v-if="isFieldVisible('categorieObjet') && data.categorieObjet && !recapObjetsIncidentMulti(data)" cols="12" md="6">
                  <dt id="lbl-categorieObjet">
                    <v-label class="ge-field-label">{{ t("categoriesObjets.titre") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-categorieObjet">
                    {{ formatCategorie(data.categorieObjet) }}
                  </dd>
                </v-col>

                <v-col v-if="isFieldVisible('typeDommage') && !!data.typeDommage" cols="12" md="6">
                  <dt id="lbl-typeDommage">
                    <v-label class="ge-field-label">{{ t("dommages.typeDommage") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-typeDommage">
                    {{ formatTypeDommage(data.typeDommage) }}
                  </dd>
                </v-col>

                <v-col v-if="isFieldVisible('sousCategorie') && data.sousCategorie && !recapObjetsIncidentMulti(data)" cols="12" md="6">
                  <dt id="lbl-sousCategorie">
                    <v-label class="ge-field-label">{{ t("sousCategories.titre") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-sousCategorie">
                    {{ formatSousCategorie(data.sousCategorie, data.categorieObjet) }}
                  </dd>
                </v-col>

                <v-col
                  v-if="
                    (data.typeIncident === 'vol' || data.typeDommage === 'dommage-vehicule') &&
                    data.typeObjet &&
                    !recapObjetsIncidentMulti(data)
                  "
                  cols="12"
                  md="6"
                >
                  <dt id="lbl-typeObjet">
                    <v-label class="ge-field-label">{{ t("incidentTypes.typeObjet") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-typeObjet">
                    {{ formatRipol(data.typeObjet) }}
                  </dd>
                </v-col>

                <v-col
                  v-if="
                    isFieldVisible('fabricant') &&
                    data.fabricant &&
                    data.fabricant?.code !== 'AUTRE' &&
                    !recapObjetsIncidentMulti(data)
                  "
                  cols="12"
                  md="6"
                >
                  <dt id="lbl-fabricant">
                    <v-label class="ge-field-label">{{ t("incidentTypes.fabricant") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-fabricant">
                    {{ formatRipol(data.fabricant) }}
                  </dd>
                </v-col>

                <v-col
                  v-if="
                    isFieldVisible('fabricantAutre') &&
                    data.fabricantAutre &&
                    data.fabricant?.code === 'AUTRE' &&
                    !recapObjetsIncidentMulti(data)
                  "
                  cols="12"
                  md="6"
                >
                  <dt id="lbl-fabricantAutre">
                    <v-label class="ge-field-label">{{ t("incidentTypes.fabricant") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-fabricantAutre">
                    {{ data.fabricantAutre }}
                  </dd>
                </v-col>

                <v-col
                  v-if="
                    isFieldVisible('modele') &&
                    data.modele &&
                    data.modele?.code !== 'AUTRE' &&
                    !recapObjetsIncidentMulti(data)
                  "
                  cols="12"
                  md="6"
                >
                  <dt id="lbl-modele">
                    <v-label class="ge-field-label">{{ t("incidentTypes.modele") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-modele">
                    {{ formatRipol(data.modele) }}
                  </dd>
                </v-col>

                <v-col
                  v-if="
                    isFieldVisible('modeleAutre') &&
                    data.modeleAutre &&
                    data.modele?.code === 'AUTRE' &&
                    !recapObjetsIncidentMulti(data)
                  "
                  cols="12"
                  md="6"
                >
                  <dt id="lbl-modeleAutre">
                    <v-label class="ge-field-label">{{ t("incidentTypes.modele") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-modeleAutre">
                    {{ data.modeleAutre }}
                  </dd>
                </v-col>

                <v-col
                  v-if="
                    (data.typeIncident === 'vol' || data.typeDommage === 'dommage-vehicule') &&
                    data.couleur &&
                    !recapObjetsIncidentMulti(data)
                  "
                  cols="12"
                  md="6"
                >
                  <dt id="lbl-couleur">
                    <v-label class="ge-field-label">{{ t("incidentTypes.couleur") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-couleur">
                    {{ formatRipol(data.couleur) }}
                  </dd>
                </v-col>

                <v-col
                  v-if="
                    (data.typeIncident === 'vol' || data.typeDommage === 'dommage-vehicule') &&
                    data.couleurSecondaire &&
                    !recapObjetsIncidentMulti(data)
                  "
                  cols="12"
                  md="6"
                >
                  <dt id="lbl-couleurSecondaire">
                    <v-label class="ge-field-label">{{ t("incidentTypes.couleurSecondaire") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-couleurSecondaire">
                    {{ formatRipol(data.couleurSecondaire) }}
                  </dd>
                </v-col>

                <template v-if="data.sousCategorie === 'velos' && !recapObjetsIncidentMulti(data)">
                  <v-col
                    v-if="isFieldVisible('numeroCadre') && data.numeroCadre && !data.numeroCadreInconnu"
                    cols="12"
                    md="6"
                  >
                    <dt id="lbl-numeroCadre">
                      <v-label class="ge-field-label">{{ t("incidentTypes.numeroCadre") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-numeroCadre">
                      {{ data.numeroCadre }}
                    </dd>
                  </v-col>

                  <v-col v-if="isFieldVisible('numeroCadreInconnu') && data.numeroCadreInconnu" cols="12" md="6">
                    <dt id="lbl-numeroCadreInconnu">
                      <v-label class="ge-field-label">{{ t("incidentTypes.numeroCadreInconnu") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-numeroCadreInconnu">
                      {{ t("common.oui") }}
                    </dd>
                  </v-col>
                </template>

                <template
                  v-if="
                    data.sousCategorie &&
                    VEHICULE_CATEGORIES_AVEC_VIN.includes(data.sousCategorie) &&
                    !recapObjetsIncidentMulti(data)
                  "
                >
                  <v-col v-if="isFieldVisible('vin') && data.vin && !data.vinInconnu" cols="12" md="6">
                    <dt id="lbl-vin">
                      <v-label class="ge-field-label">{{ t("incidentTypes.vin") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-vin">
                      {{ data.vin }}
                    </dd>
                  </v-col>

                  <v-col v-if="isFieldVisible('vinInconnu') && data.vinInconnu" cols="12" md="6">
                    <dt id="lbl-vinInconnu">
                      <v-label class="ge-field-label">{{ t("incidentTypes.vinInconnu") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-vinInconnu">
                      {{ t("common.oui") }}
                    </dd>
                  </v-col>
                </template>

                <v-col
                  v-if="
                    isFieldVisible('velofinderId') &&
                    data.velofinderId &&
                    data.sousCategorie === 'velos' &&
                    !recapObjetsIncidentMulti(data)
                  "
                  cols="12"
                  md="6"
                >
                  <dt id="lbl-velofinderId">
                    <v-label class="ge-field-label">{{ t("incidentTypes.velofinderId") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-velofinderId">
                    {{ data.velofinderId }}
                  </dd>
                </v-col>

                <v-col
                  v-if="
                    isFieldVisible('dateAchat') &&
                    data.dateAchat &&
                    (data.categorieObjet === 'vehicule' || data.typeDommage === 'dommage-vehicule') &&
                    !recapObjetsIncidentMulti(data)
                  "
                  cols="12"
                  md="6"
                >
                  <dt id="lbl-dateAchat">
                    <v-label class="ge-field-label">{{ t("incidentTypes.dateAchat") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-dateAchat">
                    {{ data.dateAchat }}
                  </dd>
                </v-col>

                <template
                  v-if="
                    data.sousCategorie &&
                    VEHICULE_CATEGORIES_AVEC_PLAQUE.includes(data.sousCategorie) &&
                    !recapObjetsIncidentMulti(data)
                  "
                >
                  <v-col
                    v-if="isFieldVisible('plaqueNumero') && data.plaqueNumero && !data.plaqueInconnu"
                    cols="12"
                    md="6"
                  >
                    <dt id="lbl-plaqueNumero">
                      <v-label class="ge-field-label">{{ t("incidentTypes.plaqueNumero") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-plaqueNumero">
                      {{ data.plaqueNumero }}
                    </dd>
                  </v-col>

                  <v-col v-if="isFieldVisible('plaqueInconnu') && data.plaqueInconnu" cols="12" md="6">
                    <dt id="lbl-plaqueInconnu">
                      <v-label class="ge-field-label">{{ t("incidentTypes.plaqueInconnu") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-plaqueInconnu">
                      {{ t("common.oui") }}
                    </dd>
                  </v-col>

                  <v-col v-if="isFieldVisible('plaquePays') && data.plaquePays && !data.plaqueInconnu" cols="12" md="6">
                    <dt id="lbl-plaquePays">
                      <v-label class="ge-field-label">{{ t("incidentTypes.plaquePays") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-plaquePays">
                      {{ formatRipol(data.plaquePays) }}
                    </dd>
                  </v-col>

                  <v-col
                    v-if="
                      isFieldVisible('plaqueCanton') &&
                      data.plaqueCanton &&
                      !data.plaqueInconnu &&
                      data.plaquePays?.code === RIPOL.PAYS_SUISSE
                    "
                    cols="12"
                    md="6"
                  >
                    <dt id="lbl-plaqueCanton">
                      <v-label class="ge-field-label">{{ t("incidentTypes.plaqueCanton") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-plaqueCanton">
                      {{ formatRipol(data.plaqueCanton) }}
                    </dd>
                  </v-col>
                </template>

                <v-col
                  v-if="
                    isFieldVisible('gravure') &&
                    data.categorieObjet === 'bijoux' &&
                    data.gravure &&
                    !recapObjetsIncidentMulti(data)
                  "
                  cols="12"
                  md="6"
                >
                  <dt id="lbl-gravure">
                    <v-label class="ge-field-label">{{ t("incidentTypes.gravure") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-gravure">
                    {{ data.gravure }}
                  </dd>
                </v-col>

                <v-col v-if="isFieldVisible('valeurReelle') && data.valeurReelle && !recapObjetsIncidentMulti(data)" cols="12" md="6">
                  <dt id="lbl-valeurReelle">
                    <v-label class="ge-field-label">{{ t("incidentTypes.valeurReelle") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-valeurReelle">
                    {{ data.valeurReelle }}
                  </dd>
                </v-col>

                <template v-if="data.categorieObjet !== 'vehicule' && !recapObjetsIncidentMulti(data)">
                  <v-col
                    v-if="isFieldVisible('numeroSerie') && data.numeroSerie && !data.numeroSerieInconnu"
                    cols="12"
                    md="6"
                  >
                    <dt id="lbl-numeroSerie">
                      <v-label class="ge-field-label">{{ t("incidentTypes.numeroSerie") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-numeroSerie">
                      {{ data.numeroSerie }}
                    </dd>
                  </v-col>

                  <v-col v-if="isFieldVisible('numeroSerieInconnu') && data.numeroSerieInconnu" cols="12" md="6">
                    <dt id="lbl-numeroSerieInconnu">
                      <v-label class="ge-field-label">{{ t("incidentTypes.numeroSerieInconnu") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-numeroSerieInconnu">
                      {{ t("common.oui") }}
                    </dd>
                  </v-col>
                </template>

                <template v-if="hasImei(data.typeObjet) && !recapObjetsIncidentMulti(data)">
                  <v-col
                    v-if="isFieldVisible('numeroIMEI') && data.numeroIMEI && !data.numeroIMEIInconnu"
                    cols="12"
                    md="6"
                  >
                    <dt id="lbl-numeroIMEI">
                      <v-label class="ge-field-label">{{ t("incidentTypes.numeroImei") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-numeroIMEI">
                      {{ data.numeroIMEI }}
                    </dd>
                  </v-col>

                  <v-col v-if="isFieldVisible('numeroIMEIInconnu') && data.numeroIMEIInconnu" cols="12" md="6">
                    <dt id="lbl-numeroIMEIInconnu">
                      <v-label class="ge-field-label">{{ t("incidentTypes.numeroIMEIInconnu") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-numeroIMEIInconnu">
                      {{ t("common.oui") }}
                    </dd>
                  </v-col>

                  <v-col
                    v-if="
                      isFieldVisible('justificationAbsenceIMEI') &&
                      data.numeroIMEIInconnu &&
                      data.justificationAbsenceIMEI
                    "
                    cols="12"
                    md="6"
                  >
                    <dt id="lbl-justificationAbsenceIMEI">
                      <v-label class="ge-field-label">{{ t("incidentTypes.justificationAbsenceIMEI") }}</v-label>
                    </dt>
                    <dd class="ge-field-value text-body-1" aria-labelledby="lbl-justificationAbsenceIMEI">
                      {{ data.justificationAbsenceIMEI }}
                    </dd>
                  </v-col>
                </template>

                <v-col
                  v-if="
                    isFieldVisible('descriptionObjet') &&
                    data.descriptionObjet &&
                    !recapObjetsIncidentMulti(data)
                  "
                  cols="12"
                >
                  <dt id="lbl-descriptionObjet">
                    <v-label class="ge-field-label">
                      {{
                        t(data.typeIncident === "vol"
                          ? "incidentTypes.descriptionComplementaireObjet"
                          : "incidentTypes.descriptionObjet")
                      }}
                    </v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-descriptionObjet">
                    {{ data.descriptionObjet }}
                  </dd>
                </v-col>

                <v-col v-if="isFieldVisible('avezVousDegradation') && data.avezVousDegradation" cols="12" md="6">
                  <dt id="lbl-avezVousDegradation">
                    <v-label class="ge-field-label">{{ t("dommages.questionDegradation") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-avezVousDegradation">
                    {{ data.avezVousDegradation ? t("common.oui") : t("common.non") }}
                  </dd>
                </v-col>

                <v-col v-if="isFieldVisible('montantEstime') && data.montantEstime" cols="12" md="6">
                  <dt id="lbl-montantEstime">
                    <v-label class="ge-field-label">{{ t("dommages.montantEstime") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-montantEstime">
                    {{ data.devise || "CHF" }} {{ data.montantEstime }}
                  </dd>
                </v-col>

                <v-col
                  v-if="
                    isFieldVisible('naturesDommage') && Array.isArray(data.naturesDommage) && data.naturesDommage.length
                  "
                  cols="12"
                  md="6"
                >
                  <dt id="lbl-naturesDommage">
                    <v-label class="ge-field-label">{{ t("dommages.natureDommage") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-naturesDommage">
                    {{ formatNatureDommage(data.naturesDommage) }}
                  </dd>
                </v-col>

                <v-col v-if="isFieldVisible('description') && data.description" cols="12">
                  <dt id="lbl-description">
                    <v-label class="ge-field-label">{{ t("dommages.descriptionDommage") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-description">
                    {{ data.description }}
                  </dd>
                </v-col>

                <v-col v-if="isFieldVisible('constatPresent') && data.constatPresent !== undefined" cols="12" md="6">
                  <dt id="lbl-constatPresent">
                    <v-label class="ge-field-label">{{ t("dommages.constat") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-constatPresent">
                    {{ data.constatPresent ? t("common.oui") : t("common.non") }}
                  </dd>
                </v-col>

                <v-col v-if="isFieldVisible('dateConstat') && data.dateConstat" cols="12" md="6">
                  <dt id="lbl-dateConstat">
                    <v-label class="ge-field-label">{{ t("dommages.constatDate") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-dateConstat">
                    {{ data.dateConstat }}
                  </dd>
                </v-col>

                <v-col v-if="isFieldVisible('typeCybercrime') && data.typeCybercrime" cols="12">
                  <dt id="lbl-typeCybercrime">
                    <v-label class="ge-field-label">{{ t("cybercrime.type") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-typeCybercrime">
                    {{ formatTypeCybercrime(data.typeCybercrime) }}
                  </dd>
                </v-col>

                <RecapitulatifCommandeFrauduleuse :data="data" v-if="data.typeCybercrime === 'commande-frauduleuse'" />
                <RecapitulatifAchatNonRecu :data="data" v-if="data.typeCybercrime === 'achat-non-recu'" />
                <RecapitulatifFausseAnnonce :data="data" v-if="data.typeCybercrime === 'fausse-annonce'" />

                <v-col v-if="isFieldVisible('descriptionCybercrime') && data.descriptionCybercrime" cols="12">
                  <dt id="lbl-descriptionCybercrime">
                    <v-label class="ge-field-label">{{ t("cybercrime.description") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-descriptionCybercrime">
                    {{ data.descriptionCybercrime }}
                  </dd>
                </v-col>
              </v-row>
            </dl>
          </v-col>
        </v-row>
      </div>
      <v-card-actions class="pa-0 pt-4">
        <v-btn variant="outlined" color="primary" prepend-icon="mdi-pencil" @click="goTo(4)">{{
          t("common.modifier")
        }}</v-btn>
      </v-card-actions>
    </v-card>

    <v-card v-if="data.dateSouhaitee || data.creneauPrefere || data.selectedCreneau" class="pa-2 pa-md-6 mb-4">
      <h2 class="pre-plainte-main-card-title mb-4 text-h2 text-wrap">{{ t("steps.prendreRendezVous") }}</h2>
      <div class="mb-4">
        <v-row class="ma-0">
          <v-col cols="12" class="pa-0">
            <dl class="w-100">
              <v-row class="ma-0">
                <v-col v-if="data.dateSouhaitee" cols="12" md="6">
                  <dt id="lbl-dateSouhaitee">
                    <v-label class="ge-field-label">{{ t("rendezVous.dateSouhaitee") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-dateSouhaitee">
                    {{ formatDateTimeFrench(data.dateSouhaitee, true) }}
                  </dd>
                </v-col>

                <v-col v-if="data.selectedCreneau?.lieu" cols="12" md="6">
                  <dt id="lbl-selectedCreneauLieu">
                    <v-label class="ge-field-label">{{ t("informationsEvenement.lieu") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-selectedCreneauLieu">
                    {{ data.selectedCreneau?.lieu }}
                  </dd>
                </v-col>

                <v-col v-if="data.creneauPrefere || data.selectedCreneau" cols="12" md="6">
                  <dt id="lbl-creneauPrefere">
                    <v-label class="ge-field-label">{{ t("rendezVous.creneauPrefere") }}</v-label>
                  </dt>
                  <dd class="ge-field-value text-body-1" aria-labelledby="lbl-creneauPrefere">
                    {{ formatCreneau() }}
                  </dd>
                </v-col>
              </v-row>
            </dl>
          </v-col>
        </v-row>
      </div>
      <v-card-actions class="pa-0 pt-4">
        <v-btn variant="outlined" color="primary" prepend-icon="mdi-pencil" @click="goTo(5)">{{
          t("common.modifier")
        }}</v-btn>
      </v-card-actions>
    </v-card>

    <div v-if="captchaEnabled && !captchaVisible" @click.stop class="mt-6">
      <Captcha
        :model-value="captchaToken"
        :sitekey="captchaSiteKey"
        api-endpoint="https://eu.frcapi.com/api/v2"
        @solved="store.setCaptchaToken"
        @reset="store.resetCaptchaToken"
        class="mb-6"
      />
    </div>

    <v-alert
      v-if="submitError"
      type="error"
      class="mb-4"
      role="alert"
      closable
      @click:close="submitError = ''"
      :icon="mobile ? false : undefined"
    >
      {{ submitError }}
      <div class="mt-2 d-flex justify-end">
        <v-btn v-if="isRdvConflict" variant="flat" color="primary" @click="goTo(5)">
          {{ t("submission.erreurRendezVousNonDispoLien") }}
        </v-btn>
        <v-btn v-if="showEmailChallengeFix" variant="flat" color="primary" :loading="resendInProgress" @click="onResendCodeFromRecap">
          {{ t("emailChallenge.renvoyerCode") }}
        </v-btn>
      </div>
    </v-alert>

    <v-expand-transition>
      <div v-if="showEmailChallengeFix">
        <EmailChallengeOtpSection
          :model-value="confirmationEmailFix"
          :error-message="confirmationEmailFixFieldError"
          :email="data.email"
          @update:model-value="onConfirmationEmailFixInput"
        />
      </div>
    </v-expand-transition>

    <div class="d-md-none d-flex justify-center mt-4">
      <v-btn
        color="primary"
        variant="flat"
        class="w-100"
        :loading="isSubmitting"
        :disabled="isSubmitDisabled"
        @click="submit"
      >
        {{ t("submission.soumettrePrePlainte") }}
      </v-btn>
    </div>

    <div class="d-none d-md-flex justify-end mt-4">
      <v-btn
        color="primary"
        variant="flat"
        :loading="isSubmitting"
        :disabled="isSubmitDisabled"
        @click="submit"
      >
        {{ t("submission.soumettrePrePlainte") }}
      </v-btn>
    </div>

    <div class="d-md-none mt-4 d-flex flex-column align-center gap-2">
      <ExitActionsForm :is-mobile="true" />
    </div>
  </main>
</template>

<script setup lang="ts">
import { storeToRefs } from "pinia";
import { useI18n } from "vue-i18n";
import { useDisplay, useTheme } from "vuetify";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";
import { onMounted, type Ref } from "vue";
import type { PrePlainteFormFields } from "@/types/pre-plainte.interface";
import type { RipolSelection } from "@/types/ripol.interface";
import { ref, computed } from "vue";
import RecapitulatifAchatNonRecu from "@/components/pre-plainte-component/event-info/cybercrime/RecapitulatifAchatNonRecu.vue";
import RecapitulatifCommandeFrauduleuse from "@/components/pre-plainte-component/event-info/cybercrime/RecapitulatifCommandeFrauduleuse.vue";
import RecapitulatifFausseAnnonce from "@/components/pre-plainte-component/event-info/cybercrime/RecapitulatifFausseAnnonce.vue";
import { EsiriusService } from "@/services/esiriusService";
import { PrePlainteService } from "@/services/prePlainteService";
import { buildEsiriusPayload } from "@/utils/helpers/esiriusFormatBuilder";
import { buildPrePlainteForBackend } from "@/utils/preplainteFormatBuilder";
import { INCIDENT_FIELDS, INCIDENT_TYPE_MAP } from "@/utils/incident-fields";
import {
  CATEGORIES_OBJETS,
  GENRE_LABEL_KEYS,
  RIPOL,
  TOTAL_STEPS,
  VEHICULE_CATEGORIES_AVEC_PLAQUE,
  VEHICULE_CATEGORIES_AVEC_VIN,
} from "@/constants/constant";
import Captcha from "@/components/captcha/Captcha.vue";
import { getCaptchaSitekey, isCaptchaEnabled } from "@/config/config.ts";
import { useCountries } from "@/composables/useCountries";
import { verifyEmailChallengeCode, requestEmailChallengeCode } from "@/services/challengeEmailService";
import {
  getEmailChallengeCodeValidationMessage,
  sanitizeEmailChallengeCodeInput,
} from "@/utils/validations/field-validation.utils";
import { formatDateTimeFrench, toIsoDate } from "@/utils/helpers/dateHelpers.ts";
import ExitActionsForm from "@/components/actions/ExitActionsForm.vue";
import EmailChallengeOtpSection from "@/components/email/EmailChallengeOtpSection.vue";

const { t } = useI18n();
const { getCountryByCode } = useCountries();

const captchaVisible = ref(false);

onMounted(() => {
  captchaVisible.value = !!captchaToken.value
});

const formatRipol = (value: RipolSelection | null | undefined): string => {
  if (!value) {
    return "";
  }
  return value.label || value.code || "";
};

function recapVolObjetsMulti(d: PrePlainteFormFields): boolean {
  return d.typeIncident === "vol" && (d.objetsVolesValides?.length ?? 0) > 0;
}

function recapDommageVehiculesMulti(d: PrePlainteFormFields): boolean {
  return (
    d.typeIncident === "degat-delit" &&
    d.typeDommage === "dommage-vehicule" &&
    (d.objetsDegradesValides?.length ?? 0) > 0
  );
}

function recapObjetsIncidentMulti(d: PrePlainteFormFields): boolean {
  return recapVolObjetsMulti(d) || recapDommageVehiculesMulti(d);
}

const formatGenre = (value: RipolSelection | null): string => {
  if (!value) {
    return "";
  }
  const key = GENRE_LABEL_KEYS[value.code];
  return key ? t(key) : value.label;
};

const formatPays = (code: string | undefined): string => {
  if (!code) {
    return "";
  }
  const country = getCountryByCode(code);
  return country?.name || code;
};

const { mobile } = useDisplay();
const theme = useTheme();
const isDarkMode = computed(() => theme.global.current.value.dark);
const innerElevation = computed(() => (isDarkMode.value ? 2 : 1));
const innerVariant = computed(() => (isDarkMode.value ? "tonal" : "flat") as "flat" | "tonal");

const store = useCreatePrePlainteStore();
const { userFormData } = storeToRefs(store);
const { captchaToken } = storeToRefs(store);
const data = userFormData as Ref<PrePlainteFormFields>;

const isSubmitting = ref(false);
const submitError = ref("");
const showValidation = ref(false);
const isRdvConflict = ref(false);

const captchaSiteKey = getCaptchaSitekey() || "";
const captchaEnabled = isCaptchaEnabled();
const isSubmitDisabled = computed(() => isSubmitting.value || (captchaEnabled && !captchaToken.value));

const confirmationEmailFixFieldError = ref("");

async function tryCreateEsiriusAppointment(demandeId: string): Promise<{ success: boolean; error: string }> {
  if (!data.value.selectedCreneau) {
    return { success: true, error: "" };
  }
  try {
    const esiriusPayload = buildEsiriusPayload(demandeId, data.value, data.value.selectedCreneau);
    const esiriusResult = await EsiriusService.createAppointment(esiriusPayload);

    if (esiriusResult?.codeRdv) {
      store.setUserFormData({
        ...data.value,
        selectedCreneau: {
          ...data.value.selectedCreneau,
          codeRdv: esiriusResult.codeRdv,
        },
        codeRdv: esiriusResult.codeRdv,
      });
      return { success: true, error: "" };
    }
    isRdvConflict.value = true;
    return { success: false, error: t("submission.erreurRendezVousNonDispo") };
  } catch (err) {
    return {
      success: false,
      error: err instanceof Error ? err.message : t("submission.erreurCreationRendezVous"),
    };
  }
}

const submit = async () => {
  isSubmitting.value = true;
  submitError.value = "";
  confirmationEmailFixFieldError.value = "";
  isRdvConflict.value = false;

  try {
    const codeCheckResult = await validateAndVerifyEmailChallengeCode();
    if (!codeCheckResult.success) {
      return;
    }

    const backendData = await buildPrePlainteForBackend(data.value);
    const demandeId = await submitPrePlainte(backendData);

    if (!demandeId) {
      return;
    }

    await finalizeAppointmentSubmission(demandeId);
  } catch (error) {
    console.error("[PPL submit] Échec de la soumission", error);
    submitError.value = (error as Error).message || t("submission.erreurInconnue");
    store.resetCaptchaToken();
  } finally {
    isSubmitting.value = false;
  }
};

const validateAndVerifyEmailChallengeCode = async (): Promise<{ success: boolean }> => {
  const rawCode = (showEmailChallengeFix.value ? confirmationEmailFix.value : data.value.confirmationEmail) ?? "";
  const codeToCheck = sanitizeEmailChallengeCodeInput(rawCode);

  if (showEmailChallengeFix.value) {
    confirmationEmailFix.value = codeToCheck;
  }

  data.value.confirmationEmail = codeToCheck;

  const codeFormatError = getEmailChallengeCodeValidationMessage(codeToCheck, t);
  if (codeFormatError) {
    submitError.value = codeFormatError;
    confirmationEmailFixFieldError.value = codeFormatError;
    showEmailChallengeFix.value = true;
    return { success: false };
  }

  const verifyChallenge = await verifyEmailChallengeCode(data.value.email, store.keyChallenge ?? "", codeToCheck);

  if (!verifyChallenge.success) {
    submitError.value = getChallengeErrorMessage(verifyChallenge);

    if (isChallengeError(verifyChallenge.status)) {
      showEmailChallengeFix.value = true;
      confirmationEmailFix.value = codeToCheck;
      data.value.confirmationEmail = codeToCheck;
    }

    return { success: false };
  }

  if (showEmailChallengeFix.value) {
    data.value.confirmationEmail = confirmationEmailFix.value;
  }

  return { success: true };
};

const submitPrePlainte = async (backendData: Awaited<ReturnType<typeof buildPrePlainteForBackend>>) => {
  const demandeId = await PrePlainteService.submit(backendData, captchaToken.value);
  store.setDemandeId(demandeId);

  if (!demandeId) {
    submitError.value = t("submission.erreurInconnue");
    store.resetCaptchaToken();
    return "";
  }

  return demandeId;
};

const finalizeAppointmentSubmission = async (demandeId: string) => {
  const { success: esiriusSuccess, error: esiriusError } = await tryCreateEsiriusAppointment(demandeId);

  if (esiriusSuccess) {
    store.setStep(TOTAL_STEPS);
    store.clearPersistedDataAfterSubmission();
    showValidation.value = true;
    return;
  }

  submitError.value = esiriusError || t("submission.erreurCreationRendezVous");
  store.resetCaptchaToken();
};

const goTo = (s: number) => {
  store.setStep(s);
};

const formatCategorie = (val?: string) => {
  const categorie = CATEGORIES_OBJETS.find(cat => cat.value === val);
  return categorie ? t(categorie.labelKey) : val;
};

const formatSousCategorie = (val?: string, categorie?: string) => {
  const sousCategorie = CATEGORIES_OBJETS.find(cat => cat.value === categorie)?.subCategories.find(
    sousCat => sousCat.value === val,
  );
  return sousCategorie ? t(sousCategorie.labelKey) : val;
};

const formatTypeDocumentIdentite = (val?: string) => {
  switch (val) {
    case "carte_identite":
      return t("informationsPersonnelles.carteIdentite");
    case "passeport":
      return t("informationsPersonnelles.passeport");
    case "aucun_document":
      return t("informationsPersonnelles.aucunDocument");
    default:
      return val || "";
  }
};

const formatPostePersonneMorale = (val?: string) => {
  switch (val) {
    case "administratrice_administrateur":
      return t("typePersonneMorale.administratriceAdministrateur");
    case "associee_associe":
      return t("typePersonneMorale.associeeAssocie");
    case "avocate_avocat":
      return t("typePersonneMorale.avocateAvocat");
    case "chargee_affaires_charge_affaires":
      return t("typePersonneMorale.chargeeAffairesChargeAffaires");
    case "chargee_communication_charge_communication":
      return t("typePersonneMorale.chargeeCommunicationChargeCommunication");
    case "chef_projet":
      return t("typePersonneMorale.chefProjet");
    case "collaboratrice_collaborateur":
      return t("typePersonneMorale.collaboratriceCollaborateur");
    case "commercante_commercant":
      return t("typePersonneMorale.commercanteCommercant");
    case "consultante_consultant":
      return t("typePersonneMorale.consultanteConsultant");
    case "directrice_directeur":
      return t("typePersonneMorale.directriceDirecteur");
    case "employee_employe":
      return t("typePersonneMorale.employeeEmploye");
    case "experte_comptable_expert_comptable":
      return t("typePersonneMorale.experteComptableExpertComptable");
    case "fiduciaire":
      return t("typePersonneMorale.fiduciaire");
    case "fondatrice_fondateur":
      return t("typePersonneMorale.fondatriceFondateur");
    case "gerante_gerant":
      return t("typePersonneMorale.geranteGerant");
    case "mandataire":
      return t("typePersonneMorale.mandataire");
    case "membre_comite":
      return t("typePersonneMorale.membreComite");
    case "membre_direction":
      return t("typePersonneMorale.membreDirection");
    case "notaire":
      return t("typePersonneMorale.notaire");
    case "presidente_president":
      return t("typePersonneMorale.presidentePresident");
    case "proprietaire":
      return t("typePersonneMorale.proprietaire");
    case "representante_legale_representant_legal":
      return t("typePersonneMorale.representanteLegaleRepresentantLegal");
    case "responsable_administratif_administrative":
      return t("typePersonneMorale.responsableAdministratifAdministrative");
    case "responsable_service":
      return t("typePersonneMorale.responsableService");
    case "responsable_juridique":
      return t("typePersonneMorale.responsableJuridique");
    case "responsable_rh":
      return t("typePersonneMorale.responsableRh");
    case "responsable_technique":
      return t("typePersonneMorale.responsableTechnique");
    case "secretaire":
      return t("typePersonneMorale.secretaire");
    case "stagiaire":
      return t("typePersonneMorale.stagiaire");
    case "tresoriere_tresorier":
      return t("typePersonneMorale.tresoriereTresorier");
    case "vice_presidente_vice_president":
      return t("typePersonneMorale.vicePresidenteVicePresident");
    default:
      return val || "";
  }
};

const formatTypeRepresentation = (val?: string) => {
  switch (val) {
    case "legal":
      return t("representation.legal");
    case "famille":
      return t("representation.famille");
    case "ami":
      return t("representation.ami");
    case "curateur":
      return t("representation.curateur");
    case "autre":
      return t("representation.autre");
    default:
      return val || "";
  }
};

const formatTypeIncident = (val?: string) => {
  switch (val) {
    case "vol":
      return t("incidentTypes.vol");
    case "degat-delit":
      return t("incidentTypes.degatDelit");
    case "cybercrime":
      return t("incidentTypes.cybercrime");
    default:
      return val || "";
  }
};

const formatTypeDommage = (value?: string) => {
  if (!value) {
    return "";
  }
  switch (value) {
    case "dommage-vehicule":
      return t("typesDommage.dommageVehicule");
    case "dommage-propriete":
      return t("typesDommage.dommagePropriete");
    case "autre":
      return t("typesDommage.autre");
    default:
      return value;
  }
};

const formatNatureDommage = (vals?: string[]) => {
  if (!Array.isArray(vals)) {
    return "";
  }
  return vals
    .map(v => {
      switch (v) {
        case "degradations":
          return t("dommages.degradations");
        case "tags-graffiti":
          return t("dommages.tagsGraffiti");
        case "autre":
          return t("dommages.autre");
        default:
          return v;
      }
    })
    .join(", ");
};

const formatCreneau = () => {
  if (data.value.selectedCreneau) {
    const c = data.value.selectedCreneau;
    return `${c.heureDebut} - ${c.heureFin}`;
  }
  return data.value.creneauPrefere || "";
};

const formatTypeCybercrime = (val?: string) => {
  switch (val) {
    case "commande-frauduleuse":
      return t("cybercrime.commandeFrauduleuse");
    case "achat-non-recu":
      return t("cybercrime.achatNonRecu");
    case "fausse-annonce":
      return t("cybercrime.fausseAnnonce");
    case "rancongiciel":
      return t("cybercrime.rancongiciel");
    case "cyberharcelement":
      return t("cybercrime.cyberharcelement");
    case "autre":
      return t("common.autre");
    default:
      return val || "";
  }
};

const incidentType = computed(() => data.value.typeIncident);

function isFieldVisible(field: string): boolean {
  const incidentKey = INCIDENT_TYPE_MAP[incidentType.value];
  if (!incidentKey) {
    return false;
  }

  return (INCIDENT_FIELDS[incidentKey] as readonly string[]).includes(field);
}

function hasImei(typeObjet: RipolSelection | null): boolean {
  const code = typeObjet?.code;
  if (!code) {
    return false;
  }
  return code.startsWith(RIPOL.PREFIX_TELEPHONE_MOBILE) || code.startsWith(RIPOL.PREFIX_TABLETTE);
}

function getChallengeErrorMessage(result: { status: string; remainingAttempts?: number | null }) {
  switch (result.status) {
    case "INVALID":
      return t("emailChallenge.codeInvalide");
    case "EXPIRED":
      return t("emailChallenge.codeExpire");
    case "LOCKED":
      return t("emailChallenge.tropDeTentatives");
    case "NOT_FOUND":
      return t("emailChallenge.challengeIntrouvable");
    default:
      return t("emailChallenge.erreurVerification");
  }
}

const showEmailChallengeFix = ref(false);
const confirmationEmailFix = ref(sanitizeEmailChallengeCodeInput(data.value.confirmationEmail ?? ""));
const resendInProgress = ref(false);

const onConfirmationEmailFixInput = (cleaned: string) => {
  confirmationEmailFixFieldError.value = "";
  confirmationEmailFix.value = cleaned;
  data.value.confirmationEmail = cleaned;
};

const isChallengeError = (status: string) => ["INVALID", "EXPIRED", "LOCKED", "NOT_FOUND"].includes(status);

const onResendCodeFromRecap = async () => {
  resendInProgress.value = true;
  try {
    const key = store.keyChallenge ?? crypto.randomUUID();
    if (!store.keyChallenge) {
      store.setKeyChallenge(key);
    }
    await requestEmailChallengeCode(data.value.email, key);
  } finally {
    resendInProgress.value = false;
  }
};

const hasAdresseEvenementPrincipale = computed(
  () =>
    !!data.value.adresseEvenement ||
    !!data.value.adressePostaleEvenement ||
    !!data.value.npaEvenement ||
    !!data.value.localiteEvenement ||
    !!data.value.paysEvenement,
);

const hasAdresseEvenementSecondaire = computed(
  () =>
    !!data.value.adresseEvenementSecondaire ||
    !!data.value.adressePostaleEvenementSecondaire ||
    !!data.value.npaEvenementSecondaire ||
    !!data.value.localiteEvenementSecondaire ||
    !!data.value.paysEvenementSecondaire,
);

const isTrajetRenseigne = computed(() => hasAdresseEvenementPrincipale.value && hasAdresseEvenementSecondaire.value);

const isTiers = computed(() => data.value.lienAvecPersonne === "TIERS");
const isEntreprise = computed(() => data.value.lienAvecPersonne === "ENTREPRISE");

const formatLienAvecPersonne = (value?: string) => {
  switch (value) {
    case "MOI_MEME":
      return t("informationsPersonnelles.moiMeme");
    case "TIERS":
      return t("informationsPersonnelles.tiers");
    case "ENTREPRISE":
      return t("informationsPersonnelles.monEntreprise");
    default:
      return value || "";
  }
};
</script>

<style scoped>
:deep(.ge-field-label) {
  white-space: normal !important;
  overflow-wrap: anywhere;
  word-break: break-word;
  font-size: 0.875rem;
  opacity: 1 !important;
  font-weight: 600;
  line-height: 1.25rem;
  display: inline-block;
  max-width: 100%;
  color: rgb(var(--v-theme-on-surface));
}

:deep(dl dt) {
  max-width: 100%;
}

:deep(.ge-field-value) {
  color: rgb(var(--v-theme-on-surface));
}
</style>
