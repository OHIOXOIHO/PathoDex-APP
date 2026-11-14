# Contributing

Thanks for taking the time to contribute.

## Before you start

- **Open an issue first** for anything non-trivial, so the approach can be agreed
  before you spend time on it.
- Content changes (dossier text, taxonomy, images) belong to the PathoDex service,
  not this repository — the app only displays them. Please raise those at
  [pathodex.ir](https://pathodex.ir/about) instead.

## Getting the project running

See [README](README.md#building) for the toolchain and build commands.

```bash
gradle testDebugUnitTest    # run before you open a pull request
```

## Style

- Kotlin, Jetpack Compose, Material 3. Keep the design tokens in the theme layer —
  do not hard-code colours, spacing or type sizes in screens.
- The interface is Persian and **right-to-left**. Keep `LayoutDirection.Rtl`; Latin
  names go in an LTR container.
- Match the surrounding code. No drive-by reformatting.

## Pull requests

- One change per pull request.
- Describe *why*, not just *what*.
- Make sure the unit tests pass.

## Reporting a bug

Include the app version, Android version, device, and the exact steps to reproduce.
For search problems, include the query you typed and what you expected to find.

## Security

Please **do not** open a public issue for a security vulnerability. Email
ohioxoiho@gmail.com instead. See [SECURITY](SECURITY.md).
