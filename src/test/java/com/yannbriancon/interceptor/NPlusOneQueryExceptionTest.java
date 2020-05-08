package com.yannbriancon.interceptor;

import com.yannbriancon.exception.NPlusOneQueryException;
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
class NPlusOneQueryExceptionTest {

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void nPlusOneQueryDetection_throwCallbackExceptionWhenNPlusOneQuery() {
        // Fetch the 2 messages without the authors
        List<Message> messages = messageRepository.findAll();

        try {
            // Trigger N+1 query
            List<String> names = messages.stream()
                    .map(message -> message.getAuthor().getName())
                    .collect(Collectors.toList());
            assert false;
        } catch (NPlusOneQueryException exception) {
            assertThat(exception.getMessage())
                    .isEqualTo("N+1 query detected on a getter of the entity com.yannbriancon.utils.entity.User\n" +
                            "    at com.yannbriancon.interceptor.NPlusOneQueryExceptionTest" +
                            ".lambda$nPlusOneQueryDetection_throwCallbackExceptionWhenNPlusOneQuery$0" +
                            "(NPlusOneQueryExceptionTest.java:34)\n" +
                            "    Hint: Missing Eager fetching configuration on the query " +
                            "that fetches the object of type com.yannbriancon.utils.entity.User\n");
        }
    }

    @Test
    void nPlusOneQueryDetection_isNotThrowingExceptionWhenNoNPlusOneQuery() {
        // Fetch the 2 messages with the authors
        List<Message> messages = messageRepository.getAllBy();

        // Do not trigger N+1 query
        List<String> names = messages.stream()
                .map(message -> message.getAuthor().getName())
                .collect(Collectors.toList());
    }
}
