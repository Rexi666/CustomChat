<p align="center">
  <img src="https://img.shields.io/badge/platform-Paper-blue" height="30" width="150"/>
  <img src="https://img.shields.io/badge/supports-PlaceholderAPI-yellow" height="30" width="250"/>
  <img src="https://img.shields.io/badge/license-MIT-green" height="30" width="150"/>
</p>

# CustomChat
> 📢 A modern Minecraft chat plugin for Paper servers with full PlaceholderAPI support and rich message customization.

CustomChat is a highly customizable chat formatting plugin for Paper Minecraft servers. It allows you to create different chat formats based on permissions, with support for colors, hover messages, click actions, and [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) integration.

Designed for performance and flexibility, CustomChat supports both legacy and modern chat events depending on your server version.

---

## ✅ Features

- 🧩 Format chat messages using **MiniMessage** or **legacy color codes (&)**
- 🔐 Multiple chat formats with **permission-based priority**
- 🖱️ Support for **hover tooltips** and **click actions** (suggest command, open URL, etc.)
- 🎨 Configurable message colors and formatting
- 🛡️ Optionally blocks the `&k` obfuscated text code
- 📦 Integration with **PlaceholderAPI** for dynamic player info
- ⚙️ Simple `/customchat reload` command to reload config without restart

---

## 📦 Commands

| Command                     | Description                                     | Permission                 |
|-----------------------------|-------------------------------------------------|----------------------------|
| `/customchat reload`        | Reloads the config file                         | `customchat.admin`         |
| `/chatcolor`                | Opens the chatcolor menu                        | `customchat.chatcolor`     |
| `/customchat mentiontoggle` | In case mentioning is enable, toggle per player | `customchat.mentiontoggle` |

---

## 🔐 Permissions
| Permission                                 | Description                                       |
|--------------------------------------------|---------------------------------------------------|
| `customchat.admin`                         | Allows access to /customchat reload               |
| `customchat.format.<format>`               | Allows usage of the specified chat format         |
| `customchat.changecolor`                   | Allows changing color of own messages             |
| `customchat.changeformat`                  | Allows changing format of own messages            |
| `customchat.chatcolor`                     | Allows access to /chatcolor                       |
| `customchat.colorchat.color.<color>`       | Allows access to a specific color in colorchat    |
| `customchat.colorchat.gradient.<gradient>` | Allows access to a specific gradient in colorchat |
| `customchat.hover.item`                    | Allow access to [item]                            |
| `customchat.hover.inv`                     | Allow access to [inv]                             |
| `customchat.hover.ender`                   | Allow access to [ender]                           |
| `customchat.hover.location`                | Allow access to [location]                        |
| `customchat.hover.ping`                    | Allow access to [ping]                            |
| `customchat.mentiontoggle`                 | Allow access to /customchat mentiontoggle         |

---

## 🧠 How it Works

1. The plugin loads chat formats defined in `config.yml`.
2. When a player sends a message, the plugin:
    - Detects their applicable format (based on permission priority).
    - Applies placeholders, colors, and hover/click actions.
    - Sends the formatted message to all players and the console.
3. If the player has the proper permission, they can use & color codes in their messages
4. If `&k` is blocked and the player uses it, they receive a warning.

---

## 📥 Installation

1. Download the plugin JAR.
2. Place it in your `plugins/` folder.
3. Restart the server.
4. Configure `config.yml` and run `/customchat reload`.

---

### 💬 Need Help or Support?
📖 Wiki: https://rexi666-plugins.gitbook.io/rexi666/customchat

Join my Discord server (Spanish/English):
<p align="center">
  <a href="https://discord.com/invite/a3zkKtrjTr">
    <img src="https://discordapp.com/api/guilds/1025688556779360266/widget.png?style=banner3" alt="Discord Invite"/>
  </a>
</p>

---

## 🙋‍♂️ Author

Made with ❤️ by **Rexi666**

If you enjoy this plugin, consider [donating](https://paypal.me/rexigamer666)!