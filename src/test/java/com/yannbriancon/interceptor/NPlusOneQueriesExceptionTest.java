package com.yannbriancon.interceptor;

import com.yannbriancon.exception.NPlusOneQueriesException;
import com.yannbriancon.utils.entity.Message;
import com.yannbriancon.utils.entity.User;
import com.yannbriancon.utils.repository.MessageRepository;
import com.yannbriancon.utils.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest("hibernate.query.interceptor.error-level=EXCEPTION")
@Transactional
class NPlusOneQueriesExceptionTest {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HibernateQueryInterceptor hibernateQueryInterceptor;

    @Test
    void nPlusOneQueriesDetection_throwsCallbackExceptionWhenNPlusOneQueries() {
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
                    .contains("N+1 queries detected on a getter of the entity com.yannbriancon.utils.entity.User\n" +
                            "    at com.yannbriancon.interceptor.NPlusOneQueriesExceptionTest." +
                            "lambda$nPlusOneQueriesDetection_throwsCallbackExceptionWhenNPlusOneQueries$0");
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

    @Test
    void nPlusOneQueriesDetection_throwsExceptionWhenSessionIsCleared() {
        User author = new User("author");
        userRepository.saveAndFlush(author);
        Message newMessage = new Message("text", author);
        messageRepository.saveAndFlush(newMessage);

        // Test a method that should return a N+1 query
        // The method does not return an exception because we just created the message so it is loaded in the Session
        getMessageAuthorNameWithNPlusOneQuery(newMessage.getId());

        // Clear the session to be able to correctly detect the N+1 queries in the tests
        hibernateQueryInterceptor.clearNPlusOneQuerySession(entityManager);

        try {
            // Test a method that should return a N+1 query
            // This time the Session is empty and the N+1 query is detected
            getMessageAuthorNameWithNPlusOneQuery(newMessage.getId());
            assert false;
        } catch (NPlusOneQueriesException exception) {
            assertThat(exception.getMessage())
                    .contains("N+1 queries detected on a getter of the entity com.yannbriancon.utils.entity.User\n" +
                            "    at com.yannbriancon.interceptor.NPlusOneQueriesExceptionTest" +
                            ".getMessageAuthorNameWithNPlusOneQuery");
        }
    }


    String getMessageAuthorNameWithNPlusOneQuery(Long messageId) {
        Message message = messageRepository.findById(messageId).get();

        // Should trigger N+1 query
        return message.getAuthor().getName();
    }
}
