import type { Config } from 'tailwindcss'

// Custom palette inspired by the provided design
// Strategy: remap Tailwind's orange scale to a blue accent so
// existing classes like `bg-orange-500` adopt the new style globally.
export default <Partial<Config>>{
  content: [
    './components/**/*.{vue,js,ts}',
    './layouts/**/*.vue',
    './pages/**/*.vue',
    './app.vue',
    './plugins/**/*.{js,ts}'
  ],
  theme: {
    extend: {
      colors: {
        // Blue accent (replacing orange scale usage)
        orange: {
          50:  '#eff6ff',  // blue-50
          100: '#dbeafe',  // blue-100
          200: '#bfdbfe',  // blue-200
          300: '#93c5fd',  // blue-300
          400: '#60a5fa',  // blue-400
          500: '#3b82f6',  // blue-500 (primary)
          600: '#2563eb',  // blue-600 (hover)
          700: '#1d4ed8',  // blue-700
          800: '#1e40af',  // blue-800
          900: '#1e3a8a',  // blue-900
          950: '#172554'
        },
        // Electric cyan for secondary highlights if needed
        electric: {
          400: '#3de8ff',
          500: '#00e5ff',
          600: '#00c2e0'
        },
        // Deep navy backgrounds (optional utilities like bg-navy-900)
        navy: {
          900: '#0b1220',
          950: '#070d18'
        }
      }
    }
  },
  plugins: []
}
