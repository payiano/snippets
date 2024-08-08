<?php

/**
 * Learn how to verify webhook event sent by Payiano API.
 *
 * @author Payiano Team <info@payiano.com>
 * @see https://docs.payiano.com/api/rest/reference/webhooks/verify-signature
 */
class VerifyPayianoWebhookEvent
{
    /**
     * Check if signature is verified.
     *
     * @param  array $payload
     * @param  string $receivedSignature
     * @param  string $secret
     * @return bool
     */
    public static function isVerifiedSignature(array $payload, string $receivedSignature, string $secret): bool
    {
        $computedSignature = self::getComputedSignature($payload, $secret);

        return hash_equals($computedSignature, $receivedSignature);
    }

    /**
     * Get computed signature.
     *
     * @param  array $payload
     * @param  string $secret
     * @return string
     */
    public static function getComputedSignature(array $payload, string $secret): string
    {
        $signatureText = self::getSignatureText($payload);

        return hash_hmac('sha256', $signatureText, $secret);
    }

    /**
     * Get signature text.
     *
     * @param  array $payload
     * @return string
     */
    public static function getSignatureText(array $payload): string
    {
        $flatten = self::getFlattenPayload($payload);

        $cleaned = self::getCleanedPayload($flatten);

        $sorted = self::getSortedPayload($cleaned);

        $simplified = self::getSimplifiedPayload($sorted);

        return implode('&', $simplified);
    }

    /**
     * Get flatten payload.
     *
     * @param  array $payload
     * @param  ?string $parent
     * @return array
     */
    public static function getFlattenPayload(array $payload, ?string $parent = null): array
    {
        $carry = [];

        foreach ($payload as $key => $value) {
            $key = $parent ? "{$parent}.{$key}" : $key;

            if (is_array($value)) {
                $carry = array_merge(
                    $carry,
                    self::getFlattenPayload($value, $key)
                );
            } else {
                $carry[$key] = $value;
            }
        }

        return $carry;
    }

    /**
     * Get cleaned payload.
     *
     * @param  array $flatten
     * @return array
     */
    public static function getCleanedPayload(array $flatten): array
    {
        $carry = [];

        foreach ($flatten as $key => $value) {
            if (self::isValueNotEmpty($value)) {
                if (is_string($value)) {
                    // Remove new lines and any spaces from strings.
                    $value = preg_replace('/[\r\n\s]+/', '', $value);
                } elseif (is_bool($value)) {
                    // Replace boolean values with boolean strings.
                    $value = $value ? 'true' : 'false';
                }

                $carry[$key] = $value;
            }
        }

        return $carry;
    }

    /**
     * Get simplified payload.
     *
     * @param  array $sorted
     * @return array
     */
    public static function getSimplifiedPayload(array $sorted): array
    {
        $carry = [];

        foreach ($sorted as $key => $value) {
            $carry[] = "{$key}={$value}";
        }

        return $carry;
    }

    /**
     * Get sorted payload.
     *
     * @param  array $cleaned
     * @return array
     */
    public static function getSortedPayload(array $cleaned): array
    {
        // Sort the keys alphabetically.
        ksort($cleaned);

        return $cleaned;
    }

    /**
     * Check if value is not empty.
     *
     * @param  mixed $value
     * @return bool
     */
    public static function isValueNotEmpty($value): bool
    {
        if (is_array($value)) {
            if (! $value) {
                return false;
            }

            foreach ($value as $v) {
                if (self::isValueNotEmpty($v)) {
                    return true;
                }
            }
        } elseif (is_string($value)) {
            return !!strlen(preg_replace('/\s+/', '', $value));
        } elseif (is_bool($value)) {
            return in_array($value, [true, false]);
        } elseif (! is_numeric($value) && in_array($value, [null])) {
            return false;
        }

        return true;
    }
}

// Find the following example:
$payloadString = '{"webhook_event":{"id":"01j3521znn3b6wderr4vbyq18n","type":"company.created","version":"v1","fired_at":"1722572118554"},"webhook_event_attempt":{"id":"01j354j6nkwh3mdvhs6dsmswt8","sent_at":"1722572118554"},"details":{"data":{"company":{"name":"Graply URL Shortenr","avatar":null,"is_active":true,"is_approved":false,"employees_count":0,"owners":[{"name":"Amgad Yassen","position":"CEO","percentage":51.5},{"name":"Kamal Allam","position":"CEO","percentage":48.5}],"description":"A leading company providing\\n solutions for converting lengthy\\n URLs into short ones & simplifying online sharing!","social_urls":{"facebook_url":"https://facebook.com/graply","linked_in_url":null}}}}}';

$payload = json_decode($payloadString, true);

$receivedSignature = 'a621fc745416b00bb24758440fa75a850f6f8cb3f901217a6a9854f043ff8b70';

$secret = 'OWlPF9plag9KEtYvw3EM+7UDrgXb84xjZPR2TvzJM1I=';

print_r([
    'getSignatureText' => VerifyPayianoWebhookEvent::getSignatureText($payload),
    'getComputedSignature' => VerifyPayianoWebhookEvent::getComputedSignature($payload, $secret),
    'isVerifiedSignature' => (int) VerifyPayianoWebhookEvent::isVerifiedSignature($payload, $receivedSignature, $secret),
]);
