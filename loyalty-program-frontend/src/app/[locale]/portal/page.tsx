"use client";

import { useEffect, useState } from "react";
import { Link } from "@/i18n/routing";
import { 
  Users, 
  Coins, 
  Share2, 
  Sparkles, 
  TrendingUp, 
  ArrowUpRight, 
  Activity, 
  ArrowRight, 
  UserCheck, 
  ShoppingBag, 
  Clock, 
  CheckCircle2, 
  ShieldCheck,
  Settings
} from "lucide-react";
import { useTranslations } from "next-intl";
import { ResponsiveContainer, AreaChart, Area, XAxis, Tooltip } from "recharts";

interface Member {
  id: string;
  name: string;
  email: string;
  balance: number;
  status: string;
  referrals: string[];
  referredBy?: string | null;
}

interface ActivityItem {
  id: string;
  type: "signup" | "purchase" | "referral" | "system";
  title: string;
  description: string;
  timestamp: string;
  points?: number;
  memberName?: string;
  avatarInitials?: string;
}

const DEFAULT_MEMBERS: Member[] = [
  { id: "usr_9921", name: "Amandine Dubois", email: "amandine@example.com", balance: 5400, status: "ACTIVE", referrals: [] },
  { id: "usr_9922", name: "Jean Dupont", email: "jean.dupont@example.com", balance: 1200, status: "PENDING_KYC", referrals: [] },
  { id: "usr_9923", name: "Alice Martin", email: "alice.martin@example.com", balance: 850, status: "ACTIVE", referrals: [] },
  { id: "usr_9924", name: "Paul Lambert", email: "paul.lambert@example.com", balance: 0, status: "FROZEN", referrals: [] }
];

