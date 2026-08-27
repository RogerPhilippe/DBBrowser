# DBBrowser 1.0.0-alphav1

First alpha release of DBBrowser, a desktop SQLite database browser and query tool.

## Highlights

### SQL Editor
- Syntax highlighting for SQL commands.
- Autocomplete suggestions for SQL keywords, table names, and column names.
- Auto-complete suggestion panel positioned dynamically based on the current line/cursor.
- Command separation by `;` and automatic skipping of commented-out lines when executing.
- Execute a command from the current line/selection, or run the full script.
- SQL commands run on a background thread to keep the UI responsive.
- Persistence of SQL editor content between application sessions.
- Ctrl+Z (undo) support in the SQL text area.

### Query Results
- Results grid displaying query output.
- JSON response handling with rendering directly into the results grid.
- Table panel is cleared before each new command execution.

### SQLite Realtime
- Initial implementation of SQLite Realtime: a socket-based client/server connection allowing SQL commands to be sent to and executed against the application remotely.
- Usage instructions included in the README.

### Data & Table Management
- Export tables functionality.
- Keyword list is cleared automatically after reopening a database file.

### Application UI
- Application icon shown in the title bar.
- Window minimize button.
- Resizable panel along the X axis.
- "About" screen.
- Initial application properties/settings support.

### Diagnostics
- Application version number is now logged on startup.
- Error details are now included in terminal/log output for easier troubleshooting.

### Fixes
- Fixed cursor positioning bug related to command execution.
- Fixed auto-complete panel behavior when code wraps to a new line.
