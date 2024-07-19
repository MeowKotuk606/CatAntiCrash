# CatJDA
1. Download the latest version of [JDA](https://github.com/discord-jda/JDA/releases/) (file ...withDependencies.jar)
2. Extract it to any empty folder (using WinRAR) and delete the META-INF folder and the downloaded jar file
3. Create an empty project in Eclipse or another Java IDE, create a yaml file named plugin.yml in the main folder (i.e., without a package) with this content:
```yaml
main: cat.CatJDA
name: CatJDA
depend: [CatLib]
version: 1.0
author: MeowKotuk606
```
Then create a package named cat and in it a Java file named CatJDA with this content:
```java
package cat;

import org.bukkit.plugin.java.JavaPlugin;

public class CatJDA extends JavaPlugin {}
```
And build the package into a file named CatJDA.jar on the desktop

4. Open the built file using WinRAR and move everything that was created in the empty folder in step 2 into it (i.e., everything from the empty folder)
5. Congratulations! You now have CatJDA on your desktop that you can be sure is virus-free
