import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi, type AdminVO } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const currentUser = ref<AdminVO | null>(null)
  const isLoggedIn = ref(false)

  async function fetchMe() {
    try {
      const res = await authApi.me()
      currentUser.value = res.data
      isLoggedIn.value = true
    } catch {
      currentUser.value = null
      isLoggedIn.value = false
    }
  }

  function setUser(user: AdminVO) {
    currentUser.value = user
    isLoggedIn.value = true
  }

  function clearUser() {
    currentUser.value = null
    isLoggedIn.value = false
  }

  return { currentUser, isLoggedIn, fetchMe, setUser, clearUser }
})
