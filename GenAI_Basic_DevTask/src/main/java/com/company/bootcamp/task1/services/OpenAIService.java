package com.company.bootcamp.task1.services;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.FunctionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class OpenAIService {
    private final Kernel kernel;

    @Autowired
    public OpenAIService(Kernel kernel) {
        this.kernel =  kernel;
    }

    public String generateResponseUsingPlugin(String prompt) {
        try {
            FunctionResult<String> result = kernel.invoke("Simple Plugin", "processInputAndGenerateResponse");
            return result.getResult();
        } catch (Exception e) {
            return "Error generating response: " + e.getMessage();
        }
    }
}