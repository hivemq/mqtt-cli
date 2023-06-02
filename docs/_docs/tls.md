---
title: TLS
nav_order: 11
redirect_from: /docs/tls.html
---

# TLS

***

## Configuration

The MQTT CLI allows both TLS and mutual TLS (mTLS) to establish a secure connection.

It can be configured either by using the command line options
(e.g. [Publish TLS-Authentication](publish.md#tls-authentication)) or via the properties configuration file
([Configuration](configuration.md)).

In order to use TLS with your default values inside the properties configuration file, simply add `-s` or `--secure`.

**NOTE**: TLS command line options will always override default properties configurations.

**NOTE**: It is also possible to mix between styles of configurations e.g. using a default properties truststore and
having a keystore or client certificate and private key provided via the command line option.

***

## Versions

Supported/enabled TLS versions are JDK dependent. The MQTT CLI was tested with the following versions:

- `TLSv1.2` (default)
- `TLSv1.3`

**NOTE**: `TLSv1.3` was back-ported for Java 8 in later versions of multiple distributions.

***

## Cipher Suites

The supported/enabled cipher suites are different for TLS versions and are JDK dependent. The MQTT CLI was tested with
the following cipher suites:

### TLS 1.2

- `TLS_RSA_WITH_AES_128_CBC_SHA`
- `TLS_DHE_RSA_WITH_AES_128_CBC_SHA`
- `TLS_RSA_WITH_AES_256_CBC_SHA`
- `TLS_DHE_RSA_WITH_AES_256_CBC_SHA`
- `TLS_RSA_WITH_AES_128_CBC_SHA256`
- `TLS_RSA_WITH_AES_256_CBC_SHA256`
- `TLS_DHE_RSA_WITH_AES_128_CBC_SHA256`
- `TLS_DHE_RSA_WITH_AES_256_CBC_SHA256`
- `TLS_RSA_WITH_AES_128_GCM_SHA256`
- `TLS_RSA_WITH_AES_256_GCM_SHA384`
- `TLS_DHE_RSA_WITH_AES_128_GCM_SHA256`
- `TLS_DHE_RSA_WITH_AES_256_GCM_SHA384`
- `TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA`
- `TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA`
- `TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256`
- `TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384`
- `TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256`
- `TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384`
- `TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256`
- `TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256`

### TLS 1.3

- `TLS_AES_128_GCM_SHA256`
- `TLS_AES_256_GCM_SHA384`
- `TLS_CHACHA20_POLY1305_SHA256`

***

## Formats and Encryption Algorithms

There are several different formats for keystore (and truststore) archives as well as certificate and private key
files. Additionally, private keys can also be encrypted with different encryption algorithms. The MQTT CLI tries to
combine usability, by accepting multiple formats and encryption algorithms, with convention, by only accepting files
with
generally agreed file endings. This enables users to quickly identify and understand TLS configurations.

### Keystores and Truststores

Supported archive types:

- PKCS#12 (`.p12`, `.pfx`)
- JKS (`.jks`)

#### Additional information

- Although occasionally used, it is discouraged to use keystores without a password. Therefor, it is not supported by
  the MQTT CLI.
- JKS uses a proprietary format and is considered to be deprecated and replaced by PKCS#12. To achieve optimal
  interoperability with the HiveMQ broker, JKS is still supported by the MQTT CLI.
- JKS supports different passwords for keystore and private key passwords. The MQTT CLI supports this behavior and will
  ask explicitly if the private key passwords is different.

### Certificates

Certificates can be stored as standalone files. They usually follow the X.509 standard and can be
represented in multiple ways. The MQTT CLI supports the following formats:

- ASN.1 structured, binary certificates encoded with DER (`.cer`, `.crt`)
- ASN.1 structured, Base64-encoded DER certificates (`.pem`)

**NOTE**: File endings are not standardised, one could also store a Base64-encoded certificate inside a `.cer` file.
While this is discouraged, to avoid content encoding inconsistencies, the MQTT CLI will still accept those certificates.

### Private Keys

Private keys can be stored as standalone files. There are multiple standards available to represent private keys as well
as multiple ways to encrypt the private key. The MQTT CLI supports the following standards:

- ASN.1 structured, Base64-encoded DER (`.pem`)
    - PKCS#1
        - unencrypted
        - aes256
        - des3
    - PKCS#8
        - unencrypted
        - aes256
        - des3

**NOTE**: The MQTT CLI does currently not support binary private keys encoded with DER (`.der`).
