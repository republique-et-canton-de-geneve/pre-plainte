const RGB_WHITE = "rgb(255, 255, 255)";
const RGB_SURFACE = "rgb(252, 252, 255)";
const RGB_ON_SURFACE = "rgb(26, 28, 30)";

const geThemeVariables = {
  "--md-sys-color-primary": "rgb(1, 98, 157)",
  "--md-sys-color-on-primary": RGB_WHITE,
  "--md-sys-color-primary-container": "rgb(214, 228, 255)",
  "--md-sys-color-on-primary-container": "rgb(0, 29, 53)",
  "--md-sys-color-secondary": "rgb(82, 95, 113)",
  "--md-sys-color-on-secondary": RGB_WHITE,
  "--md-sys-color-secondary-container": "rgb(214, 227, 248)",
  "--md-sys-color-on-secondary-container": "rgb(15, 28, 43)",
  "--md-sys-color-tertiary": "rgb(107, 87, 118)",
  "--md-sys-color-on-tertiary": RGB_WHITE,
  "--md-sys-color-tertiary-container": "rgb(243, 218, 255)",
  "--md-sys-color-on-tertiary-container": "rgb(37, 20, 48)",
  "--md-sys-color-error": "rgb(186, 26, 26)",
  "--md-sys-color-on-error": RGB_WHITE,
  "--md-sys-color-error-container": "rgb(255, 218, 214)",
  "--md-sys-color-on-error-container": "rgb(65, 0, 2)",
  "--md-sys-color-background": RGB_SURFACE,
  "--md-sys-color-on-background": RGB_ON_SURFACE,
  "--md-sys-color-surface": RGB_SURFACE,
  "--md-sys-color-on-surface": RGB_ON_SURFACE,
  "--md-sys-color-surface-variant": "rgb(222, 226, 235)",
  "--md-sys-color-on-surface-variant": "rgb(66, 71, 78)",
  "--md-sys-color-surface-bright": RGB_SURFACE,
  "--md-sys-color-surface-container": "rgb(240, 244, 249)",
  "--md-sys-color-surface-container-low": "rgb(246, 250, 255)",
  "--md-sys-color-surface-container-lowest": RGB_WHITE,
  "--md-sys-color-surface-container-high": "rgb(234, 238, 244)",
  "--md-sys-color-surface-container-highest": "rgb(228, 233, 239)",
  "--md-sys-color-outline": "rgb(114, 119, 127)",
  "--md-sys-color-inverse-surface": "rgb(47, 48, 51)",
  "--md-sys-color-inverse-on-surface": "rgb(241, 240, 244)",
  "--md-sys-color-inverse-primary": "rgb(165, 201, 255)",
  "--md-sys-color-success": "rgb(0, 104, 55)",
  "--md-sys-color-on-success": RGB_WHITE,
  "--md-sys-color-success-container": "rgb(137, 245, 177)",
  "--md-sys-color-on-success-container": "rgb(0, 33, 14)",
  "--md-sys-color-warning": "rgb(121, 87, 0)",
  "--md-sys-color-on-warning": RGB_WHITE,
  "--md-sys-color-warning-container": "rgb(255, 223, 153)",
  "--md-sys-color-on-warning-container": "rgb(38, 25, 0)",
  "--md-sys-shape-corner-extra-small": "4px",
  "--md-sys-shape-corner-small": "8px",
  "--md-sys-shape-corner-medium": "12px",
  "--md-sys-shape-corner-large": "16px",
  "--md-sys-shape-corner-extra-large": "28px",
  "--md-sys-shape-corner-full": "9999px",
  "--md-sys-shape-corner-none": "0px",
};

export const applyGeThemeVariables = (win) => {
  for (const [name, value] of Object.entries(geThemeVariables)) {
    win.document.documentElement.style.setProperty(name, value);
  }
};
