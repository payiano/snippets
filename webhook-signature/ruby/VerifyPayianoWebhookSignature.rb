# Learn how to verify webhook event sent by Payiano API.
# @author Payiano Team <info@payiano.com>
# @see https://docs.payiano.com/api/rest/reference/webhooks/verify-signature

require 'json'
require 'openssl'
require 'active_support/security_utils'

class VerifyPayianoWebhookSignature
  def self.isVerifiedSignature(payload, receivedSignature, secret)
    computedSignature = getComputedSignature(payload, secret)
    secure_compare(computedSignature, receivedSignature)
  end

  def self.getComputedSignature(payload, secret)
    signatureText = getSignatureText(payload)
    OpenSSL::HMAC.hexdigest('sha256', secret, signatureText)
  end

  def self.getSignatureText(payload)
    flatten = getFlattenPayload(payload)
    cleaned = getCleanedPayload(flatten)
    sorted = getSortedPayload(cleaned)
    simplified = getSimplifiedPayload(sorted)
    simplified.join('&')
  end

  def self.getFlattenPayload(payload, parent = nil)
    carry = {}

    payload.each do |key, value|
      newKey = parent ? "#{parent}.#{key}" : key

      if value.is_a?(Hash)
        carry.merge!(getFlattenPayload(value, newKey))
      elsif value.is_a?(Array)
        value.each_with_index do |item, index|
          indexedKey = "#{newKey}.#{index}"
          if item.is_a?(Hash) || item.is_a?(Array)
            carry.merge!(getFlattenPayload(item, indexedKey))
          else
            carry[indexedKey] = item
          end
        end
      else
        carry[newKey] = value
      end
    end

    carry
  end

  def self.getCleanedPayload(flatten)
    carry = {}

    flatten.each do |key, value|
      if isValueNotEmpty(value)
        if value.is_a?(String)
          value = value.gsub(/[\r\n\s]+/, '')
        elsif !!value == value # check if boolean
          value = value ? 'true' : 'false'
        end
        carry[key] = value
      end
    end

    carry
  end

  def self.getSimplifiedPayload(sorted)
    sorted.map { |key, value| "#{key}=#{value}" }
  end

  def self.getSortedPayload(cleaned)
    cleaned.sort.to_h
  end

  def self.isValueNotEmpty(value)
    if value.is_a?(Hash)
      value.values.any? { |v| isValueNotEmpty(v) }
    elsif value.is_a?(String)
      !value.strip.empty?
    elsif !!value == value # check if boolean
      true
    else
      !value.nil?
    end
  end

  def self.secure_compare(a, b)
    ActiveSupport::SecurityUtils.secure_compare(a, b)
  end

  private_class_method :getFlattenPayload, :getCleanedPayload, :getSimplifiedPayload, :getSortedPayload, :isValueNotEmpty
end

# Example usage:
payloadString = '{"webhook_event":{"id":"01j3521znn3b6wderr4vbyq18n","type":"company.created","version":"v1","fired_at":"1722572118554"},"webhook_event_attempt":{"id":"01j354j6nkwh3mdvhs6dsmswt8","sent_at":"1722572118554"},"details":{"data":{"company":{"name":"Pyngy URL Shortenr","avatar":null,"is_active":true,"is_approved":false,"employees_count":0,"owners":[{"name":"Amgad Yassen","position":"CEO","percentage":51.5},{"name":"Kamal Allam","position":"CEO","percentage":48.5}],"description":"A leading company providing\n solutions for converting lengthy\n URLs into short ones & simplifying online sharing!","social_urls":{"facebook_url":"https://facebook.com/pyngy","linked_in_url":null}}}}}'

payload = JSON.parse(payloadString)
receivedSignature = "7159d656803a7136be897193dd70a48ca757786d0fe3531f33a48dc17d995725"
secret = "OWlPF9plag9KEtYvw3EM+7UDrgXb84xjZPR2TvzJM1I="

puts({
  "getSignatureText" => VerifyPayianoWebhookSignature.getSignatureText(payload),
  "getComputedSignature" => VerifyPayianoWebhookSignature.getComputedSignature(payload, secret),
  "isVerifiedSignature" => VerifyPayianoWebhookSignature.isVerifiedSignature(payload, receivedSignature, secret),
})
