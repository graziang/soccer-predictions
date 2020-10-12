package soccerpredictions.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import soccerpredictions.component.PredictionAPIComponent;
import soccerpredictions.service.PredictionService;

import javax.annotation.PostConstruct;

@Component
public class BotInit {

    private final Logger log = LoggerFactory.getLogger(BotInit.class);

    @Autowired
    PredictionService predictionService;


    @PostConstruct
    public void init(){

    }



}
