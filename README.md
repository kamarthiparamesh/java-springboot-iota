# Introduction

This is a reference app generated by Affinidi CLI tool with Java and Springboot framework

## Prerequisite

1. Make sure you have [JAVA 21](https://jdk.java.net/21/) installed on your machine. Run the following command in a terminal session to check the java version

```sh
   java --version
```

You should get an output as below. If not, please reinstall java21 using the link above

```sh
openjdk 21.0.2 2024-01-16
```

2. Ensure that your JAVA_HOME environment variable is set to the installation directory

```sh
export JAVA_HOME="/usr/local/opt/openjdk"
```

3. Optionally you can add this entry to your respective profile (~/.bashrc or ~/.zshrc)

## Getting Started

Setting up the reference app is easy, just follow these steps:

1. Install the dependencies

```sh
sh mvnw clean
sh mvnw install
```

2. Update application environment variables

Create a .env file for your application to hold environment variables

```sh
cp .env.example .env
```

3. Click here to [Set up your environment variables for Affinidi Login configuration](#set-up-your-affinidi-login-configuration)

4. Click here to [Set up your Personnel Access Token to interact with Affinidi services](#setup-personal-access-token)

5. Click here to [Set up your Credential Issuance Configuration](#setup-credential-issuance-configuration)

6. Click here to [Set up your Affinidi Iota Framework Configuration](#setup-affinidi-iota-request)

## Build and run the project:

```sh
sh mvnw spring-boot:run
```

Then visit: http://localhost:8080/ to browse the reference app

## Set up your Affinidi Login configuration

Learn more about [here](https://docs.affinidi.com/docs/affinidi-login/login-configuration/).

1. Follow [this guide](./docs/setup-login-config.md) to set up your login configuration with callback URL as `http://localhost:8080/login/oauth2/code/javademo`

2. Copy your **Client ID**, **Client Secret** and **Issuer** from your login configuration and paste them into your `.env` file:

```ini
PROVIDER_CLIENT_ID="<CLIENT_ID>"
PROVIDER_CLIENT_SECRET="<CLIENT_SECRET>"
PROVIDER_ISSUER="<ISSUER>"
```

## Setup Personal Access Token

Follow [this guide](./docs/create-pat.md) to set up your Personnel Access Token.
Learn more about [here](https://docs.affinidi.com/dev-tools/affinidi-cli/manage-token/).

# Setup Credential Issuance Configuration

To issue a Verifiable Credential, it is required to setup the **Issuance Configuration** on your project, where you select the **issuing wallet** and **supported schemas** to create a credential offer that the application issue

Learn more about [here](https://docs.affinidi.com/docs/affinidi-elements/credential-issuance/).

You can easily do this using the [Affinidi Portal](https://portal.affinidi.com)

1. Go to [Affinidi Portal](https://portal.affinidi.com).

2. Open `Wallets` menu under the `Tools` section and click on `Create Wallet` with any name (e.g. `MyWallet`) and DID method as `did:key`.
   ![alt text](./docs/cis-image/wallet-create.png)

For more information, refer to the [Wallets documentation](https://docs.affinidi.com/dev-tools/wallets)

3. Go to `Credential Issuance Service` under `Services` section.

4. Click on `Create Configuration` and set the following fields:

   `Issuing Wallet`: Select Wallet Created previous step
   `Lifetime of Credential Offer` as `600`

5. Add schemas by clicking on "Add new item" under `Supported Schemas`

Schema 1 :

- _Schema_ as `Manual Input`,
- _Credential Type ID_ as `InsuranceRegistration`
- _JSON Schema URL_ as `https://schema.affinidi.io/TtestschemaIsusdfsfsfdV1R0.json`
- _JSDON-LD Context URL_ = `https://schema.affinidi.io/TtestschemaIsusdfsfsfdV1R0.jsonld`

Sample Configuration
![alt text](./docs/cis-image/cis-configuration.png)

6. Open [http://localhost:8080/issuance](http://localhost:8080/issuance) and start VC issuing a VC for _Insurance Registration_

# Setup Affinidi Iota Request

A framework that provides a secured and simplified data-sharing process from Affinidi Vault with user consent for enhanced user experience.
The Affinidi Iota Framework leverages the OID4VP (OpenID for Verifiable Presentation) standard to request and receive data from Affinidi Vault. The OID4VP is built with the OAuth 2.0 authorisation framework, providing developers with a simple and secure presentation of credentials.

Learn more about [here](https://docs.affinidi.com/frameworks/iota-framework/).

## Create Iota configuration

When integrating with the Affinidi Iota Framework, developers must create a Configuration first, where they configure the Wallet used for signing the Request Token, the Request Token expiration to enhance security, and Presentation Definitions to query the data from the Affinidi Vault that users will consent to share.

1. Go to [Affinidi Portal](https://portal.affinidi.com/login) and click on the Affinidi Iota Framework page.

2. Click on Create Configuration and set the following fields:

- Wallet: Create a new wallet and provide the new wallet name, or select an existing Wallet that will sign and issue the credentials to the user.
- Vault JWT Expiration time: Credential Offers have a limited lifetime to enhance security. Consumers must claim the offer within this timeframe.

3. Select `Data Sharing Flow Mode` as `Redirect` with redirect URL as `http://localhost:8080/iota`

4. Optionally, you can configure whether to enable:

- Enable Verification: To verify the credentials the user shares using the Credential Verification service.
- Enable Consent Audit Log: To store the consent given by the user whenever they share data with the website.

5. After creating the configuration, define the Presentation Definitions to query specific data from the Affinidi Vault.

6. Create Presentations definitions for requesting `InsuranceRegistration` VC

```Json
{
  "id": "InsuranceRegistration",
  "input_descriptors": [
    {
      "id": "insurance_vc",
      "name": "Insurance Registration VC",
      "purpose": "Check VC",
      "constraints": {
        "fields": [
          {
            "path": [
              "$.type"
            ],
            "purpose": "VC Type Check",
            "filter": {
              "type": "array",
              "contains": {
                "type": "string",
                "pattern": "^InsuranceRegistration"
              }
            }
          }
        ]
      }
    }
  ]
}
```

## Test Iota Request

1. Update `.env` file with the `ConfigurationId` and `QueryId` obtained in previous step

```
IOTA_CONFIG_ID=""
IOTA_QUERY_ID=""
```

2. Open [http://localhost:8080/iota](http://localhost:8080/iota) and request for VC based on the PEX.
