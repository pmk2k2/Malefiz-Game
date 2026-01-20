<template>
  <div class="keyboard-guide-container">
    <h4 class="guide-title">Kurzanleitung</h4>
    <ul class="guide-list">
      <li>
        <span class="key-cap"><-</span> <span class="key-cap">-></span> : Wechseln zwischen Figuren
      </li>
      <li><span class="key-cap">w</span> : Figur vorwärts bewegen</li>
      <li>
        <span class="key-cap">a</span> <span class="key-cap">d</span> : Figur nach links/rechts
        drehen
      </li>
      <li><span class="key-cap">Leertaste</span> : Mit Figur springen</li>
    </ul>
  </div>
</template>

<style scoped>
.keyboard-guide-container {
  background: rgba(45, 25, 10, 0.7);
  backdrop-filter: blur(4px);
  border: 2px solid #8b5e3c;
  border-radius: 12px;
  padding: 10px 15px;
  color: #f0e2d0;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.5);
  min-width: 250px;
}

.guide-title {
  color: #ffc107;
  margin: 0 0 8px 0;
  font-weight: 800;
  text-transform: uppercase;
  display: flex;
  justify-content: center;
}

.guide-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.key-cap {
  background: #3d2b1f;
  border: 1px solid #ffc107;
  border-radius: 4px;
  padding: 1px 6px;
  font-family: monospace;
  color: #ffc107;
  margin: 0 2px;
}
</style>
