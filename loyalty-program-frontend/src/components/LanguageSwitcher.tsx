"use client";

import { useLocale } from "next-intl";
import { usePathname, Link } from "@/i18n/routing";
import { Button } from "@/components/ui/button";

export function LanguageSwitcher() {
  const locale = useLocale();
  const pathname = usePathname();

  const nextLocale = locale === "en" ? "fr" : "en";
  const label = locale === "en" ? "🇫🇷 Français" : "🇬🇧 English";

  return (
    <Button variant="outline" size="sm" asChild className="w-[120px] justify-center cursor-pointer">
      <Link href={pathname} locale={nextLocale}>
        {label}
      </Link>
    </Button>
  );
}
