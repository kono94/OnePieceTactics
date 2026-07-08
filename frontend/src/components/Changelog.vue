<script setup lang="ts">
defineOptions({
  name: 'GameChangelog'
})

defineEmits(['back'])

const versionXXXCommits = [
  { hash: 'pending', title: 'Add backend combat simulation reports' },
  { hash: 'pending', title: 'Upgrade third-party dependencies' }
]

const version161Commits = [
  { hash: '92d4a30', title: 'Prevent completed unit lines from reappearing' },
  { hash: '8833a6f', title: 'Balance augments' },
  { hash: 'f3a35cd', title: 'Balance bot opponent scaling' }
]

const version160Commits = [
  { hash: 'fc426b0', title: 'Fix line abilities silently missing off-axis targets' },
  { hash: '78c7cc3', title: 'Balance One Piece units and augments' },
  { hash: '21e362d', title: 'Add defender mana on direct hits' },
  { hash: '869c746', title: 'Update agent instructions' },
  { hash: 'b771cf4', title: 'Balance Pokemon traits and add changelog' },
  { hash: 'e5dba11', title: 'Fix unit drag preview rendering' },
  { hash: '989c37b', title: 'Prevent joining started games' },
  { hash: '700c520', title: 'Fix ghost unit board state sync' },
  { hash: 'c9fe5e5', title: 'Fix knockback combat grid bounds' }
]
</script>

