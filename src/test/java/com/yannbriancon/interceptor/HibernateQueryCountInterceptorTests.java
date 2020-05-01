package com.yannbriancon.interceptor;

import com.yannbriancon.util.Example;
import com.yannbriancon.util.ExampleRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class HibernateQueryCountInterceptorTests {

    @Autowired
    private ExampleRepository exampleRepository;

    @Autowired
    private HibernateQueryInterceptor hibernateQueryInterceptor;

    @Test
    void countIsOkWhenCallingARepository() {
        hibernateQueryInterceptor.startCounter();

        Example example = new Example("my new example");
        exampleRepository.save(example);

        assertThat(hibernateQueryInterceptor.getQueryCount()).isEqualTo(1);

        exampleRepository.findAll();

        assertThat(hibernateQueryInterceptor.getQueryCount()).isEqualTo(2);

        hibernateQueryInterceptor.removeCounter();

        assertThat(hibernateQueryInterceptor.getQueryCount()).isNull();
    }

}
