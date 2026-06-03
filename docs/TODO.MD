Étape 1 — Vérifier que la base est solide (maintenant)
Avant d'avancer, confirme que ces trois choses sont vraies. mvn test -Dtest=HexagonalArchitectureTest passe en vert. mvn spring-boot:run -Dspring-boot.run.profiles=dev,stub démarre sans erreur. GET /api/v1/health/tenant avec un JWT de test retourne 200. Si l'une de ces trois vérifications échoue, il faut corriger avant de continuer — sinon on construit sur une base cassée.

Étape 2 — Module Wallet (semaine suivante)
C'est le premier vrai module métier à développer. Les autres modules en dépendent car les récompenses et les cashbacks créditent le wallet.
On commence par les entités R2DBC et la migration SQL, ensuite le service domaine WalletDomainService avec les règles de crédit, débit, gel et fermeture, puis les handlers applicatifs CreditWalletHandler, DebitWalletHandler, WithdrawHandler avec l'idempotence Redis, ensuite les contrôleurs WebFlux, et enfin les tests d'intégration Testcontainers.
Le guide exhaustif de ce module existe déjà dans notre conversation — la liste des 14 fonctionnalités et tous les fichiers à créer.

Étape 3 — Moteur de règles et points (après le wallet)
C'est le cerveau du système. On crée les entités Rule, Trigger, Condition, Effect, Counter, PointsAccount. Ensuite le RuleEngine qui évalue les règles sur un Event entrant. L'endpoint POST /api/v1/events qui reçoit les actions des plateformes clientes.

Étape 4 — Intégration API Bonification externe
Le guide complet pour le camarade existe déjà dans la conversation. BonificationPort dans le domaine, BonificationApiAdapter dans l'infrastructure avec Resilience4j, fallback en cas d'indisponibilité.

Étape 5 — Module Récompenses
Catalogue Reward, attribution RewardGrant, service d'échange de points RedemptionService, endpoint de consommation au checkout.

Étape 6 — Module Parrainage
ReferralProgram, ReferralLink, ReferralEvent. Génération des liens, enrôlement des filleuls, détection de fraude basique, déclenchement des récompenses à la conversion.

Étape 7 — Module Codes Promo
PromoCampaign, PromoEligibility, compteur d'utilisations dans Redis, PromoUsage. Endpoint de validation avant paiement et endpoint d'application après confirmation.

Étape 8 — Campagnes temporisées et Paliers
TierPolicy, MemberTier, calcul automatique du palier après attribution de points. Campaign avec le Scheduler pour activation et désactivation automatiques.

Étape 9 — Module Abonnements SaaS
SubscriptionPlan, TenantSubscription, InvoiceRecord. Gestion des plans que Royal vend aux tenants qui veulent utiliser la plateforme.

Étape 10 — Frontend Dashboard (Next.js)
Une fois le backend stable et testé, on attaque le frontend avec les pages de configuration du programme de fidélité, le Rule Builder visuel, la gestion du wallet, les analytics.

Question concrète maintenant : est-ce que tu veux qu'on génère le guide exhaustif du Module Wallet dans le même format que ce qu'on avait fait pour les fondations — liste complète des fichiers à créer avec leur contenu exact, dans l'ordre d'implémentation — pour que tu puisses le donner à Claude Code ?
