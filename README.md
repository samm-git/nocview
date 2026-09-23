# NOC View

An Android client for **Nagios Core** that shows host and service status and can
acknowledge problems, built with Jetpack Compose and Material 3.

> "Nagios" is a third-party trademark and is intentionally **not** used in the
> app name, package id, or display strings.

## Features

- **Status at a glance** — bottom-nav tabs for **Hosts / Services / Problems**
  with a summary header showing live state counts.
- **Color coding** — green for OK/UP, red for CRITICAL/DOWN, amber for
  WARNING/UNREACHABLE, purple for UNKNOWN; badges for acknowledged and
  scheduled-downtime objects.
- **Filters** — tap a summary box to filter by state; search the lists.
- **Details** — host detail lists its services; service detail shows output,
  long output, performance data, check timing and state history.
- **Acknowledge** — acknowledge hosts and services (with a comment) through the
  external command interface.
- **Refresh** — pull-to-refresh plus an optional auto-refresh interval.
- **Offline cache** — last-known status is cached in Room and shown when the
  server is unreachable, with a "last updated" banner.
- **Home-screen widget** — host/service/problem totals, refreshed periodically.

## Requirements

- Android 12 (API 31) or newer
- A reachable Nagios Core server exposing the CGI JSON interface
  (`statusjson.cgi`) and, for acknowledgement, the command CGI (`cmd.cgi`)

## Build

Requires **JDK 17+** (JDK 21 recommended) and the Android SDK (compile/target
SDK 35). Create `local.properties` with your SDK path, e.g.:

```properties
sdk.dir=/path/to/Android/sdk
```

Then:

```bash
./gradlew :app:assembleDebug          # build the debug APK
./gradlew :app:testDebugUnitTest      # run unit tests
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Configuration

Open the **Settings** tab and set:

- **Monitoring web root URL** — e.g. `http://your-nagios-host/nagios`
- **Username** / **Password** — HTTP Basic credentials
- **Trust self-signed certificates** — for HTTPS servers with self-signed certs
- **Auto-refresh interval** — optional periodic refresh

Credentials are stored in `EncryptedSharedPreferences`. Use **Test connection**
to verify the URL and credentials.

## API notes

- Always sends `formatoptions=enumerate`; the numeric `status` field is
  unreliable in Nagios Core 4.5.x. Enumerated values return textual states
  (`"up"`, `"ok"`, `"hard"`, ...).
- Queries used: `hostcount`, `servicecount`, `hostlist`, `servicelist`, `host`,
  `service`, `programstatus`.
- Acknowledgement uses `cmd.cgi` (GET to obtain the CSRF form id, then a form
  POST), since the command interface is not part of `statusjson.cgi`.

## Stack

- Kotlin + Jetpack Compose + Material 3, Navigation Compose
- Hilt (dependency injection)
- OkHttp + Retrofit + kotlinx.serialization
- Room (offline cache)
- Jetpack Security `EncryptedSharedPreferences`
- Glance + WorkManager (home-screen widget)
- MVVM + StateFlow + coroutines/Flow

## License

Released under the [MIT License](LICENSE).
