<script setup>
import { useTheme } from '@/composables/useTheme'
const { currentTheme } = useTheme()
</script>

<template>
  <div :class="'theme-' + currentTheme">
    <router-view />
  </div>
</template>

<style></style>
