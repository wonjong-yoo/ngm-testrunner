# test-runner

![Build](https://github.com/wonjong-yoo/test-runner/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [ ] Get familiar with the [template documentation][template].
- [ ] Adjust the [pluginGroup](./gradle.properties), [plugin ID](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/kotlin).
- [ ] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [ ] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [ ] Set the `PLUGIN_ID` in the above README badges.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.

<!-- Plugin description -->
<h1>🌟 NGM TestRunner: Quickly Respond to Code Changes! 🌟</h1>

Testing is essential to ensure the stability of changed code. However, running all tests can often be inefficient. If you're wondering, "Which methods are affected by the code I modified?", give the NGM TestRunner a try!
(The NGM means Ninja-Guru-Magician)
<h2>✅ Key Features:</h2>
⚠️ This plugin only support Junit test framework

**1. Method-level Exploration**: When you select a specific method in the code, the tool automatically explores all methods affected by it and immediately runs the associated tests.

**2. Git Local Changes-based Exploration**: Based on recent Git local changes, it identifies methods influenced by changes and automatically runs tests, allowing you to quickly verify the stability of the modified code.

**3. Visual Test Results Display**: Test results are shown in an intuitive call graph format in the ToolWindow. Each node is highlighted in different colors based on the success or failure of the tests, providing an at-a-glance understanding of test status.

🚀 No need to worry about extensive code changes anymore! With NGM TestRunner, you can easily and quickly identify the scope of impact from code changes and their associated test statuses.

Install now and start a smarter testing experience!
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "test-runner"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/wonjong-yoo/test-runner/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation