<script setup lang="ts">
defineOptions({
  name: 'GameChangelog'
})

defineEmits(['back'])

const commits = [
  { hash: 'pending-1', title: 'Tune bot opponent roster scaling' },
  { hash: 'pending-2', title: 'Prevent completed 3-star unit lines from reappearing in shops and unit loot' }
]
</script>

<template>
  <main class="changelog-page">
    <header class="hero">
      <button class="back-button" type="button" @click="$emit('back')">Back</button>
      <div>
        <p class="eyebrow">Release Notes</p>
        <h1>Version X.X.X</h1>
        <p class="summary">
          Bot opponents now scale more deliberately, and completed 3-star character lines stop appearing in shops and unit loot.
        </p>
      </div>
    </header>

    <section class="release-grid">
      <article class="release-panel">
        <div class="section-heading">
          <span class="marker release"></span>
          <h2>Latest Development</h2>
        </div>
        <ul class="commit-list">
          <li v-for="commit in commits" :key="commit.hash">
            <span class="hash">{{ commit.hash }}</span>
            <span>{{ commit.title }}</span>
          </li>
        </ul>
      </article>

      <article class="balance-panel">
        <div class="section-heading">
          <span class="marker balance"></span>
          <h2>Balance Changes</h2>
        </div>

        <div class="balance-block">
          <div class="balance-title">
            <span class="tag mixed">Mixed</span>
            <h3>Bot Opponents</h3>
          </div>
          <p>
            Rounds 1-3 stay at
            <strong class="value buff">5%</strong> 2-star and
            <strong class="value buff">1%</strong> 3-star odds. Rounds 4-6 1-3 cost upgrade odds:
            <span class="old-value">5% 2-star / 1% 3-star</span>
            <span class="change-arrow">&nbsp;=>&nbsp;</span>
            <strong class="value buff">24% 2-star / 2% 3-star</strong>. Rounds 7-9:
            <span class="old-value">5% / 1%</span>
            <span class="change-arrow">&nbsp;=>&nbsp;</span>
            <strong class="value buff">34% / 8%</strong>. Rounds 10-13:
            <span class="old-value">5% / 1%</span>
            <span class="change-arrow">&nbsp;=>&nbsp;</span>
            <strong class="value buff">40% / 16%</strong>. Rounds 14+:
            <span class="old-value">5% / 1%</span>
            <span class="change-arrow">&nbsp;=>&nbsp;</span>
            <strong class="value buff">35% / 30%</strong>.
          </p>
        </div>

        <div class="balance-block">
          <div class="balance-title">
            <span class="tag nerf">Nerf</span>
            <h3>Late Bot Boards</h3>
          </div>
          <p>
            Maximum late board size:
            <span class="old-value">9 units</span>
            <span class="change-arrow">&nbsp;=>&nbsp;</span>
            <strong class="value nerf">7 units</strong> from the late-round roster profiles.
          </p>
        </div>

        <div class="balance-block">
          <div class="balance-title">
            <span class="tag mixed">Mixed</span>
            <h3>Premium Bot Units</h3>
          </div>
          <p>
            4-5 cost 3-star chance:
            <span class="old-value">1%</span>
            <span class="change-arrow">&nbsp;=>&nbsp;</span>
            <strong class="value nerf">0%</strong>. 4-5 cost 2-star chance scales from
            <span class="old-value">5%</span>
            <span class="change-arrow">&nbsp;=>&nbsp;</span>
            <strong class="value buff">12/18/25/35%</strong> across the later round bands.
          </p>
        </div>
      </article>
    </section>
  </main>
</template>

<style scoped>
.changelog-page {
    height: 100vh;
    overflow-y: auto;
    padding: 36px clamp(18px, 5vw, 72px) 56px;
    color: #f8fafc;
    background:
        linear-gradient(135deg, rgba(14, 165, 233, 0.14), transparent 34%),
        radial-gradient(circle at 92% 14%, rgba(244, 114, 182, 0.16), transparent 28%),
        #0b1120;
}

