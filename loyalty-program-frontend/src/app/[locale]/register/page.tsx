"use client";

import { useState } from "react";
import { Link, useRouter } from "@/i18n/routing";
import { Gift, AlertTriangle, Mail } from "lucide-react";
import { useTranslations } from "next-intl";
import { LandingHeader } from "@/components/LandingHeader";

export default function RegisterPage() {
  const [formData, setFormData] = useState({
    email: "",
    lastName: "",
    firstName: "",
    username: "",
    password: "",
    confirmPassword: "",
    acceptTerms: true,
  });

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const router = useRouter();
  const t = useTranslations("Register");

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type } = e.target;
    if (type === "checkbox") {
      const target = e.target as HTMLInputElement;
      setFormData((prev) => ({ ...prev, [name]: target.checked }));
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!formData.email.trim()) {
      setError(t("emailPlaceholder") + " is required");
      return;
    }
    if (!formData.lastName.trim() || !formData.firstName.trim()) {
      setError("Name fields are required");
      return;
    }
    if (!formData.username.trim() || !formData.password.trim()) {
      setError("Username and Password are required");
      return;
    }
    if (formData.password !== formData.confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    if (!formData.acceptTerms) {
      setError("You must accept the terms");
      return;
    }

    setIsLoading(true);
    try {
      // Fake API Call for UI implementation
      await new Promise((resolve) => setTimeout(resolve, 1500));
      // For now, redirect to login
      router.push("/login");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-6 pt-24 relative overflow-hidden bg-background">
      <LandingHeader />

      <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none opacity-[0.4]">
        <div className="absolute -top-64 -left-64 w-[800px] h-[800px] bg-secondary rounded-full blur-3xl mix-blend-multiply" />
        <div className="absolute -bottom-64 -right-64 w-[600px] h-[600px] bg-[#d7ccc8] rounded-full blur-3xl mix-blend-multiply opacity-50" />
      </div>

      <div className="w-full max-w-lg z-10 space-y-6 bg-card p-10 rounded-xl shadow-xl shadow-primary/5 border border-border mt-12 mb-12">
        <div className="space-y-3 text-center">
          <Link href="/">
            <div className="mx-auto w-16 h-16 rounded-2xl bg-secondary flex items-center justify-center mb-4 shadow-sm border border-border cursor-pointer hover:bg-secondary/80 transition-colors">
              <Gift className="w-8 h-8 text-primary" />
            </div>
          </Link>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">{t("title")}</h1>
        </div>

        {error && (
          <div className="bg-destructive/10 border border-destructive/20 text-destructive px-4 py-3 rounded-lg text-sm font-medium flex items-center gap-2">
            <AlertTriangle className="w-5 h-5 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleRegister} className="space-y-4 pt-2">
          <div className="space-y-2 relative">
            <span className="absolute left-3 top-3.5 text-muted-foreground/60 z-10">
              <Mail className="w-5 h-5" />
            </span>
            <input
              type="email"
              name="email"
              placeholder={t("emailPlaceholder")}
              value={formData.email}
              onChange={handleChange}
              className="flex h-12 w-full rounded-lg border border-border bg-background pl-10 pr-4 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <input
                type="text"
                name="firstName"
                placeholder={t("firstName")}
                value={formData.firstName}
                onChange={handleChange}
                className="flex h-12 w-full rounded-lg border border-border bg-background px-4 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary"
              />
            </div>
            <div className="space-y-2">
              <input
                type="text"
                name="lastName"
                placeholder={t("lastName")}
                value={formData.lastName}
                onChange={handleChange}
                className="flex h-12 w-full rounded-lg border border-border bg-background px-4 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary"
              />
            </div>
          </div>

          <div className="space-y-1">
            <input
              type="text"
              name="username"
              placeholder={t("username")}
              value={formData.username}
              onChange={handleChange}
              className="flex h-12 w-full rounded-lg border border-border bg-background px-4 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary"
            />
            <p className="text-[11px] text-muted-foreground/80 pl-1 pt-1 leading-tight">
              {t("usernameHint")}
            </p>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <input
                type="password"
                name="password"
                placeholder={t("password")}
                value={formData.password}
                onChange={handleChange}
                className="flex h-12 w-full rounded-lg border border-border bg-background px-4 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary"
              />
            </div>
            <div className="space-y-2">
              <input
                type="password"
                name="confirmPassword"
                placeholder={t("confirmPassword")}
                value={formData.confirmPassword}
                onChange={handleChange}
                className="flex h-12 w-full rounded-lg border border-border bg-background px-4 py-2 text-sm shadow-sm transition-all placeholder:text-muted-foreground/50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/20 focus-visible:border-primary"
              />
            </div>
          </div>

          <div className="pt-2">
            <label className="flex items-start gap-3 cursor-pointer group">
              <input
                type="checkbox"
                name="acceptTerms"
                checked={formData.acceptTerms}
                onChange={handleChange}
                className="mt-1 w-4 h-4 rounded border-border text-primary focus:ring-primary/20 accent-primary"
              />
              <span className="text-sm text-muted-foreground leading-snug group-hover:text-foreground transition-colors">
                {t("acceptTerms")}
              </span>
            </label>
          </div>

          <div className="pt-4">
            <button
              type="submit"
              disabled={isLoading}
              className="flex-1 w-full inline-flex items-center justify-center whitespace-nowrap rounded-lg text-sm font-medium transition-all shadow-md bg-primary text-primary-foreground hover:bg-primary/90 h-12 px-4 disabled:pointer-events-none disabled:opacity-50 active:scale-[0.98]"
            >
              {isLoading ? "..." : t("openAccount")}
            </button>
          </div>
        </form>

        <div className="pt-2 text-center">
          <Link href="/login" className="text-sm font-medium text-primary hover:underline underline-offset-4">
            {t("loginInstead")}
          </Link>
        </div>
      </div>
    </main>
  );
}
