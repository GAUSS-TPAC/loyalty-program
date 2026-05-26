export interface Referral {
    id: string;
    name: string;
    joined: string;
    status: "Converti" | "En attente" | "Inactif";
    reward: string;
}

export interface Transaction {
    id: string;
    date: string;
    type: "Achat" | "Parrainage" | "Ajustement Admin" | "Rachat Récompense" | "Débit Wallet" | "Crédit Wallet";
    ledger: "Points" | "Wallet";
    amount: number; // positive or negative
    description: string;
}

export interface RewardGrant {
    id: string;
    name: string;
    grantedDate: string;
    status: "Disponible" | "Utilisé" | "Expiré";
    pointsCost: number;
}

export interface AdminHistory {
    id: string;
    date: string;
    action: string;
    reason: string;
    admin: string;
}

export interface Member {
    id: string;
    name: string;
    email: string;
    phone: string;
    externalId: string;
    tier: "Bronze" | "Silver" | "Gold" | "Platinum";
    points: number;
    walletBalance: number;
    walletStatus: "ACTIVE" | "FROZEN" | "CLOSED";
    joined: string;
    lastActive: string;
    avatar: string;
    segment: "VIP" | "Régulier" | "Nouveau" | "Inactif";
    referralCode: string;
    referralsCount: number;
    nextTierPoints: number;
    referrals: Referral[];
    transactions: Transaction[];
    rewards: RewardGrant[];
    adminHistory: AdminHistory[];
}

