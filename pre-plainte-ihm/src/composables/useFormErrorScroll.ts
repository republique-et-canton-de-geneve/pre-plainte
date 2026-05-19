import { nextTick } from "vue";
import { SCROLL_CONFIG, CONDITIONAL_FIELD_PATTERNS } from "@/constants/constant";

const extractValidationErrors = (errors: any): Record<string, any> => {
  if (!errors || typeof errors !== "object") {
    return {};
  }

  if (errors.errors && typeof errors.errors === "object") {
    return errors.errors;
  }

  if (Object.keys(errors).some(key => typeof errors[key] === "string" && errors[key].length > 0)) {
    return errors;
  }

  return {};
};

const isConditionalField = (fieldName: string): boolean => {
  const lowerFieldName = fieldName.toLowerCase();
  return CONDITIONAL_FIELD_PATTERNS.some(pattern => lowerFieldName.includes(pattern));
};

export function useFormErrorScroll() {
  const scrollToFirstVisibleError = async () => {
    await nextTick();

    const errorSelectors = [
      ".v-messages__message",
      ".text-error",
      ".v-field--error",
      ".v-input--error",
      ".v-select--error",
      ".v-textarea--error",
      ".v-checkbox--error",
      ".v-radio-group--error",
    ];

    let firstErrorElement: Element | null = null;

    for (const selector of errorSelectors) {
      const elements = document.querySelectorAll(selector);

      for (const element of Array.from(elements)) {
        if (element.textContent?.trim()) {
          firstErrorElement = element;
          break;
        }
      }
      if (firstErrorElement) {
        break;
      }
    }

    if (firstErrorElement) {
      const formElement = firstErrorElement.closest(
        ".v-field, .v-input, .v-select, .v-textarea, .v-checkbox, .v-radio-group, .v-form",
      );

      if (formElement) {
        const conditionalSection = formElement.closest(".inputs-container, .inputs-fields");
        if (conditionalSection) {
          conditionalSection.scrollIntoView({
            behavior: "smooth",
            block: "start",
            inline: "nearest",
          });
        } else {
          formElement.scrollIntoView({
            behavior: "smooth",
            block: "center",
            inline: "nearest",
          });
        }

        const inputElement = formElement.querySelector("input, textarea, select") as HTMLElement;
        if (inputElement && typeof inputElement.focus === "function") {
          setTimeout(() => {
            inputElement.focus();
          }, SCROLL_CONFIG.FOCUS_DELAY);
        }
      }
    } else {
      const requiredFields = document.querySelectorAll("input[required], select[required], textarea[required]");

      for (const field of Array.from(requiredFields)) {
        const input = field as HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement;
        if (!input.value || input.value.trim() === "") {
          input.scrollIntoView({
            behavior: "smooth",
            block: "center",
            inline: "nearest",
          });
          setTimeout(() => {
            input.focus();
          }, 300);
          break;
        }
      }
    }
  };

  const scrollToTopOnConditionalErrors = async (errors: any) => {
    await nextTick();

    const validationErrors = extractValidationErrors(errors);

    if (Object.keys(validationErrors).length === 0) {
      return;
    }

    const conditionalErrorFields = Object.keys(validationErrors).filter(fieldName => isConditionalField(fieldName));

    if (conditionalErrorFields.length > 0) {
      const form = document.querySelector("form, .v-form, .v-card");
      if (form) {
        form.scrollIntoView({
          behavior: "smooth",
          block: "start",
          inline: "nearest",
        });

        setTimeout(async () => {
          await scrollToFirstValidationError(errors);
        }, SCROLL_CONFIG.CONDITIONAL_SCROLL_DELAY);
      }
    } else {
      await scrollToFirstValidationError(errors);
    }
  };

  const scrollToFirstValidationError = async (errors: any) => {
    await nextTick();

    const validationErrors = extractValidationErrors(errors);

    if (Object.keys(validationErrors).length === 0) {
      return;
    }

    const firstErrorField = Object.keys(validationErrors).find(field => validationErrors[field]);

    if (firstErrorField) {
      let element =
        document.querySelector(`[name="${firstErrorField}"]`) ||
        document.querySelector(`#${firstErrorField}`) ||
        document.querySelector(`[data-field="${firstErrorField}"]`) ||
        document.querySelector(`input[name="${firstErrorField}"]`) ||
        document.querySelector(`select[name="${firstErrorField}"]`) ||
        document.querySelector(`textarea[name="${firstErrorField}"]`);

      if (!element) {
        const errorMessage = validationErrors[firstErrorField];

        if (typeof errorMessage === "string") {
          const errorElements = document.querySelectorAll(".v-messages__message, .text-error, .v-field--error");

          for (const errorEl of Array.from(errorElements)) {
            if (errorEl.textContent?.includes(errorMessage)) {
              element = errorEl.closest(".v-field, .v-input, .v-select, .v-textarea, .v-checkbox, .v-radio-group");
              break;
            }
          }
        }
      }

      if (element) {
        const conditionalSection = element.closest(".inputs-container, .inputs-fields");
        if (conditionalSection) {
          conditionalSection.scrollIntoView({
            behavior: "smooth",
            block: "start",
            inline: "nearest",
          });
        } else {
          element.scrollIntoView({
            behavior: "smooth",
            block: "center",
            inline: "nearest",
          });
        }

        const inputElement = element.querySelector("input, textarea, select") as HTMLElement;
        if (inputElement && typeof inputElement.focus === "function") {
          setTimeout(() => {
            inputElement.focus();
          }, SCROLL_CONFIG.FOCUS_DELAY);
        }
      } else {
        await scrollToFirstVisibleError();
      }
    }
  };

  return {
    scrollToFirstVisibleError,
    scrollToFirstValidationError,
    scrollToTopOnConditionalErrors,
  };
}
