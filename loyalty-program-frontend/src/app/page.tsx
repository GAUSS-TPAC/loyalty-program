import React from "react";
import Link from "next/link";
import {
    ArrowRight,
    Sparkles,
    Shield,
    Zap,
    BarChart3,
    ChevronRight,
    Gift,
    Globe,
    CheckCircle2,
    Lock,
    Layers,
    MousePointer2,
    Activity,
    Rocket
} from "lucide-react";
import { Button } from "@/components/ui/button";

export default function WelcomePage() {
    return (
        <div className="min-h-screen bg-slate-50 text-slate-900 selection:bg-purple-200 selection:text-purple-900 overflow-x-hidden">
            {/* Dynamic Background Mesh */}
            <div className="fixed inset-0 overflow-hidden pointer-events-none z-0">
                <div className="absolute top-[-10%] right-[-10%] w-[70%] h-[70%] bg-gradient-to-br from-purple-400/20 to-fuchsia-400/20 rounded-full blur-[160px] animate-pulse" />
                <div className="absolute top-[20%] left-[-10%] w-[40%] h-[40%] bg-gradient-to-tr from-indigo-400/20 to-blue-400/20 rounded-full blur-[140px]" />
                <div className="absolute bottom-[0%] right-[20%] w-[50%] h-[50%] bg-gradient-to-tl from-amber-200/20 to-orange-300/10 rounded-full blur-[120px]" />
                <div className="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20 mix-blend-overlay" />
            </div>

            {/* Navigation */}
            <nav className="relative z-50 border-b border-white/20 bg-white/40 backdrop-blur-2xl sticky top-0">
                <div className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div className="w-11 h-11 bg-gradient-to-br from-indigo-600 via-purple-600 to-fuchsia-600 rounded-[15px] flex items-center justify-center shadow-xl shadow-purple-500/20 transform hover:scale-110 transition-transform cursor-pointer group">
                            <Sparkles className="text-white group-hover:rotate-12 transition-transform" size={22} />
                        </div>
                        <span className="text-xl font-black tracking-tighter text-zinc-900 group">
                            YOWYOB <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-purple-600 tracking-tight">LOYALTY</span>
                        </span>
                    </div>

                    <div className="hidden lg:flex items-center gap-10 text-[10px] font-black uppercase tracking-[0.25em] text-slate-500">
                        <a href="#features" className="hover:text-indigo-600 transition-colors relative group">
                            Plateforme
                            <span className="absolute -bottom-1 left-0 w-0 h-0.5 bg-indigo-600 transition-all group-hover:w-full" />
                        </a>
                        <a href="#solutions" className="hover:text-indigo-600 transition-colors relative group">
                            Solutions
                            <span className="absolute -bottom-1 left-0 w-0 h-0.5 bg-indigo-600 transition-all group-hover:w-full" />
                        </a>
                        <a href="#" className="hover:text-indigo-600 transition-colors relative group">
                            Documentation
                            <span className="absolute -bottom-1 left-0 w-0 h-0.5 bg-indigo-600 transition-all group-hover:w-full" />
                        </a>
                    </div>

                    <div className="flex items-center gap-4">
                        <Link href="/login">
                            <Button variant="ghost" className="font-black text-[10px] uppercase tracking-widest h-10 px-6 rounded-xl hover:bg-white/50">
                                Log In
                            </Button>
                        </Link>
                        <Link href="/login">
                            <Button className="bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-white font-black text-[10px] uppercase tracking-widest h-11 px-8 rounded-xl shadow-[0_10px_30px_-5px_rgba(79,70,229,0.3)] transition-all hover:scale-105 active:scale-95 group">
                                Commencer <ArrowRight className="ml-2 group-hover:translate-x-1 transition-transform" size={14} />
                            </Button>
                        </Link>
                    </div>
                </div>
            </nav>

            {/* Hero Section */}
            <section className="relative z-10 pt-24 pb-40 px-6">
                <div className="max-w-7xl mx-auto flex flex-col items-center">
                    <div className="inline-flex items-center gap-2 bg-white/80 backdrop-blur-md border border-indigo-100/50 text-indigo-700 px-6 py-3 rounded-full mb-12 shadow-sm animate-in fade-in slide-in-from-bottom-4 duration-1000">
                        <div className="flex -space-x-2 mr-2">
                            {[1, 2, 3].map(i => (
                                <div key={i} className="w-5 h-5 rounded-full border-2 border-white bg-gradient-to-br from-indigo-400 to-purple-400" />
                            ))}
                        </div>
                        <span className="text-[10px] font-black uppercase tracking-[0.2em]">+500 marques nous font confiance</span>
                    </div>

                    <h1 className="text-7xl md:text-[160px] font-black tracking-tighter leading-[0.75] mb-12 text-zinc-900 text-center animate-in fade-in slide-in-from-bottom-8 duration-1000 delay-100">
                        Fidélité <br />
                        <span className="text-transparent bg-clip-text bg-gradient-to-br from-indigo-600 via-purple-600 to-fuchsia-600">Réinventée</span>.
                    </h1>

                    <div className="max-w-3xl text-center mb-16 animate-in fade-in slide-in-from-bottom-12 duration-1000 delay-200">
                        <p className="text-slate-600 text-xl md:text-3xl font-medium leading-[1.4] tracking-tight">
                            Yowyob Loyalty est le <span className="text-zinc-900 font-bold underline decoration-indigo-400/50 decoration-4 underline-offset-4">moteur de croissance</span> le plus audacieux pour votre marque. Automatisez, engagez, et dominez votre marché.
                        </p>
                    </div>

                    <div className="flex flex-col sm:flex-row items-center gap-8 animate-in fade-in slide-in-from-bottom-16 duration-1000 delay-300">
                        <Link href="/login">
                            <Button className="bg-zinc-900 hover:bg-black text-white h-20 px-14 rounded-[28px] font-black text-base uppercase tracking-widest shadow-2xl shadow-zinc-950/40 group relative overflow-hidden transition-all hover:scale-[1.02]">
                                <span className="relative z-10 flex items-center gap-3">Lancer mon programme <ArrowRight size={22} className="group-hover:translate-x-1 transition-transform" /></span>
                                <div className="absolute inset-0 bg-gradient-to-r from-indigo-600 via-purple-600 to-fuchsia-600 opacity-0 group-hover:opacity-100 transition-opacity duration-500" />
                            </Button>
                        </Link>
                        <Button variant="outline" className="h-20 px-14 rounded-[28px] bg-white border-2 border-slate-200 text-zinc-600 font-black text-base uppercase tracking-widest hover:border-indigo-400 hover:text-indigo-600 transition-all shadow-sm">
                            Démo Interactive
                        </Button>
                    </div>

                    {/* Glowing Platform Preview */}
                    <div className="mt-36 w-full max-w-6xl relative group animate-in fade-in zoom-in duration-1200 delay-500">
                        {/* Visual Aura */}
                        <div className="absolute -inset-10 bg-gradient-to-tr from-indigo-500/20 via-purple-500/20 to-fuchsia-500/20 blur-[120px] rounded-[100px] opacity-100 group-hover:scale-110 transition-transform duration-1000" />

                        <div className="relative bg-white/90 backdrop-blur-xl border-2 border-white p-6 md:p-10 rounded-[60px] shadow-[0_50px_100px_-30px_rgba(0,0,0,0.1)] overflow-hidden ring-1 ring-zinc-950/5">
                            <div className="flex items-center justify-between mb-12">
                                <div className="flex gap-2.5">
                                    <div className="w-3.5 h-3.5 rounded-full bg-rose-500/80 shadow-[0_0_10px_rgba(244,63,94,0.3)]" />
                                    <div className="w-3.5 h-3.5 rounded-full bg-amber-500/80 shadow-[0_0_10px_rgba(245,158,11,0.3)]" />
                                    <div className="w-3.5 h-3.5 rounded-full bg-emerald-500/80 shadow-[0_0_10px_rgba(16,185,129,0.3)]" />
                                </div>
                                <div className="flex items-center gap-4 bg-zinc-100/50 px-5 py-2 rounded-2xl border border-white">
                                    <Activity size={14} className="text-indigo-600 animate-pulse" />
                                    <span className="text-[10px] font-black uppercase text-zinc-500 tracking-widest">Temps réel : 142 évènements /sec</span>
                                </div>
                            </div>

                            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                                <div className="lg:col-span-2 p-10 bg-gradient-to-br from-zinc-50 to-white rounded-[40px] border border-white shadow-inner">
                                    <div className="flex justify-between items-end mb-12">
                                        <div>
                                            <h4 className="text-[10px] font-black text-indigo-600 uppercase tracking-[0.3em] mb-2">Performances Hebdo</h4>
                                            <p className="text-4xl font-black text-zinc-900">+124.8% <span className="text-emerald-500 text-lg ml-2">↑</span></p>
                                        </div>
                                        <div className="flex gap-2">
                                            {['7j', '30j', '1an'].map(t => (
                                                <div key={t} className={`px-4 py-1.5 rounded-full text-[9px] font-black uppercase transition-all cursor-pointer ${t === '7j' ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-500/30' : 'bg-white text-zinc-400 hover:bg-zinc-100 border border-zinc-100'}`}>{t}</div>
                                            ))}
                                        </div>
                                    </div>
                                    <div className="h-48 w-full flex items-end gap-2.5">
                                        {[30, 45, 35, 60, 25, 80, 55, 90, 70, 85, 45, 65].map((h, i) => (
                                            <div key={i} className="flex-1 bg-gradient-to-t from-indigo-500 to-purple-500 rounded-t-xl group-hover:shadow-[0_0_20px_rgba(99,102,241,0.3)] transition-all duration-1000 ease-out" style={{ height: `${h}%`, transitionDelay: `${i * 50}ms` }} />
                                        ))}
                                    </div>
                                </div>

                                <div className="space-y-8">
                                    <div className="p-8 bg-zinc-900 rounded-[40px] shadow-2xl relative overflow-hidden group/card h-full">
                                        <div className="absolute top-0 right-0 w-32 h-32 bg-indigo-500/20 rounded-full blur-[40px] -mr-16 -mt-16" />
                                        <div className="relative z-10 flex flex-col justify-between h-full">
                                            <div>
                                                <Rocket className="text-amber-400 mb-6" size={32} />
                                                <h4 className="text-white text-lg font-black mb-2 uppercase tracking-tight">Accélération</h4>
                                                <p className="text-zinc-500 text-xs font-medium leading-relaxed">Engagement multiplié par 3.2 lors du dernier semestre.</p>
                                            </div>
                                            <Button className="w-full mt-8 bg-white/10 hover:bg-white/20 text-white border-0 backdrop-blur-md rounded-2xl h-12 text-[10px] font-black uppercase tracking-widest">
                                                Détails
                                            </Button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* Solutions / Features Section with Color Cards */}
            <section id="features" className="relative z-10 py-56 px-6 bg-zinc-950">
                <div className="max-w-7xl mx-auto">
                    <div className="mb-24">
                        <h2 className="text-4xl md:text-7xl font-black tracking-tight text-white leading-tight mb-10">
                            Des outils <span className="italic text-transparent bg-clip-text bg-gradient-to-r from-amber-400 to-orange-500 italic">surpuissants</span> <br />
                            pour chaque scénario.
                        </h2>
                        <div className="w-24 h-2 bg-indigo-600 rounded-full" />
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
                        {[
                            {
                                icon: Zap,
                                title: "Vitesse",
                                desc: "Transactions validées en millisecondes.",
                                bg: "bg-indigo-600/10",
                                text: "text-indigo-400",
                                shadow: "shadow-indigo-500/20 hover:shadow-indigo-500/40",
                                border: "border-indigo-500/30"
                            },
                            {
                                icon: Shield,
                                title: "Blindé",
                                desc: "Architecture certifiée Enterprise-Grade.",
                                bg: "bg-emerald-600/10",
                                text: "text-emerald-400",
                                shadow: "shadow-emerald-500/20 hover:shadow-emerald-500/40",
                                border: "border-emerald-500/30"
                            },
                            {
                                icon: BarChart3,
                                title: "Vision",
                                desc: "Données prédictives activées par IA.",
                                bg: "bg-fuchsia-600/10",
                                text: "text-fuchsia-400",
                                shadow: "shadow-fuchsia-500/20 hover:shadow-fuchsia-500/40",
                                border: "border-fuchsia-500/30"
                            },
                            {
                                icon: Gift,
                                title: "Magie",
                                desc: "Expérience utilisateur époustouflante.",
                                bg: "bg-amber-600/10",
                                text: "text-amber-400",
                                shadow: "shadow-amber-500/20 hover:shadow-amber-500/40",
                                border: "border-amber-500/30"
                            }
                        ].map((f, i) => (
                            <div key={i} className={`p-10 rounded-[42px] border-2 ${f.border} ${f.bg} backdrop-blur-sm shadow-2xl ${f.shadow} transition-all hover:-translate-y-4 group cursor-pointer`}>
                                <f.icon className={`${f.text} mb-8 group-hover:scale-125 transition-transform`} size={44} />
                                <h4 className="text-white text-2xl font-black mb-4 uppercase tracking-tighter">{f.title}</h4>
                                <p className="text-zinc-400 font-medium leading-relaxed">{f.desc}</p>
                                <div className={`mt-10 flex items-center justify-center w-12 h-12 bg-white/5 rounded-2xl ${f.text}`}>
                                    <ArrowRight size={20} />
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Explosive CTA Section */}
            <section className="relative z-10 py-48 px-6 bg-white shrink-0">
                <div className="max-w-7xl mx-auto">
                    <div className="relative group p-1 md:p-2 bg-gradient-to-br from-indigo-600 via-purple-600 to-fuchsia-600 rounded-[70px] shadow-[0_50px_120px_-30px_rgba(79,70,229,0.4)] overflow-hidden">
                        <div className="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-30 mix-blend-overlay" />
                        <div className="relative bg-zinc-900 rounded-[64px] p-16 md:p-36 flex flex-col items-center text-center">
                            <div className="w-16 h-16 bg-white/10 rounded-3xl flex items-center justify-center mb-10 backdrop-blur-3xl animate-bounce">
                                <Sparkles className="text-white" size={32} />
                            </div>
                            <h2 className="text-6xl md:text-9xl font-black tracking-tighter text-white leading-[0.8] mb-12">
                                Rejoignez la <br /> <span className="text-transparent bg-clip-text bg-gradient-to-r from-amber-300 to-orange-400">révolution</span>.
                            </h2>
                            <div className="flex flex-col sm:flex-row gap-8 mt-4">
                                <Link href="/login">
                                    <Button className="bg-white hover:bg-zinc-100 text-zinc-900 h-22 px-20 rounded-[30px] font-black text-lg uppercase tracking-[0.25em] shadow-[0_25px_60px_-15px_rgba(255,255,255,0.3)] transition-all hover:scale-110 active:scale-95">
                                        Start Now
                                    </Button>
                                </Link>
                                <Button variant="ghost" className="text-white h-22 px-12 rounded-[30px] border-2 border-white/20 font-black text-lg uppercase tracking-widest hover:bg-white/10">
                                    Book a Call
                                </Button>
                            </div>
                            <p className="mt-16 text-zinc-500 text-[11px] font-black uppercase tracking-[0.4em]">Propulsé par Yowyob Loyalty OS • Vitesse 1ms</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* Vibrant Footer */}
            <footer className="relative z-10 py-32 px-6 bg-slate-50 border-t border-slate-200">
                <div className="max-w-7xl mx-auto">
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-24 items-start mb-32">
                        <div className="md:col-span-2">
                            <div className="flex items-center gap-3 mb-10">
                                <div className="w-12 h-12 bg-gradient-to-br from-indigo-600 to-purple-600 rounded-[16px] flex items-center justify-center shadow-lg">
                                    <Sparkles className="text-white" size={24} />
                                </div>
                                <span className="text-2xl font-black tracking-tighter text-zinc-900 italic">YOWYOB <span className="text-indigo-600 tracking-tight">LOYALTY</span></span>
                            </div>
                            <p className="text-slate-500 text-2xl font-medium leading-relaxed max-w-xl">
                                L'avenir de la fidélité n'est plus transactionnel. Il est relationnel. <span className="text-zinc-900 font-bold italic">Bienvenue dans l'ère Yowyob.</span>
                            </p>
                        </div>

                        <div className="space-y-10 pt-4">
                            <h4 className="text-[11px] font-black uppercase tracking-[0.4em] text-zinc-400">Navigation</h4>
                            <ul className="space-y-5 text-sm font-black text-zinc-900 uppercase tracking-widest">
                                {['Plateforme', 'Dev Tools', 'Pricing', 'Guides'].map(item => (
                                    <li key={item} className="hover:text-indigo-600 cursor-pointer transition-colors flex items-center gap-2 group">
                                        <span className="w-0 h-1 bg-indigo-600 transition-all group-hover:w-4" /> {item}
                                    </li>
                                ))}
                            </ul>
                        </div>

                        <div className="space-y-10 pt-4">
                            <h4 className="text-[11px] font-black uppercase tracking-[0.4em] text-zinc-400">Contact</h4>
                            <div className="space-y-6">
                                <p className="text-sm font-black text-zinc-900 uppercase tracking-widest">hello@yowyob.com</p>
                                <div className="flex gap-4">
                                    {[Globe, Shield, Zap].map((Icon, idx) => (
                                        <div key={idx} className="w-12 h-12 rounded-2xl bg-white border border-slate-200 flex items-center justify-center hover:border-indigo-400 transition-all cursor-pointer shadow-sm">
                                            <Icon size={20} className="text-zinc-400 hover:text-indigo-600" />
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="flex flex-col md:flex-row items-center justify-between gap-10 pt-20 border-t border-slate-200">
                        <p className="text-[11px] font-black text-slate-400 uppercase tracking-[0.4em]">© 2026 Yowyob Loyalty OS. Unlimited Creativity.</p>
                        <div className="flex items-center gap-4">
                            <div className="px-4 py-2 bg-zinc-900 text-white rounded-xl text-[10px] font-black uppercase tracking-widest">v2.0 Stable</div>
                        </div>
                    </div>
                </div>
            </footer>
        </div>
    );
}