export const initialMembers: Member[] = [
    {
        id: "1",
        name: "Marie Ndjomo",
        email: "marie.ndjomo@email.com",
        phone: "+237 690 123 456",
        externalId: "EMP-9982",
        tier: "Gold",
        points: 12450,
        walletBalance: 8750,
        walletStatus: "ACTIVE",
        joined: "2025-02-12",
        lastActive: "2026-05-23",
        avatar: "MN",
        segment: "VIP",
        referralCode: "MARIE998",
        referralsCount: 3,
        nextTierPoints: 20000,
        referrals: [
            { id: "ref1", name: "Alice Koffi", joined: "2025-04-10", status: "Converti", reward: "500 Points" },
            { id: "ref2", name: "Marc Kamga", joined: "2025-08-15", status: "En attente", reward: "Aucune" },
            { id: "ref3", name: "Sarah Touré", joined: "2026-01-20", status: "Converti", reward: "500 Points" }
        ],
        transactions: [
            { id: "tx1", date: "2026-05-23", type: "Achat", ledger: "Points", amount: 450, description: "Achat supermarché Yaoundé" },
            { id: "tx2", date: "2026-05-23", type: "Crédit Wallet", ledger: "Wallet", amount: 5000, description: "Rechargement Orange Money" },
            { id: "tx3", date: "2026-05-18", type: "Rachat Récompense", ledger: "Points", amount: -1500, description: "Échange bon d'achat Boulangerie" },
            { id: "tx4", date: "2026-05-15", type: "Débit Wallet", ledger: "Wallet", amount: -1200, description: "Paiement partenaire Cinéma" },
            { id: "tx5", date: "2026-05-10", type: "Parrainage", ledger: "Points", amount: 500, description: "Parrainage converti Sarah Touré" },
            { id: "tx6", date: "2026-04-30", type: "Ajustement Admin", ledger: "Points", amount: 1000, description: "Geste commercial anniversaire" }
        ],
        rewards: [
            { id: "rew1", name: "Bon d'achat Boulangerie 1000 XAF", grantedDate: "2026-05-18", status: "Utilisé", pointsCost: 1500 },
            { id: "rew2", name: "Café Offert - Station Total", grantedDate: "2026-05-22", status: "Disponible", pointsCost: 500 },
            { id: "rew3", name: "Réduction 10% Boutique Mode", grantedDate: "2025-12-15", status: "Expiré", pointsCost: 1000 }
        ],
        adminHistory: [
            { id: "adh1", date: "2026-04-30", action: "Ajustement Points", reason: "Geste commercial anniversaire (Campagne)", admin: "Admin Principal" }
        ]
    },
    {
        id: "2",
        name: "Jean Dupont",
        email: "jean.dupont@email.com",
        phone: "+237 655 789 012",
        externalId: "EMP-4109",
        tier: "Silver",
        points: 3420,
        walletBalance: 1250,
        walletStatus: "FROZEN",
        joined: "2025-11-05",
        lastActive: "2026-05-20",
        avatar: "JD",
        segment: "Régulier",
        referralCode: "JEAND410",
        referralsCount: 1,
        nextTierPoints: 5000,
        referrals: [
            { id: "ref4", name: "Paul Biyogo", joined: "2026-02-14", status: "Inactif", reward: "Aucune" }
        ],
        transactions: [
            { id: "tx7", date: "2026-05-20", type: "Achat", ledger: "Points", amount: 120, description: "Achat boutique station-service" },
            { id: "tx8", date: "2026-05-02", type: "Débit Wallet", ledger: "Wallet", amount: -500, description: "Transfert wallet suspect détecté" }
        ],
        rewards: [
            { id: "rew4", name: "Bon Boisson Offerte", grantedDate: "2026-03-01", status: "Disponible", pointsCost: 400 }
        ],
        adminHistory: [
            { id: "adh2", date: "2026-05-20", action: "Gel Wallet", reason: "Activité transactionnelle suspecte détectée par le filtre automatique", admin: "Système de sécurité" }
        ]
    },
    {
        id: "3",
        name: "Alice Koffi",
        email: "alice.koffi@email.com",
        phone: "+225 07 48 99 22 11",
        externalId: "EMP-8843",
        tier: "Bronze",
        points: 850,
        walletBalance: 4000,
        walletStatus: "FROZEN",
        joined: "2026-04-10",
        lastActive: "2026-05-24",
        avatar: "AK",
        segment: "Nouveau",
        referralCode: "ALICE884",
        referralsCount: 0,
        nextTierPoints: 2000,
        referrals: [],
        transactions: [
            { id: "tx9", date: "2026-05-24", type: "Crédit Wallet", ledger: "Wallet", amount: 4000, description: "Rechargement Wave CI" },
            { id: "tx10", date: "2026-05-24", type: "Achat", ledger: "Points", amount: 850, description: "Premier achat validé" }
        ],
        rewards: [],
        adminHistory: [
            { id: "adh3", date: "2026-05-24", action: "Gel Wallet", reason: "Suspicion de double transaction de parrainage", admin: "Contrôleur de conformité" }
        ]
    },
    {
        id: "4",
        name: "Marc Koffi",
        email: "marc.koffi@email.com",
        phone: "+225 05 12 34 56 78",
        externalId: "EMP-8750",
        tier: "Gold",
        points: 15400,
        walletBalance: 24500,
        walletStatus: "ACTIVE",
        joined: "2025-05-15",
        lastActive: "2026-05-25",
        avatar: "MK",
        segment: "VIP",
        referralCode: "MARCK875",
        referralsCount: 5,
        nextTierPoints: 20000,
        referrals: [
            { id: "ref5", name: "Pierre Yao", joined: "2025-06-01", status: "Converti", reward: "500 Points" },
            { id: "ref6", name: "Julienne Kra", joined: "2025-06-12", status: "Converti", reward: "500 Points" }
        ],
        transactions: [
            { id: "tx11", date: "2026-05-25", type: "Achat", ledger: "Points", amount: 1500, description: "Achat magasin Abidjan" },
            { id: "tx12", date: "2026-05-20", type: "Crédit Wallet", ledger: "Wallet", amount: 15000, description: "Rechargement Moov" }
        ],
        rewards: [
            { id: "rew5", name: "Bon d'achat Supermarché 5000 XAF", grantedDate: "2026-04-10", status: "Utilisé", pointsCost: 7500 }
        ],
        adminHistory: []
    },
    {
        id: "5",
        name: "Sarah Touré",
        email: "sarah.toure@email.com",
        phone: "+221 77 123 45 67",
        externalId: "EMP-2009",
        tier: "Gold",
        points: 10500,
        walletBalance: 12000,
        walletStatus: "ACTIVE",
        joined: "2026-01-20",
        lastActive: "2026-05-24",
        avatar: "ST",
        segment: "VIP",
        referralCode: "SARAHT200",
        referralsCount: 0,
        nextTierPoints: 20000,
        referrals: [],
        transactions: [
            { id: "tx13", date: "2026-05-24", type: "Achat", ledger: "Points", amount: 1000, description: "Achat Dakar Mall" }
        ],
        rewards: [],
        adminHistory: []
    },
    {
        id: "6",
        name: "Stéphane Ndour",
        email: "stephane.ndour@email.com",
        phone: "+221 76 987 65 43",
        externalId: "EMP-1024",
        tier: "Platinum",
        points: 38900,
        walletBalance: 45000,
        walletStatus: "ACTIVE",
        joined: "2024-03-10",
        lastActive: "2026-05-22",
        avatar: "SN",
        segment: "VIP",
        referralCode: "STEPH102",
        referralsCount: 8,
        nextTierPoints: 50000,
        referrals: [
            { id: "ref7", name: "Amadou Diallo", joined: "2024-05-15", status: "Converti", reward: "500 Points" }
        ],
        transactions: [
            { id: "tx14", date: "2026-05-22", type: "Achat", ledger: "Points", amount: 3200, description: "Achat Vol Dakar-Paris" }
        ],
        rewards: [
            { id: "rew6", name: "Accès Salon VIP Aéroport", grantedDate: "2026-05-20", status: "Utilisé", pointsCost: 15000 }
        ],
        adminHistory: []
    },
    {
        id: "7",
        name: "Fatou Diome",
        email: "fatou.diome@email.com",
        phone: "+221 78 555 44 33",
        externalId: "EMP-0456",
        tier: "Bronze",
        points: 450,
        walletBalance: 0,
        walletStatus: "CLOSED",
        joined: "2025-01-10",
        lastActive: "2025-03-15",
        avatar: "FD",
        segment: "Inactif",
        referralCode: "FATOUD04",
        referralsCount: 0,
        nextTierPoints: 2000,
        referrals: [],
        transactions: [
            { id: "tx15", date: "2025-03-15", type: "Débit Wallet", ledger: "Wallet", amount: -400, description: "Solde vidé pour clôture" }
        ],
        rewards: [],
        adminHistory: [
            { id: "adh4", date: "2025-03-15", action: "Clôture Compte", reason: "À la demande du client", admin: "Conseiller Clientèle" }
        ]
    }
];

// Helper functions to save modified states locally in browser/memory
let customStoredMembers: Member[] | null = null;

export function getMembers(): Member[] {
    if (typeof window !== "undefined") {
        const stored = localStorage.getItem("loyalty_members");
        if (stored) {
            try {
                return JSON.parse(stored);
            } catch (e) {
                console.error(e);
            }
        }
    }
    if (!customStoredMembers) {
        customStoredMembers = [...initialMembers];
    }
    return customStoredMembers;
}

export function saveMembers(members: Member[]) {
    customStoredMembers = members;
    if (typeof window !== "undefined") {
        localStorage.setItem("loyalty_members", JSON.stringify(members));
    }
}
