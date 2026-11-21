# Requested workspace fixes

- Selecting a folder now marks it active; Explorer New Folder/New File actions create inside that folder, defaulting to the workspace root when no folder is selected.
- Added a VS Code-style horizontally scrollable open-file tab strip with a fixed plus button on the left and close controls on each tab.
- HTML files now open in an isolated browser preview when Run is clicked, with Expand/Restore and Back to Code controls.
- Added session restoration through `GET /api/v1/users/me` using the existing HttpOnly cookie. No JWT is stored in localStorage. LocalStorage stores only UI preferences, open file IDs, and unsaved editor drafts.
- Added a Settings entry with Midnight, Daylight, Ocean, and Amethyst themes.
- REST endpoints use the single `VITE_API_BASE_URL` setting in `src/api.js`; the execution WebSocket URL is derived from the same setting when it is configured.
- Added `GET /api/v1/users/me` and `POST /api/v1/users/logout` frontend API methods.

The existing execution and code-content unit tests pass. A Vite production build could not be run in this environment because dependency installation did not complete.

## Interactive stdin input panel
- The terminal now reveals a dedicated stdin panel as soon as the running program emits output or the backend reports a running/waiting-for-input status.
- The input panel remains available after sending a line, so programs using multiple `Scanner`, `input()`, `scanf()`, or `cin` reads can receive subsequent lines.
- Sent input is echoed into the terminal output for visibility.
- Existing execution, language loading, file management, authentication, and AI UI code paths were otherwise left unchanged.


## WebSocket START executionId fix
- Generate executionId on the frontend before START.
- Reuse the same executionId for INPUT and CANCEL.
- Do not send display version as `version`; backend expects optional integer `versionIndex`, and uses application.properties when omitted.


## Scratch files, conditional stdin, and Analyze composer behavior
- Clicking a language logo now opens an in-memory scratch file only; it does not call the file-create API or add an entry to the workspace explorer.
- Clicking Save creates the scratch file in the selected workspace folder once, then subsequent saves update that same file ID.
- Existing workspace files continue to use update/save behavior.
- The stdin panel now appears only after explicit INPUT_REQUEST / STDIN_REQUEST / WAITING_FOR_INPUT messages or matching waiting statuses; ordinary output and RUNNING status no longer show it. After sending a line, the input panel hides until another input request arrives.
- Analyze now pre-fills the Ask Anything composer instead of sending automatically. When the user presses Send, the current editor code is attached to the analysis request.

## Latest editor-tab and Analyze workflow update
- Language selection now creates/reuses one temporary in-memory tab per language (e.g. Java -> `Main.java`, Python -> `app.py`).
- Switching back to a previously selected language restores its existing temporary tab and unsaved editor contents.
- Temporary tabs do not have workspace file IDs and are not added to the file explorer. Explicit Save creates the workspace file once; subsequent saves update that saved file.
- Analyze pre-fills the AI composer with the complete current editor source. The composer is multiline so the full code can be reviewed/edited before Send.
