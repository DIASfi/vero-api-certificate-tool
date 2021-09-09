# VERO API CERTIFICATE TOOL PRE ALPHA

## Manual test

Initial implementation test with  Vero prodided Private Keys

0. Build

```
$ ./scripts/build
```


1. Request new certificate

```
release/certificate new tmp/test-run.jks --customer-id="0123456-7" --customer-name="Ab PKI Developer Company Oy" -e TEST --key-store-password="password" --transfer-id="12345678903" --transfer-password="Pw8a1d4u3HhOqhlo" --key-store-alias=2021-09-09-12846 --use-existing-private-key="test/SignNewCertificate_Private.key"
```

2. Renew requested certificate with previous certificate

```
$ release/certificate renew tmp/test-run.jks --customer-id="0123456-7" --customer-name="Ab PKI Developer Company Oy" -e TEST --key-store-password="password" --key-store-alias=2021-09-09-12846 -v --use-existing-private-key="test/RenewCertificate_Private.key"
```
