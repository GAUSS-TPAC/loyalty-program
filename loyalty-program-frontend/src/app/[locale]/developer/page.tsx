"use client";

import { useTranslations } from "next-intl";
import { Link } from "@/i18n/routing";
import { Key, Webhook, Terminal, ArrowRight } from "lucide-react";
import { useApiKeys, useWebhooks, useWebhookDeliveries } from "@/hooks/useBackend";

export default function DeveloperDashboardPage() {
  const t = useTranslations("Developer");
  const { data: apiKeys } = useApiKeys();
  const { data: webhooks } = useWebhooks();
  const { data: deliveries } = useWebhookDeliveries(0, 5);

  const activeKeys = apiKeys?.filter((k) => k.active).length ?? 0;
  const activeWebhooks = webhooks?.filter((w) => w.active).length ?? 0;

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">{t("dashboardTitle")}</h1>
        <p className="text-sm text-muted-foreground mt-1">{t("dashboardDescription")}</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard icon={Key} label={t("navApiKeys")} value={activeKeys} href="/developer/api-keys" />
        <StatCard icon={Webhook} label={t("navWebhooks")} value={activeWebhooks} href="/developer/webhooks" />
        <StatCard
          icon={Terminal}
          label={t("navLogs")}
          value={deliveries?.length ?? 0}
          href="/developer/logs"
        />
      </div>

      <div className="bg-card border border-border rounded-xl p-6">
        <h2 className="text-sm font-semibold text-foreground mb-4">Recent Webhook Deliveries</h2>
        {!deliveries || deliveries.length === 0 ? (
          <p className="text-sm text-muted-foreground">No deliveries yet.</p>
        ) : (
          <div className="space-y-2">
            {deliveries.map((d) => (
              <div
                key={d.id}
                className="flex items-center justify-between text-sm py-2 border-b border-border last:border-0"
              >
                <span className="font-mono text-xs text-muted-foreground">{d.eventType}</span>
                <StatusBadge status={d.status} />
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

function StatCard({
  icon: Icon,
  label,
  value,
  href,
}: {
  icon: React.ElementType;
  label: string;
  value: number;
  href: string;
}) {
  return (
    <Link
      href={href}
      className="bg-card border border-border rounded-xl p-5 hover:border-primary/50 transition-colors group"
    >
      <div className="flex items-center justify-between">
        <div className="w-9 h-9 rounded-lg bg-secondary flex items-center justify-center border border-border">
          <Icon className="w-4.5 h-4.5 text-primary" />
        </div>
        <ArrowRight className="w-4 h-4 text-muted-foreground group-hover:translate-x-0.5 transition-transform" />
      </div>
      <div className="mt-3">
        <div className="text-2xl font-semibold text-foreground">{value}</div>
        <div className="text-xs text-muted-foreground">{label}</div>
      </div>
    </Link>
  );
}

function StatusBadge({ status }: { status: string }) {
  const colors: Record<string, string> = {
    SUCCEEDED: "bg-green-500/10 text-green-600 border-green-500/20",
    PENDING: "bg-yellow-500/10 text-yellow-600 border-yellow-500/20",
    FAILED: "bg-red-500/10 text-red-600 border-red-500/20",
    EXHAUSTED: "bg-red-500/10 text-red-700 border-red-500/20",
  };
  return (
    <span className={`text-xs px-2 py-0.5 rounded-full border ${colors[status] ?? ""}`}>{status}</span>
  );
}