<template>
  <main class="changelog-page">
    <button class="back-button" type="button" @click="$emit('back')">Back</button>

    <div class="release-history">
      <section class="release-section">
        <div class="release-header">
          <p class="eyebrow">Latest</p>
          <h2>Version X.X.X</h2>
          <p>
            Frontend and backend dependencies have been refreshed for the current security baseline, including Vite 8,
            Spring Boot 4.1, Vue 3.5, and updated build tooling.
          </p>
        </div>

        <div class="release-grid">
          <article class="release-panel">
            <div class="section-heading">
              <span class="marker release"></span>
              <h3>Development</h3>
            </div>
            <ul class="commit-list">
              <li v-for="commit in versionXXXCommits" :key="commit.hash">
                <span class="hash">{{ commit.hash }}</span>
                <span>{{ commit.title }}</span>
              </li>
            </ul>
          </article>

          <article class="release-panel">
            <div class="section-heading">
              <span class="marker release"></span>
              <h3>Simulation</h3>
            </div>
            <p>
              Backend balance tooling can now run deterministic tick-based combat simulations and generate Pokemon-first
              Markdown reports for 100k cost-profile board matchups, split unit rankings by star level, and trait impact
              tables, with sample coverage diagnostics, console progress, and configurable parallel workers during long
              runs.
            </p>
          </article>
        </div>
      </section>

      <section class="release-section">
        <div class="release-header">
          <p class="eyebrow">Previous</p>
          <h2>Version 1.6.1</h2>
          <p>
            Bot opponents now scale more deliberately, completed 3-star character lines stop appearing in shops and unit
            loot, and augment rewards have a tighter combat-versus-economy balance.
          </p>
        </div>

        <div class="release-grid">
          <article class="release-panel">
            <div class="section-heading">
              <span class="marker release"></span>
              <h3>Development</h3>
            </div>
            <ul class="commit-list">
              <li v-for="commit in version161Commits" :key="commit.hash">
                <span class="hash">{{ commit.hash }}</span>
                <span>{{ commit.title }}</span>
              </li>
            </ul>
          </article>

          <article class="balance-panel">
            <div class="section-heading">
              <span class="marker balance"></span>
              <h3>Balance Changes</h3>
            </div>

            <div class="balance-block">
              <div class="balance-title">
                <span class="tag mixed">Mixed</span>
                <h4>Augments</h4>
              </div>
              <p>
                Flat gold:
                <span class="old-value">20/35/50 gold</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value nerf">15/25/35 gold</strong>. On-kill team Attack Damage:
                <span class="old-value">5/8/12</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">6/10/15</strong>. Melee Lifesteal:
                <span class="old-value">8/12/18%</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">10/16/24%</strong>. Starting Shield:
                <span class="old-value">100/180/300</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">125/225/375</strong>. Ability Power now increases damaging ability output by
                <strong class="value buff">10/18/30%</strong>.
              </p>
            </div>

            <div class="balance-block">
              <div class="balance-title">
                <span class="tag buff">Buff</span>
                <h4>Bot Opponents</h4>
              </div>
              <p>
                Overall bot power level:
                <span class="old-value">too low</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">greatly improved</strong>, with more pressure from early upgraded boards and
                stronger late-game threats.
              </p>
            </div>
          </article>
        </div>
      </section>

      <section class="release-section">
        <div class="release-header">
          <p class="eyebrow">Previous</p>
          <h2>Version 1.6.0</h2>
          <p>
            Combat reliability fixes, lobby safeguards, drag polish, mana flow, and Pokemon plus One Piece balance
            passes.
          </p>
        </div>

        <div class="release-grid">
          <article class="release-panel">
            <div class="section-heading">
              <span class="marker release"></span>
              <h3>Development</h3>
            </div>
            <ul class="commit-list">
              <li v-for="commit in version160Commits" :key="commit.hash">
                <span class="hash">{{ commit.hash }}</span>
                <span>{{ commit.title }}</span>
              </li>
            </ul>
          </article>

          <article class="balance-panel">
            <div class="section-heading">
              <span class="marker balance"></span>
              <h3>Balance Changes</h3>
            </div>

            <div class="balance-block">
              <div class="balance-title">
                <span class="tag buff">Buff</span>
                <h4>Mana</h4>
              </div>
              <p>
                Defender mana on direct attacks and damage abilities:
                <span class="old-value">0%</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">5%</strong> max mana.
              </p>
            </div>

            <div class="balance-block">
              <div class="balance-title">
                <span class="tag nerf">Nerf</span>
                <h4>Straw Hat</h4>
              </div>
              <p>
                Health:
                <span class="old-value">200/400/700/1000</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value nerf">150/300/500/750</strong>. Attack speed:
                <span class="old-value">10/25/50/75%</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value nerf">6/14/28/42%</strong>.
              </p>
            </div>

            <div class="balance-block">
              <div class="balance-title">
                <span class="tag nerf">Nerf</span>
                <h4>Jinbei</h4>
              </div>
              <p>
                Health:
                <span class="old-value">1050/1890/3402</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value nerf">950/1710/3078</strong>. Mana:
                <span class="old-value">65</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value nerf">75</strong>. Armor:
                <span class="old-value">40</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value nerf">35</strong>. Vagabond Drill:
                <span class="old-value">500/900/1620</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value nerf">420/760/1368</strong>.
              </p>
            </div>

            <div class="balance-block">
              <div class="balance-title">
                <span class="tag nerf">Nerf</span>
                <h4>Boa Hancock</h4>
              </div>
              <p>
                Slave Arrow stun:
                <span class="old-value">3/4/5s</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value nerf">1/1/2s</strong>.
              </p>
            </div>

            <div class="balance-block">
              <div class="balance-title">
                <span class="tag buff">Buff</span>
                <h4>Garp</h4>
              </div>
              <p>
                Mana:
                <span class="old-value">60</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">55</strong>. Fist of Love:
                <span class="old-value">600/1080/1944</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">700/1260/2268</strong>. Missing HP scaling:
                <span class="old-value">0.5/0.75/1.0</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">0.6/0.85/1.1</strong>.
              </p>
            </div>

            <div class="balance-block">
              <div class="balance-title">
                <span class="tag buff">Buff</span>
                <h4>One Piece Augments</h4>
              </div>
              <p>
                Battle Standard:
                <span class="old-value">120/220/360</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">150/275/450</strong>. Sharpened Blades:
                <span class="old-value">4/7/10</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">6/10/16</strong>. Iron Line:
                <span class="old-value">8/14/24</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">10/18/30</strong>. Backline Barrage:
                <span class="old-value">5/8/12</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">7/12/18</strong>.
              </p>
            </div>

            <div class="balance-block">
              <div class="balance-title">
                <span class="tag nerf">Nerf</span>
                <h4>Normal</h4>
              </div>
              <p>
                Team attack bonus:
                <span class="old-value">3/10/16/24%</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value nerf">1/5/10/16%</strong>.
              </p>
            </div>

            <div class="balance-block">
              <div class="balance-title">
                <span class="tag nerf">Nerf</span>
                <h4>Flying</h4>
              </div>
              <p>
                Low-health attack speed:
                <span class="old-value">8/24/40/60%</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value nerf">4/12/24/36%</strong>.
              </p>
            </div>

            <div class="balance-block">
              <div class="balance-title">
                <span class="tag buff">Buff</span>
                <h4>Poison</h4>
              </div>
              <p>
                Duration:
                <span class="old-value">2s</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">3s</strong>. Damage per tick:
                <span class="old-value">2/7/12/18%</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">3/7/11/16%</strong> attack damage.
              </p>
            </div>

            <div class="balance-block">
              <div class="balance-title">
                <span class="tag nerf">Nerf</span>
                <h4>Raichu</h4>
              </div>
              <p>
                Thunder target count:
                <span class="old-value">uncapped</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value nerf">3 targets</strong>. 3-star stun remains
                <strong class="value buff">2s</strong>.
              </p>
            </div>

            <div class="balance-block">
              <div class="balance-title">
                <span class="tag mixed">Economy</span>
                <h4>Augments</h4>
              </div>
              <p>
                Flat gold:
                <span class="old-value">30/45/70</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value nerf">20/35/50</strong>. Empty bench gold:
                <span class="old-value">2/3/5</span>
                <span class="change-arrow">&nbsp;=>&nbsp;</span>
                <strong class="value buff">3/5/8</strong> per slot.
              </p>
            </div>
          </article>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
