1. Скачать [JDA](https://github.com/discord-jda/JDA/releases/tag/v5.0.0) (файл ...withDependencies.jar)
2. Распаковать в любую пустую папку (с помощью WinRAR) и удалить META-INF и скачанный jar файл
3. Создать в Eclipse или другой Java IDE пустой проект, создать yaml файл plugin.yml в основной папке (т.е. без пакета (package)) с этим содержанием:
```yaml
main: cat.CatJDA
name: CatJDA
depend: [CatLib]
version: 0
author: MeowKotuk606
```
Потом создать пакет cat и в нём java файл CatJDA с этим содержанием:
```java
package cat;

import org.bukkit.plugin.java.JavaPlugin;

public class CatJDA extends JavaPlugin {}
```
И собрать пакет в файл CatJDA.jar на рабочий стол
4. Открыть собранный файл с помощью WinRAR и переместить в него всё что было созданно в пустой папке во 2 пункте (т.е. всё из пустой папки)
5. Поздравляю! У тебя на рабочем столе CatJDA в котором ты можешь быть уверен что он без вирусов
