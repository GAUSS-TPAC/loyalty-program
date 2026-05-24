"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Mail, Lock, Eye, EyeOff } from "lucide-react";
import { useState } from "react";

export default function AdminLoginPage() {
    const [showPassword, setShowPassword] = useState(false);

    return (
        <div className="min-h-screen bg-zinc-100 flex items-center justify-center p-6">
            <Card className="w-full max-w-md shadow-xl border border-zinc-200 bg-white">
                <CardHeader className="text-center pb-8">
                    <div className="mx-auto w-20 h-20 bg-purple-100 rounded-3xl flex items-center justify-center mb-6">
                        <span className="text-4xl">🛡️</span>
                    </div>
                    <CardTitle className="text-3xl font-bold text-gray-900">
                        Yowyob Loyalty
                    </CardTitle>
                    <CardDescription className="text-lg text-gray-600 mt-2">
                        Tableau de bord administrateur
                    </CardDescription>
                </CardHeader>

                <CardContent className="space-y-6">
                    <div className="space-y-4">
                        {/* Email Field */}
                        <div className="space-y-2">
                            <Label htmlFor="email">Adresse email</Label>
                            <div className="relative">
                                <Mail className="absolute left-3 top-3.5 h-5 w-5 text-gray-400" />
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="admin@entreprise.com"
                                    className="pl-11 h-12 bg-white"
                                />
                            </div>
                        </div>

                        {/* Password Field */}
                        <div className="space-y-2">
                            <Label htmlFor="password">Mot de passe</Label>
                            <div className="relative">
                                <Lock className="absolute left-3 top-3.5 h-5 w-5 text-gray-400" />
                                <Input
                                    id="password"
                                    type={showPassword ? "text" : "password"}
                                    placeholder="••••••••"
                                    className="pl-11 pr-11 h-12 bg-white"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-3 top-3.5 text-gray-400 hover:text-gray-600"
                                >
                                    {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* Forgot Password */}
                    <div className="text-right">
                        <button className="text-sm text-purple-600 hover:text-purple-700 font-medium">
                            Mot de passe oublié ?
                        </button>
                    </div>

                    {/* Login Button */}
                    <Button
                        className="w-full h-12 text-base font-semibold bg-purple-600 hover:bg-purple-700"
                        size="lg"
                    >
                        Se connecter
                    </Button>

                    {/* Divider */}
                    <div className="relative my-6">
                        <div className="absolute inset-0 flex items-center">
                            <div className="w-full border-t border-gray-200" />
                        </div>
                        <div className="relative flex justify-center text-sm">
                            <span className="bg-white px-4 text-gray-500">Ou continuer avec</span>
                        </div>
                    </div>

                    {/* Social Login */}
                    <div className="grid grid-cols-2 gap-4">
                        <Button variant="outline" className="h-12">
                            <img
                                src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg"
                                alt="Google"
                                className="w-5 h-5 mr-2"
                            />
                            Google
                        </Button>
                        <Button variant="outline" className="h-12">
                            <span className="mr-2 text-xl">☁️</span>
                            Microsoft
                        </Button>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}