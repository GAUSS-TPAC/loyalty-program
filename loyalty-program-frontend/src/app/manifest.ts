import type { MetadataRoute } from "next";

export default function manifest(): MetadataRoute.Manifest {
  return {
    name: "Loyalty Core Portal",
    short_name: "Loyalty Core",
    description: "Portail d'administration du programme de fidélité Yowyob",
    start_url: "/en/portal",
    display: "standalone",
    background_color: "#fdfbf7",
    theme_color: "#8d6e63",
  };
}
