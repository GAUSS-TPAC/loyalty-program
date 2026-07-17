"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { ExternalLink } from "lucide-react";

const CODE_BLOCK =
  "bg-muted border border-border rounded-md p-4 text-xs font-mono overflow-x-auto whitespace-pre";

function CodeTabs({ samples }: { samples: Record<string, string> }) {
  const languages = Object.keys(samples);
  const [active, setActive] = useState(languages[0]);
  return (
    <div className="space-y-0">
      <div className="flex gap-1 flex-wrap">
        {languages.map((lang) => (
          <button
            key={lang}
            onClick={() => setActive(lang)}
            className={`text-xs px-3 py-1.5 rounded-t-md border border-b-0 transition-colors ${
              active === lang
                ? "bg-muted border-border text-foreground font-medium"
                : "bg-background border-transparent text-muted-foreground hover:text-foreground"
            }`}
          >
            {lang}
          </button>
        ))}
      </div>
      <pre className={CODE_BLOCK}>{samples[active]}</pre>
    </div>
  );
}

function Step({ n, title, children }: { n: string; title: string; children: React.ReactNode }) {
  return (
    <section className="bg-card border border-border rounded-xl p-6 space-y-3">
      <h3 className="text-sm font-semibold text-foreground">
        <span className="text-primary mr-2">{n}</span>
        {title}
      </h3>
      {children}
    </section>
  );
}

const TRACK_EVENT_SAMPLES = {
  curl: `curl -X POST "$BASE_URL/api/v1/apps/pk_live_xxx/events" \\
  -H "X-Api-Key: sk_live_xxx" \\
  -H "Content-Type: application/json" \\
  -H "Idempotency-Key: commande-2026-0042" \\
  -d '{
    "eventType": "purchase.completed",
    "memberId": "00000000-0000-0000-0000-000000000123",
    "occurredAt": "2026-07-17T10:00:00Z",
    "payload": { "amount": 4990 }
  }'`,
  PHP: `$loyalty = new \\Yowyob\\Loyalty\\LoyaltyClient(
    $_ENV['LOYALTY_PUBLIC_KEY'],
    $_ENV['LOYALTY_PRIVATE_KEY'],
    'https://loyalty.yowyob.com'
);

$result = $loyalty->trackEvent(
    'purchase.completed',
    $memberId,
    null,
    ['amount' => 4990],
    'commande-' . $orderId   // clé d'idempotence
);`,
  JavaScript: `import { LoyaltyClient } from "@yowyob/loyalty-sdk";

const loyalty = new LoyaltyClient(
  process.env.LOYALTY_PUBLIC_KEY,
  process.env.LOYALTY_PRIVATE_KEY,
  "https://loyalty.yowyob.com"
);

const result = await loyalty.trackEvent("purchase.completed", memberId, {
  payload: { amount: 4990 },
  idempotencyKey: \`commande-\${orderId}\`,
});`,
  Python: `from loyalty_sdk import LoyaltyClient

loyalty = LoyaltyClient(
    public_key=os.environ["LOYALTY_PUBLIC_KEY"],
    private_key=os.environ["LOYALTY_PRIVATE_KEY"],
    base_url="https://loyalty.yowyob.com",
)

result = loyalty.track_event(
    "purchase.completed",
    member_id,
    payload={"amount": 4990},
    idempotency_key=f"commande-{order_id}",
)`,
  Java: `LoyaltyClient loyalty = new LoyaltyClient(
        System.getenv("LOYALTY_PUBLIC_KEY"),
        System.getenv("LOYALTY_PRIVATE_KEY"),
        "https://loyalty.yowyob.com");

Map<String, Object> result = loyalty.trackEvent(
        "purchase.completed", memberId, null,
        Map.of("amount", 4990),
        "commande-" + orderId);`,
};

