# ApplicationLock-Android

## Overview
A Kotlin-based Android app that simulates application-level locking and protection. Designed for mobile security drills, this project demonstrates permission handling, persistent PIN storage, and package-level protection logic.

## Core Workflow
1. **Initial Setup**
   - Upon installation, users set a **PIN code** for the Application Lock itself.
   - This PIN persists across device restarts.

2. **Permission Activation**
   - App guides user to enable:
     - **Usage Access**
     - **Overlay Permission**
     - **Device Admin Access**

3. **App Lock Configuration**
   - User sets a separate **PIN code** for each target app to be locked.
   - Inputs the **package name** (e.g., `com.android.chrome`) for each app.
   - Starts protection — lock icon appears in the **notification panel**, indicating active monitoring.

4. **Persistence**
   - Both the main app PIN and individual app PINs remain active even after a device reboot.

## Tech Stack
- **Language**: Kotlin
- **Platform**: Android SDK
- **Architecture**: MVVM
- **Security Concepts**:
  - Permission handling
  - Persistent storage
  - App-level access control

## Project Structure
- `/ui` – User interface components and flows
- `/permissions` – Usage access, overlay, and admin logic
- `/lockcore` – PIN setup, validation, and persistence
- `/monitor` – Package name tracking and protection triggers

## Setup Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/egocedrick/ApplicationLock-Android
