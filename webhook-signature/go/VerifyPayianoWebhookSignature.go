/**
 * Learn how to verify webhook event sent by Payiano API.
 *
 * @author Payiano Team <info@payiano.com>
 * @see https://docs.payiano.com/api/rest/reference/webhooks/verify-signature
 */
package main

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"sort"
	"strings"
)

type VerifyPayianoWebhookSignature struct{}

func (v VerifyPayianoWebhookSignature) isVerifiedSignature(payload map[string]interface{}, receivedSignature string, secret string) bool {
	computedSignature := v.getComputedSignature(payload, secret)
	return hmac.Equal([]byte(computedSignature), []byte(receivedSignature))
}

func (v VerifyPayianoWebhookSignature) getComputedSignature(payload map[string]interface{}, secret string) string {
	signatureText := v.getSignatureText(payload)
	h := hmac.New(sha256.New, []byte(secret))
	h.Write([]byte(signatureText))
	return hex.EncodeToString(h.Sum(nil))
}

func (v VerifyPayianoWebhookSignature) getSignatureText(payload map[string]interface{}) string {
	flatten := v.getFlattenPayload(payload, "")
	cleaned := v.getCleanedPayload(flatten)
	sorted := v.getSortedPayload(cleaned)
	simplified := v.getSimplifiedPayload(sorted)
	return strings.Join(simplified, "&")
}

func (v VerifyPayianoWebhookSignature) getFlattenPayload(payload map[string]interface{}, parent string) map[string]interface{} {
	carry := make(map[string]interface{})
	for key, value := range payload {
		newKey := key
		if parent != "" {
			newKey = parent + "." + key
		}

		switch value := value.(type) {
		case map[string]interface{}:
			flattened := v.getFlattenPayload(value, newKey)
			for k, v := range flattened {
				carry[k] = v
			}
		case []interface{}:
			// Sort the list elements if they contain maps
			for index, item := range value {
				indexedKey := fmt.Sprintf("%s.%d", newKey, index)
				switch item := item.(type) {
				case map[string]interface{}:
					flattened := v.getFlattenPayload(item, indexedKey)
					for k, v := range flattened {
						carry[k] = v
					}
				case []interface{}:
					flattened := v.getFlattenPayload(map[string]interface{}{fmt.Sprintf("%d", index): item}, indexedKey)
					for k, v := range flattened {
						carry[k] = v
					}
				default:
					carry[indexedKey] = item
				}
			}
		default:
			carry[newKey] = value
		}
	}
	return carry
}

func (v VerifyPayianoWebhookSignature) getCleanedPayload(flatten map[string]interface{}) map[string]interface{} {
	carry := make(map[string]interface{})
	for key, value := range flatten {
		if v.isValueNotEmpty(value) {
			switch value := value.(type) {
			case string:
				value = strings.TrimSpace(value)
				value = strings.ReplaceAll(value, "\n", "")
				value = strings.ReplaceAll(value, "\t", "")
				value = strings.ReplaceAll(value, " ", "")
				carry[key] = value
			case bool:
				if value {
					carry[key] = "true"
				} else {
					carry[key] = "false"
				}
			default:
				carry[key] = value
			}
		}
	}
	return carry
}

func (v VerifyPayianoWebhookSignature) getSimplifiedPayload(sorted map[string]interface{}) []string {
	keys := make([]string, 0, len(sorted))
	for key := range sorted {
		keys = append(keys, key)
	}

	// Sort the keys to ensure a consistent order
	sort.Strings(keys)

	carry := make([]string, 0, len(sorted))
	for _, key := range keys {
		carry = append(carry, fmt.Sprintf("%s=%v", key, sorted[key]))
	}

	return carry
}

func (v VerifyPayianoWebhookSignature) getSortedPayload(cleaned map[string]interface{}) map[string]interface{} {
	sorted := make(map[string]interface{})
	keys := make([]string, 0, len(cleaned))
	for key := range cleaned {
		keys = append(keys, key)
	}
	sort.Strings(keys)
	for _, key := range keys {
		sorted[key] = cleaned[key]
	}
	return sorted
}

func (v VerifyPayianoWebhookSignature) isValueNotEmpty(value interface{}) bool {
	switch value := value.(type) {
	case map[string]interface{}:
		for _, v := range value {
			if v != nil {
				return true
			}
		}
	case string:
		return len(strings.TrimSpace(value)) > 0
	case bool:
		return true
	default:
		return value != nil
	}
	return false
}

func main() {
	payloadString := `{"webhook_event":{"id":"01j3521znn3b6wderr4vbyq18n","type":"company.created","version":"v1","fired_at":"1722572118554"},"webhook_event_attempt":{"id":"01j354j6nkwh3mdvhs6dsmswt8","sent_at":"1722572118554"},"details":{"data":{"company":{"name":"Pyngy URL Shortenr","avatar":null,"is_active":true,"is_approved":false,"employees_count":0,"owners":[{"name":"Amgad Yassen","position":"CEO","percentage":51.5},{"name":"Kamal Allam","position":"CEO","percentage":48.5}],"description":"A leading company providing\n solutions for converting lengthy\n URLs into short ones & simplifying online sharing!","social_urls":{"facebook_url":"https://facebook.com/pyngy","linked_in_url":null}}}}}`

	var payload map[string]interface{}
	json.Unmarshal([]byte(payloadString), &payload)

	receivedSignature := "7159d656803a7136be897193dd70a48ca757786d0fe3531f33a48dc17d995725"
	secret := "OWlPF9plag9KEtYvw3EM+7UDrgXb84xjZPR2TvzJM1I="

	verifier := VerifyPayianoWebhookSignature{}
	fmt.Println(map[string]interface{}{
		"getSignatureText":     verifier.getSignatureText(payload),
		"getComputedSignature": verifier.getComputedSignature(payload, secret),
		"isVerifiedSignature":  verifier.isVerifiedSignature(payload, receivedSignature, secret),
	})
}
