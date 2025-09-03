export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<any[]>([])

  const addNotification = (notification: any) => {
    notifications.value.push(notification)
  }

  const removeNotification = (id: string) => {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  return {
    notifications,
    addNotification,
    removeNotification
  }
})