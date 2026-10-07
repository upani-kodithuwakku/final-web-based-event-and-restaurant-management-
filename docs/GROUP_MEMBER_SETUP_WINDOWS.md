# Get the full team project and run it on Windows

You have already cloned the project and finished your own branch.
You do not need to clone it again.

Use GitHub Desktop for the Git steps below. No Git commands are needed.
Docker is not needed for this guide.

## 1. Open the correct project in GitHub Desktop

1. Open **GitHub Desktop**.
2. Click **Current repository** at the top left.
3. Select **final-web-based-event-and-restaurant-management-**.
4. Click **View on GitHub** to check that it opens our shared repository:
   **upani-kodithuwakku / final-web-based-event-and-restaurant-management-**.
5. Return to GitHub Desktop.

If this project is not listed, click **File → Add local repository** and choose
its existing folder on your computer. Do not download another copy.

## 2. Keep your own work on your own branch

If your work is already committed, continue to Step 3.

If the **Changes** tab still lists your work:

1. Check the files you want to save. Do not include `.env` or password files.
2. Write a short message in **Summary**, for example **Finish my feature**.
3. Click **Commit to [your branch name]**.
4. Click **Push origin** if the button appears.

If you are not ready to commit and GitHub Desktop asks about your changes when
switching branches, choose **Leave my changes on [your branch name]**.
This keeps them on your own branch.

## 3. Get everyone's merged code

1. Click **Current branch** at the top.
2. Select **main**.
3. Click **Fetch origin**.
4. Wait for it to finish.
5. If **Pull origin** appears, click it and wait.
6. Check that **Current branch** still says **main**.

If the button stays as **Fetch origin**, and there is no message saying you are
behind, your main branch is already up to date.

You now have all code that has been merged into the shared main branch.
Your own feature branch is still there. It has not been deleted.

If GitHub Desktop shows a conflict or an error, send that message to Upani.
Do not delete files to make the error disappear.

## 4. Open that same folder in VS Code

1. In GitHub Desktop, click **Open in Visual Studio Code**.
2. If that button is not available, click **Repository → Show in Explorer**.
3. Open VS Code, click **File → Open Folder**, and choose that folder.

If you use another Java editor, open this same folder there.
Do not open your old copy of the project.

You should see these folders:

- `database`
- `restaurant-event-backend`
- `restaurant-event-frontend`

In VS Code, the branch shown at the bottom left should say **main**.

## 5. Check Java before starting

1. In VS Code, click **Terminal → New Terminal**.
2. Type this and press Enter:

```text
java -version
```

3. Then type:

```text
mvn -version
```

Use **Java 21** for this project. Both commands should show Java 21.
If a command is not found, or Maven uses a different Java version, ask Upani
before continuing.

You also need Node.js and npm for the frontend. If they are not installed,
install Node.js 22 or newer with npm, then reopen VS Code.

## 6. Choose your database instructions

### A. You already have MySQL Workbench

Workbench is the app used to view a **MySQL** database.
You also need **MySQL Server** running on your computer.

1. Open **MySQL Workbench**.
2. Open your existing local MySQL connection.
3. Enter your MySQL password when asked.
4. If the SQL editor opens, your connection works.

If you need to create the connection:

1. Click **+** next to **MySQL Connections**.
2. Set **Connection Name** to **Team Project**.
3. Set **Hostname** to **127.0.0.1**.
4. Set **Port** to **3306**, or the port you used when installing MySQL.
5. Set **Username** to **root**.
6. Click **Test Connection** and enter your local MySQL root password.
7. Click **OK**, then open the connection.

Use the password you set when installing MySQL on **your computer**.
You do not need Upani's password.

### B. You have Microsoft SQL Server / SSMS

**Microsoft SQL Server is different from MySQL.**

The current project uses MySQL. Its SQL file cannot be run unchanged in SSMS,
and the current backend cannot connect to SQL Server as it is.

For this version of the project:

1. Keep SQL Server and SSMS installed if you need them for other work.
2. Install **MySQL Server 8.0** and **MySQL Workbench** on Windows.
3. You do not need Docker.
4. Follow section A above.

