"use client";

import { useEffect, useState } from "react";
import { Link } from "@/i18n/routing";
import { Users, Search, ArrowRight, ShieldAlert, ShieldCheck } from "lucide-react";
import { useTranslations } from "next-intl";

interface Member {
  id: string;
  name: string;
  email: string;
  balance: number;
  status: string;
  referrals: string[];
  referredBy?: string | null;
}

const DEFAULT_MEMBERS: Member[] = [
  { id: "usr_9921", name: "Amandine Dubois", email: "amandine@example.com", balance: 5400, status: "ACTIVE", referrals: [] },
  { id: "usr_9922", name: "Jean Dupont", email: "jean.dupont@example.com", balance: 1200, status: "PENDING_KYC", referrals: [] },
  { id: "usr_9923", name: "Alice Martin", email: "alice.martin@example.com", balance: 850, status: "ACTIVE", referrals: [] },
  { id: "usr_9924", name: "Paul Lambert", email: "paul.lambert@example.com", balance: 0, status: "FROZEN", referrals: [] }
];

export default function MembersDirectory() {
  const t = useTranslations("Members");
  const [members, setMembers] = useState<Member[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [isLoaded, setIsLoaded] = useState(false);

  useEffect(() => {
    const saved = localStorage.getItem("loyalty_members");
    if (saved) {
      try {
        setMembers(JSON.parse(saved));
      } catch (e) {
        setMembers(DEFAULT_MEMBERS);
      }
    } else {
      setMembers(DEFAULT_MEMBERS);
      localStorage.setItem("loyalty_members", JSON.stringify(DEFAULT_MEMBERS));
    }
    setIsLoaded(true);
  }, []);

  const filteredMembers = members.filter(
    (m) =>
      m.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      m.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      m.id.toLowerCase().includes(searchQuery.toLowerCase())
  );

  if (!isLoaded) {
    return <div className="space-y-6 animate-pulse" />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-semibold tracking-tight">{t("title")}</h1>
          <p className="text-muted-foreground text-sm">{t("description")}</p>
        </div>

        <div className="relative w-full md:w-72">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <input
            type="text"
            placeholder={t("search")}
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-card border border-border rounded-lg pl-9 pr-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary shadow-sm transition-all"
          />
        </div>
      </div>

      <div className="border border-border bg-card rounded-xl overflow-hidden shadow-sm mt-6">
        <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-3">
          <Users className="w-5 h-5 text-primary" />
          <h3 className="font-medium text-foreground">
            {t("registered")} ({filteredMembers.length})
          </h3>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left">
            <thead className="text-xs text-muted-foreground uppercase bg-muted/50 border-b border-border">
              <tr>
                <th className="px-6 py-4 font-semibold tracking-wider">{t("memberId")}</th>
                <th className="px-6 py-4 font-semibold tracking-wider">{t("identity")}</th>
                <th className="px-6 py-4 font-semibold tracking-wider text-right">{t("walletBalance")}</th>
                <th className="px-6 py-4 font-semibold tracking-wider">{t("status")}</th>
                <th className="px-6 py-4 font-semibold tracking-wider text-right">{t("action")}</th>
              </tr>
            </thead>
            <tbody>
              {filteredMembers.map((member, index) => (
                <tr
                  key={member.id}
                  className={`border-b border-border/50 hover:bg-secondary/50 transition-colors ${
                    index % 2 === 0 ? "bg-background" : "bg-muted/10"
                  }`}
                >
                  <td className="px-6 py-4 font-mono text-muted-foreground text-xs">{member.id}</td>
                  <td className="px-6 py-4">
                    <div className="font-medium text-foreground">{member.name}</div>
                    <div className="text-xs text-muted-foreground">{member.email}</div>
                  </td>
                  <td className="px-6 py-4 font-mono text-primary font-medium text-right">
                    {member.balance.toLocaleString()} CR
                  </td>
                  <td className="px-6 py-4">
                    {member.status === "ACTIVE" && (
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-medium bg-green-100 text-green-700 border border-green-200">
                        <ShieldCheck className="w-3 h-3" /> {t("active")}
                      </span>
                    )}
                    {member.status === "PENDING_KYC" && (
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-medium bg-orange-100 text-orange-700 border border-orange-200">
                        <ShieldAlert className="w-3 h-3" /> {t("pendingKyc")}
                      </span>
                    )}
                    {member.status === "FROZEN" && (
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-medium bg-destructive/10 text-destructive border border-destructive/20">
                        <ShieldAlert className="w-3 h-3" /> {t("frozen")}
                      </span>
                    )}
                  </td>
                  <td className="px-6 py-4 text-right">
                    <Link
                      href={`/portal/members/${member.id}`}
                      className="inline-flex items-center gap-2 text-xs font-medium text-primary hover:text-primary/80 transition-colors"
                    >
                      {t("manage")}
                      <ArrowRight className="w-4 h-4" />
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
