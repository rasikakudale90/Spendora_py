# AGENTS.md — Project Rules

1. **Environment Driven**
   - All environment-specific values and configurations must be environment-driven.
   - Never hardcode URLs, ports, credentials, secrets, API keys, or deployment-specific settings.

2. **Environment & Secrets**
   - Never read, modify, expose, commit, or push `.env` files.
   - Use `.env.example` for documenting required environment variables.
   - Never commit secrets, credentials, API keys, or tokens.

3. **Follow the SRS**
   - Follow the folder structure, architecture, tech stack, naming conventions, and requirements defined in `SRS.md`.
   - Do not introduce unnecessary architectural changes.

4. **No Inline Styling**
   - Do not use inline CSS/styles.
   - Keep styling in the project's designated CSS/style files and follow the existing styling structure.

5. **Responsive UI**
   - Every UI change must be responsive and work properly on mobile, tablet, and desktop.
   - Do not introduce fixed layouts that break on smaller screens.

6. **No Hardcoding**
   - Do not hardcode configuration, business-critical values, API endpoints, or repeated constants.
   - Use environment variables, configuration files, constants, or existing project mechanisms where appropriate.

7. **Scope Control**
   - Implement only the requirements explicitly requested or defined for the current version.
   - Do not implement future-scope features unless explicitly instructed.

8. **Reuse Before Creating**
   - Before creating new components, utilities, services, or files, check whether an existing one can be reused or extended.
   - Avoid duplicate logic and unnecessary dependencies.

9. **Validate Every Task**
   - After making changes, verify that the affected functionality works and that existing functionality has not been broken.
   - Fix errors introduced by the changes before considering the task complete.

10. **Git Discipline**
   - After every completed task, commit all relevant changes with a clear commit message and push them to the current Git repository/branch.
   - Never commit `.env` files or secrets.