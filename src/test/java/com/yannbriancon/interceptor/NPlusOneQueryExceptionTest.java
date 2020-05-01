package com.yannbriancon.interceptor;

import com.yannbriancon.exception.NPlusOneQueryException;
import com.yannbriancon.utils.entity.Example;
import com.yannbriancon.utils.repository.DomainUserRepository;
import com.yannbriancon.utils.repository.ExampleRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class NPlusOneQueryExceptionTest {

    @Autowired
    private DomainUserRepository domainUserRepository;

    @Autowired
    private ExampleRepository exampleRepository;

    @Autowired
    private HibernateQueryInterceptor hibernateQueryInterceptor;

    @Test
    void nPlusOneQueryDetection_throwCallbackExceptionWhenFetchingWithoutEntityGraph() {
        List<Example> examples = exampleRepository.findAll();

        try {
            // Trigger N+1 query
            examples.get(0).getAuthor().getBrother();
            assert false;
        } catch (NPlusOneQueryException exception) {
            assertThat(exception.getMessage())
                    .isEqualTo("N+1 query detected for entity: com.yannbriancon.utils.entity.DomainUser");
        }
    }

    @Test
    void nPlusOneQueryDetection_isOkWhenFetchingWithEntityGraph() {
        // Do not trigger N+1 query
        List<Example> examples = exampleRepository.getAllBy();
        examples.get(0).getAuthor().getBrother();
    }
}
