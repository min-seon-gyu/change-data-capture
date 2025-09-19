package demo.chargedatacapture.example;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleRepository exampleRepository;

    public Example save(String name, String description) {
        Example e = new Example(name, description);

        return exampleRepository.save(e);
    }
}
