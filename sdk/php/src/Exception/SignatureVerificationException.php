<?php

namespace Yowyob\Loyalty\Exception;

/**
 * La signature du callback webhook est absente, invalide ou expirée.
 * Ne traitez JAMAIS un callback qui lève cette exception.
 */
class SignatureVerificationException extends LoyaltyException
{
}
