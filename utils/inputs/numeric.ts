export type NumericOptions = {
  allowDecimal?: boolean;
  decimalSeparator?: "." | ",";
  allowNegative?: boolean;
  maxLength?: number;
};

type Cleanup = () => void;

/**
 * Attach numeric-only behavior to a text input element.
 * - Blocks non-numeric keys
 * - Handles paste/input sanitization
 * - Prevents wheel/arrow key stepping behavior
 */
export function attachNumericInput(
  el: HTMLInputElement,
  opts: NumericOptions = {}
): Cleanup {
  if (!el) return () => {};

  const options: Required<NumericOptions> = {
    allowDecimal: !!opts.allowDecimal,
    decimalSeparator: (opts.decimalSeparator || ".") as "." | ",",
    allowNegative: !!opts.allowNegative,
    maxLength: opts.maxLength || 0,
  };

  const isCtrlKey = (e: KeyboardEvent) => e.ctrlKey || e.metaKey;

  const sanitize = (raw: string): string => {
    let s = raw || "";
    // Replace any non-digit except decimal/negative (as configured)
    const dec = options.allowDecimal ? options.decimalSeparator : "";
    const neg = options.allowNegative ? "-" : "";
    const allowed = new RegExp(`[^0-9${dec ? "\\" + dec : ""}${neg}]`, "g");
    s = s.replace(allowed, "");

    // Enforce single negative at start if allowed
    if (options.allowNegative) {
      const negCount = (s.match(/-/g) || []).length;
      s = s.replace(/-/g, "");
      if (raw.trim().startsWith("-") && negCount > 0) s = "-" + s;
    }

    // Enforce single decimal separator if allowed
    if (options.allowDecimal && dec) {
      const parts = s.split(dec);
      if (parts.length > 1) s = parts.shift() + dec + parts.join("");
    } else {
      // strip any decimals if not allowed
      s = s.replace(/[\.,]/g, "");
    }

    // Enforce max length (digits only not counting sign/decimal)
    if (options.maxLength > 0) {
      const sign = s.startsWith("-") ? "-" : "";
      const parts =
        options.allowDecimal && dec
          ? s.replace("-", "").split(dec)
          : [s.replace("-", ""), ""];
      const intPart = parts[0] || "";
      const fracPart = parts[1] || "";
      const digits = (intPart + (fracPart || "")).slice(0, options.maxLength);
      if (options.allowDecimal && dec) {
        const keepFrac = fracPart ? digits.slice(intPart.length) : "";
        s =
          sign +
          digits.slice(0, intPart.length) +
          (keepFrac ? dec + keepFrac : "");
      } else {
        s = sign + digits;
      }
    }

    return s;
  };

  const onKeyDown = (e: KeyboardEvent) => {
    const k = e.key;
    // allow control/navigation keys
    const allowedKeys = [
      "Backspace",
      "Delete",
      "Tab",
      "Escape",
      "Enter",
      "Home",
      "End",
      "ArrowLeft",
      "ArrowRight",
    ];
    if (allowedKeys.includes(k)) return;
    if (isCtrlKey(e) && (k === "a" || k === "c" || k === "v" || k === "x"))
      return;

    // digits
    if (/^[0-9]$/.test(k)) return;

    // decimal
    if (options.allowDecimal && k === options.decimalSeparator) {
      if (el.value.includes(options.decimalSeparator)) e.preventDefault();
      return;
    }

    // negative sign only at start
    if (options.allowNegative && k === "-") {
      const { selectionStart } = el;
      if (selectionStart !== 0 || el.value.startsWith("-")) e.preventDefault();
      return;
    }

    // Block everything else
    e.preventDefault();
  };

  const onInput = () => {
    const cur = el.value;
    const next = sanitize(cur);
    if (next !== cur) {
      const pos = el.selectionStart || 0;
      el.value = next;
      try {
        el.setSelectionRange(pos, pos);
      } catch (_) {}
      el.dispatchEvent(new Event("input", { bubbles: true }));
    }
  };

  const onPaste = (e: ClipboardEvent) => {
    const data = e.clipboardData?.getData("text") || "";
    const clean = sanitize(data);
    if (clean !== data) {
      e.preventDefault();
      const start = el.selectionStart || 0;
      const end = el.selectionEnd || 0;
      const value = el.value;
      el.value = value.slice(0, start) + clean + value.slice(end);
      const caret = start + clean.length;
      try {
        el.setSelectionRange(caret, caret);
      } catch (_) {}
      el.dispatchEvent(new Event("input", { bubbles: true }));
    }
  };

  const onWheel = (e: WheelEvent) => {
    // Prevent accidental value change on scroll
    e.preventDefault();
  };

  // Attach listeners
  el.addEventListener("keydown", onKeyDown);
  el.addEventListener("input", onInput);
  el.addEventListener("paste", onPaste);
  el.addEventListener("wheel", onWheel, { passive: false });

  return () => {
    el.removeEventListener("keydown", onKeyDown);
    el.removeEventListener("input", onInput);
    el.removeEventListener("paste", onPaste);
    el.removeEventListener("wheel", onWheel as any);
  };
}
