import { createRouter, createWebHistory } from "vue-router";
import DeclarerPlainteView from "@/pages/DeclarerPrePlainteView.vue";
import AnnulerPrePlainteView from "@/pages/AnnulerPrePlainteView.vue";
import ModifierRdvView from "@/pages/ModifierRdvPrePlainteView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "declarer-pre-plainte",
      component: DeclarerPlainteView,
    },
    {
      path: "/annuler-pre-plainte",
      name: "annuler-pre-plainte",
      component: AnnulerPrePlainteView,
    },
    {
      path: "/modifier-rdv-pre-plainte",
      name: "modifier-rdv",
      component: ModifierRdvView,
    },
  ],
});

export default router;
