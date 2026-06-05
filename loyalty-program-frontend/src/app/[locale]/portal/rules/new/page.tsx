"use client";

import { useEffect } from "react";
import { useRouter } from "@/i18n/routing";

export default function RedirectNewRule() {
  const router = useRouter();

  useEffect(() => {
    router.replace("/portal/rules");
  }, [router]);

  return null;
}
