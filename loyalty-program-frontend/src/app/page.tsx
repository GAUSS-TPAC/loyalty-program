"use client";

import React from "react";
import Link from "next/link";
import { 
  ArrowRight, 
  Sparkles, 
  Shield, 
  Zap, 
  ChevronRight,
  Gift,
  Globe,
  Rocket,
  CheckCircle2,
  Lock,
  Smartphone
} from "lucide-react";
import { Button } from "@/components/ui/button";

export default function WelcomePage() {
  return (
    <div className="min-h-screen bg-white text-zinc-900 selection:bg-indigo-100 selection:text-indigo-900 overflow-x-hidden font-sans">
      {/* Background Dynamics */}
      <div className="fixed inset-0 overflow-hidden pointer-events-none z-0">
        <div className="absolute top-[-10%] right-[-10%] w-[70%] h-[70%] bg-gradient-to-br from-indigo-500/10 to-purple-500/10 rounded-full blur-[160px]" />
        <div className="absolute bottom-[-10%] left-[-10%] w-[50%] h-[50%] bg-gradient-to-tr from-fuchsia-500/5 to-rose-500/5 rounded-full blur-[140px]" />
        <div className="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-[0.15] mix-blend-overlay" />
      </div>

      {/* Modern Navigation */}
      <nav className="relative z-50 bg-white/70 backdrop-blur-xl border-b border-zinc-100 sticky top-0">
        <div className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-9 h-9 bg-zinc-900 rounded-xl flex items-center justify-center">
              <Sparkles className="text-white" size={18} />
            </div>
            <span className="text-lg font-black tracking-tighter uppercase">
              YOWYOB <span className="text-indigo-600">LOYALTY</span>
            </span>
          </div>
          
          <div className="hidden md:flex items-center gap-8 text-[11px] font-black uppercase tracking-widest text-zinc-400">
            <a href="#features" className="hover:text-zinc-900 transition-colors">Plateforme</a>
            <a href="#benefits" className="hover:text-zinc-900 transition-colors">Bénéfices</a>
            <a href="#" className="hover:text-zinc-900 transition-colors">Prix</a>
          </div>

          <div className="flex items-center gap-4">
            <Link href="/login">
              <Button variant="ghost" className="h-10 px-5 text-[11px] font-black uppercase tracking-widest rounded-full">Identifiez-vous</Button>
            </Link>
            <Link href="/login">
              <Button className="h-12 px-8 bg-indigo-600 hover:bg-indigo-700 text-white text-[11px] font-black uppercase tracking-widest rounded-full shadow-lg shadow-indigo-200 transition-all hover:scale-105 active:scale-95">
                Essai Gratuit
              </Button>
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero */}
      <section className="relative z-10 pt-20 md:pt-32 pb-40 px-6">
        <div className="max-w-7xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
          <div>
            <div className="inline-flex items-center gap-2 bg-indigo-50 border border-indigo-100 text-indigo-600 px-4 py-2 rounded-full mb-8">
              <div className="w-1.5 h-1.5 rounded-full bg-indigo-600 animate-pulse" />
              <span className="text-[10px] font-black uppercase tracking-widest leading-none">Nouvelle version v2.0 disponible</span>
            </div>
            
            <h1 className="text-6xl md:text-8xl font-black tracking-tighter leading-[0.9] mb-8 text-zinc-900">
               Libérez le <br />
               <span className="italic text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-purple-600">potentiel</span> <br />
               de votre audience.
            </h1>
            
            <p className="text-lg md:text-xl text-zinc-500 font-medium leading-relaxed mb-12 max-w-xl">
              La plateforme de fidélité la plus performante pour les marques modernes. Créez des expériences mémorables, pas seulement des transactions.
            </p>
            
            <div className="flex flex-col sm:flex-row gap-4">
              <Link href="/login">
                <Button className="h-16 px-10 bg-zinc-900 hover:bg-black text-white rounded-2xl font-black text-xs uppercase tracking-[0.2em] shadow-xl shadow-zinc-500/20 group">
                  Lancer mon projet <ArrowRight className="ml-2 group-hover:translate-x-1 transition-transform" size={18} />
                </Button>
              </Link>
              <Button variant="outline" className="h-16 px-10 rounded-2xl border-2 border-zinc-100 text-zinc-600 font-black text-xs uppercase tracking-[0.2em] hover:bg-zinc-50 transition-all">
                Voir la démo
              </Button>
            </div>
          </div>
          
          <div className="relative">
             <div className="absolute -inset-10 bg-indigo-500/10 blur-[100px] rounded-full scale-125" />
             <div className="relative bg-zinc-900 p-8 rounded-[50px] shadow-2xl border border-white/10 aspect-square flex items-center justify-center overflow-hidden">
                <Sparkles className="text-indigo-500/20 absolute scale-[5]" size={100} />
                <div className="relative z-10 text-center">
                   <div className="w-24 h-24 bg-white/5 rounded-3xl flex items-center justify-center mx-auto mb-6 backdrop-blur-xl border border-white/10">
                      <Gift className="text-white" size={40} />
                   </div>
                   <h3 className="text-white text-2xl font-black uppercase tracking-widest">Premium Rewards</h3>
                   <p className="text-zinc-500 mt-2 font-bold uppercase tracking-widest text-[10px]">Automatisé par IA</p>
                </div>
             </div>
          </div>
        </div>
      </section>

      {/* Basic Features */}
      <section id="features" className="relative z-10 py-32 px-6 bg-zinc-50 border-y border-zinc-100">
        <div className="max-w-7xl mx-auto">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-16">
            {[
              { 
                icon: Zap, 
                title: "Vitesse Éclair", 
                desc: "Déployez votre programme en moins de 15 minutes avec nos outils low-code.",
                color: "text-indigo-600"
              },
              { 
                icon: Shield, 
                title: "Sécurité Totale", 
                desc: "Vos données sont protégées par des standards bancaires.",
                color: "text-emerald-600"
              },
              { 
                icon: Globe, 
                title: "Global ready", 
                desc: "Support multi-devises natif pour conquérir le monde.",
                color: "text-blue-600"
              }
            ].map((feature, i) => (
              <div key={i} className="space-y-6">
                <div className="w-14 h-14 rounded-2xl flex items-center justify-center bg-white shadow-sm border border-zinc-100">
                  <feature.icon size={28} className={feature.color} />
                </div>
                <h3 className="text-xl font-black tracking-tight">{feature.title}</h3>
                <p className="text-zinc-500 font-medium leading-relaxed">{feature.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Final CTA */}
      <section className="relative z-10 py-40 px-6 bg-zinc-950">
        <div className="max-w-4xl mx-auto text-center flex flex-col items-center">
          <h2 className="text-5xl md:text-8xl font-black tracking-tighter text-white mb-12 leading-[0.9]">
             Prêt à <br /> transformer <br /> la fidélité ?
          </h2>
          <Link href="/login">
            <Button className="h-20 px-16 bg-white hover:bg-zinc-100 text-zinc-900 rounded-[30px] font-black text-lg uppercase tracking-[0.2em] shadow-2xl transition-all hover:scale-110 active:scale-95">
               Commencer
            </Button>
          </Link>
        </div>
      </section>

      {/* Simple Footer */}
      <footer className="relative z-10 py-24 px-6 border-t border-zinc-100 bg-white">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-12">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 bg-zinc-900 rounded-lg flex items-center justify-center">
              <Sparkles className="text-white" size={16} />
            </div>
            <span className="text-base font-black tracking-tighter uppercase">
              YOWYOB <span className="text-indigo-600">LOYALTY</span>
            </span>
          </div>
          <p className="text-zinc-400 text-[10px] font-black uppercase tracking-widest">© 2026 Yowyob Loyalty OS.</p>
        </div>
      </footer>
    </div>
  );
}