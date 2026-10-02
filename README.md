# OpportunityGuard AI
*Never miss your next opportunity.*
Chennai Institute of Technology · Department of Computer Science and Engineering · B.E. CSE

A multi-user Java web application: Servlets + JSP + JDBC + MySQL on Apache Tomcat 10.1, with Google OAuth 2.0 / Gmail API and the Gemini API. No Spring, Hibernate, Lombok, JavaFX, or JS frameworks.

```
Browser -> Tomcat -> Servlets (controller) -> Services -> JDBC repositories -> MySQL
                                   \-> Gmail API (OAuth 2.0)   \-> Gemini API
```

## 1. Prerequisites
| Tool | Version |
|---|---|
| JDK | 21 |
| Apache Tomcat | 10.1.x (NOT 9, NOT 11; this uses `jakarta.*`) |
| MySQL Server | 8.x |
| Maven | bundled with IntelliJ, or 3.9+ |
| IntelliJ IDEA | **Ultimate** has the built-in Tomcat run configuration. Community: use the free *Smart Tomcat* plugin (step 5B). |

## 2. MySQL setup
1. Start MySQL and open a client (MySQL Workbench or `mysql -u root -p`).
2. Run the script `database/schema.sql`. It creates database `opportunityguard` and tables `users`, `opportunities`, `notifications`, `email_records`, plus `gmail_tokens` (holds each user's own Google refresh token).
3. Note your MySQL user and password. They are given to the app through configuration, never written in source code.

## 3. Configuration (environment variables OR `-D` JVM options)
| Name | Required | Default |
|---|---|---|
| `DB_URL` | no | `jdbc:mysql://localhost:3306/opportunityguard?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USER` | no | `root` |
| `DB_PASSWORD` | **yes** if your MySQL user has a password | empty |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | for Gmail | none |
| `GOOGLE_REDIRECT_URI` | no | `http://localhost:8080/OpportunityGuardAI/gmail/callback` |
| `GEMINI_API_KEY` | for AI analysis | none |
| `GEMINI_MODEL` | no | `gemini-2.5-flash` |

The most reliable way in IntelliJ is **VM options** in the Tomcat run configuration, for example:
```
-DDB_PASSWORD=your_mysql_password -DGOOGLE_CLIENT_ID=xxx -DGOOGLE_CLIENT_SECRET=yyy -DGEMINI_API_KEY=zzz
```
(If your Tomcat configuration also has an "Environment variables" field, you can use that instead.)
Without Google/Gemini keys the app still runs fully (register, login, add and track opportunities, notifications). The Gmail page then says the integration is not configured; nothing is faked.

## 4. Open the project in IntelliJ IDEA
1. **File > Open** and select the `OpportunityGuardAI` folder (the one containing `pom.xml`). Choose "Open as Project", trust it.
2. **File > Project Structure > Project**: set SDK to JDK 21 (Download JDK if needed), language level 21.
3. Wait for Maven to import (Maven tool window > Reload All Maven Projects if dependencies are red).

## 5A. Tomcat in IntelliJ Ultimate
1. Download Tomcat 10.1.x (zip) from tomcat.apache.org and unzip it somewhere.
2. **Run > Edit Configurations > + > Tomcat Server > Local**.
3. **Application server > Configure...** and choose the unzipped Tomcat folder.
4. **Server tab**: JRE = 21, URL `http://localhost:8080/OpportunityGuardAI/`, and put the VM options from step 3 in **VM options**.
5. **Deployment tab > + > Artifact...** choose `OpportunityGuardAI:war exploded`. (If missing: Project Structure > Artifacts > + > Web Application: Exploded > From Modules, or click the "Fix" button.)
6. Set **Application context** to `/OpportunityGuardAI`.
7. Press the green **Run** button. When the log says "Server startup", Chrome opens `http://localhost:8080/OpportunityGuardAI/`.

## 5B. IntelliJ Community (Smart Tomcat plugin)
Install *Smart Tomcat* (Settings > Plugins), then Run > Edit Configurations > + > Smart Tomcat: Tomcat Server = your Tomcat 10.1 folder, Deployment Directory = `src/main/webapp`, Context Path = `/OpportunityGuardAI`, Port 8080, VM options as in step 3, "Use classpath of module" = OpportunityGuardAI.

## 5C. Stand-alone Tomcat (any editor)
`mvn clean package`, copy `target/OpportunityGuardAI.war` into Tomcat's `webapps/`, set `CATALINA_OPTS="-DDB_PASSWORD=..."`, run `bin/startup`.

## 6. Gmail OAuth setup (Google Cloud Console)
1. console.cloud.google.com > create a project.
2. **APIs & Services > Library** > enable **Gmail API**.
3. **OAuth consent screen**: External, fill app name and your email. Under **Test users** add every Google account that will connect Gmail (required while the app is in "Testing").
4. **Credentials > Create credentials > OAuth client ID** > *Web application*. Under **Authorized redirect URIs** add exactly:
   `http://localhost:8080/OpportunityGuardAI/gmail/callback`
5. Copy the Client ID and Client Secret into `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET`.
6. In the app: **Gmail Sync > Connect Gmail**, approve the read-only permission, then **Sync now**.
The scope is `gmail.readonly`. The app never sees the Google password; it stores a refresh token per user.

## 7. Gemini API setup
Create a key at aistudio.google.com/apikey and set `GEMINI_API_KEY`. If a model name is retired, set `GEMINI_MODEL` to a current one. The trust verdict and "model confidence" shown in the app are the model's own estimate for that email; they are not a measured accuracy.

## 8. Testing multi-user isolation
1. Open `http://localhost:8080/OpportunityGuardAI/` in a normal Chrome window and **Create Account**: Arun, arun@gmail.com. Log in.
2. **Opportunities > Add an opportunity manually**: name "Opportunity A", pick a deadline 2 days from today. Save. Note the id in the URL when you click Manage (e.g. `/opportunity?id=1`).
3. Open the dashboard and **Notifications**: you get "Registration deadline approaching" for Opportunity A (real, derived from your deadline).
4. Open a Chrome **Incognito** window (a separate session) and register Priya, priya@gmail.com. Log in. Add "Opportunity B" (it gets another id, e.g. 2).
5. Verify as Priya: the Opportunities list, Dashboard counts, Notifications and Gmail email records show only B-related data.
6. Still as Priya, manually type `.../opportunity?id=1` (Arun's id). Result: **404 page**. POSTing to `/opportunity/status` or `/opportunity/delete` with `id=1` also fails with 404 and changes nothing.
7. Back in the first window as Arun: `.../opportunity?id=2` gives 404 too, and Arun's counts are unaffected.
8. Log out and press Browser Back: you are sent to the login page (pages are `no-store`; the session is invalidated).
9. If both users connect Gmail (both must be Google test users), each user's **Gmail Sync** page lists only their own `email_records`.

How isolation is enforced: the user id is only ever read from the server-side HttpSession (`SessionUtil.requireUserId`). Every repository method for user data takes that id and uses `WHERE user_id = ?` (or `id = ? AND user_id = ?`). A URL/form never supplies a user id.

## 9. Security summary
PBKDF2-HMAC-SHA256 password hashing (210k iterations, per-password salt) · session authentication filter on every non-public URL · session id rotation on login · CSRF token required on every POST · PreparedStatement everywhere · output escaped with `<c:out>` · OAuth `state` check · HttpOnly session cookie · no-cache and anti-framing headers.

## 10. Project layout
`config` (Config, Db) · `model` · `repository` (JDBC) · `service` · `controller` (servlets) · `security` (AuthFilter, PasswordHasher, SessionUtil) · `gmail` (OAuth + Gmail REST clients) · `gemini` (Gemini REST client) · `util`.
JSP views live in `src/main/webapp/WEB-INF/views` (not directly reachable by URL); CSS in `webapp/css`.

## Troubleshooting
* **HTTP 500 / "Communications link failure"**: MySQL not running, or wrong `DB_URL`/`DB_USER`/`DB_PASSWORD`. Check the Tomcat log.
* **`Table ... doesn't exist`**: you did not run `database/schema.sql`.
* **404 for everything**: context path must be exactly `/OpportunityGuardAI`.
* **JSP compile errors about `jakarta.tags.core`**: Maven has not downloaded the JSTL dependency; reload Maven and redeploy.
* **Google `redirect_uri_mismatch`**: the redirect URI in Google Console must match `GOOGLE_REDIRECT_URI` exactly.
* **Google "access blocked"**: add your account under Test users on the consent screen.
* **"Google did not return a refresh token"**: remove the app at myaccount.google.com/permissions and connect again.
* **403 "Invalid or missing security token"**: your session expired; reload the page.
