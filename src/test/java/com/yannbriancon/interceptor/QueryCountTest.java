package com.yannbriancon.interceptor;

import com.yannbriancon.utils.entity.User;
import com.yannbriancon.utils.repository.MessageRepository;
import com.yannbriancon.utils.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class QueryCountTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private HibernateQueryInterceptor hibernateQueryInterceptor;

    @Test
    void queryCount_isOkWhenCallingRepository() {
        hibernateQueryInterceptor.startQueryCount();

        messageRepository.findAll();

        assertThat(hibernateQueryInterceptor.getQueryCount()).isEqualTo(1);
    }

    @Test
    void queryCount_isOkWhenSaveQueryIsExecutedBeforeStartingTheCount() {
        userRepository.saveAndFlush(new User());

        hibernateQueryInterceptor.startQueryCount();

        messageRepository.findAll();

        assertThat(hibernateQueryInterceptor.getQueryCount()).isEqualTo(1);
    }

    @ParameterizedTest
    @MethodSource("prepareQueryCountWithStartsWithFilterData")
    void queryCountWithStartsWithFilter(String filter, int expectedQueryCount) {
        final Predicate<String> predicate = f -> f.startsWith(filter);

        hibernateQueryInterceptor.startQueryCount(predicate);

        userRepository.saveAndFlush(new User());
        messageRepository.findAll();

        assertThat(hibernateQueryInterceptor.getQueryCount()).isEqualTo(expectedQueryCount);
    }

    static Stream<Arguments> prepareQueryCountWithStartsWithFilterData() {
        return Stream.of(arguments("select", 1),
                arguments("insert", 1),
                arguments("update", 0),
                arguments("delete", 0));
    }
}
