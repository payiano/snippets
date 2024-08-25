const crypto = require('crypto')

/**
 * Learn how to verify webhook event sent by Payiano API.
 *
 * @author Payiano Team <info@payiano.com>
 * @see https://docs.payiano.com/api/rest/reference/webhooks/verify-signature
 */

/**
 * Check if value is not empty.
 *
 * @param  {mixed} value
 * @return {bool}
 */
const isValueNotEmpty = (value) => {
  if (value && [Object, Array].includes(value.constructor)) {
    const values = Object.values(value)
    return !!values.length && values.some(isValueNotEmpty)
  } else if (typeof value === 'string') {
    return !!value.replace(/\s+/g, '').length
  } else if (value && typeof value === 'boolean') {
    return [true, false].includes(value)
  } else if ([null, undefined].includes(value)) {
    return false
  }

  return true
}

/**
 * Get flatten payload.
 *
 * @param  {mixed} payload
 * @param  {string} parent
 * @return {object}
 */
const getFlattenPayload = (payload, parent = null) =>
  Object.entries(payload).reduce((carry, [key, value]) => {
    key = parent ? `${parent}.${key}` : key

    if (value && [Object, Array].includes(value?.constructor)) {
      Object.assign(carry, {
        ...getFlattenPayload(value, key)
      })
    } else {
      carry[key] = value
    }

    return carry
  }, {})

/**
 * Get cleaned payload.
 *
 * @param  {mixed} flatten
 * @return {object}
 */
const getCleanedPayload = (flatten) =>
  Object.entries(flatten).reduce((carry, [key, value]) => {
    if (isValueNotEmpty(value)) {
      carry[key] =
        typeof value === 'string' ? value.replace(/[\r\n\s]/g, '') : value
    }

    return carry
  }, {})

/**
 * Get sorted payload.
 *
 * @param  {object} cleaned
 * @return {object}
 */
const getSortedPayload = (cleaned) => {
  const sortedKeys = Object.keys(cleaned).sort()

  return sortedKeys.map((key) => ({
    key,
    value: cleaned[key]
  }))
}

/**
 * Get simplified payload.
 *
 * @param  {object} sorted
 * @return {object}
 */
const getSimplifiedPayload = (sorted) =>
  sorted.map(({ key, value }) => `${key}=${value}`)

/**
 * Get signature text.
 *
 * @param  {object} payload
 * @return {object}
 */
const getSignatureText = (payload) => {
  const flatten = getFlattenPayload(payload)

  const cleaned = getCleanedPayload(flatten)

  const sorted = getSortedPayload(cleaned)

  const simplified = getSimplifiedPayload(sorted)

  return simplified.join('&')
}

/**
 * Get computed signature.
 *
 * @param  {object} payload
 * @param  {string} secret
 * @return {string}
 */
const getComputedSignature = (payload, secret) => {
  const signatureText = getSignatureText(payload)

  return crypto.createHmac('sha256', secret).update(signatureText).digest('hex')
}

/**
 * Check if signature is verified.
 *
 * @param  {object} payload
 * @param  {string} receivedSignature
 * @param  {string} secret
 * @return {bool}
 */
const isVerifiedSignature = (payload, receivedSignature, secret) => {
  const computedSignature = getComputedSignature(payload, secret)

  return crypto.timingSafeEqual(
    Buffer.from(computedSignature),
    Buffer.from(receivedSignature)
  )
}

// Find the following example:
const payloadString =
  '{"webhook_event":{"id":"01j3521znn3b6wderr4vbyq18n","type":"company.created","version":"v1","fired_at":"1722572118554"},"webhook_event_attempt":{"id":"01j354j6nkwh3mdvhs6dsmswt8","sent_at":"1722572118554"},"details":{"data":{"company":{"name":"Pyngy URL Shortenr","avatar":null,"is_active":true,"is_approved":false,"employees_count":0,"owners":[{"name":"Amgad Yassen","position":"CEO","percentage":51.5},{"name":"Kamal Allam","position":"CEO","percentage":48.5}],"description":"A leading company providing\\n solutions for converting lengthy\\n URLs into short ones & simplifying online sharing!","social_urls":{"facebook_url":"https://facebook.com/pyngy","linked_in_url":null}}}}}'

const payload = JSON.parse(payloadString)

const receivedSignature =
  '7159d656803a7136be897193dd70a48ca757786d0fe3531f33a48dc17d995725'

const secret = 'OWlPF9plag9KEtYvw3EM+7UDrgXb84xjZPR2TvzJM1I='

console.log({
  getSignatureText: getSignatureText(payload),
  getComputedSignature: getComputedSignature(payload, secret),
  isVerifiedSignature: isVerifiedSignature(payload, receivedSignature, secret)
})
