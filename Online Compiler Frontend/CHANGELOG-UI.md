# Focused AI chat UI fixes

Only the AI assistant composer and chat scrolling were adjusted in this update.

- Fixed the mode selector trigger sizing so the selected label stays inside its pill and no longer overlaps/outgrows the composer.
- Restyled the mode menu as separated, centered option cards with clear spacing and descriptions.
- Added automatic scrolling to the latest chat message after the user message, loading state, and HTML response update, for both desktop and mobile AI panels.
- Kept the chat header and composer fixed while the message list scrolls internally; scrollbar remains visually hidden.
- Kept backend HTML response rendering via the existing `dangerouslySetInnerHTML` implementation. Only render trusted/sanitized HTML returned by your backend.

No file explorer, editor, execution, authentication, or REST/WebSocket integration code was changed.
