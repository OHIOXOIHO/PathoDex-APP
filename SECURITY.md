# Security policy

## Reporting a vulnerability

Please report security vulnerabilities **privately**:

    ohioxoiho@gmail.com

Do not open a public issue for anything security-sensitive. Include a description,
the affected version, and steps to reproduce. You can expect an acknowledgement
within a few days.

## Scope

This repository contains only the Android client. The PathoDex service
(pathodex.ir) — its API, authentication and content pipeline — is a separate,
private codebase and is **not** in scope for reports here.

## Notes for reviewers

- The application contains **no secrets**. Google sign-in uses a public web client
  id; the server holds the credentials.
- The release keystore is kept outside this repository and must stay there.
- If you find what looks like a committed credential, treat it as a live secret:
  report it privately and do not quote it in a public issue.
