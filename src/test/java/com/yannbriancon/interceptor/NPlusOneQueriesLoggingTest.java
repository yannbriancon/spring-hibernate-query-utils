package com.yannbriancon.interceptor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.yannbriancon.utils.entity.Message;
import com.yannbriancon.utils.entity.Post;
import com.yannbriancon.utils.repository.MessageRepository;
import com.yannbriancon.utils.repository.PostRepository;
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
class NPlusOneQueriesLoggingTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PostRepository postRepository;

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
    void hibernateQueryInterceptor_isDetectingNPlusOneQueriesWhenMissingEagerFetchingOnQuery() {
        // Fetch the 2 messages without the authors
        List<Message> messages = messageRepository.findAll();

        // The getters trigger N+1 queries
        List<String> names = messages.stream()
                .map(message -> message.getAuthor().getName())
                .collect(Collectors.toList());

        verify(mockedAppender, times(2)).doAppend(loggingEventCaptor.capture());

        LoggingEvent loggingEvent = loggingEventCaptor.getAllValues().get(0);
        assertThat(loggingEvent.getMessage())
                .contains("N+1 queries detected on a getter of the entity com.yannbriancon.utils.entity.User\n" +
                        "    at com.yannbriancon.interceptor.NPlusOneQueriesLoggingTest." +
                        "lambda$hibernateQueryInterceptor_isDetectingNPlusOneQueriesWhenMissingEagerFetchingOnQuery$0");
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.ERROR);
    }

    @Test
    void hibernateQueryInterceptor_isDetectingNPlusOneQueriesWhenMissingLazyFetchingOnEntityField() {
        // The query triggers N+1 queries to eager fetch each post message
        List<Post> posts = postRepository.findAll();

        verify(mockedAppender, times(2)).doAppend(loggingEventCaptor.capture());

        LoggingEvent loggingEvent = loggingEventCaptor.getAllValues().get(0);
        assertThat(loggingEvent.getMessage())
                .contains("N+1 queries detected with eager fetching on the entity com.yannbriancon.utils.entity.Message\n" +
                        "    at com.yannbriancon.interceptor.NPlusOneQueriesLoggingTest." +
                        "hibernateQueryInterceptor_isDetectingNPlusOneQueriesWhenMissingLazyFetchingOnEntityField");
        assertThat(loggingEvent.getLevel()).isEqualTo(Level.ERROR);
    }


    @Test
    void nPlusOneQueriesDetection_isNotLoggingWhenNoNPlusOneQueries() {
        // Fetch the messages and does not trigger N+1 queries

        messageRepository.findById(2L);

        verify(mockedAppender, times(0)).doAppend(any());
    }
}
