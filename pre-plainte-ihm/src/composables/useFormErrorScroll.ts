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

const findFirstVisibleError = (): Element | null => {
  for (const selector of ERROR_SELECTORS) {
    const elements = document.querySelectorAll(selector);

    for (const element of Array.from(elements)) {
      if (element.textContent?.trim()) {
        return element;
      }
    }
  }

  return null;
};

type FormInputElement = | HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement;

const focusInput = (input: FormInputElement | null) => {
  if (!input) {
    return;
  }

  setTimeout(() => {
    input.focus();
  }, SCROLL_CONFIG.FOCUS_DELAY);
};

const scrollToError = (errorElement: Element) => {
  const formElement = errorElement.closest(
    ".v-field, .v-input, .v-select, .v-textarea, .v-checkbox, .v-radio-group, .v-form",
  );

  if (!formElement) {
    return;
  }

  const conditionalSection = formElement.closest(".inputs-container, .inputs-fields");

  (conditionalSection ?? formElement).scrollIntoView({
    behavior: "smooth",
    block: conditionalSection ? "start" : "center",
    inline: "nearest",
  });

  const input = formElement.querySelector(
    "input, textarea, select",
  ) as HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement | null;

  focusInput(input);
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

    input.scrollIntoView({
      behavior: "smooth",
      block: "center",
      inline: "nearest",
    });

    focusInput(input);
    return;
  }
};

const findFieldElement = (field: string): Element | null => {
  const selectors = [
    `[name="${field}"]`,
    `#${field}`,
    `[data-field="${field}"]`,
    `input[name="${field}"]`,
    `select[name="${field}"]`,
    `textarea[name="${field}"]`,
  ];

  for (const selector of selectors) {
    const element = document.querySelector(selector);

    if (element) {
      return element;
    }
  }

  return null;
};

const findElementFromErrorMessage = (message: string): Element | null => {
  const errorElements = document.querySelectorAll(
    ".v-messages__message, .text-error, .v-field--error",
  );

  for (const errorElement of Array.from(errorElements)) {
    if (errorElement.textContent?.includes(message)) {
      return errorElement.closest(
        ".v-field, .v-input, .v-select, .v-textarea, .v-checkbox, .v-radio-group",
      );
    }
  }

  return null;
};

export function useFormErrorScroll() {
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

  return {
    scrollToFirstVisibleError,
    scrollToFirstValidationError,
    scrollToTopOnConditionalErrors,
  };
}
