import { afterEach, describe, expect, it, vi } from "vitest";
import { defineComponent, nextTick } from "vue";
import { mount } from "@vue/test-utils";
import { useMobileKeyboardInset } from "@/composables/useMobileKeyboardInset";

describe("useMobileKeyboardInset", () => {
  afterEach(() => {
    document.documentElement.style.removeProperty("--vv-keyboard-inset");
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
  });

  it("ne fait rien si visualViewport est absent", async () => {
    vi.stubGlobal("visualViewport", undefined);

    const Comp = defineComponent({
      setup() {
        useMobileKeyboardInset();
        return () => null;
      },
    });

    const wrapper = mount(Comp);
    await nextTick();

    expect(document.documentElement.style.getPropertyValue("--vv-keyboard-inset")).toBe("");
    wrapper.unmount();
  });

  it("met a jour --vv-keyboard-inset selon la hauteur du clavier virtuel", async () => {
    const listeners = new Map<string, EventListener>();
    const visualViewport = {
      height: 500,
      offsetTop: 0,
      addEventListener: vi.fn((event: string, handler: EventListener) => {
        listeners.set(event, handler);
      }),
      removeEventListener: vi.fn(),
    };

    Object.defineProperty(window, "innerHeight", {
      configurable: true,
      value: 800,
    });
    vi.stubGlobal("visualViewport", visualViewport);

    const Comp = defineComponent({
      setup() {
        useMobileKeyboardInset();
        return () => null;
      },
    });

    const wrapper = mount(Comp);
    await nextTick();

    expect(document.documentElement.style.getPropertyValue("--vv-keyboard-inset")).toBe("300px");
    expect(visualViewport.addEventListener).toHaveBeenCalledWith("resize", expect.any(Function));
    expect(visualViewport.addEventListener).toHaveBeenCalledWith("scroll", expect.any(Function));

    visualViewport.height = 700;
    listeners.get("resize")?.(new Event("resize"));
    expect(document.documentElement.style.getPropertyValue("--vv-keyboard-inset")).toBe("100px");

    wrapper.unmount();
    await nextTick();

    expect(visualViewport.removeEventListener).toHaveBeenCalled();
    expect(document.documentElement.style.getPropertyValue("--vv-keyboard-inset")).toBe("");
  });
});
