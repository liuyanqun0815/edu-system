<template>
  <div class="layout">
    <AppHeader />
    <div class="container">
      <SidebarMenu />
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import AppHeader from '@/components/AppHeader.vue'
import SidebarMenu from '@/components/SidebarMenu.vue'

const userStore = useUserStore()

onMounted(() => {
  userStore.loadMenuIds()
  userStore.fetchUserMenus()
  userStore.fetchUserInfo()
})
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.main-content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
  background: #f5f7fa;
  margin-left: 250px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
