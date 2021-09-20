# Vero API Certificate Tool

This tool is for creating new certificate and renewing them which are needed to interact with Vero API's.

[![Run Tests](https://github.com/DIASfi/vero-api-certificate-tool/actions/workflows/on-push.yaml/badge.svg)](https://github.com/DIASfi/vero-api-certificate-tool/actions/workflows/on-push.yaml)

## Installation

1. Ensure that you running *NIX system and have Java 11+ installed
2. Download latest executable from [https://github.com/DIASfi/vero-api-certificate-tool/releases](https://github.com/DIASfi/vero-api-certificate-tool/releases)
3. Add execution rights `$ chmod +x certificate`
4. Add it to your `PATH` or not
5. Try it `$ certificate -V`

## Usage

### Request new certificate

#### General information

```shell
$ certificate new -h
```

#### Example

```shell
$ certificate new -v --env="TEST" \
  --customer-id="7017233-8" \
  --customer-name="Testifirma OY" \
  # Password used for all JKS actions \
  --key-store-password="password" \
  --transfer-id="92090786525592125851283719040090" \
  --transfer-password="LJ3taJTi29fechaD" \
  # Alias used when saving entries to JKS \
  --key-store-alias="2021-09-09-00001" \
  new.jks
```

### Renew certificate using existing certificate

#### General information

```shell
$ certificate renew -h
```

#### Example

```shell
$ certificate renew -v --env="TEST" \
  --customer-id="7017233-8" \
  --customer-name="Testifirma OY" \
  # Password used for all JKS actions \
  --key-store-password="password" \
  # Alias which is used for reading private key and certificate from JKS for signing renew request \
  --key-store-alias="2021-09-09-00001" \
  existing.jks
```


## Manual test against "Vero-testipenkki"

Initial implementation test with Vero provided Private Keys

0. Build

```shell
$ ./scripts/build
```

1. Request new certificate

```shell
$ release/certificate new -v --env="DEV" \
  --customer-id="0123456-7" \
  --customer-name="Ab PKI Developer Company Oy" \
  --key-store-password="password" \
  --transfer-id="12345678903" \
  --transfer-password="Pw8a1d4u3HhOqhlo" \
  --key-store-alias="2021-09-09-12846" \
  --use-existing-private-key="test/SignNewCertificate_Private.key" \
  tmp/test-run.jks
```

2. Renew requested certificate with previous certificate

```shell
$ release/certificate renew -v --env="DEV" \
  --use-existing-private-key="test/RenewCertificate_Private.key" \
  --customer-id="0123456-7" \
  --customer-name="Ab PKI Developer Company Oy" \
  --key-store-password="password" \
  --key-store-alias="2021-09-09-12846" \
  tmp/test-run.jks
```
