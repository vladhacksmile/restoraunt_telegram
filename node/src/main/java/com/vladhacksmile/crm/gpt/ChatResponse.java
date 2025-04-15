package com.vladhacksmile.crm.gpt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatResponse {

    private List<Choice> choices;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Choice {

        private int index;

        private GPTMessage message;
    }
}