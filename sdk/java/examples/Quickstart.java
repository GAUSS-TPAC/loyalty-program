import com.yowyob.loyalty.sdk.ApiException;
import com.yowyob.loyalty.sdk.LoyaltyClient;

import java.util.List;
import java.util.Map;

/**
 * Démarrage rapide — après `mvn install` du SDK, ajoutez la dépendance
 * com.yowyob:loyalty-sdk:1.0.0 à votre projet puis :
 */
public class Quickstart {

    public static void main(String[] args) {
        // 1. Initialisation — récupérez ces clés dans Portail > Applications
        LoyaltyClient loyalty = new LoyaltyClient(
                envOr("LOYALTY_PUBLIC_KEY", "pk_test_xxx"),
                envOr("LOYALTY_PRIVATE_KEY", "sk_test_xxx"),
                envOr("LOYALTY_BASE_URL", "http://localhost:8081"),
                System.getenv("LOYALTY_WEBHOOK_SECRET"));

        String memberId = "00000000-0000-0000-0000-000000000123";

        // 2. Envoyer un événement d'achat (avec clé d'idempotence)
        try {
            Map<String, Object> result = loyalty.trackEvent(
                    "purchase.completed", memberId, null, Map.of("amount", 4990), "commande-2026-0042");
            System.out.println("Événement traité : " + result.get("eventId"));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> effects = (List<Map<String, Object>>) result.get("effectsApplied");
            for (Map<String, Object> effect : effects) {
                System.out.println(" → effet : " + effect.get("effectType") + " (règle : " + effect.get("ruleName") + ")");
            }
        } catch (ApiException e) {
            System.err.println("Erreur API (" + e.getStatusCode() + ") : " + e.getMessage());
        }

        // 3. Consulter le solde de points du membre
        Map<String, Object> points = loyalty.getMemberPoints(memberId);
        System.out.println("Points disponibles : " + points.get("availablePoints"));

        // 4. Dans votre contrôleur de callback (ex. Spring) :
        //    Map<String, Object> event = loyalty.checkCallbackIntegrity(headers, rawBody);
        //    → lève SignatureVerificationException si la requête est forgée ; répondez alors 400.
    }

    private static String envOr(String name, String fallback) {
        String value = System.getenv(name);
        return value != null && !value.isBlank() ? value : fallback;
    }
}
