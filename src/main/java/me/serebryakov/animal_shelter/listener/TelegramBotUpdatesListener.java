package me.serebryakov.animal_shelter.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import jakarta.annotation.PostConstruct;
import me.serebryakov.animal_shelter.entity.Volunteer;
import me.serebryakov.animal_shelter.keyboard.TelegramKeyboard;
import me.serebryakov.animal_shelter.service.VolunteerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final TelegramBot telegramBot;
    private final ExecutorService executorService;
    private final TelegramKeyboard telegramKeyboard;
    private final VolunteerService volunteerService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, TelegramKeyboard telegramKeyboard, VolunteerService volunteerService) {
        this.telegramBot = telegramBot;
        this.volunteerService = volunteerService;
        this.executorService = Executors.newFixedThreadPool(10);
        this.telegramKeyboard = telegramKeyboard;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> list) {
        executorService.submit(() -> {
            try {
                list.forEach(update -> {
                    logger.info("handles update: {}", update);
                    Message message = update.message();

                    SendMessage sendMessage = null;
                    SendPhoto sendPhoto = null;
                    Volunteer volunteer = volunteerService.getByChatId(message.chat().id());
                    if (volunteer != null) {
                        long chatId = message.chat().id();
                        String text = message.text();

                        if ("/start".equals(text) || "Главное меню".equals(text)) {
                            sendMessage = getVolunteerMenu(chatId);
                        } else if ("Получить отчёт на проверку".equals(text)) {
                            sendPhoto = telegramKeyboard.getUncheckedReports(chatId, telegramBot, volunteerService);
                            if (sendPhoto == null) {
                                sendMessage = new SendMessage(chatId, "Сегодня ещё не было отчётов");
                            }
                        } else if ("Отклоненные отчёты по дате".equals(text)) {

                        } else if ("Одобренные отчёты по дате".equals(text)) {

                        } else if ("Непровернные отчёты по дате".equals(text)) {

                        } else if ("Одобрить".equals(text) || "Отклонить".equals(text)) {
                            sendMessage = telegramKeyboard.setReportStatus(chatId, text, volunteer.getReportChatId());
                        } else {
                            sendMessage = new SendMessage(chatId, "Неизвестная команда. Для начала работы введите /start");
                        }
                    } else {
                        sendMessage = telegramKeyboard.getResponse(message);
                    }

                    //смотрим пришло сообщение или фото и отправляем его
                    SendResponse sendResponse;
                    if (sendMessage != null) {
                        sendResponse = telegramBot.execute(sendMessage);
                    } else if (sendPhoto != null){
                        sendResponse = telegramBot.execute(sendPhoto);
                    } else {
                        sendResponse = telegramBot.execute(new SendMessage(message.chat().id(), "ERROR"));
                    }


                    if (!sendResponse.isOk()) {
                        logger.error("Error sending message: {}", sendResponse.description());
                    }
                });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
    private SendMessage getVolunteerMenu(long chatId) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup("Получить отчёт на проверку", "Отклоненные отчёты по дате",
                "Одобренные отчёты по дате", "Непровернные отчёты по дате");
        return new SendMessage(chatId, "Выберите нужный пункт меню.").replyMarkup(replyKeyboardMarkup);
    }

}
