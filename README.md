Basta
=====


## Environments

| Service        | URL                                            |
| -------------- | ---------------------------------------------- |
| Pre-production | https://basta.intern.dev.nav.no                |
| Production     | https://basta.intern.nav.no                    |


## Deployment

Deployed using [github actions](https://github.com/navikt/basta/actions). The action uses a self hosted runner on-premises. The setup for basta includes a separate settings.xml and environment variables for the runner service. Beyond that, the runner uses the default setup from Github.

## Development

The application can be started by running the `StandaloneBastaJettyRunner` class/main method. You can log in
with username "prodadmin" and password "prodadmin". The app will listen on http://localhost:1337.

When compiling and building in environments without access to Oracle Database Server the DatabaseScriptsTest test needs to be disabled.
This can be done by adding -Dtest=\!DatabaseScriptsTest.java to the maven command: `mvn -Dtest=\!DatabaseScriptsTest.java clean install`

## Dependency updates

Dependabot checks Maven and GitHub Actions weekly. Maven minor and patch updates
are batched together. Maven major updates are grouped by related dependency
families and require human approval. Dependencies outside the named families
receive individual major-update PRs. Spring Boot-managed versions remain managed
by the parent POM.

All GitHub Actions updates share one group, regardless of version type, and
always require manual review and merging.

The `Dependabot auto-merge` workflow approves and enables squash auto-merge only
for Maven PRs whose highest update type is minor or patch. Major or unclassified
updates are never auto-approved. This also applies to Maven security-update PRs;
the version-update groups do not batch security updates or delay their creation.
The workflow verifies Dependabot commit metadata and does not check out PR code.

If the merge command merges immediately as `github-actions[bot]`, the workflow
explicitly dispatches `deploy.yaml` on `main`: pushes made with `GITHUB_TOKEN`
do not trigger other workflows. Pending auto-merges use the normal push-triggered
deployment. The auto-merge job requests `actions: write` for this dispatch; no
additional secrets are required. Re-running after an immediate bot merge can
dispatch deployment again.

### Required GitHub settings (manual setup)

Repository settings have not been changed. Configure the following before
expecting automatic merges:

1. Under **Settings > Branches**, edit the existing branch protection rule for
   `main`. Enable **Require status checks to pass before merging** and require
   `Run integration tests`, `Analyze (actions)`, and `Analyze (java-kotlin)`.
   Enable **Require branches to be up to date before merging** so the checks
   cover the current base branch. Keep the one-approval requirement.
2. Under **Settings > Actions > General > Workflow permissions**, enable
   **Allow GitHub Actions to create and approve pull requests**. The default
   token permissions can remain read-only; the workflow requests its own
   write permissions. Organization policy must allow these permissions.
3. Under **Settings > General > Pull Requests**, enable **Allow squash merging**
   and **Allow auto-merge**, after configuring the required checks.

The workflow refuses to approve or enable auto-merge unless auto-merge is enabled
and all three checks are required by the classic branch protection rule for
`main`. A ruleset-only setup is not supported by this guard. GitHub then waits
for the required checks and approvals before merging; the workflow does not
bypass branch protection or merge failing PRs.

If **Require approval of the most recent reviewable push** prevents the
`github-actions[bot]` approval from satisfying protection, disable that option
while retaining **Dismiss stale pull request approvals when new commits are
pushed** and the one-approval requirement. Major updates and GitHub Actions
updates still need a human approval because the workflow never approves them.

Merge this configuration to `main` to activate it. After completing the settings,
re-run the auto-merge workflow for any existing eligible PR whose run failed the
settings guard. If additional CI jobs are introduced, add them to both the
required checks and the workflow's merge-gate guard.

## Security

Access levels are controlled through AD groups. These map to access to the environment classes `u`, `t`, `q` and `p`.
This is implemented in the class `no.nav.aura.basta.User`.
    
Currently this is the setup:

| Class | Role                 | Role-group-mapping in Fasit                                               
| ----- | -------------------- | ------------------------------------------------------------------------- |
| u     | ROLE_USER            | None (authenticated user)                                                 |
| t     | ROLE_OPERATIONS      | http://fasit.adeo.no/resources?10&resourceAlias=env-config.operations     |
| q     | ROLE_PROD_OPERATIONS | http://fasit.adeo.no/resources?12&resourceAlias=env-config.prodoperations |
| p     | ROLE_PROD_OPERATIONS | http://fasit.adeo.no/resources?12&resourceAlias=env-config.prodoperations |


## Sources

| /src/main/java      | Java stuff                      |
| /src/main/resources | Java-config stuff               |
