package com.pvt.bean;

import java.io.Serializable;

public class OcrBean implements Serializable {
    private String log_id;
    private String words_result_num;
    private Result words_result;

    public String getLog_id() {
        return log_id;
    }

    public void setLog_id(String log_id) {
        this.log_id = log_id;
    }

    public String getWords_result_num() {
        return words_result_num;
    }

    public void setWords_result_num(String words_result_num) {
        this.words_result_num = words_result_num;
    }

    public Result getWords_result() {
        return words_result;
    }

    public void setWords_result(Result words_result) {
        this.words_result = words_result;
    }

    public static class Result implements Serializable{
        private String words;

        public String getWords() {
            return words;
        }

        public void setWords(String words) {
            this.words = words;
        }
    }
}
