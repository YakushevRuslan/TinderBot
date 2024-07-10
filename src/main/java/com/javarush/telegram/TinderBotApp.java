package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import java.util.ArrayList;

public class TinderBotApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "javarush_al_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "7290765279:AAFkThq4f_8ILQqK-CUpF45O4wrsvrMRT4w"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "gpt:4dws6NYyD0BDK2ufp71ZJFkblB3TCC3tppbmX6OYmhSFydbM"; //TODO: добавь токен ChatGPT в кавычках
    private DialogMode curretMode = null;
    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private ArrayList<String> list = new ArrayList<>();

    public TinderBotApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        String message = getMessageText();

        // раздел START
        if(message.equals("/start"))
        {
            curretMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);

            showMainMenu("главное меню бота", "/start",
                    "генерация Tinder-профля \uD83D\uDE0E","/profile",
                    "сообщение для знакомства \uD83E\uDD70", "/opener",
                    "переписка от вашего имени \uD83D\uDE08", "/message",
                    "переписка со звездами \uD83D\uDD25", "/date",
                    "Общение с ChatGPT \uD83E\uDDE0", "/gpt");
            return;
        }

        // раздел GPT
        if (message.equals("/gpt"))
        {
            curretMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (curretMode == DialogMode.GPT)
        {
            String prompt = loadPrompt("gpt");

            Message msg = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
            String answer =  chatGPT.sendMessage(prompt, message);
            updateTextMessage(msg,answer);
            return;
        }

        // раздел DATE
        if(message.equals("/date"))
        {
            curretMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендея", "date_zendaya",
                    "Райан Гослинг", "date_gosling",
                    "Том Харди", "date_hardy");
            return;
        }

        if(curretMode == DialogMode.DATE)
        {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage(" Отличный выбор!\nПригласи человека на свидание за 5 сообщений");
                chatGPT.setPrompt(loadPrompt(query));
                return;
            }

            Message msg = sendTextMessage("Подождите пару секунд - человек набирает текст...");
            String answer = chatGPT.addMessage(message);
            updateTextMessage(msg,answer);
            return;
        }

        // раздел MESSAGE
        if(message.equals("/message"))
        {
            curretMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите в чат вашу переписку",
                    "Следующее сообщение","message_next",
                    "Пригласить на свидание","message_date");

            return;
        }

        if (curretMode == DialogMode.MESSAGE)
        {
            String query = getCallbackQueryButtonKey();
            if(query.startsWith("message_")) {
                String prompt = loadPrompt(query);
                String userCharHistory = String.join("\n\n", list);

                Message msg = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
                String answer = chatGPT.sendMessage(prompt, userCharHistory);
                updateTextMessage(msg,answer);
                return;
            }
            list.add(message);
            return;
        }


        sendTextMessage("*Привет !*");
        sendTextMessage("*Вы написали* :" + "_" + message + "_");
        sendTextButtonsMessage("Выберите режим работы : ",
                "Старт", "start",
                "Стоп", "stop");
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBotApp());
    }
}
