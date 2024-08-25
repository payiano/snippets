# Learn how to verify webhook event sent by Payiano API.
# @author Payiano Team <info@payiano.com>
# @see https://docs.payiano.com/api/rest/reference/webhooks/verify-signature

import hashlib
import hmac
import json
import re


class VerifyPayianoWebhookSignature:
    @staticmethod
    def isVerifiedSignature(payload: dict, receivedSignature: str, secret: str) -> bool:
        computedSignature = VerifyPayianoWebhookSignature.getComputedSignature(
            payload, secret
        )
        return hmac.compare_digest(computedSignature, receivedSignature)

    @staticmethod
    def getComputedSignature(payload: dict, secret: str) -> str:
        signatureText = VerifyPayianoWebhookSignature.getSignatureText(payload)
        return hmac.new(
            secret.encode(), signatureText.encode(), hashlib.sha256
        ).hexdigest()

    @staticmethod
    def getSignatureText(payload: dict) -> str:
        flatten = VerifyPayianoWebhookSignature.getFlattenPayload(payload)
        cleaned = VerifyPayianoWebhookSignature.getCleanedPayload(flatten)
        sorted = VerifyPayianoWebhookSignature.getSortedPayload(cleaned)
        simplified = VerifyPayianoWebhookSignature.getSimplifiedPayload(sorted)
        return "&".join(simplified)

    @staticmethod
    def getFlattenPayload(payload: dict | list, parent: str = None) -> dict:
        carry = {}
        for key, value in payload.items():
            newKey = f"{parent}.{key}" if parent else key

            if isinstance(value, dict):
                # Handle associative array (dictionary)
                carry.update(
                    VerifyPayianoWebhookSignature.getFlattenPayload(value, newKey)
                )
            elif isinstance(value, list):
                # Handle indexed array (list)
                for index, item in enumerate(value):
                    indexedKey = f"{newKey}.{index}"
                    if isinstance(item, (dict, list)):
                        carry.update(
                            VerifyPayianoWebhookSignature.getFlattenPayload(
                                item, indexedKey
                            )
                        )
                    else:
                        carry[indexedKey] = item
            else:
                carry[newKey] = value

        return carry

    @staticmethod
    def getCleanedPayload(flatten: dict) -> dict:
        carry = {}
        for key, value in flatten.items():
            if VerifyPayianoWebhookSignature.isValueNotEmpty(value):
                if isinstance(value, str):
                    value = re.sub(r"[\r\n\s]+", "", value)
                elif isinstance(value, bool):
                    value = "true" if value else "false"
                carry[key] = value
        return carry

    @staticmethod
    def getSimplifiedPayload(sorted: dict) -> list:
        carry = []
        for key, value in sorted.items():
            carry.append(f"{key}={value}")
        return carry

    @staticmethod
    def getSortedPayload(cleaned: dict) -> dict:
        return dict(sorted(cleaned.items()))

    @staticmethod
    def isValueNotEmpty(value) -> bool:
        if isinstance(value, dict):
            return any(
                VerifyPayianoWebhookSignature.isValueNotEmpty(v) for v in value.values()
            )
        elif isinstance(value, str):
            return bool(re.sub(r"\s+", "", value))
        elif isinstance(value, bool):
            return value in [True, False]
        else:
            return value is not None


# Find the following example:
payloadString = '{"webhook_event":{"id":"01j3521znn3b6wderr4vbyq18n","type":"company.created","version":"v1","fired_at":"1722572118554"},"webhook_event_attempt":{"id":"01j354j6nkwh3mdvhs6dsmswt8","sent_at":"1722572118554"},"details":{"data":{"company":{"name":"Pyngy URL Shortenr","avatar":null,"is_active":true,"is_approved":false,"employees_count":0,"owners":[{"name":"Amgad Yassen","position":"CEO","percentage":51.5},{"name":"Kamal Allam","position":"CEO","percentage":48.5}],"description":"A leading company providing\\n solutions for converting lengthy\\n URLs into short ones & simplifying online sharing!","social_urls":{"facebook_url":"https://facebook.com/pyngy","linked_in_url":null}}}}}'

payload = json.loads(payloadString)
receivedSignature = "7159d656803a7136be897193dd70a48ca757786d0fe3531f33a48dc17d995725"
secret = "OWlPF9plag9KEtYvw3EM+7UDrgXb84xjZPR2TvzJM1I="

print(
    {
        "getSignatureText": VerifyPayianoWebhookSignature.getSignatureText(payload),
        "getComputedSignature": VerifyPayianoWebhookSignature.getComputedSignature(
            payload, secret
        ),
        "isVerifiedSignature": int(
            VerifyPayianoWebhookSignature.isVerifiedSignature(
                payload, receivedSignature, secret
            )
        ),
    }
)