const READ_SAMPLES = {
  curl: `curl "$BASE_URL/api/v1/members/$MEMBER_ID/points" -H "X-Api-Key: sk_live_xxx"
curl "$BASE_URL/api/v1/members/$MEMBER_ID/tier"   -H "X-Api-Key: sk_live_xxx"
curl "$BASE_URL/api/v1/members/$MEMBER_ID/wallet" -H "X-Api-Key: sk_live_xxx"`,
  PHP: `$points = $loyalty->getMemberPoints($memberId);
$tier   = $loyalty->getMemberTier($memberId);
$wallet = $loyalty->getWallet($memberId);`,
  JavaScript: `const points = await loyalty.getMemberPoints(memberId);
const tier   = await loyalty.getMemberTier(memberId);
const wallet = await loyalty.getWallet(memberId);`,
  Python: `points = loyalty.get_member_points(member_id)
tier = loyalty.get_member_tier(member_id)
wallet = loyalty.get_wallet(member_id)`,
  Java: `Map<String, Object> points = loyalty.getMemberPoints(memberId);
Map<String, Object> tier   = loyalty.getMemberTier(memberId);
Map<String, Object> wallet = loyalty.getWallet(memberId);`,
};

const CALLBACK_SAMPLES = {
  PHP: `// Route POST /loyalty/callback
try {
    $event = $loyalty->checkCallbackIntegrity(
        getallheaders(),
        file_get_contents('php://input')   // corps BRUT
    );
    // $event['type'] === 'points.earned', $event['application'] === 'pk_live_…'
    http_response_code(200); echo 'OK';
} catch (\\Yowyob\\Loyalty\\Exception\\SignatureVerificationException $e) {
    http_response_code(400); echo 'KO';
}`,
  JavaScript: `// IMPORTANT : corps BRUT (pas express.json()) sur cette route
app.post("/loyalty/callback",
  express.raw({ type: "application/json" }),
  (req, res) => {
    try {
      const event = loyalty.checkCallbackIntegrity(req.headers, req.body);
      res.status(200).send("OK");
    } catch (e) {
      res.status(400).send("KO");
    }
  });`,
  Python: `@app.post("/loyalty/callback")
def loyalty_callback():
    try:
        event = loyalty.check_callback_integrity(
            request.headers, request.get_data()  # corps BRUT
        )
        return "OK", 200
    except SignatureVerificationError:
        return "KO", 400`,
  Java: `@PostMapping("/loyalty/callback")
public ResponseEntity<String> callback(@RequestHeader Map<String, String> headers,
                                       @RequestBody String rawBody) {
    try {
        Map<String, Object> event = loyalty.checkCallbackIntegrity(headers, rawBody);
        return ResponseEntity.ok("OK");
    } catch (SignatureVerificationException e) {
        return ResponseEntity.badRequest().body("KO");
    }
}`,
  pseudo: `signed_payload = timestamp + "." + corps_brut
attendu = "sha256=" + hex(hmac_sha256(webhook_secret, signed_payload))

si comparaison_temps_constant(attendu, header["X-Webhook-Signature"]) échoue
   ou |maintenant - header["X-Webhook-Timestamp"]| > 300 s :
    rejeter la requête (réponse 400 "KO")`,
};

