package demo.chargedatacapture.example;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExampleController {

    private final ExampleService exampleService;

    @PostMapping("/examples")
    public SaveExampleRes save(@RequestBody SaveExampleReq req) {
        Example e = exampleService.save(req.title(), req.description());

        return new SaveExampleRes(e.getId(), e.getTitle(), e.getDescription());
    }
}
