package com.example.myapplication;

import java.util.List;

public class AlBean {
    private long log_id;
    private int words_result_num;
    private List<Pdf> words_result;
    public static class Pdf {
        private String words;
        public String getWords() {
            return words;
        }
    }

    public long getLog_id() {
        return log_id;
    }

    public int getWords_result_num() {
        return words_result_num;
    }

    public List<Pdf> getWords_result() {
        return words_result;
    }
}
