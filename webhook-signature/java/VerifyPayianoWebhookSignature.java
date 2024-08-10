
/**
 * Learn how to verify webhook event sent by Payiano API.
 *
 * @author Payiano Team <info@payiano.com>
 * @see https://docs.payiano.com/api/rest/reference/webhooks/verify-signature
 */
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VerifyPayianoWebhookSignature {

    public static boolean isVerifiedSignature(Map<String, Object> payload, String receivedSignature, String secret) throws Exception {
        String computedSignature = getComputedSignature(payload, secret);
        return Arrays.equals(computedSignature.getBytes(StandardCharsets.UTF_8), receivedSignature.getBytes(StandardCharsets.UTF_8));
    }

    public static String getComputedSignature(Map<String, Object> payload, String secret) throws Exception {
        String signatureText = getSignatureText(payload);
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secretKey);
        return bytesToHex(sha256_HMAC.doFinal(signatureText.getBytes(StandardCharsets.UTF_8)));
    }

    public static String getSignatureText(Map<String, Object> payload) {
        Map<String, Object> flatten = getFlattenPayload(payload, null);
        Map<String, Object> cleaned = getCleanedPayload(flatten);
        Map<String, Object> sorted = getSortedPayload(cleaned);
        List<String> simplified = getSimplifiedPayload(sorted);
        return String.join("&", simplified);
    }

    public static Map<String, Object> getFlattenPayload(Map<String, Object> payload, String parent) {
        Map<String, Object> carry = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            String key = (parent != null) ? parent + "." + entry.getKey() : entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                carry.putAll(getFlattenPayload((Map<String, Object>) value, key));
            } else if (value instanceof List) {
                List<Object> list = (List<Object>) value;
                for (int index = 0; index < list.size(); index++) {
                    String indexedKey = key + "." + index;
                    Object item = list.get(index);
                    if (item instanceof Map || item instanceof List) {
                        carry.putAll(getFlattenPayload((Map<String, Object>) item, indexedKey));
                    } else {
                        carry.put(indexedKey, item);
                    }
                }
            } else {
                carry.put(key, value);
            }
        }
        return carry;
    }

    public static Map<String, Object> getCleanedPayload(Map<String, Object> flatten) {
        Map<String, Object> carry = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : flatten.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (isValueNotEmpty(value)) {
                if (value instanceof String) {
                    value = ((String) value).replaceAll("[\\r\\n\\s]+", "");
                } else if (value instanceof Boolean) {
                    value = value.toString();
                }
                carry.put(key, value);
            }
        }
        return carry;
    }

    public static Map<String, Object> getSortedPayload(Map<String, Object> cleaned) {
        return new TreeMap<>(cleaned);
    }

    public static List<String> getSimplifiedPayload(Map<String, Object> sorted) {
        List<String> carry = new ArrayList<>();
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            carry.add(entry.getKey() + "=" + entry.getValue());
        }
        return carry;
    }

    public static boolean isValueNotEmpty(Object value) {
        if (value instanceof Map) {
            for (Object v : ((Map<?, ?>) value).values()) {
                if (isValueNotEmpty(v)) {
                    return true;
                }
            }
            return false;
        } else if (value instanceof String) {
            return !((String) value).replaceAll("\\s+", "").isEmpty();
        } else if (value instanceof Boolean) {
            return value.equals(true) || value.equals(false);
        } else {
            return value != null;
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void main(String[] args) throws Exception {
        String payloadString = "{\"webhook_event\":{\"id\":\"01j3521znn3b6wderr4vbyq18n\",\"type\":\"company.created\",\"version\":\"v1\",\"fired_at\":\"1722572118554\"},\"webhook_event_attempt\":{\"id\":\"01j354j6nkwh3mdvhs6dsmswt8\",\"sent_at\":\"1722572118554\"},\"details\":{\"data\":{\"company\":{\"name\":\"Graply URL Shortenr\",\"avatar\":null,\"is_active\":true,\"is_approved\":false,\"employees_count\":0,\"owners\":[{\"name\":\"Amgad Yassen\",\"position\":\"CEO\",\"percentage\":51.5},{\"name\":\"Kamal Allam\",\"position\":\"CEO\",\"percentage\":48.5}],\"description\":\"A leading company providing\\n solutions for converting lengthy\\n URLs into short ones & simplifying online sharing!\",\"social_urls\":{\"facebook_url\":\"https://facebook.com/graply\",\"linked_in_url\":null}}}}}";

        Map<String, Object> payload = new ObjectMapper().readValue(payloadString, Map.class);
        String receivedSignature = "a621fc745416b00bb24758440fa75a850f6f8cb3f901217a6a9854f043ff8b70";
        String secret = "OWlPF9plag9KEtYvw3EM+7UDrgXb84xjZPR2TvzJM1I=";

        System.out.println("getSignatureText: " + VerifyPayianoWebhookSignature.getSignatureText(payload));
        System.out.println("getComputedSignature: " + VerifyPayianoWebhookSignature.getComputedSignature(payload, secret));
        System.out.println("isVerifiedSignature: " + VerifyPayianoWebhookSignature.isVerifiedSignature(payload, receivedSignature, secret));
    }
}
