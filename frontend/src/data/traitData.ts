
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

// Helper to normalize trait names to IDs
export const normalizeTraitId = (name: string): string => {
    return name.toLowerCase().replace(/\s+/g, '_');
}

export const TRAIT_DATA: Record<string, TraitDefinition> = {
    "straw_hat": {
        id: "straw_hat",
        name: "Straw Hat",
        type: "origin",
        description: "Straw Hats gain Health and Attack Speed.",
        iconColor: "#facc15", // yellow-400
        effects: [
            { minUnits: 2, description: "+200 HP, +10% AS", style: 'bronze' },
            { minUnits: 4, description: "+400 HP, +25% AS", style: 'silver' },
            { minUnits: 6, description: "+700 HP, +50% AS", style: 'gold' }
        ]
    },
    "fighter": {
        id: "fighter",
        name: "Fighter",
        type: "class",
        description: "Fighters gain bonus Max Health.",
        iconColor: "#ef4444", // red-500
        effects: [
            { minUnits: 2, description: "+150 Health", style: 'bronze' },
            { minUnits: 4, description: "+350 Health", style: 'silver' },
            { minUnits: 6, description: "+700 Health", style: 'gold' }
        ]
    },
    "swordsman": {
        id: "swordsman",
        name: "Swordsman",
        type: "class",
        description: "Swordsmen have a chance to trigger 2 extra attacks.",
        iconColor: "#94a3b8", // slate-400
        effects: [
            { minUnits: 2, description: "30% chance", style: 'bronze' },
            { minUnits: 4, description: "55% chance", style: 'silver' },
            { minUnits: 6, description: "80% chance", style: 'gold' }
        ]
    },
    "navigator": {
        id: "navigator",
        name: "Navigator",
        type: "class",
        description: "Navigators grant gold at end of combat.",
        iconColor: "#3b82f6", // blue-500
        effects: [
            { minUnits: 1, description: "1-3 Gold per round", style: 'gold' }
        ]
    }
};

export const getTraitData = (name: string): TraitDefinition | null => {
    const id = normalizeTraitId(name);
    return TRAIT_DATA[id] || null;
}
