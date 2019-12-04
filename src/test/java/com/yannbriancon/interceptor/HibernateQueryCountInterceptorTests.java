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
    private HibernateQueryCountInterceptor hibernateQueryCountInterceptor;

    @Test
    void countIsOkWhenCallingARepository() {
        hibernateQueryCountInterceptor.startCounter();

        Example example = new Example("my new example");
        exampleRepository.save(example);

        assertThat(hibernateQueryCountInterceptor.getQueryCount()).isEqualTo(1);

        exampleRepository.findAll();

        assertThat(hibernateQueryCountInterceptor.getQueryCount()).isEqualTo(2);

        hibernateQueryCountInterceptor.removeCounter();

        assertThat(hibernateQueryCountInterceptor.getQueryCount()).isNull();
    }

}
