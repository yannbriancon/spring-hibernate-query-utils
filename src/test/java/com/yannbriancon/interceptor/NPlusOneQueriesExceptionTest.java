package com.yannbriancon.interceptor;

import com.yannbriancon.exception.NPlusOneQueriesException;
import com.yannbriancon.utils.entity.Message;
import com.yannbriancon.utils.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest("hibernate.query.interceptor.error-level=EXCEPTION")
@Transactional
class NPlusOneQueriesExceptionTest {

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void nPlusOneQueriesDetection_throwCallbackExceptionWhenNPlusOneQueries() {
        // Fetch the 2 messages without the authors
        List<Message> messages = messageRepository.findAll();

        try {
            // Trigger N+1 queries
            List<String> names = messages.stream()
                    .map(message -> message.getAuthor().getName())
                    .collect(Collectors.toList());
            assert false;
        } catch (NPlusOneQueriesException exception) {
            assertThat(exception.getMessage())
                    .isEqualTo("N+1 queries detected on a getter of the entity com.yannbriancon.utils.entity.User\n" +
                            "    at com.yannbriancon.interceptor.NPlusOneQueriesExceptionTest" +
                            ".lambda$nPlusOneQueriesDetection_throwCallbackExceptionWhenNPlusOneQueries$0" +
                            "(NPlusOneQueriesExceptionTest.java:34)\n" +
                            "    Hint: Missing Eager fetching configuration on the query " +
                            "that fetches the object of type com.yannbriancon.utils.entity.User\n");
        }
    }

    @Test
    void nPlusOneQueriesDetection_isNotThrowingExceptionWhenNoNPlusOneQueries() {
        // Fetch the 2 messages with the authors
        List<Message> messages = messageRepository.getAllBy();

        // Do not trigger N+1 queries
        List<String> names = messages.stream()
                .map(message -> message.getAuthor().getName())
                .collect(Collectors.toList());
    }
}
