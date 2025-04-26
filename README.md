# Dark Restart Plugin

Мощный плагин для управления рестартом сервера Minecraft с расширенными возможностями.

## Возможности

- Управление рестартом через команды и меню
- Автоматический рестарт по расписанию
- Технический рестарт с невозможностью отмены
- Уведомления в чате и ActionBar
- Звуковые оповещения
- Гибкая система прав доступа

## Команды

### Основные команды
- `/drestart menu` - Открыть меню рестарта
- `/drestart start <время> [причина]` - Запланировать рестарт
- `/drestart cancel` - Отменить рестарт
- `/drestart status` - Показать статус рестарта

### Команды авторестарта
- `/autorestart set <часы>` - Установить интервал авторестарта
- `/autorestart cancel` - Отменить авторестарт
- `/autorestart status` - Показать статус авторестарта

## Права доступа

### Группы прав
- `darkrestart.admin` - Полный доступ к плагину
- `darkrestart.moderator` - Доступ к основным функциям
- `darkrestart.helper` - Базовый доступ

### Отдельные права
- `darkrestart.use` - Использование основных команд
- `darkrestart.autorestart` - Управление авторестартом
- `darkrestart.technical` - Доступ к техническому рестарту
- `darkrestart.notify` - Получение уведомлений

## Установка

1. Скачайте последнюю версию из [Releases](https://github.com/AuitDarkness/DarkRestart/releases)
2. Поместите файл .jar в папку `plugins` вашего сервера
3. Перезапустите сервер
4. Настройте плагин через `config.yml`

## Конфигурация

Основные настройки находятся в файле `config.yml`:

```yaml
restart:
  time: 0
  reason: "Запланированный рестарт"
  is-technical: false
  countdown-sound: "block.note_block.pling"
  auto-restart-interval: 0

notifications:
  interval: 300
  actionbar: true
  sounds: true
  sound-volume: 1.0
  sound-pitch: 1.0
```

## Поддерживаемые версии

- Minecraft: 1.19+
- Spigot/Paper: 1.19+

## Лицензия

Этот проект распространяется под лицензией MIT. Подробности в файле [LICENSE](LICENSE).

## Поддержка

Если у вас возникли проблемы или вопросы:
1. Создайте Issue в этом репозитории
2. Напишите мне в Discord: ваш_дискорд
3. Посетите наш [Discord сервер](https://discord.gg/ваш_сервер)

## Вклад в проект

Мы приветствуем вклад в проект! Если вы хотите внести свой вклад:
1. Форкните репозиторий
2. Создайте ветку для ваших изменений
3. Сделайте Pull Request

## Благодарности

- [SpigotMC](https://www.spigotmc.org/) за API
- [PaperMC](https://papermc.io/) за поддержку
- Всем, кто помогал в разработке плагина 