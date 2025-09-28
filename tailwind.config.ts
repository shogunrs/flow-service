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
        },
        brand: {
          50: '#eef2ff',
          100: '#e0e7ff',
          200: '#c7d2fe',
          300: '#a5b4fc',
          400: '#818cf8',
          500: '#6366f1',
          600: '#4f46e5',
          700: '#4338ca',
          800: '#3730a3',
          900: '#312e81'
        }
      },
      backdropBlur: {
        xs: '2px'
      },
      boxShadow: {
        glow: '0 0 40px rgba(79,70,229,.35)'
      },
      animation: {
        'spin-slow': 'spin 6s linear infinite',
        'pulse-slow': 'pulse 4s ease-in-out infinite',
        shimmer: 'shimmer 3s linear infinite',
        float: 'float 8s ease-in-out infinite',
        gradient: 'gradient 8s ease infinite'
      },
      keyframes: {
        shimmer: {
          '0%': { backgroundPosition: '-1000px 0' },
          '100%': { backgroundPosition: '1000px 0' }
        },
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-10px)' }
        },
        gradient: {
          '0%': { backgroundPosition: '0% 50%' },
          '50%': { backgroundPosition: '100% 50%' },
          '100%': { backgroundPosition: '0% 50%' }
        }
      }
    }
  },
  plugins: []
}
