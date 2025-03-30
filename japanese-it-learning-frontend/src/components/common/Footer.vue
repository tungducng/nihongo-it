<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <v-footer app>
    <v-col class="text-center" cols="12">
      &copy; {{ new Date().getFullYear() }} Japanese IT Learning
    </v-col>
  </v-footer>
</template>

<style lang="scss" scoped>
.v-footer {
  background-color: $color-background-soft;
  padding: 1rem;
}
</style>
