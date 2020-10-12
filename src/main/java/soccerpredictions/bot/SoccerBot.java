package soccerpredictions.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import soccerpredictions.component.PredictionAPIComponent;
import soccerpredictions.domain.Prediction;
import soccerpredictions.service.PredictionService;

import java.util.Arrays;
import java.util.List;

@Component
public class SoccerBot extends TelegramLongPollingBot {

    private PredictionService predictionService;


    public SoccerBot(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    public String getBotUsername() {
            return "soccerpredictionspepsbot";
        }

        @Override
        public String getBotToken() {
            // inserire qui il proprio token
            return "1268597160:AAEu4HYM-4gwjUhiOyoBMK1QvxyuVbN3AVE";
        }
        public void onUpdateReceived(Update update) {

            String msg = update.getMessage().getText();
            String chatId=update.getMessage().getChatId().toString();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);

            String risposta=null;
            // conversione dei valori
            if(msg.startsWith("/best")) {
                risposta = predictionService.bestOf();
            }
            else if(msg.equals("/raddoppio")) {
                risposta = predictionService.bestRaddoppioAll();
            }
            else if(msg.startsWith("/raddoppio")) {
                msg = msg.substring(msg.indexOf("/raddoppio") + "/raddoppio".length());
                List<String> strings = Arrays.asList(msg.split(""));
                risposta = predictionService.bestRaddoppioSub(strings);
            }
            else if(msg.startsWith("/")) {
               risposta = predictionService.getMessage(msg.substring(1));
            }
            else {
                risposta = "Comando non valido: " + msg;
            }


            sendMessage.setText(risposta);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {

            }
        }

}
