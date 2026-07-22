# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [2.13.1] - 2026-07-21

### Added

### Changed
- Automate GitHub release creation from tags, including changelog release notes and signed APK/AAB assets.
- Clarify repository guidance for maintaining unreleased changelog entries.

### Deprecated

### Removed

### Fixed

### Security

## [2.8.2] 2024-11-12

### Added

### Changed

### Deprecated

### Removed

### Fixed
- Fix daily reminder being fired always

### Security

## [2.8.1] 2024-11-07

### Added

### Changed

### Deprecated

### Removed

### Fixed
- Missing copies
- Notification icon
- Haptic vibration for sorting tasks

### Security

## [2.8.0] - 2024-11-06

### Added
- New onboarding
- Daily notification

### Changed
- Revamped task detail design
- Revamped postpone task screen
- Moved today to the TopBar

### Deprecated

### Removed

### Fixed
- Order tasks on agenda

### Security

## [2.7.0] - 2024-10-10 

### Added
- Swipe to remove

### Changed
- New adaptative layout
- Task detail screen is used by default now
- New today indicator
- Cleaner and more condensed design
- Header is not expandable now, it's been replaced by the select day dialog

### Deprecated

### Removed

### Fixed

### Security

## [2.6.0] - 2024-08-02

### Added
- New task detail screen

### Changed

### Deprecated

### Removed

### Fixed
- Fix notification permission
- Fix schedule exact alarm permission 

### Security

## [2.5.2] - 2024-06-28 

### Added

### Changed
- Enhanced overall app performance
- Updated task management: Tasks are no longer automatically reordered when marked as done or undone. You can now customize the order of your tasks by dragging them to your preferred position

### Deprecated

### Removed

### Fixed
- Resolved an issue with reminder notifications that occasionally failed to appear on time due to Android's permissions. Notifications should now function reliably.

### Security

## [2.5.1] - 2024-02-14

### Added
- Now it's possible to apply changes to single or future related tasks when editing a recurring task 

### Changed

### Deprecated

### Removed

### Fixed
- Recurring tasks now work perfectly. There was a problem with removing recurring tasks

### Security

## [2.5.0] 2024-01-17

### Added
- Recurrent tasks

### Changed
- Improved week & month calendar navigation adding the ability to swipe to move backward and forward

### Deprecated

### Removed

### Fixed


## [2.4.0] 2023-12-07

### Added
- In-App review dialog that will be shown when the user completes a determinate amount of tasks

### Changed

### Deprecated

### Removed

### Fixed
- Fix edit task's day that caused issues when editing a task changing it's day when there were other tasks with the same position

## [2.3.0] 2023-11-30

### Added
- Add today indicator in the week calendar
- Add baseline profiles to improve performance

### Changed
- Minor improvements in the UI

### Deprecated

### Removed

### Fixed
- Fix edit task's day that caused issues when editing a task changing it's day when there were other tasks with the same position

### Security

## [2.2.0] 2023-10-23

### Added
- Add ability to order task dragging the cards on the agenda screen
- Add themed icon
- Add custom logger
- Configure build variants to differentiate between develop and production

### Changed

### Deprecated

### Removed

### Fixed
- Fix change due date when postponing a task
- Fix calendar expanded localization. Previously the week days were not localized at all and the month needed to recompose to show the proper translation. Now the whole calendar is reactive and recomposes as soon as the language gets changed via the system settings

### Security

## [2.1.0] 2023-10-19

### Added
- Add ability to postpone reminders dynamically, before only one hour was the only choice
- Add donation page for whoever wanna contribute with the project

### Changed
- Enhanced notification logic moving the code into a module to encapsulate it
- Use USE_EXACT_ALARM and add rationale when it SCHUDULE_EXACT_ALARM get revoked, just in case in the future we don't have access to USE_EXACT_ALARM you never know when it depends on Google :D
- Notification permission handling has been improved and now a rationale is shown when the user revokes the permission. Also, it's dynamic, as soon as the permission gets granted even via settings the app will be refreshed

### Deprecated

### Removed

### Fixed
- Fixed an issue that opened the app many times when tapping on notifications
- Fixed an issue when editing or postponing a reminder
- Fixed the ability to set reminders for the past which means that the notification is instantly fired

### Security


## [2.0.0] 2023-09-18

### Added
- Material 3 support with a shiny redesign
- Support foldable phones and tablets
- Re-order tasks
- Auto-postpone unfinished tasks to the next day
- New calendar view

## [0.7.0] 2022-06-22
### Added
- Edit task feature

## [0.6.0] 2022-05-07
### Changed
- Added more days to horizontal calendar on Agenda
- Changed colors of settings screen
- Upgraded time picker library version
### Fixed
- Pass selected date to date picker
- Show selected date's month when open date picker
- Tint save task button's icon properly
- Notification actions. Before was impossible to use different actions when having multiple reminder notifications

## [0.5.0] - 2022-05-03
### Added
- New home bottom menu
### Changed
- Improved create task screen (new date picker and reminder picker)
- Keyboard now opens automatically when opens create task screen and has proper IME action to create the task
### Fixed
- Fix minor UI issues with paddings

## [0.4.0] - 2022-03-19
### Added
- Settings screen
- Detekt
### Changed
- Mark today in calendar with a point
### Fixed
- Dark theme icons

## [0.3.0] - 2022-01-28
### Added
- Mark as done/undone in task action dialog
- Notification actions for done & postpone

## [0.2.1] - 2022-01-09
### Added
- Changelog
- Firebase analytics and Crashlytics
- GitHub Actions for CI

### Changed
- Notification color
- Task reminder now skips done tasks
- Chips content color
- Close keyboard when create a task

### Fixed
- Show create task bottom always as expanded
