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
 
## Features
- Set up PIN for the app itself (8 digits).
- Assign PINs to other applications (8 digits).
- Add/remove applications for locking via buttons.
- Usage access and overlay permission handling.
- Enable/disable device admin to prevent uninstallation.
- Start/stop protection toggle for all locked apps.
- Foreground lock screen overlay.
- Locking mechanism persists after device restart.

## Impact
- Provides controlled access to company ticketing applications.
- Protects sensitive client files from unauthorized use.
- Prevents installation of unauthorized apps and access to malicious websites.
- Restricts camera and settings modifications without consent.
- Ensures company apps cannot be uninstalled intentionally.


## Project Structure
- `/ui` – User interface components and flows
- `/permissions` – Usage access, overlay, and admin logic
- `/lockcore` – PIN setup, validation, and persistence
- `/monitor` – Package name tracking and protection triggers

## Security Methodology

This app was built and hardened using the 
**[BBS Framework](https://github.com/egocedrick/BBS-Framework)** 
— a personal Build-Break-Secure methodology for mobile development.

The Safe Mode vulnerability discovered in this app 
is what directly inspired the creation of BBS.

## Setup Instructions
1. Clone the repository:
   ```bash
   git clone https://github.com/egocedrick/ApplicationLock-Android
