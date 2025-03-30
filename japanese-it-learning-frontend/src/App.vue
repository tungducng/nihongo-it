<script setup lang="ts">
import { RouterView } from 'vue-router'
import Header from './components/common/Header.vue'
import Footer from './components/common/Footer.vue'
import { onMounted } from 'vue'

onMounted(() => {
  // Initialize user from localStorage if available
})
</script>

<template>
  <v-app>
    <Header />

    <v-main>
      <v-container fluid>
        <RouterView />
      </v-container>
    </v-main>

    <Footer />
  </v-app>
</template>

<style lang="scss"></style>
