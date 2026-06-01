"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Mail, Lock, Eye, EyeOff } from "lucide-react";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

const DEMO_ACCOUNTS: Record<string, { password: string; redirect: string; label: string }> = {
    "admin@yowyob.com":  { password: "admin123",   redirect: "/dashboard", label: "Super Admin" },
    "ridngo@demo.com":   { password: "ridngo123",  redirect: "/dashboard", label: "Admin RidnGo" },
    "ksmshop@demo.com":  { password: "ksmshop123", redirect: "/dashboard", label: "Admin KSM Shop" },
    "marie@demo.cm":     { password: "marie123",   redirect: "/member",    label: "Membre Marie" },
};

export default function AdminLoginPage() {
    const router = useRouter();
    const [showPassword, setShowPassword] = useState(false);
    const [email, setEmail]       = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading]   = useState(false);
    const [error, setError]       = useState("");

    const handleLogin = (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        setLoading(true);
        setTimeout(() => {
            const account = DEMO_ACCOUNTS[email.trim().toLowerCase()];
            if (account && account.password === password) {
                toast.success("Connexion réussie — " + account.label);
                router.push(account.redirect);
            } else {
                setError("Email ou mot de passe incorrect.");
                toast.error("Identifiants invalides");
            }
            setLoading(false);
        }, 600);
    };

    return (
        <div className="min-h-screen bg-zinc-100 flex items-center justify-center p-6">
            <Card className="w-full max-w-md shadow-xl border border-zinc-200 bg-white">
                <CardHeader className="text-center pb-8">
                    <div className="mx-auto w-20 h-20 bg-purple-100 rounded-3xl flex items-center justify-center mb-6">
                        <span className="text-4xl">🛡️</span>
                    </div>
                    <CardTitle className="text-3xl font-bold text-gray-900">Yowyob Loyalty</CardTitle>
                    <CardDescription className="text-lg text-gray-600 mt-2">Tableau de bord administrateur</CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                    <div className="bg-indigo-50 border border-indigo-200 rounded-xl p-3 text-xs text-indigo-800 space-y-1">
                        <p className="font-extrabold uppercase tracking-wide mb-1">Comptes de démo</p>
                        <p>📧 <span className="font-mono font-bold">admin@yowyob.com</span> / <span className="font-mono font-bold">admin123</span></p>
                        <p>📧 <span className="font-mono font-bold">ridngo@demo.com</span> / <span className="font-mono font-bold">ridngo123</span></p>
                        <p>👤 <span className="font-mono font-bold">marie@demo.cm</span> / <span className="font-mono font-bold">marie123</span> → portail membre</p>
                    </div>
                    <form onSubmit={handleLogin} className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="email">Adresse email</Label>
                            <div className="relative">
                                <Mail className="absolute left-3 top-3.5 h-5 w-5 text-gray-400" />
                                <Input id="email" type="email" placeholder="admin@entreprise.com"
                                    className="pl-11 h-12 bg-white" value={email}
                                    onChange={(e) => setEmail(e.target.value)} required />
                            </div>
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="password">Mot de passe</Label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-3.5 h-5 w-5 text-gray-400" />
                                <Input id="password" type={showPassword ? "text" : "password"}
                                    placeholder="••••••••" className="pl-11 pr-11 h-12 bg-white"
                                    value={password} onChange={(e) => setPassword(e.target.value)} required />
                                <button type="button" onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-3 top-3.5 text-gray-400 hover:text-gray-600">
                                    {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                                </button>
                            </div>
                        </div>
                        {error && (
                            <p className="text-xs font-bold text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">{error}</p>
                        )}
                        <div className="text-right">
                            <button type="button" className="text-sm text-purple-600 hover:text-purple-700 font-medium">Mot de passe oublié ?</button>
                        </div>
                        <Button type="submit" disabled={loading}
                            className="w-full h-12 text-base font-semibold bg-purple-600 hover:bg-purple-700 disabled:opacity-60" size="lg">
                            {loading ? "Connexion..." : "Se connecter"}
                        </Button>
                    </form>
                    <div className="relative my-6">
                        <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-gray-200" /></div>
                        <div className="relative flex justify-center text-sm"><span className="bg-white px-4 text-gray-500">Ou continuer avec</span></div>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                        <Button variant="outline" className="h-12" type="button"
                            onClick={() => toast.info("OAuth Google non configuré en démo")}>
                            <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google" className="w-5 h-5 mr-2" />
                            Google
                        </Button>
                        <Button variant="outline" className="h-12" type="button"
                            onClick={() => toast.info("OAuth Microsoft non configuré en démo")}>
                            <span className="mr-2 text-xl">☁️</span>Microsoft
                        </Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
