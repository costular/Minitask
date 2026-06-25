#!/usr/bin/env python3
import argparse
import re
import sys
from datetime import datetime, timezone
from pathlib import Path


CATEGORIES = ["Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"]


def version_from_tag(tag_name: str) -> str:
    return tag_name[1:] if tag_name.startswith("v") else tag_name


def empty_unreleased_section() -> str:
    return "## [Unreleased]\n\n" + "\n\n".join(
        f"### {category}" for category in CATEGORIES
    ) + "\n"


def prepare_changelog(changelog: str, tag_name: str, release_date: str) -> tuple[str, str]:
    version = version_from_tag(tag_name)
    version_heading = re.compile(
        rf"^## \[{re.escape(version)}\](?:[ \t]+-?[ \t]*.*)?[ \t]*$",
        re.MULTILINE,
    )

    if not version_heading.search(changelog):
        unreleased_heading = re.search(
            r"^## \[Unreleased\][ \t]*$",
            changelog,
            re.MULTILINE,
        )
        if not unreleased_heading:
            sys.exit("CHANGELOG.md does not contain a ## [Unreleased] section")

        replacement = f"{empty_unreleased_section()}\n## [{version}] - {release_date}"
        changelog = (
            changelog[:unreleased_heading.start()]
            + replacement
            + changelog[unreleased_heading.end():]
        )

    match = version_heading.search(changelog)
    if not match:
        sys.exit(f"CHANGELOG.md does not contain a section for {version}")

    next_section = re.search(r"^## \[", changelog[match.end():], re.MULTILINE)
    end = match.end() + next_section.start() if next_section else len(changelog)
    release_notes = changelog[match.start():end].strip() + "\n"
    return changelog, release_notes


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Promote CHANGELOG.md Unreleased notes for a tag release.",
    )
    parser.add_argument("--tag", required=True, help="Release tag, such as 2.12.0 or v2.12.0.")
    parser.add_argument(
        "--changelog",
        default="CHANGELOG.md",
        type=Path,
        help="Path to CHANGELOG.md.",
    )
    parser.add_argument(
        "--release-notes",
        default="release-notes.md",
        type=Path,
        help="Path where extracted release notes should be written.",
    )
    parser.add_argument(
        "--date",
        default=datetime.now(timezone.utc).date().isoformat(),
        help="Release date to write when promoting Unreleased.",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    changelog = args.changelog.read_text()
    prepared_changelog, release_notes = prepare_changelog(
        changelog=changelog,
        tag_name=args.tag,
        release_date=args.date,
    )
    args.changelog.write_text(prepared_changelog)
    args.release_notes.write_text(release_notes)


if __name__ == "__main__":
    main()
