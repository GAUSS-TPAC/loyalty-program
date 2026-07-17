<?php

namespace Yowyob\Loyalty\Exception;

/**
 * Erreur renvoyée par l'API Loyalty (statut HTTP non 2xx).
 */
class ApiException extends LoyaltyException
{
    private int $statusCode;
    private ?array $body;

    public function __construct(string $message, int $statusCode, ?array $body = null)
    {
        parent::__construct($message, $statusCode);
        $this->statusCode = $statusCode;
        $this->body = $body;
    }

    public function getStatusCode(): int
    {
        return $this->statusCode;
    }

    public function getBody(): ?array
    {
        return $this->body;
    }
}
