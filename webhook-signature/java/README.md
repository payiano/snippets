# Verify Payiano Webhook Signature With Java

This project provides a simple Java example to verify webhook signatures using `HMAC-SHA256`. The example uses the Jackson library to parse JSON payloads.

## Prerequisites

Before running this example, ensure you have the following:

- **Java Development Kit (JDK)** installed on your machine.
- The following **Jackson library JAR files**:
- jackson-core-2.15.2.jar (https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar)
- jackson-databind-2.15.2.jar (https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar)
- jackson-annotations-2.15.2.jar (https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar)

## Setup Instructions

1. **Download the Required JAR Files:**

Download the necessary Jackson library JAR files from the links provided above.

2. **Place the JAR Files and Java File:**

Place the downloaded JAR files and the `VerifyPayianoWebhookSignature.java` file in the same directory.

3. **Compile the Java File:**

Open a terminal or command prompt and navigate to the directory where you placed the files. Use the following command to compile the Java file:

```shell
javac -cp .:jackson-core-2.15.2.jar:jackson-databind-2.15.2.jar:jackson-annotations-2.15.2.jar VerifyPayianoWebhookSignature.java
VerifyPayianoWebhookSignature
```

Note: On Windows, replace `:` with `;` in the classpath:
```shell
javac -cp .;jackson-core-2.15.2.jar;jackson-databind-2.15.2.jar;jackson-annotations-2.15.2.jar VerifyPayianoWebhookSignature.java
VerifyPayianoWebhookSignature
```

4. **Run the Java Program:**

After compiling the file, run the program using the following command:

```shell
java -cp .:jackson-core-2.15.2.jar:jackson-databind-2.15.2.jar:jackson-annotations-2.15.2.jar VerifyPayianoWebhookSignature
```

Note: On Windows, replace `:` with `;` in the classpath:

```shell
java -cp .;jackson-core-2.15.2.jar;jackson-databind-2.15.2.jar;jackson-annotations-2.15.2.jar VerifyPayianoWebhookSignature
```

## Testing

The program will output the following information to the console:

- **Signature Text**: The text used to compute the `HMAC` signature.
- **Computed Signature**: The `HMAC-SHA256` signature computed from the payload and secret.
- **Verified Signature**: Whether the computed signature matches the received signature.

If everything is set up correctly, you should see output similar to this:

```
getSignatureText: details.data.company.description=AleadingcompanyprovidingsolutionsforconvertinglengthyURLsintoshortones&simplifyingonlinesharing!&details.data.company.employees_count=0&details.data.company.is_active=true&details.data.company.is_approved=false&details.data.company.name=PyngyURLShortenr&details.data.company.owners.0.name=AmgadYassen&details.data.company.owners.0.percentage=51.5&details.data.company.owners.0.position=CEO&details.data.company.owners.1.name=KamalAllam&details.data.company.owners.1.percentage=48.5&details.data.company.owners.1.position=CEO&details.data.company.social_urls.facebook_url=https://facebook.com/pyngy&webhook_event.fired_at=1722572118554&webhook_event.id=01j3521znn3b6wderr4vbyq18n&webhook_event.type=company.created&webhook_event.version=v1&webhook_event_attempt.id=01j354j6nkwh3mdvhs6dsmswt8&webhook_event_attempt.sent_at=1722572118554
getComputedSignature: 7159d656803a7136be897193dd70a48ca757786d0fe3531f33a48dc17d995725
isVerifiedSignature: true
```

## Troubleshooting

- Ensure that all the required JAR files are in the same directory as the Java file.
- Double-check that the correct classpath is being used in the compile and run commands.
- Verify that your JDK is correctly installed and configured.

## License

This project is open-source and available under the MIT License.
