package com.yannbriancon.interceptor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.yannbriancon.utils.entity.Message;
import com.yannbriancon.utils.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
@Transactional
class NPlusOneQueryLoggingTest {

    @Autowired
    private MessageRepository messageRepository;

    @Mock
    private Appender mockedAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

    @BeforeEach
    public void setup() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(mockedAppender);
    }

    @Test
    void nPlusOneQueryDetection_isLoggingWhenDetectingNPlusOneQuery() {
        // Fetch the 2 messages without the authors
        List<Message> messages = messageRepository.findAll();

        // Trigger N+1 query
        List<String> names = messages.stream()
                .map(message -> message.getAuthor().getName())
                .collect(Collectors.toList());

        verify(mockedAppender, times(2)).doAppend(loggingEventCaptor.capture());

        LoggingEvent loggingEvent = loggingEventCaptor.getAllValues().get(0);
        assertThat("N+1 query detected for entity: com.yannbriancon.utils.entity.User")
                .isEqualTo(loggingEvent.getMessage());
        assertThat(Level.ERROR).isEqualTo(loggingEvent.getLevel());
    }

    @Test
    void nPlusOneQueryDetection_isNotLoggingWhenNotDetectingNPlusOneQuery() {
        // Fetch the 2 messages with the authors
        List<Message> messages = messageRepository.getAllBy();

        // Do not trigger N+1 query
        List<String> names = messages.stream()
                .map(message -> message.getAuthor().getName())
                .collect(Collectors.toList());

        verify(mockedAppender, times(0)).doAppend(any());
    }
}
