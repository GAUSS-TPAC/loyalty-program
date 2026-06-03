"use client";

import { useLocale, useTranslations } from "next-intl";
import { usePathname, useRouter } from "@/i18n/routing";
import { useTransition } from "react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";

export function LanguageSwitcher() {
  const t = useTranslations("Header");
  const locale = useLocale();
  const router = useRouter();
  const pathname = usePathname();
  const [isPending, startTransition] = useTransition();

  const handleLocaleChange = (nextLocale: "en" | "fr") => {
    startTransition(() => {
      router.replace(pathname, { locale: nextLocale });
    });
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" size="sm" className="w-[120px] justify-between" disabled={isPending}>
          {locale === "en" ? "🇬🇧 English" : "🇫🇷 Français"}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem
          onClick={() => handleLocaleChange("en")}
          className={locale === "en" ? "bg-secondary" : ""}
        >
          🇬🇧 {t("en")}
        </DropdownMenuItem>
        <DropdownMenuItem
          onClick={() => handleLocaleChange("fr")}
          className={locale === "fr" ? "bg-secondary" : ""}
        >
          🇫🇷 {t("fr")}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
