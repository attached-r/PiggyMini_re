// 主题管理 Composable
import { ref, watch, onMounted } from 'vue'
import { useLocalStorage } from '@vueuse/core'

// 导入所有主题 CSS
import '@/assets/styles/themes/light.css'
import '@/assets/styles/themes/dark.css'
import '@/assets/styles/themes/black.css'
import '@/assets/styles/themes/pixel.css'
import '@/assets/styles/themes/nietzsche.css'

export type ThemeType = 'light' | 'dark' | 'black' | 'pixel' | 'nietzsche'

export interface ThemeInfo {
  id: ThemeType
  name: string
  icon: string
  description: string
}

// 主题配置
export const themes: Record<ThemeType, ThemeInfo> = {
  light: {
    id: 'light',
    name: '亮色主题',
    icon: '☀️',
    description: '清新的白色界面，适合白天使用'
  },
  dark: {
    id: 'dark',
    name: '暗色主题',
    icon: '🌙',
    description: '柔和的深灰色，呵护你的眼睛'
  },
  black: {
    id: 'black',
    name: '纯黑主题',
    icon: '⚫',
    description: '纯黑背景，更加护眼省电'
  },
  pixel: {
    id: 'pixel',
    name: '像素风格',
    icon: '🎮',
    description: '复古8-bit游戏风格'
  },
  nietzsche: {
    id: 'nietzsche',
    name: '尼采风格',
    icon: '📜',
    description: '古典优雅的羊皮纸风格'
  }
}

// 获取主题列表
export const getThemeList = (): ThemeInfo[] => {
  return Object.values(themes)
}

// 获取默认主题
const getDefaultTheme = (): ThemeType => {
  return 'light'
}

// 主题管理
export function useTheme() {
  // 使用 localStorage 持久化主题
  const currentTheme = useLocalStorage<ThemeType>('piggy_theme', getDefaultTheme())

  // 主题切换
  const setTheme = (theme: ThemeType) => {
    if (!themes[theme]) {
      console.warn(`主题 "${theme}" 不存在，使用默认主题`)
      theme = getDefaultTheme()
    }
    currentTheme.value = theme
    applyTheme(theme)
  }

  // 应用主题到 DOM
  const applyTheme = (theme: ThemeType) => {
    // 找到根元素（App.vue 中的 div）
    const root = document.querySelector('.theme-light, .theme-dark, .theme-black, .theme-pixel, .theme-nietzsche')
    
    if (root) {
      // 移除所有主题类名
      Object.keys(themes).forEach(t => {
        root.classList.remove(`theme-${t}`)
      })
      // 添加新的主题类名
      root.classList.add(`theme-${theme}`)
    }
    
    // 同时也给 #app 添加类（兼容旧代码）
    const app = document.getElementById('app')
    if (app) {
      Object.keys(themes).forEach(t => {
        app.classList.remove(`theme-${t}`)
      })
      app.classList.add(`theme-${theme}`)
    }

    // 更新 meta theme-color
    updateMetaThemeColor(theme)
  }

  // 更新 meta theme-color
  const updateMetaThemeColor = (theme: ThemeType) => {
    const metaThemeColor = document.querySelector('meta[name="theme-color"]')

    const colors: Record<ThemeType, string> = {
      light: '#f8fafc',
      dark: '#0f172a',
      black: '#0a0a0a',
      pixel: '#1a1a2e',
      nietzsche: '#2c1810'
    }

    if (metaThemeColor) {
      metaThemeColor.setAttribute('content', colors[theme])
    }
  }

  // 初始化主题
  const initTheme = () => {
    applyTheme(currentTheme.value)
  }

  // 获取当前主题信息
  const getCurrentThemeInfo = (): ThemeInfo => {
    return themes[currentTheme.value]
  }

  // 获取下一个主题（用于切换）
  const getNextTheme = (): ThemeType => {
    const themeList = Object.keys(themes) as ThemeType[]
    const currentIndex = themeList.indexOf(currentTheme.value)
    const nextIndex = (currentIndex + 1) % themeList.length
    return themeList[nextIndex]
  }

  // 循环切换主题
  const cycleTheme = () => {
    const nextTheme = getNextTheme()
    setTheme(nextTheme)
  }

  return {
    currentTheme,
    setTheme,
    getThemeList,
    getCurrentThemeInfo,
    getNextTheme,
    cycleTheme,
    initTheme
  }
}