.changelog-page {
    position: relative;
    height: 100vh;
    overflow-y: auto;
    padding: 36px clamp(18px, 5vw, 72px) 56px;
    color: #f8fafc;
    background:
        linear-gradient(135deg, rgba(14, 165, 233, 0.14), transparent 34%),
        radial-gradient(circle at 92% 14%, rgba(244, 114, 182, 0.16), transparent 28%),
        #0b1120;
}

.back-button {
    position: sticky;
    top: 0;
    z-index: 2;
    margin-bottom: 18px;
    padding: 10px 14px;
    border: 1px solid rgba(148, 163, 184, 0.34);
    border-radius: 6px;
    background: rgba(15, 23, 42, 0.86);
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

.release-history {
    display: grid;
    gap: 42px;
}

.release-section {
    max-width: 1180px;
    margin: 0 auto;
}

.release-header {
    margin-bottom: 18px;
}

.release-header h2 {
    margin: 0;
    font-size: clamp(30px, 4vw, 48px);
    line-height: 1;
}

.release-header p:last-child {
    max-width: 720px;
    margin: 12px 0 0;
    color: #cbd5e1;
    font-size: 17px;
}

.release-grid {
    display: grid;
    grid-template-columns: minmax(280px, 0.9fr) minmax(320px, 1.3fr);
    gap: 22px;
    margin: 0;
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

.section-heading h3,
.balance-title h4 {
    margin: 0;
}

.section-heading h3 {
    font-size: 22px;
}

.balance-title h4 {
    font-size: 18px;
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
    .release-grid {
        grid-template-columns: 1fr;
    }

    .commit-list li {
        grid-template-columns: 1fr;
    }
}
</style>
