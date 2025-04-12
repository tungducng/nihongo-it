<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <v-footer app>
    <v-col class="text-center" cols="12">
      &copy; {{ new Date().getFullYear() }} Japanese IT Learning
    </v-col>
  </v-footer>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-facing-decorator'

@Component({
  name: 'Footer',
})
export default class Footer extends Vue {}
</script>

<style lang="sass" scoped>
.v-footer
  background-color: #f8f9fa
  padding: 1rem
</style>
