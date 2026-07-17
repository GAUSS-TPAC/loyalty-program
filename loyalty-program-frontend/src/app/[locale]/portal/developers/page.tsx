"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { UserPlus, ShieldAlert, CheckCircle2, AlertTriangle } from "lucide-react";
import { useAccess } from "@/hooks/useBackend";
import { developerInviteApi } from "@/lib/api";

export default function DevelopersPage() {
  const t = useTranslations("Developer");
  const { data: access, isLoading: accessLoading } = useAccess();
  const isAdmin = access?.tenantAdmin ?? false;

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [inviting, setInviting] = useState(false);
  const [successEmail, setSuccessEmail] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleInvite = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!firstName.trim() || !lastName.trim() || !email.trim()) return;
    setInviting(true);
    setError(null);
    try {
      await developerInviteApi.invite({
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: email.trim(),
      });
      setSuccessEmail(email.trim());
      setFirstName("");
      setLastName("");
      setEmail("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Échec de l'invitation.");
    } finally {
      setInviting(false);
    }
  };

  if (accessLoading) {
    return <div className="h-40 bg-muted animate-pulse rounded-xl" />;
  }

  if (!isAdmin) {
    return (
      <div className="bg-card border border-border rounded-xl p-8 text-center space-y-3">
        <ShieldAlert className="w-8 h-8 mx-auto text-muted-foreground" />
        <p className="text-sm text-muted-foreground">{t("adminOnlyNotice")}</p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-semibold text-foreground">{t("inviteDeveloperTitle")}</h1>
        <p className="text-sm text-muted-foreground mt-1">{t("inviteDeveloperDescription")}</p>
      </div>

      {successEmail && (
        <div className="bg-emerald-500/10 border border-emerald-500/30 rounded-xl p-5 flex items-start gap-3">
          <CheckCircle2 className="w-4 h-4 mt-0.5 text-emerald-600 shrink-0" />
          <p className="text-sm text-emerald-700">{t("inviteSuccess", { email: successEmail })}</p>
        </div>
      )}

      {error && (
        <div className="bg-destructive/10 border border-destructive/30 rounded-xl p-5 flex items-start gap-3">
          <AlertTriangle className="w-4 h-4 mt-0.5 text-destructive shrink-0" />
          <p className="text-sm text-destructive">{error}</p>
        </div>
      )}

      <form
        onSubmit={handleInvite}
        className="bg-card border border-border rounded-xl p-5 flex flex-col sm:flex-row gap-3 sm:items-end"
      >
        <div className="flex-1 space-y-1.5">
          <label className="text-xs font-medium text-muted-foreground">{t("firstNameLabel")}</label>
          <input
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            placeholder="Jean"
            className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <div className="flex-1 space-y-1.5">
          <label className="text-xs font-medium text-muted-foreground">{t("lastNameLabel")}</label>
          <input
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            placeholder="Dupont"
            className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <div className="flex-1 space-y-1.5">
          <label className="text-xs font-medium text-muted-foreground">{t("emailLabel")}</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="jean.dupont@example.com"
            className="w-full px-3 py-2 text-sm rounded-md border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary"
          />
        </div>
        <button
          type="submit"
          disabled={inviting}
          className="flex items-center gap-2 px-4 py-2 rounded-md bg-primary text-primary-foreground text-sm font-medium hover:opacity-90 transition-opacity disabled:opacity-50"
        >
          <UserPlus className="w-4 h-4" />
          {t("inviteButton")}
        </button>
      </form>
    </div>
  );
}