export default function OverviewDashboard() {
  const t = useTranslations("Dashboard");

  // Client side states
  const [members, setMembers] = useState<Member[]>([]);
  const [pointValue, setPointValue] = useState<number>(100);
  const [activeRulesCount, setActiveRulesCount] = useState<number>(0);
  const [activities, setActivities] = useState<ActivityItem[]>([]);
  const [isLoaded, setIsLoaded] = useState(false);

  useEffect(() => {
    // Load members
    const savedMembers = localStorage.getItem("loyalty_members");
    let currentMembers = DEFAULT_MEMBERS;
    if (savedMembers) {
      try {
        currentMembers = JSON.parse(savedMembers);
      } catch (e) {
        currentMembers = DEFAULT_MEMBERS;
      }
    } else {
      localStorage.setItem("loyalty_members", JSON.stringify(DEFAULT_MEMBERS));
    }
    setMembers(currentMembers);

    // Load active rules count
    const savedRules = localStorage.getItem("loyalty_rules");
    if (savedRules) {
      try {
        const rules = JSON.parse(savedRules);
        setActiveRulesCount(rules.length);
      } catch (e) {
        setActiveRulesCount(2);
      }
    } else {
      setActiveRulesCount(2);
    }

    // Load point value
    const savedPointValue = localStorage.getItem("loyalty_point_value");
    if (savedPointValue) {
      const parsed = parseFloat(savedPointValue);
      if (!isNaN(parsed)) setPointValue(parsed);
    }

    // Generate recent dynamic activities based on loaded members database
    const recentLogs: ActivityItem[] = [];
    if (currentMembers.length > 0) {
      // 1. Transaction event from top member
      const topMember = currentMembers[0];
      recentLogs.push({
        id: "act_1",
        type: "purchase",
        title: "Purchase Bonus Issued",
        description: `Awarded transaction points to ${topMember.name}`,
        timestamp: "5m ago",
        points: 120,
        memberName: topMember.name,
        avatarInitials: topMember.name.split(" ").map(n => n[0]).join("")
      });

      // 2. Referral event if any, or general signup
      const referredMember = currentMembers.find(m => m.referredBy);
      if (referredMember) {
        const referrer = currentMembers.find(m => m.id === referredMember.referredBy) || topMember;
        recentLogs.push({
          id: "act_2",
          type: "referral",
          title: "Referral Reward Credited",
          description: `${referredMember.name} joined via referral code from ${referrer.name}`,
          timestamp: "45m ago",
          points: 100,
          memberName: referredMember.name,
          avatarInitials: referredMember.name.split(" ").map(n => n[0]).join("")
        });
      } else {
        const secondaryMember = currentMembers[1] || topMember;
        recentLogs.push({
          id: "act_2",
          type: "signup",
          title: "Welcome Bonus Credited",
          description: `New registration welcome reward for ${secondaryMember.name}`,
          timestamp: "1h ago",
          points: 50,
          memberName: secondaryMember.name,
          avatarInitials: secondaryMember.name.split(" ").map(n => n[0]).join("")
        });
      }

      // 3. System checks
      recentLogs.push({
        id: "act_3",
        type: "system",
        title: "Bonification Campaign Synced",
        description: "Rule evaluation parameters updated successfully.",
        timestamp: "3h ago"
      });

      // 4. Wallet adjustments
      const lastMember = currentMembers[currentMembers.length - 1];
      recentLogs.push({
        id: "act_4",
        type: "signup",
        title: "Member Profile Created",
        description: `Wallet status initialized for ${lastMember.name}`,
        timestamp: "5h ago",
        memberName: lastMember.name,
        avatarInitials: lastMember.name.split(" ").map(n => n[0]).join("")
      });
    }
    setActivities(recentLogs);

    setIsLoaded(true);
  }, []);

  // Compute metrics
  const totalMembersCount = members.length;
  const totalIssuedPoints = members.reduce((sum, m) => sum + m.balance, 0);
  const referralCount = members.filter((m) => m.referredBy).length;



  // Points history for Recharts Area chart
  const pointsHistoryData = [
    { day: "Mon", points: Math.round(totalIssuedPoints * 0.72) },
    { day: "Tue", points: Math.round(totalIssuedPoints * 0.78) },
    { day: "Wed", points: Math.round(totalIssuedPoints * 0.85) },
    { day: "Thu", points: Math.round(totalIssuedPoints * 0.91) },
    { day: "Fri", points: totalIssuedPoints },
  ];

  if (!isLoaded) {
    return (
      <div className="space-y-6 animate-pulse">
        <div className="h-32 bg-muted rounded-xl w-full" />
        <div className="grid grid-cols-4 gap-6">
          <div className="h-24 bg-muted rounded-xl" />
          <div className="h-24 bg-muted rounded-xl" />
          <div className="h-24 bg-muted rounded-xl" />
          <div className="h-24 bg-muted rounded-xl" />
        </div>
        <div className="h-64 bg-muted rounded-xl w-full" />
      </div>
    );
  }

  return (
    <div className="space-y-8">
      
      {/* Hero Welcome Banner */}
      <div className="relative overflow-hidden rounded-2xl border border-border bg-gradient-to-r from-primary/10 via-primary/5 to-card p-6 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-6 group hover:shadow-md transition-all duration-300">
        <div className="absolute top-0 right-0 w-[300px] h-[300px] bg-primary/5 rounded-full blur-3xl pointer-events-none -mr-32 -mt-32" />
        <div className="space-y-2 z-10">
          <div className="flex items-center gap-2.5">
            <span className="relative flex h-2.5 w-2.5">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500"></span>
            </span>
            <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground">{t("systemStatus")}: {t("connected")}</span>
          </div>
          <h2 className="text-2xl font-bold tracking-tight text-foreground">{t("heroTitle")}</h2>
          <p className="text-sm text-muted-foreground max-w-2xl">{t("heroSubtitle")}</p>
        </div>
        <div className="flex gap-3 z-10 shrink-0">
          <Link
            href="/portal/rules"
            className="inline-flex items-center gap-2 text-xs font-semibold bg-primary text-primary-foreground hover:bg-primary/95 px-4.5 py-2.5 rounded-lg shadow-sm hover:shadow transition-all active:scale-[0.98]"
          >
            <Settings className="w-3.5 h-3.5" />
            {t("configureRules")}
          </Link>
          <Link
            href="/portal/members"
            className="inline-flex items-center gap-2 text-xs font-semibold bg-background border border-border hover:bg-secondary text-foreground px-4.5 py-2.5 rounded-lg shadow-sm hover:shadow transition-all active:scale-[0.98]"
          >
            <Users className="w-3.5 h-3.5 text-muted-foreground" />
            {t("viewMembers")}
          </Link>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {/* Card 1: Total Members */}
        <div className="border border-border bg-card p-6 rounded-xl shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300 flex items-center justify-between group">
          <div className="space-y-2">
            <p className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">{t("totalMembers")}</p>
            <p className="text-3xl font-bold text-foreground font-mono">{totalMembersCount}</p>
            <div className="flex items-center gap-1 text-[11px] font-semibold text-emerald-600">
              <ArrowUpRight className="w-3 h-3" />
              <span>+12.4% this week</span>
            </div>
          </div>
          <div className="w-12 h-12 rounded-xl bg-secondary flex items-center justify-center border border-border group-hover:scale-105 transition-all">
            <Users className="w-5 h-5 text-primary" />
          </div>
        </div>

        {/* Card 2: Total Issued Points */}
        <div className="border border-border bg-card p-6 rounded-xl shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300 flex items-center justify-between group">
          <div className="space-y-2">
            <p className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">{t("totalPoints")}</p>
            <p className="text-3xl font-bold text-foreground font-mono">{totalIssuedPoints.toLocaleString()} <span className="text-sm font-semibold">CR</span></p>
            <div className="flex items-center gap-1 text-[11px] font-semibold text-emerald-600">
              <ArrowUpRight className="w-3 h-3" />
              <span>+8.2% vs yesterday</span>
            </div>
          </div>
          <div className="w-12 h-12 rounded-xl bg-secondary flex items-center justify-center border border-border group-hover:scale-105 transition-all">
            <Sparkles className="w-5 h-5 text-primary" />
          </div>
        </div>

        {/* Card 3: Referral Rate */}
        <div className="border border-border bg-card p-6 rounded-xl shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300 flex items-center justify-between group">
          <div className="space-y-2">
            <p className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">{t("referralRate")}</p>
            <p className="text-3xl font-bold text-foreground font-mono">{referralCount}</p>
            <div className="text-[11px] text-muted-foreground font-medium">
              <span>{(totalMembersCount > 0 ? (referralCount / totalMembersCount * 100).toFixed(0) : 0)}% of members active</span>
            </div>
          </div>
          <div className="w-12 h-12 rounded-xl bg-secondary flex items-center justify-center border border-border group-hover:scale-105 transition-all">
            <Share2 className="w-5 h-5 text-primary" />
          </div>
        </div>

        {/* Card 4: Point Value */}
        <div className="border border-border bg-card p-6 rounded-xl shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300 flex items-center justify-between group">
          <div className="space-y-2">
            <p className="text-xs font-semibold uppercase text-muted-foreground tracking-wider">{t("pointValue")}</p>
            <p className="text-3xl font-bold text-foreground font-mono">{pointValue.toFixed(2)} <span className="text-sm font-semibold text-muted-foreground">FCFA</span></p>
            <div className="text-[11px] text-muted-foreground font-medium">
              <span>Conversion exchange value</span>
            </div>
          </div>
          <div className="w-12 h-12 rounded-xl bg-secondary flex items-center justify-center border border-border group-hover:scale-105 transition-all">
            <Coins className="w-5 h-5 text-primary" />
          </div>
        </div>
      </div>

      {/* Visual Analytics Chart */}
      <div className="border border-border bg-card rounded-xl p-6 shadow-sm flex flex-col justify-between">
        <div className="mb-4">
          <h3 className="font-semibold text-foreground">{t("pointsDistribution")}</h3>
          <p className="text-xs text-muted-foreground">{t("pointsOverTime")}</p>
        </div>
        <div className="h-64 w-full min-w-0 relative">
          <ResponsiveContainer width="100%" height="100%" minWidth={0}>
            <AreaChart data={pointsHistoryData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
              <defs>
                <linearGradient id="colorPoints" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="var(--color-primary, #6366f1)" stopOpacity={0.2} />
                  <stop offset="95%" stopColor="var(--color-primary, #6366f1)" stopOpacity={0} />
                </linearGradient>
              </defs>
              <XAxis dataKey="day" stroke="#888888" fontSize={12} tickLine={false} axisLine={false} />
              <Tooltip
                contentStyle={{ background: "var(--background-card, #ffffff)", border: "1px solid var(--color-border, #e2e8f0)", borderRadius: "8px" }}
                labelClassName="font-semibold text-foreground text-xs"
                itemStyle={{ fontSize: "12px", color: "var(--color-primary, #6366f1)" }}
              />
              <Area type="monotone" dataKey="points" stroke="var(--color-primary, #6366f1)" strokeWidth={2} fillOpacity={1} fill="url(#colorPoints)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Bottom Section: Activities & Insights */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Dynamic Activity Feed */}
        <div className="lg:col-span-2 border border-border bg-card rounded-xl shadow-sm flex flex-col overflow-hidden">
          <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
            <Activity className="w-5 h-5 text-primary" />
            <div>
              <h3 className="font-semibold text-foreground">{t("activityTitle")}</h3>
              <p className="text-xs text-muted-foreground">{t("activitySubtitle")}</p>
            </div>
          </div>
          
          <div className="divide-y divide-border flex-1">
            {activities.length === 0 ? (
              <div className="p-8 text-center text-muted-foreground text-sm italic">
                No recent campaign activity.
              </div>
            ) : (
              activities.map((act) => (
                <div key={act.id} className="px-6 py-4 flex items-center justify-between hover:bg-muted/10 transition-colors group">
                  <div className="flex items-center gap-3.5">
                    {act.avatarInitials ? (
                      <div className="w-9 h-9 rounded-full bg-secondary border border-border flex items-center justify-center text-xs font-semibold text-primary">
                        {act.avatarInitials}
                      </div>
                    ) : (
                      <div className="w-9 h-9 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                        <Activity className="w-4 h-4" />
                      </div>
                    )}
                    
                    <div className="space-y-0.5">
                      <p className="text-sm font-semibold text-foreground group-hover:text-primary transition-colors">{act.title}</p>
                      <p className="text-xs text-muted-foreground">{act.description}</p>
                    </div>
                  </div>
                  
                  <div className="flex items-center gap-3">
                    {act.points !== undefined && (
                      <span className="inline-flex items-center text-xs font-bold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-100">
                        +{act.points} CR
                      </span>
                    )}
                    <span className="text-xs text-muted-foreground flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      {act.timestamp}
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Campaign Program Health & Fast Links */}
        <div className="border border-border bg-card rounded-xl shadow-sm overflow-hidden flex flex-col justify-between">
          <div>
            <div className="bg-secondary px-6 py-4 border-b border-border flex items-center gap-2.5">
              <ShieldCheck className="w-5 h-5 text-primary" />
              <div>
                <h3 className="font-semibold text-foreground">{t("healthCheckTitle")}</h3>
                <p className="text-xs text-muted-foreground">{t("healthCheckSubtitle")}</p>
              </div>
            </div>

            <div className="p-6 space-y-4">
              {/* Check 1: Rule Engine status */}
              <div className="flex items-center justify-between p-3 rounded-lg border border-border bg-muted/20">
                <div className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4.5 h-4.5 text-emerald-500" />
                  <span className="text-sm font-medium text-foreground">Rule Engine</span>
                </div>
                <span className="text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">
                  Active
                </span>
              </div>

              {/* Check 2: Active rules count */}
              <div className="flex items-center justify-between p-3 rounded-lg border border-border bg-muted/20">
                <div className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4.5 h-4.5 text-emerald-500" />
                  <span className="text-sm font-medium text-foreground">{t("activeRules")}</span>
                </div>
                <span className="text-xs font-semibold text-primary bg-secondary px-2.5 py-0.5 rounded border border-border font-mono">
                  {activeRulesCount} loaded
                </span>
              </div>

              {/* Check 3: Local Storage Database */}
              <div className="flex items-center justify-between p-3 rounded-lg border border-border bg-muted/20">
                <div className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4.5 h-4.5 text-emerald-500" />
                  <span className="text-sm font-medium text-foreground">Local Database</span>
                </div>
                <span className="text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">
                  Operational
                </span>
              </div>
            </div>
          </div>

          <div className="p-6 bg-muted/25 border-t border-border flex items-center justify-between">
            <span className="text-xs text-muted-foreground">Loyalty Campaign Core</span>
            <Link 
              href="/portal/rules" 
              className="text-xs font-bold text-primary flex items-center gap-1 hover:underline"
            >
              Configure settings <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>
        </div>

      </div>
    </div>
  );
}
