import type { AbilityDefinition } from '../types'

export interface UltimateGalleryUnit {
    id: string
    name: string
    cost: number
    abilityType: AbilityDefinition['type']
    pattern: AbilityDefinition['pattern']
    abilityName: string
}

export const ULTIMATE_GALLERY_ROSTER: UltimateGalleryUnit[] = [
    { id: 'luffy_v1', name: 'Monkey D. Luffy', cost: 1, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Gum Gum Pistol' },
    { id: 'nami_v1', name: 'Nami', cost: 1, abilityType: 'STUN', pattern: 'LINE', abilityName: 'Thunderbolt Tempo' },
    { id: 'usopp_v1', name: 'Usopp', cost: 1, abilityType: 'BUFF_ATK', pattern: 'SURROUND', abilityName: 'I Got This!' },
    { id: 'zoro_v1', name: 'Roronoa Zoro', cost: 2, abilityType: 'DAMAGE', pattern: 'SURROUND', abilityName: '360 Pound Cannon' },
    { id: 'sanji_v1', name: 'Sanji', cost: 2, abilityType: 'BUFF_SPD', pattern: 'SURROUND', abilityName: 'Diable Jambe' },
    { id: 'chopper_v1', name: 'Tony Tony Chopper', cost: 2, abilityType: 'HEAL', pattern: 'SINGLE', abilityName: 'Emergency Treatment' },
    { id: 'robin_v1', name: 'Nico Robin', cost: 3, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Cien Fleur: Clutch' },
    { id: 'franky_v1', name: 'Franky', cost: 3, abilityType: 'DAMAGE', pattern: 'LINE', abilityName: 'Radical Beam' },
    { id: 'brook_v1', name: 'Brook', cost: 4, abilityType: 'STUN', pattern: 'SURROUND', abilityName: 'Soul Solid' },
    { id: 'jinbei_v1', name: 'Jinbei', cost: 4, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Vagabond Drill' },
    { id: 'koby_v1', name: 'Koby', cost: 1, abilityType: 'HEAL', pattern: 'SINGLE', abilityName: 'Determination' },
    { id: 'helmeppo_v1', name: 'Helmeppo', cost: 1, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Kukri Slash' },
    { id: 'tashigi_v1', name: 'Tashigi', cost: 2, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Justice Strike' },
    { id: 'hina_v1', name: 'Hina', cost: 2, abilityType: 'STUN', pattern: 'SINGLE', abilityName: 'Cage Cage' },
    { id: 'smoker_v1', name: 'Smoker', cost: 3, abilityType: 'STUN', pattern: 'SURROUND', abilityName: 'White Smoke' },
    { id: 'garp_v1', name: 'Garp', cost: 4, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Galaxy Fist' },
    { id: 'sengoku_v1', name: 'Sengoku', cost: 4, abilityType: 'BUFF_ATK', pattern: 'SURROUND', abilityName: 'Buddha Palm' },
    { id: 'kizaru_v1', name: 'Kizaru', cost: 5, abilityType: 'DAMAGE', pattern: 'LINE', abilityName: 'Yata no Kagami' },
    { id: 'akainu_v1', name: 'Akainu', cost: 5, abilityType: 'DAMAGE', pattern: 'SURROUND', abilityName: 'Meteor Volcano' },
    { id: 'buggy_v1', name: 'Buggy', cost: 1, abilityType: 'DAMAGE', pattern: 'SURROUND', abilityName: 'Chop Chop Festival' },
    { id: 'moria_v1', name: 'Moria', cost: 2, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Shadow Steal' },
    { id: 'crocodile_v1', name: 'Crocodile', cost: 3, abilityType: 'DAMAGE', pattern: 'LINE', abilityName: 'Ground Secco' },
    { id: 'kuma_v1', name: 'Kuma', cost: 3, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Ursus Shock' },
    { id: 'doflamingo_v1', name: 'Doflamingo', cost: 4, abilityType: 'DAMAGE', pattern: 'LINE', abilityName: 'Overheat' },
    { id: 'mihawk_v1', name: 'Mihawk', cost: 4, abilityType: 'DAMAGE', pattern: 'LINE', abilityName: "World's Strongest Slash" },
    { id: 'hancock_v1', name: 'Boa Hancock', cost: 4, abilityType: 'STUN', pattern: 'LINE', abilityName: 'Slave Arrow' },
    { id: 'gifter_v1', name: 'Gifter', cost: 1, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Wild Charge' },
    { id: 'headliner_v1', name: 'Headliner', cost: 1, abilityType: 'BUFF_ATK', pattern: 'SURROUND', abilityName: 'Rampage' },
    { id: 'ulti_v1', name: 'Ulti', cost: 2, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Ulti-Mortar' },
    { id: 'page_one_v1', name: 'Page One', cost: 2, abilityType: 'BUFF_ATK', pattern: 'SURROUND', abilityName: 'Spinosaurus Shield' },
    { id: 'sasaki_v1', name: 'Sasaki', cost: 3, abilityType: 'DAMAGE', pattern: 'LINE', abilityName: 'Triceratops Charge' },
    { id: 'whos_who_v1', name: "Who's-Who", cost: 3, abilityType: 'DAMAGE', pattern: 'LINE', abilityName: 'Fang Pistol' },
    { id: 'queen_v1', name: 'Queen', cost: 4, abilityType: 'DAMAGE', pattern: 'SURROUND', abilityName: 'Plague Bullet' },
    { id: 'king_v1', name: 'King', cost: 4, abilityType: 'DAMAGE', pattern: 'LINE', abilityName: 'Magma Dragon' },
    { id: 'kaido_v1', name: 'Kaido', cost: 5, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Thunder Bagua' },
    { id: 'chess_soldiers_v1', name: 'Chess Soldiers', cost: 1, abilityType: 'BUFF_ATK', pattern: 'SURROUND', abilityName: 'Formation' },
    { id: 'prometheus_v1', name: 'Prometheus', cost: 1, abilityType: 'DAMAGE', pattern: 'SURROUND', abilityName: 'Fire Burst' },
    { id: 'perospero_v1', name: 'Charlotte Perospero', cost: 2, abilityType: 'HEAL', pattern: 'SURROUND', abilityName: 'Candy Shower' },
    { id: 'daifuku_v1', name: 'Charlotte Daifuku', cost: 2, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Genie Strike' },
    { id: 'cracker_v1', name: 'Charlotte Cracker', cost: 3, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Biscuit Slash' },
    { id: 'smoothie_v1', name: 'Charlotte Smoothie', cost: 3, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Juice Extract' },
    { id: 'katakuri_v1', name: 'Charlotte Katakuri', cost: 5, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Mochi Thrust' },
    { id: 'big_mom_v1', name: 'Big Mom', cost: 5, abilityType: 'DAMAGE', pattern: 'SURROUND', abilityName: 'Soul Pocus' },
    { id: 'hack_v1', name: 'Hack', cost: 1, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Fishman Punch' },
    { id: 'koala_v1', name: 'Koala', cost: 2, abilityType: 'BUFF_ATK', pattern: 'SURROUND', abilityName: 'Fishman Karate' },
    { id: 'belo_betty_v1', name: 'Belo Betty', cost: 2, abilityType: 'BUFF_ATK', pattern: 'SURROUND', abilityName: 'Kobu Kobu Inspiration' },
    { id: 'ivankov_v1', name: 'Ivankov', cost: 3, abilityType: 'HEAL', pattern: 'SURROUND', abilityName: 'Emporio Healing Hormone' },
    { id: 'sabo_v1', name: 'Sabo', cost: 4, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Dragon Claw' },
    { id: 'dragon_v1', name: 'Dragon', cost: 5, abilityType: 'DAMAGE', pattern: 'SURROUND', abilityName: 'Storm Bringer' },
    { id: 'thatch_v1', name: 'Thatch', cost: 1, abilityType: 'DAMAGE', pattern: 'SINGLE', abilityName: 'Dual Blade' },
    { id: 'jozu_v1', name: 'Jozu', cost: 2, abilityType: 'HEAL', pattern: 'SINGLE', abilityName: 'Diamond Defense' },
    { id: 'vista_v1', name: 'Vista', cost: 3, abilityType: 'DAMAGE', pattern: 'SURROUND', abilityName: 'Flower Sword' },
    { id: 'ace_v1', name: 'Ace', cost: 4, abilityType: 'DAMAGE', pattern: 'SURROUND', abilityName: 'Fire Fist' },
    { id: 'marco_v1', name: 'Marco', cost: 4, abilityType: 'HEAL', pattern: 'SURROUND', abilityName: 'Phoenix Flames' },
    { id: 'whitebeard_v1', name: 'Whitebeard', cost: 5, abilityType: 'DAMAGE', pattern: 'SURROUND', abilityName: 'Quake Punch' }
]