.hero {
    display: flex;
    align-items: flex-start;
    gap: 22px;
    max-width: 1180px;
    margin: 0 auto 34px;
}

.back-button {
    flex: 0 0 auto;
    padding: 10px 14px;
    border: 1px solid rgba(148, 163, 184, 0.34);
    border-radius: 6px;
    background: rgba(15, 23, 42, 0.76);
    color: #e2e8f0;
    font-weight: 800;
    cursor: pointer;
}

.eyebrow {
    margin: 0 0 8px;
    color: #38bdf8;
    font-size: 13px;
    font-weight: 900;
    text-transform: uppercase;
}

h1 {
    margin: 0;
    font-size: clamp(38px, 6vw, 72px);
    line-height: 0.95;
}

.summary {
    max-width: 720px;
    margin: 18px 0 0;
    color: #cbd5e1;
    font-size: 18px;
}

.release-grid {
    display: grid;
    grid-template-columns: minmax(280px, 0.9fr) minmax(320px, 1.3fr);
    gap: 22px;
    max-width: 1180px;
    margin: 0 auto;
}

.release-panel,
.balance-panel {
    border: 1px solid rgba(148, 163, 184, 0.22);
    border-radius: 8px;
    background: rgba(15, 23, 42, 0.72);
    box-shadow: 0 24px 70px rgba(0, 0, 0, 0.28);
}

.release-panel {
    padding: 24px;
}

.balance-panel {
    padding: 24px;
}

.section-heading,
.balance-title {
    display: flex;
    align-items: center;
    gap: 10px;
}

.section-heading {
    margin-bottom: 18px;
}

.section-heading h2,
.balance-title h3 {
    margin: 0;
}

.marker {
    width: 10px;
    height: 28px;
    border-radius: 6px;
}

.marker.release {
    background: #38bdf8;
}

.marker.balance {
    background: #f59e0b;
}

.commit-list {
    display: grid;
    gap: 12px;
    margin: 0;
    padding: 0;
    list-style: none;
}

.commit-list li {
    display: grid;
    grid-template-columns: 76px 1fr;
    gap: 12px;
    align-items: center;
    padding: 12px;
    border: 1px solid rgba(148, 163, 184, 0.16);
    border-radius: 6px;
    background: rgba(30, 41, 59, 0.62);
}

.hash {
    color: #67e8f9;
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
    font-size: 13px;
    font-weight: 900;
}

.balance-block {
    padding: 16px 0;
    border-top: 1px solid rgba(148, 163, 184, 0.18);
}

.balance-block:first-of-type {
    border-top: 0;
    padding-top: 0;
}

.balance-block p {
    margin: 8px 0 0;
    color: #dbeafe;
    font-size: 16px;
}

.tag {
    min-width: 68px;
    padding: 4px 8px;
    border-radius: 5px;
    font-size: 12px;
    font-weight: 900;
    text-align: center;
    text-transform: uppercase;
}

.tag.nerf,
.value.nerf {
    color: #fca5a5;
}

.tag.nerf {
    background: rgba(239, 68, 68, 0.18);
}

.tag.buff,
.value.buff {
    color: #86efac;
}

.tag.buff {
    background: rgba(34, 197, 94, 0.18);
}

.tag.mixed {
    color: #fde68a;
    background: rgba(245, 158, 11, 0.18);
}

.value {
    font-weight: 900;
}

.old-value {
    display: inline-block;
    margin: 0 4px;
    color: #cbd5e1;
    font-weight: 900;
    text-decoration-line: line-through;
    text-decoration-thickness: 2px;
    text-decoration-color: rgba(203, 213, 225, 0.74);
}

.change-arrow {
    margin: 0 4px;
    color: #64748b;
    font-size: 13px;
    font-weight: 900;
}

@media (max-width: 820px) {
    .hero {
        flex-direction: column;
    }

    .release-grid {
        grid-template-columns: 1fr;
    }

    .commit-list li {
        grid-template-columns: 1fr;
    }
}
</style>
