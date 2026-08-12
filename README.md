# Bluetalk

**Bluetalk is an offline, ephemeral, peer-to-peer Android communication application designed for nearby Bluetooth messaging and large file transfers without internet infrastructure.**

Bluetalk is being developed incrementally. The previous web chat architecture has been removed; this repository is now an Android-only Kotlin project.

## Currently Implemented

- Native Android project structure using Kotlin.
- Jetpack Compose application shell.
- AndroidX ViewModel and StateFlow-based UI state.
- Minimal home screen showing Bluetooth/session status.
- Clean package identity: `com.bluetalk.app`.
- Bluetooth transport interfaces prepared for future Bluetooth Classic/RFCOMM work.
- In-memory session, message, transfer, protocol, and security boundaries.
- Android manifest permission declarations for future Bluetooth scanning, connection, and advertising.
- Backup/data extraction rules that avoid backing up app file data.

## Planned Functionality

Bluetalk does not yet perform Bluetooth discovery, Bluetooth connections, message transmission, file transfer, or encryption. Those features are planned for later phases.

Conversation data is intended to exist only during an active session. Bluetalk will not persist chat history to a local database, cloud backend, or server.

## Architecture

The project is organized around clear responsibility boundaries:

- `ui`: Compose screens, navigation, reusable UI components, and ViewModels.
- `bluetooth`: Bluetooth transport contracts and Android Bluetooth availability/permission foundation.
- `protocol`: Bluetalk packet representation and lightweight encoding/decoding foundation.
- `session`: In-memory session identity, members, and lifecycle state.
- `messaging`: In-memory message models and message state management.
- `transfer`: File-transfer models and state boundaries for future streaming/chunked transfers.
- `security`: Interfaces for future established cryptographic session handling.
- `model`: Shared app models such as device identity.

Future communication flow:

```text
User action
    -> UI/ViewModel
    -> session or messaging layer
    -> Bluetalk packet
    -> protocol encoder
    -> Bluetooth transport
    -> nearby Android device
```

## Roadmap

### Phase 0 - Android architecture migration

Convert the previous web application into the Bluetalk native Android architecture.

### Phase 1 - Bluetooth 1-to-1 connectivity

Two physical Android devices can:

- discover/connect
- establish RFCOMM communication
- exchange basic text bidirectionally

### Phase 2 - Bluetalk communication protocol

Implement structured packets, encoding/decoding, acknowledgements and protocol versioning.

### Phase 3 - Ephemeral private sessions

Implement temporary session identity, session codes, members and in-memory-only conversation state.

### Phase 4 - Complete chat experience

Implement:

- message bubbles
- usernames
- timestamps
- replies
- presence
- join/leave events
- delivery acknowledgements
- connection failure states

### Phase 5 - Large file transfer

Implement:

- streaming
- chunking
- progress
- cancellation
- resume/recovery
- transfer acknowledgement
- integrity verification

### Phase 6 - Session encryption and privacy hardening

Implement established cryptographic mechanisms for session communication and temporary session keys.

Destroy session keys/state when sessions end.

### Phase 7 - Multi-user Bluetooth sessions

Allow multiple nearby users to participate in a temporary Bluetalk session.

### Future research - Multi-hop communication

Experiment with:

```text
Phone A <-> Phone B <-> Phone C
```

where B can relay communication between devices that cannot directly reach one another.

Multi-hop communication is not part of the initial implementation phases.

## Build

Open the project in Android Studio and let Gradle sync, or build from the repository root with the included Gradle wrapper:

```powershell
.\gradlew.bat :app:assembleDebug
```

This project requires a modern Android build environment with JDK 17 and an installed Android SDK.
