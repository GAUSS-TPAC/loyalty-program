"use client";

import { useTranslations } from "next-intl";
import { ExternalLink } from "lucide-react";

const CODE_BLOCK = "bg-muted border border-border rounded-md p-4 text-xs font-mono overflow-x-auto";

export default function DocsPage() {
  const t = useTranslations("Developer");

  return (
    <div className="space-y-8 max-w-3xl">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">{t("docsTitle")}</h1>
        <p className="text-sm text-muted-foreground mt-1">{t("docsDescription")}</p>
      </div>

      <section className="bg-card border border-border rounded-xl p-6 space-y-3">
        <h2 className="text-sm font-semibold text-foreground">Authentication</h2>
        <p className="text-sm text-muted-foreground">
          Your backend systems authenticate to the Loyalty API using an API key, sent in the{" "}
          <code className="text-xs bg-muted px-1 py-0.5 rounded">X-Api-Key</code> header. Create keys in the{" "}
          <span className="font-medium">API Keys</span> section. Use a <code>TEST</code> mode key while
          integrating, and switch to a <code>LIVE</code> key in production.
        </p>
        <pre className={CODE_BLOCK}>{`curl -X POST /api/v1/events \\
  -H "X-Api-Key: lk_live_..." \\
  -H "Content-Type: application/json" \\
  -d '{"eventType":"purchase.completed","memberId":"usr_123","amount":49.90}'`}</pre>
      </section>

      <section className="bg-card border border-border rounded-xl p-6 space-y-3">
        <h2 className="text-sm font-semibold text-foreground">Event catalog</h2>
        <p className="text-sm text-muted-foreground">
          Webhooks can be subscribed to the following event types:
        </p>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 text-sm">
          {[
            ["points.earned", "A member was credited loyalty points"],
            ["points.redeemed", "A member spent loyalty points"],
            ["reward.granted", "A reward was granted to a member"],
            ["reward.redeemed", "A member redeemed a granted reward"],
            ["tier.changed", "A member's loyalty tier changed"],
          ].map(([code, desc]) => (
            <div key={code} className="border border-border rounded-md p-3">
              <code className="text-xs font-mono text-primary">{code}</code>
              <p className="text-xs text-muted-foreground mt-1">{desc}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="bg-card border border-border rounded-xl p-6 space-y-3">
        <h2 className="text-sm font-semibold text-foreground">Verifying webhook signatures</h2>
        <p className="text-sm text-muted-foreground">
          Every webhook request includes <code className="text-xs bg-muted px-1 py-0.5 rounded">X-Webhook-Signature</code>,{" "}
          <code className="text-xs bg-muted px-1 py-0.5 rounded">X-Webhook-Timestamp</code>, and{" "}
          <code className="text-xs bg-muted px-1 py-0.5 rounded">X-Webhook-Id</code> headers. Recompute the
          signature and compare it to reject forged requests.
        </p>
        <pre className={CODE_BLOCK}>{`signed_payload = timestamp + "." + raw_request_body
expected_signature = hex(hmac_sha256(webhook_secret, signed_payload))

if not constant_time_equals(expected_signature, header["X-Webhook-Signature"].removeprefix("sha256=")):
    reject_request()`}</pre>
      </section>

      <section className="bg-card border border-border rounded-xl p-6 space-y-3">
        <h2 className="text-sm font-semibold text-foreground">Full API reference</h2>
        <p className="text-sm text-muted-foreground">
          The complete OpenAPI reference for every endpoint is available in Swagger UI.
        </p>
        <a
          href="/swagger-ui.html"
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-2 text-sm text-primary hover:underline"
        >
          Open Swagger UI
          <ExternalLink className="w-3.5 h-3.5" />
        </a>
      </section>
    </div>
  );
}