export default function DocsPage() {
  const t = useTranslations("Developer");

  return (
    <div className="space-y-10 max-w-3xl">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">{t("docsTitle")}</h1>
        <p className="text-sm text-muted-foreground mt-1">{t("docsDescription")}</p>
      </div>

      {/* ════════ I. PRÉSENTATION ════════ */}
      <div className="space-y-4">
        <h2 className="text-lg font-semibold text-foreground">I. Présentation</h2>
        <section className="bg-card border border-border rounded-xl p-6 space-y-3">
          <p className="text-sm text-muted-foreground">
            L&apos;API Loyalty permet à vos sites web, applications mobiles et systèmes de caisse
            d&apos;alimenter votre programme de fidélité (envoi d&apos;événements) et de consulter les
            données de vos membres (points, palier, portefeuille). L&apos;intégration repose sur une{" "}
            <span className="font-medium text-foreground">Application</span> créée dans ce portail,
            qui fournit trois clés :
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 text-sm">
            {[
              ["pk_live_… / pk_test_…", "Clé publique — identifie votre application. Exposable sans risque (URL, callbacks)."],
              ["sk_live_… / sk_test_…", "Clé privée — authentifie vos serveurs (en-tête X-Api-Key). À garder SECRÈTE."],
              ["whsec_…", "Secret webhook — signe les callbacks que nous vous envoyons. À garder SECRET."],
            ].map(([code, desc]) => (
              <div key={code} className="border border-border rounded-md p-3">
                <code className="text-xs font-mono text-primary">{code}</code>
                <p className="text-xs text-muted-foreground mt-1">{desc}</p>
              </div>
            ))}
          </div>
        </section>
      </div>

      {/* ════════ II. INTÉGRATION ════════ */}
      <div className="space-y-4">
        <h2 className="text-lg font-semibold text-foreground">II. Intégration</h2>

        <Step n="1)" title="Prérequis : créer une application">
          <p className="text-sm text-muted-foreground">
            Dans <span className="font-medium text-foreground">Portail → Applications</span>,
            créez une application en renseignant : un nom, l&apos;URL de votre site, une URL de
            callback (optionnelle) et les événements auxquels vous souhaitez être notifié. À la
            création, la <span className="font-medium text-foreground">clé privée</span> et le{" "}
            <span className="font-medium text-foreground">secret webhook</span> ne sont affichés
            qu&apos;<span className="font-medium text-foreground">une seule fois</span> : stockez-les
            immédiatement dans les variables d&apos;environnement de votre serveur. Commencez en mode{" "}
            <code className="text-xs bg-muted px-1 py-0.5 rounded">TEST</code>, passez en{" "}
            <code className="text-xs bg-muted px-1 py-0.5 rounded">LIVE</code> pour la production.
          </p>
        </Step>

        <Step n="2)" title="Installer un SDK (recommandé)">
          <p className="text-sm text-muted-foreground">
            Des SDK officiels sont disponibles dans le dépôt (<code className="text-xs bg-muted px-1 py-0.5 rounded">sdk/</code>) pour{" "}
            <span className="font-medium text-foreground">PHP</span> (Composer),{" "}
            <span className="font-medium text-foreground">JavaScript/TypeScript</span> (npm),{" "}
            <span className="font-medium text-foreground">Python</span> (pip) et{" "}
            <span className="font-medium text-foreground">Java</span> (Maven). Ils gèrent
            l&apos;authentification, l&apos;idempotence et la vérification des signatures. Vous pouvez
            aussi appeler l&apos;API REST directement (voir les exemples curl).
          </p>
        </Step>

        <Step n="3)" title="Envoyer un événement">
          <p className="text-sm text-muted-foreground">
            Chaque action de vos clients (achat, trajet, inscription…) devient un événement envoyé à{" "}
            <code className="text-xs bg-muted px-1 py-0.5 rounded">
              POST /api/v1/apps/{"{clé_publique}"}/events
            </code>
            , authentifié par la clé privée. Le moteur de règles évalue l&apos;événement et applique
            les effets (crédit de points, récompense, changement de palier…). Fournissez toujours une{" "}
            <code className="text-xs bg-muted px-1 py-0.5 rounded">Idempotency-Key</code> : en cas de
            retry réseau, l&apos;événement ne sera traité qu&apos;une fois.
          </p>
          <CodeTabs samples={TRACK_EVENT_SAMPLES} />
        </Step>

        <Step n="4)" title="Consulter points, palier et portefeuille">
          <p className="text-sm text-muted-foreground">
            Affichez le solde de fidélité dans votre propre interface :
          </p>
          <CodeTabs samples={READ_SAMPLES} />
        </Step>

        <Step n="5)" title="Gérer les callbacks (webhooks)">
          <p className="text-sm text-muted-foreground">
            À chaque événement de fidélité (
            {["points.earned", "points.redeemed", "reward.granted", "reward.redeemed", "tier.changed"].map(
              (c, i) => (
                <span key={c}>
                  {i > 0 && ", "}
                  <code className="text-xs bg-muted px-1 py-0.5 rounded">{c}</code>
                </span>
              )
            )}
            ), la plateforme envoie un POST signé à votre URL de callback :
          </p>
          <pre className={CODE_BLOCK}>{`{
  "id": "…",                       // identifiant de livraison
  "type": "points.earned",
  "createdAt": "2026-07-17T10:00:02Z",
  "application": "pk_live_…",      // clé publique de votre application
  "data": { … }                    // données de l'événement
}`}</pre>
          <p className="text-sm text-muted-foreground">
            En-têtes : <code className="text-xs bg-muted px-1 py-0.5 rounded">X-Webhook-Signature</code>{" "}
            (<code className="text-xs bg-muted px-1 py-0.5 rounded">sha256=&lt;hex&gt;</code>),{" "}
            <code className="text-xs bg-muted px-1 py-0.5 rounded">X-Webhook-Timestamp</code> (epoch
            secondes), <code className="text-xs bg-muted px-1 py-0.5 rounded">X-Webhook-Id</code>.{" "}
            <span className="font-medium text-foreground">
              Vérifiez toujours la signature avant de traiter
            </span>{" "}
            — elle utilise le <span className="font-medium text-foreground">secret webhook</span>{" "}
            (whsec_…), pas la clé privée. Répondez un statut 2xx (« OK ») ; toute autre réponse est
            retentée jusqu&apos;à 6 fois avec backoff exponentiel (30 s, 60 s, 120 s…).
          </p>
          <CodeTabs samples={CALLBACK_SAMPLES} />
        </Step>
      </div>

      {/* ════════ III. RECOMMANDATIONS DE SÉCURITÉ ════════ */}
      <div className="space-y-4">
        <h2 className="text-lg font-semibold text-foreground">III. Recommandations de sécurité</h2>
        <section className="bg-card border border-border rounded-xl p-6 space-y-3">
          <ul className="space-y-2 text-sm text-muted-foreground list-disc pl-5">
            <li>
              <span className="font-medium text-foreground">Confidentialité des clés</span> — la clé
              publique est exposable ; la clé privée et le secret webhook ne doivent jamais apparaître
              dans un dépôt Git, un navigateur ou une application mobile. Utilisez des variables
              d&apos;environnement côté serveur.
            </li>
            <li>
              <span className="font-medium text-foreground">HTTPS obligatoire</span> — servez votre URL
              de callback derrière un certificat SSL valide, et choisissez un chemin difficile à
              deviner (ex. <code className="text-xs bg-muted px-1 py-0.5 rounded">/callback/jkdKo0Lp8lsdfjk4j0H</code>).
            </li>
            <li>
              <span className="font-medium text-foreground">Intégrité des callbacks</span> — recalculez
              la signature HMAC et comparez-la en temps constant ; rejetez tout horodatage à plus de
              300 s (anti-rejeu). Les SDK le font via{" "}
              <code className="text-xs bg-muted px-1 py-0.5 rounded">checkCallbackIntegrity</code>.
            </li>
            <li>
              <span className="font-medium text-foreground">Idempotence</span> — envoyez une{" "}
              <code className="text-xs bg-muted px-1 py-0.5 rounded">Idempotency-Key</code> unique par
              action métier : vos retries ne créditeront jamais deux fois.
            </li>
            <li>
              <span className="font-medium text-foreground">Rotation</span> — en cas de fuite
              soupçonnée, régénérez immédiatement la clé privée ou le secret webhook depuis la page
              Applications ; l&apos;ancienne valeur est révoquée aussitôt.
            </li>
          </ul>
        </section>
      </div>

      <section className="bg-card border border-border rounded-xl p-6 space-y-3">
        <h2 className="text-sm font-semibold text-foreground">Référence API complète</h2>
        <p className="text-sm text-muted-foreground">
          La référence OpenAPI de tous les endpoints est disponible dans Swagger UI.
        </p>
        <a
          href="/swagger-ui.html"
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-2 text-sm text-primary hover:underline"
        >
          Ouvrir Swagger UI
          <ExternalLink className="w-3.5 h-3.5" />
        </a>
      </section>
    </div>
  );
}
