import { createInfosPersonnellesSchema } from "./infos-personnelles.schema.ts";
import { createInfosTiersSchema } from "./infos-tiers.schema.ts";
import { createEvenementInfoSchema } from "./incident-evenement.schema.ts";
import { rendezvousInfoSchema } from "./rdv-schema";

const personalShape = (createInfosPersonnellesSchema as any)._def.schema;
const tiersShape = (createInfosTiersSchema as any)._def.schema;
const eventShape = (createEvenementInfoSchema as any)._def.schema;
const rendezvousShape = (rendezvousInfoSchema as any)._def.schema;

export const fullPrePlainteSchema = personalShape.merge(tiersShape).merge(eventShape).merge(rendezvousShape);
