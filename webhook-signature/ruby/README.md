
# Verify Payiano Webhook Signature With Ruby

This project provides a Ruby implementation for verifying webhook signatures sent by the Payiano API using `HMAC-SHA256`.

## Prerequisites

Before running this example, ensure you have the following:

- **Ruby** installed on your machine.
- **ActiveSupport** gem installed for secure comparison (`gem install activesupport`).

## Setup Instructions

1. **Save the Ruby Script:**
Save the provided Ruby code to a file named `VerifyPayianoWebhookSignature.rb`.

2. **Run the Script:**
Open a terminal, navigate to the directory where the file is saved, and run the script using Ruby:

```bash
ruby VerifyPayianoWebhookSignature.rb
```

## Testing

The script will output the following information to the console:

- **getSignatureText**: The text used to compute the `HMAC` signature.
- **getComputedSignature**: The `HMAC-SHA256` signature computed from the payload and secret.
- **isVerifiedSignature**: Whether the computed signature matches the received signature.

If everything is set up correctly, you should see output similar to this:

```
{"getSignatureText"=>"details.data.company.description=AleadingcompanyprovidingsolutionsforconvertinglengthyURLsintoshortones&simplifyingonlinesharing!&details.data.company.employees_count=0&details.data.company.is_active=true&details.data.company.is_approved=false&details.data.company.name=PyngyURLShortenr&details.data.company.owners.0.name=AmgadYassen&details.data.company.owners.0.percentage=51.5&details.data.company.owners.0.position=CEO&details.data.company.owners.1.name=KamalAllam&details.data.company.owners.1.percentage=48.5&details.data.company.owners.1.position=CEO&details.data.company.social_urls.facebook_url=https://facebook.com/pyngy&webhook_event.fired_at=1722572118554&webhook_event.id=01j3521znn3b6wderr4vbyq18n&webhook_event.type=company.created&webhook_event.version=v1&webhook_event_attempt.id=01j354j6nkwh3mdvhs6dsmswt8&webhook_event_attempt.sent_at=1722572118554", "getComputedSignature"=>"7159d656803a7136be897193dd70a48ca757786d0fe3531f33a48dc17d995725", "isVerifiedSignature"=>true}
```

## Troubleshooting

- Ensure Ruby is correctly installed and configured.
- Double-check that the correct file path is being used when running the script.
- Ensure the payload structure is consistent and that keys are ordered correctly.

## License

This project is open-source and available under the MIT License.
