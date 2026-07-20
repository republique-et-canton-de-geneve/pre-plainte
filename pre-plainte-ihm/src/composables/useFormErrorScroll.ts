import { nextTick } from "vue";
import { SCROLL_CONFIG, CONDITIONAL_FIELD_PATTERNS } from "@/constants/constant";

const ERROR_SELECTORS = [
  ".v-messages__message",
  ".text-error",
  ".v-field--error",
  ".v-input--error",
  ".v-select--error",
  ".v-textarea--error",
  ".v-checkbox--error",
  ".v-radio-group--error",
];

const FIELD_CONTAINER_SELECTOR =
  ".v-field, .v-input, .v-select, .v-textarea, .v-checkbox, .v-radio-group, fieldset[data-field]";

const STICKY_SCROLL_MARGIN_TOP_PX = 160;

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

const isInsideFormErrorSummary = (element: Element): boolean =>
  Boolean(element.closest('[data-cy="form-error-summary"]'));

const resolveScrollTarget = (element: Element): Element => {
  if (element.matches(FIELD_CONTAINER_SELECTOR)) {
    return element;
  }

  return element.closest(FIELD_CONTAINER_SELECTOR) ?? element;
};

const findFirstVisibleError = (): Element | null => {
  for (const selector of ERROR_SELECTORS) {
    const elements = document.querySelectorAll(selector);

    for (const element of Array.from(elements)) {
      if (isInsideFormErrorSummary(element)) {
        continue;
      }
      if (element.textContent?.trim()) {
        return element;
      }
    }
  }

  return null;
};

type FormInputElement = HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement;

const focusInput = (target: Element) => {
  const input = (
    target instanceof HTMLInputElement ||
    target instanceof HTMLTextAreaElement ||
    target instanceof HTMLSelectElement
      ? target
      : target.querySelector("input, textarea, select, [tabindex]:not([tabindex='-1'])")
  ) as FormInputElement | HTMLElement | null;

  if (!input || !(input instanceof HTMLElement)) {
    return;
  }

  setTimeout(() => {
    input.focus({ preventScroll: true });
  }, SCROLL_CONFIG.FOCUS_DELAY);
};

const scrollToError = (errorElement: Element) => {
  const target = resolveScrollTarget(errorElement);

  if (target instanceof HTMLElement) {
    target.style.scrollMarginTop = `${STICKY_SCROLL_MARGIN_TOP_PX}px`;
  }

  target.scrollIntoView({
    behavior: "smooth",
    block: "start",
    inline: "nearest",
  });

  focusInput(target);
};

const scrollToFirstRequiredField = () => {
  const requiredFields = document.querySelectorAll(
    "input[required], select[required], textarea[required]",
  );

  for (const field of Array.from(requiredFields)) {
    const input = field as
      | HTMLInputElement
      | HTMLSelectElement
      | HTMLTextAreaElement;

    if (input.value?.trim()) {
      continue;
    }

    scrollToError(input);
    return;
  }
};

const escapeSelectorValue = (value: string): string => {
  if (typeof CSS !== "undefined" && typeof CSS.escape === "function") {
    return CSS.escape(value);
  }
  return value.replaceAll(/["\\]/g, "\\$&");
};

const findFieldElement = (field: string): Element | null => {
  if (!field) {
    return null;
  }

  const escaped = escapeSelectorValue(field);
  const selectors = [
    `[data-field="${escaped}"]`,
    `[name="${escaped}"]`,
    `#${escaped}`,
    `input[name="${escaped}"]`,
    `select[name="${escaped}"]`,
    `textarea[name="${escaped}"]`,
  ];

  for (const selector of selectors) {
    try {
      const element = document.querySelector(selector);
      if (element && !isInsideFormErrorSummary(element)) {
        return element;
      }
    } catch {
      // Sélecteur invalide pour certains chemins de champs
    }
  }

  return null;
};

const findElementFromErrorMessage = (message: string): Element | null => {
  const errorElements = document.querySelectorAll(
    ".v-messages__message, .text-error, .v-field--error, .v-input--error, .v-radio-group--error",
  );

  for (const errorElement of Array.from(errorElements)) {
    if (isInsideFormErrorSummary(errorElement)) {
      continue;
    }
    if (errorElement.textContent?.includes(message)) {
      return resolveScrollTarget(errorElement);
    }
  }

  return null;
};

export function useFormErrorScroll() {
  const FORM_ERROR_SUMMARY_SELECTOR = '[data-cy="form-error-summary"]';

  const scrollToFormErrorSummary = async () => {
    await nextTick();

    const summary = document.querySelector(FORM_ERROR_SUMMARY_SELECTOR);
    if (!summary) {
      return;
    }

    if (summary instanceof HTMLElement) {
      summary.style.scrollMarginTop = `${STICKY_SCROLL_MARGIN_TOP_PX}px`;
    }

    summary.scrollIntoView({
      behavior: "smooth",
      block: "start",
      inline: "nearest",
    });

    if (summary instanceof HTMLElement) {
      summary.setAttribute("tabindex", "-1");
      summary.focus({ preventScroll: true });
    }
  };

  const scrollToFirstVisibleError = async () => {
    await nextTick();

    const firstError = findFirstVisibleError();

    if (firstError) {
      scrollToError(firstError);
      return;
    }

    scrollToFirstRequiredField();
  };

  const scrollToTopOnConditionalErrors = async (errors: any) => {
    await scrollToFormErrorSummary();

    const validationErrors = extractValidationErrors(errors);
    if (Object.keys(validationErrors).length === 0) {
      return;
    }

    const hasConditionalError = Object.keys(validationErrors).some(fieldName =>
      isConditionalField(fieldName),
    );

    if (!hasConditionalError) {
      return;
    }

    setTimeout(async () => {
      await scrollToFirstValidationError(errors);
    }, SCROLL_CONFIG.CONDITIONAL_SCROLL_DELAY);
  };

  const scrollToFirstValidationError = async (errors: any) => {
    await nextTick();

    const validationErrors = extractValidationErrors(errors);

    if (Object.keys(validationErrors).length === 0) {
      return;
    }

    const firstErrorField = Object.keys(validationErrors).find(
      field => validationErrors[field],
    );

    if (!firstErrorField) {
      return;
    }

    let element = findFieldElement(firstErrorField);

    if (!element) {
      const message = validationErrors[firstErrorField];

      if (typeof message === "string") {
        element = findElementFromErrorMessage(message);
      }
    }

    if (!element) {
      await scrollToFirstVisibleError();
      return;
    }

    scrollToError(element);
  };

  const scrollToValidationError = async (path: string, message: string) => {
    await nextTick();

    const rootPath = path.split(".")[0] ?? path;
    let element = rootPath ? findFieldElement(rootPath) : null;

    if (!element && path && path !== rootPath) {
      element = findFieldElement(path);
    }

    if (!element && message) {
      element = findElementFromErrorMessage(message);
    }

    if (!element) {
      await scrollToFirstVisibleError();
      return;
    }

    scrollToError(element);
  };

  return {
    scrollToFormErrorSummary,
    scrollToFirstVisibleError,
    scrollToFirstValidationError,
    scrollToTopOnConditionalErrors,
    scrollToValidationError,
  };
}
