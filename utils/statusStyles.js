// Utility for consistent status styling across the application
// Based on the modern design from admin/status.vue

// Helper function to convert hex to rgba
export function hexToRgba(hex, alpha = 0.2) {
  if (!hex || !hex.startsWith('#')) return `rgba(156, 163, 175, ${alpha})`;

  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

// Enhanced function to get dynamic status style with modern glassmorphism effects
export function getEnhancedStatusStyle(status) {
  if (!status?.color) {
    // Fallback style for status without color
    return `
      background: linear-gradient(135deg, rgba(156, 163, 175, 0.15), rgba(156, 163, 175, 0.05));
      color: #9ca3af;
      border: 1px solid rgba(156, 163, 175, 0.3);
      box-shadow: 0 4px 12px rgba(156, 163, 175, 0.15);
    `.replace(/\s+/g, ' ').trim();
  }

  const baseColor = status.color;
  return `
    background: linear-gradient(135deg, ${hexToRgba(baseColor, 0.15)}, ${hexToRgba(baseColor, 0.05)});
    color: ${baseColor};
    border: 1px solid ${hexToRgba(baseColor, 0.3)};
    box-shadow: 0 4px 12px ${hexToRgba(baseColor, 0.15)};
    backdrop-filter: blur(8px);
  `.replace(/\s+/g, ' ').trim();
}

// Generate CSS classes for status badges (for use in templates)
export function getStatusBadgeClasses(status) {
  return 'inline-flex items-center gap-1.5 px-2 py-1 rounded-md font-medium text-xs transition-all duration-300 hover:scale-105 backdrop-blur-sm';
}

// Get status dot style
export function getStatusDotStyle(color) {
  return {
    backgroundColor: color || '#9ca3af',
    width: '6px',
    height: '6px',
    borderRadius: '50%',
    flexShrink: 0
  };
}

// Generate card glow effect style
export function getCardGlowStyle(color) {
  if (!color) return '';

  return `
    background: linear-gradient(135deg, ${hexToRgba(color, 0.05)}, transparent);
  `.replace(/\s+/g, ' ').trim();
}

// Status hover effect for cards
export function getStatusHoverStyle(color) {
  if (!color) return '';

  return `
    box-shadow: 0 8px 25px ${hexToRgba(color, 0.2)};
    border-color: ${hexToRgba(color, 0.4)};
  `.replace(/\s+/g, ' ').trim();
}

// Predefined color palette from admin page
export const statusColorPalette = [
  { name: "Azul", value: "#3b82f6" },
  { name: "Verde", value: "#10b981" },
  { name: "Vermelho", value: "#ef4444" },
  { name: "Amarelo", value: "#f59e0b" },
  { name: "Roxo", value: "#8b5cf6" },
  { name: "Rosa", value: "#ec4899" },
  { name: "Laranja", value: "#f97316" },
  { name: "Ciano", value: "#06b6d4" },
  { name: "Lima", value: "#65a30d" },
  { name: "Índigo", value: "#6366f1" },
];

// Legacy support: Map Tailwind color names to hex values
export const tailwindColorMap = {
  'blue-500': '#3b82f6',
  'green-500': '#10b981',
  'red-500': '#ef4444',
  'yellow-500': '#f59e0b',
  'purple-500': '#8b5cf6',
  'pink-500': '#ec4899',
  'orange-500': '#f97316',
  'cyan-500': '#06b6d4',
  'lime-500': '#65a30d',
  'indigo-500': '#6366f1',
  'emerald-500': '#10b981',
  'amber-500': '#f59e0b',
  'violet-500': '#8b5cf6',
  'fuchsia-500': '#ec4899',
  'teal-500': '#14b8a6',
  'sky-500': '#0ea5e9',
  'rose-500': '#f43f5e',
  'slate-500': '#64748b',
};

// Convert legacy Tailwind classes to hex color
export function getTailwindColorHex(colorClass) {
  return tailwindColorMap[colorClass] || '#9ca3af';
}