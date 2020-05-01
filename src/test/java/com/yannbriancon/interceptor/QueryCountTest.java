package com.yannbriancon.interceptor;

import com.yannbriancon.utils.entity.DomainUser;
import com.yannbriancon.utils.repository.DomainUserRepository;
import com.yannbriancon.utils.repository.ExampleRepository;
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
class QueryCountTest {

    @Autowired
    private DomainUserRepository domainUserRepository;

    @Autowired
    private ExampleRepository exampleRepository;

    @Autowired
    private HibernateQueryInterceptor hibernateQueryInterceptor;

    @Test
    void queryCount_isOkWhenCallingRepository() {
        hibernateQueryInterceptor.startQueryCount();

        exampleRepository.findAll();

        assertThat(hibernateQueryInterceptor.getQueryCount()).isEqualTo(1);

    }

    @Test
    void queryCount_isOkWhenSaveQueryIsExecutedBeforeStartingTheCount() {
        domainUserRepository.saveAndFlush(new DomainUser());

        hibernateQueryInterceptor.startQueryCount();

        exampleRepository.findAll();

        assertThat(hibernateQueryInterceptor.getQueryCount()).isEqualTo(1);
    }
}
