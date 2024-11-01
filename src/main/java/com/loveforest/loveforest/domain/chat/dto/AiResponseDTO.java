package com.loveforest.loveforest.domain.chat.dto;

public class AiResponseDTO {
    private Response response;

    public static class Response {
        private String answer;

        // Getter and Setter
        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }

    // Getter and Setter
    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }
}