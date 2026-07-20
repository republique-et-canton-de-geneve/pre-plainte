import { onMounted, onUnmounted } from "vue";

const KEYBOARD_INSET_CSS_VAR = "--vv-keyboard-inset";

export function useMobileKeyboardInset() {
  onMounted(() => {
    const visualViewport = window.visualViewport;
    if (!visualViewport) {
      return;
    }

    const updateInset = () => {
      const inset = Math.max(0, window.innerHeight - visualViewport.height - visualViewport.offsetTop);
      document.documentElement.style.setProperty(KEYBOARD_INSET_CSS_VAR, `${Math.round(inset)}px`);
    };

    visualViewport.addEventListener("resize", updateInset);
    visualViewport.addEventListener("scroll", updateInset);
    window.addEventListener("orientationchange", updateInset);
    updateInset();

    onUnmounted(() => {
      visualViewport.removeEventListener("resize", updateInset);
      visualViewport.removeEventListener("scroll", updateInset);
      window.removeEventListener("orientationchange", updateInset);
      document.documentElement.style.removeProperty(KEYBOARD_INSET_CSS_VAR);
    });
  });
}