Download: [MySQL Installer for Windows](https://dev.mysql.com/downloads/installer/).
In the installer, choose **Custom** and select MySQL Server and MySQL Workbench.
Set a root password and remember it.

If the lecturer says Microsoft SQL Server is compulsory, tell Upani first.
The team must change and test the project for SQL Server before using SSMS.
There is no working SSMS setup for the current code yet.

## 7. Put the project database into your MySQL

Look inside the project's `database` folder for **00_full_schema.sql**.

If the file is there:

1. In Workbench, click **File → Open SQL Script**.
2. Choose **00_full_schema.sql** from this project.
3. Leave the text unselected.
4. Click the lightning button called **Execute All or Selection**.
5. Check the **Action Output** area at the bottom for errors.
6. Refresh **SCHEMAS** on the left.
7. Open **restaurant_event_db → Tables**.

You should see the project's tables. This file creates the tables and roles.
It does not put menu dishes or dining-space examples into them.

**If 00_full_schema.sql is missing:** ask Upani whether the latest SQL commit
has been merged into main. You can still continue with the current main:

1. In Workbench, right-click the **SCHEMAS** area.
2. Click **Create Schema**.
3. Name it **restaurant_event_db**.
4. Click **Apply**, then **Apply** again, then **Finish**.
5. Continue below. The backend will create its application tables when it starts.

If you already have a database with this name and your own records, keep it.
Do not delete it or run an old setup file over it without asking Upani.

## 8. Make your .env file in VS Code

1. Expand **restaurant-event-backend** in VS Code.
2. Find **.env.example**.
3. If `.env` does not exist, copy `.env.example` and paste it into the same folder.
4. Rename the copy to exactly **.env**.
5. Do not call it `.env.txt` or add a space after the name.
6. Open `.env`.

Use these values for your local MySQL setup:

```dotenv
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=restaurant_event_db
DB_USERNAME=restaurant_app
DB_PASSWORD="PUT_YOUR_MYSQL_PASSWORD_HERE"
JWT_SECRET=PUT_YOUR_GENERATED_SECRET_HERE
SEED_DEMO_USERS=true
SEED_EVENT_CATALOG=true
```

Replace **PUT_YOUR_MYSQL_PASSWORD_HERE** with the same password that worked in
Workbench. Keep the double quotes around it.
If your MySQL port is different, change `DB_PORT` to match it.

To make the JWT secret, copy this one line into the VS Code terminal:

```text
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
```

Copy the long text it prints. Paste it after `JWT_SECRET=` instead of
**PUT_YOUR_GENERATED_SECRET_HERE**.

Press **Ctrl+S** to save `.env`.
Do not send this file to the group or commit it to GitHub.

If `.env.example` also has `MYSQL_ROOT_PASSWORD`, leave that line alone.
It is for Docker; this guide does not use Docker.

## 9. Start the backend

1. In the VS Code file list, right-click **restaurant-event-backend**.
2. Click **Open in Integrated Terminal**.
3. Type:

```text
mvn spring-boot:run
```

4. Wait until it says **Started RestaurantEventApplication**.
5. Keep this terminal open and running.

Check it by opening:
[http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html).

If it opens, the backend is running.
The backend also adds the event catalogue and the local demo accounts.

## 10. Add the example menu and tables

For a new database, do this once while the backend is running.

1. Go back to **MySQL Workbench**.
2. Under **SCHEMAS**, double-click **restaurant_event_db**. Its name becomes bold.
3. Click **File → Open SQL Script**.
4. Open each file below from the project's `database` folder.
5. Click **Execute All or Selection** for the whole file.
6. Check for errors before going to the next file.

Use this order:

| Order | File to open |
| --- | --- |
| 1 | 02_food_requests.sql |
| 2 | 03_menu_items.sql |
| 3 | 04_customer_payments.sql |
| 4 | 06_customer_management.sql |
| 5 | 08_reservation_payments.sql |
| 6 | 11_dining_spaces.sql |
| 7 | 14_supplier_details.sql |
| 8 | 10_group_dining_table.sql |
| 9 | 12_private_dining_photos.sql |
| 10 | 13_private_dining_names.sql |

These add database links, example menu items and dining spaces.
Do not run the other SQL files for this setup.

To look at your data, open **restaurant_event_db → Tables**.
Right-click **menu_items**, then click **Select Rows - Limit 1000**.
Do the same for **restaurant_tables**.

## 11. Start the frontend

1. Leave the backend terminal running.
2. In the VS Code file list, right-click **restaurant-event-frontend**.
3. Click **Open in Integrated Terminal**. This opens another terminal.
4. Type:

```text
npm.cmd ci
```

5. Wait for it to finish. Then type:

```text
npm.cmd run dev
```

6. Keep this terminal running too.

## 12. Open the full project

Open [http://localhost:5174](http://localhost:5174) in your browser.

For the local demo, use:

| Account | Email | Password |
| --- | --- | --- |
| Admin | admin@gather.com | Admin@1234 |
| Customer | customer@gather.com | Customer@1234 |

These accounts appear because you set `SEED_DEMO_USERS=true` before starting
the backend. Use them for local testing.

Check that you can:

1. See the menu dishes.
2. Sign in as a customer and see dining spaces.
3. Make a reservation with a future date.
4. Sign in as admin and see the reservation.

Keep MySQL Server and both application terminals running while using the site.

## If something goes wrong

| Problem | What to do |
| --- | --- |
| Database password error | Use the same user and password that work in Workbench. Check `.env` is saved. |
| Cannot connect to database | Check MySQL Server is running and its port matches `.env`. |
| Port 8081 is already in use | Find your earlier backend terminal and press Ctrl+C. Start the backend once. |
| Port 5174 is already in use | Find your earlier frontend terminal and press Ctrl+C. Start the frontend once. |
| Empty menu or dining spaces | Complete Step 10. Sign in before viewing dining spaces. |
| Red Java errors about Flexible Constructor Bodies | Ask Upani to check the Java extension. The prerelease version we used had a bug. |
| GitHub Desktop shows a conflict | Send the message to Upani. Do not delete your work. |

For a backend error, send the first useful error message above **BUILD FAILURE**.
Do not send your passwords or `.env`.

## When you go back to your own branch

To test the full project, stay on **main**.

If you later select your own branch in GitHub Desktop, it may still have the old
code. To update it using buttons:

1. Stop the backend and frontend with **Ctrl+C** in their terminals.
2. In GitHub Desktop, select your own branch under **Current branch**.
3. Click **Branch → Update from main**.
4. If GitHub Desktop shows conflicts, ask Upani to help.
5. After the update succeeds, restart the project.

If you saved unfinished changes with **Leave my changes**, restore them on your
original branch when you are ready. Ask Upani if you are unsure.

## Remember

Pulling main gives you everyone's merged code.
Your local bookings and orders are saved in your own database.
Upani's shared website link uses Upani's computer and database.

---



Help: [GitHub Desktop fetch and pull](https://docs.github.com/en/get-started/learning-to-code/getting-started-with-git),
[keeping changes when switching branches](https://docs.github.com/en/desktop/making-changes-in-a-branch/stashing-changes-in-github-desktop),
[MySQL Windows Installer](https://dev.mysql.com/doc/mysql-installer/en/mysql-installer.html).
