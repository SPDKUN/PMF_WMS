import { ref, watchEffect } from 'vue'

const saved = localStorage.getItem('app-theme')
const theme = ref(saved === 'dark' ? 'dark' : 'light')

watchEffect(() => {
  document.documentElement.setAttribute('data-theme', theme.value)
  localStorage.setItem('app-theme', theme.value)
})

export function useTheme() {
  const toggleTheme = () => {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
  }

  const isDark = () => theme.value === 'dark'

  return { theme, toggleTheme, isDark }
}
