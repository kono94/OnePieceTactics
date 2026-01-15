
export interface TraitEffect {
    minUnits: number;
    description: string;
    style: 'bronze' | 'silver' | 'gold' | 'prismatic';
}

export interface TraitDefinition {
    id: string; // "straw_hat", "fighter" - normalized
    name: string; // "Straw Hat", "Fighter"
    description: string;
    effects: TraitEffect[];
    type: 'origin' | 'class';
    iconColor: string; // Simple hex for placeholder
}

// Global store for traits, populated by App.vue from backend
export const TRAIT_DATA: Record<string, TraitDefinition> = {};

export const setTraitData = (traits: TraitDefinition[]) => {
    // Clear existing for hot-swap
    Object.keys(TRAIT_DATA).forEach(key => delete TRAIT_DATA[key]);
    traits.forEach(trait => {
        TRAIT_DATA[trait.id] = trait;
    });
};

// Helper to normalize trait names to IDs
export const normalizeTraitId = (name: string): string => {
    return name.toLowerCase().replace(/\s+/g, '_');
}

export const getTraitData = (name: string): TraitDefinition | null => {
    const id = normalizeTraitId(name);
    return TRAIT_DATA[id] || null;
}
