import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { useFormErrorScroll } from "@/composables/useFormErrorScroll";

describe("useFormErrorScroll", () => {
  beforeEach(() => {
    document.body.innerHTML = "";
    Element.prototype.scrollIntoView = vi.fn();
  });

  afterEach(() => {
    document.body.innerHTML = "";
    vi.restoreAllMocks();
  });

  it("scroll vers le resume d'erreurs du formulaire", async () => {
    document.body.innerHTML = `<div data-cy="form-error-summary">Resume</div>`;
    const summary = document.querySelector('[data-cy="form-error-summary"]') as HTMLElement;
    const focusSpy = vi.spyOn(summary, "focus").mockImplementation(() => undefined);

    const { scrollToFormErrorSummary } = useFormErrorScroll();
    await scrollToFormErrorSummary();

    expect(summary.scrollIntoView).toHaveBeenCalledWith({
      behavior: "smooth",
      block: "start",
      inline: "nearest",
    });
    expect(focusSpy).toHaveBeenCalledWith({ preventScroll: true });
  });

  it("scroll vers le champ cible via data-field sans cibler le resume", async () => {
    vi.useFakeTimers();
    document.body.innerHTML = `
      <div data-cy="form-error-summary">
        <div class="v-messages__message">Erreur resume</div>
      </div>
      <fieldset data-field="volDansVehicule">
        <input name="volDansVehicule" />
        <div class="v-messages__message">Veuillez indiquer si l'objet a ete vole dans ou sur un vehicule</div>
      </fieldset>
    `;

    const fieldset = document.querySelector('fieldset[data-field="volDansVehicule"]') as HTMLElement;
    const input = document.querySelector('input[name="volDansVehicule"]') as HTMLElement;
    const focusSpy = vi.spyOn(input, "focus").mockImplementation(() => undefined);

    const { scrollToValidationError } = useFormErrorScroll();
    await scrollToValidationError(
      "volDansVehicule",
      "Veuillez indiquer si l'objet a ete vole dans ou sur un vehicule",
    );
    await vi.runAllTimersAsync();

    expect(fieldset.scrollIntoView).toHaveBeenCalled();
    expect(focusSpy).toHaveBeenCalledWith({ preventScroll: true });
    vi.useRealTimers();
  });

  it("utilise le premier champ en erreur quand le chemin n'est pas trouve", async () => {
    document.body.innerHTML = `
      <div class="v-input--error">
        <input />
        <div class="v-messages__message">Le type de lieu est requis</div>
      </div>
    `;

    const message = document.querySelector(".v-messages__message") as HTMLElement;

    const { scrollToFirstValidationError } = useFormErrorScroll();
    await scrollToFirstValidationError({
      typeLieu: "Le type de lieu est requis",
    });

    expect(message.closest(".v-input--error")?.scrollIntoView).toHaveBeenCalled();
  });
});
