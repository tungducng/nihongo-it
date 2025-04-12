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

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'
import { RouterView } from 'vue-router'
import Header from './components/common/Header.vue'
import Footer from './components/common/Footer.vue'

@Component({
  name: 'App',
  components: {
    RouterView,
    Header,
    Footer,
  },
})
export default class App extends Vue {
  mounted(): void {
    // Initialize user from localStorage if available
  }
}
</script>

<style lang="sass"></style>
