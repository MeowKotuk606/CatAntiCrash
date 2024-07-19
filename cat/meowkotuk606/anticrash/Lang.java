package cat.meowkotuk606.anticrash;

public abstract class Lang {
	public String version;
	public String undefinedType;
	public String undefinedGM;
	public String undefinedGMRepl;
	public String addonConnected;
	public String undefinedChannel;
	public String passedCheckAll;
	public String passedCheckAllLog;
	public String passedCheckLog;
	public String failedCheckLog;
	public String passedCheck;
	public String failedCheck;
	public String reload;
	public String reloadConsole;
	public String removeOP;
	public String removePerms;
	public String removeGM;
	public String removeGroup;
	public String errorCheckGroup;
	public String kicked;
	public String removePermGroup;
	public String sendPacket;
	public String sendPacketLog;
	public String exploit;
	public String exploitLog;
	public String USP;
	public String timeout;
	public String timeoutLog;
	public String checksCount;

	public static class RuLang extends Lang {
		public RuLang() {
			version = "Версия %s";
			undefinedType = "Неизвестный тип проверки \"%s\", доступные типы проверки: \"button\", \"code\", \"password\"";
			undefinedGM = "Неизвестный режим игры в конфиге: %s";
			undefinedGMRepl = "Неизвестный режим игры для замены";
			addonConnected = "Подключено дополнение для %s";
			undefinedChannel = "Канал с ID \"%s\" не найден";
			passedCheckAll = "прошёл все проверки";
			passedCheckAllLog = "%s прошёл все проверки";
			passedCheckLog = "%s прошёл проверку %s";
			failedCheckLog = "%s не прошёл проверку %s";
			passedCheck = "прошёл проверку %s";
			failedCheck = "не прошёл проверку %s";
			reload = "Конфиг перезагружен";
			reloadConsole = "%s перезагрузил конфиг";
			removeOP = "Убраны OP права у %s";
			removePerms = "Убрано право %s у %s";
			removeGM = "Заменён режим игры у %s";
			removeGroup = "Убрана группа %s у %s";
			errorCheckGroup = "В целях безопасности %s помечен как с запрещённой группой т.к. произошла ошибка его проверки";
			kicked = "кикнут";
			removePermGroup = "Убрано право %s у группы %s";
			sendPacket = "отправлен вредоносный пакет";
			sendPacketLog = "%s отправил вредоносный пакет";
			exploit = "использован эксплоит";
			exploitLog = "%s использовал эксплоит";
			USP = "Найден UltimateServerProtector, проверки CatAntiCrash будут выполнятся после его проверок";
			timeout = "не успел пройти проверку %s за %s секунд";
			timeoutLog = "%s не успел пройти проверку %s за %s секунд";
			checksCount = "Количество проверок должно быть от 1 до 3, %s выходит за эти рамки";
		}
	}

	public static class EnLang extends Lang {
		public EnLang() {
			version = "Version %s";
			undefinedType = "Unknown check type \"%s\", available check types: \"button\", \"code\", \"password\"";
			undefinedGM = "Unknown game mode in config: %s";
			undefinedGMRepl = "Unknown game mode for replacement";
			addonConnected = "Addon connected for %s";
			undefinedChannel = "Channel with ID \"%s\" not found";
			passedCheckAll = "passed all checks";
			passedCheckAllLog = "%s passed all checks";
			passedCheckLog = "%s passed %s check";
			failedCheckLog = "%s failed %s check";
			passedCheck = "passed %s check";
			failedCheck = "failed %s check";
			reload = "Config reloaded";
			reloadConsole = "%s reloaded the config";
			removeOP = "OP rights removed from %s";
			removePerms = "Permission %s removed from %s";
			removeGM = "Game mode changed for %s";
			removeGroup = "Group %s removed from %s";
			errorCheckGroup = "For security reasons, %s is marked as having a forbidden group due to an error during their check";
			kicked = "kicked";
			removePermGroup = "Permission %s removed from group %s";
			sendPacket = "malicious packet sent";
			sendPacketLog = "%s sent a malicious packet";
			exploit = "exploit used";
			exploitLog = "%s used an exploit";
			USP = "UltimateServerProtector found, CatAntiCrash checks will be performed after its checks";
			timeout = "failed to pass %s check in %s seconds";
			timeoutLog = "%s failed to pass %s check in %s seconds";
			checksCount = "The number of checks should be between 1 and 3, %s is outside this range";
		}
	}
}
