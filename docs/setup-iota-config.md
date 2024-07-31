# Setup Iota Configuration

A framework that provides a secured and simplified data-sharing process from Affinidi Vault with user consent for enhanced user experience.
The Affinidi Iota Framework leverages the OID4VP (OpenID for Verifiable Presentation) standard to request and receive data from Affinidi Vault. The OID4VP is built with the OAuth 2.0 authorisation framework, providing developers with a simple and secure presentation of credentials.

## Create Iota configuration

When integrating with the Affinidi Iota Framework, developers must create a Configuration first, where they configure the Wallet used for signing the Request Token, the Request Token expiration to enhance security, and Presentation Definitions to query the data from the Affinidi Vault that users will consent to share.

1. Go to [Affinidi Portal](https://portal.affinidi.com/login) and click on the Affinidi Iota Framework page.

2. Click on Create Configuration and set the following fields:
  - Wallet: Create a new wallet and provide the new wallet name, or select an existing Wallet that will sign and issue the credentials to the user.
  - Vault JWT Expiration time: Credential Offers have a limited lifetime to enhance security. Consumers must claim the offer within this timeframe.

3. Optionally, you can configure whether to enable:

  - Enable Verification: To verify the credentials the user shares using the Credential Verification service.
  - Enable Consent Audit Log: To store the consent given by the user whenever they share data with the website.

4. After setting the fields and providing the list of the supported schema, click **Create**.

5. After creating the configuration, define the Presentation Definitions to query specific data from the Affinidi Vault. We will use Presentation Exchange to do this.

6. Create Presentations definitions for following queries
  - Address VC
  - Gender, Date of birth VC
  - Email VC



## PEX for the above Querys

1. Address VC

```
{
  "id": "vp_combined_email_user_profile_combined",
  "input_descriptors": [
    {
      "id": "address",
      "name": "address",
      "purpose": "Check if VC data contains necessary fields",
      "constraints": {
        "fields": [
          {
            "path": [
              "$.type"
            ],
            "purpose": "Check if VC type is correct",
            "filter": {
              "type": "array",
              "contains": {
                "type": "string",
                "pattern": "HITAddress"
              }
            }
          },
          {
            "path": [
              "$.credentialSubject.formatted"
            ]
          },
          {
            "path": [
              "$.credentialSubject.locality"
            ]
          },
          {
            "path": [
              "$.credentialSubject.postalCode"
            ]
          },
          {
            "path": [
              "$.credentialSubject.country"
            ]
          }
        ]
      }
    }
  ]
}
```

2. Gender, Date of birth VC

```
{
  "id": "token_with_identity_fullname_vc",
  "input_descriptors": [
    {
      "id": "profile_identity_birthdate",
      "name": "Profile Identity",
      "purpose": "Check if data contains necessary fields",
      "constraints": {
        "fields": [
          {
            "path": [
              "$.type"
            ],
            "purpose": "Check if VC type is correct",
            "filter": {
              "type": "array",
              "contains": {
                "type": "string",
                "pattern": "HITBirthdate"
              }
            }
          },
          {
            "path": [
              "$.credentialSubject.birthdate"
            ]
          }
        ]
      }
    },
    {
      "id": "profile_identity_gender",
      "name": "Profile Identity",
      "purpose": "Check if data contains necessary fields",
      "constraints": {
        "fields": [
          {
            "path": [
              "$.type"
            ],
            "purpose": "Check if VC type is correct",
            "filter": {
              "type": "array",
              "contains": {
                "type": "string",
                "pattern": "HITGender"
              }
            }
          },
          {
            "path": [
              "$.credentialSubject.gender"
            ]
          }
        ]
      }
    }
  ]
}
```

3. Email VC

```
{
  "id": "token_with_email_vc",
  "input_descriptors": [
    {
      "id": "email_vc",
      "name": "Email VC",
      "purpose": "Check if Vault contains the required VC.",
      "constraints": {
        "fields": [
          {
            "path": [
              "$.type"
            ],
            "purpose": "Check if VC type is correct",
            "filter": {
              "type": "array",
              "contains": {
                "type": "string",
                "pattern": "Email"
              }
            }
          },
          {
            "path": [
              "$.credentialSubject.email"
            ],
            "purpose": "Check if VC contains email field",
            "filter": {
              "type": "string"
            }
          },
          {
            "path": [
              "$.issuer"
            ],
            "purpose": "Check if VC Issuer is Trusted",
            "filter": {
              "type": "string",
              "pattern": "^did:key:zQ3shtMGCU89kb2RMknNZcYGUcHW8P6Cq3CoQyvoDs7Qqh33N"
            }
          }
        ]
      }
    }
  ]
}
```